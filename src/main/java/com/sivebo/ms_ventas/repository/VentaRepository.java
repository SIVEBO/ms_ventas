package com.sivebo.ms_ventas.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sivebo.ms_ventas.model.TipoEstadoVenta;
import com.sivebo.ms_ventas.model.Venta;

public interface VentaRepository extends JpaRepository<Venta, Long> {

        Optional<Venta> findByNroBoleta(Long nroBoleta);

        List<Venta> findByIdUsuario(Long idUsuario);

        List<Venta> findByIdSucursal(Long idSucursal);

        List<Venta> findByIdSucursalAndEstado(Long idSucursal, TipoEstadoVenta estado);

        List<Venta> findByIdSucursalAndFechaVtaBetween(
                Long idSucursal, LocalDateTime desde, LocalDateTime hasta);

        List<Venta> findByIdSucursalAndFechaVtaBetweenAndEstado(
                Long idSucursal, LocalDateTime desde, LocalDateTime hasta, TipoEstadoVenta estado);
}
