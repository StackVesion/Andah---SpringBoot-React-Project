package tn.esprit.reservation_service.controller;


import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.reservation_service.entity.ReservationHistory;
import tn.esprit.reservation_service.service.ReservationService;

import java.io.IOException;
import java.util.List;

@AllArgsConstructor
@RestController
@CrossOrigin(origins = "http://localhost:4200") // <- autorise Angular à appeler cette API
@RequestMapping("/history")
public class HistroyController {
     ReservationService reservationService;

     @GetMapping
    public ResponseEntity<?> getExcelSheetForHistoryReservation() throws IOException {
         reservationService.genererExcelFileForHistory();
         return ResponseEntity.ok("bien gerer");
     }
    @GetMapping("/all")
    public ResponseEntity<?> afficherReservationHistory(){
        return ResponseEntity.ok(reservationService.afficherReservationHistory());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> afficherReservationHistoryByUserId(
           @PathVariable String userId
    ){
        return ResponseEntity.ok(reservationService.afficherReservationHistoryByUserId(userId));
    }

}
