package com.sivebo.ms_ventas.dto;

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
        Long idUsuarioVta;
        Long  idSucursal;
        LocalDateTime fecha;
        Long subtotal;
}
