/*
 * @(#)NumberConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.base.converter;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.io.IOException;
import java.nio.CharBuffer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

/**
 * Formats real numbers.
 * <p>
 * Supports clamping into a {@code [min,max]} range (inclusive), a scale factor
 * and a unit label.
 * <p>
 * Also allows to specify the minimum and maximum of integer digits, fraction
 * digits, as well as the minimum of negative and positive exponent.
 *
 * @author Werner Randelshofer
 */
public class NumberConverter implements Converter<Number> {

    /**
     * Specifies whether the formatter allows null values.
     */
    private final boolean allowsNullValue;
    @SuppressWarnings("rawtypes")
    private final @NonNull Number min;
    @SuppressWarnings("rawtypes")
    private final @NonNull Number max;
    private final @Nullable String unit;
    private final @NonNull DecimalFormat decimalFormat;
    private final @NonNull DecimalFormat scientificFormat;
    private final double factor;

    private final int minNegativeExponent = -3;
    private final int minPositiveExponent = 7;
    private final boolean usesScientificNotation = true;
    private final @NonNull Class<? extends Number> valueClass;

    /**
     * Creates a <code>NumberFormatter</code> with the a default
     * <code>NumberFormat</code> instance obtained from
     * <code>NumberFormat.getNumberInstance()</code>.
     */
    @SuppressWarnings("WeakerAccess")
    public NumberConverter() {
        this(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0);
    }

    public NumberConverter(Class<? extends Number> valueClass) {
        this(valueClass, null, null, 1.0, false, null);
    }

    /**
     * Creates a NumberFormatter with the specified Format instance.
     *
     * @param min        the min
     * @param max        the max
     * @param multiplier the multiplier
     */
    @SuppressWarnings("WeakerAccess")
    public NumberConverter(double min, double max, double multiplier) {
        this(Double.class, min, max, multiplier, false, null);
    }

    /**
     * Creates a NumberFormatter with the specified Format instance.
     *
     * @param min             the min
     * @param max             the max
     * @param multiplier      the multiplier
     * @param allowsNullValue whether null values are allowed
     */
    @SuppressWarnings("WeakerAccess")
    public NumberConverter(double min, double max, double multiplier, boolean allowsNullValue) {
        this(Double.class, min, max, multiplier, allowsNullValue, null);
    }

    /**
     * Creates a NumberFormatter with the specified Format instance.
     *
     * @param min             the min
     * @param max             the max
     * @param multiplier      the multiplier
     * @param allowsNullValue whether null values are allowed
     * @param unit            the unit string
     */
    @SuppressWarnings("WeakerAccess")
    public NumberConverter(Class<? extends Number> valueClass,
                           Number min, Number max, double multiplier, boolean allowsNullValue, String unit) {
        this(valueClass, min, max, multiplier, allowsNullValue, unit,
                valueClass == Float.class
                        ? new DecimalFormat("#################0.########", new DecimalFormatSymbols(Locale.ENGLISH))
                        : new DecimalFormat("#################0.#################", new DecimalFormatSymbols(Locale.ENGLISH)),
                new DecimalFormat("0.0################E0", new DecimalFormatSymbols(Locale.ENGLISH)));
    }

    public NumberConverter(@NonNull Class<? extends Number> valueClass, @NonNull Number min, @NonNull Number max, double multiplier, boolean allowsNullValue, @Nullable String unit,
                           @NonNull DecimalFormat decimalFormat,
                           @NonNull DecimalFormat scientificFormat
    ) {
        super();
        this.valueClass = valueClass;
        this.decimalFormat = decimalFormat;
        this.scientificFormat = scientificFormat;
        this.min = min;
        this.max = max;
        this.factor = multiplier;
        this.allowsNullValue = allowsNullValue;
        this.unit = unit;
    }

    public NumberConverter(double min, double max, double multiplier, boolean allowsNullValue, String unit) {
        this(Double.class, min, max, multiplier, allowsNullValue, unit);
    }


    /**
     * Returns the minimum permissible value.
     *
     * @return Minimum legal value that can be input
     */
    @SuppressWarnings({"rawtypes", "unused"})
    public @NonNull Number getMinimum() {
        return min;
    }


    /**
     * Returns the maximum permissible value.
     *
     * @return Maximum legal value that can be input
     */
    @SuppressWarnings({"rawtypes", "unused"})
    public @NonNull Number getMaximum() {
        return max;
    }

    /**
     * Gets the factor for use in percent, per mille, and similar formats.
     *
     * @return the factor
     */
    public double getFactor() {
        return factor;
    }

