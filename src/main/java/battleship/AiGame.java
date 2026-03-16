package battleship;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Classe que gerencia o jogo de Batalha Naval contra uma IA baseada em LLM (Large Language Model).
 * A IA comunica com a API Hugging Face para decidir onde disparar os tiros.
 */
public class AiGame {

    // Configurações da API e modelo LLM
    private static final String MODEL_ID = "meta-llama/Llama-3.3-70B-Instruct";
    private static final String API_URL = "https://router.huggingface.co/v1/chat/completions";
    private static final int MAX_OUTPUT_TOKENS    = 256;      // Máximo de tokens na resposta da IA
    private static final int HTTP_TIMEOUT_SECONDS = 45;       // Tempo máximo para resposta da API
    private static final int MAX_RETRIES          = 3;        // Tentativas máximas se a IA responder mal

    // Cliente HTTP e ferramentas de manipulação JSON
    private final HttpClient   httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    // Histórico de mensagens para manter contexto na conversa com a IA
    private final List<Map<String, Object>> conversationHistory = new ArrayList<>();
    private boolean firstMove = true;  // Flag para saber se é o primeiro tiro


    /**
     * Construtor: inicializa o cliente HTTP e carrega a chave de API das variáveis de ambiente.
     * Se a chave não estiver definida, lança uma exceção.
     */
    public AiGame(){
        this.apiKey = System.getenv("API_TOKEN");
        if (this.apiKey == null || this.apiKey.isEmpty()) {
            throw new IllegalStateException("API_KEY environment variable is not set.");
        }
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(HTTP_TIMEOUT_SECONDS))
                .build();
        this.objectMapper = new ObjectMapper();

    }

    /**
     * Método principal: gera os tiros da IA consultando o LLM.
     * @param game Estado atual do jogo
     * @return Resposta processada do jogo com os resultados dos tiros
     */
    public String generateShots(IGame game){
        assert game != null;

        String userMessage = buildUserMessage(game);
        String shotsJson = askLlmForShots(userMessage);

        System.out.println("Tiros decididos pelo LLM: " + shotsJson);

        return game.readEnemyFire(shotsJson);
    }

    /**
     * Constrói o prompt do sistema que instruir a IA sobre as regras do jogo e estratégias.
     * Este é um prompt detalhado que explica à IA como jogar Batalha Naval.
     * @return String contendo o prompt completo do sistema
     */
    private String buildSystemPrompt() {
        return """
            És um perito na Batalha Naval, versão dos Descobrimentos Portugueses.
            O teu objetivo é afundar toda a frota inimiga da forma mais eficiente possível.
 
            === TABULEIRO ===
            Grelha 10x10. Linhas: A a J. Colunas: 1 a 10.
 
            === FROTA INIMIGA (que tens de afundar) ===
            - 4 Barcas      (1 posição)
            - 3 Caravelas   (2 posições, orientação N-S ou E-W)
            - 2 Naus        (3 posições, orientação N-S ou E-W)
            - 1 Fragata     (4 posições, orientação N-S ou E-W)
            - 1 Galeão      (5 posições, forma de T: corpo de 3 + 1 asa em cada extremidade)
            Os navios NUNCA se tocam, nem nas diagonais.
 
            === PROTOCOLO DE COMUNICAÇÃO (JSON) ===
            Cada rajada tua tem EXATAMENTE 3 tiros neste formato:
            [{"row":"A","column":5},{"row":"C","column":10},{"row":"F","column":5}]
 
            Após cada rajada, receberás o resultado com CADA COORDENADA associada
            ao seu resultado específico, por exemplo:
 
            EXEMPLO 1:
            Resultado da tua rajada:
            - A5 → Água
            - C10 → Acerto numa Barca (AFUNDADA!)
            - F5 → Água
            Resumo: 3 tiros válidos, 1 navio afundado (Barca), 2 na água.
 
            EXEMPLO 2:
            Resultado da tua rajada:
            - B3 → Água
            - E7 → Acerto numa Nau (ainda a flutuar)
            - H2 → Água
            Resumo: 3 tiros válidos, 1 acerto (Nau), 2 na água.
 
            EXEMPLO 3:
            Resultado da tua rajada:
            - D4 → Acerto numa Fragata (ainda a flutuar)
            - D5 → Acerto numa Fragata (ainda a flutuar)
            - D6 → Acerto numa Fragata (ainda a flutuar)
            Resumo: 3 tiros válidos, 3 acertos (Fragata), 0 na água.
 
            === ESTRATÉGIA OBRIGATÓRIA ===
            1. DIÁRIO DE BORDO: Mantém internamente o registo de TODOS os tiros já
               disparados e os seus resultados exatos (coordenada + resultado).
               Nunca dispares para uma coordenada já utilizada.
 
            2. SEM REPETIÇÕES: Verifica SEMPRE o teu Diário antes de escolher tiros.
               Tiros repetidos são um desperdício absoluto.
 
            3. PERSEGUIÇÃO (Hunt & Target):
               - Se acertares numa coordenada (ex: E7 → Nau), na próxima rajada
                 dispara nas posições adjacentes cardinais: E6, E8, D7, F7.
               - Se acertares em E7 e E8, o navio é horizontal — continua em E6 e E9.
               - Se acertares em E7 e F7, o navio é vertical — continua em D7 e G7.
               - Quando o navio for afundado, para de disparar nessa zona, pois os navios nao podem estar colados.
 
            4. HALO DE SEGURANÇA: Quando um navio for afundado, todas as posições
               adjacentes a ele são garantidamente água — nunca dispares lá.
 
            5. VARRIMENTO EFICIENTE: Quando não há navios em perseguição, usa um
               padrão de quadrícula — dispara em casas onde (número_linha + coluna)
               é par (ex: A1, A3, B2, B4...). Isso encontra qualquer navio com
               metade dos tiros.
            6. ADAPTAÇÃO CONTÍNUA: Ajusta a tua estratégia com base nos resultados anteriores.
               Se um navio grande for afundado, o padrão de varrimento pode ser
               temporariamente alterado para focar nas áreas restantes.
            7. PRIORIDADE: Perseguição > Varrimento sempre.
 
            === FORMATO DA TUA RESPOSTA ===
            Responde SEMPRE com JSON PURO de exactamente 3 tiros, SEM texto adicional,
            SEM markdown, SEM explicações. Apenas:
            [{"row":"X","column":N},{"row":"X","column":N},{"row":"X","column":N}]
 
            Confirma que entendeste respondendo apenas com: "Pronto para combate!"
            """;
    }

    /**
     * Constrói a mensagem do utilizador para enviar à IA.
     * Na primeira jogada, envia o prompt do sistema e instruções iniciais.
     * Nas jogadas seguintes, envia o resultado da última rajada e pede novos tiros.
     * @param game Estado atual do jogo
     * @return String contendo a mensagem para enviar à IA
     */
    private String buildUserMessage(IGame game) {
        // Obtém o histórico de movimentos feitos pela IA
        List<IMove> alienMoves = game.getAlienMoves();

        // Se for o primeiro tiro, envia instruções iniciais
        if (firstMove) {
            firstMove = false;
            return buildSystemPrompt() + "O jogo começa agora. " +
                    "Raciocinio antes de escolher os tiros:\n" +
                    "1. Modo: VARRIMENTO checkerboard\n" +
                    "2. Escolhe 3 casas onde (linha + coluna) é par\n\n" +
                    "Envia a tua primeira rajada de exactamente 3 tiros. " +
                    "Responde APENAS com o JSON puro, sem texto adicional.";
        }

        // Para jogadas seguintes, constrói mensagem com resultado anterior
        IMove lastMove = alienMoves.get(alienMoves.size() - 1);
        int remaining = game.getRemainingShips();
        String detailedResult = lastMove.toDetailedString();
        String alreadyShotList = buildAlreadyShotList(alienMoves);

        return "Resultado da tua última rajada (nº" + lastMove.getNumber() + "):\n"
                + detailedResult
                +"\n"
                + alreadyShotList
                + "\nNavios inimigos ainda a flutuar: " + remaining + ".\n\n"
                + "Atualiza o teu Diário de Bordo\n\n"
                + "RACIOCÍNIO obrigatório antes de responder:\n"
                + "1. Quais coordenadas acertei em navios ainda não afundados?\n"
                + "2. Modo atual: PERSEGUIÇÃO ou VARRIMENTO?\n"
                + "3. Se PERSEGUIÇÃO: orientação do navio? Próximos alvos?\n"
                + "4. Se VARRIMENTO: 3 casas do checkerboard ainda não disparadas?\n"
                + "5. Confirmar que nenhum tiro foi já disparado.\n\n"
                + "Depois do raciocínio envia a próxima rajada. "
                + "Responde APENAS com o JSON puro de 3 tiros.";
    }

    /**
     * Constrói uma lista com todas as coordenadas já disparadas.
     * Isto ajuda a IA a evitar disparar para o mesmo local duas vezes.
     * @param alienMoves Lista de movimentos anteriores da IA
     * @return String formatada com as coordenadas já disparadas
     */
    private String buildAlreadyShotList(List<IMove> alienMoves) {
        StringBuilder sb = new StringBuilder();
        sb.append("COORDENADAS JÁ DISPARADAS (NUNCA repitas nenhuma destas):\n");

        // Coleta todas as coordenadas disparadas em movimentos anteriores
        List<String> coords = new ArrayList<>();

        for (IMove move : alienMoves) {
            for (IPosition pos : move.getShots()) {
                coords.add("" + pos.getClassicRow() + pos.getClassicColumn());
            }
        }

        // Formata a resposta
        if (coords.isEmpty()) {
            sb.append("Nenhuma ainda.\n");
        } else {
            sb.append(String.join(", ", coords)).append("\n");
            sb.append("Total: ").append(coords.size()).append(" coordenadas bloqueadas.\n");
        }

        return sb.toString();
    }

    /**
     * Pergunta à IA os próximos tiros, com suporte a múltiplas tentativas.
     * Se a IA responder algo inválido, tenta novamente até MAX_RETRIES vezes.
     * Se todas as tentativas falharem, usa um fallback aleatório.
     * @param userMessage Mensagem para enviar à IA
     * @return JSON com os 3 tiros decididos
     */
    private String askLlmForShots(String userMessage) {
        // Adiciona a pergunta ao histórico
        addToHistory("user", userMessage);

        // Tenta obter uma resposta válida da IA
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            String llmResponse = callApi();

            addToHistory("assistant", llmResponse);

            String extracted = extractShotsJson(llmResponse);
            if (extracted != null && isValidShotsJson(extracted)) {
                return extracted;
            }

            // Se falhou, mostra o erro
            System.out.println("Tentativa " + attempt + "/" + MAX_RETRIES
                    + " — resposta inválida: "
                    + llmResponse.substring(0, Math.min(80, llmResponse.length())));

            // Se não é a última tentativa, pede para tentar de novo
            if (attempt < MAX_RETRIES) {
                addToHistory("user",
                        "Resposta inválida. Deves responder APENAS com um array JSON de " +
                                "EXACTAMENTE 3 tiros, sem texto adicional. Exemplo:\n" +
                                "[{\"row\":\"A\",\"column\":1},{\"row\":\"E\",\"column\":5},{\"row\":\"J\",\"column\":10}]\n" +
                                "Tenta novamente:"
                );
            }
        }

        // Se todas as tentativas falharam, usa tiros aleatórios como fallback
        System.out.println("Todas as tentativas falharam. A usar fallback aleatório.");
        return buildRandomFallbackJson();
    }

    /**
     * Faz uma chamada HTTP à API da Hugging Face para obter a resposta da IA.
     * Envia o histórico da conversa e recebe a próxima mensagem.
     * @return Resposta processada da IA (apenas o texto da mensagem)
     * @throws RuntimeException Se houver erro na chamada ou timeout
     */
    private String callApi() {
        try {
            // Prepara o corpo da requisição com o modelo e histórico
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model", MODEL_ID);
            requestBody.put("messages", conversationHistory);
            requestBody.put("max_tokens", MAX_OUTPUT_TOKENS);
            requestBody.put("temperature", 0.2);

            String requestJson = objectMapper.writeValueAsString(requestBody);

            // Constrói a requisição HTTP
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + apiKey)  // Bearer token no header
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(HTTP_TIMEOUT_SECONDS))
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                    .build();

            // Envia a requisição e processa a resposta
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            // Verifica se a requisição foi bem-sucedida
            if (response.statusCode() != 200) {
                throw new RuntimeException("Erro HTTP " + response.statusCode()
                        + ": " + response.body());
            }

            return parseAiResponse(response.body());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Chamada interrompida", e);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erro na API: " + e.getMessage(), e);
        }
    }

    /**
     * Interpreta a resposta JSON da API da Hugging Face.
     * Extrai o texto da mensagem da IA do objeto de resposta complexo.
     * @param responseBody String JSON com a resposta completa da API
     * @return Texto da mensagem da IA
     * @throws RuntimeException Se a estrutura da resposta for inesperada
     */
    private String parseAiResponse(String responseBody) {
        try {
            // Parse do JSON e extração do conteúdo
            JsonNode root    = objectMapper.readTree(responseBody);
            JsonNode choices = root.path("choices");

            // Valida se choices existe e não está vazio
            if (choices.isArray() && !choices.isEmpty()) {
                JsonNode content = choices.get(0)
                        .path("message")
                        .path("content");

                if (!content.isMissingNode()) {
                    return content.asText().trim();
                }
            }

            throw new RuntimeException("Estrutura inesperada: " + responseBody);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao interpretar resposta: "
                    + e.getMessage(), e);
        }
    }


    /**
     * Extrai o JSON dos tiros da resposta da IA.
     * Tenta múltiplas estratégias de extração (código, arrays, texto direto).
     * @param llmResponse Resposta completa da IA (pode conter texto extra)
     * @return String com o JSON dos tiros, ou null se não encontrou
     */
    private String extractShotsJson(String llmResponse) {
        if (llmResponse == null || llmResponse.isBlank()) return null;

        // Tenta extrair JSON dentro de blocos de código (```json ... ```)
        Pattern codeBlock = Pattern.compile(
                "```(?:json)?\\s*(\\[.*?])\\s*```",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher m = codeBlock.matcher(llmResponse);
        if (m.find()) {
            String c = m.group(1).trim();
            if (isValidShotsJson(c)) return c;
        }

        // Tenta extrair um array JSON diretamente
        Pattern arrayPat = Pattern.compile("(\\[\\s*\\{.*?}\\s*])", Pattern.DOTALL);
        Matcher m2 = arrayPat.matcher(llmResponse);
        while (m2.find()) {
            String c = m2.group(1).trim();
            if (isValidShotsJson(c)) return c;
        }

        // Tenta usar a resposta inteira se começar com [
        String trimmed = llmResponse.trim();
        if (trimmed.startsWith("[") && isValidShotsJson(trimmed)) return trimmed;

        return null;
    }

    /**
     * Valida se o JSON contém exatamente 3 tiros com coordenadas válidas.
     * Verifica se é um array, tem 3 elementos, e cada tiro tem linha (A-J) e coluna (1-10) válidas.
     * @param json String com o JSON a validar
     * @return true se for válido, false caso contrário
     */
    private boolean isValidShotsJson(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            // Verifica se é um array com exatamente NUMBER_SHOTS elementos
            if (!root.isArray() || root.size() != Game.NUMBER_SHOTS) return false;

            // Valida cada tiro individualmente
            for (JsonNode shot : root) {
                JsonNode rowNode = shot.get("row");
                JsonNode colNode = shot.get("column");
                if (rowNode == null || colNode == null) return false;

                // Verifica se a linha é A-J e a coluna é 1-10
                String row = rowNode.asText().toUpperCase();
                int    col = colNode.asInt();
                if (row.length() != 1 || row.charAt(0) < 'A' || row.charAt(0) > 'J') return false;
                if (col < 1 || col > 10) return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gera um fallback com 3 tiros aleatórios quando a IA falha.
     * Cria uma lista de todas as posições possíveis, embaralha e pega nos primeiros 3.
     * @return JSON formatado com 3 tiros aleatórios
     */
    private String buildRandomFallbackJson() {
        // Cria lista com todas as posições do tabuleiro
        List<IPosition> available = new ArrayList<>();
        for (int r = 0; r < Game.BOARD_SIZE; r++)
            for (int c = 0; c < Game.BOARD_SIZE; c++)
                available.add(new Position(r, c));

        // Embaralha e pega nos primeiros 3
        Collections.shuffle(available, new Random());

        return Game.jsonShots(available.subList(0, Game.NUMBER_SHOTS));
    }

    /**
     * Adiciona uma mensagem ao histórico de conversa.
     * Usa um mapa para armazenar o papel (role) e o conteúdo de cada mensagem.
     * @param role "user" ou "assistant"
     * @param content Texto da mensagem
     */
    private void addToHistory(String role, String content) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("role", role);
        message.put("content", content);
        conversationHistory.add(message);
    }

    /**
     * Retorna o número de mensagens no histórico de conversa.
     * @return Tamanho do histórico de conversação
     */
    public int getConversationLength() {
        return conversationHistory.size();
    }

    /**
     * Indica se a IA já fez pelo menos um movimento (ou seja, se o jogo já começou).
     * @return true se a IA já começou a jogar
     */
    public boolean isInitialized() {
        return !firstMove;
    }


}
