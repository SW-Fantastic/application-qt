package org.swdc.qt.view;

import io.qt.core.QModelIndex;

public interface ItemEditedListener<T> {

    void changed(T target, Object newVal, QModelIndex index, String propertyName);

}
