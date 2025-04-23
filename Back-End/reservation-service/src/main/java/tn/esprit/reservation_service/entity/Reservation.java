package tn.esprit.reservation_service.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Builder
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    private Integer numberHours;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    private String userId;
    private Long scooterId;
    private Boolean approved;


}
