package com.sivebo.ms_ventas.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name= "venta")
public class Venta {

        @Id
        @GeneratedValue(strategy=GenerationType.IDENTITY)
        Long id;

        @Column(name="nro_boleta", nullable=false, unique=true)
        Long nroBoleta;
        
        @Column(name="id_usuario_vta", nullable=false)
        Long idUsuarioVta;

        @Column(name="id_sucursal", nullable=false)
        Long  idSucursal;

        @Column(nullable=false)
        LocalDateTime fecha;

        @Column(nullable=false)
        Long subtotal;
        
}
