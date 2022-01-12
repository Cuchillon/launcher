package com.ferick;

import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Class implementing a blocking resource pool
 * The resource is obtained by calling the method take()
 * After finishing work with resource it is needed to call the free() method to return the resource to the pool
 *
 * @param <T> resource type
 */
public class BoundedResourcePool<T> {

    private final BlockingQueue<T> pool;
    private final ThreadLocal<T> occupiedResource;

    public BoundedResourcePool(Set<T> resourceSet) {
        this.pool = new ArrayBlockingQueue<>(resourceSet.size(), Boolean.TRUE, resourceSet);
        this.occupiedResource = new ThreadLocal<>();
    }

    /**
     * Method for getting resource from the pool
     * The reference to the resource received from the pool is stored in thread local variable
     * for later return to the pool
     *
     * @return resource
     * @throws InterruptedException
     */
    public T take() throws InterruptedException {
        var resource = pool.take();
        occupiedResource.set(resource);
        return resource;
    }

    /**
     * Method called at the end of working with the resource which returns the resource
     * from the thread local variable to the pool
     *
     * @throws InterruptedException
     */
    public void free() throws InterruptedException {
        pool.put(occupiedResource.get());
    }
}
