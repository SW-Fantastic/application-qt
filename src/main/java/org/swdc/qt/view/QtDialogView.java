package org.swdc.qt.view;

import io.qt.core.QBuffer;
import io.qt.core.QObject;
import io.qt.gui.QPalette;
import io.qt.gui.QResizeEvent;
import io.qt.widgets.QDialog;
import io.qt.widgets.QWidget;
import io.qt.widgets.tools.QUiLoader;
import org.swdc.dependency.DependencyContext;
import org.swdc.dependency.event.AbstractEvent;
import org.swdc.dependency.event.Events;

public class QtDialogView extends QDialog implements AbstractQtView {

    private Object controller;
    private DependencyContext context;
    private QUiLoader loader;

    private QWidget root;
    private QPalette themePalette;
    private Events events;

    @Override
    public void initView(QBuffer data) {
        if (loader == null) {
            data.reset();
            loader = new QUiLoader();
            root = loader.load(data,this);
        }
    }

    @Override
    public void setThemePalette(QPalette themePalette) {
        this.themePalette = themePalette;
    }

    @Override
    protected final void resizeEvent(QResizeEvent event) {
        super.resizeEvent(event);
        if (root != null) {
            root.resize(this.width(),this.height());
        }
        resizedEvent(event);
    }

    protected void resizedEvent(QResizeEvent event) {

    }

    @Override
    public void setContext(DependencyContext context) {
        this.context = context;
    }

    @Override
    public <T extends QObject> T findByName(String name) {
        return (T)findChild(name);
    }

    public final <T extends AbstractQtView> T getView(Class<T> viewType) {
        return context.getByClass(viewType);
    }

    public void setController(Object controller) {
        if (this.controller != null) {
            throw new RuntimeException("view is already initialized.");
        }
        this.controller = controller;
        initializeController(controller);
    }

    public QPalette getThemePalette() {
        return themePalette;
    }


    public <T> T getController() {
        return (T)controller;
    }

    @Override
    public <T extends AbstractEvent> void emit(T event) {
        events.dispatch(event);
    }

    @Override
    public void setEvents(Events events) {
        this.events = events;
    }
}
