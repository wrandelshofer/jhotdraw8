package org.jhotdraw8.icollection;

/**
 * This record holds an opaque object.
 * <p>
 * This record is used to allow subclassing of immutable classes
 * that need to create new instances of subclasses,
 * but that do not want to export their internal data structures.
 * <p>
 * <b>Example:</b>
 * <p>
 * The module 'foo' exports the package that contains class
 * ImmutableFoo.
 * The module 'foo' does not export the package that contains InternalHashtable
 * used by ImmutableFoo.
 * <p>
 * To allow subclassing of ImmutableFoo by other modules,
 * class ImmutableFoo provides a protected constructor and
 * a protected {@code newInstance} method that take an Opaque object
 * as parameters.
 * <pre>{@literal
 * module org.foo {
 *     exports org.foo;
 * }
 * package org.foo.impl;
 * public class InternalHashtable {
 *    ...
 * }
 * package org.foo;
 * public class ImmutableFoo {
 *     private final @NonNull InternalHashtable table;
 *     public ImmutableFoo() {
 *        table = new InternalHashtable();
 *     }
 *     protected ImmutableFoo(Opaque opaque) {
 *         this.table = opaque.get();
 *     }
 *     protected ImmutableFoo newInstance(Opaque opaque) {
 *         return new ImmutableFoo(opaque);
 *     }
 * }
 *
 *
 * module org.bar {
 *     imports org.foo;
 *     exports org.bar;
 * }
 * package org.bar;
 * public class ImmutableBar extends ImmutableFoo {
 *     private final int bar;
 *     public ImmutableBar(int bar) {
 *         this.bar=bar;
 *     }
 *     protected ImmutableBar(Opaque opaque, int bar) {
 *         super(opaque);
 *         this.bar=bar;
 *     }
 *     protected newInstance(Opaque opaque) {
 *         return new ImmutableBar(opaque,this.bar);
 *     }
 * }
 * }</pre>
 */
public record Opaque(Object object) {
    @SuppressWarnings("unchecked")
    <O> O get() {
        return (O) object;
    }
}