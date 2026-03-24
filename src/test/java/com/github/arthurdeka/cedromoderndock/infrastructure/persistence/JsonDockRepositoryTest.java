package com.github.arthurdeka.cedromoderndock.infrastructure.persistence;

import com.github.arthurdeka.cedromoderndock.model.DockFolderItemModel;
import com.github.arthurdeka.cedromoderndock.model.DockItem;
import com.github.arthurdeka.cedromoderndock.model.DockItemType;
import com.github.arthurdeka.cedromoderndock.model.DockModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class JsonDockRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void savesAndLoadsFolderItems() {
        Path configPath = tempDir.resolve("config.json");
        JsonDockRepository repository = new JsonDockRepository(configPath);

        DockModel model = new DockModel();
        model.addItem(new DockFolderItemModel("Projects", "C:\\Users\\Arthur Rodrigues\\Projects"));

        repository.save(model);
        DockModel loadedModel = repository.load();

        assertEquals(1, loadedModel.getItems().size());

        DockItem loadedItem = loadedModel.getItems().getFirst();
        DockFolderItemModel folderItem = assertInstanceOf(DockFolderItemModel.class, loadedItem);
        assertEquals(DockItemType.FOLDER, folderItem.getType());
        assertEquals("Projects", folderItem.getLabel());
        assertEquals("C:\\Users\\Arthur Rodrigues\\Projects", folderItem.getFolderPath());
    }
}
