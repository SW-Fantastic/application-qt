package org.swdc.qt.config.editors;

import io.qt.core.Qt;
import io.qt.widgets.QCheckBox;
import io.qt.widgets.QHBoxLayout;
import io.qt.widgets.QLabel;
import io.qt.widgets.QWidget;
import org.swdc.ours.common.type.Converter;
import org.swdc.qt.QtResource;
import org.swdc.qt.config.PropertyEditorView;
import org.swdc.qt.config.PropertyItem;


public class BooleanPropertyEditor extends PropertyEditorView {

    private QWidget editor;
    private QCheckBox checkBox;

    public BooleanPropertyEditor(QtResource resource, Object config, PropertyItem item) {
        super(resource, config, item);
    }

    @Override
    public QWidget getEditor(QWidget parent) {
        if (editor == null) {
            create(parent);
            return editor;
        }
        return editor;
    }

    private void create(QWidget parent) {
        editor = new QWidget(parent);

        QHBoxLayout layout = new QHBoxLayout();
        editor.setLayout(layout);

        QLabel label = new QLabel(editor);
        label.setText(getPropertyName());
        Object value = getValue();

        checkBox = new QCheckBox(editor);
        if (value instanceof Boolean) {
            Boolean val = (Boolean)value;
            checkBox.setCheckState(val ? Qt.CheckState.Checked : Qt.CheckState.Unchecked);
        } else {
            Converter converter = converters.getConverter(value.getClass(),Boolean.class);
            if (converter == null) {
                checkBox.setCheckState(Qt.CheckState.Unchecked);
            } else {
                Boolean val = (Boolean) converter.convert(value);
                checkBox.setCheckState(val ? Qt.CheckState.Checked : Qt.CheckState.Unchecked);
            }
        }

        layout.addWidget(label);
        layout.addWidget(checkBox);
        layout.setStretch(0,2);
        layout.setStretch(1,8);
        checkBox.stateChanged.connect(v -> {
            Qt.CheckState state = Qt.CheckState.resolve(v);
            if (state == Qt.CheckState.Checked) {
                setValue(true);
            } else {
                setValue(false);
            }
        });
    }

}
