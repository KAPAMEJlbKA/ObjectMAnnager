package com.kapamejlbka.objectmanager.domain.device.dto;

import java.util.ArrayList;
import java.util.List;

public class NetworkNodeSettingsForm {

    private List<NetworkNodeSettingsItem> nodes = new ArrayList<>();

    public List<NetworkNodeSettingsItem> getNodes() {
        return nodes;
    }

    public void setNodes(List<NetworkNodeSettingsItem> nodes) {
        this.nodes = nodes;
    }
}
