package com.example.tennisclub.surfaceType.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "surface_type")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class SurfaceType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "price_per_minute", nullable = false)
    private Double pricePerMinute;

    @Builder.Default
    private Boolean deleted = false;

}
