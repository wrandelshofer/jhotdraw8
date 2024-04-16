/*
 * @(#)AbstractStyleAttributesInspector.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.inspector;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyMapProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.css.StyleOrigin;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.css.ast.AndCombinator;
import org.jhotdraw8.css.ast.ClassSelector;
import org.jhotdraw8.css.ast.Declaration;
import org.jhotdraw8.css.ast.IdSelector;
import org.jhotdraw8.css.ast.Selector;
import org.jhotdraw8.css.ast.SelectorGroup;
import org.jhotdraw8.css.ast.SimplePseudoClassSelector;
import org.jhotdraw8.css.ast.SimpleSelector;
import org.jhotdraw8.css.ast.StyleRule;
import org.jhotdraw8.css.ast.Stylesheet;
import org.jhotdraw8.css.ast.TypeSelector;
import org.jhotdraw8.css.converter.CssConverter;
import org.jhotdraw8.css.io.CssPrettyPrinter;
import org.jhotdraw8.css.manager.StylesheetsManager;
import org.jhotdraw8.css.model.SelectorModel;
import org.jhotdraw8.css.parser.CssParser;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.value.QualifiedName;
import org.jhotdraw8.draw.css.value.CssColor;
import org.jhotdraw8.draw.css.value.CssFont;
import org.jhotdraw8.draw.css.value.Paintable;
import org.jhotdraw8.draw.figure.TextFontableFigure;
import org.jhotdraw8.draw.popup.BooleanPicker;
import org.jhotdraw8.draw.popup.CssColorPicker;
import org.jhotdraw8.draw.popup.CssFontPicker;
import org.jhotdraw8.draw.popup.EnumPicker;
import org.jhotdraw8.draw.popup.ExamplesPicker;
import org.jhotdraw8.draw.popup.FontFamilyPicker;
import org.jhotdraw8.draw.popup.PaintablePicker;
import org.jhotdraw8.draw.popup.Picker;
import org.jhotdraw8.fxbase.concurrent.PlatformUtil;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.fxbase.undo.UndoableEditHelper;

import javax.swing.event.UndoableEditEvent;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SequencedMap;
import java.util.SequencedSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Intentionally does not implement the inspector interface, so
 * that subclasses can use this inspector on different subject
 * types.
 *
 * @param <E> the element type
 */
public abstract class AbstractStyleAttributesInspector<E> {
    /**
     * The name of the {@link #showingProperty}.
     */
    public static final @NonNull String SHOWING_PROPERTY = "showing";
    /**
     * This placeholder is displayed to indicate that no value has
     * been specified for this property.
     * <p>
     * The placeholder should be a comment, e.g. "/* unspecified value * /",
     * or white space, e.g. "  ", or one of the keywords
     * {@link CssTokenType#IDENT_INITIAL},
     * {@link CssTokenType#IDENT_INHERIT},
     * {@link CssTokenType#IDENT_REVERT},
     * {@link CssTokenType#IDENT_UNSET},
     */
    public static final @NonNull String UNSPECIFIED_VALUE_PLACEHOLDER = "  ";//"/* unspecified value */";
    /**
     * This placeholder is displayed to indicate that multiple values have
     * been specified for this property.
     * <p>
     * The placeholder should be a comment, e.g. "/* multiple values * /",
     * or white space, e.g. "  ".
     */
    public static final @NonNull String MULTIPLE_VALUES_PLACEHOLDER = "/* multiple values */";
    protected final @NonNull BooleanProperty showing = new SimpleBooleanProperty(this, SHOWING_PROPERTY, true);
    protected final @NonNull UndoableEditHelper undoHelper = new UndoableEditHelper(this, this::forwardUndoableEdit);
    private final @NonNull ObjectProperty<Predicate<QualifiedName>> attributeFilter = new SimpleObjectProperty<>(k -> true);
    private final @NonNull ObjectProperty<Supplier<CssParser>> cssParserFactory = new SimpleObjectProperty<>(CssParser::new);
    private final @NonNull ReadOnlyMapProperty<WritableStyleableMapAccessor<?>, Picker<?>> accessorPickerMap = new SimpleMapProperty<>(FXCollections.observableMap(new LinkedHashMap<>()));
    private final @NonNull SetProperty<E> selection = new SimpleSetProperty<>();
    private final @NonNull Map<QualifiedName, String> helpTexts = new HashMap<>();
    private final @NonNull List<LookupEntry> lookupTable = new ArrayList<>();
    private Node node;
    @FXML
    private Button applyButton;
    @FXML
    private Button selectButton;
    @FXML
    private CheckBox showUnspecifiedAttributesCheckBox;
    @FXML
    private CheckBox updateContentsCheckBox;
    @FXML
    private CheckBox updateSelectorCheckBox;
    @FXML
    private CheckBox composeAttributesCheckBox;
    @FXML
    private TextArea textArea;
    @FXML
    private RadioButton showAttributeValues;
    @FXML
    private ToggleGroup shownValues;
    @FXML
    private RadioButton showStylesheetValues;
    @FXML
    private RadioButton showUserAgentValues;
    @FXML
    private RadioButton showAppliedValues;
    private boolean textAreaValid = true;
    private boolean isApplying;

