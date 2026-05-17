package com.sivebo.ms_ventas.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig {
        
        @Value("${ms.usuarios.url}")
        private String usuariosBaseUrl;

        @Value("${ms.sucursales.url}")
        private String sucursalBaseUrl;

        @Bean
        public WebClient.Builder webClientBuilder() {
                return WebClient.builder();
        }

        @Bean
        public WebClient usuariosoWebClient(WebClient.Builder webClientBuilder) {
                return webClientBuilder
                        .baseUrl(usuariosBaseUrl)
                        .build();
        }

        @Bean
        public WebClient sucursalWebClient(WebClient.Builder webClientBuilder) {
                return webClientBuilder
                        .baseUrl(sucursalBaseUrl)
                        .build();
        }

}
