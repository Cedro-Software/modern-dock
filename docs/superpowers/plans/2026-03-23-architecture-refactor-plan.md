# Cedro Modern Dock Architecture Refactor Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Tornar a arquitetura do projeto mais limpa, previsivel e testavel sem reescrever o app inteiro nem alterar o comportamento visivel do dock.

**Architecture:** A refatoracao vai migrar o projeto de um MVC desktop acoplado para uma arquitetura em camadas leve: `ui` para JavaFX/FXML, `application` para casos de uso, `domain` para modelos e regras, e `infrastructure` para persistencia, integracoes Windows e icones. O foco e inverter dependencias para que controllers nao saibam detalhes de persistencia/nativo e modelos nao executem efeitos colaterais.

**Tech Stack:** Java 21, JavaFX, Maven, Jackson, JNA, JUnit 5

---

## Scope

Este plano cobre um unico fluxo arquitetural: reduzir acoplamento entre UI, modelos, persistencia e integracoes Windows. Nao cobre redesign visual, novos recursos do produto nem mudancas de empacotamento.

## Target File Structure

**Create:**
- `src/main/java/com/github/arthurdeka/cedromoderndock/application/DockService.java`
- `src/main/java/com/github/arthurdeka/cedromoderndock/application/DockAppearanceService.java`
- `src/main/java/com/github/arthurdeka/cedromoderndock/application/DockItemActionService.java`
- `src/main/java/com/github/arthurdeka/cedromoderndock/application/WindowPreviewService.java`
- `src/main/java/com/github/arthurdeka/cedromoderndock/domain/DockRepository.java`
- `src/main/java/com/github/arthurdeka/cedromoderndock/domain/ProgramLauncher.java`
- `src/main/java/com/github/arthurdeka/cedromoderndock/domain/WindowsModuleLauncher.java`
- `src/main/java/com/github/arthurdeka/cedromoderndock/domain/WindowQueryGateway.java`
- `src/main/java/com/github/arthurdeka/cedromoderndock/domain/IconGateway.java`
- `src/main/java/com/github/arthurdeka/cedromoderndock/infrastructure/persistence/JsonDockRepository.java`
- `src/main/java/com/github/arthurdeka/cedromoderndock/infrastructure/system/DefaultProgramLauncher.java`
- `src/main/java/com/github/arthurdeka/cedromoderndock/infrastructure/system/DefaultWindowsModuleLauncher.java`
- `src/main/java/com/github/arthurdeka/cedromoderndock/infrastructure/system/JnaWindowQueryGateway.java`
- `src/main/java/com/github/arthurdeka/cedromoderndock/infrastructure/system/CachedWindowsIconGateway.java`
- `src/test/java/com/github/arthurdeka/cedromoderndock/application/DockServiceTest.java`
- `src/test/java/com/github/arthurdeka/cedromoderndock/application/DockAppearanceServiceTest.java`
- `src/test/java/com/github/arthurdeka/cedromoderndock/application/DockItemActionServiceTest.java`
- `src/test/java/com/github/arthurdeka/cedromoderndock/application/WindowPreviewServiceTest.java`
- `src/test/java/com/github/arthurdeka/cedromoderndock/infrastructure/persistence/JsonDockRepositoryTest.java`

**Modify:**
- `src/main/java/com/github/arthurdeka/cedromoderndock/App.java`
- `src/main/java/com/github/arthurdeka/cedromoderndock/controller/DockController.java`
- `src/main/java/com/github/arthurdeka/cedromoderndock/controller/SettingsController.java`
- `src/main/java/com/github/arthurdeka/cedromoderndock/controller/AddWindowsModulesModalController.java`
- `src/main/java/com/github/arthurdeka/cedromoderndock/model/DockItem.java`
- `src/main/java/com/github/arthurdeka/cedromoderndock/model/DockModel.java`
- `src/main/java/com/github/arthurdeka/cedromoderndock/model/DockProgramItemModel.java`
- `src/main/java/com/github/arthurdeka/cedromoderndock/model/DockSettingsItemModel.java`
- `src/main/java/com/github/arthurdeka/cedromoderndock/model/DockWindowsModuleItemModel.java`
- `src/main/java/com/github/arthurdeka/cedromoderndock/util/SaveAndLoadDockSettings.java`
- `src/main/java/com/github/arthurdeka/cedromoderndock/util/NativeWindowUtils.java`
- `src/main/java/com/github/arthurdeka/cedromoderndock/util/WindowsIconHandler.java`
- `src/main/java/com/github/arthurdeka/cedromoderndock/view/WindowPreviewPopup.java`
- `src/main/java/module-info.java`
- `README.md`

