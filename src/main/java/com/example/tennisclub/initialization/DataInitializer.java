
package com.example.tennisclub.initialization;


import com.example.tennisclub.court.CourtService;
import com.example.tennisclub.court.entity.Court;
import com.example.tennisclub.reservation.ReservationService;
import com.example.tennisclub.reservation.dto.ReservationRequestDto;
import com.example.tennisclub.surfaceType.SurfaceTypeService;
import com.example.tennisclub.surfaceType.entity.SurfaceType;
import com.example.tennisclub.user.Role;
import com.example.tennisclub.user.UserService;
import com.example.tennisclub.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SurfaceTypeService surfaceTypeService;
    private final CourtService courtService;
    private final DataInitializerProperties properties;

    private final UserService userService;
    private final ReservationService reservationService;

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

        Court createdCourt1 = courtService.save(court1);
        Court createdCourt2 = courtService.save(court2);
        courtService.save(court3);
        courtService.save(court4);

        //Create users
        String dummyPassword = "12345";

        User admin = User.builder()
                .phoneNumber("1234567890")
                .password(dummyPassword)
                .username("Alice")
                .roles(Set.of(Role.ADMIN))
                .build();

        User member = User.builder()
                .phoneNumber("0987654321")
                .password(dummyPassword)
                .username("Bob")
                .roles(Set.of(Role.MEMBER))
                .build();

        userService.save(admin);
        User createdMemberUser = userService.save(member);

        //Create reservations
        ReservationRequestDto reservation1 = new ReservationRequestDto(
                createdCourt1.getId(), // courtId
                false, // isDoubles
                createdMemberUser.getPhoneNumber(),
                LocalDateTime.now().plusDays(1).withHour(10).withMinute(0), // start time (future)
                LocalDateTime.now().plusDays(1).withHour(11).withMinute(0)  // end time (1 hour later)
        );

        ReservationRequestDto reservation2 = new ReservationRequestDto(
                createdCourt2.getId(), // courtId
                true, // isDoubles
                createdMemberUser.getPhoneNumber(),
                LocalDateTime.now().plusDays(2).withHour(15).withMinute(30), // start time (future)
                LocalDateTime.now().plusDays(2).withHour(17).withMinute(0)   // end time (1.5 hours later)
        );

        reservationService.create(reservation1);
        reservationService.create(reservation2);
    }
}