    /**
     * Returns true if null values are allowed.
     *
     * @return true if null values are allowed
     */
    @SuppressWarnings("WeakerAccess")
    public boolean getAllowsNullValue() {
        return allowsNullValue;
    }


    @Override
    public void toString(@NonNull Appendable buf, @Nullable IdSupplier idSupplier, @Nullable Number value) throws IOException {
        if (value == null) {
            buf.append((allowsNullValue) ? "none" : "NaN");
            return;
        }

        switch (value) {
            case Long l -> {
                long v = l;
                if (factor != 1.0) {
                    v = (long) (v * factor);
                }
                buf.append(Long.toString(v));
            }
            case Integer i -> {
                int v = i;
                if (factor != 1.0) {
                    v = (int) (v * factor);
                }
                buf.append(Integer.toString(v));
            }
            case Byte b -> {
                byte v = b;
                if (factor != 1.0) {
                    v = (byte) (v * factor);
                }
                buf.append(Byte.toString(v));
            }
            case Short i -> {
                short v = i;
                if (factor != 1.0) {
                    v = (short) (v * factor);
                }
                buf.append(Short.toString(v));
            }
            case Float aFloat -> {
                float v = aFloat;
                if (factor != 1.0) {
                    v = (float) (v * factor);
                }
                if (Float.isInfinite(v)) {
                    if (v < 0.0) {
                        buf.append('-');
                    }
                    buf.append("INF");
                } else if (Float.isNaN(v)) {
                    buf.append("NaN");
                } else {
                    String str;// = Float.toString(v);
                    double exponent = v == 0 ? 0 : Math.log10(Math.abs(v));
                    if (!usesScientificNotation || exponent > minNegativeExponent
                                                   && exponent < minPositiveExponent) {
                        // DecimalFormat produces too many digits, because it
                        // promotes the float to a double before it converts it.
                        str = Float.toString(v);
                        int exponentIndex = str.indexOf('E');
                        int pointIndex = str.indexOf('.');
                        int fractionDigits = (exponentIndex == -1 ? str.length() : exponentIndex) - pointIndex;
                        if (str.endsWith(".0")) {
                            str = str.substring(0, str.length() - 2);
                        }
                        if (exponentIndex >= 0 || fractionDigits > decimalFormat.getMaximumFractionDigits()) {
                            str = decimalFormat.format(v);
                        }
                    } else {
                        str = scientificFormat.format(v);
                    }
                    buf.append(str);
                }
            }
            default -> {
                double v = value.doubleValue();
                if (factor != 1.0) {
                    v = v * factor;
                }
                if (Double.isInfinite(v)) {
                    if (v < 0.0) {
                        buf.append('-');
                    }
                    buf.append("INF");
                } else if (Double.isNaN(v)) {
                    buf.append("NaN");
                } else {
                    String str;
                    double exponent = v == 0 ? 1 : Math.log10(Math.abs(v));
                    if (!usesScientificNotation || exponent > minNegativeExponent
                                                   && exponent < minPositiveExponent) {
                        str = decimalFormat.format(v);
                    } else {
                        str = scientificFormat.format(v);
                    }
                    buf.append(str);
                }
            }
        }

        if (value != null) {
            if (unit != null) {
                buf.append(unit);
            }
        }
    }

