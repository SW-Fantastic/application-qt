package org.swdc.qt.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PropEditor {

    /**
     * 使用哪个编辑器类对属性进行编辑。
     * @return
     */
    Class editor();

    /**
     * 显示在view的label的名称
     * @return
     */
    String name () default "";

    /**
     * 鼠标悬停在label上面显示的tooltip
     * @return
     */
    String description() default "";

    /**
     * Editor有的时候需要文件操作，这里指定
     * 文件路径。
     * @return
     */
    String resource() default "";

}
