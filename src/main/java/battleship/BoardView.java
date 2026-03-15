package battleship;

import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import java.util.List;

/**
 * Representa a visualização gráfica de um tabuleiro de Batalha Naval utilizando JavaFX.
 * Esta classe estende {@link GridPane} para organizar as células, letras e números
 * numa grelha bidimensional.
 *
 * O tabuleiro inclui coordenadas (A-J e 1-10) e diferencia visualmente entre a
 * visão da frota própria e a visão do radar (inimigo).
 */
public class BoardView extends GridPane {

    /** Tamanho padrão do tabuleiro (10x10). */
    private final int SIZE = 10;

    /** Tamanho em pixels de cada célula quadrada. */
    private final double CELL_SIZE = 35.0;

    /**
     * Constrói uma nova visualização do tabuleiro e renderiza o seu conteúdo inicial.
     *
     * @param fleet       A frota a ser exibida (pode ser nula se for apenas visualização de ataques).
     * @param moves       A lista de jogadas (movimentos) realizados para exibir os tiros efetuados.
     * @param isEnemyView Se verdadeiro, oculta os navios que ainda não foram atingidos (visão de radar).
     */
    public BoardView(IFleet fleet, List<IMove> moves, boolean isEnemyView) {
        renderBoard(fleet, moves, isEnemyView);
    }

    /**
     * Responsável por desenhar todos os componentes do tabuleiro, incluindo as etiquetas
     * de coordenadas e as células de jogo.
     *
     * @param fleet       A frota de navios para determinar as posições ocupadas.
     * @param moves       O histórico de movimentos para determinar os tiros (água ou acerto).
     * @param isEnemyView Define se a renderização deve seguir as regras de visualização do adversário.
     */
    public void renderBoard(IFleet fleet, List<IMove> moves, boolean isEnemyView) {
        this.getChildren().clear();
        this.setAlignment(Pos.CENTER);

        // 1. Adicionar números no topo (1 a 10)
        for (int col = 0; col < SIZE; col++) {
            Text label = new Text(String.valueOf(col + 1));
            label.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            StackPane container = new StackPane(label);
            container.setPrefSize(CELL_SIZE, CELL_SIZE / 2);
            this.add(container, col + 1, 0);
        }

        // 2. Adicionar letras na esquerda (A a J) e as células do tabuleiro
        for (int row = 0; row < SIZE; row++) {
            Text label = new Text(String.valueOf((char) ('A' + row)));
            label.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            StackPane container = new StackPane(label);
            container.setPrefSize(CELL_SIZE / 2, CELL_SIZE);
            this.add(container, 0, row + 1);

            for (int col = 0; col < SIZE; col++) {
                Position currentPos = new Position(row, col);
                this.add(createCell(currentPos, fleet, moves, isEnemyView), col + 1, row + 1);
            }
        }
    }

    /**
     * Cria uma célula individual (StackPane) contendo a representação visual de uma posição.
     * A cor da célula muda conforme o estado: água, navio, tiro certeiro ou falhado.
     * Navios completamente afundados são pintados a cor diferente (laranja escuro).
     *
     * @param pos         A posição correspondente na quadrícula.
     * @param fleet       A frota para verificar ocupação por navios.
     * @param moves       Os movimentos para verificar se a posição foi atingida.
     * @param isEnemyView Se verdadeiro, não pinta os navios (cinzento) se não houver dano.
     * @return Um {@link StackPane} configurado com a cor e bordas adequadas.
     */
    private StackPane createCell(Position pos, IFleet fleet, List<IMove> moves, boolean isEnemyView) {
        StackPane pane = new StackPane();
        Rectangle rect = new Rectangle(CELL_SIZE, CELL_SIZE);
        rect.setStroke(Color.DARKBLUE);
        rect.setFill(Color.LIGHTBLUE); // Cor base: desconhecido

        // Verifica se a posição pertence a um navio afundado
        boolean isSunkShipCell = false;

        // Lógica de cores para Navios
        if (fleet != null && fleet.getShips() != null) {
            for (IShip ship : fleet.getShips()) {
                if (ship.occupies(pos)) {
                    if (!isEnemyView) {
                        // Navio afundado → cor especial (laranja escuro)
                        if (!ship.stillFloating()) {
                            rect.setFill(Color.DARKORANGE);
                            isSunkShipCell = true;
                        } else {
                            rect.setFill(Color.GRAY); // Navio intacto
                        }
                    }
                }
            }
        }

        // Lógica de cores para Tiros (sobrepõe a cor do navio se houver impacto)
        if (moves != null) {
            for (IMove move : moves) {
                List<IPosition> shots = move.getShots();
                List<IGame.ShotResult> results = move.getShotResults();
                for (int i = 0; i < shots.size(); i++) {
                    if (shots.get(i).equals(pos)) {
                        IGame.ShotResult result = results.get(i);
                        if (result.ship() != null) {
                            if (result.sunk()) {
                                rect.setFill(Color.DARKORANGE); // Navio afundado
                            } else {
                                rect.setFill(Color.RED); // Tiro certeiro (não afundado)
                            }
                        } else {
                            rect.setFill(Color.WHITE); // Tiro na água
                        }
                    }
                }
            }
        }

        pane.getChildren().add(rect);
        return pane;
    }
}