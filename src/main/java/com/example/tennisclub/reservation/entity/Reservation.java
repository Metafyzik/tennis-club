package com.example.tennisclub.reservation.entity;


import com.example.tennisclub.court.entity.Court;
import com.example.tennisclub.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservation")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@ToString
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "court_id", nullable = false)
    private Court court;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "is_doubles", nullable = false)
    private Boolean isDoubles;

    @Column(name = "total_price")
    private Double totalPrice;

    @Builder.Default
    private Boolean deleted = false;
}
