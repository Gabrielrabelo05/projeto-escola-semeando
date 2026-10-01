package br.org.semeando.digital.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.org.semeando.digital.domain.shared.Cpf;
import br.org.semeando.digital.domain.shared.Dinheiro;
import br.org.semeando.digital.domain.shared.DomainException;
import br.org.semeando.digital.domain.shared.IdentidadeInstitucional;
import br.org.semeando.digital.domain.shared.RegraNegocioVioladaException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ValueObjectsTest {

    @Test
    void cpfValidoEhNormalizadoEFormatado() {
        Cpf cpf = Cpf.de("529.982.247-25");
        assertThat(cpf.numero()).isEqualTo("52998224725");
        assertThat(cpf.formatado()).isEqualTo("529.982.247-25");
    }

    @ParameterizedTest
    @ValueSource(strings = {"11111111111", "52998224724", "123", ""})
    void cpfInvalidoEhRejeitado(String numero) {
        assertThatThrownBy(() -> Cpf.de(numero)).isInstanceOf(DomainException.class);
    }

    @Test
    void dinheiroArredondaParaDuasCasasENaoAceitaNegativo() {
        assertThat(Dinheiro.de("10.005").valor()).isEqualByComparingTo("10.01");
        assertThat(Dinheiro.de("375").multiplicar(11)).isEqualTo(Dinheiro.de("4125.00"));
        assertThatThrownBy(() -> Dinheiro.de("-1")).isInstanceOf(DomainException.class);
    }

    @Test
    void rn09IdentidadeInstitucionalDivergenteEhBloqueada() {
        IdentidadeInstitucional.validar(IdentidadeInstitucional.NOME, IdentidadeInstitucional.CNPJ);
        assertThatThrownBy(() -> IdentidadeInstitucional.validar("Centro Educacional Semeando",
                IdentidadeInstitucional.CNPJ))
                .isInstanceOf(RegraNegocioVioladaException.class)
                .hasFieldOrPropertyWithValue("codigo", "RN-09");
    }
}
