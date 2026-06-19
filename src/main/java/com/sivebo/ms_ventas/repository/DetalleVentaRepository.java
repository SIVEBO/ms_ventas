package com.sivebo.ms_ventas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sivebo.ms_ventas.model.DetalleVenta;

public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {

        List<DetalleVenta> findByVenta_Id(Long ventaId);

        void deleteByVenta_Id(Long ventaId);
}
