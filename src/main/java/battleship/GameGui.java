package battleship;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Controlador principal da Interface Gráfica (GUI) para o jogo Batalha Naval.
 * Esta classe utiliza o padrão Singleton para garantir que apenas uma janela de monitorização
 * esteja ativa, permitindo atualizações em tempo real a partir da consola ou do simulador.
 * * A interface exibe dois tabuleiros lado a lado: a frota do jogador e o radar de ataques,
 * acompanhados por uma legenda explicativa das cores.</p>
 */
public class GameGui {

    /** Instância única da interface (Singleton). */
    private static GameGui instance;

    /** Janela principal do JavaFX. */
    private Stage stage;

    /** Referência para o estado atual do jogo. */
    private IGame game;

    /** Componente visual do tabuleiro do jogador. */
    private BoardView myBoard;

    /** Componente visual do radar de ataques ao adversário. */
    private BoardView radarBoard;

    /**
     * Exibe a janela da GUI. Se a janela já existir, apenas atualiza os dados do jogo
     * e redesenha os componentes.
     *
     * @param game A instância do jogo {@link IGame} a ser visualizada.
     */
    public static void show(IGame game) {
        if (instance == null) {
            instance = new GameGui(game);
        } else {
            instance.game = game;
            instance.refresh();
        }
    }

    /**
     * Solicita a atualização dos tabuleiros na Thread da aplicação JavaFX.
     * Este método deve ser chamado sempre que houver uma alteração no estado do jogo
     * (ex: após um disparo no comando 'rajada' ou no 'simula').
     */
    public static void update() {
        if (instance != null) {
            Platform.runLater(() -> instance.refresh());
        }
    }

    /**
     * Construtor privado para inicializar a interface com um jogo específico.
     *
     * @param game Instância do jogo.
     */
    private GameGui(IGame game) {
        this.game = game;
        this.stage = new Stage();
        initContent();
    }

    /**
     * Configura a estrutura principal da janela, incluindo layouts, tabuleiros e legenda.
     */
    private void initContent() {
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setAlignment(Pos.CENTER);

        HBox boardsLayout = new HBox(40);
        boardsLayout.setAlignment(Pos.CENTER);

        // Inicialização dos tabuleiros
        myBoard = new BoardView(game.getMyFleet(), game.getAlienMoves(), false);
        radarBoard = new BoardView(null, game.getMyMoves(), true);

        VBox leftBox = createBoardContainer("A MINHA FROTA", myBoard);
        VBox rightBox = createBoardContainer("RADAR DE ATAQUE", radarBoard);

        boardsLayout.getChildren().addAll(leftBox, rightBox);

        // Legenda de cores
        HBox legend = createLegend();

        mainLayout.getChildren().addAll(boardsLayout, legend);

        Scene scene = new Scene(mainLayout);
        stage.setTitle("Batalha Naval - Monitor de Jogo");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Cria um contentor vertical para um tabuleiro, incluindo um título estilizado.
     *
     * @param title O título do tabuleiro.
     * @param board A instância de {@link BoardView} a conter.
     * @return Um {@link VBox} formatado.
     */
    private VBox createBoardContainer(String title, BoardView board) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        Text t = new Text(title);
        t.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        box.getChildren().addAll(t, board);
        return box;
    }

    /**
     * Gera uma barra de legenda horizontal para identificar o significado das cores no tabuleiro.
     *
     * @return Um {@link HBox} contendo os itens da legenda.
     */
    private HBox createLegend() {
        HBox hBox = new HBox(20);
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().addAll(
                createLegendItem(Color.GRAY, "Navio"),
                createLegendItem(Color.RED, "Tiro Certeiro (*)"),
                createLegendItem(Color.WHITE, "Água (o)"),
                createLegendItem(Color.LIGHTBLUE, "Desconhecido")
        );
        return hBox;
    }

    /**
     * Cria um item individual da legenda com uma amostra de cor e um texto descritivo.
     *
     * @param color A cor a exibir no quadrado da legenda.
     * @param label A descrição da cor.
     * @return Um {@link HBox} com o quadrado colorido e o texto.
     */
    private HBox createLegendItem(Color color, String label) {
        HBox item = new HBox(5);
        item.setAlignment(Pos.CENTER);
        Rectangle r = new Rectangle(15, 15, color);
        r.setStroke(Color.BLACK);
        item.getChildren().addAll(r, new Text(label));
        return item;
    }

    /**
     * Redesenha os tabuleiros com os dados mais recentes do jogo.
     */
    private void refresh() {
        myBoard.renderBoard(game.getMyFleet(), game.getAlienMoves(), false);
        radarBoard.renderBoard(null, game.getMyMoves(), true);
    }
}