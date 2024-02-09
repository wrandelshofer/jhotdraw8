/*
 * @(#)StyleableFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.Node;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.draw.key.NullableStringStyleableKey;
import org.jhotdraw8.draw.key.StringReadOnlyStyleableKey;
import org.jhotdraw8.draw.key.WordSetKey;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.draw.render.RenderingIntent;
import org.jhotdraw8.fxcollection.facade.ObservableSetFacade;
import org.jhotdraw8.icollection.ChampSet;
import org.jhotdraw8.icollection.readonly.ReadOnlySet;

/**
 * {@code StyleableFigure} provides user-editable "id", "style class" and "style" properties,
 * and a non-user-editable "pseudo-class" property.
 *
 * @author Werner Randelshofer
 */
public interface StyleableFigure extends Figure {

    /**
     * Defines the id for styling the figure with CSS.
     * <p>
     * Default value: {@code null}.
     */
    @NonNull
    NullableStringStyleableKey ID = new NullableStringStyleableKey("id");
    /**
     * Defines the style class of the figure. The style class is used for
     * styling a figure with CSS.
     * <p>
     * Default value: empty set.
     */
    @NonNull
    WordSetKey STYLE_CLASS = new WordSetKey("class", ChampSet.of());
    /**
     * Defines the pseudo class states of the figure. The pseudo class states
     * are used for styling a figure with CSS.
     * This property should not be made persistent because it is a computed value.
     * <p>
     * Default value: empty set.
     */
    @NonNull
    WordSetKey PSEUDO_CLASS = new WordSetKey("pseudoClass", ChampSet.of());
    /**
     * Defines the style of the figure. The style is used for styling a figure
     * with CSS.
     * <p>
     * Default value: {@code null}.
     */
    @NonNull
    StringReadOnlyStyleableKey STYLE = new StringReadOnlyStyleableKey("style");

    /**
     * We store the type selector of the figure on the node that is used
     * to render the figure. This allows to pass this information through
     * to classes that export a rendered scene graph.
     * <p>
     * This key is a String, because it is set as a property
     * on the JavaFX node that is used to render the figure.
     * The value type of the property is a String.
     */
    String TYPE_SELECTOR_NODE_KEY = "figureTypeSelector";

    /**
     * Updates a figure node with all style and effect properties defined in
     * this interface.
     * <p>
     * Applies the following properties: {@code ID}, {@code VISIBLE}.
     * <p>
     * This method is intended to be used by {@link #updateNode}.
     *
     * @param ctx  the render context
     * @param node a node which was created with method {@link #createNode}.
     */
    default void applyStyleableFigureProperties(@NonNull RenderContext ctx, @NonNull Node node) {
        if (ctx.get(RenderContext.RENDERING_INTENT) == RenderingIntent.EXPORT) {
            String styleId = getId();
            node.setId(styleId == null ? "" : styleId);
            node.getStyleClass().setAll(new ObservableSetFacade<>(getStyleClasses()));
            node.getProperties().put(TYPE_SELECTOR_NODE_KEY, getTypeSelector());
        }
    }

    @Override
    default @Nullable String getStyle() {
        return get(STYLE);
    }

    @Override
    default @NonNull ReadOnlySet<String> getStyleClasses() {
        return getNonNull(STYLE_CLASS);
    }

    @Override
    default @NonNull ReadOnlySet<String> getPseudoClassStates() {
        return getNonNull(PSEUDO_CLASS);
    }


    @Override
    default @Nullable String getId() {
        return get(ID);
    }

    @Override
    default @NonNull ReadOnlyProperty<String> idProperty() {
        return ID.readOnlyPropertyAt(getProperties());
    }
}
