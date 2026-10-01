package br.org.semeando.digital.infrastructure.config;

import br.org.semeando.digital.infrastructure.security.AtorArgumentResolver;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AtorArgumentResolver atorArgumentResolver;

    public WebConfig(AtorArgumentResolver atorArgumentResolver) {
        this.atorArgumentResolver = atorArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(atorArgumentResolver);
    }
}
