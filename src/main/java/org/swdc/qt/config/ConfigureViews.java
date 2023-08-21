package org.swdc.qt.config;

import io.qt.widgets.QSizePolicy;
import io.qt.widgets.QSpacerItem;
import io.qt.widgets.QVBoxLayout;
import io.qt.widgets.QWidget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.ours.common.annotations.AnnotationDescription;
import org.swdc.ours.common.annotations.Annotations;
import org.swdc.qt.QtResource;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

public class ConfigureViews {

    private static Logger logger = LoggerFactory.getLogger(ConfigureViews.class);

    public static List<PropertyItem> parseConfigure(Object theConfigure) {
        List<PropertyItem> parsed = new ArrayList<>();
        Class type = theConfigure.getClass();
        List<Field> properties = Annotations.getAnnotationField(
                type,
                PropEditor.class
        );
        for (Field field: properties) {
            AnnotationDescription desc = Annotations.findAnnotation(field,PropEditor.class);
            PropertyItem item = new PropertyItem(desc,field);
            parsed.add(item);
        }
        return parsed;
    }

    public static List<PropertyEditorView> createViews(QtResource resource,Object theConfig,List<PropertyItem> items) {
        List<PropertyEditorView> views = new ArrayList<>();
        for (PropertyItem item: items) {
            try {
                Constructor constructor = item.getEditorClass()
                        .getConstructor(QtResource.class,Object.class,PropertyItem.class);
                PropertyEditorView view = (PropertyEditorView) constructor
                        .newInstance(resource,theConfig,item);
                views.add(view);
            } catch (Exception e) {
                logger.error("failed to create a editor", e);
            }
        }
        return views;
    }

    public static void getPropertyConfigView(QWidget widget, QtResource resource, Object config) {
        List<PropertyItem> items = parseConfigure(config);
        List<PropertyEditorView> editorViews = createViews(resource,config,items);
        QVBoxLayout layout = new QVBoxLayout();
        for (PropertyEditorView view : editorViews) {
            QWidget editor = view.getEditor(widget);
            layout.addWidget(editor);
        }
        layout.addSpacerItem(new QSpacerItem(1,1, QSizePolicy.Policy.Preferred, QSizePolicy.Policy.Expanding));
        widget.setLayout(layout);
    }

}
