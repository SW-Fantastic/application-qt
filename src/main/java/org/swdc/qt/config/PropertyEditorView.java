package org.swdc.qt.config;

import io.qt.widgets.QWidget;
import org.swdc.dependency.utils.ReflectionUtil;
import org.swdc.ours.common.type.ClassTypeAndMethods;
import org.swdc.ours.common.type.Converter;
import org.swdc.ours.common.type.Converters;
import org.swdc.qt.QtResource;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public abstract class PropertyEditorView {

    protected Converters converters = new Converters();
    private PropertyItem item;

    private Object config;
    private QtResource resource;

    public PropertyEditorView(QtResource resource, Object config, PropertyItem item) {
        this.config = config;
        this.item = item;
        this.resource = resource;
    }

    public abstract QWidget getEditor(QWidget parent);

    public <T> T getValue() {
        try {
            Field field = item.getPropertyField();
            Method getter = ClassTypeAndMethods.extractGetter(field);
            if (getter == null) {
                throw new RuntimeException("missing field getter: " + field.getName());
            }
            return (T)getter.invoke(config);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setValue(Object value) {
        try {
            Field field = item.getPropertyField();
            Method setter = ClassTypeAndMethods.extractSetter(field);
            if (setter == null) {
                throw new RuntimeException("missing field setter: " + field.getName());
            }
            if (value.getClass() == field.getType()) {
                setter.invoke(config,value);
            } else {
                Converter converter = converters.getConverter(value.getClass(),field.getType());
                if (converter != null) {
                    setter.invoke(config,converter.convert(value));
                } else {
                    throw new RuntimeException("can not converter from " + value.getClass() + " to " + field.getType());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getPropertyName() {
        return item.getName();
    }

    public String getPropertyDesc() {
        return item.getDesc();
    }

    public QtResource getResource() {
        return resource;
    }

    public String getPropertyResource() {
        return item.getResource();
    }
}
