package org.swdc.qt.view;

import org.swdc.qt.QtResource;

import java.io.File;

public abstract class Splash {

    protected File resources;

    public Splash(File resources) {
        this.resources = resources;
    }

    public abstract void show();

    public abstract void hide();

}
