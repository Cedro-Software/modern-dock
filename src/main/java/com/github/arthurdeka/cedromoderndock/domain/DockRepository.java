package com.github.arthurdeka.cedromoderndock.domain;

import com.github.arthurdeka.cedromoderndock.model.DockModel;

public interface DockRepository {
    DockModel load();

    void save(DockModel model);
}
