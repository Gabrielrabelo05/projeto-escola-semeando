package br.org.semeando.digital;

import org.springframework.boot.SpringApplication;

/**
 * Sobe a API com o perfil "test" (H2 em memória + dados sintéticos), sem Docker.
 * Usado pelos testes end-to-end do frontend: {@code ./mvnw spring-boot:test-run}.
 */
public class ServidorLocalTeste {

    public static void main(String[] args) {
        SpringApplication.from(SemeandoDigitalApplication::main)
                .withAdditionalProfiles("test")
                .run(args);
    }
}
