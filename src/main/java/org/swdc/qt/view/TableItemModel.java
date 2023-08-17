package org.swdc.qt.view;

import io.qt.NonNull;
import io.qt.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.dependency.utils.AnnotationDescription;
import org.swdc.dependency.utils.AnnotationUtil;
import org.swdc.dependency.utils.ReflectionUtil;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class TableItemModel<T> extends QAbstractTableModel {

    static class ColumnItem {

        private Qt.ItemFlags flags;

        private String name;
        private Field field;
        private double width;
        private Function<Object,String> func;
        private Qt.Alignment alignment;

        public void setAlignment(Qt.Alignment alignment) {
            this.alignment = alignment;
        }

        public Qt.Alignment getAlignment() {
            return alignment;
        }

        public void setFunc(Function<Object, String> func) {
            this.func = func;
        }

        public Function<Object, String> getFunc() {
            return func;
        }

        public double getWidth() {
            return width;
        }

        public void setWidth(double width) {
            this.width = width;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Field getField() {
            return field;
        }

        public void setField(Field field) {
            this.field = field;
        }

        public void setFlags(Qt.ItemFlag ...flags) {
            if (flags != null && flags.length > 0) {
                this.flags = Qt.ItemFlag.flags(flags);
            }
        }

    }

    private Map<String,ColumnItem> namedFieldMap = new HashMap<>();
    private Map<String,ItemEditedListener<T>> namedListenerMap = new HashMap<>();
    private List<String> properties = new ArrayList<>();


    private List<T> items = new ArrayList<>();

    private Class<T> itemType;

    private Logger logger = LoggerFactory.getLogger(TableItemModel.class);

    private double headerHeight;

    public TableItemModel(Class<T> itemType) {
        this.itemType = itemType;
    }

    public void setHeaderHeight(double headerHeight) {
        this.headerHeight = headerHeight;
    }

    @Override
    public Object headerData(int section, Qt.Orientation orientation, int role) {

        if (orientation == Qt.Orientation.Horizontal) {
            if (section >= properties.size()) {
                return new QVariant();
            }
            String property = properties.get(section);
            ColumnItem item = namedFieldMap.get(property);
            if (role == Qt.ItemDataRole.DisplayRole) {
                return new QVariant(item.getName());
            } else if (role == Qt.ItemDataRole.SizeHintRole) {
                return new QVariant(new QSizeF(item.width,headerHeight));
            } else if (role == Qt.ItemDataRole.TextAlignmentRole) {
                return new QVariant(item.getAlignment());
            }
            return new QVariant();
        } else {
            return "" + section;
        }
    }

    public void addColumn(String propertyName, String columnName, double width) {
        addColumn(propertyName,columnName,width,null,new Qt.Alignment(Qt.AlignmentFlag.AlignCenter),new Qt.ItemFlag[0]);
    }

    public void addColumn(String propertyName, String columnName, double width, Qt.Alignment alignment) {
        addColumn(propertyName,columnName,width,null,alignment,new Qt.ItemFlag[0]);
    }


    public void addColumn(String propertyName, String columnName, double width,Function<Object,String> func) {
        addColumn(propertyName,columnName,width,func,new Qt.Alignment(Qt.AlignmentFlag.AlignCenter),new Qt.ItemFlag[0]);
    }

    public void addColumn(String propertyName,String columnName, double width, Function<Object,String> getter, Qt.Alignment alignment) {
        addColumn(propertyName,columnName,width,getter,alignment,new Qt.ItemFlag[0]);
    }

    public void addColumn(String propertyName, String columnName, double width, Qt.ItemFlag ...flags) {
        addColumn(propertyName,columnName,width,null,new Qt.Alignment(Qt.AlignmentFlag.AlignCenter),flags);
    }

    public void addColumn(String propertyName, String columnName, double width, Function<Object,String> getter, Qt.Alignment alignment,Qt.ItemFlag ...flags) {
        try {
            Field propField = itemType.getDeclaredField(propertyName);
            propField.setAccessible(true);
            beginInsertColumns(null,namedFieldMap.size() == 0 ? 0 : namedFieldMap.size() - 1,namedFieldMap.size() == 0 ? 0 : namedFieldMap.size() - 1);
            ColumnItem item = new ColumnItem();
            item.setField(propField);
            item.setName(columnName);
            item.setWidth(width);
            item.setFunc(getter);
            item.setAlignment(alignment);
            item.setFlags(flags != null && flags.length > 0 ? flags : null);
            namedFieldMap.put(propertyName,item);
            properties.add(propertyName);
            endInsertColumns();
        } catch (Exception e) {
            logger.error("failed to resolve field :" + propertyName + " on item " + itemType.getName(),e);
        }
    }

    public void setOnItemChanged(String propertyName, ItemEditedListener<T> listener) {
        if (!properties.contains(propertyName)) {
            return;
        }
        namedListenerMap.put(propertyName,listener);
    }

    public void removeColumn(String propertyName) {
        if (!properties.contains(propertyName)) {
            return;
        }
        int pos = properties.indexOf(propertyName);
        beginRemoveColumns(null,pos, pos);
        namedFieldMap.remove(propertyName);
        endRemoveColumns();
    }

    @Override
    public int columnCount(QModelIndex parent) {
        return namedFieldMap.size();
    }

    @Override
    public Object data(QModelIndex index, int role) {
        if (items.size() <= index.row()) {
            return new QVariant();
        }
        T item = items.get(index.row());
        String property = properties.get(index.column());
        if (property == null) {
            return new QVariant("No such column");
        }
        ColumnItem field = namedFieldMap.get(property);
        if (role == Qt.ItemDataRole.DisplayRole) {
            try{
                if(field.getFunc() != null) {
                    return new QVariant(
                            field.getFunc().apply(field.getField().get(item))
                    );
                }
                return new QVariant(field.getField().get(item));
            } catch (Exception e) {
                logger.error("failed to load property on filed: " + property + " at " + itemType.getName());
            }
        } else if (role == Qt.ItemDataRole.TextAlignmentRole) {
            return new QVariant(field.getAlignment());
        }
        return new QVariant();
    }

    @Override
    public boolean setData(@NonNull QModelIndex index, Object value, int role) {
        if (index == null || !index.isValid()) {
            return super.setData(index, value, role);
        }
        int col = index.column();
        String propName = properties.get(col);
        if (namedListenerMap.containsKey(propName)) {
            ItemEditedListener<T> listener = namedListenerMap.get(propName);
            listener.changed(items.get(index.row()),value,index,propName);
        }
        return super.setData(index, value, role);
    }

    @Override
    public int rowCount(QModelIndex parent) {
        return items.size();
    }

    public void add(T t) {
        this.beginInsertRows(null,items.size() == 0 ? 0 : items.size() - 1,items.size() == 0 ? 0 : items.size() - 1);
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

    @Override
    public Qt.ItemFlags flags(QModelIndex index) {
        if (index == null || !index.isValid() || index.column() > properties.size()) {
            return super.flags(index);
        }
        String fieldName = properties.get(index.column());
        if (!namedFieldMap.containsKey(fieldName)) {
            return super.flags(index);
        }
        ColumnItem item = this.namedFieldMap.get(fieldName);
        return item.flags != null ? item.flags : super.flags(index);
    }
}
