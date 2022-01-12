package com.ferick;

public class ExceptionHandler {

    /** If Throwable is an Error, then issue it;
     * if it is a RuntimeException, then return it,
     * otherwise issue an IllegalStateException
     */
    public static RuntimeException launderThrowable(Throwable t) {
        if (t instanceof RuntimeException) {
            return (RuntimeException) t;
        } else if (t instanceof Error) {
            throw (Error) t;
        } else {
            throw new IllegalStateException("It is not unchecked", t);
        }
    }
}
