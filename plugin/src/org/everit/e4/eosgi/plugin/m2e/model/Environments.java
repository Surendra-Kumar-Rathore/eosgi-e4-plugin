package org.everit.e4.eosgi.plugin.m2e.model;

import java.util.List;

public class Environments {

    private List<Environment> environments;

    public List<Environment> getEnvironments() {
        return environments;
    }

    public void setEnvironments(List<Environment> environments) {
        this.environments = environments;
    }

    @Override
    public String toString() {
        return "Environments [environments=" + environments + "]";
    }

}
