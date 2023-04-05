/*
 * @(#)CustomBinding.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxbase.binding;

import javafx.beans.binding.Binding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WritableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.util.StringConverter;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Provides bindings with conversion functions.
 * <p>
 * Slightly adapted code from example shown at stackoverflow.com.
 * <p>
 * Reference:
 * <dl>
 *     <dt>Stackoverflow. Custom Bidirectional Bindings in JavaFX,
 *     Copyright BlackLabrador, CC BY-SA 4.0 license</dt>
 *     <dd><a href="http://stackoverflow.com/questions/27052927/custom-bidirectional-bindings-in-javafx">
 *   stackoverflow.com</a></dd>
 * </dl>
 *
 * @author BlackLabrador (as stated in the reference)
 */
public class CustomBinding {

    /**
     * Don't let anyone instantiate this class.
     */
    private CustomBinding() {
    }

    /**
     * Binds property 'a' to property 'b'. Property b is provided by 'mediator'.
     * <p>
     * This method keeps a strong reference to {@code propertyB}.
     *
     * @param <T>       the type of properties 'a' and 'b'
     * @param <M>       the type of the mediator property
     * @param propertyA property 'a'
     * @param mediator  the mediator property 'm'
     * @param propertyB property 'b', this can be a new instance on each call
     */
    public static <T, M> void bindBidirectionalStrongly(
            @NonNull Property<T> propertyA, @NonNull Property<M> mediator, @NonNull Function<M, Property<T>> propertyB) {

        final ChangeListener<M> changeListener = new ChangeListener<M>() {
            private Property<T> strongReference;

            @Override
            public void changed(ObservableValue<? extends M> o, M oldv, M newv) {
                if (oldv != null && strongReference != null) {
                    propertyA.unbindBidirectional(strongReference);
                    strongReference = null;
                }
                if (newv != null) {
                    strongReference = propertyB.apply(newv);
                    propertyA.bindBidirectional(strongReference);
                }
            }
        };
        changeListener.changed(mediator, null, mediator.getValue());
        mediator.addListener(changeListener);
    }

    public static <T, M> void bindBidirectionalStrongly2(
            @NonNull Property<T> propertyA, @NonNull Property<M> mediator, @NonNull Function<M, Property<T>> propertyB) {

        final ChangeListener<M> changeListener = new ChangeListener<M>() {
            private Property<T> strongReference;

            @Override
            public void changed(ObservableValue<? extends M> o, M oldv, M newv) {
                if (oldv != null && strongReference != null) {
                    StrongBidirectionalBinding.unbind(propertyA, strongReference);
                    strongReference = null;
                }
                if (newv != null) {
                    strongReference = propertyB.apply(newv);
                    StrongBidirectionalBinding.bind(propertyA, strongReference);
                }
            }
        };
        changeListener.changed(mediator, null, mediator.getValue());
        mediator.addListener(changeListener);
    }

    /**
     * Binds property 'a' to property 'b'. Property b is provided by 'mediator'.
     *
     * @param <T>       the type of properties 'a' and 'b'
     * @param <M>       the type of the mediator property
     * @param propertyA property 'a'
     * @param mediator  the mediator property 'm'
     * @param propertyB property 'b', must return the same instance each time
     *                  when called for the same object 'm'.
     */
    public static <T, M> void bindBidirectional(
            @NonNull Property<T> propertyA, @NonNull Property<M> mediator, @NonNull Function<M, Property<T>> propertyB) {

        final ChangeListener<M> changeListener = new ChangeListener<M>() {
            @Override
            public void changed(ObservableValue<? extends M> o, M oldv, M newv) {
                if (oldv != null) {
                    propertyA.unbindBidirectional(propertyB.apply(oldv));
                }
                if (newv != null) {
                    propertyA.bindBidirectional(propertyB.apply(newv));
                }
            }
        };
        changeListener.changed(mediator, null, mediator.getValue());
        mediator.addListener(changeListener);
    }

    /**
     * Binds property 'a' to property 'b'. Property 'b' is provided by 'mediatorB'.
     * <p>
     * When 'a' is unbound from 'b', its value is set to the value that
     * 'a' had, when this method was invoked.
     *
     * @param <T>       the type of properties 'a' and 'b'
     * @param <M>       the type of the mediator property
     * @param propertyA property 'a'
     * @param mediatorB the mediator property
     * @param propertyB property 'b'
     */
    public static <T, M> void bind(
            @NonNull Property<T> propertyA, @NonNull Property<M> mediatorB, @NonNull Function<M, ObservableValue<T>> propertyB) {
        bind(propertyA, mediatorB, propertyB, propertyA.getValue());
    }

