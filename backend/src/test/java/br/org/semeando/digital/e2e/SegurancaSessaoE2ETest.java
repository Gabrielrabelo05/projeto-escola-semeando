package br.org.semeando.digital.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
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

/** Sessão em cookie HttpOnly, CSRF, revogação imediata e bloqueio por força bruta (RNF-01). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SegurancaSessaoE2ETest {

    private static final String SENHA = "Semeando@2026";

    @Autowired
    MockMvc mvc;

    @Test
    void loginDevolveCookieHttpOnlySecureSameSiteStrictESemTokenNoCorpo() throws Exception {
        MvcResult resultado = entrar("secretaria@semeando.dev", SENHA)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.usuario.email").value("secretaria@semeando.dev"))
                .andReturn();

        String setCookie = resultado.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        assertThat(setCookie).contains("semeando_sessao=", "HttpOnly", "Secure", "SameSite=Strict", "Path=/api");
        mvc.perform(comCookie(get("/api/auth/sessao"), cookie(resultado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("SECRETARIA"));
    }

    @Test
    void alteracaoComCookieSemHeaderAntiCsrfEhRecusada() throws Exception {
        String sessao = cookie(entrar("financeiro@semeando.dev", SENHA).andReturn());

        mvc.perform(post("/api/cobrancas/gerar-mensal").cookie(new Cookie("semeando_sessao", sessao))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"mes\":\"2030-01\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(comCookie(post("/api/cobrancas/gerar-mensal"), sessao)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"mes\":\"2030-01\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void cookieVencidoNaoAtrapalhaRotasPublicasEEhApagado() throws Exception {
        mvc.perform(get("/api/publico/turmas").cookie(new Cookie("semeando_sessao", "token-invalido")))
                .andExpect(status().isOk());
        String apagado = mvc.perform(comCookie(get("/api/alunos"), "token-invalido"))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getHeader(HttpHeaders.SET_COOKIE);
        assertThat(apagado).contains("semeando_sessao=", "Max-Age=0");
    }

    @Test
    void inativarUsuarioDerrubaASessaoNaHora() throws Exception {
        String admin = cookie(entrar("admin@semeando.dev", SENHA).andReturn());
        String usuarioId = criarComSenha(admin, "Joana Teste Inativacao", "joana.inativacao@example.com");
        String sessaoJoana = cookie(entrar("joana.inativacao@example.com", SENHA).andReturn());
        mvc.perform(comCookie(get("/api/auth/sessao"), sessaoJoana)).andExpect(status().isOk());

        mvc.perform(comCookie(patch("/api/usuarios/" + usuarioId + "/inativar"), admin)).andExpect(status().isOk());

        mvc.perform(comCookie(get("/api/auth/sessao"), sessaoJoana)).andExpect(status().isUnauthorized());
        entrar("joana.inativacao@example.com", SENHA).andExpect(status().isUnauthorized());
    }

    @Test
    void cincoSenhasErradasBloqueiamOLoginPorQuinzeMinutos() throws Exception {
        String admin = cookie(entrar("admin@semeando.dev", SENHA).andReturn());
        criarComSenha(admin, "Bruno Teste Bloqueio", "bruno.bloqueio@example.com");
        for (int i = 0; i < 5; i++) {
            entrar("bruno.bloqueio@example.com", "senha-errada").andExpect(status().isUnauthorized());
        }
        entrar("bruno.bloqueio@example.com", SENHA)
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.codigo").value("MUITAS_TENTATIVAS"));
    }

    /** Cria o acesso por convite e define a senha pelo link; devolve o id do usuário. */
    private String criarComSenha(String admin, String nome, String email) throws Exception {
        String convite = mvc.perform(comCookie(post("/api/usuarios"), admin).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"%s","email":"%s","perfil":"SECRETARIA"}""".formatted(nome, email)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String link = JsonPath.read(convite, "$.link");
        String token = link.substring(link.indexOf("token=") + "token=".length());
        mvc.perform(post("/api/publico/convites/" + token + "/senha").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senha\":\"%s\"}".formatted(SENHA)))
                .andExpect(status().isOk());
        return JsonPath.read(convite, "$.usuario.id");
    }

    private ResultActions entrar(String email, String senha) throws Exception {
        return mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"%s\",\"senha\":\"%s\"}".formatted(email, senha)));
    }

    private static String cookie(MvcResult resultado) {
        return resultado.getResponse().getCookie("semeando_sessao").getValue();
    }

    private static MockHttpServletRequestBuilder comCookie(MockHttpServletRequestBuilder req, String sessao) {
        return req.cookie(new Cookie("semeando_sessao", sessao)).header("X-Requested-With", "XMLHttpRequest");
    }
}
