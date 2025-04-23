package tn.esprit.reservation_service.service;

import tn.esprit.reservation_service.dto.RequestReservation;
import tn.esprit.reservation_service.entity.Reservation;
import tn.esprit.reservation_service.entity.ReservationHistory;
import tn.esprit.reservation_service.entity.ReservationStatus;

import java.io.IOException;
import java.util.List;

public interface ReservationService {

    void genererExcelFileForHistory() throws IOException;
    void genererPDfFileForHistory() throws IOException;
    Reservation reserver(RequestReservation reservation);
    boolean annulerReservation(Long idReservation);
    List<Reservation> getReservationByUserId(String userID);
    boolean updateReservationStatus(Long idReservation, ReservationStatus status);
    Reservation getReservationById(Long idReservation);
    Boolean ApprovedReservation(Long idReservation);
    Boolean DesApprovedReservation(Long idReservation);
    List<ReservationHistory>afficherReservationHistoryByUserId(String userId);
    List<ReservationHistory>afficherReservationHistory();




}
