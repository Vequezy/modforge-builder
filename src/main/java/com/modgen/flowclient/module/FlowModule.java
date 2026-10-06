package com.modgen.flowclient.module;

public final class FlowModule {
    public final String name, category, section, description;
    public boolean enabled;
    public FlowModule(String name, String category, String section, String description) {
        this.name = name;
        this.category = category;
        this.section = section;
        this.description = description;
    }
}
