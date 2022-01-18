package com.ferick;

import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;

public class Launcher<T, R> {

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
    public <U> void launch(List<U> scenarios, ResultHandler<R> resultHandler, BiFunction<T, U, R> function) {
        CompletionService<R> completionService = new ExecutorCompletionService<>(executor);
        launch(completionService, scenarios.size(), resultHandler, () ->
                scenarios.forEach(scenario -> completionService.submit(new ScenarioTask<>(resources, scenario, function))));
    }

    /**
     * Method for multithreading launching scenarios using bounded resource pool
     *
     * @param count count to be launched
     * @param resultHandler handler for saving and getting scenario execution results
     * @param function scenario execution logic
     */
    public void launch(Integer count, ResultHandler<R> resultHandler, Function<T, R> function) {
        CompletionService<R> completionService = new ExecutorCompletionService<>(executor);
        launch(completionService, count, resultHandler, () ->
                IntStream.range(0, count).forEach(i -> completionService.submit(new SimpleTask<>(resources, function))));
    }

    private void launch(CompletionService<R> completionService,
                        Integer count,
                        ResultHandler<R> resultHandler,
                        Runnable runnable) {
        runnable.run();
        IntStream.range(0, count).forEach(i -> {
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
