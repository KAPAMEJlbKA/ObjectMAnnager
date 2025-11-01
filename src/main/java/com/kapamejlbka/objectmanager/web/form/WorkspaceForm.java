package com.kapamejlbka.objectmanager.web.form;

import org.springframework.util.StringUtils;

public class WorkspaceForm {

    private String name;
    private String location;
    private String equipment;
    private String assignedNode;

    public boolean isEmpty() {
        return !StringUtils.hasText(name)
                && !StringUtils.hasText(location)
                && !StringUtils.hasText(equipment)
                && !StringUtils.hasText(assignedNode);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public String getAssignedNode() {
        return assignedNode;
    }

    public void setAssignedNode(String assignedNode) {
        this.assignedNode = assignedNode;
    }
}
