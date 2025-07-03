package com.example.tennisclub.surfaceType;


import com.example.tennisclub.exception.EntityFinder;
import com.example.tennisclub.surfaceType.dto.SurfaceTypeRequestDTO;
import com.example.tennisclub.surfaceType.dto.SurfaceTypeResponseDto;
import com.example.tennisclub.surfaceType.entity.SurfaceType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SurfaceTypeService {

    private final SurfaceTypeRepository surfaceTypeRepository;
    private final EntityFinder entityFinder;

    public SurfaceType findByIdOrThrow(Long id) {
        return entityFinder.findByIdOrThrow(
                surfaceTypeRepository.findById(id), id, "SurfaceType");
    }

    public SurfaceTypeResponseDto getSurfaceTypeById(Long id){
        return mapToResponseDto(findByIdOrThrow(id));
    }

    public List<SurfaceType> findAll() {
        return surfaceTypeRepository.findAll();
    }

    public List<SurfaceTypeResponseDto> getAll() {
        return findAll().stream().map(this::mapToResponseDto).toList();
    }

    @Transactional
    public SurfaceTypeResponseDto create(SurfaceTypeRequestDTO dto) {
        SurfaceType surfaceType = mapToEntity(dto);
        SurfaceType savedSurfaceType = save(surfaceType);
        return mapToResponseDto(savedSurfaceType);
    }

    @Transactional
    public SurfaceType save(SurfaceType surfaceType) {
        return surfaceTypeRepository.save(surfaceType);
    }

    @Transactional
    public SurfaceTypeResponseDto update(Long id, SurfaceTypeRequestDTO updated) {
        SurfaceType existing = findByIdOrThrow(id);

        existing.setName(updated.name());
        existing.setPricePerMinute(updated.pricePerMinute());

        surfaceTypeRepository.update(existing);

        return mapToResponseDto(existing);
    }

    @Transactional
    public void softDelete(Long id) {
        if (!surfaceTypeRepository.softDelete(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"SurfaceType with id " + id + " not found.");
        }
    }

    public long count() {
        return surfaceTypeRepository.count();
    }

    public SurfaceType mapToEntity(SurfaceTypeRequestDTO dto) {

        return SurfaceType.builder()
                .name(dto.name())
                .pricePerMinute(dto.pricePerMinute())
                .build();
    }

    public SurfaceTypeResponseDto mapToResponseDto(SurfaceType st) {
        return new SurfaceTypeResponseDto(
                st.getId(), st.getName(), st.getPricePerMinute()
        );
    }
}