    /**
     * Binds property 'a' to property 'b'. Property 'b' is provided by 'mediatorB'.
     * <p>
     * When 'a' is unbound from 'b', its value is set to 'unboundValue'.
     *
     * @param <T>          the type of properties 'a' and 'b'
     * @param <M>          the type of the mediator property
     * @param propertyA    property 'a'
     * @param mediatorB    the mediator property
     * @param propertyB    property 'b'
     * @param unboundValue the value to be set on 'a' when 'a' is unbound from 'b'.
     */
    public static <T, M> void bind(
            @NonNull Property<T> propertyA, @NonNull Property<M> mediatorB, @NonNull Function<M, ObservableValue<T>> propertyB, T unboundValue) {

        final ChangeListener<M> changeListener = (o, oldv, newv) -> {
            if (oldv != null) {
                propertyA.unbind();
                propertyA.setValue(unboundValue);
            }
            if (newv != null) {
                propertyA.bind(propertyB.apply(newv));
            }
        };
        changeListener.changed(mediatorB, null, mediatorB.getValue());
        mediatorB.addListener(changeListener);
    }

    /**
     * Binds property 'a' to property 'b'. Property 'a' is provided by 'mediatorA'.
     * <p>
     * When 'a' is unbound from 'b', its value is set to 'null'.
     *
     * @param <T>       the type of properties 'a' and 'b'
     * @param <M>       the type of the mediator property
     * @param propertyA property 'a'
     * @param mediatorA the mediator property
     * @param propertyB property 'b'
     */
    public static <T, M> void bind(@NonNull ObservableValue<M> mediatorA,
                                   @NonNull Function<M, Property<T>> propertyA, @NonNull ObservableValue<T> propertyB) {
        bind(mediatorA, propertyA, propertyB, null);
    }

    /**
     * Binds property 'a' to property 'b'. Property 'a' is provided by 'mediatorA'.
     * <p>
     * When 'a' is unbound from 'b', its value is set to 'unboundValue'.
     *
     * @param <T>          the type of properties 'a' and 'b'
     * @param <M>          the type of the mediator property
     * @param propertyA    property 'a'
     * @param mediatorA    the mediator property
     * @param propertyB    property 'b'
     * @param unboundValue the value to be set on 'a' when 'a' is unbound from 'b'.
     */
    public static <T, M> void bind(@NonNull ObservableValue<M> mediatorA,
                                   @NonNull Function<M, Property<T>> propertyA, @NonNull ObservableValue<T> propertyB, T unboundValue) {

        final ChangeListener<M> changeListener = (o, oldv, newv) -> {
            if (oldv != null) {
                Property<T> mediatedA = propertyA.apply(oldv);
                mediatedA.unbind();
                mediatedA.setValue(unboundValue);
            }
            if (newv != null) {
                propertyA.apply(newv).bind(propertyB);
            }
        };
        changeListener.changed(mediatorA, null, mediatorA.getValue());
        mediatorA.addListener(changeListener);
    }

    /**
     * Binds property 'a' to property 'b'. Property b is provided by 'mediator'.
     *
     * @param <T>             the value type of property 'b'
     * @param <S>             the value type of the mediator property
     * @param propertyA       property 'a'
     * @param mediator        the mediator property
     * @param propertyB       property 'b'
     * @param stringConverter the converter
     */
    public static <T, S> void bindBidirectional(@NonNull StringProperty propertyA, @NonNull Property<S> mediator, @NonNull Function<S, Property<T>> propertyB,
                                                @NonNull StringConverter<T> stringConverter) {
        final ChangeListener<S> changeListener = (o, oldv, newv) -> {
            if (oldv != null) {
                propertyA.unbindBidirectional(propertyB.apply(oldv));
            }
            if (newv != null) {
                propertyA.bindBidirectional(propertyB.apply(newv), stringConverter);
            }
        };
        changeListener.changed(mediator, null, null);
        mediator.addListener(changeListener);
    }