## Migration Strategy

### Low impact
- Cobrir comportamento atual com testes de caracterizacao.
- Tirar persistencia para fora do `DockModel`.
- Introduzir servicos de aplicacao para centralizar casos de uso.

### Medium impact
- Tirar execucao nativa de dentro de `DockItem`.
- Trocar controllers gordos por controllers apoiados em servicos.
- Encapsular consulta de janelas e cache de icones em gateways.

### High impact
- Redefinir fronteiras de pacote e documentar a nova arquitetura.
- Remover o papel de pacote-curinga de `util`.

### Task 1: Criar cobertura de seguranca antes da refatoracao

**Files:**
- Create: `src/test/java/com/github/arthurdeka/cedromoderndock/infrastructure/persistence/JsonDockRepositoryTest.java`
- Create: `src/test/java/com/github/arthurdeka/cedromoderndock/application/DockServiceTest.java`
- Modify: `pom.xml`

- [ ] **Step 1: Adicionar suporte minimo de testes no Maven**

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.2.5</version>
</plugin>
```

- [ ] **Step 2: Escrever teste de persistencia do estado do dock**

```java
@Test
void shouldLoadDefaultDockWhenConfigDoesNotExist() {
    JsonDockRepository repository = new JsonDockRepository(tempConfigPath);
    DockModel model = repository.load();

    assertEquals(1, model.getItems().size());
    assertEquals("Settings", model.getItems().getFirst().getLabel());
}
```

- [ ] **Step 3: Escrever teste de servico para adicionar e remover itens**

```java
@Test
void shouldAddAndRemoveItemsWithoutTouchingUi() {
    InMemoryDockRepository repository = new InMemoryDockRepository(new DockModel());
    DockService service = new DockService(repository);

    service.addProgram("Notepad", "C:\\\\Windows\\\\notepad.exe");
    service.removeItem(0);

    assertFalse(service.getDock().getItems().isEmpty());
}
```

- [ ] **Step 4: Rodar testes para confirmar falha inicial ou cobertura ausente**

Run: `mvn -Dtest=JsonDockRepositoryTest,DockServiceTest test`

Expected: falhas iniciais por classes ainda inexistentes.

- [ ] **Step 5: Commit**

```bash
git add pom.xml src/test/java
git commit -m "test: add characterization coverage for architecture refactor"
```

### Task 2: Extrair persistencia para um repository de dominio

**Files:**
- Create: `src/main/java/com/github/arthurdeka/cedromoderndock/domain/DockRepository.java`
- Create: `src/main/java/com/github/arthurdeka/cedromoderndock/infrastructure/persistence/JsonDockRepository.java`
- Modify: `src/main/java/com/github/arthurdeka/cedromoderndock/model/DockModel.java`
- Modify: `src/main/java/com/github/arthurdeka/cedromoderndock/util/SaveAndLoadDockSettings.java`
- Modify: `src/main/java/module-info.java`

- [ ] **Step 1: Criar a porta de persistencia**

```java
public interface DockRepository {
    DockModel load();
    void save(DockModel model);
}
```

- [ ] **Step 2: Implementar o repository Jackson usando a logica atual**

```java
public final class JsonDockRepository implements DockRepository {
    @Override
    public DockModel load() { ... }

