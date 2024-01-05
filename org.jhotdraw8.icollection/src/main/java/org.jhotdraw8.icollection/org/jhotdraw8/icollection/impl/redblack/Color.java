package org.jhotdraw8.icollection.impl.redblack;

public enum Color {

    RED, BLACK;

    @Override
    public String toString() {
        return (this == RED) ? "R" : "B";
    }
}
