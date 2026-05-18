package com.sivebo.ms_ventas.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.sivebo.ms_ventas.dto.VentaRequestDTO;
import com.sivebo.ms_ventas.dto.VentaResponseDTO;
import com.sivebo.ms_ventas.model.Venta;
import com.sivebo.ms_ventas.repository.VentaRepository;
import com.sivebo.ms_ventas.utils.WebClientUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@RequiredArgsConstructor
public class VentaService {
        
        private final VentaRepository ventaRepository;
        private final WebClientUtil webClientUtil;

        @Qualifier("usuarioWebClient")
        private final WebClient usuarioWebClient;

        @Qualifier("sucursalWebClient")
        private final WebClient sucursalWebClient;

        private VentaResponseDTO mapToDTO(Venta venta){
                return new VentaResponseDTO(
                        venta.getId(),
                        venta.getNroBoleta(),
                        venta.getIdUsuarioVta(),
                        venta.getIdSucursal(),
                        venta.getFechaHora(),
                        venta.getSubtotal()
                );
        }
        
        public List<VentaResponseDTO> getAll(){
                return ventaRepository.findAll()
                        .stream().map(this::mapToDTO).toList();
        }
        
        public Optional<VentaResponseDTO> getById(Long id) {
                return ventaRepository.findById(id).map(this::mapToDTO);
        }
        
        public Optional<VentaResponseDTO> getByNroBoleta(Long nroBoleta) {
                return ventaRepository.findByNroBoleta(nroBoleta).map(this::mapToDTO);
        }
        
        public List<VentaResponseDTO> getByIdSucursal(Long idSucursal){
                return ventaRepository.findByIdSucursal(idSucursal)
                        .stream().map(this::mapToDTO).toList();
        }       
        
        public List<VentaResponseDTO> getByIdUsuarioVta(Long idUsuarioVta){
                return ventaRepository.findByIdUsuarioVta(idUsuarioVta)
                        .stream().map(this::mapToDTO).toList();
        }
        
        public List<VentaResponseDTO> getByFecha(LocalDate fecha){
                return ventaRepository.findByFechaHoraBetween(fecha.atStartOfDay(), fecha.atTime(LocalTime.MAX))
                        .stream().map(this::mapToDTO).toList();
        }

        public List<VentaResponseDTO> getBySubtotalBetween(Long min, Long max){
                return ventaRepository.findBySubtotalBetween(min, max)
                        .stream().map(this::mapToDTO).toList();
        }

        public List<VentaResponseDTO> getBySubtotalMin(Long min){
                return ventaRepository.findBySubtotalGreaterThanEqual(min)
                        .stream().map(this::mapToDTO).toList();
        }

        public List<VentaResponseDTO> getBySubtotalMax(Long max){
                return ventaRepository.findBySubtotalLessThanEqual(max)
                        .stream().map(this::mapToDTO).toList();
        }
        
        public VentaResponseDTO create(VentaRequestDTO dto){
                webClientUtil.validateMicroServiceById(dto.getIdUsuarioVta(), "usuarios", usuarioWebClient);
                webClientUtil.validateMicroServiceById(dto.getIdSucursal(), "sucursal", sucursalWebClient);
                
                return mapToDTO(ventaRepository.save(
                        new Venta(
                        null,
                        dto.getNroBoleta(),
                        dto.getIdUsuarioVta(),
                        dto.getIdSucursal(),
                        dto.getFechaHora(),
                        dto.getSubtotal()
                        )
                ));
        }
        
        public Boolean delete(Long id){
                ventaRepository.deleteById(id);
                return !ventaRepository.existsById(id);
        }
}
