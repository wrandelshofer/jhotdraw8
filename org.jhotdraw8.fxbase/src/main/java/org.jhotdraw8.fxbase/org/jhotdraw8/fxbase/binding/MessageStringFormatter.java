/*
 * @(#)MessageStringFormatter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxbase.binding;

import javafx.beans.binding.StringBinding;
import javafx.beans.binding.StringExpression;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jspecify.annotations.Nullable;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * MessageStringFormatter.
 * <p>
 * Boolean values are converted to 0 and 1. This allows to format the boolean
 * value using a choice: {@code {0,choice,0#false|1#true} }
 *
 */
public abstract class MessageStringFormatter extends StringBinding {

    /**
     * Don't let anyone instantiate this class.
     */
    private MessageStringFormatter() {
    }

    private static Object extractValue(Object obj) {
        Object value = obj instanceof ObservableValue ? ((ObservableValue<?>) obj).getValue() : obj;
        // since message format can not handle booleans, we convert them to 1 and 0
        if (Boolean.TRUE.equals(value)) {
            return 1;
        }
        if (Boolean.FALSE.equals(value)) {
            return 0;
        }
        return value;
    }

    private static Object[] extractValues(Object[] objs) {
        final int n = objs.length;
        final Object[] values = new Object[n];
        for (int i = 0; i < n; i++) {
            values[i] = extractValue(objs[i]);
        }
        return values;
    }

    private static ObservableValue<?>[] extractDependencies(Object... args) {
        final List<ObservableValue<?>> dependencies = new ArrayList<>();
        for (final Object obj : args) {
            if (obj instanceof ObservableValue) {
                dependencies.add((ObservableValue<?>) obj);
            }
        }
        return dependencies.toArray(new ObservableValue<?>[0]);
    }

    public static StringExpression format(final @Nullable String format, final Object... args) {
        Objects.requireNonNull(format, "format");
        if (extractDependencies(args).length == 0) {
            return ConstantStringExpression.of(String.format(format, args));
        }
        final MessageStringFormatter formatter = new MessageStringFormatter() {
            {
                super.bind(extractDependencies(args));
            }

            @Override
            public void dispose() {
                super.unbind(extractDependencies(args));
            }

            @Override
            protected String computeValue() {
                final Object[] values = extractValues(args);
                return MessageFormat.format(format, values);
            }

            @Override
            public ObservableList<ObservableValue<?>> getDependencies() {
                return FXCollections.unmodifiableObservableList(FXCollections
                        .observableArrayList(extractDependencies(args)));
            }
        };
        // Force calculation to check format
        formatter.get();
        return formatter;
    }
}
