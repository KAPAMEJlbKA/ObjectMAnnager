package com.kapamejlbka.objectmanager.domain.topology.dto;

import java.util.ArrayList;
import java.util.List;

public class TopologyLinkLengthsForm {

    private List<TopologyLinkLengthUpdate> links = new ArrayList<>();

    public List<TopologyLinkLengthUpdate> getLinks() {
        return links;
    }

    public void setLinks(List<TopologyLinkLengthUpdate> links) {
        this.links = links;
    }
}
