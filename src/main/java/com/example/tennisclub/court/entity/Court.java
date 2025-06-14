package com.example.tennisclub.court.entity;


import com.example.tennisclub.surfaceType.entity.SurfaceType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "court")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Court {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(optional = false)
    @JoinColumn(name = "surface_type_id", nullable = false)
    private SurfaceType surfaceType;

    @Builder.Default
    private Boolean deleted = false;
}
