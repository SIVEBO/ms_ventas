package com.sivebo.ms_ventas.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.sivebo.ms_ventas.dto.request.DetalleVentaRequestDTO;
import com.sivebo.ms_ventas.dto.request.VentaRequestDTO;
import com.sivebo.ms_ventas.dto.response.VentaResponseDTO;
import com.sivebo.ms_ventas.exception.MicroserviceValidationException;
import com.sivebo.ms_ventas.model.DetalleVenta;
import com.sivebo.ms_ventas.model.TipoEstadoVenta;
import com.sivebo.ms_ventas.model.Venta;
import com.sivebo.ms_ventas.repository.DetalleVentaRepository;
import com.sivebo.ms_ventas.repository.VentaRepository;
import com.sivebo.ms_ventas.utils.WebClientUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class VentaService {

        private final VentaRepository ventaRepository;
        private final DetalleVentaRepository detalleVentaRepository;
        private final WebClientUtil webClientUtil;

        @Qualifier("usuarioWebClient")
        private final WebClient usuarioWebClient;

        @Qualifier("sucursalWebClient")
        private final WebClient sucursalWebClient;

        @Qualifier("articuloWebClient")
        private final WebClient articuloWebClient;

        @Qualifier("finanzasWebClient")
        private final WebClient finanzasWebClient;

        private VentaResponseDTO mapToDTO(Venta venta) {
                return new VentaResponseDTO(
                        venta.getId(),
                        venta.getNroBoleta(),
                        venta.getIdUsuario(),
                        venta.getIdSucursal(),
                        venta.getFechaVta(),
                        venta.getSubtotal(),
                        venta.getIva(),
                        venta.getTotal(),
                        venta.getEstado().name()
                );
        }

        @Transactional(readOnly = true)
        public List<VentaResponseDTO> getAll() {
                return ventaRepository.findAll().stream().map(this::mapToDTO).toList();
        }

        @Transactional(readOnly = true)
        public Optional<VentaResponseDTO> getById(Long id) {
                return ventaRepository.findById(id).map(this::mapToDTO);
        }

        @Transactional(readOnly = true)
        public Optional<VentaResponseDTO> getByNroBoleta(Long nroBoleta) {
                return ventaRepository.findByNroBoleta(nroBoleta).map(this::mapToDTO);
        }

        @Transactional(readOnly = true)
        public List<VentaResponseDTO> getByIdUsuario(Long idUsuario) {
                return ventaRepository.findByIdUsuario(idUsuario).stream().map(this::mapToDTO).toList();
        }

        @Transactional(readOnly = true)
        public List<VentaResponseDTO> getByIdSucursal(
                Long idSucursal, LocalDateTime desde, LocalDateTime hasta, TipoEstadoVenta estado) {

                if (desde != null && hasta != null && estado != null) {
                        return ventaRepository.findByIdSucursalAndFechaVtaBetweenAndEstado(
                                idSucursal, desde, hasta, estado).stream().map(this::mapToDTO).toList();
                } else if (desde != null && hasta != null) {
                        return ventaRepository.findByIdSucursalAndFechaVtaBetween(
                                idSucursal, desde, hasta).stream().map(this::mapToDTO).toList();
                } else if (estado != null) {
                        return ventaRepository.findByIdSucursalAndEstado(
                                idSucursal, estado).stream().map(this::mapToDTO).toList();
                } else {
                        return ventaRepository.findByIdSucursal(idSucursal).stream().map(this::mapToDTO).toList();
                }
        }

        @Transactional
        public VentaResponseDTO create(VentaRequestDTO dto) {
                webClientUtil.validateMicroServiceById(dto.getIdUsuario(), "usuarios", usuarioWebClient);
                webClientUtil.validateSucursalExiste(dto.getIdSucursal(), sucursalWebClient);

                // RF-29: verify stock for every embalaje article before committing anything
                for (DetalleVentaRequestDTO d : dto.getDetalles()) {
                        if (d.getIdArticulo() != null) {
                                Boolean ok = webClientUtil.verificarStock(
                                        d.getIdArticulo(), dto.getIdSucursal(), d.getCantidadArt(), articuloWebClient);
                                if (!ok) {
                                        throw new MicroserviceValidationException(
                                                "Stock insuficiente para artículo " + d.getIdArticulo()
                                                + " en sucursal " + dto.getIdSucursal());
                                }
                        }
                }

                // RF-33: resolve the authoritative unit price from ms_embalaje (once per line),
                // ignoring any client-supplied price for article lines.
                List<DetalleVentaRequestDTO> detalles = dto.getDetalles();
                List<Long> precios = new ArrayList<>(detalles.size());
                for (DetalleVentaRequestDTO d : detalles) {
                        if (d.getIdArticulo() != null) {
                                precios.add(webClientUtil.resolvePrecioArticulo(d.getIdArticulo(), articuloWebClient));
                        } else {
                                precios.add(d.getPrecioUnitHistoricoArt() != null ? d.getPrecioUnitHistoricoArt() : 0L);
                        }
                }

                long subtotal = 0L;
                for (int i = 0; i < detalles.size(); i++) {
                        DetalleVentaRequestDTO d = detalles.get(i);
                        subtotal += (long) d.getCantidadArt() * precios.get(i)
                                + (d.getPrecioAdmision() != null ? d.getPrecioAdmision() : 0L);
                }
                long iva = Math.round(subtotal * 0.19);
                long total = subtotal + iva;
                long nroBoleta = ventaRepository.count() + 1L;

                Venta saved = ventaRepository.save(new Venta(
                        null, nroBoleta, dto.getIdUsuario(), dto.getIdSucursal(),
                        dto.getFechaVta(), subtotal, iva, total, TipoEstadoVenta.ACTIVA
                ));

                for (int i = 0; i < detalles.size(); i++) {
                        DetalleVentaRequestDTO d = detalles.get(i);
                        detalleVentaRepository.save(new DetalleVenta(
                                null, saved, d.getIdArticulo(), d.getIdAdmision(),
                                d.getCantidadArt(), precios.get(i), d.getPrecioAdmision()
                        ));
                        // RF-28: decrement stock after saving the line
                        if (d.getIdArticulo() != null) {
                                webClientUtil.descontarStock(
                                        d.getIdArticulo(), dto.getIdSucursal(), d.getCantidadArt(), articuloWebClient);
                        }
                }

                log.info(">>> Venta {} creada con nroBoleta={}, subtotal={}, iva={}, total={}",
                        saved.getId(), nroBoleta, subtotal, iva, total);

                // RF-37: register INGRESO movimiento in the branch caja (non-blocking if no open session)
                webClientUtil.resolveIdSesionAbierta(dto.getIdSucursal(), finanzasWebClient)
                        .ifPresent(idSesion -> webClientUtil.registrarMovimiento(
                                idSesion, "INGRESO", saved.getTotal(), saved.getId(), finanzasWebClient));

                return mapToDTO(saved);
        }

        @Transactional
        public VentaResponseDTO anular(Long id, Long idUsuario) {
                webClientUtil.validateUserRole(idUsuario, usuarioWebClient);
                Venta venta = ventaRepository.findById(id)
                        .orElseThrow(() -> new MicroserviceValidationException("Venta con id " + id + " no encontrada"));
                venta.setEstado(TipoEstadoVenta.ANULADA);
                Venta saved = ventaRepository.save(venta);
                log.info(">>> Venta {} anulada por usuario {}", id, idUsuario);

                // RF-34: register EGRESO movimiento to reverse the original INGRESO
                webClientUtil.resolveIdSesionAbierta(saved.getIdSucursal(), finanzasWebClient)
                        .ifPresent(idSesion -> webClientUtil.registrarMovimiento(
                                idSesion, "EGRESO", saved.getTotal(), saved.getId(), finanzasWebClient));

                return mapToDTO(saved);
        }

        @Transactional
        public Boolean delete(Long id) {
                ventaRepository.deleteById(id);
                return !ventaRepository.existsById(id);
        }
}
