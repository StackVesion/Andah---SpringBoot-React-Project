package tn.esprit.reservation_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.reservation_service.entity.Reservation;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepo extends JpaRepository<Reservation,Long> {
    Optional<Reservation> findByScooterId(Long scooterId);
    List<Reservation> findAllByUserId(String userId);
}
