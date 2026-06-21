package com.sivebo.ms_ventas.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sivebo.ms_ventas.dto.response.DetalleVentaResponseDTO;
import com.sivebo.ms_ventas.service.DetalleVentaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("api/v1/detalles")
@RequiredArgsConstructor
@Tag(name = "Detalles de Venta", description = "Líneas de artículos de embalaje y servicios de envío en cada boleta")
public class DetalleVentaController {

        private final DetalleVentaService detalleVentaService;

        @Operation(summary = "Listar todos los detalles")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Detalles obtenidos exitosamente",
                        content = @Content(mediaType = "application/json",
                                schema = @Schema(implementation = DetalleVentaResponseDTO.class)))
        })
        @GetMapping
        public List<DetalleVentaResponseDTO> getAll() {
                return detalleVentaService.getAll();
        }

        @Operation(summary = "Obtener detalle por ID")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Detalle encontrado",
                        content = @Content(mediaType = "application/json",
                                schema = @Schema(implementation = DetalleVentaResponseDTO.class))),
                @ApiResponse(responseCode = "404", description = "Detalle no encontrado",
                        content = @Content(mediaType = "application/json"))
        })
        @GetMapping("/{id}")
        public ResponseEntity<DetalleVentaResponseDTO> getById(@PathVariable Long id) {
                return detalleVentaService.getById(id)
                        .map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
        }

        @Operation(
                summary = "Obtener detalles de una venta",
                description = "Retorna todas las líneas de artículos y servicios de envío asociadas a una venta"
        )
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Detalles obtenidos exitosamente",
                        content = @Content(mediaType = "application/json",
                                schema = @Schema(implementation = DetalleVentaResponseDTO.class)))
        })
        @GetMapping("/venta/{idVenta}")
        public List<DetalleVentaResponseDTO> getByVentaId(@PathVariable Long idVenta) {
                return detalleVentaService.getByVentaId(idVenta);
        }

        @Operation(summary = "Eliminar un detalle de venta")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Detalle eliminado exitosamente",
                        content = @Content(mediaType = "application/json")),
                @ApiResponse(responseCode = "404", description = "Detalle no encontrado",
                        content = @Content(mediaType = "application/json"))
        })
        @DeleteMapping("/{id}")
        public ResponseEntity<String> delete(@PathVariable Long id) {
                if (detalleVentaService.delete(id)) {
                        return ResponseEntity.ok("Detalle eliminado");
                } else {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Detalle no encontrado");
                }
        }
}
