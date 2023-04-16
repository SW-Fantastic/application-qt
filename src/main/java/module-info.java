module swdc.application.qt {

    requires jakarta.inject;
    requires lesscss.engine;
    requires org.slf4j;
    requires swdc.application.dependency;
    requires swdc.application.configs;
    requires java.desktop;

    requires qtjambi;
    requires qtjambi.uitools;
    requires qtjambi.opengl;
    requires qtjambi.openglwidgets;

    exports org.swdc.qt;
    exports org.swdc.qt.view;
    exports org.swdc.qt.config;
    exports org.swdc.qt.font;
    exports org.swdc.qt.utils;

    opens org.swdc.qt.config to
            swdc.application.configs;

    opens platforms;

}