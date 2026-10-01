package br.org.semeando.digital.infrastructure.persistence.cobranca;

import br.org.semeando.digital.domain.cobranca.Cobranca;
import br.org.semeando.digital.domain.cobranca.CobrancaRepository;
import br.org.semeando.digital.domain.cobranca.StatusCobranca;
import br.org.semeando.digital.domain.cobranca.TipoCobranca;
import br.org.semeando.digital.domain.shared.MesReferencia;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class CobrancaRepositoryAdapter implements CobrancaRepository {

    private final CobrancaJpaRepository jpa;

    CobrancaRepositoryAdapter(CobrancaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Cobranca salvar(Cobranca cobranca) {
        return jpa.save(CobrancaEntity.de(cobranca)).paraDominio();
    }

    @Override
    public Optional<Cobranca> buscarPorId(UUID id) {
        return jpa.findById(id).map(CobrancaEntity::paraDominio);
    }

    @Override
    public Optional<Cobranca> buscarPorIdCora(String idCora) {
        return jpa.findByBoletoIdCora(idCora).map(CobrancaEntity::paraDominio);
    }

    @Override
    public boolean existe(UUID alunoId, TipoCobranca tipo, MesReferencia mes) {
        return jpa.existsByAlunoIdAndTipoAndMesReferenciaAndStatusNot(alunoId, tipo, mes.toString(),
                StatusCobranca.CANCELADA);
    }

    @Override
    public List<Cobranca> listarPorMes(MesReferencia mes) {
        return jpa.findByMesReferenciaOrderByVencimento(mes.toString()).stream().map(CobrancaEntity::paraDominio)
                .toList();
    }

    @Override
    public List<Cobranca> listarPorAno(int ano) {
        return jpa.findByMesReferenciaStartingWithOrderByVencimento(ano + "-").stream()
                .map(CobrancaEntity::paraDominio).toList();
    }

    @Override
    public List<Cobranca> listarPorAluno(UUID alunoId) {
        return jpa.findByAlunoIdOrderByVencimentoDesc(alunoId).stream().map(CobrancaEntity::paraDominio).toList();
    }

    @Override
    public List<Cobranca> listarEmAberto() {
        return jpa.findByStatusOrderByVencimento(StatusCobranca.EMITIDA).stream().map(CobrancaEntity::paraDominio)
                .toList();
    }
}
