package com.sivebo.ms_ventas.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleVentaResponseDTO {

        Long id;
        Long idVenta;
        Long idArticulo;
        Long idAdmision;
        Integer cantidadArt;
        Long precioUnitHistoricoArt;
        Long precioAdmision;
}