    /**
     * Creates a bidirectional binding for properties A and B using the
     * provided conversion functions.
     *
     * @param <A>          the type of value A
     * @param <B>          the type of value B
     * @param <PROPERTY_A> the type of property A
     * @param <PROPERTY_B> the type of property B
     * @param propertyA    property A
     * @param propertyB    property B
     * @param convertAtoB  converts a value from A to B
     * @param convertBtoA  converts a value from B to A
     */
    public static <A, B, PROPERTY_A extends WritableValue<A> & ObservableValue<A>, PROPERTY_B extends WritableValue<B> & ObservableValue<B>>
    void bindBidirectionalAndConvert(@NonNull PROPERTY_A propertyA, @NonNull PROPERTY_B propertyB, @NonNull Function<A, B> convertAtoB, @NonNull Function<B, A> convertBtoA) {
        boolean[] alreadyCalled = new boolean[1];
        propertyB.setValue(convertAtoB.apply(propertyA.getValue()));
        addFlaggedChangeListener(propertyB, propertyA, convertAtoB, alreadyCalled);
        addFlaggedChangeListener(propertyA, propertyB, convertBtoA, alreadyCalled);
    }


    private static <Y, X> void addFlaggedChangeListener(@NonNull WritableValue<X> propertyX, @NonNull ObservableValue<Y> propertyY, @NonNull Function<Y, X> updateX,
                                                        boolean[] alreadyCalled) {
        propertyY.addListener((observable, oldValue, newValue) -> {
                    if (!alreadyCalled[0]) {
                        try {
                            alreadyCalled[0] = true;
                            propertyX.setValue(updateX.apply(newValue));
                        } finally {
                            alreadyCalled[0] = false;
                        }
                    }
                }
        );
    }

    /**
     * Returns a string expression which uses {@code java.test.MessageFormat} to
     * format the text. See {@link MessageStringFormatter} for special treatment
     * of boolean values.
     *
     * @param format The format string.
     * @param args   The arguments.
     * @return The string expression
     */
    public static @NonNull StringExpression formatted(String format, Object... args) {
        return MessageStringFormatter.format(format, args);
    }

    /**
     * Binds list dest to list source.
     *
     * @param dest   list dest
     * @param src    list source
     * @param toDest mapping function to dest
     * @param <D>    the type of list dest
     * @param <S>    the type of list source
     */
    public static <D, S> void bindContent(@NonNull ObservableList<D> dest, @NonNull ObservableList<S> src, @NonNull Function<S, D> toDest) {
        bindContent(dest, src, toDest, null);
    }

    /**
     * Binds list dest to list source.
     *
     * @param dest         list dest
     * @param src          list source
     * @param toDest       mapping function to dest
     * @param destOnRemove this consumer is called when an element is removed from the dest list
     * @param <D>          the type of list dest
     * @param <S>          the type of list source
     */
    public static <D, S> void bindContent(@NonNull ObservableList<D> dest, @NonNull ObservableList<S> src, @NonNull Function<S, D> toDest, @Nullable Consumer<D> destOnRemove) {
        ListTransformContentBinding<D, S> binding = new ListTransformContentBinding<>(dest, src, toDest, null, destOnRemove, null);
        src.addListener(binding.getSourceChangeListener());
    }

    /**
     * Binds list dest to list source bidirectionally.
     *
     * @param dest         list dest
     * @param src          list source
     * @param toDest       mapping function to dest
     * @param destOnRemove this consumer is called when an element is removed from the dest list
     * @param <D>          the type of list dest
     * @param <S>          the type of list source
     */
    public static <D, S> void bindContentBidirectional(
            @NonNull ObservableList<D> dest, @NonNull ObservableList<S> src,
            @NonNull Function<S, D> toDest, @Nullable Consumer<D> destOnRemove,
            @NonNull Function<D, S> toSource, @Nullable Consumer<S> sourceOnRemove) {
        ListTransformContentBinding<D, S> binding = new ListTransformContentBinding<>(dest, src, toDest, toSource, destOnRemove, sourceOnRemove);
        src.addListener(binding.getSourceChangeListener());
        dest.addListener(binding.getDestChangeListener());
    }

    /**
     * Binds list dest to set source.
     *
     * @param dest   list dest
     * @param src    list source
     * @param toDest mapping function to dest
     * @param <D>    the type of list dest
     * @param <S>    the type of list source
     */
    public static <D, S> void bindListContentToSet(ObservableList<D> dest, ObservableSet<S> src, Function<S, D> toDest) {
        ListToSetTransformContentBinding<D, S> binding = new ListToSetTransformContentBinding<>(dest, src, toDest);
        src.addListener(binding);
    }

