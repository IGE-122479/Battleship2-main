package battleship;

import java.util.Scanner;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * The type Tasks.
 */
public class Tasks {
	/**
	 * The constant LOGGER.
	 */
	private static final Logger LOGGER = LogManager.getLogger();

	/**
	 * The constant GOODBYE_MESSAGE.
	 */
	private static final String GOODBYE_MESSAGE = "Bons ventos!";

	/**
	 * Strings to be used by the user
	 */
	enum Command {
		AJUDA("ajuda"),
		GERAFROTA("gerafrota"),
		LEFROTA("lefrota"),
		DESISTIR("desisto"),
		RAJADA("rajada"),
		TIROS("tiros"),
		MAPA("mapa"),
		STATUS("estado"),
		SIMULA("simula"),
		GUARDAPDF("guardapdf"),
		TEMPO("tempo"),
		SCOREBOARD("scoreboard"),
		MAPAADV("mapaadversario"),
		RAJADAIA("rajadaia");

		final String value;

		Command(String value) {
			this.value = value;
		}
	}

	/**
	 * Creates the Game for a given fleet.
	 * Override in tests to return a controlled fake.
	 */
	protected IGame createGame(IFleet fleet) {
		return new Game(fleet);
	}

	/**
	 * Creates the AiGame.
	 * Override in tests to return a controlled fake (or throw on purpose).
	 */
	protected AiGame createAiGame() {
		return new AiGame();
	}

	/**
	 * This task also tests the fighting element of a round of three shots
	 */
	public void runMenu() {

		IFleet myFleet = null;
		IGame game = null;
		AiGame aiadversario = null;
		menuHelp();

		System.out.print("> ");
		Scanner in = new Scanner(System.in);
		String command = in.next();
		while (!command.equals(Command.DESISTIR.value)) {
			boolean gameEnded = false;
			switch (command) {
				case "gerafrota":
					myFleet = Fleet.createRandom();
					game = createGame(myFleet);
					System.out.println("A tua frota foi gerada! A frota do adversário está pronta.");
					game.printMyBoard(false, true);
					initJavaFX();

					final IGame currentGame = game;
					Platform.runLater(() -> {
						GameGui.show(currentGame);
					});

					break;
				case "lefrota":
					myFleet = buildFleet(in);
					game = createGame(myFleet);
					game.printMyBoard(false, true);
					break;
				case "estado":
					handleEstado(game, myFleet);
					break;
				case "mapa":
					if (myFleet != null)
						game.printMyBoard(false, true);
					break;
				case "mapaadversario":
					handleMapaAdversario(game);
					break;
				case "rajada":
					// O jogador ataca o adversário
					if (game instanceof Game g) {
						System.out.println("--- O teu ataque ---");
						g.readMyFire(in);
						game.getAlienFleet().printStatus();
						g.printAlienBoard(true, false);

						// Verificar se o jogador ganhou
						if (g.getAlienRemainingShips() == 0) {
							g.win();
							gameEnded = true;
						}
						// Resposta do adversário (se o jogo ainda não terminou)
						if (!gameEnded) {
							gameEnded = playEnemyTurn(game, myFleet, game::randomEnemyFire);
						}

					} else {
						System.out.println("Nenhum jogo em curso. Usa 'gerafrota' primeiro.");
					}
					break;
				case "simula":
					if (game != null) {
						final IGame gameParaGUI = game;
						initJavaFX();
						Platform.runLater(() -> GameGui.show(gameParaGUI));
						while (game.getRemainingShips() > 0) {
							game.randomEnemyFire();
							GameGui.update();
							myFleet.printStatus();
							game.printMyBoard(true, false);

							try {
								Thread.sleep(3000);
							} catch (InterruptedException e) {
								Thread.currentThread().interrupt();
								break;
							}
						}

						if (game.getRemainingShips() == 0) {
							game.over();
							// Removido o System.exit para não fechar a GUI imediatamente no fim
						}
					} else {
						System.out.println("Nenhum jogo em curso. Usa 'gerafrota' primeiro.");
					}
					break;
				case "tiros":
					if (game != null)
						game.printMyBoard(true, true);
					break;
				case "tempo":
					handleTempo(game);
					break;
				case "ajuda":
					menuHelp();
					break;
				case "guardapdf":
					handleGuardaPdf(game);
					break;
				case "rajadaia":
					if (game instanceof Game g) {
						if (aiadversario == null) {
							try {
								aiadversario = createAiGame();
							} catch (IllegalStateException e) {
								System.out.println("API_KEY não definida. Define-a nas Run Configurations.");
								break;
							}
						}

						System.out.println("--- O teu ataque ---");
						g.readMyFire(in);
						game.getAlienFleet().printStatus();
						g.printAlienBoard(true, false);

						if (g.getAlienRemainingShips() == 0) {
							g.win();
							gameEnded = true;
						}

						// Resposta do AI (se o jogo ainda não terminou)
						if (!gameEnded) {
							final AiGame ai = aiadversario;
							final IGame gameRef = game;
							gameEnded = playEnemyTurn(game, myFleet, () -> {
								try {
									ai.generateShots(gameRef);
								} catch (RuntimeException e) {
									System.out.println("Erro: " + e.getMessage() + " — usando fallback aleatório.");
									gameRef.randomEnemyFire();
								}
							});
						}
					} else {
						System.out.println("Nenhum jogo em curso. Usa 'gerafrota' primeiro.");
					}
					break;
				case "scoreboard":
					ScoreboardManager.printScoreboard();
					break;
				default:
					System.out.println("Que comando é esse??? Repete ...");
			}
			// Se o jogo terminou, forçar saída do menu
			if (gameEnded) {
				command = Command.DESISTIR.value;
			} else {
				System.out.print("> ");
				command = in.next();
			}
		}
		System.out.println(GOODBYE_MESSAGE);
	}

