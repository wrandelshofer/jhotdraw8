/*
 * @(#)ColorChooserPaneModel.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxcontrols.colorchooser;

import javafx.beans.Observable;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.color.A98RgbColorSpace;
import org.jhotdraw8.color.CieLabColorSpace;
import org.jhotdraw8.color.DisplayP3ColorSpace;
import org.jhotdraw8.color.NamedColor;
import org.jhotdraw8.color.NamedColorSpace;
import org.jhotdraw8.color.OKHlsColorSpace;
import org.jhotdraw8.color.OKLabColorSpace;
import org.jhotdraw8.color.OKLchColorSpace;
import org.jhotdraw8.color.ParametricHlsColorSpace;
import org.jhotdraw8.color.ParametricHsvColorSpace;
import org.jhotdraw8.color.ParametricLchColorSpace;
import org.jhotdraw8.color.ProPhotoRgbColorSpace;
import org.jhotdraw8.color.Rec2020ColorSpace;
import org.jhotdraw8.color.RgbBitConverters;
import org.jhotdraw8.color.SrgbColorSpace;

import java.awt.color.ColorSpace;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;

/**
 * Data flow:
 * <pre>
 *     colorChooser + targetColorSpace
 *                  |
 *                  v
 *           chooserColorSpace
 *
 *     c0,c1,c2,c3,alpha     +   chooserColorSpace,
 *                           |
 *                           v
 *                      chooserColor
 *
 *    chooserColor + targetColorSpace
 *                 |
 *                 v
 *            targetColor
 *
 *    chooserColor + displayColorSpace + displayBitDepth
 *                 |
 *                 v
 *            displayColor
 * </pre>
 */
public class ColorChooserPaneModel {
    public enum ColorSyntax {
        HEX_COLOR,
        NAMED_COLOR,
        RGB_FUNCTION,
        HSL_FUNCTION,
        HWB_FUNCTION,
        LAB_FUNCTION,
        LCH_FUNCTION,
        COLOR_FUNCTION,
    }

    public enum ChooserType {
        OK_HSL_SRGB,
        SLIDERS,
        SWATCHES,
        HSL_SRGB,
        HSV_SRGB,
        OK_LCH,
        CIE_LCH,
    }

    public final @NonNull FloatProperty c0 = new SimpleFloatProperty(this, "c0");
    public final @NonNull IntegerProperty sourceColorSpaceHueIndex = new SimpleIntegerProperty(this, "targetColorSpaceHueIndex");
    public final @NonNull IntegerProperty sourceColorSpaceSaturationChromaIndex = new SimpleIntegerProperty(this, "targetColorSaturationChromaIndex");
    public final @NonNull IntegerProperty sourceColorSpaceLightnessValueIndex = new SimpleIntegerProperty(this, "targetColorLuminanceChromaIndex");
    public final @NonNull FloatProperty c1 = new SimpleFloatProperty(this, "c1");
    public final @NonNull FloatProperty c2 = new SimpleFloatProperty(this, "c2");
    public final @NonNull FloatProperty c3 = new SimpleFloatProperty(this, "c3");
    public final @NonNull FloatProperty alpha = new SimpleFloatProperty(this, "alpha");
    public final @NonNull ObjectProperty<NamedColorSpace> sourceColorSpace = new SimpleObjectProperty<>(this, "chooserColorSpace");
    public final @NonNull ObjectProperty<NamedColorSpace> targetColorSpace = new SimpleObjectProperty<>(this, "targetColorSpace");
    public final @NonNull ListProperty<NamedColorSpace> targetColorSpaces = new SimpleListProperty<>(this, "targetColorSpaces", FXCollections.observableArrayList());
    public final @NonNull ListProperty<NamedColorSpace> displayColorSpaces = new SimpleListProperty<>(this, "displayColorSpaces", FXCollections.observableArrayList());
    public final @NonNull ListProperty<Map.Entry<String, ToIntFunction<Integer>>> displayBitDepths = new SimpleListProperty<>(this, "displayBitDepths", FXCollections.observableArrayList());

    public final @NonNull ObjectProperty<ColorSyntax> targetColorSyntax = new SimpleObjectProperty<>(this, "ColorSyntax");
    public final @NonNull ListProperty<ColorSyntax> targetColorSyntaxes = new SimpleListProperty<>(this, "ColorSyntaxes", FXCollections.observableArrayList());
    public final @NonNull ObjectProperty<ChooserType> chooserType = new SimpleObjectProperty<>(this, "ChooserType");
    public final @NonNull ListProperty<ChooserType> chooserTypes = new SimpleListProperty<>(this, "ChooserTypes", FXCollections.observableArrayList());
    public final @NonNull ObjectProperty<NamedColorSpace> displayColorSpace = new SimpleObjectProperty<>(this, "displayColorSpace");
    public final @NonNull ObjectProperty<NamedColor> chooserColor = new SimpleObjectProperty<>(this, "chooserColor");
    public final @NonNull ObjectProperty<NamedColor> targetColor = new SimpleObjectProperty<>(this, "targetColor");
    public final @NonNull ObjectProperty<NamedColor> displayColor = new SimpleObjectProperty<>(this, "displayColor");
    public final @NonNull ObjectProperty<Color> previewColor = new SimpleObjectProperty<>(this, "previewColor");
    public final @NonNull ObjectProperty<Map.Entry<String, ToIntFunction<Integer>>> displayBitDepth = new SimpleObjectProperty(this, "displayBitDepth");