    @Override
    public @Nullable Number fromString(@NonNull CharBuffer str, @Nullable IdResolver idResolver) throws
            ParseException {
        if ((str.length() == 0) && getAllowsNullValue()) {
            return null;
        }
        if (str == null) {
            throw new ParseException("str", 0);
        }

        // Parse the remaining characters from the CharBuffer
        final int remaining = str.remaining();
        int end = 0; // end is a relative to CharBuffer.position();
        {
            boolean noMoreSigns = false;
            boolean noMorePoints = false;
            boolean noMoreEs = false;
            Outer:
            for (; end < remaining; end++) {
                char c = str.charAt(end);// does not consume chars from CharBuffer!
                switch (c) {
                    case '+':
                    case '-':
                        if (noMoreSigns) {
                            break Outer;
                        }
                        noMoreSigns = true;
                        break;
                    case '.':
                        if (noMorePoints) {
                            break Outer;
                        }
                        noMoreSigns = true;
                        noMorePoints = true;
                        break;
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                        noMoreSigns = true;
                        break;
                    case 'e':
                    case 'E':
                        if (noMoreEs) {
                            break Outer;
                        }
                        noMoreSigns = false;
                        noMorePoints = false;
                        noMoreEs = true;
                        break;
                    case 'I':// INF
                    case 'N': // NaN
                        end += 3;
                        break Outer;
                    default:
                        break Outer;
                }
            }
        }

        String text = str.subSequence(0, end).toString();
        switch (text) {
            case "-INF":
                str.position(str.position() + end);
                return Double.NEGATIVE_INFINITY;
            case "INF":
                str.position(str.position() + end);
                return Double.POSITIVE_INFINITY;
            case "NaN":
                str.position(str.position() + end);
                return Double.NaN;
        }

        // Remove unit from text
        if (unit != null && end + unit.length() <= str.length()) {
            if (str.subSequence(end, end + unit.length()).toString().startsWith(unit)) {
                end += unit.length();
            }
        }
        if (text.isEmpty()) {
            throw new ParseException("invalid value", str.position());
        }

        Class<?> valueClass = getValueClass();
        Number value;
        if (valueClass != null) {
            try {
                if (valueClass == Integer.class) {
                    int v = Integer.parseInt(text);
                    if (factor != 1.0) {
                        v = (int) (v / factor);
                    }
                    value = v;
                } else if (valueClass == Long.class) {
                    long v = Long.parseLong(text);
                    if (factor != 1.0) {
                        v = (long) (v / factor);
                    }
                    value = v;
                } else if (valueClass == Float.class) {
                    float v = Float.parseFloat(text);
                    if (factor != 1.0) {
                        v = (float) (v / factor);
                    }
                    value = v;
                } else if (valueClass == Double.class) {
                    double v = Double.parseDouble(text);
                    if (factor != 1.0) {
                        v = (v / factor);
                    }
                    value = v;
                } else if (valueClass == Byte.class) {
                    byte v = Byte.parseByte(text);
                    if (factor != 1.0) {
                        v = (byte) (v / factor);
                    }
                    value = v;
                } else if (valueClass == Short.class) {
                    short v = Short.parseShort(text);
                    if (factor != 1.0) {
                        v = (short) (v / factor);
                    }
                    value = v;
                } else {
                    throw new ParseException("parse error (1)", str.position());
                }
            } catch (NumberFormatException e) {
                ParseException pe = new ParseException("illegal number format", str.position());
                pe.initCause(e);
                throw pe;
            }
        } else {
            throw new ParseException("illegal value class:" + valueClass, str.position());
        }

        try {
            if (!isValidValue(value, true)) {
                throw new ParseException("invalid value", str.position());
            }
        } catch (ClassCastException cce) {
            ParseException pe = new ParseException("invalid value", str.position());
            pe.initCause(cce);
            throw pe;
        }
        // consume the text that we just parsed
        str.position(str.position() + end);
        return value;
    }

    /**
     * Returns true if <code>value</code> is between the min/max.
     *
     * @param wantsCCE If false, and a ClassCastException is thrown in comparing
     *                 the values, the exception is consumed and false is returned.
     */
    @SuppressWarnings({"unchecked", "WeakerAccess"})
    private boolean isValidValue(@NonNull Number value, boolean wantsCCE) {
        try {
            if (min instanceof Comparable c && c.compareTo(value) > 0) {
                return false;
            }
        } catch (ClassCastException cce) {
            if (wantsCCE) {
                throw cce;
            }
            return false;
        }

        try {
            if (max instanceof Comparable c && c.compareTo(value) < 0) {
                return false;
            }
        } catch (ClassCastException cce) {
            if (wantsCCE) {
                throw cce;
            }
            return false;
        }
        return true;
    }


    /**
     * Gets the minimum negative exponent value for scientific notation.
     *
     * @return the minimum negative exponent
     */
    @SuppressWarnings("unused")
    public int getMinimumNegativeExponent() {
        return minNegativeExponent;
    }


    /**
     * Gets the minimum positive exponent value for scientific notation.
     *
     * @return the minimum positive exponent
     */
    @SuppressWarnings("unused")
    public int getMinimumPositiveExponent() {
        return minPositiveExponent;
    }


    /**
     * Returns true if scientific notation is used.
     *
     * @return true if scientific notation is used
     */
    @SuppressWarnings("unused")
    public boolean isUsesScientificNotation() {
        return usesScientificNotation;
    }

    /**
     * Gets the value class.
     *
     * @return the value class
     */
    public @NonNull Class<? extends Number> getValueClass() {
        return valueClass;
    }


    @Override
    public @NonNull Number getDefaultValue() {
        return 0.0;
    }
}
