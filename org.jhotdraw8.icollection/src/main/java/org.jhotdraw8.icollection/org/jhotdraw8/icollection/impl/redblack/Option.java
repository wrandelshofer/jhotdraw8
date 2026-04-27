/*
 * @(#)Option.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection.impl.redblack;

import org.jhotdraw8.icollection.readable.ReadableCollection;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

/// An Option is a read-only collection of one element or of zero elements.
///
/// Instances of Option are either an instance of [Some] or of [None].
///
/// This class has been derived from 'vavr' Option.java.
///
/// [vavr Option.java](https://github.com/vavr-io/vavr/blob/26181f14b9629ceb729a73795d3854363c7dce0e/src/main/java/io/vavr/control/Option.java)
/// [vavr MIT-License](https://github.com/vavr-io/vavr/blob/26181f14b9629ceb729a73795d3854363c7dce0e/LICENSE)
///
///
/// @param <T> The type of the optional value.
public interface Option<T> extends ReadableCollection<T> {
    @SuppressWarnings("unchecked")
    static <T> Option<T> none() {
        return (Option<T>) None.INSTANCE;
    }

    static <T> Option<T> some(T value) {
        return new Some<>(value);
    }


    /// Creates a new `Option` of a given value.
    /// <pre>
    /// `// = Some(3), an Option which contains the value 3Option<Integer> option = Option.of(3);// = None, the empty OptionOption<Integer> none = Option.of(null);`</pre>
    ///
    /// @param value A value
    /// @param <T>   type of the value
    /// @return `Some(value)` if value is not `null`, `None` otherwise
    static <T> Option<T> of(T value) {
        return (value == null) ? none() : some(value);
    }
    record Some<T>(T value) implements Option<T> {

        @Override
        public int size() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean contains(Object o) {
            return Objects.equals(value, o);
        }

        @Override
        public Iterator<T> iterator() {
            return Set.of(value).iterator();
        }

        @Override
        public Option<T> orElse(Option<? extends T> other) {
            return this;
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public T orNull() {
            return value;
        }

        @Override
        public T orThrow() {
            return value;
        }
    }

    record None<T>() implements Option<T> {
        /// The singleton instance of None.
        private static final None<?> INSTANCE = new None<>();

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean contains(Object o) {
            return false;
        }

        @Override
        public Iterator<T> iterator() {
            return Collections.emptyIterator();
        }

        @SuppressWarnings("unchecked")
        @Override
        public Option<T> orElse(Option<? extends T> other) {
            return (Option<T>) other;
        }

        @Override
        public T get() {
            throw new NoSuchElementException();
        }

        @Override
        public @Nullable T orNull() {
            return null;
        }

        @Override
        public T orThrow() {
            throw new NoSuchElementException();
        }
    }

    /// Returns true, if this is `None`, otherwise false, if this is `Some`.
    /// <pre>
    /// `// Prints "false"System.out.println(Option.of(10).isEmpty());// Prints "true"System.out.println(Option.none().isEmpty());`</pre>
    ///
    /// @return true, if this `Option` is empty, false otherwise
    boolean isEmpty();

    /// Returns this `Option` if it is nonempty, otherwise return the alternative.
    /// <pre>
    /// `Option<String> other = Option.of("Other");// = Some("Hello World")Option.of("Hello World").orElse(other);// = Some("Other")Option.none().orElse(other);`</pre>
    ///
    /// @param other An alternative `Option`
    /// @return this `Option` if it is nonempty, otherwise return the alternative.
    @SuppressWarnings("unchecked")
    Option<T> orElse(Option<? extends T> other);

    /// Returns the value if this is a `Some` or the `other` value if this is a `None`.
    ///
    /// Please note, that the other value is eagerly evaluated.
    /// <pre>
    /// `// Prints "Hello"System.out.println(Option.of("Hello").getOrElse("World"));// Prints "World"Option.none().getOrElse("World");`</pre>
    ///
    /// @param other An alternative value
    /// @return This value, if this Option is defined or the `other` value, if this Option is empty.
    default @Nullable T getOrElse(@Nullable T other) {
        return isEmpty() ? other : get();
    }

    /// Gets the value if this is a `Some` or throws if this is a `None`.
    /// <pre>
    /// `// Prints "57"System.out.println(Option.of(57).get());// Throws a NoSuchElementExceptionOption.none().get();`</pre>
    ///
    /// @return the value
    /// @throws NoSuchElementException if this is a `None`.
    @Nullable T get();

    /// Returns this `Option` if this is defined, or `null` if it is empty.
    /// <pre>
    /// `// = Some("Hello World")Option.of("Hello World").orNull();// = nullOption.none().orNull();`</pre>
    ///
    /// @return this value if it is defined, or `null` if it is empty.
    @Nullable T orNull();

    /// Returns this `Option` if this is defined, or throws a `NoSuchElementException` if it is empty.
    /// <pre>
    /// `// = Some("Hello World")Option.of("Hello World").orThrow();// = nullOption.none().orThrow();`</pre>
    ///
    /// @return this value if it is defined, or `null` if it is empty.
    @Nullable T orThrow();

}