    public void initWithDefaultValues() {
        SrgbColorSpace srgbColorSpace = new SrgbColorSpace();
        DisplayP3ColorSpace displayP3ColorSpace = new DisplayP3ColorSpace();
        ProPhotoRgbColorSpace proPhotoRgbColorSpace = new ProPhotoRgbColorSpace();
        Rec2020ColorSpace rec2020ColorSpace = new Rec2020ColorSpace();
        displayColorSpaces.get().setAll(
                srgbColorSpace,
                displayP3ColorSpace,
                // new A98RgbColorSpace(), -> not a display
                rec2020ColorSpace,
                proPhotoRgbColorSpace
        );
        targetColorSpaces.get().setAll(
                srgbColorSpace,
                displayP3ColorSpace,
                rec2020ColorSpace,
                proPhotoRgbColorSpace,
                new OKLabColorSpace(),
                new CieLabColorSpace(),
                new A98RgbColorSpace()
        );
        targetColorSyntaxes.get().setAll(ColorSyntax.values());
        targetColorSyntax.set(ColorSyntax.HEX_COLOR);
        chooserTypes.get().setAll(ChooserType.values());
        chooserType.set(ChooserType.OK_HSL_SRGB);
        displayColorSpace.set(srgbColorSpace);
        targetColorSpace.set(srgbColorSpace);
        alpha.set(1f);
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
        displayBitDepths.get().setAll(map.entrySet());
        displayBitDepth.set(new AbstractMap.SimpleImmutableEntry<>("24", argb -> argb));
    }

    public float getC0() {
        return c0.get();
    }

    public @NonNull FloatProperty c0Property() {
        return c0;
    }

    public void setC0(float c0) {
        this.c0.set(c0);
    }

    public float getC1() {
        return c1.get();
    }

    public @NonNull FloatProperty c1Property() {
        return c1;
    }

    public void setC1(float c1) {
        this.c1.set(c1);
    }

    public float getC2() {
        return c2.get();
    }

    public @NonNull FloatProperty c2Property() {
        return c2;
    }

    public void setC2(float c2) {
        this.c2.set(c2);
    }

    public float getC3() {
        return c3.get();
    }

    public @NonNull FloatProperty c3Property() {
        return c3;
    }

    public void setC3(float c3) {
        this.c3.set(c3);
    }

    public float getAlpha() {
        return alpha.get();
    }