    {
        showing.addListener((o, oldv, newv) -> {
            if (newv) {
                Platform.runLater(this::validateTextArea);
            }
        });
    }

    {
        SetChangeListener<E> listener = change -> invalidateTextArea(selection);
        selection.addListener((o, oldv, newv) -> {
            if (oldv != null) {
                oldv.removeListener(listener);
            }
            if (newv != null) {
                newv.addListener(listener);
                invalidateTextArea(selection);
            }
        });
    }

    public AbstractStyleAttributesInspector() {
        this(StyleAttributesInspector.class.getResource("StyleAttributesInspector.fxml"));
    }

    public AbstractStyleAttributesInspector(@NonNull URL fxmlUrl) {
        init(fxmlUrl);
    }

    protected abstract void forwardUndoableEdit(@NonNull UndoableEditEvent event);

    public @NonNull ReadOnlyMapProperty<WritableStyleableMapAccessor<?>, Picker<?>> accessorPickerMapProperty() {
        return accessorPickerMap;
    }

    private void apply(ActionEvent event) {
        undoHelper.startCompositeEdit(null);
        isApplying = true;
        CssParser parser = getCssParserFactoryOrDefault().get();
        TextArea textArea = getTextArea();
        try {
            Stylesheet stylesheet = parser.parseStylesheet(textArea.getText(), null, null);
            if (!parser.getParseExceptions().isEmpty()) {
                System.out.println("StyleAttributesInspector:\n" + parser.getParseExceptions().toString().replace(',', '\n'));
                ParseException e = parser.getParseExceptions().getFirst();
                new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
                textArea.positionCaret(e.getErrorOffset());
                textArea.requestFocus();
                return;
            }

            ObservableMap<String, Set<E>> pseudoStyles = createPseudoStyles();

            StylesheetsManager<E> sm = getStyleManager();
            if (sm == null) {
                return;
            }
            SelectorModel<E> fsm = sm.getSelectorModel();
            fsm.additionalPseudoClassStatesProperty().setValue(pseudoStyles);
            // This must not be done in parallel, because we may have observers on
            // the entities.
            for (E entity : getEntities()) {
                if (sm.applyStylesheetTo(StyleOrigin.USER, stylesheet, entity, false)) {
                    fireInvalidated(entity);
                }
            }
            recreateHandles();
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Unexpected Exception " + ex.getMessage(), ex);

            return;
        } catch (ParseException e) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Unexpected Exception " + e.getMessage(), e);

            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
            textArea.positionCaret(e.getErrorOffset());
            textArea.requestFocus();
        }
        isApplying = false;
        undoHelper.stopCompositeEdit();
    }


    /**
     * Attribute filter can be used to show only a specific set
     * of attributes in the inspector.
     *
     * @return attribute filter
     */
    public @NonNull Property<Predicate<QualifiedName>> attributeFilter() {
        return attributeFilter;
    }

    private @Nullable String buildString(@Nullable List<CssToken> attribute) {
        if (attribute == null) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        for (CssToken t : attribute) {
            buf.append(t.fromToken());
        }
        return buf.toString();
    }

    private @NonNull Map<QualifiedName, String> collectAttributeValues(boolean decompose, @NonNull List<E> matchedFigures, @NonNull SelectorModel<E> selectorModel) {
        final StyleOrigin origin;
        if (showAttributeValues.isSelected()) {
            origin = StyleOrigin.USER;
        } else if (showStylesheetValues.isSelected()) {
            origin = StyleOrigin.AUTHOR;
        } else if (showUserAgentValues.isSelected()) {
            origin = StyleOrigin.USER_AGENT;
        } else {
            origin = null;
        }

        // Collect attributes that are contained in all matched figures
        Map<QualifiedName, String> attr = new TreeMap<>();
        Predicate<QualifiedName> filter = getAttributeFilter();
        boolean first = true;
        for (E f : matchedFigures) {
            if (first) {
                first = false;
                for (QualifiedName qname : decompose ? selectorModel.getDecomposedAttributeNames(f) : selectorModel.getComposedAttributeNames(f)) {
                    if (!filter.test(qname)) {
                        continue;
                    }
                    String attribute = buildString(selectorModel.getAttribute(f, origin, qname.namespace(), qname.name()));
                    attr.put(qname, attribute == null ? UNSPECIFIED_VALUE_PLACEHOLDER : attribute);
                }
            } else {
                for (Iterator<QualifiedName> i = attr.keySet().iterator(); i.hasNext(); ) {
                    QualifiedName qname = i.next();
                    if (!selectorModel.hasAttribute(f, qname.namespace(), qname.name())) {
                        i.remove();
                        continue;
                    }
                    String oldAttrValue = attr.get(qname);
                    String newAttrValue = buildString(selectorModel.getAttribute(f, origin, qname.namespace(), qname.name()));
                    if (newAttrValue == null) {
                        newAttrValue = UNSPECIFIED_VALUE_PLACEHOLDER;
                    }
                    if (!Objects.equals(oldAttrValue, newAttrValue)) {
                        attr.put(qname, MULTIPLE_VALUES_PLACEHOLDER);
                    }

                }
            }
        }

        if (!showUnspecifiedAttributesCheckBox.isSelected()) {
            attr.entrySet().removeIf(entry -> UNSPECIFIED_VALUE_PLACEHOLDER.equals(entry.getValue()));
        }


        return attr;
    }

    protected void collectHelpTexts(@NonNull Collection<E> figures) {
        StylesheetsManager<E> styleManager = getStyleManager();
        if (styleManager == null) {
            return;
        }
        SelectorModel<E> selectorModel = styleManager.getSelectorModel();

        for (E f : figures) {
            for (QualifiedName qname : selectorModel.getAttributeNames(f)) {
                Converter<?> c = getConverter(selectorModel, f, qname.namespace(), qname.name());
                String helpText = c == null ? null : c.getHelpText();
                if (helpText != null) {
                    helpTexts.put(qname, helpText);
                }
            }
        }
    }

    private @NonNull <T> Picker<T> createAndCachePicker(@NonNull WritableStyleableMapAccessor<T> acc) {
        ObservableMap<WritableStyleableMapAccessor<?>, Picker<?>> amap = getAccessorPickerMap();
        @SuppressWarnings("unchecked") Picker<T> picker = (Picker<T>) amap.get(acc);
        if (picker == null) {
            picker = createPicker(acc);
            amap.put(acc, picker);
        }
        return picker;
    }

    @SuppressWarnings("unchecked")
    protected @NonNull <T> Picker<T> createPicker(@NonNull WritableStyleableMapAccessor<T> acc) {
        Class<T> type = acc.getRawValueType();
        boolean nullable = true;
        if (acc.getCssConverter() instanceof CssConverter<T> converter) {
            nullable = converter.isNullable();
        }
        Picker<?> p = null;
        if (type == Boolean.class) {
            p = new BooleanPicker(nullable);
        } else if (type == CssColor.class) {
            p = new CssColorPicker();
        } else if (type == Paintable.class) {
            p = new PaintablePicker();
        } else if (type == CssFont.class) {
            p = new CssFontPicker();
        } else if (acc == TextFontableFigure.FONT_FAMILY) {
            p = new FontFamilyPicker();
        } else if (type.isEnum()) {
            Class<? extends Enum<?>> enumClazz = (Class<? extends Enum<?>>) type;
            @SuppressWarnings("rawtypes")
            EnumPicker suppress = new EnumPicker(enumClazz, acc.getCssConverter());
            p = suppress;
        } else {
            @SuppressWarnings("rawtypes")
            ExamplesPicker suppress = new ExamplesPicker(acc.getExamples(), acc.getCssConverter());
            p = suppress;
        }

        return (Picker<T>) p;
    }

    private @NonNull ObservableMap<String, Set<E>> createPseudoStyles() {
        ObservableMap<String, Set<E>> pseudoStyles = FXCollections.observableHashMap();
        SequencedSet<E> fs = new LinkedHashSet<>(selection.get());
        // handling of emptyness must be consistent with code in
        // handleSelectionChanged() method
        if (fs.isEmpty()) {
            fs.add(getRoot());
        }

        pseudoStyles.put("selected", fs);
        return pseudoStyles;
    }

    private @NonNull SelectorGroup createSelector(@NonNull Set<E> selection, @NonNull SelectorModel<E> selectorModel) {
        String id = null;
        QualifiedName type = null;
        Set<String> styleClasses = new TreeSet<>();
        boolean first = true;
        for (E f : selection) {
            if (first) {
                id = selectorModel.getId(f);
                type = selectorModel.getType(f);
                first = false;
                styleClasses.addAll(selectorModel.getStyleClasses(f).asCollection());
            } else {
                id = null;
                type = Objects.equals(selectorModel.getType(f), type) ? type : null;
                styleClasses.retainAll(selectorModel.getStyleClasses(f).asCollection());
            }
        }

        List<SimpleSelector> selectors = new ArrayList<>();
        if (type != null && !type.name().isEmpty()) {
            selectors.add(new TypeSelector(null, TypeSelector.ANY_NAMESPACE, type.name()));
        }
        if (id != null && !id.isEmpty()) {
            selectors.add(new IdSelector(null, id));
        }
        for (String clazz : styleClasses) {
            selectors.add(new ClassSelector(null, clazz));
        }
        selectors.add(new SimplePseudoClassSelector(null, "selected"));

        Selector prev = null;
        Collections.reverse(selectors);
        for (SimpleSelector s : selectors) {
            if (prev != null) {
                prev = new AndCombinator(null, s, prev);
            } else {
                prev = s;
            }
        }
        return new SelectorGroup(null, Collections.singletonList(prev));
    }

    /**
     * This method is invoked when this inspector has changed properties of
     * the specified element.
     *
     * @param f an element
     */
    protected abstract void fireInvalidated(E f);

    protected abstract @Nullable Object get(E f, WritableStyleableMapAccessor<Object> finalSelectedAccessor);

    protected abstract @Nullable WritableStyleableMapAccessor<?> getAccessor(SelectorModel<E> fsm, E f, String propertyNamespace, String propertyName);

    public ObservableMap<WritableStyleableMapAccessor<?>, Picker<?>> getAccessorPickerMap() {
        return accessorPickerMap.get();
    }

    public Predicate<QualifiedName> getAttributeFilter() {
        return attributeFilter.get();
    }

    public void setAttributeFilter(Predicate<QualifiedName> attributeFilter) {
        this.attributeFilter.set(attributeFilter);
    }

    protected abstract @Nullable Converter<?> getConverter(SelectorModel<E> selectorModel, E f, String namespace, String name);

    protected abstract @NonNull Iterable<E> getEntities();

    private @Nullable LookupEntry getLookupEntryAt(int caretPosition) {
        int insertionPoint = Collections.binarySearch(lookupTable, new LookupEntry(caretPosition, null, null));
        if (insertionPoint < 0) {
            insertionPoint = ~insertionPoint - 1;
        }
        LookupEntry d = null;
        if (0 <= insertionPoint && insertionPoint < lookupTable.size()) {
            LookupEntry entry = lookupTable.get(insertionPoint);
            if (caretPosition <= entry.declaration.getEndPos()) {
                d = entry;
            }
        }
        return d;
    }

    public Node getNode() {
        return node;
    }

    protected abstract @Nullable E getRoot();

    public ObservableSet<E> getSelection() {
        ObservableSet<E> es = selection.get();
        return es == null ? FXCollections.emptyObservableSet() : es;
    }

    protected abstract @Nullable StylesheetsManager<E> getStyleManager();

    protected TextArea getTextArea() {
        return textArea;
    }

    protected void init(@NonNull URL fxmlUrl) {
        // We must use invoke and wait here, because we instantiate Tooltips
        // which immediately instanciate a Window and a Scene.
        PlatformUtil.invokeAndWait(() -> {
            FXMLLoader loader = new FXMLLoader();
            loader.setResources(InspectorLabels.getResources().asResourceBundle());
            loader.setController(this);

            try (InputStream in = fxmlUrl.openStream()) {
                node = loader.load(in);
            } catch (IOException ex) {
                throw new InternalError(ex);
            }
        });
        Preferences prefs = Preferences.userNodeForPackage(StyleAttributesInspector.class);
        updateContentsCheckBox.setSelected(prefs.getBoolean("updateContents", true));
        updateContentsCheckBox.selectedProperty().addListener((o, oldValue, newValue)
                -> prefs.putBoolean("updateContents", newValue));
        updateSelectorCheckBox.setSelected(prefs.getBoolean("updateSelector", true));
        updateSelectorCheckBox.selectedProperty().addListener((o, oldValue, newValue)
                -> prefs.putBoolean("updateSelector", newValue));
        composeAttributesCheckBox.setSelected(prefs.getBoolean("composeAttributes", true));
        composeAttributesCheckBox.selectedProperty().addListener((o, oldValue, newValue)
                -> prefs.putBoolean("composeAttributes", newValue));
        showUnspecifiedAttributesCheckBox.selectedProperty().addListener((o, oldValue, newValue)
                -> prefs.putBoolean("showUnspecifiedAttributes", newValue));
        showUnspecifiedAttributesCheckBox.setSelected(prefs.getBoolean("showUnspecifiedAttributes", true));

        // XXX Use weak references because of memory leak in JavaFX
        // https://bugs.openjdk.java.net/browse/JDK-8274022
        WeakReference<AbstractStyleAttributesInspector<E>> r = new WeakReference<>(this);
        applyButton.setOnAction(event1 -> {
            AbstractStyleAttributesInspector<E> inspector = r.get();
            if (inspector != null) {
                inspector.apply(event1);
            }
        });
        selectButton.setOnAction(event1 -> {
            AbstractStyleAttributesInspector<E> inspector = r.get();
            if (inspector != null) {
                inspector.select(event1);
            }
        });
        EventHandler<ActionEvent> invalidateTextAreaAction = event -> {
            AbstractStyleAttributesInspector<E> inspector = r.get();
            if (inspector != null) {
                inspector.invalidateTextArea(null);
            }
        };
        showUnspecifiedAttributesCheckBox.setOnAction(invalidateTextAreaAction);
        composeAttributesCheckBox.setOnAction(invalidateTextAreaAction);

        textArea.textProperty().addListener(this::updateLookupTable);
        textArea.caretPositionProperty().addListener(this::onCaretPositionChanged);
        EventHandler<? super KeyEvent> eventHandler = (EventHandler<KeyEvent>) event -> {
            if (event.getCode() == KeyCode.ENTER &&
                    (event.isAltDown() || event.isControlDown())) {
                event.consume();
                apply(null);
            }
        };
        textArea.addEventHandler(KeyEvent.KEY_PRESSED, eventHandler);


        switch (prefs.get("shownValues", "user")) {
        case "author":
            showStylesheetValues.setSelected(true);
            break;
        case "user":
            showAttributeValues.setSelected(true);
            break;
        case "userAgent":
            showUserAgentValues.setSelected(true);
            break;
        case "styled":
        default:
            showAppliedValues.setSelected(true);
            break;
        }
        shownValues.selectedToggleProperty().addListener(o -> {
            AbstractStyleAttributesInspector<E> inspector = r.get();
            if (inspector != null) {
                inspector.updateShownValues(o);
            }
        });
        TextArea textArea = getTextArea();
        textArea.textProperty().addListener(this::updateLookupTable);
        textArea.caretPositionProperty().addListener(this::onCaretPositionChanged);
        textArea.addEventHandler(MouseEvent.MOUSE_CLICKED, this::onTextAreaClicked);
    }

    protected void invalidateTextArea(Observable observable) {
        if (!isApplying && textAreaValid && updateContentsCheckBox.isSelected()) {
            textAreaValid = false;
            if (isShowing()) {
                Platform.runLater(this::validateTextArea);
            }
        }
    }

    public boolean isShowing() {
        return showingProperty().get();
    }

    public void setShowing(boolean newValue) {
        showingProperty().set(newValue);
    }

    protected void onCaretPositionChanged(Observable o, Number oldv, @NonNull Number newv) {
        LookupEntry entry = getLookupEntryAt(newv.intValue());
        Declaration d = entry == null ? null : entry.declaration;
        String helpText = null;
        if (d != null) {
            helpText = helpTexts.get(new QualifiedName(d.getNamespace(), d.getPropertyName()));
        }

        StylesheetsManager<E> sm = getStyleManager();

        String smHelpText = sm.getHelpText();
        if (helpText == null) {
            helpText = smHelpText;
        } else if (smHelpText == null || !smHelpText.isEmpty()) {
            helpText = helpText + "\n\n" + smHelpText;
        }


        setHelpText(helpText);
    }

    private void onTextAreaClicked(@NonNull MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2 && mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED) {
            mouseEvent.consume();
            int caretPosition = getTextArea().getCaretPosition();
            showPicker(caretPosition, mouseEvent.getScreenX(), mouseEvent.getScreenY());
        }
    }

    private SelectorGroup parseSelector() {
        CssParser parser = getCssParserFactoryOrDefault().get();
        try {
            Stylesheet s = parser.parseStylesheet(textArea.getText(), null, null);
            if (!parser.getParseExceptions().isEmpty()) {
                System.err.println("StyleAttributesInspector:\n" + parser.getParseExceptions().toString().replace(',', '\n'));
                return new SelectorGroup(null, Collections.emptyList());
            }
            for (StyleRule styleRule : s.getStyleRules()) {
                return styleRule.getSelectorGroup();
            }

            return new SelectorGroup(null, Collections.emptyList());
        } catch (IOException e) {
            return new SelectorGroup(null, Collections.emptyList());
        }
    }


    protected abstract void recreateHandles();

    protected abstract void remove(E f, WritableStyleableMapAccessor<Object> finalSelectedAccessor);

    private void select(ActionEvent event) {
        CssParser parser = getCssParserFactory().get();
        ;
        try {
            Stylesheet s = parser.parseStylesheet(textArea.getText(), null, null);
            if (!parser.getParseExceptions().isEmpty()) {
                System.err.println("StyleAttributesInspector:\n" + parser.getParseExceptions().toString().replace(',', '\n'));
            }

            ObservableMap<String, Set<E>> pseudoStyles = FXCollections.observableHashMap();
            SequencedSet<E> fs = new LinkedHashSet<>(getSelection());
            pseudoStyles.put("selected", fs);

            List<E> matchedFigures = new ArrayList<>();
            StylesheetsManager<E> sm = getStyleManager();
            SelectorModel<E> fsm = sm.getSelectorModel();
            fsm.additionalPseudoClassStatesProperty().setValue(pseudoStyles);
            for (E f : getEntities()) {
                if (sm.matchesElement(s, f)) {
                    matchedFigures.add(f);
                }
            }

            getSelection().clear();
            getSelection().addAll(matchedFigures);

            showSelection();
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Unexpected Exception " + ex.getMessage(), ex);

        }

    }

    @NonNull SetProperty<E> selectionProperty() {
        return selection;
    }

    protected abstract void set(E f, WritableStyleableMapAccessor<Object> finalSelectedAccessor, Object o);

    protected abstract void setHelpText(String helpText);

    private void showPicker(int caretPosition, double screenX, double screenY) {
        LookupEntry entry = getLookupEntryAt(caretPosition);
        Declaration declaration = entry == null ? null : entry.declaration;
        StyleRule styleRule = entry == null ? null : entry.styleRule;

        if (styleRule != null && declaration != null) {
            ObservableMap<String, Set<E>> pseudoStyles = createPseudoStyles();

            StylesheetsManager<E> sm = getStyleManager();
            if (sm == null) {
                return;
            }
            SelectorModel<E> fsm = sm.getSelectorModel();
            fsm.additionalPseudoClassStatesProperty().setValue(pseudoStyles);
            SequencedSet<E> selectedF = new LinkedHashSet<>();
            WritableStyleableMapAccessor<?> selectedAccessor = null;
            boolean multipleAccessorTypes = false;
            for (E f : getEntities()) {
                if (null != styleRule.getSelectorGroup().matchSelector(fsm, f)) {
                    WritableStyleableMapAccessor<?> accessor = getAccessor(fsm, f, declaration.getNamespace(), declaration.getPropertyName());
                    if (selectedAccessor == null || selectedAccessor == accessor) {
                        selectedAccessor = accessor;
                        selectedF.add(f);
                    } else {
                        multipleAccessorTypes = true;
                    }
                }
            }
            if (!multipleAccessorTypes && selectedAccessor != null && !selectedF.isEmpty()) {
                @SuppressWarnings("unchecked")
                Picker<Object> picker = (Picker<Object>) createAndCachePicker(selectedAccessor);


                Object initialValue = null;
                @SuppressWarnings("unchecked")
                WritableStyleableMapAccessor<Object> finalSelectedAccessor
                        = (WritableStyleableMapAccessor<Object>) selectedAccessor;
                for (E f : selectedF) {
                    initialValue = get(f, finalSelectedAccessor);
                    break;
                }
                BiConsumer<Boolean, Object> lambda = (b, o) -> {
                    undoHelper.startCompositeEdit(null);
                    if (b) {
                        for (E f : selectedF) {
                            AbstractStyleAttributesInspector.this.set(f, finalSelectedAccessor, o);
                        }
                    } else {
                        for (E f : selectedF) {
                            AbstractStyleAttributesInspector.this.remove(f, finalSelectedAccessor);
                        }
                    }
                    invalidateTextArea(null);
                    undoHelper.stopCompositeEdit();
                };
                picker.show(getTextArea(), screenX, screenY,
                        initialValue, lambda);
            }

        }

    }

    /**
     * This method shows the selection in the drawing view, by scrolling
     * the selected elements into the view and "jiggling" the handles.
     * <p>
     * This method is called when the user hits the "select" button.
     */
    protected abstract void showSelection();

    public @NonNull BooleanProperty showingProperty() {
        return showing;
    }

    protected void updateLookupTable(Observable o) {
        lookupTable.clear();
        CssParser parser = getCssParserFactory().get();
        ;
        try {
            Stylesheet s = parser.parseStylesheet(getTextArea().getText(), null, null);
            for (StyleRule r : s.getStyleRules()) {
                for (Declaration d : r.getDeclarations()) {
                    lookupTable.add(new LookupEntry(d.getStartPos(), r, d));
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Unexpected Exception " + ex.getMessage(), ex);

        }
    }

    private SelectorGroup updateSelector(@NonNull Set<E> selection, @NonNull SelectorModel<E> selectorModel) {
        if (updateSelectorCheckBox.isSelected()) {
            return createSelector(selection, selectorModel);
        } else {
            return parseSelector();
        }
    }

    protected void updateShownValues(Observable o) {
        Preferences prefs = Preferences.userNodeForPackage(StyleAttributesInspector.class);
        String origin;
        if (showAttributeValues.isSelected()) {
            origin = "user";
        } else if (showStylesheetValues.isSelected()) {
            origin = "author";
        } else if (showUserAgentValues.isSelected()) {
            origin = "userAgent";
        } else {
            origin = "styled";
        }
        prefs.put("shownValues", origin);

        invalidateTextArea(null);
    }

    private void updateStylesheetInfo(CssPrettyPrinter pp, List<E> matchedFigures, StylesheetsManager<E> styleManager) {
        final List<StylesheetsManager.StylesheetInfo> stylesheets = styleManager.getStylesheets();
        SequencedMap<StylesheetsManager.StylesheetInfo, Set<StyleRule>> matchedInfos = new LinkedHashMap<>();

        final ArrayList<StylesheetsManager.StylesheetInfo> stylesheetInfos = new ArrayList<>();
        for (StylesheetsManager.StylesheetInfo stylesheet : stylesheets) {
            final StyleOrigin origin = stylesheet.getOrigin();
            switch (origin) {

            case USER_AGENT:
                if (showUserAgentValues.isSelected()) {
                    stylesheetInfos.add(stylesheet);
                }
                break;
                case USER, INLINE:
                break;
            case AUTHOR:
                if (showStylesheetValues.isSelected()) {
                    stylesheetInfos.add(stylesheet);
                }
                break;
            }
        }

        if (!stylesheetInfos.isEmpty()) {
            for (E f : matchedFigures) {
                for (StylesheetsManager.StylesheetInfo info : stylesheetInfos) {
                    final List<StyleRule> matchingRules = styleManager.getMatchingRulesForElement(info.getStylesheet(), f);
                    if (!matchingRules.isEmpty()) {
                        matchedInfos.computeIfAbsent(info, k -> new LinkedHashSet<>()).addAll(matchingRules);
                    }
                }
            }
        }
        if (!matchedInfos.isEmpty()) {
            StringBuilder buf = new StringBuilder();
            buf.append("\n/*");
            buf.append("\nThe following stylesheets match:");
            for (Map.Entry<StylesheetsManager.StylesheetInfo, Set<StyleRule>> matchedInfo : matchedInfos.entrySet()) {
                buf.append("\n  ");
                buf.append(matchedInfo.getKey().getOrigin());
                buf.append(": ");
                buf.append(matchedInfo.getKey().getUri().toString());
                buf.append("\n  Rules:");
                for (StyleRule rule : matchedInfo.getValue()) {
                    buf.append("\n    ");
                    rule.getSelectorGroup().produceTokens(token -> buf.append(token.fromToken()));
                    var sourceLocator = rule.getSourceLocator();
                    if (sourceLocator != null && sourceLocator.lineNumber() >= 0) {
                        buf.append(" line: ").append(sourceLocator.lineNumber());
                    }
                }
            }
            buf.append("\n*/");
            pp.append(buf.toString());
        }
    }

    protected void updateTextArea() {
        final boolean decompose = !composeAttributesCheckBox.isSelected();

        // handling of emptyness must be consistent with code in apply() method
        SequencedSet<E> selectedOrRoot = new LinkedHashSet<>(getSelection());
        if (selectedOrRoot.isEmpty()) {
            selectedOrRoot.add(getRoot());
        }

        StylesheetsManager<E> styleManager = getStyleManager();
        ObservableMap<String, Set<E>> pseudoStyles = FXCollections.observableHashMap();
        SequencedSet<E> fs = new LinkedHashSet<>(selectedOrRoot);
        pseudoStyles.put("selected", fs);
        StylesheetsManager<E> sm = getStyleManager();
        if (sm == null) {
            return;
        }
        SelectorModel<E> selectorModel = sm.getSelectorModel();
        selectorModel.additionalPseudoClassStatesProperty().setValue(pseudoStyles);
        SelectorGroup selector = updateSelector(selectedOrRoot, selectorModel);

        List<E> matchedFigures;
        if (updateSelectorCheckBox.isSelected()) {
            matchedFigures = new ArrayList<>(getSelection());
        } else {
            matchedFigures =
                    StreamSupport.stream(getEntities().spliterator(), true).filter(entity ->
                            selector.matches(selectorModel, entity)).collect(Collectors.toList());
        }


        collectHelpTexts(selectedOrRoot);
        Map<QualifiedName, String> attr = collectAttributeValues(decompose, matchedFigures, selectorModel);

        StringBuilder buf = new StringBuilder();
        CssPrettyPrinter pp = new CssPrettyPrinter(buf);
        selector.produceTokens(t -> pp.append(t.fromToken()));
        pp.append(" {");
        for (Map.Entry<QualifiedName, String> a : attr.entrySet()) {
            pp.append("\n  ").append(a.getKey().name()).append(": ");
            pp.append(a.getValue());
            pp.append(";");
        }
        pp.append("\n}");

        updateStylesheetInfo(pp, matchedFigures, styleManager);

        textArea.setText(buf.toString());
        int rows = 1;
        for (int i = 0; i < buf.length(); i++) {
            if (buf.charAt(i) == '\n') {
                rows++;
            }
        }
        textArea.setPrefRowCount(Math.min(Math.max(5, rows), 25));
    }

    private void validateTextArea() {
        if (!textAreaValid) {
            if (updateContentsCheckBox.isSelected()) {
                updateTextArea();
            }
            textAreaValid = true;
        }
    }

    public @Nullable Supplier<CssParser> getCssParserFactory() {
        return cssParserFactory.get();
    }

    public @NonNull Supplier<CssParser> getCssParserFactoryOrDefault() {
        var s = cssParserFactory.get();
        return s == null ? CssParser::new : s;
    }

    public void setCssParserFactory(@Nullable Supplier<CssParser> cssParserFactory) {
        this.cssParserFactory.set(cssParserFactory);
    }

    public @NonNull ObjectProperty<Supplier<CssParser>> cssParserFactoryProperty() {
        return cssParserFactory;
    }

    private record LookupEntry(int position, @NonNull StyleRule styleRule,
                               @NonNull Declaration declaration) implements Comparable<LookupEntry> {

        @Override
            public int compareTo(@NonNull LookupEntry o) {
                return this.position - o.position;
            }

        }
}
