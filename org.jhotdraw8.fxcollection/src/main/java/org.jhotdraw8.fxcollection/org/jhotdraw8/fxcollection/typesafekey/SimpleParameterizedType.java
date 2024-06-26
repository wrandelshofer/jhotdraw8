package org.jhotdraw8.fxcollection.typesafekey;

import org.jspecify.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public record SimpleParameterizedType(Type rawType,
                                      Type... actualTypeArguments) implements ParameterizedType {

    public Type getTypeArgument(int i) {
        return actualTypeArguments[i];
    }

    public int getTypeArgumentCount() {
        return actualTypeArguments.length;
    }

    @Override
    public String getTypeName() {
        StringBuilder b = new StringBuilder();
        b.append(rawType.getTypeName());
        b.append('<');
        int first = b.length();
        for (Type t : actualTypeArguments) {
            if (b.length() != first) {
                b.append(',');
            }
            b.append(t.getTypeName());
        }
        b.append('>');
        return b.toString();
    }

    @Override
    public Type[] getActualTypeArguments() {
        return actualTypeArguments.clone();
    }

    @Override
    public Type getRawType() {
        return rawType;
    }

    @Override
    public @Nullable Type getOwnerType() {
        return null;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SimpleParameterizedType that = (SimpleParameterizedType) o;
        return Objects.equals(rawType, that.rawType) && Arrays.equals(actualTypeArguments, that.actualTypeArguments);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(rawType);
        result = 31 * result + Arrays.hashCode(actualTypeArguments);
        return result;
    }

    @Override
    public String toString() {
        return "SimpleParameterizedType{"
               + rawType
               + '<' + Arrays.stream(actualTypeArguments).map(Type::getTypeName).collect(Collectors.joining(", "))
               + ">}";
    }
}
