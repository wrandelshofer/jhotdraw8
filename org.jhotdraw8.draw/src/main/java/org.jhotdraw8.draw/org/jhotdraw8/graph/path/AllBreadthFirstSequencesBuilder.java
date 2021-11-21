package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.AbstractEnumeratorSpliterator;
import org.jhotdraw8.collection.ImmutableList;
import org.jhotdraw8.collection.OrderedPair;
import org.jhotdraw8.collection.SpliteratorIterable;
import org.jhotdraw8.graph.Arc;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Function;
import java.util.function.Predicate;

public class AllBreadthFirstSequencesBuilder<V, A> {
    private final @NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction;

    public AllBreadthFirstSequencesBuilder(@NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction) {
        this.nextArcsFunction = nextArcsFunction;
    }

    /**
     * Lazily Enumerates all vertex sequences from start to goal up to the specified
     * maximal path length.
     *
     * @param start the start vertex
     * @param goal  the goal predicate
     * @return the enumerated sequences
     */
    public @NonNull Iterable<OrderedPair<ImmutableList<Arc<V, A>>, Integer>> findAllArcSequences(@NonNull V start,
                                                                                                 @NonNull Predicate<V> goal,
                                                                                                 int maxLength) {

        return new SpliteratorIterable<>(() -> new AllPathsSpliterator<>(
                start, goal, nextArcsFunction,
                (backLink) -> AbstractCostAndBackLinksSequenceBuilder.toArrowSequence(backLink, (a, b) -> new Arc<>(a.getVertex(), b.getVertex(), b.getArrow())),
                maxLength));
    }

    /**
     * Lazily Enumerates all vertex sequences from start to goal up to the specified
     * maximal path length.
     *
     * @param start the start vertex
     * @param goal  the goal predicate
     * @return the enumerated sequences
     */
    public @NonNull Iterable<OrderedPair<ImmutableList<A>, Integer>> findAllArrowSequences(@NonNull V start,
                                                                                           @NonNull Predicate<V> goal,
                                                                                           int maxLength) {

        return new SpliteratorIterable<>(() -> new AllPathsSpliterator<>(
                start, goal, nextArcsFunction,
                (backLink) -> AbstractCostAndBackLinksSequenceBuilder.toArrowSequence(backLink, (a, b) -> b.getArrow()),
                maxLength));
    }

    /**
     * Lazily Enumerates all vertex sequences from start to goal up to the specified
     * maximal path length.
     *
     * @param start the start vertex
     * @param goal  the goal predicate
     * @return the enumerated sequences
     */
    public @NonNull Iterable<OrderedPair<ImmutableList<V>, Integer>> findAllVertexSequences(@NonNull V start,
                                                                                            @NonNull Predicate<V> goal,
                                                                                            @NonNull int maxLength) {

        return new SpliteratorIterable<>(() -> new AllPathsSpliterator<>(start, goal, nextArcsFunction,
                (backLink) -> AbstractCostAndBackLinksSequenceBuilder.toVertexSequence(backLink, AbstractCostAndBackLinksSequenceBuilder.BackLink::getVertex),
                maxLength));
    }


    private static class AllPathsSpliterator<V, A, X> extends AbstractEnumeratorSpliterator<OrderedPair<ImmutableList<X>, Integer>> {
        private final @NonNull Deque<AbstractCostAndBackLinksSequenceBuilder.BackLink<V, A, Integer>> stack = new ArrayDeque<>();
        private final @NonNull Predicate<V> goal;
        private final int maxLength;
        private final @NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction;
        private final @NonNull Function<AbstractCostAndBackLinksSequenceBuilder.BackLink<V, A, Integer>,
                OrderedPair<ImmutableList<X>, Integer>> sequenceFunction;

        public AllPathsSpliterator(@NonNull V start,
                                   @NonNull Predicate<V> goal,
                                   @NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
                                   @NonNull Function<AbstractCostAndBackLinksSequenceBuilder.BackLink<V, A, Integer>,
                                           OrderedPair<ImmutableList<X>, Integer>> sequenceFunction,
                                   int maxLength) {
            super(Long.MAX_VALUE, 0);
            this.maxLength = maxLength;
            stack.push(new AbstractCostAndBackLinksSequenceBuilder.BackLink<>(start, null, null, 1));
            this.goal = goal;
            this.nextArcsFunction = nextArcsFunction;
            this.sequenceFunction = sequenceFunction;
        }

        @Override
        public boolean moveNext() {
            while (!stack.isEmpty()) {
                AbstractCostAndBackLinksSequenceBuilder.BackLink<V, A, Integer> popped = stack.pop();
                if (goal.test(popped.getVertex())) {
                    this.current = sequenceFunction.apply(popped);
                    return true;
                }
                if (popped.getCost() < maxLength) {
                    for (Arc<V, A> v : nextArcsFunction.apply(popped.getVertex())) {
                        AbstractCostAndBackLinksSequenceBuilder.BackLink<V, A, Integer> newNode = new AbstractCostAndBackLinksSequenceBuilder.BackLink<>(v.getEnd(), v.getData(), popped, popped.getCost() + 1);
                        stack.push(newNode);
                    }
                }
            }
            return false;
        }
    }


}
