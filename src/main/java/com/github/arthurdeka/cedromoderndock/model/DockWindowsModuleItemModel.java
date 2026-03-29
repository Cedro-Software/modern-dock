package com.github.arthurdeka.cedromoderndock.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@NoArgsConstructor
public class DockWindowsModuleItemModel implements DockItem {

    private String label = "";
    private String iconPath = "";
    @Setter
    @Getter
    private String module = "";

    public DockWindowsModuleItemModel(String label, String module) {


        if (Objects.equals(module, "mypc")) {
            this.iconPath = "/com/github/arthurdeka/cedromoderndock/icons/my_computer.png";

        } else if (Objects.equals(module, "trash")) {
            this.iconPath = "/com/github/arthurdeka/cedromoderndock/icons/trash.png";

        } else if (Objects.equals(module, "ctrlpnl")) {
            this.iconPath = "/com/github/arthurdeka/cedromoderndock/icons/control.png";

        } else if (Objects.equals(module, "pconfig")) {
            this.iconPath = "/com/github/arthurdeka/cedromoderndock/icons/windows_settings.png";

        }

        this.label = label;
        this.module = module;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getPath() {
        return iconPath;
    }

    @Override
    public DockItemType getType() {
        return DockItemType.WINDOWS_MODULE;
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public void setPath(String path) {
        this.iconPath = path;
    }

}
