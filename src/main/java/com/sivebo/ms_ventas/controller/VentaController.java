package com.sivebo.ms_ventas.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sivebo.ms_ventas.dto.request.VentaRequestDTO;
import com.sivebo.ms_ventas.dto.response.VentaResponseDTO;
import com.sivebo.ms_ventas.model.TipoEstadoVenta;
import com.sivebo.ms_ventas.service.VentaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("api/v1/ventas")
@RequiredArgsConstructor
@Tag(name = "Ventas", description = "Gestión de ventas y punto de venta POS (RF-30 a RF-35)")
public class VentaController {

        private final VentaService ventaService;

        @Operation(summary = "Listar todas las ventas")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Ventas obtenidas exitosamente",
                        content = @Content(mediaType = "application/json",
                                schema = @Schema(implementation = VentaResponseDTO.class)))
        })
        @GetMapping
        public List<VentaResponseDTO> getAll() {
                return ventaService.getAll();
        }

        @Operation(summary = "Obtener venta por ID")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Venta encontrada",
                        content = @Content(mediaType = "application/json",
                                schema = @Schema(implementation = VentaResponseDTO.class))),
                @ApiResponse(responseCode = "404", description = "Venta no encontrada",
                        content = @Content(mediaType = "application/json"))
        })
        @GetMapping("/{id}")
        public ResponseEntity<VentaResponseDTO> getById(@PathVariable Long id) {
                return ventaService.getById(id)
                        .map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
        }

        @Operation(
                summary = "Buscar venta por atributo",
                description = "Busca por: nro_boleta o id_usuario (un parámetro a la vez)"
        )
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Resultados encontrados",
                        content = @Content(mediaType = "application/json",
                                schema = @Schema(implementation = VentaResponseDTO.class))),
                @ApiResponse(responseCode = "400", description = "Parámetro inválido o múltiples parámetros",
                        content = @Content(mediaType = "application/json")),
                @ApiResponse(responseCode = "404", description = "No encontrado",
                        content = @Content(mediaType = "application/json"))
        })
        @GetMapping("/buscar")
        public ResponseEntity<?> buscar(
                @RequestParam(required = false) String nro_boleta,
                @RequestParam(required = false) String id_usuario) {

                long provided = Stream.of(nro_boleta, id_usuario).filter(Objects::nonNull).count();

                if (provided == 0) {
                        return ResponseEntity.badRequest().body("Debe proporcionar un atributo de búsqueda válido");
                } else if (provided > 1) {
                        return ResponseEntity.badRequest().body("Solo se permite un atributo de búsqueda a la vez");
                } else if (nro_boleta != null) {
                        log.info(">>> Buscando venta por nro_boleta: {}", nro_boleta);
                        return ventaService.getByNroBoleta(Long.valueOf(nro_boleta))
                                .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
                } else {
                        log.info(">>> Buscando ventas por id_usuario: {}", id_usuario);
                        return ResponseEntity.ok(ventaService.getByIdUsuario(Long.valueOf(id_usuario)));
                }
        }

        @Operation(
                summary = "Consultar ventas de una sucursal (RF-35)",
                description = "Retorna ventas de una sucursal con filtros opcionales de rango de fecha y estado"
        )
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Ventas obtenidas exitosamente",
                        content = @Content(mediaType = "application/json",
                                schema = @Schema(implementation = VentaResponseDTO.class))),
                @ApiResponse(responseCode = "400", description = "id_sucursal es requerido",
                        content = @Content(mediaType = "application/json"))
        })
        @GetMapping("/sucursal")
        public ResponseEntity<?> getBySucursal(
                @RequestParam Long id_sucursal,
                @RequestParam(required = false) LocalDateTime desde,
                @RequestParam(required = false) LocalDateTime hasta,
                @RequestParam(required = false) TipoEstadoVenta estado) {

                log.info(">>> Buscando ventas de sucursal {} (desde={}, hasta={}, estado={})",
                        id_sucursal, desde, hasta, estado);
                return ResponseEntity.ok(
                        ventaService.getByIdSucursal(id_sucursal, desde, hasta, estado));
        }

        @Operation(
                summary = "Registrar una venta (RF-30, RF-31, RF-32, RF-33)",
                description = "Crea una venta con uno o más artículos. IVA (19%), total y nroBoleta se calculan automáticamente."
        )
        @ApiResponses(value = {
                @ApiResponse(responseCode = "201", description = "Venta registrada exitosamente",
                        content = @Content(mediaType = "application/json",
                                schema = @Schema(implementation = VentaResponseDTO.class))),
                @ApiResponse(responseCode = "400", description = "Datos inválidos",
                        content = @Content(mediaType = "application/json")),
                @ApiResponse(responseCode = "404", description = "Usuario o sucursal no encontrado",
                        content = @Content(mediaType = "application/json")),
                @ApiResponse(responseCode = "503", description = "Microservicio no disponible",
                        content = @Content(mediaType = "application/json"))
        })
        @PostMapping
        public ResponseEntity<VentaResponseDTO> create(@Valid @RequestBody VentaRequestDTO dto) {
                return ResponseEntity.status(HttpStatus.CREATED).body(ventaService.create(dto));
        }

        @Operation(
                summary = "Anular una venta (RF-34)",
                description = "Cambia el estado de la venta a ANULADA. Solo usuarios con rol ADMIN o SUPERVISOR pueden anular."
        )
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Venta anulada exitosamente",
                        content = @Content(mediaType = "application/json",
                                schema = @Schema(implementation = VentaResponseDTO.class))),
                @ApiResponse(responseCode = "403", description = "Usuario sin permiso para anular",
                        content = @Content(mediaType = "application/json")),
                @ApiResponse(responseCode = "404", description = "Venta o usuario no encontrado",
                        content = @Content(mediaType = "application/json")),
                @ApiResponse(responseCode = "503", description = "ms_usuarios no disponible",
                        content = @Content(mediaType = "application/json"))
        })
        @PutMapping("/anular/{id}")
        public ResponseEntity<VentaResponseDTO> anular(
                @PathVariable Long id,
                @RequestParam Long id_usuario) {
                return ResponseEntity.ok(ventaService.anular(id, id_usuario));
        }

        @Operation(summary = "Eliminar una venta")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Venta eliminada exitosamente",
                        content = @Content(mediaType = "application/json")),
                @ApiResponse(responseCode = "404", description = "Venta no encontrada",
                        content = @Content(mediaType = "application/json"))
        })
        @DeleteMapping("/{id}")
        public ResponseEntity<String> delete(@PathVariable Long id) {
                if (ventaService.delete(id)) {
                        return ResponseEntity.ok("Venta eliminada");
                } else {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Venta no encontrada");
                }
        }
}
