package com.example.tennisclub.courtTests;

import com.example.tennisclub.court.dto.CourtRequestDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CourtRequestDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validCourtRequestDto_ShouldPassValidation() {
        CourtRequestDto dto = new CourtRequestDto("Court 1", 1L);

        Set<ConstraintViolation<CourtRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void courtRequestDto_WithBlankName_ShouldFailValidation() {
        CourtRequestDto dto = new CourtRequestDto("", 1L);

        Set<ConstraintViolation<CourtRequestDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Court name must not be blank");
    }

    @Test
    void courtRequestDto_WithNullName_ShouldFailValidation() {
        CourtRequestDto dto = new CourtRequestDto(null, 1L);

        Set<ConstraintViolation<CourtRequestDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Court name must not be blank");
    }

    @Test
    void courtRequestDto_WithWhitespaceOnlyName_ShouldFailValidation() {
        CourtRequestDto dto = new CourtRequestDto("   ", 1L);

        Set<ConstraintViolation<CourtRequestDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Court name must not be blank");
    }

    @Test
    void courtRequestDto_WithNullSurfaceTypeId_ShouldFailValidation() {
        CourtRequestDto dto = new CourtRequestDto("Court 1", null);

        Set<ConstraintViolation<CourtRequestDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("SurfaceTypeId ID must not be null");
    }
}