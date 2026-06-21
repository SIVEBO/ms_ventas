package com.sivebo.ms_ventas.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleVentaRequestDTO {

        @NotNull(message = "El id de artículo es obligatorio")
        Long idArticulo;

        Long idAdmision;

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        Integer cantidadArt;

        @Min(value = 0, message = "El precio unitario no puede ser negativo")
        Long precioUnitHistoricoArt;

        Long precioAdmision;
}
