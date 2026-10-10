package br.org.semeando.digital.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Teste ponta a ponta da API: percorre o fluxo de matrícula da seção 9 do levantamento
 * (pré-matrícula → aprovação → assinatura → pagamento via webhook → ATIVO → cobrança mensal),
 * verificando RBAC e regras de negócio pelo contrato HTTP.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MatriculaE2ETest {

    private static final String SENHA = "Semeando@2026";
    private static final String CPF_RESPONSAVEL = "39053344705";

    @Autowired
    MockMvc mvc;

    @Test
    void fluxoCompletoDeMatriculaDoEnsinoFundamental() throws Exception {
        String admin = login("admin@semeando.dev");
        String secretaria = login("secretaria@semeando.dev");
        String financeiro = login("financeiro@semeando.dev");
        String turmaId = turmaDaSerie("SEGUNDO_ANO");

        // RF-01: pré-matrícula pública
        String matricula = mvc.perform(post("/api/pre-matriculas").contentType(MediaType.APPLICATION_JSON)
                        .content(preMatricula("Bruno Costa Ferreira", CPF_RESPONSAVEL, turmaId, true)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("AGUARDANDO_ANALISE"))
                .andExpect(jsonPath("$.documentos.length()").value(6))
                .andReturn().getResponse().getContentAsString();
        String matriculaId = JsonPath.read(matricula, "$.id");
        String alunoId = JsonPath.read(matricula, "$.alunoId");

        criarUsuarioResponsavel(admin);
        String responsavel = login("bruno.pai@exemplo.dev");

        // RF-35: responsável não aprova matrícula
        mvc.perform(autenticado(post("/api/matriculas/" + matriculaId + "/aprovar"), responsavel))
                .andExpect(status().isForbidden());

        // RF-02 + RN-01: aprovação gera contrato de prestação de serviços com valores oficiais
        String contrato = mvc.perform(autenticado(post("/api/matriculas/" + matriculaId + "/aprovar"), secretaria))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("CONTRATO_PRESTACAO_SERVICOS"))
                .andExpect(jsonPath("$.valorParcela").value(375.0))
                .andExpect(jsonPath("$.valorTotal").value(4125.0))
                .andReturn().getResponse().getContentAsString();
        String contratoId = JsonPath.read(contrato, "$.id");

        // RN-02: valores divergentes bloqueiam a geração
        mvc.perform(autenticado(post("/api/contratos"), secretaria).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"matriculaId":"%s","valorParcela":350.00,"numeroParcelas":11}
                                """.formatted(matriculaId)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.codigo").value("RN-02"));

        // Outro responsável não acessa o contrato
        mvc.perform(autenticado(get("/api/contratos/" + contratoId), login("responsavel@semeando.dev")))
                .andExpect(status().isForbidden());

        // RF-15: assinatura eletrônica pelo responsável
        mvc.perform(autenticado(post("/api/contratos/" + contratoId + "/assinar"), responsavel)
                        .header("X-Forwarded-For", "200.10.20.30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ASSINADO"))
                .andExpect(jsonPath("$.assinatura.enderecoIp").value("200.10.20.30"));

        // RF-28: webhook do Cora confirma o pagamento da primeira parcela, que vale como sinal do contrato
        String boletos = mvc.perform(autenticado(get("/api/cobrancas/aluno/" + alunoId), financeiro))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("MENSALIDADE"))
                .andExpect(jsonPath("$[0].valor").value(375.0))
                .andExpect(jsonPath("$[0].valorComDesconto").value(337.0))
                .andExpect(jsonPath("$[0].valorDevido").value(337.0))
                .andReturn().getResponse().getContentAsString();
        String idCora = JsonPath.read(boletos, "$[0].idBoletoCora");
        String evento = """
                {"id":"%s","status":"PAID","paid_at":null,"total_paid":337.00}""".formatted(idCora);

        mvc.perform(post("/api/webhooks/cora").contentType(MediaType.APPLICATION_JSON).content(evento))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/webhooks/cora").header("X-Webhook-Secret", "dev-webhook-secret")
                        .contentType(MediaType.APPLICATION_JSON).content(evento))
                .andExpect(status().isNoContent());

        // Passo 13: matrícula ATIVA
        mvc.perform(autenticado(get("/api/matriculas/" + matriculaId), secretaria))
                .andExpect(jsonPath("$.status").value("ATIVA"))
                .andExpect(jsonPath("$.aluno.status").value("ATIVO"));

        // RF-24 + RN-07: mensalidade gerada para o aluno ativo com contrato assinado
        String mes = YearMonth.now().plusMonths(1).toString();
        mvc.perform(autenticado(post("/api/cobrancas/gerar-mensal"), financeiro)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"mes\":\"" + mes + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.geradas", hasItem("Bruno Costa Ferreira")));

        // Portal do responsável
        mvc.perform(autenticado(get("/api/portal/filhos"), responsavel))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].aluno.nome").value("Bruno Costa Ferreira"))
                .andExpect(jsonPath("$[0].statusMatricula").value("ATIVA"));

        // RF-36: as ações ficam registradas na auditoria
        String auditoria = mvc.perform(autenticado(get("/api/auditoria").param("entidade", "Contrato"), admin))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        List<String> acoes = JsonPath.read(auditoria, "$[*].acao");
        assertThat(acoes).contains("ASSINATURA");
    }

    @Test
    void rn06PreMatriculaSemAutorizadosEhRecusada() throws Exception {
        mvc.perform(post("/api/pre-matriculas").contentType(MediaType.APPLICATION_JSON)
                        .content(preMatricula("Sem Autorizados", "11144477735", turmaDaSerie("QUARTO_ANO"), false)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos['aluno.autorizados']").exists());
    }

    @Test
    void autenticacaoERbac() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@semeando.dev\",\"senha\":\"errada\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/alunos")).andExpect(status().isUnauthorized());
        mvc.perform(autenticado(get("/api/cobrancas/inadimplencia"), login("professora@semeando.dev")))
                .andExpect(status().isForbidden());
        mvc.perform(autenticado(get("/api/pedagogico/calendario").param("ano", "2026"),
                        login("responsavel@semeando.dev")))
                .andExpect(status().isOk());
    }

    private String login(String email) throws Exception {
        return mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"senha\":\"%s\"}".formatted(email, SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andReturn().getResponse().getCookie("semeando_sessao").getValue();
    }

    private String turmaDaSerie(String serie) throws Exception {
        String turmas = mvc.perform(get("/api/publico/turmas")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        List<String> ids = JsonPath.read(turmas, "$[?(@.serie == '" + serie + "')].id");
        return ids.getFirst();
    }

    /** RF-35: o acesso é criado por convite; a própria pessoa define a senha pelo link de uso único. */
    private void criarUsuarioResponsavel(String admin) throws Exception {
        String convite = mvc.perform(autenticado(post("/api/usuarios"), admin).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Pai E2E","email":"bruno.pai@exemplo.dev","perfil":"RESPONSAVEL","cpf":"%s"}
                                """.formatted(CPF_RESPONSAVEL)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario.acessoPendente").value(true))
                .andReturn().getResponse().getContentAsString();
        String link = JsonPath.read(convite, "$.link");
        String token = link.substring(link.indexOf("token=") + "token=".length());

        // Sem senha definida, o login é recusado
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"bruno.pai@exemplo.dev\",\"senha\":\"qualquer-coisa\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/publico/convites/" + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("bruno.pai@exemplo.dev"));
        String senha = "{\"senha\":\"%s\"}".formatted(SENHA);
        mvc.perform(post("/api/publico/convites/" + token + "/senha").contentType(MediaType.APPLICATION_JSON)
                        .content(senha))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acessoPendente").value(false));
        // O link só vale uma vez
        mvc.perform(post("/api/publico/convites/" + token + "/senha").contentType(MediaType.APPLICATION_JSON)
                        .content(senha))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.codigo").value("CONVITE_INVALIDO"));
    }

    /** Como o navegador: cookie HttpOnly de sessão + header anti-CSRF. */
    private static MockHttpServletRequestBuilder autenticado(MockHttpServletRequestBuilder req, String token) {
        return req.cookie(new Cookie("semeando_sessao", token)).header("X-Requested-With", "XMLHttpRequest");
    }

    private static String preMatricula(String aluno, String cpf, String turmaId, boolean comAutorizados) {
        String autorizados = comAutorizados
                ? "[{\"nome\":\"Tia Sintética\",\"documento\":\"RG 999\",\"parentesco\":\"Tia\",\"telefone\":null}]"
                : "[]";
        return """
                {
                  "aluno": {"nome":"%s","dataNascimento":"2019-02-10","alergias":"Lactose","tipoSanguineo":"A+",
                            "necessidadesEspeciais":null,"hospitalEmergencia":"Hospital São Domingos",
                            "autorizados":%s},
                  "responsavel": {"nome":"Pai E2E","cpf":"%s","email":"bruno.pai@exemplo.dev",
                                  "telefone":"98911112222","parentesco":"Pai"},
                  "turmaId":"%s"
                }""".formatted(aluno, autorizados, cpf, turmaId);
    }
}
