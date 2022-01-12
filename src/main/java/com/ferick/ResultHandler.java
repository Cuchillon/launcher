package com.ferick;

import java.util.List;

/**
 * Result handler to be implemented for saving and getting scenario execution results
 *
 * @param <T> result type
 */
public interface ResultHandler<T> {

    void saveResult(T result);

    List<T> getResults();
}
