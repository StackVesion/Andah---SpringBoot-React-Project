package tn.esprit.reservation_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Builder
public class ReservationHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private ReservationStatus oldStatus;
    @Enumerated(EnumType.STRING)
    private ReservationStatus newStatus;
    private Long idReservation;
    private LocalDate changedOn;
    private Boolean approved;
    private String userId;
    private Long scooterId;
}
