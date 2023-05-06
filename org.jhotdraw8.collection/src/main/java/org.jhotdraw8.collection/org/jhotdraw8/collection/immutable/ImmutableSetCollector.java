package org.jhotdraw8.collection.immutable;

import java.util.Set;
import java.util.Stack;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * Recursively builds a larger set from smaller instances of the set.
 * <p>
 * Use this collector for sets that can perform {@link ImmutableSet#addAll(Iterable)}
 * faster than {@link ImmutableSet#add}.
 * <p>
 * Usage with {@link Stream}:
 * <pre>{@literal
 *     Stream<E> stream = ...
 *     ChampSet result = stream.collect(new ImmutableSetCollector<>(ChampSet.of());
 * }</pre>
 * Usage with enhanced for-loop:
 * <pre>{@literal
 *     var c = new ImmutableSetCollector<E,ChampSet<E>>(ChampSet.of();
 *     for (E e : ...) {
 *      c.accept(e);
 *     }
 *     ChampSet result = c.build();
 * }</pre>
 *
 * @param <T> the element type
 */
public class ImmutableSetCollector<T, S extends ImmutableSet<T>> implements Consumer<T>,
        Collector<T, ImmutableSetCollector<T, S>, S> {
    private final Stack<ImmutableSet<T>> stack = new Stack<ImmutableSet<T>>();
    private ImmutableSet<T> s;
    private final Set<Characteristics> characteristics;

    public ImmutableSetCollector(ImmutableSet<T> s) {
        this.s = s;
        characteristics = s instanceof ImmutableSequencedSet<T>
                ? Set.of()
                : Set.of(Characteristics.UNORDERED);
    }

    @Override
    public void accept(T t) {
        s = s.add(t);
        if (s.size() == 32) {
            if (!stack.isEmpty()) {
                do {
                    s = stack.pop().addAll(s);
                } while (!stack.isEmpty() && stack.peek().size() <= s.size());
            }
            stack.push(s);
            s = s.clear();
        }
    }

    @SuppressWarnings("unchecked")
    public S build() {
        while (!stack.isEmpty()) {
            s = stack.pop().addAll(s);
        }
        return (S) s;
    }

    @Override
    public Supplier<ImmutableSetCollector<T, S>> supplier() {
        return () -> new ImmutableSetCollector<>(s.clear());
    }

    @Override
    public BiConsumer<ImmutableSetCollector<T, S>, T> accumulator() {
        return ImmutableSetCollector::accept;
    }

    @Override
    public BinaryOperator<ImmutableSetCollector<T, S>> combiner() {
        return (a, b) -> new ImmutableSetCollector<>(a.build().addAll(b.build()));
    }

    @Override
    public Function<ImmutableSetCollector<T, S>, S> finisher() {
        return ImmutableSetCollector::build;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return characteristics;
    }
}
