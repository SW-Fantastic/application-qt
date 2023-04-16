package org.swdc.qt.view;

import io.qt.core.QBuffer;
import io.qt.widgets.QApplication;
import io.qt.widgets.QWidget;
import org.swdc.dependency.AbstractDependencyScope;
import org.swdc.qt.QtResource;
import org.swdc.qt.config.ApplicationConfigure;

import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;

public class QtViewManager extends AbstractDependencyScope {

    private ConcurrentHashMap<String,QBuffer> loadedViewResourced = new ConcurrentHashMap<>();


    @Override
    public Class getScopeType() {
        return QView.class;
    }

    @Override
    public <T> T put(String name, Class clazz, T component) {
        if (!AbstractQtView.class.isAssignableFrom(clazz)) {
            throw new RuntimeException("Qt的View必须要继承QtView，并且使用QView注解标注。");
        }
        QView view = (QView) clazz.getAnnotation(QView.class);
        String qtDesignerFile = view.viewLocation();
        QBuffer buffer = loadedViewResourced.get(qtDesignerFile);
        if (buffer == null) {
            try {
                InputStream in = clazz.getModule().getResourceAsStream(qtDesignerFile);
                if (in == null) {
                    throw new RuntimeException("无法读取Qt的View文件，此文件应位于：" + qtDesignerFile + "，它通过QtDesigner进行设计和保存，通常后缀是“.ui”的xml文件。");
                }
                byte[] data = in.readAllBytes();
                buffer = new QBuffer();
                buffer.setData(data);
                loadedViewResourced.put(qtDesignerFile,buffer);

            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        QtResource resource = context.getByClass(QtResource.class);
        ApplicationConfigure configure = context.getByClass(resource.getConfigureClass());

        QApplication.setWindowIcon(resource.getAppIcon());

        AbstractQtView instance = (AbstractQtView) component;
        instance.initView(buffer);
        if (view.stage()) {
            QWidget widget = (QWidget)instance;
            widget.setMinimumSize(view.width(),view.height());
            widget.setWindowTitle(view.title());
            widget.setWindowModality(view.modalType());
            widget.setWindowIcon(resource.getAppIcon());
            if (!view.resizeable()) {
                widget.setMaximumSize(view.width(),view.height());
            }
        }
        Theme theme = Theme.getTheme(configure.getTheme(),resource.getAssetFolder());
        theme.applyWithView(instance);
        instance.setContext(context);
        instance.setThemePalette(theme.getPalette());

        if (view.controller() != Object.class) {
            instance.setController(context.getByClass(view.controller()));
        }

        if (!view.multiple()) {
            return (T) super.put(name,clazz,instance);
        }
        return (T)instance;
    }

}
