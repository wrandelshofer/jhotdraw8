/*
 * @(#)Selector.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.ast;

import org.jhotdraw8.css.model.SelectorModel;
import org.jspecify.annotations.Nullable;


/// A "selector" is a tree of "combinator"s.
public abstract class Selector extends AbstractSyntaxTree {


    public Selector(@Nullable SourceLocator sourceLocator) {
        super(sourceLocator);
    }

    /// Returns the specificity of this selector.
    ///
    /// A selector's specificity is calculated as follows:
    ///
    ///   - count the number of ID selectors in the selector (= a)
    ///   - count the number of class selectors, attributes selectors, and
    ///     pseudo-classes in the selector (= b)
    ///   - count the number of type selectors and pseudo-elements in the
    ///     selector (= c)
    ///   - ignore the universal selector
    ///
    ///
    /// Selectors inside the negation pseudo-class are counted like any other,
    /// but the negation itself does not count as a pseudo-class.
    ///
    /// Concatenating the three numbers a-b-c (in a number system with a large
    /// base) gives the specificity.
    ///
    /// In this implementation we compute specificity with
    /// `specificity=100*a+10*b+c`.
    ///
    /// References:
    /// <dl>
    ///     <dt>CSS Syntax Selectors Level 3, Chapter 9. Calculating a selector's
    ///       specificity</dt>
    /// <dd><a href="https://www.w3.org/TR/2011/REC-css3-selectors-20110929/">w3.org</a></dd>
    /// </dl>
    ///
    /// @return the specificity
    public abstract int getSpecificity();

    /// Returns true if the selector matches the element.
    ///
    /// @param <T>     the element type
    /// @param model   The helper is used to access properties of the element and
    ///                parent or sibling elements in the document.
    /// @param element the element
    /// @return true on match
    public <T> boolean matches(SelectorModel<T> model, T element) {
        return match(model, element) != null;
    }

    /// Returns the matching element.
    ///
    /// @param <T>     element type
    /// @param model   The helper is used to access properties of the element and
    ///                parent or sibling elements in the document.
    /// @param element the element
    /// @return the matching element or null
    protected abstract @Nullable <T> T match(SelectorModel<T> model, T element);

    /// Returns a qualified name, if this selector only matches on elements with
    /// a specific type name.
    ///
    /// This implementation returns null.
    ///
    /// @return a type name or null
    public @Nullable TypeSelector matchesOnlyOnASpecificType() {
        return null;
    }

}