    public static <D, S> void bindListContentToSet(ObservableList<D> dest, ObservableSet<S> src, Function<S, D> toDest, Consumer<D> disposeDest) {
        ListToSetTransformContentBinding<D, S> binding = new ListToSetTransformContentBinding<>(dest, src, toDest, disposeDest);
        src.addListener(binding);
    }

    /**
     * Unbinds list dest from set source.
     *
     * @param dest   list dest
     * @param src    list source
     * @param <D>    the type of list dest
     * @param <S>    the type of list source
     */
    public static <D, S> void unbindListContentToSet(ObservableList<D> dest, ObservableSet<S> src) {
        ListToSetTransformContentBinding<D, S> binding = new ListToSetTransformContentBinding<>(dest, src, null);
        src.removeListener(binding);
    }

    /**
     * Binds the specified property of all list elements to the given property.
     * <p>
     * If an element is added, its property is bound.
     * <p>
     * If an element is removed, its property is unbound and the property value
     * is set to null.
     *
     * @param list     the list
     * @param getter   the getter for the element property
     * @param property the property to which the element properties shall be bound
     * @param <E>      the element type
     * @param <T>      the property type
     */
    public static <E, T> void bindElements(ObservableList<E> list, Function<E, Property<T>> getter, Property<T> property) {
        bindElements(list, getter, property, null);
    }

    /**
     * Binds the specified property of all list elements to the given property.
     * <p>
     * If an element is added, its property is bound.
     * <p>
     * If an element is removed, its property is unbound and the property value
     * is set to {@code unboundValue}.
     *
     * @param list         the list
     * @param getter       the getter for the element property
     * @param property     the property to which the element properties shall be bound
     * @param unboundValue the value to that is set when the property is unbound.
     * @param <E>          the element type
     * @param <T>          the property type
     */
    public static <E, T> void bindElements(ObservableList<E> list, Function<E, Property<T>> getter, Property<T> property, T unboundValue) {
        for (E elem : list) {
            Property<T> p = getter.apply(elem);
            p.unbind();
            p.bind(property);
        }
        list.addListener((ListChangeListener.Change<? extends E> change) -> {
            while (change.next()) {
                for (E removed : change.getRemoved()) {
                    Property<T> p = getter.apply(removed);
                    p.unbind();
                    p.setValue(unboundValue);
                }
                for (E added : change.getAddedSubList()) {
                    Property<T> p = getter.apply(added);
                    p.unbind();
                    p.bind(property);
                }
            }
        });
    }

    /**
     * Binds the specified property of all list elements to the given binding.
     * <p>
     * If an element is added, its property is bound.
     * <p>
     * If an element is removed, its property is unbound and the property value
     * is set to null.
     *
     * @param list    the list
     * @param getter  the getter for the element property
     * @param binding the binding to which the element properties shall be bound
     * @param <E>     the element type
     * @param <T>     the property type
     */
    public static <E, T> void bindElements(ObservableList<E> list, Function<E, Property<T>> getter, Binding<T> binding) {
        bindElements(list, getter, binding, null);
    }

    /**
     * Binds the specified property of all list elements to the given binding.
     * <p>
     * If an element is added, its property is bound.
     * <p>
     * If an element is removed, its property is unbound and the property value
     * is set to {@code unboundValue}.
     *
     * @param list    the list
     * @param getter  the getter for the element property
     * @param binding the binding to which the element properties shall be bound
     * @param <E>     the element type
     * @param <T>     the property type
     */
    public static <E, T> void bindElements(ObservableList<E> list, Function<E, Property<T>> getter, Binding<T> binding, T unboundValue) {
        for (E elem : list) {
            Property<T> p = getter.apply(elem);
            p.unbind();
            p.bind(binding);
        }
        list.addListener((ListChangeListener.Change<? extends E> change) -> {
            while (change.next()) {
                for (E removed : change.getRemoved()) {
                    Property<T> p = getter.apply(removed);
                    p.unbind();
                    p.setValue(unboundValue);
                }
                for (E added : change.getAddedSubList()) {
                    Property<T> p = getter.apply(added);
                    p.unbind();
                    p.bind(binding);
                }
            }
        });
    }

