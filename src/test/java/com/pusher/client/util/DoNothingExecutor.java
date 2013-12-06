package com.pusher.client.util;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DoNothingExecutor implements ScheduledExecutorService {

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException { return false; }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException { return null; }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException { return null; }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException { return null; }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException { return null; }

    @Override
    public boolean isShutdown() { return false; }

    @Override
    public boolean isTerminated() { return false; }

    @Override
    public void shutdown() { }

    @Override
    public List<Runnable> shutdownNow() { return null; }

    @Override
    public <T> Future<T> submit(Callable<T> task) { return null; }

    @Override
    public Future<?> submit(Runnable task) { return null; }

    @Override
    public <T> Future<T> submit(Runnable task, T result) { return null; }

    @Override
    public void execute(Runnable command) { }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) { return null; }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) { return null; }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) { return null; }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) { return null; }

}
