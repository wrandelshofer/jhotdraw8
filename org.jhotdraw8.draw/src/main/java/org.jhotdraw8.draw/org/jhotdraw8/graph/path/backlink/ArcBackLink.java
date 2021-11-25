package org.jhotdraw8.graph.path.backlink;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.ImmutableList;
import org.jhotdraw8.collection.ImmutableLists;
import org.jhotdraw8.collection.OrderedPair;

import java.util.ArrayDeque;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Represents a back link with cost and depth.
 *
 * @param <V> the vertex type
 * @param <A> the arrow type
 * @param <C> the cost number type
 */
public class ArcBackLink<V, A, C extends Number & Comparable<C>> extends AbstractBackLink<ArcBackLink<V, A, C>, C> {
    private final @NonNull V vertex;
    private final @Nullable A arrow;

    public ArcBackLink(@NonNull V node, @Nullable A arrow, @Nullable ArcBackLink<V, A, C> parent, @NonNull C cost) {
        super(parent, cost);
        this.vertex = node;
        this.arrow = arrow;
    }

    public static <VV, AA, CC extends Number & Comparable<CC>, XX> @Nullable OrderedPair<ImmutableList<XX>, CC> toVertexSequence(@Nullable ArcBackLink<VV, AA, CC> node,
                                                                                                                                 @NonNull Function<ArcBackLink<VV, AA, CC>, XX> mappingFunction) {
        if (node == null) {
            return null;
        }
        //
        ArrayDeque<XX> deque = new ArrayDeque<>();
        for (ArcBackLink<VV, AA, CC> parent = node; parent != null; parent = parent.getParent()) {
            deque.addFirst(mappingFunction.apply(parent));
        }
        return new OrderedPair<>(ImmutableLists.copyOf(deque), node.getCost());
    }

    public static <VV, AA, CC extends Number & Comparable<CC>, XX> @Nullable OrderedPair<ImmutableList<XX>, CC> toArrowSequence(@Nullable ArcBackLink<VV, AA, CC> node,
                                                                                                                                @NonNull BiFunction<ArcBackLink<VV, AA, CC>, ArcBackLink<VV, AA, CC>, XX> mappingFunction) {
        if (node == null) {
            return null;
        }
        //
        ArrayDeque<XX> deque = new ArrayDeque<>();
        ArcBackLink<VV, AA, CC> prev = node;
        for (ArcBackLink<VV, AA, CC> parent = node.getParent(); parent != null; parent = parent.getParent()) {
            deque.addFirst(mappingFunction.apply(parent, prev));
            prev = parent;
        }
        return new OrderedPair<>(ImmutableLists.copyOf(deque), node.getCost());
    }

    public @Nullable A getArrow() {
        return arrow;
    }

    public @NonNull V getVertex() {
        return vertex;
    }

    @Override
    public String toString() {
        return "ArcBackLink{" +
                "depth=" + depth +
                ", vertex=" + vertex +
                ", arrow=" + arrow +
                '}';
    }
}