    /**
     * Sets the specified value to all elements of the list.
     * <p>
     * If an element is added, the specified value is set.
     * <p>
     * If an element is removed, the null value is set.
     *
     * @param list   the list
     * @param setter the setter for the value on the element
     * @param value  the value
     * @param <E>    the element type
     * @param <T>    the value type
     */
    public static <E, T> void bindElements(ObservableList<E> list, BiConsumer<E, T> setter, T value) {
        for (E elem : list) {
            setter.accept(elem, value);
        }
        list.addListener((ListChangeListener.Change<? extends E> change) -> {
            while (change.next()) {
                for (E removed : change.getRemoved()) {
                    setter.accept(removed, null);
                }
                for (E added : change.getAddedSubList()) {
                    setter.accept(added, value);
                }
            }
        });
    }

    /**
     * Adds or removes an element from a set depending on the bound boolean value.
     *
     * @param set     a set
     * @param element an element that is added on true and removed on false
     * @param value   the boolean value
     * @param <E>     the element type
     */
    public static <E> void bindMembershipToBoolean(ObservableSet<E> set, E element, ObservableValue<Boolean> value) {
        ChangeListener<Boolean> changeListener = (o, oldv, newv) -> {
            if (newv) {
                set.add(element);
            } else {
                set.remove(element);
            }
        };
        value.addListener(changeListener);
        changeListener.changed(value, !value.getValue(), value.getValue());
    }

    /**
     * Creates a binding with a computed value.
     * <p>
     * If the value of one of the dependencies changes, the binding is marked as
     * invalid.
     *
     * @param op           the operation that computes the value
     * @param dependendies the depencies that invalidate the computed value
     * @return a new binding
     */
    public static DoubleBinding computeDouble(DoubleSupplier op, ObservableValue<?>... dependendies) {
        return new DoubleBinding() {
            {
                super.bind(dependendies);
            }

            @Override
            protected double computeValue() {
                return op.getAsDouble();
            }

            @Override
            public ObservableList<?> getDependencies() {
                return FXCollections.observableArrayList(dependendies);
            }

        };

    }

    /**
     * Creates a binding with a computed value.
     * <p>
     * If the value of one of the dependencies changes, the binding is marked as
     * invalid.
     *
     * @param op           the operation that computes the value
     * @param dependencies the depencies that invalidate the computed value
     * @param <T>          the type of the value
     * @return a new binding
     */
    public static <T> ObjectBinding<T> compute(Supplier<T> op, ObservableValue<?>... dependencies) {
        return new ObjectBinding<T>() {
            {
                super.bind(dependencies);
            }

            @Override
            protected T computeValue() {
                return op.get();
            }

            @Override
            public ObservableList<?> getDependencies() {
                return FXCollections.observableArrayList(dependencies);
            }

        };

    }

    /**
     * Creates a binding with a converted value.
     * <p>
     * If the value of one of the dependencies changes, the binding is marked as
     * invalid.
     *
     * @param a       the property A
     * @param convert the conversion function from A to B
     * @param <A>     the type of the value
     * @param <B>     the type of the converted binding
     * @return a new binding
     */
    public static <A, B> ObjectBinding<B> convert(@NonNull ObservableValue<A> a, @NonNull Function<A, B> convert) {
        return new ObjectBinding<B>() {
            {
                super.bind(a);
            }

            @Override
            protected B computeValue() {
                return convert.apply(a.getValue());
            }

            @Override
            public ObservableList<?> getDependencies() {
                return FXCollections.singletonObservableList(a);
            }

        };

    }

    private static class ViaProperty<AA, BB> extends SimpleObjectProperty<BB> implements ChangeListener<AA> {
        final Function<AA, ? extends ObservableValue<BB>> stepFunction;

        ViaProperty(Function<AA, ? extends ObservableValue<BB>> viaFunction) {
            this.stepFunction = viaFunction;
        }

        @Override
        public void changed(ObservableValue<? extends AA> o, AA oldValue, AA newValue) {
            if (oldValue != null) {
                unbind();
            }
            if (newValue != null) {
                bind(stepFunction.apply(newValue));
            } else {
                set(null);
            }
        }

        @Override
        protected void invalidated() {
            // By calling get(), we mark the property as valid again.
            // Without calling get, the property will be marked as invalid once,
            // and then will never call invalidated() again.
            get();
        }
    }

