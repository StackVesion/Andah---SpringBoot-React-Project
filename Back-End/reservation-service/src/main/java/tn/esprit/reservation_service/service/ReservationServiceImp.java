package tn.esprit.reservation_service.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import tn.esprit.reservation_service.Exception.OperationNotPermitedException;
import tn.esprit.reservation_service.dto.RequestReservation;
import tn.esprit.reservation_service.dto.ReservationMapper;
import tn.esprit.reservation_service.entity.Reservation;
import tn.esprit.reservation_service.entity.ReservationHistory;
import tn.esprit.reservation_service.entity.ReservationStatus;
import tn.esprit.reservation_service.repository.HistoryRepositroy;
import tn.esprit.reservation_service.repository.ReservationRepo;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static tn.esprit.reservation_service.entity.ReservationStatus.*;

@AllArgsConstructor
@Service
public class ReservationServiceImp implements ReservationService{
    ExcelService excelService;
    HistoryRepositroy historyRepositroy;
    ReservationRepo reservationRepo;
    ReservationMapper  reservationMapper;
    //SmsService smsService;
    EmailService emailService;
    @Override
    public void genererExcelFileForHistory() throws IOException {
        List<ReservationHistory> reservationHistoryList = historyRepositroy.findAll();




        excelService.generateExcelHistory(reservationHistoryList,"/app/exports/reservations.xlsx");
    }

    @Override
    public void genererPDfFileForHistory() throws IOException {

    }

    @Override
    public Reservation reserver(RequestReservation reservation) {
        Optional<Reservation> reservation1 = reservationRepo.findByScooterId(reservation.scooterId());
        if(reservation1.isPresent()){
            throw new OperationNotPermitedException("you can not reserve scooter reserved ");
        }
        Reservation reservation2 =reservationMapper.toReservation(reservation);
        reservation2.setApproved(false);
        reservation2.setDate(LocalDate.now());
          Reservation reservation3 =reservationRepo.save(reservation2);
        ReservationHistory reservationHistory = ReservationHistory.builder()
                .idReservation(reservation3.getId())
                .approved(reservation3.getApproved())
                .changedOn(LocalDate.now())
                .oldStatus(reservation3.getStatus())
                .newStatus(reservation3.getStatus())
                .userId(reservation3.getUserId())
                .scooterId(reservation3.getScooterId())

                .build();
         historyRepositroy.save(reservationHistory);
         String message= "reservation of scotter with id " + reservation3.getScooterId();
// smsService.sendSms("+21629601848",message);
        return  reservation3;
    }

    @Override
    public boolean annulerReservation(Long idReservation) {

        Optional<Reservation> reservation = reservationRepo.findById(idReservation);
        if (reservation.isPresent()){
            Optional<ReservationHistory> reservationHistory = historyRepositroy.findByIdReservation(reservation.get().getId());
            if (reservationHistory.isPresent()) {
                reservationHistory.get().setNewStatus(CANCELLED);
                historyRepositroy.save(reservationHistory.get());
            }
            reservationRepo.delete(reservation.get());
            emailService.sendProfileModificationEmail("maram.naderi@esprit.tn","maram.nader");
            return true;
        }
        return false;
    }

    @Override
    public List<Reservation> getReservationByUserId(String userID) {
        return reservationRepo.findAllByUserId(userID);
    }

    @Override
    public boolean updateReservationStatus(Long idReservation, ReservationStatus status) {
        Optional<Reservation> reservation = reservationRepo.findById(idReservation);
        if (reservation.isPresent()){

            reservation.get().setStatus(status);
            Optional<ReservationHistory> reservationHistory = historyRepositroy.findByIdReservation(reservation.get().getId());
            if (reservationHistory.isPresent()) {

                reservationHistory.get().setNewStatus(status);
                historyRepositroy.save(reservationHistory.get());
            }
            reservationRepo.save(reservation.get());
            return true;
        }
        return false;
    }


    @Override
    public Reservation getReservationById(Long idReservation) {
        return reservationRepo.findById(idReservation).get();
    }

    @Override
    public Boolean ApprovedReservation(Long idReservation) {
          Optional<Reservation> reservation = reservationRepo.findById(idReservation);
          if (reservation.isPresent()){
              reservation.get().setApproved(true);
              reservation.get().setStatus(CONFIRMED);
              Optional<ReservationHistory> reservationHistory = historyRepositroy.findByIdReservation(reservation.get().getId());
              if (reservationHistory.isPresent()) {
                  reservationHistory.get().setApproved(true);
                  reservationHistory.get().setNewStatus(CONFIRMED);
                  historyRepositroy.save(reservationHistory.get());
              }
              reservationRepo.save(reservation.get());
              return true;
          }
          return false;
    }

    @Override
    public Boolean DesApprovedReservation(Long idReservation) {
        Optional<Reservation> reservation = reservationRepo.findById(idReservation);
        if (reservation.isPresent()){
            reservation.get().setApproved(false);
            reservation.get().setStatus(REJECTED);
            Optional<ReservationHistory> reservationHistory = historyRepositroy.findByIdReservation(reservation.get().getId());
            if (reservationHistory.isPresent()) {
                reservationHistory.get().setApproved(false);
                reservationHistory.get().setNewStatus(REJECTED);
                historyRepositroy.save(reservationHistory.get());
            }
            reservationRepo.save(reservation.get());
            return true;
        }
        return false;
    }

    @Override
    public List<ReservationHistory> afficherReservationHistoryByUserId(String userId) {
        return historyRepositroy.findAllByUserId(userId);
    }

    @Override
    public List<ReservationHistory> afficherReservationHistory() {
        return historyRepositroy.findAll();
    }
}
