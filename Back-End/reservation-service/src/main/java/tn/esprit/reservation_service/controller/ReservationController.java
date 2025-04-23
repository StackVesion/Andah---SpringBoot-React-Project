package tn.esprit.reservation_service.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.reservation_service.dto.RequestReservation;
import tn.esprit.reservation_service.entity.ReservationStatus;
import tn.esprit.reservation_service.service.EmailService;
import tn.esprit.reservation_service.service.ReservationService;

import java.util.List;

@AllArgsConstructor
@CrossOrigin(origins = "http://localhost:4200") // <- autorise Angular à appeler cette API

@RestController
public class ReservationController {
    EmailService emailService;
    ReservationService reservationService;

    @PostMapping("/add")
    public ResponseEntity<?> reserver(
         @Valid @RequestBody RequestReservation reservation
    ){

       return ResponseEntity.ok(reservationService.reserver(reservation));
    }


    @DeleteMapping ("/annuler/{idReservation}")
    public ResponseEntity<?> annulerReservation(
          @PathVariable Long idReservation
    ){
           String message ;
           if(reservationService.annulerReservation(idReservation)){
               message ="canceled with success ";
           }else {
               message ="failed to canceled  ";
           }
        return ResponseEntity.ok(message);
    }

    @GetMapping("/userId/{userId}")
    public ResponseEntity<?> getReservationByUserId(
            @PathVariable String userId
    ){
        return ResponseEntity.ok(reservationService.getReservationByUserId(userId));
    }

    @GetMapping("/reservationId/{idReservation}")
    public ResponseEntity<?> getReservationById(
          @PathVariable  Long idReservation
    ){
        return ResponseEntity.ok(reservationService.getReservationById(idReservation));
    }

    @PutMapping("/approve/{idReservation}")
    public ResponseEntity<?> ApprovedReservation(
            @PathVariable Long idReservation
    ){
        String message ;
        if(reservationService.ApprovedReservation(idReservation)){
            message ="Confirmed with success ";
        }else {
            message ="failed to confirme  ";
        }
        return ResponseEntity.ok(message);
    }

    @PutMapping("/inapprove/{idReservation}")
    public ResponseEntity<?> DesapprovedReservation(
            @PathVariable Long idReservation
    ){
        String message ;
        if(reservationService.DesApprovedReservation(idReservation)){
            message ="Rejected with success ";
        }else {
            message ="failed to reject  ";
        }
        return ResponseEntity.ok(message);
    }

    @PutMapping("/status/{idReservation}/{status}")
    public ResponseEntity<?> updateReservationStatus(
           @PathVariable Long idReservation,
           @PathVariable ReservationStatus status
    ){
        String message ;
        if(reservationService.updateReservationStatus(idReservation,status)){
            message ="status updated with success ";
        }else {
            message ="failed to update  ";
        }
        return ResponseEntity.ok(message);
    }
}
