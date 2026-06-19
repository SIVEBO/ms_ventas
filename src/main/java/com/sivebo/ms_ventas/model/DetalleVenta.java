package com.sivebo.ms_ventas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "DETALLE_VENTA")
public class DetalleVenta {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        Long id;

        @ManyToOne(optional = false)
        @JoinColumn(name = "id_venta")
        Venta venta;

        @Column(name = "id_articulo", nullable = false)
        Long idArticulo;

        @Column(name = "id_admision")
        Long idAdmision;

        @Column(name = "cantidad_art", nullable = false)
        Integer cantidadArt;

        @Column(name = "precio_unit_historico_art", nullable = false)
        Long precioUnitHistoricoArt;

        @Column(name = "precio_admision")
        Long precioAdmision;
}
