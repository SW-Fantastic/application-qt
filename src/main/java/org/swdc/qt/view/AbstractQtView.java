package org.swdc.qt.view;

import io.qt.core.QBuffer;
import io.qt.core.QMetaObject;
import io.qt.core.QObject;
import io.qt.core.Qt;
import io.qt.gui.QPalette;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.dependency.DependencyContext;
import org.swdc.dependency.EventEmitter;
import org.swdc.dependency.utils.ReflectionUtil;
import org.swdc.ours.common.annotations.AnnotationDescription;
import org.swdc.ours.common.annotations.Annotations;
import org.swdc.ours.common.type.ClassTypeAndMethods;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public interface AbstractQtView extends SignalSupport, EventEmitter {

    Logger logger = LoggerFactory.getLogger(AbstractQtView.class);

    void initView(QBuffer data);

    void setContext(DependencyContext context);

    void setThemePalette(QPalette palette);

    default void initializeController(Object controller) {
        List<Method> methods = ClassTypeAndMethods.findAllMethods(controller.getClass());
        for (Method method : methods) {
            AnnotationDescription desc = Annotations.findAnnotation(method,SignalConnect.class);
            if (desc == null) {
                continue;
            }
            String widgetName = desc.getProperty(String.class,"name");
            String signalName = desc.getProperty(String.class,"signal");
            Qt.ConnectionType connectionType = desc.getProperty(Qt.ConnectionType.class,"type");

            QObject widget = findByName(widgetName);
            if (widget == null) {
                logger.error("failed to connect slot-method : " + method.getName() + " to widget: " + widgetName + ", no such widget on view.");
                continue;
            }
            try {
                Field signalField = widget.getClass().getField(signalName);
                Class type = signalField.getType();

                if (!QMetaObject.AbstractSignal.class.isAssignableFrom(type)) {
                    throw new RuntimeException("the signal-name : " + signalName + " is invalid.");
                }

                QMetaObject.AbstractSignal signal = (QMetaObject.AbstractSignal) signalField.get(widget);
                signal.connect(controller,slotMethod(method),connectionType);
            } catch (Exception e) {
                logger.error("failed to connect slot-method :" + method.getName() + " to widget: " + widgetName + ", error read signal field.",e);
            }
        }

        List<Field> fields = Annotations.findFieldsByAnnotation(controller.getClass(),QtNamedWidget.class);
        for (Field field: fields) {
            try {
                AnnotationDescription desc = Annotations.findAnnotation(field,QtNamedWidget.class);
                String widgetName = desc.getProperty(String.class,"value");
                if (widgetName.isEmpty()) {
                    widgetName = field.getName();
                }
                QObject widget = findByName(widgetName);
                if (widget == null) {
                    logger.error("failed to setup widget: " + widgetName + " on controller " + controller.getClass().getName());
                    continue;
                }
                field.setAccessible(true);
                field.set(controller,widget);
            }catch (Exception e) {
                logger.error("failed to setup widget controller: " + controller.getClass().getName(), e);
            }
        }

        if (controller instanceof QtViewController) {
            QtViewController viewController = (QtViewController) controller;
            viewController.initialize(this);
        }
    }

    <T extends QObject> T findByName(String name);

    void setController(Object controller);

    <T> T getController();

    default void signalConnect(QMetaObject.AbstractSignal signal, String slot) {
        signalConnect(
                getController() == null ? this : getController(),
                signal,
                slot,
                Qt.ConnectionType.AutoConnection
        );
    }

    default void signalConnect(QMetaObject.AbstractSignal signal, String slot, Qt.ConnectionType type) {
        signalConnect(
                getController() == null ? this : getController(),
                signal,
                slot,
                type
        );
    }

    default void signalConnect(QMetaObject.AbstractPublicSignal0 signal, QMetaObject.Slot0 slot) {
        doSignalConnect(getController() == null ? this : getController(),signal,slot, Qt.ConnectionType.AutoConnection);
    }

    default <T> void signalConnect(QMetaObject.AbstractPublicSignal1<T> signal, QMetaObject.Slot1<T> slot1) {
        doSignalConnect(getController() == null ? this : getController(),signal,slot1, Qt.ConnectionType.AutoConnection);
    }

    default <A,B> void signalConnect(QMetaObject.AbstractPublicSignal2<A,B> signal, QMetaObject.Slot2<A,B> slot) {
        doSignalConnect(getController() == null ? this : getController(),signal,slot, Qt.ConnectionType.AutoConnection);
    }

    default <A,B,C> void signalConnect( QMetaObject.AbstractPublicSignal3<A,B,C> signal, QMetaObject.Slot3<A,B,C> slot) {
        doSignalConnect(getController() == null ? this : getController(),signal,slot, Qt.ConnectionType.AutoConnection);
    }

    default <A,B,C,D> void signalConnect( QMetaObject.AbstractPublicSignal4<A,B,C,D> signal, QMetaObject.Slot4<A,B,C,D> slot) {
        doSignalConnect(getController() == null ? this : getController(),signal,slot, Qt.ConnectionType.AutoConnection);
    }

    default <A,B,C,D,E> void signalConnect(QMetaObject.AbstractPublicSignal5<A,B,C,D,E> signal, QMetaObject.Slot5<A,B,C,D,E> slot) {
        doSignalConnect(getController() == null ? this : getController(),signal,slot, Qt.ConnectionType.AutoConnection);
    }

}
