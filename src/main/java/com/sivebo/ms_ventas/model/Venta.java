package com.sivebo.ms_ventas.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "VENTA")
public class Venta {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        Long id;

        @Column(name = "nro_boleta", nullable = false, unique = true)
        Long nroBoleta;

        @Column(name = "id_usuario", nullable = false)
        Long idUsuario;

        @Column(name = "id_sucursal", nullable = false)
        Long idSucursal;

        @Column(name = "fecha_vta", nullable = false)
        LocalDateTime fechaVta;

        @Column(name = "subtotal", nullable = false)
        Long subtotal;

        @Column(name = "iva", nullable = false)
        Long iva;

        @Column(name = "total", nullable = false)
        Long total;

        @Column(name = "estado", nullable = false)
        @Enumerated(EnumType.STRING)
        TipoEstadoVenta estado;
}
