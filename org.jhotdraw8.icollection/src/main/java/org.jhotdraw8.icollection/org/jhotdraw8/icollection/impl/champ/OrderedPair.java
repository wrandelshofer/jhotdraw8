package org.jhotdraw8.icollection.impl.champ;

/**
 * An ordered pair.
 * <p>
 * This is a value-type.
 *
 * @param <U> the type of the first element of the pair
 * @param <V> the type of the second element of the pair
 */
public record OrderedPair<U, V>(U first, V second) {
}
