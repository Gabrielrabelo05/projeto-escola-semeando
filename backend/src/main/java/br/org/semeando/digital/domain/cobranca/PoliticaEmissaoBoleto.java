package br.org.semeando.digital.domain.cobranca;

import br.org.semeando.digital.domain.aluno.Aluno;
import br.org.semeando.digital.domain.contrato.DocumentoVinculo;
import br.org.semeando.digital.domain.shared.RegraNegocioVioladaException;
import java.util.Optional;

/** RN-07: boletos recorrentes só podem ser gerados para alunos ATIVOS com contrato/ficha de sócio assinado. */
public final class PoliticaEmissaoBoleto {

    private PoliticaEmissaoBoleto() {
    }

    public static void verificar(Aluno aluno, Optional<DocumentoVinculo> vinculo) {
        if (!aluno.ativo()) {
            throw new RegraNegocioVioladaException("RN-07", "Aluno " + aluno.nome() + " não está ativo");
        }
        if (vinculo.isEmpty() || !vinculo.get().assinado()) {
            throw new RegraNegocioVioladaException("RN-07",
                    "Aluno " + aluno.nome() + " não possui contrato/ficha de sócio assinado");
        }
    }

    public static boolean permite(Aluno aluno, Optional<DocumentoVinculo> vinculo) {
        return aluno.ativo() && vinculo.map(DocumentoVinculo::assinado).orElse(false);
    }
}
