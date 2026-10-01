-- Semeando Digital - esquema inicial
-- Identificadores UUID são gerados pelo domínio.

CREATE TABLE usuarios (
    id          UUID PRIMARY KEY,
    nome        VARCHAR(150) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    senha_hash  VARCHAR(100) NOT NULL,
    perfil      VARCHAR(20)  NOT NULL,
    cpf         VARCHAR(11),
    ativo       BOOLEAN      NOT NULL
);

CREATE TABLE responsaveis (
    id          UUID PRIMARY KEY,
    nome        VARCHAR(150) NOT NULL,
    cpf         VARCHAR(11)  NOT NULL UNIQUE,
    email       VARCHAR(150),
    telefone    VARCHAR(20),
    parentesco  VARCHAR(50)  NOT NULL,
    ativo       BOOLEAN      NOT NULL
);

CREATE TABLE alunos (
    id                      UUID PRIMARY KEY,
    nome                    VARCHAR(150) NOT NULL,
    data_nascimento         DATE         NOT NULL,
    responsavel_id          UUID         NOT NULL REFERENCES responsaveis (id),
    cpf_responsavel         VARCHAR(11)  NOT NULL,
    alergias                TEXT,
    tipo_sanguineo          VARCHAR(5),
    necessidades_especiais  TEXT,
    hospital_emergencia     VARCHAR(200),
    foto_url                VARCHAR(500),
    status                  VARCHAR(20)  NOT NULL
);
CREATE INDEX idx_alunos_cpf_responsavel ON alunos (cpf_responsavel);

CREATE TABLE aluno_autorizados (
    aluno_id    UUID         NOT NULL REFERENCES alunos (id),
    ordem       INTEGER      NOT NULL,
    nome        VARCHAR(150) NOT NULL,
    documento   VARCHAR(30)  NOT NULL,
    parentesco  VARCHAR(50)  NOT NULL,
    telefone    VARCHAR(20),
    PRIMARY KEY (aluno_id, ordem)
);

CREATE TABLE professores (
    id                     UUID PRIMARY KEY,
    nome                   VARCHAR(150) NOT NULL,
    cpf                    VARCHAR(11)  NOT NULL UNIQUE,
    email                  VARCHAR(150),
    telefone               VARCHAR(20),
    ativo                  BOOLEAN      NOT NULL,
    rg                     VARCHAR(20),
    formacao_academica     VARCHAR(200),
    carga_horaria_semanal  INTEGER      NOT NULL
);

CREATE TABLE professor_disciplinas (
    professor_id  UUID         NOT NULL REFERENCES professores (id),
    ordem         INTEGER      NOT NULL,
    disciplina    VARCHAR(100) NOT NULL,
    PRIMARY KEY (professor_id, ordem)
);

CREATE TABLE funcionarios (
    id             UUID PRIMARY KEY,
    nome           VARCHAR(150) NOT NULL,
    cpf            VARCHAR(11)  NOT NULL UNIQUE,
    email          VARCHAR(150),
    telefone       VARCHAR(20),
    ativo          BOOLEAN      NOT NULL,
    cargo          VARCHAR(100) NOT NULL,
    setor          VARCHAR(100) NOT NULL,
    perfil_acesso  VARCHAR(20)  NOT NULL
);

CREATE TABLE prestadores (
    id                UUID PRIMARY KEY,
    nome              VARCHAR(150) NOT NULL,
    documento         VARCHAR(14)  NOT NULL,
    servico_prestado  VARCHAR(150) NOT NULL,
    vigencia_inicio   DATE         NOT NULL,
    vigencia_fim      DATE         NOT NULL,
    email             VARCHAR(150),
    telefone          VARCHAR(20),
    ativo             BOOLEAN      NOT NULL
);

CREATE TABLE prestador_documentos (
    prestador_id  UUID         NOT NULL REFERENCES prestadores (id),
    ordem         INTEGER      NOT NULL,
    descricao     VARCHAR(150) NOT NULL,
    PRIMARY KEY (prestador_id, ordem)
);

CREATE TABLE turmas (
    id                    UUID PRIMARY KEY,
    nome                  VARCHAR(100) NOT NULL,
    serie                 VARCHAR(20)  NOT NULL,
    ano_letivo            INTEGER      NOT NULL,
    capacidade_maxima     INTEGER      NOT NULL,
    professor_titular_id  UUID REFERENCES professores (id)
);

