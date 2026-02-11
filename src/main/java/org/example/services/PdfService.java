package org.example.services;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import org.example.entities.Paiement;

import java.io.File;
import java.io.IOException;

public class PdfService {

    public static void generateReceipt(Paiement p) throws IOException {
        String dest = "receipts/recu_" + p.getIdPaiement() + ".pdf";
        File file = new File("receipts");
        if (!file.exists()) file.mkdirs();

        PdfWriter writer = new PdfWriter(dest);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Header
        document.add(new Paragraph("REÇU DE PAIEMENT")
                .setBold()
                .setFontSize(20)
                .setTextAlignment(TextAlignment.CENTER));
        
        document.add(new Paragraph("Généré par GestionPaiementApp")
                .setTextAlignment(TextAlignment.CENTER)
                .setItalic());

        document.add(new Paragraph("\n"));

        // Table
        float[] columnWidths = {100f, 200f};
        Table table = new Table(columnWidths);

        table.addCell("Référence:");
        table.addCell("#" + p.getIdPaiement());

        table.addCell("Montant:");
        table.addCell(String.format("%.2f DT", p.getMontant()));

        table.addCell("Date:");
        table.addCell(p.getDatePaiement().toString());

        table.addCell("Méthode:");
        table.addCell(p.getMethodePaiement());

        table.addCell("Statut:");
        table.addCell(p.getStatutPaiement());

        document.add(table);

        document.add(new Paragraph("\nMerci pour votre confiance !")
                .setTextAlignment(TextAlignment.CENTER));

        document.close();
        System.out.println("✅ PDF généré: " + dest);
    }
}
