package com.sivebo.ms_ventas.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sivebo.ms_ventas.model.Venta;

public interface VentaRepository extends JpaRepository<Venta, Object>{
        
        Optional<Venta> findByNroBoleta(Long nroBoleta);

        List<Venta> findByIdSucursal(Long idSucursal);
        
        List<Venta> findByIdUsuarioVta(Long idUsuarioVta);

        List<Venta> findByFechaHoraBetween(LocalDateTime comienzo, LocalDateTime fin);

        List<Venta> findBySubtotalBetween(Long min, Long max);

        List<Venta> findBySubtotalMin(Long min);

        List<Venta> findBySubtotalMax(Long max);

}
