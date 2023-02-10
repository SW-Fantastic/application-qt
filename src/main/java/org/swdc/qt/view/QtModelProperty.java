package org.swdc.qt.view;

import io.qt.core.Qt;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注于Model对象的字段上，为Qt的View提供必要的信息。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface QtModelProperty {

    /**
     * 对应QtModel的Column name
     */
    String name() default "";

    /**
     * 对应Qt的Role
     */
    int itemDataRole() default Qt.ItemDataRole.DisplayRole;

    /**
     * 对于多个Column的View，提供Column index
     */
    int columnIndex() default -1;

}
