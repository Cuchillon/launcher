package com.ferick;

import java.util.concurrent.Callable;
import java.util.function.Function;

public class SimpleTask<T, R> implements Callable<R> {

    private final BoundedResourcePool<T> resources;
    private final Function<T, R> function;

    public SimpleTask(BoundedResourcePool<T> resources, Function<T, R> function) {
        this.resources = resources;
        this.function = function;
    }

    @Override
    public R call() throws Exception {
        R launchResult = null;
        try {
            var resource = resources.take();
            launchResult = function.apply(resource);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            resources.free();
        }
        return launchResult;
    }
}
