package com.sivebo.ms_ventas.dto.request;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaRequestDTO {

        @NotNull(message = "El id de usuario es obligatorio")
        Long idUsuario;

        @NotNull(message = "El id de sucursal es obligatorio")
        Long idSucursal;

        @NotNull(message = "La fecha de venta es obligatoria")
        LocalDateTime fechaVta;

        @NotNull(message = "Debe incluir al menos un detalle")
        @Size(min = 1, message = "Debe incluir al menos un artículo en la venta")
        List<@Valid DetalleVentaRequestDTO> detalles;
}
