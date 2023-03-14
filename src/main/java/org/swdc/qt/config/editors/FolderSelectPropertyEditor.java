package org.swdc.qt.config.editors;

import io.qt.core.Qt;
import io.qt.widgets.QComboBox;
import io.qt.widgets.QHBoxLayout;
import io.qt.widgets.QLabel;
import io.qt.widgets.QWidget;
import org.swdc.qt.QtResource;
import org.swdc.qt.config.PropertyEditorView;
import org.swdc.qt.config.PropertyItem;
import org.swdc.qt.view.ListItemModel;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class FolderSelectPropertyEditor extends PropertyEditorView {

    private static class FileItem {
        private String name;
        public FileItem(File file) {
            this.name = file.getName();
        }
        public String getName() {
            return name;
        }
    }

    private QWidget editor;
    private ListItemModel<FileItem> items;
    private QComboBox comboBox;

    public FolderSelectPropertyEditor(QtResource resource,Object config, PropertyItem item) {
        super(resource,config, item);
    }

    private void create(QWidget parent) {
        editor = new QWidget(parent);
        items = new ListItemModel<>(FileItem.class,"name");

        QHBoxLayout layout = new QHBoxLayout();
        QLabel label = new QLabel(editor);
        label.setText(getPropertyName());
        layout.addWidget(label);

        comboBox = new QComboBox(editor);
        comboBox.setMinimumHeight(26);
        comboBox.setFocusPolicy(Qt.FocusPolicy.NoFocus);
        comboBox.setModel(items);
        layout.addWidget(comboBox);

        refreshFolder();

        String folderName = getValue();
        for (FileItem item: items.items()) {
            if (item.getName().equals(folderName)) {
                comboBox.setCurrentIndex(items.items().indexOf(item));
                break;
            }
        }

        comboBox.currentIndexChanged.connect(idx -> {
            String newValue = items.get(idx).getName();
            setValue(newValue);
        });
        editor.setLayout(layout);

        layout.setStretch(0,2);
        layout.setStretch(1,8);
    }

    private void refreshFolder() {

        int curr = comboBox.currentIndex();
        FileItem item = curr >= 0 && items.items().size() > 0 ? items.get(curr) : null;

        String path = getPropertyResource();
        QtResource resources = getResource();
        Path assets = resources.getAssetFolder().toPath();

        try {

            Path selectingFolderPath = assets.resolve(path);
            List<FileItem> folders = Files.list(selectingFolderPath)
                    .filter(Files::isDirectory)
                    .map(p -> new FileItem(p.toFile()))
                    .collect(Collectors.toList());

            items.clear();
            items.addAll(folders);

            if (item != null) {
                for (FileItem cur: folders) {
                    if (cur.getName().equals(item.getName())) {
                        comboBox.setCurrentIndex(folders.indexOf(cur));
                        break;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public QWidget getEditor(QWidget parent) {
        if (editor == null) {
            create(parent);
        }
        return editor;
    }
}
