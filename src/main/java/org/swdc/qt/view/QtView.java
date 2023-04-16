package org.swdc.qt.view;


import io.qt.core.QBuffer;
import io.qt.core.QObject;
import io.qt.gui.QPalette;
import io.qt.gui.QResizeEvent;
import io.qt.widgets.QWidget;
import io.qt.widgets.tools.QUiLoader;
import org.swdc.dependency.DependencyContext;
import org.swdc.dependency.event.AbstractEvent;
import org.swdc.dependency.event.Events;

public abstract class QtView extends QWidget implements AbstractQtView {

    private Object controller;
    private DependencyContext context;
    private QUiLoader loader;

    private QWidget root;

    private QPalette themePalette;
    private Events events;

    public void setContext(DependencyContext context) {
        this.context = context;
    }

    @Override
    public void setThemePalette(QPalette palette) {
        this.themePalette = palette;
    }

    public QPalette getThemePalette() {
        return themePalette;
    }

    public void initView(QBuffer data) {
        if (loader == null) {
            data.reset();
            loader = new QUiLoader();
            root = loader.load(data,this);
        }
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

    public void setController(Object controller) {
        if (this.controller != null) {
            throw new RuntimeException("view is already initialized.");
        }
        this.controller = controller;
        initializeController(controller);
    }

    public final <T extends AbstractQtView> T getView(Class<T> viewType) {
        return context.getByClass(viewType);
    }

    public final  <T extends QObject> T findByName(String name) {
        return (T)findChild(name);
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
