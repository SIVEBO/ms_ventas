package com.sivebo.ms_ventas.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaResponseDTO {

        Long id;
        Long nroBoleta;
        Long idUsuario;
        Long idSucursal;
        LocalDateTime fechaVta;
        Long subtotal;
        Long iva;
        Long total;
        String estado;
}
