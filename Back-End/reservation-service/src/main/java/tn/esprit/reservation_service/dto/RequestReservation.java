package tn.esprit.reservation_service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tn.esprit.reservation_service.entity.ReservationStatus;

public record RequestReservation(

        @NotNull(message = "Le nombre d'heures ne peut pas être nul")
        @Min(value = 1, message = "Le nombre d'heures doit être supérieur ou égal à 1")
        @Max(value = 24, message = "Le nombre d'heures doit être inférieur ou égal à 24")
        Integer numberHours,

        @NotNull(message = "Le statut de la réservation est obligatoire")
        ReservationStatus status,

        @NotNull(message = "L'ID de l'utilisateur est obligatoire")
        @Size(min = 1, max = 50, message = "L'ID utilisateur doit être compris entre 1 et 50 caractères")
        String userId,

        @NotNull(message = "L'ID du scooter est obligatoire")
        Long scooterId,

        @NotNull(message = "Le statut de l'approbation est obligatoire")
        Boolean approved

) {
}
