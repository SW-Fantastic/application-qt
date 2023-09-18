package org.swdc.qt;

import org.swdc.qt.view.Splash;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SWQtApplication {

    String[] icons() default {};

    Class[] configures();

    String assetsFolder() default "assets";

    Class<? extends Splash> splash() default Splash.class;

}
