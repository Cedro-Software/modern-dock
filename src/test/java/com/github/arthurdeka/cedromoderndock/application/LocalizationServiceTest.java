package com.github.arthurdeka.cedromoderndock.application;

import com.github.arthurdeka.cedromoderndock.domain.DockRepository;
import com.github.arthurdeka.cedromoderndock.model.DockModel;
import com.github.arthurdeka.cedromoderndock.model.DockProgramItemModel;
import com.github.arthurdeka.cedromoderndock.model.DockSettingsItemModel;
import com.github.arthurdeka.cedromoderndock.model.DockWindowsModuleItemModel;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class LocalizationServiceTest {

    @Test
    void localizesBuiltInItemsButKeepsProgramLabelsUntouched() {
        InMemoryDockRepository repository = new InMemoryDockRepository();
        DockService dockService = new DockService(repository);
        LocalizationService localizationService = new LocalizationService(dockService);

        assertEquals("Settings", localizationService.dockItemLabel(new DockSettingsItemModel()));
        assertEquals("Control Panel", localizationService.dockItemLabel(new DockWindowsModuleItemModel("Control Panel", "ctrlpnl")));
        assertEquals("Discord", localizationService.dockItemLabel(new DockProgramItemModel("Discord", "C:\\Discord.exe")));

        localizationService.setLanguage(SupportedLanguage.PT_BR);

        assertEquals("Configurações", localizationService.dockItemLabel(new DockSettingsItemModel()));
        assertEquals("Painel de Controle", localizationService.dockItemLabel(new DockWindowsModuleItemModel("Control Panel", "ctrlpnl")));
        assertEquals("Discord", localizationService.dockItemLabel(new DockProgramItemModel("Discord", "C:\\Discord.exe")));
    }

    @Test
    void persistsLanguageAndNotifiesListeners() {
        InMemoryDockRepository repository = new InMemoryDockRepository();
        DockService dockService = new DockService(repository);
        LocalizationService localizationService = new LocalizationService(dockService);
        AtomicInteger notificationCount = new AtomicInteger();

        localizationService.addListener(notificationCount::incrementAndGet);
        localizationService.setLanguage(SupportedLanguage.PT_BR);

        assertEquals(SupportedLanguage.PT_BR, repository.savedModel.getLanguage());
        assertEquals(1, notificationCount.get());
    }

    @Test
    void languageSelectorUsesNativeLanguageNames() {
        InMemoryDockRepository repository = new InMemoryDockRepository();
        DockService dockService = new DockService(repository);
        LocalizationService localizationService = new LocalizationService(dockService);

        assertEquals("English", localizationService.languageDisplayName(SupportedLanguage.EN_US));
        assertEquals("Portugu\u00EAs (Brasil)", localizationService.languageDisplayName(SupportedLanguage.PT_BR));
    }

    @Test
    void everySupportedLanguageHasATranslationBundle() {
        for (SupportedLanguage language : SupportedLanguage.values()) {
            String windowTitle = LocalizationService.bootstrapText(language, "settings.window.title");
            String moduleTitle = LocalizationService.bootstrapText(language, "windowsModule.modal.title");

            assertFalse(windowTitle.isBlank(), "Missing settings.window.title for " + language);
            assertFalse(moduleTitle.isBlank(), "Missing windowsModule.modal.title for " + language);
            assertFalse("settings.window.title".equals(windowTitle), "Bundle fallback failed for " + language);
            assertFalse("windowsModule.modal.title".equals(moduleTitle), "Bundle fallback failed for " + language);
        }
    }

    private static final class InMemoryDockRepository implements DockRepository {
        private DockModel savedModel = new DockModel();

        @Override
        public DockModel load() {
            return savedModel;
        }

        @Override
        public void save(DockModel model) {
            savedModel = model;
        }
    }
}
