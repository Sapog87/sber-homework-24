package org.sber.task;

import java.util.concurrent.Callable;

public class Task<T> {
    private volatile T result;
    private volatile RuntimeException exception;
    private final Callable<T> callable;

    public Task(Callable<T> callable) {
        this.callable = callable;
    }

    public T get() {
        if (result != null) {
            return result;
        }
        if (exception != null) {
            throw exception;
        }
        synchronized (this) {
            if (result != null) {
                return result;
            }
            if (exception != null) {
                throw exception;
            }
            try {
                result = callable.call();
                return result;
            } catch (Exception e) {
                exception = new CallFailedException(e);
                throw exception;
            }
        }
    }
}
