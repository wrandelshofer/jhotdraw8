package org.jhotdraw8.icollection;

import org.jspecify.annotations.Nullable;

/**
 * This record holds an object that contains private data.
 * <p>
 * This record is used to allow subclassing of persistent classes
 * that need to create new instances of subclasses,
 * but that do not want to export their internal data structures.
 * <p>
 * <b>Example:</b>
 * <p>
 * The module 'foo' exports the package that contains class
 * PersistentFoo.
 * The module 'foo' does not export the package that contains InternalHashtable
 * used by PersistentFoo.
 * <p>
 * To allow subclassing of PersistentFoo by other modules,
 * class PersistentFoo provides a protected constructor and
 * a protected {@code newInstance} method that take an PrivateData object
 * as parameters.
 * <pre>{@literal
 * module org.foo {
 *     exports org.foo;
 * }
 * package org.foo.impl;

import java.util.logging.Level;
import java.util.logging.Logger;
 * public class InternalHashtable {
 *    ...
 * }
 * package org.foo;

import java.util.logging.Level;
import java.util.logging.Logger;
 * public class PersistentFoo {
 *     private final InternalHashtable table;
 *     public PersistentFoo() {
 *        table = new InternalHashtable();
 *     }
 *     protected PersistentFoo(PrivateData opaque) {
 *         this.table = opaque.get();
 *     }
 *     protected PersistentFoo newInstance(PrivateData opaque) {
 *         return new PersistentFoo(opaque);
 *     }
 * }
 *
 *
 * module org.bar {
 *     imports org.foo;
 *     exports org.bar;
 * }
 * package org.bar;

import java.util.logging.Level;
import java.util.logging.Logger;
 * public class PersistentBar extends PersistentFoo {
 *     private final int bar;
 *     public PersistentBar(int bar) {
 *         this.bar=bar;
 *     }
 *     protected PersistentBar(PrivateData opaque, int bar) {
 *         super(opaque);
 *         this.bar=bar;
 *     }
 *     protected newInstance(PrivateData opaque) {
 *         return new PersistentBar(opaque,this.bar);
 *     }
 * }
 * }</pre>
 */
public record PrivateData(@Nullable Object object) {
    @SuppressWarnings("unchecked")
    <O> @Nullable O get() {
        return (O) object;
    }
}
