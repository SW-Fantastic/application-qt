package org.swdc.qt.view;

import io.qt.core.Qt;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于Controller，标注的方法将会在初始化时自动
 * 完成Signal-Slot链接。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SignalConnect {

    /**
     * 组件名称，必须填写，用于在初始化的时候
     * 通过findChild查找对应的组件以便将它的Signal自动链接到
     * 本方法。
     */
    String name();

    /**
     * 被链接组件的信号名称。
     */
    String signal();

    /**
     * 链接方式，默认是AutoConnection、
     */
    Qt.ConnectionType type() default Qt.ConnectionType.AutoConnection;

}
