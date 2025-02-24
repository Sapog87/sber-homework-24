package org.sber.execution;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
public class ExecutionManagerImpl implements ExecutionManager {
    private final ExecutorService executor;

    @Override
    public Context execute(Runnable callback, Runnable... tasks) {
        var context = new ContextImpl(tasks.length);
        var callbackExecuted = new AtomicBoolean(false);

        for (Runnable task : tasks) {
            executor.execute(() -> {
                if (!context.isInterrupted.get()) {
                    try {
                        task.run();
                        context.completed.incrementAndGet();
                    } catch (Exception e) {
                        context.failed.incrementAndGet();
                    }
                } else {
                    context.interrupted.incrementAndGet();
                }

                if (context.isFinished() && callbackExecuted.compareAndSet(false, true)) {
                    callback.run();
                }
            });
        }

        return context;
    }
}