    public @NonNull FloatProperty alphaProperty() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha.set(alpha);
    }

    public NamedColorSpace getSourceColorSpace() {
        return sourceColorSpace.get();
    }

    public @NonNull ObjectProperty<NamedColorSpace> sourceColorSpaceProperty() {
        return sourceColorSpace;
    }

    public void setSourceColorSpace(NamedColorSpace sourceColorSpace) {
        this.sourceColorSpace.set(sourceColorSpace);
    }

    public NamedColorSpace getTargetColorSpace() {
        return targetColorSpace.get();
    }

    public @NonNull ObjectProperty<NamedColorSpace> targetColorSpaceProperty() {
        return targetColorSpace;
    }

    public void setTargetColorSpace(NamedColorSpace targetColorSpace) {
        this.targetColorSpace.set(targetColorSpace);
    }

    public ColorSyntax getTargetColorSyntax() {
        return targetColorSyntax.get();
    }

    public @NonNull ObjectProperty<ColorSyntax> targetColorSyntaxProperty() {
        return targetColorSyntax;
    }

    public void setTargetColorSyntax(ColorSyntax targetColorSyntax) {
        this.targetColorSyntax.set(targetColorSyntax);
    }

    public NamedColorSpace getDisplayColorSpace() {
        return displayColorSpace.get();
    }

    public @NonNull ObjectProperty<NamedColorSpace> displayColorSpaceProperty() {
        return displayColorSpace;
    }

    public void setDisplayColorSpace(NamedColorSpace displayColorSpace) {
        this.displayColorSpace.set(displayColorSpace);
    }

    public NamedColor getChooserColor() {
        return chooserColor.get();
    }

    public @NonNull ObjectProperty<NamedColor> chooserColorProperty() {
        return chooserColor;
    }

    public void setChooserColor(NamedColor chooserColor) {
        this.chooserColor.set(chooserColor);
    }

    public NamedColor getTargetColor() {
        return targetColor.get();
    }

    public @NonNull ObjectProperty<NamedColor> targetColorProperty() {
        return targetColor;
    }

    public void setTargetColor(NamedColor targetColor) {
        this.targetColor.set(targetColor);
    }

    public NamedColor getDisplayColor() {
        return displayColor.get();
    }

    public @NonNull ObjectProperty<NamedColor> displayColorProperty() {
        return displayColor;
    }

    public void setDisplayColor(NamedColor displayColor) {
        this.displayColor.set(displayColor);
    }


    public ObservableList<NamedColorSpace> getTargetColorSpaces() {
        return targetColorSpaces.get();
    }

    public @NonNull ListProperty<NamedColorSpace> targetColorSpacesProperty() {
        return targetColorSpaces;
    }

    public void setTargetColorSpaces(ObservableList<NamedColorSpace> targetColorSpaces) {
        this.targetColorSpaces.set(targetColorSpaces);
    }

    public ColorChooserPaneModel() {
        sourceColorSpace.addListener(this::updateSourceColorSpaceProperties);
        chooserType.addListener(this::updateSourceColorSpace);
        targetColorSpace.addListener(this::updateSourceColorSpace);
        ChangeListener<? super Object> changeListener = this.<ChangeListener<? super Object>>ref(this::updateTargetColor);
        c0.addListener(changeListener);
        c1.addListener(changeListener);
        c2.addListener(changeListener);
        c3.addListener(changeListener);
        alpha.addListener(changeListener);
        targetColorSpace.addListener(changeListener);
        displayColorSpace.addListener(changeListener);
        displayBitDepth.addListener(changeListener);
    }

    private <T> T ref(T binding) {
        refs.add(binding);
        return binding;
    }

    private final List<Object> refs = new ArrayList<>();

    private void updateSourceColorSpace(Observable o, Object oldv, Object newv) {
        ChooserType ct = chooserType.get();
        final NamedColorSpace tcs = targetColorSpace.get();
        if (ct == null || tcs == null) {
            sourceColorSpace.set(null);
            return;
        }
        switch (ct) {
            case OK_HSL_SRGB -> {
                sourceColorSpace.set(new OKHlsColorSpace());
            }
            case SLIDERS, SWATCHES -> {
                sourceColorSpace.set(tcs);
            }
            case HSL_SRGB -> {
                sourceColorSpace.set(new ParametricHlsColorSpace("HSL", new SrgbColorSpace()));
            }
            case HSV_SRGB -> {
                sourceColorSpace.set(new ParametricHsvColorSpace("HSV", new SrgbColorSpace()));
            }
            case CIE_LCH -> {
                sourceColorSpace.set(new ParametricLchColorSpace("LCH", new CieLabColorSpace()));
            }
            case OK_LCH -> {
                sourceColorSpace.set(new OKLchColorSpace());
            }
            default -> sourceColorSpace.set(tcs);
        }
    }

    private void updateSourceColorSpaceProperties(Observable o, Object oldv, Object newv) {
        NamedColorSpace ncs = sourceColorSpace.get();
        if (ncs == null) return;
        switch (ncs.getType()) {
            default -> {
                sourceColorSpaceHueIndex.set(0);
                sourceColorSpaceSaturationChromaIndex.set(1);
                sourceColorSpaceLightnessValueIndex.set(2);
            }
            case ColorSpace.TYPE_HLS -> {
                sourceColorSpaceHueIndex.set(0);
                sourceColorSpaceSaturationChromaIndex.set(2);
                sourceColorSpaceLightnessValueIndex.set(1);
            }
            case NamedColorSpace.TYPE_LCH -> {
                sourceColorSpaceHueIndex.set(2);
                sourceColorSpaceSaturationChromaIndex.set(1);
                sourceColorSpaceLightnessValueIndex.set(0);
            }
        }
    }

    private void updateTargetColor(Observable o, Object oldv, Object newv) {
        float[] component = {c0.floatValue(), c1.floatValue(), c2.floatValue()};
        float[] rgb = new float[3];
        NamedColorSpace displayCs = displayColorSpace.get();
        NamedColorSpace sourceCs = sourceColorSpace.get();
        NamedColorSpace targetCs = targetColorSpace.get();
        if (displayCs == null || targetCs == null) return;
        Map.Entry<String, ToIntFunction<Integer>> entry = displayBitDepth.get();
        displayCs.fromRGB(sourceCs.toRGB(component, rgb), rgb);
        int value = RgbBitConverters.rgbFloatToArgb32(rgb, alpha.floatValue());
        int argb = entry == null ? value : entry.getValue().applyAsInt(value);
        Color previewColorValue = Color.rgb((argb >>> 16) & 0xff, (argb >>> 8) & 0xff, (argb) & 0xff, alpha.floatValue());
        String hexStr = "00000000" + Integer.toHexString(argb);
        hexStr = hexStr.substring(hexStr.length() - 8);
        previewColor.set(previewColorValue);
        //hexRgbLabel.setText("#" + hexStr.substring(2) + hexStr.substring(0, 2));
    }

    public int getSourceColorSpaceHueIndex() {
        return sourceColorSpaceHueIndex.get();
    }

    public @NonNull IntegerProperty sourceColorSpaceHueIndexProperty() {
        return sourceColorSpaceHueIndex;
    }

    public void setSourceColorSpaceHueIndex(int sourceColorSpaceHueIndex) {
        this.sourceColorSpaceHueIndex.set(sourceColorSpaceHueIndex);
    }

    public int getSourceColorSpaceSaturationChromaIndex() {
        return sourceColorSpaceSaturationChromaIndex.get();
    }

    public @NonNull IntegerProperty sourceColorSpaceSaturationChromaIndexProperty() {
        return sourceColorSpaceSaturationChromaIndex;
    }

    public void setSourceColorSpaceSaturationChromaIndex(int sourceColorSpaceSaturationChromaIndex) {
        this.sourceColorSpaceSaturationChromaIndex.set(sourceColorSpaceSaturationChromaIndex);
    }

    public int getSourceColorSpaceLightnessValueIndex() {
        return sourceColorSpaceLightnessValueIndex.get();
    }

    public @NonNull IntegerProperty sourceColorSpaceLightnessValueIndexProperty() {
        return sourceColorSpaceLightnessValueIndex;
    }

    public void setSourceColorSpaceLightnessValueIndex(int sourceColorSpaceLightnessValueIndex) {
        this.sourceColorSpaceLightnessValueIndex.set(sourceColorSpaceLightnessValueIndex);
    }

    public ObservableList<NamedColorSpace> getDisplayColorSpaces() {
        return displayColorSpaces.get();
    }

    public @NonNull ListProperty<NamedColorSpace> displayColorSpacesProperty() {
        return displayColorSpaces;
    }

    public void setDisplayColorSpaces(ObservableList<NamedColorSpace> displayColorSpaces) {
        this.displayColorSpaces.set(displayColorSpaces);
    }

    public ObservableList<Map.Entry<String, ToIntFunction<Integer>>> getDisplayBitDepths() {
        return displayBitDepths.get();
    }

    public @NonNull ListProperty<Map.Entry<String, ToIntFunction<Integer>>> displayBitDepthsProperty() {
        return displayBitDepths;
    }

    public void setDisplayBitDepths(ObservableList<Map.Entry<String, ToIntFunction<Integer>>> displayBitDepths) {
        this.displayBitDepths.set(displayBitDepths);
    }

    public Map.Entry<String, ToIntFunction<Integer>> getDisplayBitDepth() {
        return displayBitDepth.get();
    }

    public @NonNull ObjectProperty<Map.Entry<String, ToIntFunction<Integer>>> displayBitDepthProperty() {
        return displayBitDepth;
    }

    public void setDisplayBitDepth(Map.Entry<String, ToIntFunction<Integer>> displayBitDepth) {
        this.displayBitDepth.set(displayBitDepth);
    }

    public Color getPreviewColor() {
        return previewColor.get();
    }

    public @NonNull ObjectProperty<Color> previewColorProperty() {
        return previewColor;
    }

    public void setPreviewColor(Color previewColor) {
        this.previewColor.set(previewColor);
    }

    public ObservableList<ColorSyntax> getTargetColorSyntaxes() {
        return targetColorSyntaxes.get();
    }

    public @NonNull ListProperty<ColorSyntax> targetColorSyntaxesProperty() {
        return targetColorSyntaxes;
    }

    public void setTargetColorSyntaxes(ObservableList<ColorSyntax> targetColorSyntaxes) {
        this.targetColorSyntaxes.set(targetColorSyntaxes);
    }

    public ObservableList<ChooserType> getColorChoosers() {
        return chooserTypes.get();
    }

    public @NonNull ListProperty<ChooserType> colorChoosersProperty() {
        return chooserTypes;
    }

    public void setColorChoosers(ObservableList<ChooserType> chooserTypes) {
        this.chooserTypes.set(chooserTypes);
    }

    public ChooserType getChooserType() {
        return chooserType.get();
    }

    public @NonNull ObjectProperty<ChooserType> chooserTypeProperty() {
        return chooserType;
    }

    public void setChooserType(ChooserType chooserType) {
        this.chooserType.set(chooserType);
    }
}
