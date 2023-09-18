module swdc.application.qt {

    requires swdc.commons;

    requires jakarta.inject;
    requires lesscss.engine;
    requires org.slf4j;
    requires swdc.application.dependency;
    requires swdc.application.configs;
    requires java.desktop;

    requires qtjambi;
    requires qtjambi.uitools;

    exports org.swdc.qt;
    exports org.swdc.qt.view;
    exports org.swdc.qt.config;
    exports org.swdc.qt.font;
    exports org.swdc.qt.utils;
    exports org.swdc.qt.config.editors;

    opens org.swdc.qt.config to
            swdc.application.configs;

    opens platforms;

}