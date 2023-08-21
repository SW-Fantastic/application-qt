package org.swdc.qt.config.editors;

import io.qt.widgets.QHBoxLayout;
import io.qt.widgets.QLabel;
import io.qt.widgets.QLineEdit;
import io.qt.widgets.QWidget;
import org.swdc.ours.common.type.Converter;
import org.swdc.qt.QtResource;
import org.swdc.qt.config.PropertyEditorView;
import org.swdc.qt.config.PropertyItem;

public class TextPropertyEditor extends PropertyEditorView {

    private QWidget editor;

    public TextPropertyEditor(QtResource resource,Object config, PropertyItem item) {
        super(resource,config, item);
    }

    private void create(QWidget parent){
        editor = new QWidget(parent);

        QHBoxLayout layout = new QHBoxLayout();
        editor.setLayout(layout);

        QLabel label = new QLabel(editor);
        label.setText(getPropertyName());

        QLineEdit textField = new QLineEdit(editor);
        Object value = getValue();
        if (value instanceof String) {
            textField.setText((String) value);
        } else {
            Converter converter = converters.getConverter(value.getClass(),String.class);
            if (converter == null) {
                textField.setText(value.toString());
            } else {
                textField.setText(converter.convert(value).toString());
            }
        }

        layout.addWidget(label);
        layout.addWidget(textField);
        layout.setStretch(0,2);
        layout.setStretch(1,8);

        textField.editingFinished.connect(() -> {
            setValue(textField.text());
        });
        textField.setMinimumHeight(26);
    }

    @Override
    public QWidget getEditor(QWidget widget) {
        if (editor == null) {
            create(widget);
        }
        return editor;
    }
}
