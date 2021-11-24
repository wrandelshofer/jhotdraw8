package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.AbstractEnumeratorSpliterator;
import org.jhotdraw8.collection.ImmutableList;
import org.jhotdraw8.collection.OrderedPair;
import org.jhotdraw8.graph.Arc;
import org.jhotdraw8.util.TriFunction;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * This spliterator iterates over all paths from a given set of start vertices
 * to a set of goal vertices.
 *
 * @param <V> the vertex data type
 * @param <A> the arrow data type
 * @param <C> the cost number type
 * @param <E> the element type of the path
 */
class AllPathsSpliterator<V, A, C extends Number & Comparable<C>, E> extends AbstractEnumeratorSpliterator<OrderedPair<ImmutableList<E>, C>> {
    private final @NonNull Queue<ArcBackLink<V, A, C>> queue = new ArrayDeque<>();
    private final @NonNull Predicate<V> goal;
    private final @NonNull C maxCost;
    private final @NonNull C zero;
    private final @NonNull TriFunction<V, V, A, C> costFunction;
    private final @NonNull BiFunction<C, C, C> sumFunction;
    private final @NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction;
    private final @NonNull Function<ArcBackLink<V, A, C>,
            OrderedPair<ImmutableList<E>, C>> sequenceFunction;

    public AllPathsSpliterator(@NonNull Iterable<V> startVertices,
                               @NonNull Predicate<V> goal,
                               @NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
                               @NonNull Function<ArcBackLink<V, A, C>,
                                       OrderedPair<ImmutableList<E>, C>> sequenceFunction,
                               @NonNull C maxCost,
                               @NonNull C zero,
                               @NonNull TriFunction<V, V, A, C> costFunction,
                               @NonNull BiFunction<C, C, C> sumFunction) {
        super(Long.MAX_VALUE, 0);
        this.maxCost = maxCost;
        this.goal = goal;
        this.nextArcsFunction = nextArcsFunction;
        this.sequenceFunction = sequenceFunction;

        this.zero = zero;
        this.costFunction = costFunction;
        this.sumFunction = sumFunction;
        for (V start : startVertices) {
            queue.add(new ArcBackLink<>(start, null, null, zero));
        }

    }

    @Override
    public boolean moveNext() {
        while (!queue.isEmpty()) {
            ArcBackLink<V, A, C> polled = queue.remove();
            if (goal.test(polled.getVertex())) {
                this.current = sequenceFunction.apply(polled);
                return true;
            }
            for (Arc<V, A> arc : nextArcsFunction.apply(polled.getVertex())) {
                C cost = sumFunction.apply(polled.getCost(), costFunction.apply(polled.getVertex(), arc.getEnd(), arc.getData()));
                if (cost.compareTo(maxCost) <= 0) {
                    ArcBackLink<V, A, C> newNode = new ArcBackLink<>(arc.getEnd(), arc.getData(), polled, cost);
                    queue.add(newNode);
                }
            }
        }
        return false;
    }
}
