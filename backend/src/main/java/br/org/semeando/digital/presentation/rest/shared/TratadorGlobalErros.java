package br.org.semeando.digital.presentation.rest.shared;

import br.org.semeando.digital.application.shared.AcessoNegadoException;
import br.org.semeando.digital.domain.shared.DomainException;
import br.org.semeando.digital.domain.shared.RecursoNaoEncontradoException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class TratadorGlobalErros {

    private static final Logger LOG = LoggerFactory.getLogger(TratadorGlobalErros.class);

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    ResponseEntity<ErroResponse> naoEncontrado(RecursoNaoEncontradoException e) {
        return resposta(HttpStatus.NOT_FOUND, e.codigo(), e.getMessage());
    }

    @ExceptionHandler({AcessoNegadoException.class, AccessDeniedException.class})
    ResponseEntity<ErroResponse> acessoNegado(RuntimeException e) {
        return resposta(HttpStatus.FORBIDDEN, "ACESSO_NEGADO", e.getMessage());
    }

    /** Violações de regra de negócio e invariantes do domínio. */
    @ExceptionHandler(DomainException.class)
    ResponseEntity<ErroResponse> dominio(DomainException e) {
        HttpStatus status = "CREDENCIAIS_INVALIDAS".equals(e.codigo()) ? HttpStatus.UNAUTHORIZED
                : HttpStatus.UNPROCESSABLE_CONTENT;
        return resposta(status, e.codigo(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException e) {
        Map<String, String> campos = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(erro -> campos.put(erro.getField(), erro.getDefaultMessage()));
        return ResponseEntity.badRequest()
                .body(new ErroResponse("VALIDACAO", "Dados inválidos", campos, Instant.now()));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class})
    ResponseEntity<ErroResponse> requisicaoInvalida(Exception e) {
        return resposta(HttpStatus.BAD_REQUEST, "REQUISICAO_INVALIDA", "Requisição inválida: verifique os campos");
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErroResponse> inesperado(Exception e) {
        LOG.error("Erro inesperado", e);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "ERRO_INTERNO", "Erro inesperado. Tente novamente.");
    }

    private static ResponseEntity<ErroResponse> resposta(HttpStatus status, String codigo, String mensagem) {
        return ResponseEntity.status(status).body(ErroResponse.de(codigo, mensagem));
    }
}
