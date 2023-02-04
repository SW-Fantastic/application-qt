package org.swdc.qt.view;

import io.qt.core.QBuffer;
import org.swdc.dependency.AbstractDependencyScope;
import org.swdc.qt.QtResource;
import org.swdc.qt.config.ApplicationConfigure;

import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;

public class QtViewManager extends AbstractDependencyScope {

    private ConcurrentHashMap<String,QBuffer> loadedViewResourced = new ConcurrentHashMap<>();

    @Override
    public <T> T getByClass(Class<T> clazz) {
        QView view = clazz.getAnnotation(QView.class);
        if (view.multiple()) {
            throw new RuntimeException("你正在尝试注入非单例的View，请使用别名注入或者使用批量注入。");
        } else {
            return super.getByClass(clazz);
        }
    }

    @Override
    public Class getScopeType() {
        return QView.class;
    }

    @Override
    public <T> T put(String name, Class clazz, T component) {
        if (!QtView.class.isAssignableFrom(clazz)) {
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

        QtView instance = (QtView) component;
        instance.initView(buffer);
        if (view.stage()) {
            instance.setMinimumSize(view.width(),view.height());
            instance.setWindowTitle(view.title());
            instance.setWindowModality(view.modalType());
            instance.setWindowIcon(resource.getAppIcon());
            Theme theme = Theme.getTheme(configure.getTheme(),resource.getAssetFolder());
            theme.applyWithView(instance);
        }
        if (view.controller() != Object.class) {
            instance.setController(context.getByClass(view.controller()));
        }
        return (T) super.put(name,clazz,instance);
    }

}
