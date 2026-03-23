package com.github.arthurdeka.cedromoderndock.application;

import com.github.arthurdeka.cedromoderndock.domain.DockRepository;
import com.github.arthurdeka.cedromoderndock.model.DockItem;
import com.github.arthurdeka.cedromoderndock.model.DockModel;

import java.util.List;

public class DockService {
    private final DockRepository repository;
    private final DockModel dock;

    public DockService(DockRepository repository) {
        this.repository = repository;
        this.dock = repository.load();
    }

    public DockModel getDock() {
        return dock;
    }

    public List<DockItem> getItems() {
        return dock.getItems();
    }

    public void addItem(DockItem item) {
        dock.addItem(item);
        saveChanges();
    }

    public void removeItem(int index) {
        dock.removeItem(index);
        saveChanges();
    }

    public void swapItems(int firstItemIdx, int secondItemIdx) {
        dock.swapItems(firstItemIdx, secondItemIdx);
        saveChanges();
    }

    public void setDockPosition(double positionX, double positionY) {
        dock.setDockPosition(positionX, positionY);
        saveChanges();
    }

    public void saveChanges() {
        repository.save(dock);
    }
}