	private static boolean playEnemyTurn(IGame game, IFleet myFleet, Runnable enemyFireAction) {
		System.out.println("--- Ataque do adversário ---");
		enemyFireAction.run();
		myFleet.printStatus();
		game.printMyBoard(true, false);
		GameGui.update();
		if (game.getRemainingShips() == 0) {
			game.over();
			return true;
		}
		return false;
	}

	private static void initJavaFX() {
		try {
			// Tenta iniciar o JavaFX. O catch ignora se já estiver iniciado.
			Platform.startup(() -> {
			});
		} catch (IllegalStateException e) {
			// Toolkit já estava iniciado, podemos continuar
		}
	}

	private static void handleGuardaPdf(IGame game) {
		if (game != null)
			PdfExporter.exportGameToPdf(game);
		else
			System.out.println("Nenhum jogo em andamento para exportar.");
	}

	private static void handleTempo(IGame game) {
		// Mostrar a tabela do relógio das jogadas
		if (game instanceof Game g)
			g.printTimingStats();
		else
			System.out.println("Nenhum jogo em curso ou nenhuma jogada registada.");
	}

	private static void handleMapaAdversario(IGame game) {
		// Mostra o tabuleiro do adversário (apenas os meus tiros — não revela os navios)
		if (game instanceof Game g)
			g.printAlienBoard(true, true);
		else
			System.out.println("Nenhum jogo em curso.");
	}

	private static void handleEstado(IGame game, IFleet myFleet) {
		// Mostra o estado das duas frotas
		if (game != null) {
			System.out.print("A minha frota  -> ");
			myFleet.printStatus();
			System.out.print("Adversário     -> ");
			game.getAlienFleet().printStatus();
		}
	}

	/**
	 * Static entry-point — delegates to a default Tasks instance.
	 */
	public static void menu() {
		new Tasks().runMenu();
	}

