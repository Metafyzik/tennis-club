
package com.example.tennisclub.initialization;


import com.example.tennisclub.court.CourtService;
import com.example.tennisclub.court.entity.Court;
import com.example.tennisclub.surfaceType.SurfaceTypeService;
import com.example.tennisclub.surfaceType.entity.SurfaceType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SurfaceTypeService surfaceTypeService;
    private final CourtService courtService;
    private final DataInitializerProperties properties;

    @Override
    public void run(String... args) throws Exception {
        if (!properties.isInitData()) {
            return;
        }

        //Create surfaces
        SurfaceType clay = SurfaceType.builder()
                .name("clay")
                .pricePerMinute(0.5)
                .build();

        SurfaceType grass = SurfaceType.builder()
                .name("grass")
                .pricePerMinute(0.7)
                .build();

        SurfaceType clayType = surfaceTypeService.create(clay);
        SurfaceType grassType = surfaceTypeService.create(grass);

        //Create courts
        Court court1 = Court.builder()
                .surfaceType(clayType)
                .name("Court 1")
                .build();

        Court court2 = Court.builder()
                .surfaceType(clayType)
                .name("Court 2")
                .build();

        Court court3 = Court.builder()
                .surfaceType(grassType)
                .name("Court 3")
                .build();

        Court court4 = Court.builder()
                .surfaceType(grassType)
                .name("Court 4")
                .build();

        courtService.save(court1);
        courtService.save(court2);
        courtService.save(court3);
        courtService.save(court4);
    }
}

