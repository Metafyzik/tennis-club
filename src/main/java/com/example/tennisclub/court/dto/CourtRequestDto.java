package com.example.tennisclub.court.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CourtRequestDto(
        @NotBlank(message = "Court name must not be blank") String name,

        @NotNull(message = "SurfaceTypeId ID must not be null")
        @Min(value = 1, message = "SurfaceTypeId ID must be and greater than 0") Long surfaceTypeId

){}


