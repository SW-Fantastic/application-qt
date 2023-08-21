package org.swdc.qt.config;


import org.swdc.ours.common.annotations.AnnotationDescription;

import java.lang.reflect.Field;

public class PropertyItem {

    private Field propertyField;
    private AnnotationDescription editorDesc;

    public PropertyItem(AnnotationDescription editor, Field field) {
        this.editorDesc = editor;
        this.propertyField = field;
    }

    public String getName() {
        return editorDesc.getProperty(String.class,"name");
    }

    public String getDesc() {
        return editorDesc.getProperty(String.class,"description");
    }

    public Class getEditorClass() {
        return editorDesc.getProperty(Class.class,"editor");
    }

    public String getResource() {
        return editorDesc.getProperty(String.class,"resource");
    }

    public Field getPropertyField() {
        return propertyField;
    }

}
