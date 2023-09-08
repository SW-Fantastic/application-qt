package org.swdc.qt;

import io.qt.core.QThread;
import io.qt.gui.QIcon;
import org.swdc.qt.config.ApplicationConfigure;
import org.swdc.qt.utils.QtThreadPoolExecutor;

import java.io.File;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

public class QtResource {

    private File assetFolder;

    private AbstractExecutorService executor;

    private QIcon appIcon;

    private Class<ApplicationConfigure> configureClass;

    private List<String> args;

    public Class<ApplicationConfigure> getConfigureClass() {
        return configureClass;
    }

    public void setConfigureClass(Class<ApplicationConfigure> configureClass) {
        this.configureClass = configureClass;
    }

    public void setAppIcon(QIcon appIcon) {
        this.appIcon = appIcon;
    }

    public QIcon getAppIcon() {
        return appIcon;
    }

    public void setAssetFolder(File assetFolder) {
        this.assetFolder = assetFolder;
    }

    public void setExecutor(AbstractExecutorService executor) {
        this.executor = executor;
    }

    public File getAssetFolder() {
        return assetFolder;
    }

    public AbstractExecutorService getExecutor() {
        return executor;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }

    public List<String> getArgs() {
        return args;
    }


}
