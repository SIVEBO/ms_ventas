package com.sivebo.ms_ventas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sivebo.ms_ventas.dto.response.DetalleVentaResponseDTO;
import com.sivebo.ms_ventas.model.DetalleVenta;
import com.sivebo.ms_ventas.model.TipoEstadoVenta;
import com.sivebo.ms_ventas.model.Venta;
import com.sivebo.ms_ventas.repository.DetalleVentaRepository;

@ExtendWith(MockitoExtension.class)
class DetalleVentaServiceTest {

    @Mock DetalleVentaRepository detalleVentaRepository;

    @InjectMocks DetalleVentaService service;

    private static final Venta VENTA = new Venta(
            1L, 1L, 50L, 10L, LocalDateTime.of(2026, 6, 1, 10, 0),
            10000L, 1900L, 11900L, TipoEstadoVenta.ACTIVA);

    private static final DetalleVenta DETALLE = new DetalleVenta(
            1L, VENTA, 200L, null, 2, 5000L, null);

    @Test
    void getAllRetornasTodosLosDetalles() {
        when(detalleVentaRepository.findAll()).thenReturn(List.of(DETALLE));

        List<DetalleVentaResponseDTO> result = service.getAll();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(1L, result.get(0).getIdVenta());
        assertEquals(200L, result.get(0).getIdArticulo());
    }

    @Test
    void getByIdEncontradoRetornaDTO() {
        when(detalleVentaRepository.findById(1L)).thenReturn(Optional.of(DETALLE));

        Optional<DetalleVentaResponseDTO> result = service.getById(1L);

        assertTrue(result.isPresent());
        assertEquals(2, result.get().getCantidadArt());
        assertEquals(5000L, result.get().getPrecioUnitHistoricoArt());
    }

    @Test
    void getByIdNoExisteRetornaVacio() {
        when(detalleVentaRepository.findById(99L)).thenReturn(Optional.empty());

        assertTrue(service.getById(99L).isEmpty());
    }

    @Test
    void getByVentaIdRetornaDetallesDeVenta() {
        when(detalleVentaRepository.findByVentaId(1L)).thenReturn(List.of(DETALLE));

        List<DetalleVentaResponseDTO> result = service.getByVentaId(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getIdVenta());
    }

    @Test
    void getByVentaIdVentaSinDetallesRetornaListaVacia() {
        when(detalleVentaRepository.findByVentaId(99L)).thenReturn(List.of());

        assertTrue(service.getByVentaId(99L).isEmpty());
    }

    @Test
    void deleteEliminaYRetornaTrue() {
        doNothing().when(detalleVentaRepository).deleteById(1L);
        when(detalleVentaRepository.existsById(1L)).thenReturn(false);

        assertTrue(service.delete(1L));
    }
}