    /**
     * Creates a binding to get a property {@code a.b}.
     *
     * @param root observable value {@code a}
     * @param step the function that gets {@code a.b}
     * @param <A>  the type of {@code a}
     * @param <B>  the type of {@code b}
     * @return the binding {@code a.b}.
     */
    public static <A, B> ObservableValue<B> via(ObservableValue<A> root, Function<A, ObservableValue<B>> step) {
        ViaProperty<A, B> viaProperty = new ViaProperty<>(step);
        root.addListener(viaProperty);
        viaProperty.changed(root, null, root.getValue());
        return viaProperty;
    }

    /**
     * Creates a binding to get an observable value {@code a.b.c}.
     *
     * @param root  observable value {@code a}
     * @param stepB the function that gets {@code a.b}
     * @param stepC the function that gets {@code b.c}
     * @param <A>   the type of {@code a}
     * @param <B>   the type of {@code b}
     * @param <C>   the type of {@code c}
     * @return the binding {@code a.b.c}.
     */
    public static <A, B, C> ObservableValue<C> via(ObservableValue<A> root, Function<A, ObservableValue<B>> stepB,
                                                   Function<B, ObservableValue<C>> stepC) {
        return via(via(root, stepB), stepC);
    }

    /**
     * Creates a binding to get an observable value {@code a.b.c.d}.
     *
     * @param root  observable value {@code a}
     * @param stepB the function that gets {@code a.b}
     * @param stepC the function that gets {@code b.c}
     * @param stepD the function that gets {@code c.d}
     * @param <A>   the type of {@code a}
     * @param <B>   the type of {@code b}
     * @param <C>   the type of {@code c}
     * @param <D>   the type of {@code d}
     * @return the binding {@code a.b.c}.
     */
    public static <A, B, C, D> ObservableValue<D> via(ObservableValue<A> root, Function<A, ObservableValue<B>> stepB,
                                                      Function<B, ObservableValue<C>> stepC,
                                                      Function<C, ObservableValue<D>> stepD) {
        return via(via(via(root, stepB), stepC), stepD);
    }


    /**
     * Creates a binding to get a property {@code a.b}.
     *
     * @param root observable value {@code a}
     * @param step the function that gets {@code a.b}
     * @param <A>  the type of {@code a}
     * @param <B>  the type of {@code b}
     * @return the binding {@code a.b}.
     */
    public static <A, B> Property<B> viaBidirectional(ObservableValue<A> root, Function<A, Property<B>> step) {
        SimpleObjectProperty<B> viaProperty = new SimpleObjectProperty<>();
        ChangeListener<A> changeListener = new ChangeListener<A>() {
            @Override
            public void changed(ObservableValue<? extends A> o, A oldValue, A newValue) {
                if (oldValue != null) {
                    viaProperty.unbindBidirectional(step.apply(newValue));
                }
                if (newValue != null) {
                    viaProperty.bindBidirectional(step.apply(newValue));
                }
            }
        };
        root.addListener(changeListener);
        changeListener.changed(root, null, root.getValue());
        return viaProperty;
    }

    /**
     * Creates a binding to get a property {@code a.b.c}.
     *
     * @param root  observable value {@code a}
     * @param stepB the function that gets {@code a.b}
     * @param stepC the function that gets {@code b.c}
     * @param <A>   the type of {@code a}
     * @param <B>   the type of {@code b}
     * @param <C>   the type of {@code c}
     * @return the binding {@code a.b.c}.
     */
    public static <A, B, C> Property<C> viaBidirectional(ObservableValue<A> root, Function<A, ObservableValue<B>> stepB,
                                                         Function<B, Property<C>> stepC) {
        return viaBidirectional(via(root, stepB), stepC);
    }

    /**
     * Creates a binding to get a property {@code a.b.c.d}.
     *
     * @param root  observable value {@code a}
     * @param stepB the function that gets {@code a.b}
     * @param stepC the function that gets {@code b.c}
     * @param stepD the function that gets {@code c.d}
     * @param <A>   the type of {@code a}
     * @param <B>   the type of {@code b}
     * @param <C>   the type of {@code c}
     * @param <D>   the type of {@code d}
     * @return the binding {@code a.b.c}.
     */
    public static <A, B, C, D> Property<D> viaBidirectional(ObservableValue<A> root, Function<A, ObservableValue<B>> stepB,
                                                            Function<B, ObservableValue<C>> stepC,
                                                            Function<C, Property<D>> stepD) {
        return viaBidirectional(via(via(root, stepB), stepC), stepD);
    }
}
