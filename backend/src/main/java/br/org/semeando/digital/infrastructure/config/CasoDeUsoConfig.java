package br.org.semeando.digital.infrastructure.config;

import br.org.semeando.digital.application.shared.CasoDeUso;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Role;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.interceptor.MatchAlwaysTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

/**
 * Registra as classes anotadas com {@link CasoDeUso} como beans e envolve todos os seus métodos
 * públicos em uma transação. Assim a camada de aplicação não depende do Spring.
 */
@Configuration
@ComponentScan(basePackages = "br.org.semeando.digital.application", useDefaultFilters = false,
        includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = CasoDeUso.class))
public class CasoDeUsoConfig {

    /** O gerenciador de transações é resolvido de forma tardia pelo próprio interceptor (BeanFactoryAware). */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    static TransactionInterceptor casoDeUsoTransactionInterceptor() {
        var atributos = new MatchAlwaysTransactionAttributeSource();
        atributos.setTransactionAttribute(new DefaultTransactionAttribute());
        var interceptor = new TransactionInterceptor();
        interceptor.setTransactionAttributeSource(atributos);
        return interceptor;
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    static Advisor casoDeUsoTransactionAdvisor(TransactionInterceptor casoDeUsoTransactionInterceptor) {
        return new DefaultPointcutAdvisor(new AnnotationMatchingPointcut(CasoDeUso.class, true),
                casoDeUsoTransactionInterceptor);
    }
}
