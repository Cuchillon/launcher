package com.ferick;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LauncherTest {

    private static final Set<Integer> RESOURCES = IntStream.rangeClosed(1, 10).boxed().collect(Collectors.toSet());
    private static final List<Scenario> SCENARIOS = IntStream.rangeClosed(1, 100).mapToObj(Scenario::new).collect(Collectors.toList());
    private static final List<String> STORAGE = Collections.synchronizedList(new ArrayList<>());

    @Test
    void launchScenarioTest() {
        var launcher = new Launcher<Integer, String>(RESOURCES);
        var testHandler = new TestHandler();
        launcher.launch(SCENARIOS, testHandler, (resource, scenario) -> scenario.act(resource));
        launcher.shutdown();
        var results = testHandler.getResults();
        results.forEach(System.out::println);
        assertEquals(100, results.size());
    }

    @Test
    void launchSimpleTest() {
        var launcher = new Launcher<Integer, String>(RESOURCES);
        var testHandler = new TestHandler();
        var rand = new Random();
        launcher.launch(100, testHandler, (resource) -> new Scenario(rand.nextInt(99)).act(resource));
        launcher.shutdown();
        var results = testHandler.getResults();
        results.forEach(System.out::println);
        assertEquals(100, results.size());
    }

    @AfterEach
    void clearStorage() {
        STORAGE.clear();
        System.out.println("END OF TEST---------------------------------------------------------------------------");
    }

    static class Scenario {

        private final Integer number;

        Scenario(Integer number) {
            this.number = number;
        }

        String act(Integer resource) {
            var timeout = resource * new Random().nextInt(99);
            try {
                TimeUnit.MILLISECONDS.sleep(timeout);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return number + ". RESULT: resource " + resource + ", " + timeout + " ms" +
                    ", thread " + Thread.currentThread().getName();
        }
    }

    static class TestHandler implements ResultHandler<String> {

        @Override
        public void saveResult(String result) {
            STORAGE.add(result);
        }

        @Override
        public List<String> getResults() {
            return Collections.unmodifiableList(STORAGE);
        }
    }
}