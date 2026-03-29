package com.github.arthurdeka.cedromoderndock.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DockFolderItemModel implements DockItem {

    private String label = "";
    private String folderPath = "";

    public DockFolderItemModel(String label, String folderPath) {
        this.label = label;
        this.folderPath = folderPath;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getPath() {
        return folderPath;
    }

    @JsonIgnore
    public String getFolderPath() {
        return folderPath;
    }

    @Override
    public DockItemType getType() {
        return DockItemType.FOLDER;
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public void setPath(String path) {
        this.folderPath = path;
    }
}
