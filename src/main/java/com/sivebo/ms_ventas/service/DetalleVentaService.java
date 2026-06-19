package com.sivebo.ms_ventas.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sivebo.ms_ventas.dto.response.DetalleVentaResponseDTO;
import com.sivebo.ms_ventas.model.DetalleVenta;
import com.sivebo.ms_ventas.repository.DetalleVentaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DetalleVentaService {

        private final DetalleVentaRepository detalleVentaRepository;

        private DetalleVentaResponseDTO mapToDTO(DetalleVenta d) {
                return new DetalleVentaResponseDTO(
                        d.getId(),
                        d.getVenta().getId(),
                        d.getIdArticulo(),
                        d.getIdAdmision(),
                        d.getCantidadArt(),
                        d.getPrecioUnitHistoricoArt(),
                        d.getPrecioAdmision()
                );
        }

        @Transactional(readOnly = true)
        public List<DetalleVentaResponseDTO> getAll() {
                return detalleVentaRepository.findAll().stream().map(this::mapToDTO).toList();
        }

        @Transactional(readOnly = true)
        public Optional<DetalleVentaResponseDTO> getById(Long id) {
                return detalleVentaRepository.findById(id).map(this::mapToDTO);
        }

        @Transactional(readOnly = true)
        public List<DetalleVentaResponseDTO> getByVentaId(Long ventaId) {
                return detalleVentaRepository.findByVenta_Id(ventaId).stream().map(this::mapToDTO).toList();
        }

        @Transactional
        public Boolean delete(Long id) {
                detalleVentaRepository.deleteById(id);
                return !detalleVentaRepository.existsById(id);
        }
}
