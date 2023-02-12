package org.swdc.qt.view;

import io.qt.core.QMetaObject;
import io.qt.core.QObject;
import io.qt.core.Qt;
import org.swdc.dependency.utils.ReflectionUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

public interface SignalSupport {

    /**
     * 在QThread中调用此方法，将会调用本对象对应的method。
     * 通常用于异步的UI更新，通过本方法调用位于主线程的UI更新方法达到异步更新
     * UI的效果。
     *
     * @param methodName 被调用的method名称
     * @param params 此method的参数
     */
    default void async(String methodName, Object ...params) {
        if (!QObject.class.isAssignableFrom(this.getClass())) {
            throw new RuntimeException("emit must be calling on a QObject, please extends the QObject class.");
        }
        QMetaObject.invokeMethod((QObject)this,methodName,params);
    }

    default String slotMethod(Method method) {
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
        return sig.toString();
    }

    default String slotMethod(Object receiver, String methodName) {
        List<Method> methods = ReflectionUtil.findAllMethods(receiver.getClass());
        for(Method method: methods) {
            if (method.getName().equals(methodName)) {
                return slotMethod(method);
            }
        }
        throw new RuntimeException("no such method on class " + receiver.getClass().getName() + "#" + methodName);
    }

    default void signalConnect(Object receiver,QMetaObject.AbstractSignal signal, String slot, Qt.ConnectionType connectionType) {
        String methodSig = slotMethod(receiver,slot);
        signal.connect(receiver,methodSig,connectionType);
    }

}