    @Override
    public void save(DockModel model) { ... }
}
```

- [ ] **Step 3: Remover persistencia de dentro do model**

```java
public class DockModel {
    public void setDockPosition(Double positionX, Double positionY) {
        this.dockPositionX = positionX;
        this.dockPositionY = positionY;
    }
}
```

- [ ] **Step 4: Transformar `SaveAndLoadDockSettings` em wrapper temporario ou removelo aos poucos**

```java
@Deprecated
public final class SaveAndLoadDockSettings {
    private static final DockRepository REPOSITORY = new JsonDockRepository();
}
```

- [ ] **Step 5: Rodar os testes de persistencia**

Run: `mvn -Dtest=JsonDockRepositoryTest test`

Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add src/main/java src/test/java pom.xml
git commit -m "refactor: move dock persistence behind repository"
```

### Task 3: Criar servicos de aplicacao para casos de uso do dock

**Files:**
- Create: `src/main/java/com/github/arthurdeka/cedromoderndock/application/DockService.java`
- Create: `src/main/java/com/github/arthurdeka/cedromoderndock/application/DockAppearanceService.java`
- Create: `src/test/java/com/github/arthurdeka/cedromoderndock/application/DockAppearanceServiceTest.java`
- Modify: `src/main/java/com/github/arthurdeka/cedromoderndock/controller/DockController.java`
- Modify: `src/main/java/com/github/arthurdeka/cedromoderndock/controller/SettingsController.java`

- [ ] **Step 1: Criar servico para operacoes de lista do dock**

```java
public class DockService {
    private final DockRepository repository;
    private final DockModel dock;

    public DockService(DockRepository repository) {
        this.repository = repository;
        this.dock = repository.load();
    }
}
```

- [ ] **Step 2: Criar servico separado para customizacao visual**

```java
public class DockAppearanceService {
    public void setIconSize(DockModel dock, int size) { ... }
    public void setTransparency(DockModel dock, int value) { ... }
}
```

- [ ] **Step 3: Mover `add/remove/swap/save/load` do controller para os servicos**

```java
public void addProgram(String label, String path) {
    dock.addItem(new DockProgramItemModel(label, path));
    repository.save(dock);
}
```

- [ ] **Step 4: Atualizar controllers para apenas orquestrar UI**

```java
settingsController.setDockService(dockService);
settingsController.setAppearanceService(appearanceService);
```

- [ ] **Step 5: Cobrir regras de customizacao com testes**

Run: `mvn -Dtest=DockServiceTest,DockAppearanceServiceTest test`

Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add src/main/java src/test/java
git commit -m "refactor: introduce dock application services"
```

### Task 4: Remover efeitos colaterais de dentro dos models de item

**Files:**
- Create: `src/main/java/com/github/arthurdeka/cedromoderndock/domain/ProgramLauncher.java`
- Create: `src/main/java/com/github/arthurdeka/cedromoderndock/domain/WindowsModuleLauncher.java`
- Create: `src/main/java/com/github/arthurdeka/cedromoderndock/application/DockItemActionService.java`
- Create: `src/main/java/com/github/arthurdeka/cedromoderndock/infrastructure/system/DefaultProgramLauncher.java`
- Create: `src/main/java/com/github/arthurdeka/cedromoderndock/infrastructure/system/DefaultWindowsModuleLauncher.java`
- Create: `src/test/java/com/github/arthurdeka/cedromoderndock/application/DockItemActionServiceTest.java`
- Modify: `src/main/java/com/github/arthurdeka/cedromoderndock/model/DockItem.java`
- Modify: `src/main/java/com/github/arthurdeka/cedromoderndock/model/DockProgramItemModel.java`
- Modify: `src/main/java/com/github/arthurdeka/cedromoderndock/model/DockSettingsItemModel.java`
- Modify: `src/main/java/com/github/arthurdeka/cedromoderndock/model/DockWindowsModuleItemModel.java`
- Modify: `src/main/java/com/github/arthurdeka/cedromoderndock/controller/DockController.java`

- [ ] **Step 1: Redefinir `DockItem` como contrato de dados**

```java
public interface DockItem {
    String getLabel();
    String getPath();
    DockItemType getType();
}
```

- [ ] **Step 2: Criar um servico de acao para decidir o que fazer no clique**

```java
public class DockItemActionService {
    public void execute(DockItem item) {
        switch (item.getType()) {
            case PROGRAM -> programLauncher.launch(item.getPath());
            case WINDOWS_MODULE -> windowsModuleLauncher.launch(item.getPath());
            case SETTINGS -> settingsAction.run();
        }
    }
}
```

- [ ] **Step 3: Implementar adapters concretos para abrir programas e modulos Windows**

```java
public class DefaultProgramLauncher implements ProgramLauncher {
    @Override
    public void launch(String executablePath) { ... }
}
```

- [ ] **Step 4: Atualizar o controller para chamar o servico em vez de `item.performAction()`**

```java
button.setOnAction(event -> actionService.execute(item));
```

- [ ] **Step 5: Rodar testes de servico de acao usando doubles**

Run: `mvn -Dtest=DockItemActionServiceTest test`

Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add src/main/java src/test/java
git commit -m "refactor: move dock item side effects to action services"
```

