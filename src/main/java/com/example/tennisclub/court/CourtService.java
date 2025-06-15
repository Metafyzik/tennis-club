package com.example.tennisclub.court;


import com.example.tennisclub.court.dto.CourtRequestDto;
import com.example.tennisclub.court.dto.CourtResponseDto;
import com.example.tennisclub.court.entity.Court;
import com.example.tennisclub.exception.EntityFinder;

import com.example.tennisclub.surfaceType.SurfaceTypeService;
import com.example.tennisclub.surfaceType.dto.SurfaceTypeResponseDto;
import com.example.tennisclub.surfaceType.entity.SurfaceType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


@RequiredArgsConstructor
@Service
public class CourtService {

    private final CourtRepository courtRepository;
    private final SurfaceTypeService surfaceTypeService;
    private final EntityFinder entityFinder;

    public List<Court> findAllCourtEntities() {
        return courtRepository.findAll();
    }
    public List<CourtResponseDto> getAllCourts() {
        return findAllCourtEntities().stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    public Court findCourtEntityByIdOrThrow(Long id) {
        return entityFinder.findByIdOrThrow(courtRepository.findById(id), id, "Court");
    }

    public CourtResponseDto getCourt(Long id) {
        return mapToResponseDto(findCourtEntityByIdOrThrow(id));
    }

    @Transactional
    public Court save(Court court) {
        return courtRepository.save(court);
    }

    @Transactional
    public CourtResponseDto create(CourtRequestDto dto) {
        Court court = mapToEntity(dto);
        Court savedCourt = save(court);
        return mapToResponseDto(savedCourt);
    }

    @Transactional
    public CourtResponseDto update(Long updatedCourtId, CourtRequestDto dto) {
        long surfaceTypeId = dto.surfaceTypeId();
        SurfaceType surfaceType = surfaceTypeService.findByIdOrThrow(surfaceTypeId);

        Court court = findCourtEntityByIdOrThrow(updatedCourtId);

        court.setName(dto.name());
        court.setSurfaceType(surfaceType);

        Court updated = courtRepository.update(court);

        return mapToResponseDto(updated);
    }

    @Transactional
    public void softDelete(Long id) {
        if (!courtRepository.softDelete(id)){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Court with ID " + id + " not found");
        }
    }

    public Court mapToEntity(CourtRequestDto dto) {
        SurfaceType surfaceType = surfaceTypeService.findByIdOrThrow(dto.surfaceTypeId());
        return Court.builder()
                .name(dto.name())
                .surfaceType(surfaceType)
                .build();
    }

    public CourtResponseDto mapToResponseDto(Court court) {
        SurfaceType st = court.getSurfaceType();
        SurfaceTypeResponseDto stDto = new SurfaceTypeResponseDto(
                st.getId(), st.getName(), st.getPricePerMinute()
        );
        return new CourtResponseDto(court.getId(), court.getName(), stDto);
    }

}

