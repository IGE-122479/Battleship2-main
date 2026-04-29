# Refactoring Opportunities

| Local | Code Smell | Refabricação | Nº Aluno |
|-------|-----------|--------------|----------|
| `Game::printBoard` | Long Method | Extract Method | 113190 |
| `Game::randomEnemyFire` | Long Method | Extract Method | 113190 |
| `Game::sendMyShotsJson` | Duplicated Code | Extract Method | 113190 |
| `Game::readEnemyFire` | Duplicated Code | Extract Method | 113190 |
| `Move::processEnemyFire` | Long Method | Extract Method | 113190 |
| `Fleet::createRandom` | Long Method | Extract Method | 113190 |
| `Game::printBoard` | Long Parameter List | Introduce Parameter Object | 113190 |
| `AiGame::buildUserMessage` | Long Method | Extract Method | 99845 |
| `AiGame::buildSystemPrompt` | Primitive Obsession | Introduce Constant/ Extract Class | 99845 |
| `AiGame::buildAlreadyShotList` | Feature Envy | Move Method | 99845 |
| `PdfExporter::buildResultsText` | Complex condition | Decompose Conditional | 99845 |
| `PdfExporter::resolveOutputFile` | Long Method | Extract Method | 99845 |
| `Caravel` | Código duplicado | Pull Up Method | 99845 |
| `Carrack` | Código duplicado | Pull Up Method | 99845 |
| `Frigate` | Código duplicado | Pull Up Method | 99845 |
