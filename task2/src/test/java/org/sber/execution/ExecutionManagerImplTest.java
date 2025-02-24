package org.sber.execution;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ExecutionManagerImplTest {

    @Test
    @DisplayName("Проверка, что все Runnable выполнились успешно")
    void executeFine() throws InterruptedException {
        var executor = Executors.newCachedThreadPool();
        var executionManager = new ExecutionManagerImpl(executor);
        var callback = mock(Runnable.class);
        var task = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        var spyTask = spy(task);
        var context = executionManager.execute(callback, spyTask, spyTask, spyTask, spyTask);

        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.SECONDS);

        assertTrue(context.isFinished());
        assertEquals(4, context.getCompletedTaskCount());
        assertEquals(0, context.getFailedTaskCount());
        assertEquals(0, context.getInterruptedTaskCount());

        verify(spyTask, times(4)).run();
        verify(callback).run();
    }

    @Test
    @DisplayName("Проверка, что все Runnable упали с ошибкой")
    void executeException() throws InterruptedException {
        var executor = Executors.newCachedThreadPool();
        var executionManager = new ExecutionManagerImpl(executor);
        var callback = mock(Runnable.class);
        var task = new Runnable() {
            @Override
            public void run() {
                throw new RuntimeException();
            }
        };
        var spyTask = spy(task);
        var context = executionManager.execute(callback, spyTask, spyTask, spyTask, spyTask);

        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.SECONDS);

        assertTrue(context.isFinished());
        assertEquals(0, context.getCompletedTaskCount());
        assertEquals(4, context.getFailedTaskCount());
        assertEquals(0, context.getInterruptedTaskCount());

        verify(spyTask, times(4)).run();
        verify(callback).run();
    }

    @Test
    @DisplayName("Проверка, что 1 Runnable выполнился, а все остальные были прерваны")
    void executeInterrupted() throws InterruptedException {
        var executor = Executors.newSingleThreadExecutor();
        var executionManager = new ExecutionManagerImpl(executor);
        var callback = mock(Runnable.class);
        var task = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        var spyTask = spy(task);
        var context = executionManager.execute(callback, spyTask, spyTask, spyTask, spyTask);
        Thread.sleep(500);
        context.interrupt();

        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.SECONDS);

        assertTrue(context.isFinished());
        assertEquals(1, context.getCompletedTaskCount());
        assertEquals(0, context.getFailedTaskCount());
        assertEquals(3, context.getInterruptedTaskCount());

        verify(spyTask, times(1)).run();
        verify(callback).run();
    }
}