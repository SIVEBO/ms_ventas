package com.sivebo.ms_ventas.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sivebo.ms_ventas.dto.VentaRequestDTO;
import com.sivebo.ms_ventas.dto.VentaResponseDTO;
import com.sivebo.ms_ventas.service.VentaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("api/v1/ventas")
@RequiredArgsConstructor
public class VentaController {
        
        private final VentaService ventaService;

        @GetMapping
        public List<VentaResponseDTO> getAll() {
                return ventaService.getAll();
        }
        
        @GetMapping("{id}")
        public ResponseEntity<VentaResponseDTO> getById(@PathVariable Long id) {
                return ventaService.getById(id)
                        .map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
        }

        @GetMapping("/search")
        public ResponseEntity<?> getByAtribute(
                @RequestParam(required = false) String nroBoleta,
                @RequestParam(required = false) String idUsuarioVta,
                @RequestParam(required = false) String idSucursal,
                @RequestParam(required = false) String fecha){

                List<String> params = new ArrayList<>(List.of(nroBoleta, idUsuarioVta, idSucursal, fecha));

                int num_null = 0;
                for(String value: params){
                        if(value == null) num_null++;
                }
                if(num_null == params.size()) {
                        return ResponseEntity.badRequest().body("Debe proporcionar un atributo de búsqueda valido");
                }else if(num_null > 1) {
                        return ResponseEntity.badRequest().body("Solo se permite un atributo de búsqueda a la vez");
                }else if((params.size() - num_null) == 1){

                        if(nroBoleta != null) {
                                log.info(">>> Buscando ventas por numero de boleta: {}", nroBoleta);
                                return ventaService.getByNroBoleta(Long.valueOf(nroBoleta)).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
                        }else if(idUsuarioVta != null){
                                log.info(">>> Buscando ventas por id de usuario de venta: {}", idUsuarioVta);
                                return ResponseEntity.ok(ventaService.getByIdUsuarioVta(Long.valueOf(idUsuarioVta)));
                        }else if(idSucursal != null){
                                log.info(">>> Buscando ventas por id de sucursal: {}", idSucursal);
                                return ResponseEntity.ok(ventaService.getByIdSucursal(Long.valueOf(idSucursal)));
                        }else if(fecha != null){
                                log.info(">>> Buscando ventas por fecha: {}", fecha);
                                LocalDate fecha_formateada = LocalDate.parse(fecha, DateTimeFormatter.ofPattern("dd-MM-YY"));
                                return ResponseEntity.ok(ventaService.getByFecha(fecha_formateada));
                        }

                }
                return ResponseEntity.internalServerError().body("Error en el URL query");
        }

        @GetMapping("/monto")
        public ResponseEntity<?> getByMonto(
                @RequestParam(required=false) String min,
                @RequestParam(required=false) String max){

                if(min == null && max == null){
                       return ResponseEntity.badRequest().body("Debe proporcionar un atributo de búsqueda valido"); 
                }else if(max != null){
                        log.info(">>> Buscando ventas desde minimo: {}", min);
                        return ResponseEntity.ok(ventaService.getBySubtotalMin(Long.valueOf(min)));
                }else if(min != null){
                        log.info(">>> Buscando ventas hasta maximo: {}", max);
                        return ResponseEntity.ok(ventaService.getBySubtotalMax(Long.valueOf(max)));
                }else{
                        log.info(">>> Buscando ventas en rango: {} - {}", min ,max);
                        return ResponseEntity.ok(ventaService.getBySubtotalBetween(Long.valueOf(min),Long.valueOf(max)));
                }
        }
        
        @PostMapping 
        public ResponseEntity<VentaResponseDTO> create(@Valid @RequestBody VentaRequestDTO dto) {
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(ventaService.create(dto));
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<String> delete(@PathVariable Long id) {
                if (ventaService.delete(id)) {
                        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Venta eliminada");
                } else {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Venta no encontrada o no se pudo eliminar");
                }
        }
}
