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

/**
 * PdfExporter is a utility class responsible for exporting Battleship game move history
 * to a PDF document. This class provides functionality to generate a formatted PDF report
 * containing detailed information about all moves, shots, and their results throughout
 * a game session.
 *
 * <p>The class handles file conflicts by automatically generating timestamped filenames
 * when the default PDF file is locked or in use by another process. This ensures that
 * multiple export operations can occur without errors.
 *
 * @author Battleship Team
 * @version 2.0
 * @since 2026
 *
 * @see IMove
 * @see IPosition
 * @see IGame.ShotResult
 */
public class PdfExporter {

    /**
     * The default output filename for the exported PDF document.
     * If this file is locked by another process, a timestamped alternative will be used instead.
     */
    private static final String OUTPUT_FILE = "battleship_game.pdf";

    /**
     * Exports the game move history to a PDF document.
     *
     * <p>This method generates a comprehensive PDF report containing all moves from a Battleship game.
     * @param moves A list of {@link IMove} objects representing all moves made during the game.
     *              Each move contains the shots fired and their corresponding results.
     *              Must not be null.
     *
     * @throws Exception If an error occurs during PDF document creation or writing.
     *                   The exception is caught and printed to the console rather than propagated.
     *
     * @see IMove
     * @see IPosition
     * @see IGame.ShotResult
     * @see java.time.LocalDateTime
     */
    public static void exportGameToPdf(List<IMove> moves) {
        String outputFile = OUTPUT_FILE;

        // Attempt to delete the existing file to avoid file lock conflicts
        // If deletion fails, use a timestamped filename as a fallback
        try {
            File file = new File(OUTPUT_FILE);
            if (file.exists() && !file.delete()) {
                // File exists but cannot be deleted (likely locked by another process)
                // Generate a timestamped filename to avoid overwrite conflicts
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
                String timestamp = LocalDateTime.now().format(formatter);
                outputFile = "battleship_game_" + timestamp + ".pdf";
            }
        } catch (Exception e) {
            // Exception occurred during file deletion attempt
            // Use timestamped filename as fallback
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
            String timestamp = LocalDateTime.now().format(formatter);
            outputFile = "battleship_game_" + timestamp + ".pdf";
        }

        try {
            // Create PDF writer and document structures
            PdfWriter writer = new PdfWriter(outputFile);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Add title to the document with formatting
            document.add(new Paragraph("Batalha Naval- Histórico de Jogadas").setBold().setFontSize(18));

            // Create and configure table with 3 columns (Move Number, Shots, Results)
            Table table = new Table(3);
            table.addHeaderCell(new Cell().add(new Paragraph("Jogada").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Tiros").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Resultados").setBold()));

            // Populate table with move data
            for (IMove move : moves) {
                // Add move number
                table.addCell(new Cell().add(new Paragraph("Jogada nº" + move.getNumber())));

                // Add all shots for this move
                StringBuilder shotsBuilder = new StringBuilder();
                for (IPosition shot : move.getShots()) {
                    shotsBuilder.append(shot.toString()).append(", ");
                }
                table.addCell(new Cell().add(new Paragraph(shotsBuilder.toString())));

                // Add detailed shot results
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

            // Add table to document and close
            document.add(table);
            document.close();
            System.out.println("Game exported to " + outputFile);
        } catch (Exception e) {
            // Log error with message and stack trace for debugging
            System.out.println("Erro ao exportar PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
