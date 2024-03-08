/*
 * @(#)Tool.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.tool;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.scene.Node;
import javafx.scene.input.KeyCombination;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.event.Listener;
import org.jhotdraw8.draw.DrawingEditor;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.fxbase.beans.PropertyBean;
import org.jhotdraw8.fxbase.control.Disableable;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.fxcollection.typesafekey.NullableKey;
import org.jhotdraw8.fxcollection.typesafekey.NullableObjectKey;

/**
 * Tool.
 *
 * @author Werner Randelshofer
 */
public interface Tool extends PropertyBean, Disableable {

    /**
     * The name of the drawing view property.
     */
    String DRAWING_VIEW_PROPERTY = "drawingView";
    /**
     * The name of the drawing editor property.
     */
    String DRAWING_EDITOR_PROPERTY = "drawingEditor";

    /**
     * The key used for storing the action in an action map, and for accessing
     * resources in resource bundles.
     */
    NullableKey<String> NAME = new NullableObjectKey<>("name", String.class);
    /**
     * The key used for storing the {@code String} name for the action, used for
     * a menu or button.
     */
    NullableKey<String> LABEL = new NullableObjectKey<>("label", String.class);
    /**
     * The key used for storing a short {@code String} description for the
     * action, used for tooltip text.
     */
    NullableKey<String> SHORT_DESCRIPTION = new NullableObjectKey<>("ShortDescription", String.class);
    /**
     * The key used for storing a longer {@code String} description for the
     * action, could be used for context-sensitive help.
     */
    NullableKey<String> LONG_DESCRIPTION = new NullableObjectKey<>("LongDescription", String.class);
    /**
     * The key used for storing a small icon, such as {@code ImageView}. This is
     * typically used with menus.
     */
    Key<Node> SMALL_ICON = new NullableObjectKey<>("SmallIcon", Node.class);

    /**
     * The key used for storing a {@code KeyCombination} to be used as the
     * accelerator for the action.
     */
    Key<KeyCombination> ACCELERATOR_KEY = new NullableObjectKey<>("AcceleratorKey", KeyCombination.class);

    /**
     * The key used for storing a {@code KeyCombination} to be used as the
     * mnemonic for the action.
     *
     * @since 1.3
     */
    Key<KeyCombination> MNEMONIC_KEY = new NullableObjectKey<>("MnemonicKey", KeyCombination.class);

    /**
     * The key used for storing a {@code Boolean} that corresponds to the
     * selected state. This is typically used only for components that have a
     * meaningful selection state. For example,
     * {@code RadioButton</code> and <code>CheckBox} make use of this but
     * instances of {@code Menu} don't.
     */
    Key<Boolean> SELECTED_KEY = new NullableObjectKey<>("SelectedKey", Boolean.class);

    /**
     * The key used for large icon, such as {@code ImageView}. This is typically
     * used by buttons.
     */
    Key<Node> LARGE_ICON_KEY = new NullableObjectKey<>("LargeIconKey", Node.class);

    Key<String> STYLE_CLASS_KEY = new NullableObjectKey<>("StyleClass", String.class);

    // ---
    // Properties
    // ----

    /**
     * The currently active drawing view. By convention, this property is only
     * set by {@code DrawingView}.
     *
     * @return the drawingView property, with {@code getBean()} returning this
     * tool, and {@code getLabel()} returning {@code DRAWING_VIEW_PROPERTY}.
     */
    @NonNull ObjectProperty<DrawingView> drawingViewProperty();

    /**
     * The currently active drawing editor. By convention, this property is only
     * set by {@code DrawingEditor}.
     *
     * @return the drawingView property, with {@code getBean()} returning this
     * tool, and {@code getLabel()} returning {@code DRAWING_VIEW_PROPERTY}.
     */
    @NonNull ObjectProperty<DrawingEditor> drawingEditorProperty();

    // ---
    // Behaviors
    // ----

    /**
     * Returns the node which presents the tool and which handles input events.
     *
     * @return a node
     */
    @NonNull Node getNode();

    /**
     * Deletes the selection. Depending on the tool, this could be selected
     * figures, selected points or selected text.
     */
    void editDelete();

    /**
     * Cuts the selection into the clipboard. Depending on the tool, this could
     * be selected figures, selected points or selected text.
     */
    void editCut();

    /**
     * Copies the selection into the clipboard. Depending on the tool, this
     * could be selected figures, selected points or selected text.
     */
    void editCopy();

    /**
     * Duplicates the selection. Depending on the tool, this could be selected
     * figures, selected points or selected text.
     */
    void editDuplicate();

    /**
     * Pastes the contents of the clipboard. Depending on the tool, this could
     * be selected figures, selected points or selected text.
     */
    void editPaste();

    // ---
    // Listeners
    // ---

    /**
     * Adds a listener for this tool.
     *
     * @param l a listener
     */
    void addToolListener(Listener<ToolEvent> l);

    /**
     * Removes a listener for this tool.
     *
     * @param l a previously added listener
     */
    void removeToolListener(Listener<ToolEvent> l);

    // ---
    // Convenience Methods
    // ----

    /**
     * The localized name of the action for use in controls.
     *
     * @return The name
     */
    default @Nullable String getLabel() {
        return get(LABEL);
    }

    /**
     * The name of the action for use in action maps and for resource bundles.
     *
     * @return The instance
     */
    default @Nullable String getName() {
        return get(NAME);
    }

    /**
     * Gets the active drawing view.
     *
     * @return a drawing view
     */
    default @Nullable DrawingView getDrawingView() {
        return drawingViewProperty().get();
    }

    /**
     * Sets the active drawing view.
     * <p>
     * This method is invoked by {@link DrawingView} when the tool is set or
     * unset on the drawing view.
     *
     * @param drawingView a drawing view
     */
    default void setDrawingView(@Nullable DrawingView drawingView) {
        drawingViewProperty().set(drawingView);
    }

    /**
     * Deactivates the tool. This method is called whenever the user switches to
     * another tool.
     *
     * @param editor the editor
     */
    void deactivate(@NonNull DrawingEditor editor);

    /**
     * Activates the tool for the given editor. This method is called whenever
     * the user switches to this tool.
     *
     * @param editor the editor
     */
    void activate(DrawingEditor editor);

    default @Nullable DrawingEditor getDrawingEditor() {
        return drawingEditorProperty().get();
    }

    default void setDrawingEditor(@Nullable DrawingEditor newValue) {
        drawingEditorProperty().set(newValue);
    }

    /**
     * Returns a localized help text for this tool.
     *
     * @return the help text
     */
    String getHelpText();

    ReadOnlyBooleanProperty focusedProperty();
}
