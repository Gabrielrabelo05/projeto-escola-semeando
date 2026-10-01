package br.org.semeando.digital.presentation.rest.shared;

/** Expressões de autorização por perfil (RF-35 – RBAC), conforme a seção 3 do levantamento. */
public final class Perfis {

    public static final String ADMIN = "hasRole('ADMIN')";
    public static final String GESTAO = "hasAnyRole('ADMIN','DIRETOR')";
    public static final String SECRETARIA = "hasAnyRole('ADMIN','DIRETOR','SECRETARIA')";
    public static final String FINANCEIRO = "hasAnyRole('ADMIN','DIRETOR','FINANCEIRO')";
    public static final String FINANCEIRO_OU_SECRETARIA = "hasAnyRole('ADMIN','DIRETOR','FINANCEIRO','SECRETARIA')";
    public static final String PROFESSOR = "hasAnyRole('ADMIN','DIRETOR','PROFESSOR')";
    public static final String EQUIPE = "hasAnyRole('ADMIN','DIRETOR','SECRETARIA','FINANCEIRO','PROFESSOR')";
    public static final String RESPONSAVEL = "hasRole('RESPONSAVEL')";
    public static final String SECRETARIA_OU_RESPONSAVEL = "hasAnyRole('ADMIN','DIRETOR','SECRETARIA','RESPONSAVEL')";
    public static final String SECRETARIA_OU_PROFESSOR = "hasAnyRole('ADMIN','DIRETOR','SECRETARIA','PROFESSOR')";
    public static final String OBRIGACOES = "hasAnyRole('ADMIN','DIRETOR','SECRETARIA','PRESTADOR')";
    public static final String TODOS = "isAuthenticated()";

    private Perfis() {
    }
}
