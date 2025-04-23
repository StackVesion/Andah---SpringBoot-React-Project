package tn.esprit.reservation_service.service;

import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import tn.esprit.reservation_service.entity.ReservationHistory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@AllArgsConstructor
@Service
public class ExcelService {
    // Méthode pour générer un fichier Excel à partir d'une liste de données
    public void generateExcelHistory(List<ReservationHistory> history, String filePath) throws IOException {
        // Créer un classeur Excel
        Workbook workbook = new XSSFWorkbook();
        // Créer une feuille de travail dans le classeur
        Sheet sheet = workbook.createSheet("Reservation History");

        // Créer une ligne pour les en-têtes
        Row headerRow = sheet.createRow(0);
        String[] columns = {"ID", "Ancien Statut", "Nouveau Statut", "ID Réservation", "Date du Changement", "Approuvé"};

        // Remplir les en-têtes dans la première ligne
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
        }

        // Remplir les données dans les lignes suivantes
        int rowNum = 1;
        for (ReservationHistory historyRecord : history) {
            Row row = sheet.createRow(rowNum++);
           row.createCell(0).setCellValue(historyRecord.getId().toString());
            row.createCell(1).setCellValue(historyRecord.getOldStatus().toString());
            row.createCell(2).setCellValue(historyRecord.getNewStatus().toString());
            row.createCell(3).setCellValue(historyRecord.getIdReservation());
            row.createCell(4).setCellValue(historyRecord.getChangedOn().toString());
            row.createCell(5).setCellValue(historyRecord.getApproved().toString());
        }

        // Écriture dans un fichier
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
        } finally {
            workbook.close();
        }
    }
}
