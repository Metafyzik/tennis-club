package com.example.tennisclub.integrationTest;

import com.example.tennisclub.auth.dto.LogRequestDto;
import com.example.tennisclub.auth.dto.RegistRequestDto;
import com.example.tennisclub.auth.dto.TokenResponseDto;
import com.example.tennisclub.court.CourtService;
import com.example.tennisclub.court.entity.Court;
import com.example.tennisclub.reservation.dto.ReservationRequestDto;
import com.example.tennisclub.surfaceType.SurfaceTypeService;
import com.example.tennisclub.surfaceType.entity.SurfaceType;
import com.example.tennisclub.user.Role;
import com.example.tennisclub.user.UserService;
import com.example.tennisclub.user.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReservationControllerAuthIT {

    @Autowired
    private  PasswordEncoder passwordEncoder;
    @Autowired
    private  UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SurfaceTypeService surfaceTypeService;
    @Autowired
    private CourtService courtService;
    private static String memberToken;
    private static String adminToken;

    //currently a user with admin role can be created only internally
    private void createAdminUser(String username, String password, String phone) {
        String encodedPassword = passwordEncoder.encode(password);

        User admin = User.builder()
                .phoneNumber(phone)
                .password(encodedPassword)
                .username(username)
                .roles(Set.of(Role.ADMIN))
                .build();

        userService.save(admin);
    }

    private Court createCourt() {
        //Court requires a surfaceType
        SurfaceType clay = SurfaceType.builder()
                .name("clay")
                .pricePerMinute(0.5)
                .build();

        SurfaceType clayType = surfaceTypeService.save(clay);

        //Create court
        Court court1 = Court.builder()
                .surfaceType(clayType)
                .name("Court 1")
                .build();

        return courtService.save(court1);
    }

    private String  registerAndLoginMember() throws Exception {
        System.out.println("registerAndLoginMember()");
        var username ="memberUser";
        var password = "pass";
        var phone = "123456789";

        RegistRequestDto register = new RegistRequestDto(username, phone, password);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());

        LogRequestDto login = new LogRequestDto(username, password);
        String json = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        TokenResponseDto tokens = objectMapper.readValue(json, TokenResponseDto.class);

        return tokens.accessToken();
    }


    @BeforeAll
    void setUp() throws Exception {
        memberToken = registerAndLoginMember();
        adminToken = createAndLoginAdmin();
    }

    @Test
    void memberCanAccessGeneralEndpoints_butNotAdminOnly() throws Exception {
        mockMvc.perform(get("/api/reservations")
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/reservations/my")
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/reservations/by-phone?phoneNumber=123456789")
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void memberCanCreateReservation() throws Exception {
        Court court = createCourt();

        ReservationRequestDto requestDto = new ReservationRequestDto(
                court.getId(),
                false,
                LocalDateTime.now().plusDays(1).withHour(10).withMinute(0),
                LocalDateTime.now().plusDays(1).withHour(11).withMinute(0)
        );

        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.court.id").value(court.getId()))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.totalPrice").exists());
    }

    private String createAndLoginAdmin() throws Exception {
        var username ="adminUser";
        var password = "pass";
        var phone = "193456789";

        createAdminUser(username,password,phone);

        LogRequestDto login = new LogRequestDto(username, password);
        String json = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        TokenResponseDto tokens = objectMapper.readValue(json, TokenResponseDto.class);

        return tokens.accessToken();
    }

    @Test
    void accessRestrictedEndpointsWithoutToken_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/reservations"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/reservations/by-phone?phoneNumber=123456789"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminCanAccessAllEndpoints() throws Exception {
        mockMvc.perform(get("/api/reservations")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/reservations/by-phone?phoneNumber=193456789")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void unauthenticatedUserCannotCreateReservation() throws Exception {
        ReservationRequestDto requestDto = new ReservationRequestDto(
                1L,
                false,
                LocalDateTime.now().plusDays(1).withHour(10),
                LocalDateTime.now().plusDays(1).withHour(11)
        );

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createReservationWithInvalidData_ShouldReturn400() throws Exception {
        ReservationRequestDto invalidDto = new ReservationRequestDto(
                null, // null courtId
                false,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).minusHours(1)
        );

        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }
}
