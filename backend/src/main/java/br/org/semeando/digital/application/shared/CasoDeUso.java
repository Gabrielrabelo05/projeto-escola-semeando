package br.org.semeando.digital.application.shared;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca uma classe como caso de uso da aplicação. A infraestrutura registra estas classes
 * como beans e envolve cada chamada pública em uma transação – a camada de aplicação
 * permanece livre de dependências de framework.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CasoDeUso {
}
