package org.swdc.qt.view;

import io.qt.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.dependency.utils.AnnotationDescription;
import org.swdc.dependency.utils.AnnotationUtil;
import org.swdc.dependency.utils.ReflectionUtil;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 对Qt的ListModal进行包装，提供方便使用的Java类型。
 * @param <T>
 */
public class ListItemModal<T> extends QAbstractListModel {

    private Logger logger = LoggerFactory.getLogger(ListItemModal.class);
    private List<T> items = new ArrayList<>();

    private Map<Integer,Field> roleFieldMap = new HashMap<>();
    private QSize cellSize = null;

    public ListItemModal(Class<T> itemType) {
        List<Field> propertyFields = ReflectionUtil.findFieldsByAnnotation(itemType, QtModelProperty.class);
        for (Field field : propertyFields) {
            field.setAccessible(true);
            AnnotationDescription desc = AnnotationUtil.findAnnotation(field, QtModelProperty.class);
            int role = desc.getProperty(int.class,"itemDataRole");
            if (!roleFieldMap.containsKey(role)) {
                roleFieldMap.put(role,field);
            } else {
                logger.warn("duplicated item data role:" + field.getName());
            }
        }
    }

    public void setCellSize(QSize cellSize) {
        this.cellSize = cellSize;
    }

    public QSize getCellSize() {
        return cellSize;
    }

    @Override
    public Object data(QModelIndex index, int role) {
        try {
            if (roleFieldMap.size() == 0 && items.size() > index.row()) {
                return new QVariant(items.get(index.row()));
            } else if (roleFieldMap.containsKey(role) && items.size() > index.row()) {
                Field field = roleFieldMap.get(role);
                T object = items.get(index.row());
                return new QVariant(field.get(object));
            } else if (role == Qt.ItemDataRole.SizeHintRole && cellSize != null) {
                return new QVariant(cellSize);
            }
            return new QVariant();
        } catch (Exception e) {
            logger.error("failed to load data", e);
            return new QVariant();
        }
    }

    @Override
    public QModelIndex index(int row, int column, QModelIndex parent) {
        return createIndex(row,column);
    }

    @Override
    public int rowCount(QModelIndex parent) {
        return items.size();
    }

    public List<T> getItems() {
        return items;
    }

}
