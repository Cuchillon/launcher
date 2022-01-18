package com.ferick;

import java.util.concurrent.Callable;
import java.util.function.BiFunction;

public class ScenarioTask<T, U, R> implements Callable<R> {

    private final BoundedResourcePool<T> resources;
    private final U scenario;
    private final BiFunction<T, U, R> function;

    public ScenarioTask(BoundedResourcePool<T> resources, U scenario, BiFunction<T, U, R> function) {
        this.resources = resources;
        this.scenario = scenario;
        this.function = function;
    }

    @Override
    public R call() throws Exception {
        R launchResult = null;
        try {
            var resource = resources.take();
            launchResult = function.apply(resource, scenario);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            resources.free();
        }
        return launchResult;
    }
}
