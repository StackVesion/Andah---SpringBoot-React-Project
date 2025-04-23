package tn.esprit.reservation_service.dto;

import org.springframework.stereotype.Service;
import tn.esprit.reservation_service.entity.Reservation;

@Service
public class ReservationMapper {

    public Reservation toReservation(RequestReservation request){
        return Reservation.builder()
                .userId(request.userId())
                .numberHours(request.numberHours())
                .scooterId(request.scooterId())
                .status(request.status())

                .build();
    }

}
