/*
 * @(#)AbstractColorSlidersMain.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.examples.colorchooser;

import javafx.application.Application;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;
import javafx.util.converter.FloatStringConverter;
import org.jhotdraw8.color.NamedColorSpace;
import org.jhotdraw8.color.RgbBitConverters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

/**
 * Abstract base class for color slider examples.
 */
public abstract class AbstractColorSlidersMain extends Application {
    /**
     * Constructs a new instance.
     */
    public AbstractColorSlidersMain() {
    }

    protected ComboBox<ToIntFunction<Integer>> createBitDepthComboBox() {
        ComboBox<ToIntFunction<Integer>> comboBox = new ComboBox<>();
        var map = new LinkedHashMap<String, ToIntFunction<Integer>>();
        map.put("24", argb -> argb);
        map.put("16", argb ->
                (argb & 0xff_00_00_00)
                        | RgbBitConverters.rgb16to24(RgbBitConverters.rgb24to16(argb)));
        map.put("15", argb ->
                (argb & 0xff_00_00_00)
                        | RgbBitConverters.rgb15to24(RgbBitConverters.rgb24to15(argb)));
        map.put("12", argb ->
                (argb & 0xff_00_00_00)
                        | RgbBitConverters.rgb12to24(RgbBitConverters.rgb24to12(argb)));
        map.put("6", argb ->
                (argb & 0xff_00_00_00)
                        | RgbBitConverters.rgb6to24(RgbBitConverters.rgb24to6(argb)));

        Map<ToIntFunction<Integer>, String> inverseMap = map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        comboBox.setItems(FXCollections.observableArrayList(map.values()));
        comboBox.setValue(map.values().iterator().next());
        comboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(ToIntFunction<Integer> object) {
                return inverseMap.get(object);
            }

            @Override
            public ToIntFunction<Integer> fromString(String string) {
                return map.get(string);
            }
        });
        return comboBox;
    }

    /**
     * Creates a combo box for choosing a color space from the provided set.
     *
     * @param colorSpaces a set of color spaces
     * @return a combo box
     */
    protected ComboBox<NamedColorSpace> createColorSpaceComboBox(NamedColorSpace... colorSpaces) {
        Map<String, NamedColorSpace> map =
                Arrays.stream(colorSpaces)
                        .collect(Collectors.toMap(NamedColorSpace::getName, Function.identity(), (a, b) -> b, LinkedHashMap::new));

        ComboBox<NamedColorSpace> comboBox = new ComboBox<>(FXCollections.observableArrayList(
                map.values())
        );
        comboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(NamedColorSpace object) {
                return object.getName();
            }

            @Override
            public NamedColorSpace fromString(String string) {
                return map.get(string);
            }
        });
        comboBox.setValue(comboBox.getItems().getFirst());
        return comboBox;
    }

    /**
     * Creates text fields for the specified component properties.
     *
     * @param components properties holding component values
     * @return text fields
     */
    protected List<TextField> createTextFields(FloatProperty... components) {
        List<TextField> fields = new ArrayList<>(components.length);
        for (FloatProperty c : components) {
            TextField field = new TextField();
            field.setPrefColumnCount(8);
            TextFormatter<Float> formatter = new TextFormatter<>(new FloatStringConverter());
            field.setTextFormatter(formatter);
            ObjectProperty<Float> asObj = c.asObject();
            field.getProperties().put("asObj", asObj);
            formatter.valueProperty().bindBidirectional(asObj);
            fields.add(field);
        }
        return fields;
    }
}
