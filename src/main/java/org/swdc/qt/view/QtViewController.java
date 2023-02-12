package org.swdc.qt.view;

import io.qt.core.QMetaObject;
import io.qt.core.QObject;
import io.qt.core.Qt;

public interface QtViewController<T extends AbstractQtView> extends SignalSupport {

    void initialize(T view);

    default void signalConnect(QMetaObject.AbstractSignal signal, String slot) {
        if (!QObject.class.isAssignableFrom(this.getClass())) {
            throw new RuntimeException("make current class extends the QObject and then you can use this method");
        }
        signalConnect(
                this,
                signal,
                slot,
                Qt.ConnectionType.AutoConnection
        );
    }

    default void signalConnect(QMetaObject.AbstractSignal signal, String slot, Qt.ConnectionType type) {
        if (!QObject.class.isAssignableFrom(this.getClass())) {
            throw new RuntimeException("make current class extends the QObject and then you can use this method");
        }
        signalConnect(
                this,
                signal,
                slot,
                type
        );
    }

}
