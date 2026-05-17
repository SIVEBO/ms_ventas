package com.sivebo.ms_ventas.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WebClientUtil {

	public void validateMicroServiceById(Long id, String name_service, WebClient webClient) {
                try {
			webClient.get()
				.uri("/api/v1/" + name_service + "/{id}", id)
				.retrieve()
				.bodyToMono(String.class)
				.block();
		log.info(">>> " + name_service + " {} validada correctamente (WebCliente)", id);

                } catch (WebClientResponseException.NotFound webException) {
			throw new RuntimeException(
				name_service + " con id " + id + " no existe en el microservicio.");
                } catch (Exception exception) {
			throw new RuntimeException(
				"No se pudo conectar con el microservicio: " + exception.getMessage());
		}
	}

	public void validateMicroServiceByQuery(String name_service, String query, String value, WebClient webClient) {
                try {
			webClient.get()
				.uri("/api/v1/" + name_service + "/search?" + query + "=" + value, value)
				.retrieve()
				.bodyToMono(String.class)
				.block();
		log.info(">>> " + name_service + " {} validada correctamente (WebCliente)", value);

                } catch (WebClientResponseException.NotFound webException) {
			throw new RuntimeException(
				name_service + " con " + query + "=" + value + " no existe en el microservicio.");
                } catch (Exception exception) {
			throw new RuntimeException(
				"No se pudo conectar con el microservicio: " + exception.getMessage());
		}
	}
}
