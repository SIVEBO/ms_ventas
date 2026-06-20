package com.sivebo.ms_ventas.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock VentaRepository ventaRepository;
    @Mock DetalleVentaRepository detalleVentaRepository;
    @Mock WebClientUtil webClientUtil;
    @Mock WebClient usuarioWebClient;
    @Mock WebClient sucursalWebClient;
    @Mock WebClient articuloWebClient;
    @Mock WebClient finanzasWebClient;

    @InjectMocks VentaService service;

    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 6, 1, 10, 0);

    private static final Venta VENTA_ACTIVA = new Venta(
            1L, 1L, 50L, 10L, FECHA, 10000L, 1900L, 11900L, TipoEstadoVenta.ACTIVA);

    private static final Venta VENTA_ANULADA = new Venta(
            1L, 1L, 50L, 10L, FECHA, 10000L, 1900L, 11900L, TipoEstadoVenta.ANULADA);

    @Test
    void getAll_retornaTodasLasVentas() {
        when(ventaRepository.findAll()).thenReturn(List.of(VENTA_ACTIVA));

        List<VentaResponseDTO> result = service.getAll();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("ACTIVA", result.get(0).getEstado());
    }

    @Test
    void getById_encontrada_retornaDTO() {
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(VENTA_ACTIVA));

        Optional<VentaResponseDTO> result = service.getById(1L);

        assertTrue(result.isPresent());
        assertEquals(11900L, result.get().getTotal());
    }

    @Test
    void getById_noExiste_retornaVacio() {
        when(ventaRepository.findById(99L)).thenReturn(Optional.empty());

        assertTrue(service.getById(99L).isEmpty());
    }

    @Test
    void getByNroBoleta_encontrada_retornaDTO() {
        when(ventaRepository.findByNroBoleta(1L)).thenReturn(Optional.of(VENTA_ACTIVA));

        Optional<VentaResponseDTO> result = service.getByNroBoleta(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getNroBoleta());
    }

    @Test
    void getByIdUsuario_retornaVentasDelUsuario() {
        when(ventaRepository.findByIdUsuario(50L)).thenReturn(List.of(VENTA_ACTIVA));

        List<VentaResponseDTO> result = service.getByIdUsuario(50L);

        assertEquals(1, result.size());
        assertEquals(50L, result.get(0).getIdUsuario());
    }

    @Test
    void getByIdSucursal_sinFiltros_retornaTodasLasSucursal() {
        when(ventaRepository.findByIdSucursal(10L)).thenReturn(List.of(VENTA_ACTIVA));

        List<VentaResponseDTO> result = service.getByIdSucursal(10L, null, null, null);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getIdSucursal());
    }

    @Test
    void getByIdSucursal_conEstado_filtraPorEstado() {
        when(ventaRepository.findByIdSucursalAndEstado(10L, TipoEstadoVenta.ACTIVA))
                .thenReturn(List.of(VENTA_ACTIVA));

        List<VentaResponseDTO> result = service.getByIdSucursal(10L, null, null, TipoEstadoVenta.ACTIVA);

        assertEquals(1, result.size());
        assertEquals("ACTIVA", result.get(0).getEstado());
    }

    @Test
    void create_stockSuficiente_guardaVentaYDetalles() {
        DetalleVentaRequestDTO detalle = new DetalleVentaRequestDTO(1L, null, 2, 5000L, null);
        VentaRequestDTO dto = new VentaRequestDTO(50L, 10L, FECHA, List.of(detalle));

        doNothing().when(webClientUtil).validateMicroServiceById(anyLong(), anyString(), any());
        when(webClientUtil.verificarStock(eq(1L), eq(10L), eq(2), any(WebClient.class))).thenReturn(true);
        when(ventaRepository.count()).thenReturn(0L);
        when(ventaRepository.save(any(Venta.class))).thenReturn(VENTA_ACTIVA);
        when(detalleVentaRepository.save(any(DetalleVenta.class))).thenReturn(new DetalleVenta());
        doNothing().when(webClientUtil).descontarStock(anyLong(), anyLong(), anyInt(), any(WebClient.class));
        when(webClientUtil.resolveIdSesionAbierta(anyLong(), any(WebClient.class))).thenReturn(Optional.empty());

        VentaResponseDTO result = service.create(dto);

        assertEquals(1L, result.getId());
        assertEquals("ACTIVA", result.getEstado());
        verify(ventaRepository).save(any(Venta.class));
        verify(detalleVentaRepository).save(any(DetalleVenta.class));
        verify(webClientUtil).descontarStock(eq(1L), eq(10L), eq(2), any());
    }

    @Test
    void create_stockInsuficiente_lanzaMicroserviceValidationException() {
        DetalleVentaRequestDTO detalle = new DetalleVentaRequestDTO(1L, null, 10, 5000L, null);
        VentaRequestDTO dto = new VentaRequestDTO(50L, 10L, FECHA, List.of(detalle));

        doNothing().when(webClientUtil).validateMicroServiceById(anyLong(), anyString(), any());
        when(webClientUtil.verificarStock(eq(1L), eq(10L), eq(10), any(WebClient.class))).thenReturn(false);

        assertThrows(MicroserviceValidationException.class, () -> service.create(dto));
        verify(ventaRepository, never()).save(any());
    }

    @Test
    void create_registraMovimientoSiHaySesionAbierta() {
        DetalleVentaRequestDTO detalle = new DetalleVentaRequestDTO(1L, null, 1, 10000L, null);
        VentaRequestDTO dto = new VentaRequestDTO(50L, 10L, FECHA, List.of(detalle));

        doNothing().when(webClientUtil).validateMicroServiceById(anyLong(), anyString(), any());
        when(webClientUtil.verificarStock(anyLong(), anyLong(), anyInt(), any(WebClient.class))).thenReturn(true);
        when(ventaRepository.count()).thenReturn(0L);
        when(ventaRepository.save(any(Venta.class))).thenReturn(VENTA_ACTIVA);
        when(detalleVentaRepository.save(any(DetalleVenta.class))).thenReturn(new DetalleVenta());
        doNothing().when(webClientUtil).descontarStock(anyLong(), anyLong(), anyInt(), any(WebClient.class));
        when(webClientUtil.resolveIdSesionAbierta(eq(10L), any(WebClient.class))).thenReturn(Optional.of(99L));
        doNothing().when(webClientUtil).registrarMovimiento(anyLong(), anyString(), anyLong(), anyLong(), any(WebClient.class));

        service.create(dto);

        verify(webClientUtil).registrarMovimiento(eq(99L), eq("INGRESO"), eq(11900L), eq(1L), any(WebClient.class));
    }

    @Test
    void anular_ventaExiste_cambiaEstadoYRegistraEgreso() {
        Venta ventaOriginal = new Venta(1L, 1L, 50L, 10L, FECHA, 10000L, 1900L, 11900L, TipoEstadoVenta.ACTIVA);

        doNothing().when(webClientUtil).validateUserRole(eq(50L), any());
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(ventaOriginal));
        when(ventaRepository.save(any(Venta.class))).thenReturn(VENTA_ANULADA);
        when(webClientUtil.resolveIdSesionAbierta(eq(10L), any())).thenReturn(Optional.empty());

        VentaResponseDTO result = service.anular(1L, 50L);

        assertEquals("ANULADA", result.getEstado());
        verify(ventaRepository).save(any(Venta.class));
    }

    @Test
    void anular_ventaNoExiste_lanzaMicroserviceValidationException() {
        doNothing().when(webClientUtil).validateUserRole(anyLong(), any());
        when(ventaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(MicroserviceValidationException.class, () -> service.anular(99L, 50L));
    }

    @Test
    void delete_eliminaCorrectamente_retornaTrue() {
        doNothing().when(ventaRepository).deleteById(1L);
        when(ventaRepository.existsById(1L)).thenReturn(false);

        assertTrue(service.delete(1L));
    }
}
