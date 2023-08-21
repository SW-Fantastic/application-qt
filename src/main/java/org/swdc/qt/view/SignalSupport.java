package org.swdc.qt.view;

import io.qt.core.QMetaObject;
import io.qt.core.QObject;
import io.qt.core.Qt;
import org.swdc.dependency.utils.ReflectionUtil;
import org.swdc.ours.common.type.ClassTypeAndMethods;

import java.lang.invoke.SerializedLambda;
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
        List<Method> methods = ClassTypeAndMethods.findAllMethods(receiver.getClass());
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

    default void signalConnect(Object receiver, QMetaObject.AbstractPublicSignal0 signal, QMetaObject.Slot0 slot, Qt.ConnectionType connectionType) {
        doSignalConnect(receiver,signal,slot,connectionType);
    }

    default void signalConnect(QMetaObject.AbstractPublicSignal0 signal, QMetaObject.Slot0 slot) {
        doSignalConnect(this,signal,slot, Qt.ConnectionType.AutoConnection);
    }

    default <T> void signalConnect(Object receiver, QMetaObject.AbstractPublicSignal1<T> signal, QMetaObject.Slot1<T> slot1, Qt.ConnectionType connectionType) {
        doSignalConnect(receiver,signal,slot1,connectionType);
    }

    default <T> void signalConnect(QMetaObject.AbstractPublicSignal1<T> signal, QMetaObject.Slot1<T> slot1) {
        doSignalConnect(this,signal,slot1, Qt.ConnectionType.AutoConnection);
    }

    default <A,B> void signalConnect(Object receiver, QMetaObject.AbstractPublicSignal2<A,B> signal, QMetaObject.Slot2<A,B> slot, Qt.ConnectionType type) {
        doSignalConnect(receiver,signal,slot, type);
    }

    default <A,B> void signalConnect(QMetaObject.AbstractPublicSignal2<A,B> signal, QMetaObject.Slot2<A,B> slot) {
        doSignalConnect(this,signal,slot, Qt.ConnectionType.AutoConnection);
    }

    default <A,B,C> void signalConnect(Object receiver, QMetaObject.AbstractPublicSignal3<A,B,C> signal, QMetaObject.Slot3<A,B,C> slot, Qt.ConnectionType type) {
        doSignalConnect(receiver,signal,slot, type);
    }

    default <A,B,C> void signalConnect( QMetaObject.AbstractPublicSignal3<A,B,C> signal, QMetaObject.Slot3<A,B,C> slot) {
        doSignalConnect(this,signal,slot, Qt.ConnectionType.AutoConnection);
    }

    default <A,B,C,D> void signalConnect(Object receiver, QMetaObject.AbstractPublicSignal4<A,B,C,D> signal, QMetaObject.Slot4<A,B,C,D> slot, Qt.ConnectionType type) {
        doSignalConnect(receiver,signal,slot, type);
    }

    default <A,B,C,D> void signalConnect( QMetaObject.AbstractPublicSignal4<A,B,C,D> signal, QMetaObject.Slot4<A,B,C,D> slot) {
        doSignalConnect(this,signal,slot, Qt.ConnectionType.AutoConnection);
    }

    default <A,B,C,D,E> void signalConnect(Object receiver, QMetaObject.AbstractPublicSignal5<A,B,C,D,E> signal, QMetaObject.Slot5<A,B,C,D,E> slot, Qt.ConnectionType type) {
        doSignalConnect(receiver,signal,slot, type);
    }

    default <A,B,C,D,E> void signalConnect(QMetaObject.AbstractPublicSignal5<A,B,C,D,E> signal, QMetaObject.Slot5<A,B,C,D,E> slot) {
        doSignalConnect(this,signal,slot, Qt.ConnectionType.AutoConnection);
    }

    default  <T extends QMetaObject.AbstractSlot> void  doSignalConnect(Object receiver, QMetaObject.AbstractSignal signal, T slot, Qt.ConnectionType connectionType) {
        SerializedLambda lambda = ClassTypeAndMethods.extractSerializedLambda(slot);
        if (lambda == null) {
            throw new RuntimeException("it is not a serialized lambda");
        }
        String qualifyName = lambda.getImplClass().replace("/",".");
        if (!qualifyName.equals(receiver.getClass().getName())) {
            throw new RuntimeException("the class is mismatched : provides - " + receiver.getClass().getName() + " requires : " + qualifyName);
        }

        String theMethod = lambda.getImplMethodName();
        Method implMethod = null;
        List<Method> methods = ClassTypeAndMethods.findAllMethods(receiver.getClass());
        for (Method method : methods) {
            if (method.getName().equals(theMethod)) {
                implMethod = method;
                break;
            }
        }
        if (implMethod == null) {
            throw new RuntimeException("no such method on class " + receiver.getClass().getName() + "#" + theMethod);
        }
        signal.connect(receiver,slotMethod(implMethod),connectionType);
    }

}
