package org.swdc.qt.utils;

import io.qt.core.QObject;
import io.qt.core.QThreadPool;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

/**
 * 对Qt的ThreadPool进行包装，提供ExecutorService接口。
 */
public class QtThreadPoolExecutor extends AbstractExecutorService {

    private QThreadPool threadPool;

    public QtThreadPoolExecutor() {
        threadPool = new QThreadPool();
    }

    public QtThreadPoolExecutor(int maxSize) {
        threadPool = new QThreadPool();
        threadPool.setMaxThreadCount(maxSize);
    }

    public QtThreadPoolExecutor(QObject parent) {
        if (parent != null) {
            threadPool = new QThreadPool(parent);
        } else {
            threadPool = new QThreadPool();
        }
    }

    public QtThreadPoolExecutor(QObject parent, int maxSize) {
        if (parent != null) {
            threadPool = new QThreadPool(parent);
        } else {
            threadPool = new QThreadPool();
        }
        threadPool.setMaxThreadCount(maxSize);
    }

    @Override
    public void shutdown() {
        if (!threadPool.isDisposed()) {
            threadPool.waitForDone();
            threadPool.dispose();
        }
    }

    @Override
    public List<Runnable> shutdownNow() {
        if (!threadPool.isDisposed()) {
            threadPool.dispose();
        }
        return Collections.emptyList();
    }

    @Override
    public boolean isShutdown() {
        return threadPool.isDisposed();
    }

    @Override
    public boolean isTerminated() {
        return threadPool.isDisposed();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        if (threadPool.isDisposed()) {
            return true;
        }
        return threadPool.waitForDone((int)unit.toMillis(timeout));
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return new QtFuture(() -> {
            try {
                return callable.call();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return new QtFuture<>(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return value;
        });
    }

    @Override
    public void execute(Runnable command) {
        QtFuture future = null;
        if (command instanceof QtFuture) {
            future = (QtFuture)command;
        } else {
            future = (QtFuture) newTaskFor(command,null);
        }
        threadPool.start(future.getRunnable());
    }
}
