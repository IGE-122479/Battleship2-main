package battleship;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfExporter {

    private static final String OUTPUT_FILE = "battleship_game.pdf";

    public static void exportGameToPdf(List<IMove> moves) {
        String outputFile = OUTPUT_FILE;

        try {
            File file = new File(OUTPUT_FILE);
            if (file.exists() && !file.delete()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
                String timestamp = LocalDateTime.now().format(formatter);
                outputFile = "battleship_game_" + timestamp + ".pdf";
            }
        } catch (Exception e) {

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
            String timestamp = LocalDateTime.now().format(formatter);
            outputFile = "battleship_game_" + timestamp + ".pdf";
        }

        try {
            PdfWriter writer = new PdfWriter(outputFile);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            document.add(new Paragraph("Batalha Naval- Histórico de Jogadas").setBold().setFontSize(18));

            Table table = new Table(3);
            table.addHeaderCell(new Cell().add(new Paragraph("Jogada").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Tiros").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Resultados").setBold()));

            for (IMove move : moves) {
                table.addCell(new Cell().add(new Paragraph("Jogada nº" + move.getNumber())));

                StringBuilder shotsBuilder = new StringBuilder();
                for (IPosition shot : move.getShots()) {
                    shotsBuilder.append(shot.toString()).append(", ");
                }
                table.addCell(new Cell().add(new Paragraph(shotsBuilder.toString())));

                StringBuilder resultsBuilder = new StringBuilder();
                for (IGame.ShotResult result : move.getShotResults()) {
                    if (!result.valid())
                        resultsBuilder.append("Exterior, ");
                    else if (result.repeated())
                        resultsBuilder.append("Repetido, ");
                    else if (result.ship() == null)
                        resultsBuilder.append("Água, ");
                    else if (result.sunk())
                        resultsBuilder.append(result.ship().getCategory() + " afundado ");
                    else
                        resultsBuilder.append("Acerto em " + result.ship().getCategory() + " ");
                }
                table.addCell(new Cell().add(new Paragraph(resultsBuilder.toString())));
            }

            document.add(table);
            document.close();
            System.out.println("Game exported to " + outputFile);
        } catch (Exception e) {
            System.out.println("Erro ao exportar PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
