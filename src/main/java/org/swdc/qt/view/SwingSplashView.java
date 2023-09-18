package org.swdc.qt.view;


import javax.swing.*;
import java.io.File;

public abstract class SwingSplashView extends Splash {

    public SwingSplashView(File resources) {
        super(resources);
    }


    public abstract JWindow getSplash();

    @Override
    public void show() {
        getSplash().setVisible(true);
    }

    @Override
    public void hide() {
        getSplash().setVisible(false);
    }
}
