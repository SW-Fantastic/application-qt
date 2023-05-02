package org.swdc.qt.view;

import io.qt.core.*;
import io.qt.gui.QPainter;
import io.qt.gui.QPalette;
import io.qt.widgets.QStyle;
import io.qt.widgets.QStyleOptionViewItem;
import io.qt.widgets.QStyledItemDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 对Qt的ListModal进行包装，提供方便使用的Java类型。
 * @param <T>
 */
public class ListItemModel<T> extends QAbstractListModel {

    private Logger logger = LoggerFactory.getLogger(ListItemModel.class);
    private List<T> items = new ArrayList<>();

    private QSize cellSize = null;
    private Field theDisplay = null;

    public ListItemModel(Class<T> itemType,String propertyField) {
        if (propertyField != null) {
            try{
                theDisplay = itemType.getDeclaredField(propertyField);
                theDisplay.setAccessible(true);
            } catch (Exception e) {
                logger.error("failed to read field on type: " + itemType.getName(),e);
            }
        }
    }

    @Override
    public Object headerData(int section, Qt.Orientation orientation, int role) {
        return super.headerData(section, orientation, role);
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
            if (theDisplay == null && index.row() < items.size()) {
                return new QVariant(items.get(index.row()));
            } else if (role == Qt.ItemDataRole.SizeHintRole && cellSize != null) {
                return new QVariant(cellSize);
            } else if (index.row() < items.size() && role == Qt.ItemDataRole.DisplayRole){
                return new QVariant(theDisplay.get(items.get(index.row())));
            } else if (role == Qt.ItemDataRole.UserRole) {
                return new QVariant(items.get(index.row()));
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

    public void add(T t) {
        this.beginInsertRows(null,items.size() == 0 ? 0 : items.size() - 1,items.size());
        items.add(t);
        this.endInsertRows();
    }

    public void remove(T t) {
        int index = items.indexOf(t);
        if (index < 0) {
            return;
        }
        this.beginRemoveRows(null,index,index);
        items.remove(t);
        this.endRemoveRows();
    }

    public void addAll(Collection<T> ts) {
        for (T t: ts) {
            this.add(t);
        }
    }

    public void removeAll(Collection<T> ts) {
        for (T t: ts) {
            this.remove(t);
        }
    }

    public void clear() {
        this.beginResetModel();
        items.clear();
        this.endResetModel();
    }

    public T get(int idx) {
        return items.get(idx);
    }

    public List<T> items() {
        return Collections.unmodifiableList(items);
    }

}
