package com.ferick;

import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

public class Launcher<T, U, R> {

    private final ExecutorService executor;
    private final BoundedResourcePool<T> resources;

    public Launcher(Set<T> resources) {
        this.executor = Executors.newFixedThreadPool(resources.size());
        this.resources = new BoundedResourcePool<>(resources);
    }

    /**
     * Method for multithreading launching scenarios using bounded resource pool
     *
     * @param scenarios scenarios to be launched
     * @param resultHandler handler for saving and getting scenario execution results
     * @param function scenario execution logic
     */
    public void launch(List<U> scenarios, ResultHandler<R> resultHandler, BiFunction<T, U, R> function) {
        CompletionService<R> completionService = new ExecutorCompletionService<>(executor);
        scenarios.forEach(scenario -> completionService.submit(new Task<>(resources, scenario, function)));
        IntStream.range(0, scenarios.size()).forEach(i -> {
            try {
                var launchResultFuture = completionService.take();
                var launchResult = launchResultFuture.get();
                resultHandler.saveResult(launchResult);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                throw ExceptionHandler.launderThrowable(e.getCause());
            }
        });
    }

    /**
     * Method for finishing scenario launcher working
     */
    public void shutdown() {
        executor.shutdown();
    }
}
