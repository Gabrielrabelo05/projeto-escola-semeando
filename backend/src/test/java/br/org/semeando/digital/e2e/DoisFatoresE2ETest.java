package br.org.semeando.digital.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.org.semeando.digital.application.auth.AutenticacaoUseCase;
import br.org.semeando.digital.application.shared.Ator;
import br.org.semeando.digital.domain.usuario.Perfil;
import br.org.semeando.digital.domain.usuario.Totp;
import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** Verificação em duas etapas (RNF-01): obrigatória para o Administrador e opcional para os demais. */
@SpringBootTest(properties = "semeando.seguranca.dois-fatores-obrigatorio=ADMIN")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DoisFatoresE2ETest {

    private static final String SENHA = "Semeando@2026";
    private static final String CSRF = "X-Requested-With";

    @Autowired
    MockMvc mvc;

    @Autowired
    AutenticacaoUseCase autenticacao;

    @Test
    void administradorConfiguraNoPrimeiroAcessoEDepoisEntraComCodigoOuRecuperacao() throws Exception {
        autenticacao.criar("Renata Moura", "renata.2fa@example.com", SENHA, Perfil.ADMIN, null, Ator.sistema());

        MvcResult login = entrar("renata.2fa@example.com").andExpect(status().isOk())
                .andExpect(jsonPath("$.etapa").value("CONFIGURAR_DOIS_FATORES"))
                .andExpect(jsonPath("$.usuario").doesNotExist())
                .andReturn();
        assertThat(login.getResponse().getCookie("semeando_sessao")).isNull();
        assertThat(login.getResponse().getHeader(HttpHeaders.SET_COOKIE))
                .contains("semeando_desafio=", "HttpOnly", "SameSite=Strict", "Path=/api/auth/desafio");
        String desafio = login.getResponse().getCookie("semeando_desafio").getValue();

        // O desafio só prova a senha: não serve como sessão nem sem o header anti-CSRF.
        mvc.perform(get("/api/usuarios").header(HttpHeaders.AUTHORIZATION, "Bearer " + desafio))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/desafio/configuracao").cookie(new Cookie("semeando_desafio", desafio)))
                .andExpect(status().isUnauthorized());

        String configuracao = mvc.perform(comDesafio(post("/api/auth/desafio/configuracao"), desafio))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uri").value(org.hamcrest.Matchers.startsWith("otpauth://totp/")))
                .andReturn().getResponse().getContentAsString();
        String segredo = JsonPath.read(configuracao, "$.segredo");
        long passo = Totp.passo(Instant.now());

        codigo(post("/api/auth/desafio/ativacao"), desafio, "000000").andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.codigo").value("CODIGO_INVALIDO"));
        MvcResult ativacao = codigo(post("/api/auth/desafio/ativacao"), desafio, Totp.codigo(segredo, passo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.doisFatoresAtivo").value(true))
                .andReturn();
        List<String> recuperacao = JsonPath.read(ativacao.getResponse().getContentAsString(), "$.codigosRecuperacao");
        assertThat(recuperacao).hasSize(10).allMatch(c -> c.matches("[A-Z2-9]{5}-[A-Z2-9]{5}"));
        mvc.perform(comSessao(get("/api/usuarios"), sessao(ativacao))).andExpect(status().isOk());

        // Próximo login: código novo do aplicativo; o mesmo código não pode ser reusado.
        String desafio2 = desafio(entrar("renata.2fa@example.com").andExpect(jsonPath("$.etapa").value("CODIGO")));
        String proximo = Totp.codigo(segredo, passo + 1);
        MvcResult comCodigo = codigo(post("/api/auth/desafio/codigo"), desafio2, proximo)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.email").value("renata.2fa@example.com"))
                .andReturn();
        mvc.perform(comSessao(get("/api/usuarios"), sessao(comCodigo))).andExpect(status().isOk());

        String desafio3 = desafio(entrar("renata.2fa@example.com"));
        codigo(post("/api/auth/desafio/codigo"), desafio3, proximo).andExpect(status().isUnprocessableContent());
        codigo(post("/api/auth/desafio/codigo"), desafio3, recuperacao.getFirst().toLowerCase())
                .andExpect(status().isOk());
        codigo(post("/api/auth/desafio/codigo"), desafio(entrar("renata.2fa@example.com")), recuperacao.getFirst())
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void cincoCodigosErradosBloqueiamAEtapaDoCodigo() throws Exception {
        autenticacao.criar("Marcos Teixeira", "marcos.2fa@example.com", SENHA, Perfil.ADMIN, null, Ator.sistema());
        String desafio = desafio(entrar("marcos.2fa@example.com"));
        String segredo = JsonPath.read(mvc.perform(comDesafio(post("/api/auth/desafio/configuracao"), desafio))
                .andReturn().getResponse().getContentAsString(), "$.segredo");
        codigo(post("/api/auth/desafio/ativacao"), desafio, Totp.codigo(segredo, Totp.passo(Instant.now())))
                .andExpect(status().isOk());

        String novoDesafio = desafio(entrar("marcos.2fa@example.com"));
        for (int i = 0; i < 5; i++) {
            codigo(post("/api/auth/desafio/codigo"), novoDesafio, "123456");
        }
        codigo(post("/api/auth/desafio/codigo"), novoDesafio, "654321")
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void outrosPerfisAtivamPorContaPropriaEAsSessoesAntigasCaem() throws Exception {
        autenticacao.criar("Paula Reis", "paula.2fa@example.com", SENHA, Perfil.SECRETARIA, null, Ator.sistema());
        MvcResult login = entrar("paula.2fa@example.com").andExpect(jsonPath("$.etapa").value("AUTENTICADO"))
                .andReturn();
        String antiga = sessao(login);
        mvc.perform(comSessao(get("/api/auth/dois-fatores"), antiga))
                .andExpect(jsonPath("$.ativo").value(false))
                .andExpect(jsonPath("$.obrigatorio").value(false));

        String segredo = JsonPath.read(mvc.perform(comSessao(post("/api/auth/dois-fatores/configuracao"), antiga))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), "$.segredo");
        long passo = Totp.passo(Instant.now());
        MvcResult ativacao = mvc.perform(comSessao(post("/api/auth/dois-fatores/ativacao"), antiga)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"%s\"}".formatted(Totp.codigo(segredo, passo))))
                .andExpect(status().isOk()).andReturn();

        mvc.perform(comSessao(get("/api/auth/sessao"), antiga)).andExpect(status().isUnauthorized());
        String nova = sessao(ativacao);
        mvc.perform(comSessao(get("/api/auth/dois-fatores"), nova))
                .andExpect(jsonPath("$.ativo").value(true))
                .andExpect(jsonPath("$.codigosRecuperacaoRestantes").value(10));
        entrar("paula.2fa@example.com").andExpect(jsonPath("$.etapa").value("CODIGO"));

        MvcResult desativacao = mvc.perform(comSessao(post("/api/auth/dois-fatores/desativacao"), nova)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"%s\"}".formatted(Totp.codigo(segredo, passo + 1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessaoEncerrada").value(false))
                .andReturn();
        mvc.perform(comSessao(get("/api/auth/sessao"), sessao(desativacao))).andExpect(status().isOk());
        entrar("paula.2fa@example.com").andExpect(jsonPath("$.etapa").value("AUTENTICADO"));
    }

    private ResultActions entrar(String email) throws Exception {
        return mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"%s\",\"senha\":\"%s\"}".formatted(email, SENHA)));
    }

    private ResultActions codigo(MockHttpServletRequestBuilder req, String desafio, String codigo) throws Exception {
        return mvc.perform(comDesafio(req, desafio).contentType(MediaType.APPLICATION_JSON)
                .content("{\"codigo\":\"%s\"}".formatted(codigo)));
    }

    private static String desafio(ResultActions login) {
        return login.andReturn().getResponse().getCookie("semeando_desafio").getValue();
    }

    private static String sessao(MvcResult resultado) {
        return resultado.getResponse().getCookie("semeando_sessao").getValue();
    }

    private static MockHttpServletRequestBuilder comDesafio(MockHttpServletRequestBuilder req, String desafio) {
        return req.cookie(new Cookie("semeando_desafio", desafio)).header(CSRF, "XMLHttpRequest");
    }

    private static MockHttpServletRequestBuilder comSessao(MockHttpServletRequestBuilder req, String sessao) {
        return req.cookie(new Cookie("semeando_sessao", sessao)).header(CSRF, "XMLHttpRequest");
    }
}