	/**
	 * This function provides help information about the menu commands.
	 */
	public static void menuHelp() {
		System.out.println("======================= AJUDA DO MENU =========================");
		System.out.println("Digite um dos comandos abaixo para interagir com o jogo:");
		System.out.println("- " + Command.GERAFROTA.value + ": Gera uma frota aleatória de navios.");
		System.out.println("- " + Command.LEFROTA.value + ": Permite criar e carregar uma frota personalizada.");
		System.out.println("- " + Command.STATUS.value + ": Mostra o status atual da frota.)");
		System.out.println("- " + Command.MAPA.value + ": Exibe o mapa da frota.");
		System.out.println("- " + Command.MAPAADV.value + ": Exibe o tabuleiro do adversário (só os teus tiros).");
		System.out.println("- " + Command.RAJADA.value + ": Realiza uma rajada de disparos.");
		System.out.println("- " + Command.SIMULA.value + ": Simula um jogo completo.");
		System.out.println("- " + Command.TIROS.value + ": Lista os tiros válidos realizados (* = tiro em navio, o = tiro na água)");
		System.out.println("- " + Command.TEMPO.value + ": Mostra o relógio com o tempo gasto em cada jogada.");
		System.out.println("- " + Command.DESISTIR.value + ": Encerra o jogo.");
		System.out.println("- " + Command.GUARDAPDF.value + ": Exporta o histórico de jogadas para um arquivo PDF.");
		System.out.println("- " + Command.SCOREBOARD.value + ": Mostra o scoreboard dos jogos passados. ");
		System.out.println("- " + Command.RAJADAIA.value + ": Jogas contra a IA");
		System.out.println("===============================================================");
	}

	/**
	 * This operation allows the build up of a fleet, given user data
	 *
	 * @param in The scanner to read from
	 * @return The fleet that has been built
	 */
	public static Fleet buildFleet(Scanner in) {

		assert in != null;

		Fleet fleet = new Fleet();
		int i = 0; // i represents the total of successfully created ships
		while (i < Fleet.FLEET_SIZE) {
			IShip s = readShip(in);
			if (s != null) {
				boolean success = fleet.addShip(s);
				if (success)
					i++;
				else
					LOGGER.info("Falha na criacao de {} {} {}", s.getCategory(), s.getBearing(), s.getPosition());
			} else {
				LOGGER.info("Navio desconhecido!");
			}
		}
		LOGGER.info("{} navios adicionados com sucesso!", i);
		return fleet;
	}

	/**
	 * This operation reads data about a ship, build it and returns it
	 *
	 * @param in The scanner to read from
	 * @return The created ship based on the data that has been read
	 */
	public static Ship readShip(Scanner in) {

		assert in != null;

		String shipKind = in.next();
		Position pos = readPosition(in);
		char c = in.next().charAt(0);
		Compass bearing = Compass.charToCompass(c);
		return Ship.buildShip(shipKind, bearing, pos);
	}

	/**
	 * This operation allows reading a position in the map
	 *
	 * @param in The scanner to read from
	 * @return The position that has been read
	 */
	public static Position readPosition(Scanner in) {

		assert in != null;

		int row = in.nextInt();
		int column = in.nextInt();
		return new Position(row, column);
	}

	/**
	 * This operation allows reading a position in the map
	 *
	 * @param in The scanner to read from
	 * @return The classic position that has been read
	 */
	public static IPosition readClassicPosition(@NotNull Scanner in) {
		// Verifica se ainda há tokens disponíveis
		if (!in.hasNext()) {
			throw new IllegalArgumentException("Nenhuma posição válida encontrada!");
		}

		String part1 = in.next(); // Primeiro token
		String part2 = null;

		if (in.hasNextInt()) {
			part2 = in.next(); // Segundo token, se disponível
		}

		part1 = part1.toUpperCase();

		if (part2 == null && isCompactFormat(part1))
			return parseCompactFormat(part1);

		if (part2 != null && isSplitFormat(part1))
			return parseSplitFormat(part1, part2);

		throw new IllegalArgumentException("Formato inválido. Use 'A3', 'A 3' ou similar.");
	}

	/** Verifica se o token segue o formato compacto, ex: "A3", "J10". */
	private static boolean isCompactFormat(String token) {
		return token.matches("[A-Z]\\d+");
	}

	/** Verifica se o token é uma letra isolada, ex: "A", "J". */
	private static boolean isSplitFormat(String token) {
		return token.matches("[A-Z]");
	}

	/** Interpreta um token compacto "A3" → Position('A', 3). */
	private static IPosition parseCompactFormat(String token) {
		char column = token.charAt(0);
		int  row    = Integer.parseInt(token.substring(1));
		return new Position(column, row);
	}

	/** Interpreta dois tokens separados "A" "3" → Position('A', 3). */
	private static IPosition parseSplitFormat(String letter, String number) {
		return new Position(letter.charAt(0), Integer.parseInt(number));
	}

}
