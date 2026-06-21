package com.sivebo.ms_ventas.utils;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

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

        public void validateMicroServiceById(Long id, String nameService, WebClient webClient) {
                try {
                        webClient.get()
                                .uri("/api/v1/" + nameService + "/{id}", id)
                                .retrieve()
                                .bodyToMono(String.class)
                                .block();
                        log.info(">>> {} {} validado correctamente (WebClient)", nameService, id);
                } catch (WebClientResponseException.NotFound e) {
                        throw new MicroserviceValidationException(
                                nameService + " con id " + id + " no existe en el microservicio.");
                } catch (Exception e) {
                        throw new MicroserviceUnavailableException(
                                "No se pudo conectar con el microservicio: " + e.getMessage());
                }
        }

        public void validateMicroServiceByQuery(String nameService, String query, String value, WebClient webClient) {
                try {
                        webClient.get()
                                .uri("/api/v1/" + nameService + "/search?" + query + "=" + value)
                                .retrieve()
                                .bodyToMono(String.class)
                                .block();
                        log.info(">>> {} {} validado correctamente (WebClient)", nameService, value);
                } catch (WebClientResponseException.NotFound e) {
                        throw new MicroserviceValidationException(
                                nameService + " con " + query + "=" + value + " no existe en el microservicio.");
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

        // ms_sucursales no expone GET /{id} (solo PUT/PATCH); la búsqueda por id es vía
        // /buscar?id= (mismo patrón que usa ms_admision). Valida que la sucursal exista.
        public void validateSucursalExiste(Long id, WebClient webClient) {
                try {
                        webClient.get()
                                .uri("/api/v1/sucursales/buscar?id={id}", id)
                                .retrieve()
                                .bodyToMono(String.class)
                                .block();
                        log.info(">>> sucursal {} validada correctamente (WebClient)", id);
                } catch (WebClientResponseException.NotFound e) {
                        throw new MicroserviceValidationException(
                                "sucursal con id " + id + " no existe en el microservicio.");
                } catch (Exception e) {
                        throw new MicroserviceUnavailableException(
                                "No se pudo conectar con el microservicio: " + e.getMessage());
                }
        }

        // RF-33: resolves the authoritative unit price from ms_embalaje at sale time,
        // so the persisted historic price cannot be tampered with by the client.
        public Long resolvePrecioArticulo(Long idArt, WebClient webClient) {
                try {
                        Map<String, Object> response = webClient.get()
                                .uri("/api/v1/articulos/{id}", idArt)
                                .retrieve()
                                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                                .block();
                        if (response == null || response.get("precioVta") == null) {
                                throw new MicroserviceValidationException(
                                        "Artículo con id " + idArt + " no encontrado o sin precio de venta");
                        }
                        return new BigDecimal(String.valueOf(response.get("precioVta"))).longValue();
                } catch (MicroserviceValidationException e) {
                        throw e;
                } catch (WebClientResponseException.NotFound e) {
                        throw new MicroserviceValidationException("Artículo con id " + idArt + " no encontrado");
                } catch (Exception e) {
                        throw new MicroserviceUnavailableException("No se pudo conectar con ms_embalaje: " + e.getMessage());
                }
        }

        // RF-29: returns true if the branch has sufficient stock for the article
        public Boolean verificarStock(Long idArt, Long idSucursal, Integer cantidad, WebClient webClient) {
                try {
                        Boolean result = webClient.get()
                                .uri("/api/v1/stock/verificar?idArt={idArt}&idSucursal={idSucursal}&cantidadRequerida={cantidad}",
                                        idArt, idSucursal, cantidad)
                                .retrieve()
                                .bodyToMono(Boolean.class)
                                .block();
                        return result != null && result;
                } catch (WebClientResponseException.NotFound e) {
                        return false;
                } catch (Exception e) {
                        throw new MicroserviceUnavailableException("No se pudo conectar con ms_embalaje: " + e.getMessage());
                }
        }

        // RF-28: decrements stock after a confirmed sale; non-blocking on failure
        public void descontarStock(Long idArt, Long idSucursal, Integer cantidad, WebClient webClient) {
                try {
                        webClient.patch()
                                .uri("/api/v1/stock/descontar?idArt={idArt}&idSucursal={idSucursal}&cantidad={cantidad}",
                                        idArt, idSucursal, cantidad)
                                .retrieve()
                                .bodyToMono(String.class)
                                .block();
                        log.info(">>> Stock descontado: artículo {} en sucursal {} -{} u", idArt, idSucursal, cantidad);
                } catch (Exception e) {
                        log.warn(">>> No se pudo descontar stock de artículo {} en sucursal {}: {}", idArt, idSucursal, e.getMessage());
                }
        }

        // RF-37: resolves the active caja session id for a branch (sucursal → caja → sesión abierta)
        public Optional<Long> resolveIdSesionAbierta(Long idSucursal, WebClient webClient) {
                try {
                        Map<String, Object> caja = webClient.get()
                                .uri("/api/v1/cajas/sucursal/{idSucursal}", idSucursal)
                                .retrieve()
                                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                                .block();
                        if (caja == null) return Optional.empty();
                        Long idCaja = ((Number) caja.get("idCaja")).longValue();

                        Map<String, Object> sesion = webClient.get()
                                .uri("/api/v1/aperturas/caja/{idCaja}/abierta", idCaja)
                                .retrieve()
                                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                                .block();
                        if (sesion == null) return Optional.empty();
                        return Optional.of(((Number) sesion.get("idSesion")).longValue());
                } catch (Exception e) {
                        log.warn(">>> No se pudo obtener sesión abierta para sucursal {}: {}", idSucursal, e.getMessage());
                        return Optional.empty();
                }
        }

        // RF-37/RF-34: registers an INGRESO or EGRESO movimiento in ms_finanzas; non-blocking on failure
        public void registrarMovimiento(Long idSesion, String tipo, Long monto, Long idVenta, WebClient webClient) {
                try {
                        Map<String, Object> body = Map.of(
                                "idSesion", idSesion,
                                "tipo", tipo,
                                "monto", BigDecimal.valueOf(monto),
                                "idReferenciaVta", idVenta
                        );
                        webClient.post()
                                .uri("/api/v1/movimientos")
                                .bodyValue(body)
                                .retrieve()
                                .bodyToMono(String.class)
                                .block();
                        log.info(">>> Movimiento {} registrado en sesión {} por venta {} (monto={})", tipo, idSesion, idVenta, monto);
                } catch (Exception e) {
                        log.warn(">>> No se pudo registrar movimiento {} en ms_finanzas: {}", tipo, e.getMessage());
                }
        }
}