### Task 5: Isolar integracoes nativas de preview e icones

**Files:**
- Create: `src/main/java/com/github/arthurdeka/cedromoderndock/domain/WindowQueryGateway.java`
- Create: `src/main/java/com/github/arthurdeka/cedromoderndock/domain/IconGateway.java`
- Create: `src/main/java/com/github/arthurdeka/cedromoderndock/application/WindowPreviewService.java`
- Create: `src/main/java/com/github/arthurdeka/cedromoderndock/infrastructure/system/JnaWindowQueryGateway.java`
- Create: `src/main/java/com/github/arthurdeka/cedromoderndock/infrastructure/system/CachedWindowsIconGateway.java`
- Create: `src/test/java/com/github/arthurdeka/cedromoderndock/application/WindowPreviewServiceTest.java`
- Modify: `src/main/java/com/github/arthurdeka/cedromoderndock/util/NativeWindowUtils.java`
- Modify: `src/main/java/com/github/arthurdeka/cedromoderndock/util/WindowsIconHandler.java`
- Modify: `src/main/java/com/github/arthurdeka/cedromoderndock/controller/DockController.java`

- [ ] **Step 1: Criar portas para icones e janelas**

```java
public interface WindowQueryGateway {
    List<WindowInfo> findOpenWindows(String executablePath);
    void activate(WindowInfo windowInfo);
}
```

- [ ] **Step 2: Criar servico de preview independente de JavaFX**

```java
public class WindowPreviewService {
    public List<WindowInfo> loadPreview(String executablePath) {
        return windowQueryGateway.findOpenWindows(executablePath);
    }
}
```

- [ ] **Step 3: Adaptar codigo atual de JNA e cache de icones para as implementacoes concretas**

```java
public class JnaWindowQueryGateway implements WindowQueryGateway { ... }
public class CachedWindowsIconGateway implements IconGateway { ... }
```

- [ ] **Step 4: Fazer o controller depender do servico e nao de utilitarios estaticos**

```java
List<WindowInfo> windows = windowPreviewService.loadPreview(item.getPath());
Path iconPath = iconGateway.resolve(item.getPath());
```

- [ ] **Step 5: Rodar testes do servico de preview**

