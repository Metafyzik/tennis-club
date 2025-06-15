package com.example.tennisclub.court;



import com.example.tennisclub.court.dto.CourtRequestDto;
import com.example.tennisclub.court.dto.CourtResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courts")
@RequiredArgsConstructor
public class CourtController {

    private final CourtService courtService;

    @GetMapping
    public List<CourtResponseDto> getAllCourts() {
        return courtService.getAllCourts();
    }

    @GetMapping("/{id}")
    public CourtResponseDto getCourt(@PathVariable Long id) {
        return courtService.getCourt(id);
    }

    @PostMapping
    public  ResponseEntity<CourtResponseDto> create(@RequestBody CourtRequestDto dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(courtService.create(dto));
    }

    @PutMapping("/{id}")
    public CourtResponseDto update(@PathVariable Long id, @RequestBody CourtRequestDto dto) {
        return courtService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> softDelete(@PathVariable Long id) {
        courtService.softDelete(id);
        return ResponseEntity.ok("Court with id "+ id + " deleted successfully.");
    }
}