package com.sivebo.ms_ventas.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig {

        @Bean
        @LoadBalanced
        public WebClient.Builder webClientBuilder() {
                return WebClient.builder();
        }

        @Bean
        public WebClient usuarioWebClient(WebClient.Builder webClientBuilder) {
                return webClientBuilder.baseUrl("http://ms-auth").build();
        }

        @Bean
        public WebClient sucursalWebClient(WebClient.Builder webClientBuilder) {
                return webClientBuilder.baseUrl("http://ms-sucursales").build();
        }

        @Bean
        public WebClient articuloWebClient(WebClient.Builder webClientBuilder) {
                return webClientBuilder.baseUrl("http://ms-embalaje").build();
        }

        @Bean
        public WebClient finanzasWebClient(WebClient.Builder webClientBuilder) {
                return webClientBuilder.baseUrl("http://ms-finanzas").build();
        }
}
