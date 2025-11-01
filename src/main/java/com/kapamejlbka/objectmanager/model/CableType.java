package com.kapamejlbka.objectmanager.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "cable_types")
public class CableType {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CableFunction function = CableFunction.SIGNAL;

    protected CableType() {
    }

    public CableType(String name) {
        this(name, CableFunction.SIGNAL);
    }

    public CableType(String name, CableFunction function) {
        this.name = name;
        this.function = function == null ? CableFunction.SIGNAL : function;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CableFunction getFunction() {
        return function;
    }

    public void setFunction(CableFunction function) {
        this.function = function == null ? CableFunction.SIGNAL : function;
    }
}
