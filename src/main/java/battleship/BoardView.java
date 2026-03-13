package battleship;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class BoardView extends GridPane {
    private final int ROWS = 8;
    private final int COLS = 8;
    private final double CELL_SIZE = 50.0;

    public BoardView() {
        renderBoard();
    }

    // Método principal para desenhar o tabuleiro
    public void renderBoard() {
        this.getChildren().clear(); // Limpa antes de redesenhar

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                StackPane cell = createCell(row, col);
                this.add(cell, col, row);
            }
        }
    }

    private StackPane createCell(int row, int col) {
        StackPane pane = new StackPane();

        // Cria o fundo da casa (Alternando cores como um xadrez)
        Rectangle background = new Rectangle(CELL_SIZE, CELL_SIZE);
        background.setFill((row + col) % 2 == 0 ? Color.LIGHTGRAY : Color.GRAY);
        background.setStroke(Color.BLACK);

        pane.getChildren().add(background);

        // Exemplo: Adicionar um evento de clique na célula

      /*  pane.setOnMouseClicked(e -> {
            System.out.println("Clicou na posição: " + row + "," + col);
            // Aqui você chamaria a lógica do seu Controller/Jogo
        });*/
        return pane;
    }
}
