package com.example.tennisclub.court.dto;


import com.example.tennisclub.surfaceType.dto.SurfaceTypeResponseDto;

public record CourtResponseDto(Long id, String name, SurfaceTypeResponseDto surfaceType) {
}