CREATE TABLE matriculas (
    id                      UUID PRIMARY KEY,
    aluno_id                UUID        NOT NULL REFERENCES alunos (id),
    turma_id                UUID        NOT NULL REFERENCES turmas (id),
    nivel                   VARCHAR(30) NOT NULL,
    ano_letivo              INTEGER     NOT NULL,
    tipo                    VARCHAR(20) NOT NULL,
    status                  VARCHAR(30) NOT NULL,
    data_solicitacao        DATE        NOT NULL,
    justificativa_rejeicao  TEXT,
    farda_blusa             VARCHAR(10),
    farda_calca             VARCHAR(10),
    farda_tenis             VARCHAR(10),
    cobranca_taxa_id        UUID
);
CREATE INDEX idx_matriculas_aluno ON matriculas (aluno_id);
CREATE INDEX idx_matriculas_turma ON matriculas (turma_id);

CREATE TABLE matricula_documentos (
    matricula_id  UUID        NOT NULL REFERENCES matriculas (id),
    ordem         INTEGER     NOT NULL,
    tipo          VARCHAR(40) NOT NULL,
    arquivo       VARCHAR(500),
    entregue_em   DATE,
    PRIMARY KEY (matricula_id, ordem)
);

CREATE TABLE documentos_vinculo (
    id                    UUID PRIMARY KEY,
    numero                UUID          NOT NULL,
    versao                INTEGER       NOT NULL,
    tipo                  VARCHAR(40)   NOT NULL,
    aluno_id              UUID          NOT NULL REFERENCES alunos (id),
    matricula_id          UUID          NOT NULL REFERENCES matriculas (id),
    ano_letivo            INTEGER       NOT NULL,
    gerado_em             TIMESTAMP WITH TIME ZONE NOT NULL,
    gerado_por            VARCHAR(150)  NOT NULL,
    conteudo              TEXT          NOT NULL,
    hash_documento        VARCHAR(64)   NOT NULL,
    status                VARCHAR(30)   NOT NULL,
    assinante             VARCHAR(150),
    assinado_em           TIMESTAMP WITH TIME ZONE,
    assinatura_ip         VARCHAR(45),
    assinatura_hash       VARCHAR(64),
    valor_parcela         NUMERIC(12, 2),
    numero_parcelas       INTEGER,
    hospital_emergencia   VARCHAR(200),
    nome_socio            VARCHAR(150),
    cpf_socio             VARCHAR(11),
    aceite_estatuto       BOOLEAN,
    valor_taxa_associado  NUMERIC(12, 2),
    UNIQUE (numero, versao)
);

CREATE TABLE vinculo_autorizados (
    vinculo_id  UUID         NOT NULL REFERENCES documentos_vinculo (id),
    ordem       INTEGER      NOT NULL,
    nome        VARCHAR(150) NOT NULL,
    documento   VARCHAR(30)  NOT NULL,
    parentesco  VARCHAR(50)  NOT NULL,
    telefone    VARCHAR(20),
    PRIMARY KEY (vinculo_id, ordem)
);

CREATE TABLE cobrancas (
    id                      UUID PRIMARY KEY,
    aluno_id                UUID           NOT NULL REFERENCES alunos (id),
    tipo                    VARCHAR(20)    NOT NULL,
    mes_referencia          VARCHAR(7)     NOT NULL,
    valor                   NUMERIC(12, 2) NOT NULL,
    vencimento              DATE           NOT NULL,
    status                  VARCHAR(20)    NOT NULL,
    boleto_id_cora          VARCHAR(100) UNIQUE,
    boleto_linha_digitavel  VARCHAR(100),
    boleto_url              VARCHAR(500),
    pago_em                 DATE,
    valor_pago              NUMERIC(12, 2),
    forma_pagamento         VARCHAR(20),
    registrado_por          VARCHAR(150)
);
CREATE INDEX idx_cobrancas_mes ON cobrancas (mes_referencia);
CREATE INDEX idx_cobrancas_aluno ON cobrancas (aluno_id);

CREATE TABLE listas_materiais (
    id               UUID PRIMARY KEY,
    serie            VARCHAR(20) NOT NULL,
    ano_letivo       INTEGER     NOT NULL,
    status           VARCHAR(20) NOT NULL,
    vigencia_inicio  DATE,
    vigencia_fim     DATE,
    publicada_em     DATE
);

CREATE TABLE lista_itens (
    lista_id     UUID         NOT NULL REFERENCES listas_materiais (id),
    ordem        INTEGER      NOT NULL,
    descricao    VARCHAR(200) NOT NULL,
    quantidade   INTEGER      NOT NULL,
    tipo         VARCHAR(20)  NOT NULL,
    link_compra  VARCHAR(500),
    PRIMARY KEY (lista_id, ordem)
);