Run: `mvn -Dtest=WindowPreviewServiceTest test`

Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add src/main/java src/test/java
git commit -m "refactor: isolate native preview and icon integrations"
```

### Task 6: Enxugar controllers e clarificar o papel de cada camada

**Files:**
- Modify: `src/main/java/com/github/arthurdeka/cedromoderndock/App.java`
- Modify: `src/main/java/com/github/arthurdeka/cedromoderndock/controller/DockController.java`
- Modify: `src/main/java/com/github/arthurdeka/cedromoderndock/controller/SettingsController.java`
- Modify: `src/main/java/com/github/arthurdeka/cedromoderndock/controller/AddWindowsModulesModalController.java`
- Modify: `src/main/java/com/github/arthurdeka/cedromoderndock/view/WindowPreviewPopup.java`

- [ ] **Step 1: Centralizar composicao de dependencias em `App`**

```java
DockRepository repository = new JsonDockRepository();
DockService dockService = new DockService(repository);
DockAppearanceService appearanceService = new DockAppearanceService(repository);
```

- [ ] **Step 2: Reduzir `DockController` para eventos de UI, binding e renderizacao**

```java
public void handleInitialization() {
    renderDock(dockService.getDock());
    bindPreviewInteractions();
}
```

- [ ] **Step 3: Remover dependencia direta entre `SettingsController` e `DockController`**

```java
public void setServices(DockService dockService, DockAppearanceService appearanceService) { ... }
```

- [ ] **Step 4: Simplificar `WindowPreviewPopup` para receber apenas dados prontos**

```java
public void updateContent(List<WindowPreviewItem> items, PopupTheme theme) { ... }
```

- [ ] **Step 5: Fazer smoke test manual da UI**

Run: `mvn test`

Expected: PASS

Manual check:
- abrir o dock
- abrir settings
- adicionar `.exe`
- mover itens
- clicar em modulo Windows
- passar mouse em item com janela aberta

- [ ] **Step 6: Commit**

```bash
git add src/main/java src/test/java
git commit -m "refactor: slim controllers and centralize composition"
```

### Task 7: Consolidar nomes, pacotes e documentacao

**Files:**
- Modify: `README.md`
- Modify: `src/main/java/com/github/arthurdeka/cedromoderndock/util/SaveAndLoadDockSettings.java`
- Modify: `src/main/java/com/github/arthurdeka/cedromoderndock/util/NativeWindowUtils.java`
- Modify: `src/main/java/com/github/arthurdeka/cedromoderndock/util/WindowsIconHandler.java`
- Modify: `src/main/java/module-info.java`

- [ ] **Step 1: Remover ou marcar como legado utilitarios que viraram adapters**

```java
@Deprecated(forRemoval = true)
public final class SaveAndLoadDockSettings { }
```

- [ ] **Step 2: Atualizar `module-info.java` para novos pacotes**

```java
opens com.github.arthurdeka.cedromoderndock.model to com.fasterxml.jackson.databind;
exports com.github.arthurdeka.cedromoderndock.application;
```

- [ ] **Step 3: Atualizar o README com a arquitetura real**

```md
## Architecture

- UI: JavaFX controllers, views and CSS
- Application: use cases and orchestration
- Domain: models and ports
- Infrastructure: Jackson, JNA and OS integrations
```

- [ ] **Step 4: Rodar verificacao final**

Run: `mvn clean test package`

Expected: build verde e pacote gerado em `target/dist`

- [ ] **Step 5: Commit**

```bash
git add README.md src/main/java pom.xml src/test/java
git commit -m "docs: document layered architecture and cleanup legacy utils"
```

## Definition of Done

- `DockModel` nao chama persistencia nem utilitarios estaticos.
- `DockItem` nao executa `ProcessBuilder` nem possui efeitos colaterais.
- Controllers JavaFX nao sabem como salvar config, abrir programas ou consultar janelas nativas.
- Integracoes com Windows ficam atras de interfaces de dominio.
- Existe cobertura automatizada minima para repositorio e servicos.
- O `README` descreve a arquitetura que o codigo realmente usa.

## Risks and Safeguards

- Risco: quebrar serializacao Jackson ao mover classes.
  Mitigacao: manter nomes dos tipos Jackson e cobrir com teste de round-trip.

- Risco: regressao no hover preview por causa da extracao para servicos.
  Mitigacao: mover logica em duas etapas, primeiro encapsular sem mudar fluxo.

- Risco: controllers ficarem acoplados de outra forma via setters demais.
  Mitigacao: centralizar composicao em `App` e passar dependencias explicitamente.

- Risco: refatoracao grande demais num unico PR.
  Mitigacao: executar task por task, com commits pequenos e verificacao ao final de cada etapa.

## Suggested PR Breakdown

1. PR 1: testes + repository
2. PR 2: servicos de aplicacao
3. PR 3: action service + launchers
4. PR 4: preview/icon gateways
5. PR 5: controllers + docs
