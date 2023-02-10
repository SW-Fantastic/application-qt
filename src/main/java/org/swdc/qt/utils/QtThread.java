package org.swdc.qt.utils;

import io.qt.core.QObject;
import io.qt.core.QThread;

/**
 * 对Qt的Thread进行包装，提供标准的JavaAPI。
 */
public class QtThread extends QThread {

    private Runnable runnable = this::exec;

    public QtThread() {
    }

    public QtThread(Runnable runnable) {
        if (runnable != null) {
            this.runnable = runnable;
        }
    }

    public QtThread(QObject parent, Runnable runnable) {
        super(parent);
        if (runnable != null) {
            this.runnable = runnable;
        }
    }

    @Override
    protected void run() {
        if (runnable != null) {
            runnable.run();
        }
    }
}