CREATE TABLE planos_aula (
    id                      UUID PRIMARY KEY,
    professor_id            UUID        NOT NULL REFERENCES professores (id),
    turma_id                UUID        NOT NULL REFERENCES turmas (id),
    data                    DATE        NOT NULL,
    objetivo                TEXT        NOT NULL,
    conteudo                TEXT        NOT NULL,
    metodologia             TEXT        NOT NULL,
    avaliacao               TEXT,
    recursos                TEXT,
    status                  VARCHAR(20) NOT NULL,
    comentario_coordenacao  TEXT
);

CREATE TABLE diarios_classe (
    id                   UUID PRIMARY KEY,
    professor_id         UUID NOT NULL REFERENCES professores (id),
    turma_id             UUID NOT NULL REFERENCES turmas (id),
    data                 DATE NOT NULL,
    conteudo_ministrado  TEXT NOT NULL,
    UNIQUE (turma_id, data)
);

CREATE TABLE diario_presencas (
    diario_id  UUID    NOT NULL REFERENCES diarios_classe (id),
    ordem      INTEGER NOT NULL,
    aluno_id   UUID    NOT NULL REFERENCES alunos (id),
    presente   BOOLEAN NOT NULL,
    PRIMARY KEY (diario_id, ordem)
);

CREATE TABLE planejamentos_diarios (
    id                      UUID PRIMARY KEY,
    professor_id            UUID NOT NULL REFERENCES professores (id),
    turma_id                UUID NOT NULL REFERENCES turmas (id),
    data                    DATE NOT NULL,
    roteiro                 TEXT NOT NULL,
    comentario_coordenacao  TEXT
);

CREATE TABLE eventos_calendario (
    id      UUID PRIMARY KEY,
    titulo  VARCHAR(200) NOT NULL,
    tipo    VARCHAR(30)  NOT NULL,
    inicio  DATE         NOT NULL,
    fim     DATE         NOT NULL
);

CREATE TABLE formacoes_continuadas (
    id             UUID PRIMARY KEY,
    professor_id   UUID         NOT NULL REFERENCES professores (id),
    tema           VARCHAR(200) NOT NULL,
    data           DATE         NOT NULL,
    carga_horaria  INTEGER      NOT NULL,
    instituicao    VARCHAR(200) NOT NULL
);

CREATE TABLE obrigacoes (
    id              UUID PRIMARY KEY,
    tipo            VARCHAR(30)  NOT NULL,
    nome            VARCHAR(200) NOT NULL,
    ano_referencia  INTEGER      NOT NULL,
    bimestre        INTEGER,
    prazo           DATE         NOT NULL,
    status          VARCHAR(20)  NOT NULL,
    responsavel     VARCHAR(150) NOT NULL,
    concluida_em    DATE
);

CREATE TABLE obrigacao_checklist (
    obrigacao_id  UUID         NOT NULL REFERENCES obrigacoes (id),
    ordem         INTEGER      NOT NULL,
    descricao     VARCHAR(200) NOT NULL,
    concluido     BOOLEAN      NOT NULL,
    PRIMARY KEY (obrigacao_id, ordem)
);

CREATE TABLE obrigacao_comprovantes (
    obrigacao_id  UUID         NOT NULL REFERENCES obrigacoes (id),
    ordem         INTEGER      NOT NULL,
    nome_arquivo  VARCHAR(255) NOT NULL,
    caminho       VARCHAR(500) NOT NULL,
    enviado_em    TIMESTAMP WITH TIME ZONE NOT NULL,
    enviado_por   VARCHAR(150) NOT NULL,
    PRIMARY KEY (obrigacao_id, ordem)
);

CREATE TABLE obrigacao_alertas (
    obrigacao_id  UUID        NOT NULL REFERENCES obrigacoes (id),
    nivel         VARCHAR(20) NOT NULL,
    PRIMARY KEY (obrigacao_id, nivel)
);

CREATE TABLE auditoria (
    id           UUID PRIMARY KEY,
    usuario      VARCHAR(150) NOT NULL,
    acao         VARCHAR(20)  NOT NULL,
    entidade     VARCHAR(50)  NOT NULL,
    entidade_id  VARCHAR(100) NOT NULL,
    detalhes     TEXT,
    ocorrido_em  TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX idx_auditoria_entidade ON auditoria (entidade, entidade_id);

CREATE TABLE notificacoes_internas (
    id             UUID PRIMARY KEY,
    perfil_destino VARCHAR(20)  NOT NULL,
    titulo         VARCHAR(200) NOT NULL,
    mensagem       TEXT         NOT NULL,
    criada_em      TIMESTAMP WITH TIME ZONE NOT NULL,
    lida           BOOLEAN      NOT NULL
);
