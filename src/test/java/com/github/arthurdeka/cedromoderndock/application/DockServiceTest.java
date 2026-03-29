package com.github.arthurdeka.cedromoderndock.application;

import com.github.arthurdeka.cedromoderndock.domain.DockRepository;
import com.github.arthurdeka.cedromoderndock.model.DockModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DockServiceTest {

    @Test
    void snapsSavedDockCoordinatesToWholePixels() {
        InMemoryDockRepository repository = new InMemoryDockRepository();
        DockService service = new DockService(repository);

        service.setDockPosition(718.5, 28.2);

        DockModel savedDock = repository.savedModel;
        assertEquals(719.0, savedDock.getDockPositionX());
        assertEquals(28.0, savedDock.getDockPositionY());
    }

    private static final class InMemoryDockRepository implements DockRepository {
        private final DockModel model = new DockModel();
        private DockModel savedModel = model;

        @Override
        public void save(DockModel model) {
            savedModel = model;
        }

        @Override
        public DockModel load() {
            return model;
        }
    }
}
