package org.sber.task;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class TaskTest {

    @Test
    @DisplayName("Проверяем, что Callable вызывается только 1 раз")
    void getValue() throws InterruptedException {
        var callable = new Callable<Integer>() {

            @Override
            public Integer call() {
                return 1;
            }
        };

        var spy = Mockito.spy(callable);
        var task = new Task<>(spy);

        // запускаем Task в нескольких потоках
        var executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < 10; i++) {
            executorService.execute(task::get);
        }

        // ждем пока все Task выполнятся
        executorService.shutdown();
        executorService.awaitTermination(500, TimeUnit.MILLISECONDS);

        // проверяем, что был только 1 вызов Callable
        verify(spy, times(1)).call();
    }

    @Test
    @DisplayName("Проверяем, что Callable вызывается только 1 раз и все потоки кидают ошибку")
    void getException() {
        var callable = new Callable<Integer>() {

            @Override
            public Integer call() {
                throw new RuntimeException();
            }
        };
        var spy = Mockito.spy(callable);
        var task = new Task<>(spy);

        // запускаем Task в нескольких потоках
        var executorService = Executors.newCachedThreadPool();
        var futures = new ArrayList<Future<Integer>>();
        for (int i = 0; i < 10; i++) {
            futures.add(executorService.submit(task::get));
        }

        // ждем пока все Task выполнятся
        executorService.shutdown();

        // проверяем, что во всех потоках был выкинут Exception
        for (var future : futures) {
            var exception = assertThrowsExactly(ExecutionException.class, future::get);
            assertEquals(CallFailedException.class, exception.getCause().getClass());
        }

        // проверяем, что был только 1 вызов Callable
        verify(spy, times(1)).call();
    }
}