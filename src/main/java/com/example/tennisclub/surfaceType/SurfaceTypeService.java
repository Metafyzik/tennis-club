package com.example.tennisclub.surfaceType;


import com.example.tennisclub.exception.EntityFinder;
import com.example.tennisclub.surfaceType.entity.SurfaceType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SurfaceTypeService {

    private final SurfaceTypeRepository surfaceTypeRepository;
    private final EntityFinder entityFinder;

    public SurfaceType findByIdOrThrow(Long id) {
        return entityFinder.findByIdOrThrow(
                surfaceTypeRepository.findById(id), id, "SurfaceType");
    }

    @Transactional
    public SurfaceType create(SurfaceType surfaceType) {
        return surfaceTypeRepository.save(surfaceType);
    }
}

