package org.swdc.qt.view;

import io.qt.core.Qt;
import jakarta.inject.Scope;
import org.swdc.dependency.annotations.ScopeImplement;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Scope
@ScopeImplement(QtViewManager.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface QView {

    String viewLocation();

    String title() default "";

    Qt.WindowModality modalType() default Qt.WindowModality.NonModal;

    boolean multiple() default false;

    Class controller() default Object.class;

    boolean stage() default true;

    int width() default 800;

    int height() default 600;

    boolean resizeable() default true;

}
