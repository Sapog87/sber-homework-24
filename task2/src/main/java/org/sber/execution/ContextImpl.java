package org.sber.execution;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public class ContextImpl implements Context {
    private final int total;
    final AtomicInteger completed = new AtomicInteger(0);
    final AtomicInteger failed = new AtomicInteger(0);
    final AtomicInteger interrupted = new AtomicInteger(0);
    final AtomicBoolean isInterrupted = new AtomicBoolean(false);

    @Override
    public int getCompletedTaskCount() {
        return completed.get();
    }

    @Override
    public int getFailedTaskCount() {
        return failed.get();
    }

    @Override
    public int getInterruptedTaskCount() {
        return interrupted.get();
    }

    @Override
    public void interrupt() {
        isInterrupted.set(true);
    }

    @Override
    public boolean isFinished() {
        return total == completed.get() + failed.get() + interrupted.get();
    }
}
