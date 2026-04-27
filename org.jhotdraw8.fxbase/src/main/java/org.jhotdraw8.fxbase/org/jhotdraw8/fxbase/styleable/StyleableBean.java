/*
 * @(#)StyleableBean.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxbase.styleable;

import javafx.beans.property.ReadOnlyProperty;
import org.jhotdraw8.icollection.readable.ReadableSet;
import org.jspecify.annotations.Nullable;

/// Styleable comprises the minimal interface required for a component (bean)
/// to be styled by CSS.
public interface StyleableBean {

    /// The type selector of this `StyleableBean`.
    ///
    /// This is analogous to an "element" in HTML.
    /// (<a href="http://www.w3.org/TR/CSS2/selector.html#type-selectors">CSS Type Selector</a>).
    ///
    /// @return the type of this `StyleableBean`
    String getTypeSelector();

    /// The id selector of this `StyleableBean`.
    ///
    /// This is analogous to the "id" attribute on an HTML element
    /// (<a href="http://www.w3.org/TR/CSS21/syndata.html#value-def-identifier">CSS ID Specification</a>).
    ///
    /// @return the id of this `StyleableBean`
    @Nullable
    String getId();

    /// Gets a read-only property of the id.
    ///
    /// @return a read-only view on the id property
    ReadOnlyProperty<String> idProperty();

    /// The style class selector of this `StyleableBean`.
    ///
    /// This is analogous to the "class" attribute on an HTML element
    /// (<a href="http://www.w3.org/TR/css3-selectors/#class-html">CSS3 class</a>).
    ///
    /// @return the classes of this `StyleableBean`
    ReadableSet<String> getStyleClasses();

    /// A string representation of the CSS style associated with this
    /// specific `StyleableBean`.
    ///
    /// This is analogous to the "style" attribute of an
    /// HTML element.
    ///
    /// @return the value of the style attribute
    @Nullable
    String getStyle();


    /*
     * A string representation of the attributes of this {@code StyleableBean}.
     * <p>
     * This is analogous to the attributes of an HTML element.
     *
     * @return the style attributes
     */
    // // ReadableMap<String,String> getStyleAttributes();

    /// Return the parent of this `StyleableBean`, or null if there is no parent.
    ///
    /// @return the parent of this `StyleableBean`, or null if there is no parent
    @Nullable
    StyleableBean getStyleableParent();

    /// The pseudo class selector of this `StyleableBean`.
    ///
    /// @return the pseudo-class states
    ReadableSet<String> getPseudoClassStates();

}
