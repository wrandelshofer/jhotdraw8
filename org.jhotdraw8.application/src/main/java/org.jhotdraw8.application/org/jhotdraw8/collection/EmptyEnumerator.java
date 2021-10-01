package org.jhotdraw8.collection;

public class EmptyEnumerator<E> implements Enumerator<E> {
    private static EmptyEnumerator<Object> singleton = new EmptyEnumerator<>();

    @SuppressWarnings("unchecked")
    public static <T> Enumerator<T> emptyEnumerator() {
        return (Enumerator<T>) singleton;
    }

    private EmptyEnumerator() {

    }

    @Override
    public boolean moveNext() {
        return false;
    }

    @Override
    public E current() {
        return null;
    }
}
