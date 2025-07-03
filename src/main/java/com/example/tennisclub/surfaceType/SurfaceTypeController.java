package com.example.tennisclub.surfaceType;

import com.example.tennisclub.surfaceType.dto.SurfaceTypeRequestDTO;
import com.example.tennisclub.surfaceType.dto.SurfaceTypeResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/surface-types")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SurfaceTypeController {

    private final SurfaceTypeService surfaceTypeService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
    public ResponseEntity<List<SurfaceTypeResponseDto>> getAll() {
        List<SurfaceTypeResponseDto> surfaceTypes = surfaceTypeService.getAll();
        return ResponseEntity.ok(surfaceTypes);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
    public ResponseEntity<SurfaceTypeResponseDto> getSurfaceTypeById(@PathVariable Long id) {
        SurfaceTypeResponseDto surfaceType = surfaceTypeService.getSurfaceTypeById(id);
        return ResponseEntity.ok(surfaceType);
    }

    @PostMapping
    public ResponseEntity<SurfaceTypeResponseDto> create(@RequestBody @Valid SurfaceTypeRequestDTO dto) {
        SurfaceTypeResponseDto created = surfaceTypeService.create(dto);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SurfaceTypeResponseDto> update(@PathVariable Long id, @RequestBody  @Valid SurfaceTypeRequestDTO dto) {
        SurfaceTypeResponseDto updated = surfaceTypeService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        surfaceTypeService.softDelete(id);

        return ResponseEntity.ok("Surface type with id "+ id + " deleted successfully.");
    }

    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
    public ResponseEntity<Long> count() {
        return ResponseEntity.ok(surfaceTypeService.count());
    }
}
