package org.jhotdraw8.icollection.impl.linked;

import org.jhotdraw8.icollection.impl.IdentityObject;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class LinkedElement<E> {
    private final E value;
    private @Nullable E prev;
    private @Nullable E next;
    private final @Nullable IdentityObject owner;

    public LinkedElement(E value, @Nullable E prev, @Nullable E next) {
        this(value, prev, next, null);
    }

    public LinkedElement(E value, @Nullable E prev, @Nullable E next, @Nullable IdentityObject owner) {
        this.value = value;
        this.prev = prev;
        this.next = next;
        this.owner = owner;
    }

    public boolean isOwned(@Nullable IdentityObject owner) {
        return owner != null && this.owner == owner;
    }

    public void setNext(@Nullable E e) {
        next = e;
    }

    public void setPrev(@Nullable E e) {
        prev = e;
    }

    public E key() {
        return value;
    }

    public @Nullable E prev() {
        return prev;
    }

    public @Nullable E next() {
        return next;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (LinkedElement) obj;
        return Objects.equals(this.value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return "LinkedElement[" +
                "value=" + value + ", " +
                "prev=" + prev + ", " +
                "next=" + next + ", " +
                "owner=" + owner + ']';
    }

}
