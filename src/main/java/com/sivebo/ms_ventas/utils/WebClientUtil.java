package com.sivebo.ms_ventas.utils;

import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.sivebo.ms_ventas.exception.MicroserviceForbiddenException;
import com.sivebo.ms_ventas.exception.MicroserviceUnavailableException;
import com.sivebo.ms_ventas.exception.MicroserviceValidationException;

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
                        log.info(">>> {} {} validado correctamente (WebClient)", name_service, id);
                } catch (WebClientResponseException.NotFound e) {
                        throw new MicroserviceValidationException(
                                name_service + " con id " + id + " no existe en el microservicio.");
                } catch (Exception e) {
                        throw new MicroserviceUnavailableException(
                                "No se pudo conectar con el microservicio: " + e.getMessage());
                }
        }

        public void validateMicroServiceByQuery(String name_service, String query, String value, WebClient webClient) {
                try {
                        webClient.get()
                                .uri("/api/v1/" + name_service + "/search?" + query + "=" + value)
                                .retrieve()
                                .bodyToMono(String.class)
                                .block();
                        log.info(">>> {} {} validado correctamente (WebClient)", name_service, value);
                } catch (WebClientResponseException.NotFound e) {
                        throw new MicroserviceValidationException(
                                name_service + " con " + query + "=" + value + " no existe en el microservicio.");
                } catch (Exception e) {
                        throw new MicroserviceUnavailableException(
                                "No se pudo conectar con el microservicio: " + e.getMessage());
                }
        }

        public void validateUserRole(Long idUsuario, WebClient webClient) {
                try {
                        Map<String, Object> response = webClient.get()
                                .uri("/api/v1/usuarios/{id}", idUsuario)
                                .retrieve()
                                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                                .block();
                        if (response == null) {
                                throw new MicroserviceValidationException("Usuario no encontrado");
                        }
                        String rol = String.valueOf(response.get("rol"));
                        if (!"ADMIN".equalsIgnoreCase(rol) && !"SUPERVISOR".equalsIgnoreCase(rol)) {
                                throw new MicroserviceForbiddenException(
                                        "Usuario no tiene permiso para anular ventas (rol requerido: ADMIN o SUPERVISOR)");
                        }
                        log.info(">>> Usuario {} con rol {} autorizado para anular venta", idUsuario, rol);
                } catch (MicroserviceForbiddenException | MicroserviceValidationException e) {
                        throw e;
                } catch (WebClientResponseException.NotFound e) {
                        throw new MicroserviceValidationException("Usuario con id " + idUsuario + " no encontrado");
                } catch (Exception e) {
                        throw new MicroserviceUnavailableException(
                                "No se pudo conectar con ms_usuarios: " + e.getMessage());
                }
        }
}
