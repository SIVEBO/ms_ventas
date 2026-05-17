package com.sivebo.ms_ventas.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaRequestDTO {
        
        @NotNull(message="numero de boleta es obligatorio")
        Long nroBoleta;

        @NotNull(message="id de usuario de venta es obligatorio")
        Long idUsuarioVta;

        @NotNull(message="id de sucursal es obligatorio")
        Long  idSucursal;

        @NotNull(message="fecha es obligatorio")
        LocalDateTime fechaHora;

        @NotNull(message="subtotal es obligatorio")
        Long subtotal;
}
