package org.swdc.qt.config;

import org.swdc.config.AbstractConfig;
import org.swdc.config.annotations.Property;
import org.swdc.qt.config.editors.FolderSelectPropertyEditor;

public abstract class ApplicationConfigure extends AbstractConfig {

    @PropEditor(
            editor = FolderSelectPropertyEditor.class,
            name = "主题",
            description = "应用的主题样式",
            resource = "skin"
    )
    @Property("theme")
    private String theme;

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }
}
