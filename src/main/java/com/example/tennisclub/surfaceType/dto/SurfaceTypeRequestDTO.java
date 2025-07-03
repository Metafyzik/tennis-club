package com.example.tennisclub.surfaceType.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SurfaceTypeRequestDTO(

        @NotBlank(message = "Surface name must not be blank") String name,

        @NotNull(message = "PricePerMinute must not be null")
        @DecimalMin(value = "0.01", inclusive = true, message = "PricePerMinute must be greater than 0") Double pricePerMinute
) {}