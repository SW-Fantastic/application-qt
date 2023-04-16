package org.swdc.qt.utils;

import io.qt.core.QFuture;
import io.qt.core.QFutureInterface;
import io.qt.core.QRunnable;

import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * 对Qt的Future进行包装，提供标准的javaAPI。
 * @param <V>
 */
public class QtFuture<V> implements RunnableFuture<V> {

    private QFuture<V> future;
    private QFutureInterface<V> futureInterface;
    private QRunnable runnable;

    QtFuture(Supplier<V> function) {
        futureInterface = new QFutureInterface<>();
        runnable = new QRunnable.Impl() {
            @Override
            public void run() {
                futureInterface.reportStarted();
                V result = function.get();
                if (result == null) {
                    futureInterface.reportFinished();
                } else {
                    futureInterface.reportFinished(result);
                }
            }
        };
        futureInterface.setRunnable(runnable);
        future = futureInterface.future();
    }

    public QRunnable getRunnable() {
        return runnable;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        future.cancel();
        return isCancelled();
    }

    @Override
    public boolean isCancelled() {
        return future.isCanceled();
    }

    @Override
    public boolean isDone() {
        return future.isFinished();
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        return future.result();
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        future.waitForFinished();
        return future.result();
    }

    @Override
    public void run() {
        runnable.run();
    }
}
