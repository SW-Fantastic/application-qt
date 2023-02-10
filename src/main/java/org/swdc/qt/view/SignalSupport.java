package org.swdc.qt.view;

import io.qt.core.QMetaObject;
import io.qt.core.QObject;

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

}
