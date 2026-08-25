package org.jhotdraw8.icollection.alt.impl.linked;

import org.jhotdraw8.icollection.alt.impl.champset.BitmapIndexedNode;
import org.jhotdraw8.icollection.alt.impl.champset.Node;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;

public class LinkedElementIterator<E> implements Iterator<E> {
    private @Nullable LinkedElement<E> current;
    private BitmapIndexedNode<LinkedElement<E>> root;
    private final Function<LinkedElement<E>, E> mapper;

    public LinkedElementIterator(LinkedElement<E> current, BitmapIndexedNode<LinkedElement<E>> root, Function<LinkedElement<E>, E> mapper) {
        this.current = current;
        this.root = root;
        this.mapper = mapper;
    }

    @Override
    public boolean hasNext() {
        return current != null;
    }

    @Override
    public E next() {
        if (current == null) {
            throw new NoSuchElementException();
        }
        E value = mapper.apply(current);
        current = get(current.next());
        return value;
    }

    private @Nullable LinkedElement<E> get(@Nullable E o) {
        if (o == null) {
            return null;
        }
        Object result = root.find(
                new LinkedElement<>((E) o, null, null),
                Objects.hashCode(o), 0, Objects::equals);
        return result == Node.NO_DATA ? null : (LinkedElement<E>) result;
    }
}
