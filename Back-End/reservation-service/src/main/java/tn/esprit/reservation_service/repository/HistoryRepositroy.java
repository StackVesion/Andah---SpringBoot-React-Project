package tn.esprit.reservation_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.reservation_service.entity.Reservation;
import tn.esprit.reservation_service.entity.ReservationHistory;

import java.util.List;
import java.util.Optional;

@Repository
public interface HistoryRepositroy extends JpaRepository<ReservationHistory,Long> {
    Optional<ReservationHistory> findByIdReservation(Long idReservation);
    List<ReservationHistory> findAllByUserId(String userId);
}
