package org.swdc.qt.view;


import io.qt.core.QBuffer;
import io.qt.core.QMetaObject;
import io.qt.core.QObject;
import io.qt.core.Qt;
import io.qt.gui.QResizeEvent;
import io.qt.widgets.QVBoxLayout;
import io.qt.widgets.QWidget;
import io.qt.widgets.tools.QUiLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.dependency.DependencyContext;
import org.swdc.dependency.utils.AnnotationDescription;
import org.swdc.dependency.utils.AnnotationUtil;
import org.swdc.dependency.utils.ReflectionUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

public abstract class QtView extends QWidget {

    private Object controller;
    private DependencyContext context;
    private QUiLoader loader;

    private QWidget root;

    protected Logger logger = LoggerFactory.getLogger(QtView.class);

    final void setContext(DependencyContext context) {
        this.context = context;
    }

    final void initView(QBuffer data) {
        if (loader == null) {
            data.reset();
            loader = new QUiLoader();
            root = loader.load(data,this);
        }
    }

    @Override
    protected final void resizeEvent(QResizeEvent event) {
        super.resizeEvent(event);
        if (root != null) {
            root.resize(this.width(),this.height());
        }
        resizedEvent(event);
    }

    protected void resizedEvent(QResizeEvent event) {

    }

    final void setController(Object controller) {
        if (this.controller != null) {
            throw new RuntimeException("view is already initialized.");
        }
        this.controller = controller;

        List<Method> methods = ReflectionUtil.findAllMethods(controller.getClass());
        for (Method method : methods) {
            AnnotationDescription desc = AnnotationUtil.findAnnotation(method,SignalConnect.class);
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

                StringBuilder sig = new StringBuilder(method.getName()).append("(");
                for (int idx = 0; idx < method.getParameters().length; idx++) {
                    Parameter param = method.getParameters()[idx];
                    String typeName = param.getType().getName();
                    sig.append(typeName).append(" ");
                    if (idx + 1 < method.getParameters().length) {
                        sig.append(",");
                    }
                }
                sig.append(")");

                QMetaObject.AbstractSignal signal = (QMetaObject.AbstractSignal) signalField.get(widget);
                signal.connect(controller,sig.toString(),connectionType);
            } catch (Exception e) {
                logger.error("failed to connect slot-method :" + method.getName() + " to widget: " + widgetName + ", error read signal field.",e);
            }
        }

        List<Field> fields = ReflectionUtil.findFieldsByAnnotation(controller.getClass(),QtNamedWidget.class);
        for (Field field: fields) {
            try {
                AnnotationDescription desc = AnnotationUtil.findAnnotation(field,QtNamedWidget.class);
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


    public final  <T extends QObject> T findByName(String name) {
        return (T)findChild(name);
    }
}
