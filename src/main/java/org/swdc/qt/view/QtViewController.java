package org.swdc.qt.view;

import io.qt.core.QMetaObject;
import io.qt.core.QObject;
import io.qt.core.Qt;
import org.swdc.dependency.EventEmitter;
import org.swdc.dependency.event.AbstractEvent;
import org.swdc.dependency.event.Events;

public abstract class QtViewController<T extends AbstractQtView> extends QObject implements SignalSupport, EventEmitter {

    public abstract void initialize(T view);

    private Events events;

    @Override
    public void setEvents(Events events) {
        this.events = events;
    }

    @Override
    public <T extends AbstractEvent> void emit(T event) {
        events.dispatch(event);
    }

    protected void signalConnect(QMetaObject.AbstractSignal signal, String slot) {
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

    protected void signalConnect(QMetaObject.AbstractSignal signal, String slot, Qt.ConnectionType type) {
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
