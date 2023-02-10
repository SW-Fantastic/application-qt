package org.swdc.qt.view;

import io.qt.core.QMetaObject;
import io.qt.core.QObject;

public interface QtViewController<T extends AbstractQtView> extends SignalSupport {

    void initialize(T view);

}
