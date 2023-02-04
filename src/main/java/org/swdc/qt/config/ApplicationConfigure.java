package org.swdc.qt.config;

import org.swdc.config.AbstractConfig;
import org.swdc.config.annotations.Property;

public abstract class ApplicationConfigure extends AbstractConfig {

    @Property("theme")
    private String theme;

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }
}
