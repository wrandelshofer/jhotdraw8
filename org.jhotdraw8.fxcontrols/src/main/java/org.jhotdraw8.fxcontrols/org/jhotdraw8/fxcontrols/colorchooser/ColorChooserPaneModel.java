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
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.converter.FloatConverter;
import org.jhotdraw8.color.A98RgbColorSpace;
import org.jhotdraw8.color.CieLabColorSpace;
import org.jhotdraw8.color.D50XyzColorSpace;
import org.jhotdraw8.color.D65XyzColorSpace;
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.ToIntFunction;

import static org.jhotdraw8.color.util.MathUtil.clamp;

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
 *
 *   chooserColorSpace
 *         |
 *         v
 *     hueSliderLightness
 * </pre>
 */
public class ColorChooserPaneModel {
    public static final CieLabColorSpace CIE_LAB_COLOR_SPACE = new CieLabColorSpace();
    public static final ParametricLchColorSpace CIE_LCH_COLOR_SPACE = new ParametricLchColorSpace("CIE LCH", CIE_LAB_COLOR_SPACE);
    public static final OKLabColorSpace OK_LAB_COLOR_SPACE = new OKLabColorSpace();
    public static final OKLchColorSpace OK_LCH_COLOR_SPACE = new OKLchColorSpace();
    private final static FloatConverter number = new FloatConverter();

    private final static FloatConverter percentageNumber = new FloatConverter();

    public final @NonNull FloatProperty alpha = new SimpleFloatProperty(this, "alpha");
    public final @NonNull FloatProperty c0 = new SimpleFloatProperty(this, "c0");
    public final @NonNull FloatProperty c1 = new SimpleFloatProperty(this, "c1");
    public final @NonNull FloatProperty c2 = new SimpleFloatProperty(this, "c2");
    public final @NonNull FloatProperty hue = new SimpleFloatProperty(this, "hue");
    public final @NonNull FloatProperty hueSliderC0 = new SimpleFloatProperty(this, "hueSliderC0", 0.125f);
    public final @NonNull FloatProperty hueSliderC1 = new SimpleFloatProperty(this, "hueSliderC1", 0.125f);
    public final @NonNull FloatProperty hueSliderC2 = new SimpleFloatProperty(this, "hueSliderC2", 0.5f);
    public final @NonNull FloatProperty chroma = new SimpleFloatProperty(this, "chroma");
    public final @NonNull FloatProperty lightness = new SimpleFloatProperty(this, "lightness");
    public final @NonNull FloatProperty c3 = new SimpleFloatProperty(this, "c3");
    public final @NonNull ObjectProperty<NamedColor> sourceColor = new SimpleObjectProperty<>(this, "chooserColor");
    public final @NonNull ObjectProperty<ChooserType> chooserType = new SimpleObjectProperty<>(this, "ChooserType");
    public final @NonNull ListProperty<ChooserType> chooserTypes = new SimpleListProperty<>(this, "ChooserTypes", FXCollections.observableArrayList());
    public final @NonNull ObjectProperty<Map.Entry<String, ToIntFunction<Integer>>> displayBitDepth = new SimpleObjectProperty<>(this, "displayBitDepth");
    public final @NonNull ListProperty<Map.Entry<String, ToIntFunction<Integer>>> displayBitDepths = new SimpleListProperty<>(this, "displayBitDepths", FXCollections.observableArrayList());
    public final @NonNull ObjectProperty<NamedColor> displayColor = new SimpleObjectProperty<>(this, "displayColor");
    public final @NonNull ObjectProperty<NamedColorSpace> displayColorSpace = new SimpleObjectProperty<>(this, "displayColorSpace");
    public final @NonNull ListProperty<NamedColorSpace> displayColorSpaces = new SimpleListProperty<>(this, "displayColorSpaces", FXCollections.observableArrayList());
    public final @NonNull ObjectProperty<Color> previewColor = new SimpleObjectProperty<>(this, "previewColor");
    public final @NonNull StringProperty sourceColorField = new SimpleStringProperty(this, "sourceColorField");
    public final @NonNull StringProperty targetColorField = new SimpleStringProperty(this, "targetColorField");
    public final @NonNull StringProperty displayColorField = new SimpleStringProperty(this, "displayColorField");
    public final @NonNull ObjectProperty<NamedColorSpace> sourceColorSpace = new SimpleObjectProperty<>(this, "chooserColorSpace");
    public final @NonNull IntegerProperty sourceColorSpaceHueIndex = new SimpleIntegerProperty(this, "sourceColorSpaceHueIndex");
    public final @NonNull IntegerProperty sourceColorSpaceLightnessValueIndex = new SimpleIntegerProperty(this, "sourceColorSpaceLightnessValueIndex");
    public final @NonNull IntegerProperty sourceColorSpaceSaturationChromaIndex = new SimpleIntegerProperty(this, "sourceColorSpaceSaturationChromaIndex");
    public final @NonNull ObjectProperty<NamedColor> targetColor = new SimpleObjectProperty<>(this, "targetColor");
    public final @NonNull ObjectProperty<NamedColorSpace> targetColorSpace = new SimpleObjectProperty<>(this, "targetColorSpace");
    public final @NonNull ListProperty<NamedColorSpace> targetColorSpaces = new SimpleListProperty<>(this, "targetColorSpaces", FXCollections.observableArrayList());
    public final @NonNull ObjectProperty<ColorSyntax> targetColorSyntax = new SimpleObjectProperty<>(this, "ColorSyntax");
    public final @NonNull ListProperty<ColorSyntax> targetColorSyntaxes = new SimpleListProperty<>(this, "ColorSyntaxes", FXCollections.observableArrayList());

    public ColorChooserPaneModel() {
        sourceColorSpace.addListener(this::updateSourceColorSpaceProperties);
        chooserType.addListener(this::updateSourceColorSpace);
        targetColorSpace.addListener(this::updateSourceColorSpace);

        ChangeListener<? super Object> changeListener = (o, oldv, newv) -> {
            updateDisplayColor(o, oldv, newv);
            updateTargetColorField(o, oldv, newv);
            updateSourceColorField(o, oldv, newv);
        };
        c0.addListener(changeListener);
        c1.addListener(changeListener);
        c2.addListener(changeListener);
        c3.addListener(changeListener);
        alpha.addListener(changeListener);
        targetColorSpace.addListener(changeListener);
        displayColorSpace.addListener(changeListener);
        displayBitDepth.addListener(changeListener);
        targetColorSyntax.addListener(changeListener);
    }

    public @NonNull FloatProperty alphaProperty() {
        return alpha;
    }

    public @NonNull FloatProperty c0Property() {
        return c0;
    }

    public @NonNull FloatProperty c1Property() {
        return c1;
    }

    public @NonNull FloatProperty c2Property() {
        return c2;
    }

    public @NonNull FloatProperty c3Property() {
        return c3;
    }

    public @NonNull ObjectProperty<NamedColor> sourceColorProperty() {
        return sourceColor;
    }

    public @NonNull ObjectProperty<ChooserType> chooserTypeProperty() {
        return chooserType;
    }

    public @NonNull ListProperty<ChooserType> colorChoosersProperty() {
        return chooserTypes;
    }

    public @NonNull ObjectProperty<Map.Entry<String, ToIntFunction<Integer>>> displayBitDepthProperty() {
        return displayBitDepth;
    }

    public @NonNull ListProperty<Map.Entry<String, ToIntFunction<Integer>>> displayBitDepthsProperty() {
        return displayBitDepths;
    }

    public @NonNull ObjectProperty<NamedColor> displayColorProperty() {
        return displayColor;
    }

    public @NonNull ObjectProperty<NamedColorSpace> displayColorSpaceProperty() {
        return displayColorSpace;
    }

    public @NonNull ListProperty<NamedColorSpace> displayColorSpacesProperty() {
        return displayColorSpaces;
    }

    public float getAlpha() {
        return alpha.get();
    }

    public void setAlpha(float alpha) {
        this.alpha.set(alpha);
    }

    public float getC0() {
        return c0.get();
    }

    public void setC0(float c0) {
        this.c0.set(c0);
    }

    public float getC1() {
        return c1.get();
    }

    public void setC1(float c1) {
        this.c1.set(c1);
    }

    public float getC2() {
        return c2.get();
    }

    public void setC2(float c2) {
        this.c2.set(c2);
    }

    public float getC3() {
        return c3.get();
    }

    public void setC3(float c3) {
        this.c3.set(c3);
    }

    public NamedColor getSourceColor() {
        return sourceColor.get();
    }

    public void setSourceColor(NamedColor sourceColor) {
        this.sourceColor.set(sourceColor);
    }

    public ChooserType getChooserType() {
        return chooserType.get();
    }

    public void setChooserType(ChooserType chooserType) {
        this.chooserType.set(chooserType);
    }

    public ObservableList<ChooserType> getColorChoosers() {
        return chooserTypes.get();
    }

    public void setColorChoosers(ObservableList<ChooserType> chooserTypes) {
        this.chooserTypes.set(chooserTypes);
    }

    public Map.Entry<String, ToIntFunction<Integer>> getDisplayBitDepth() {
        return displayBitDepth.get();
    }

    public void setDisplayBitDepth(Map.Entry<String, ToIntFunction<Integer>> displayBitDepth) {
        this.displayBitDepth.set(displayBitDepth);
    }

    public ObservableList<Map.Entry<String, ToIntFunction<Integer>>> getDisplayBitDepths() {
        return displayBitDepths.get();
    }

    public void setDisplayBitDepths(ObservableList<Map.Entry<String, ToIntFunction<Integer>>> displayBitDepths) {
        this.displayBitDepths.set(displayBitDepths);
    }

    public NamedColor getDisplayColor() {
        return displayColor.get();
    }

    public void setDisplayColor(NamedColor displayColor) {
        this.displayColor.set(displayColor);
    }

    public NamedColorSpace getDisplayColorSpace() {
        return displayColorSpace.get();
    }

    public void setDisplayColorSpace(NamedColorSpace displayColorSpace) {
        this.displayColorSpace.set(displayColorSpace);
    }

    public ObservableList<NamedColorSpace> getDisplayColorSpaces() {
        return displayColorSpaces.get();
    }

    public void setDisplayColorSpaces(ObservableList<NamedColorSpace> displayColorSpaces) {
        this.displayColorSpaces.set(displayColorSpaces);
    }

    public Color getPreviewColor() {
        return previewColor.get();
    }

    public void setPreviewColor(Color previewColor) {
        this.previewColor.set(previewColor);
    }

    public String getSourceColorField() {
        return sourceColorField.get();
    }

    public void setSourceColorField(String sourceColorField) {
        this.sourceColorField.set(sourceColorField);
    }

    public NamedColorSpace getSourceColorSpace() {
        return sourceColorSpace.get();
    }

    public void setSourceColorSpace(NamedColorSpace sourceColorSpace) {
        this.sourceColorSpace.set(sourceColorSpace);
    }

    public int getSourceColorSpaceHueIndex() {
        return sourceColorSpaceHueIndex.get();
    }

    public void setSourceColorSpaceHueIndex(int sourceColorSpaceHueIndex) {
        this.sourceColorSpaceHueIndex.set(sourceColorSpaceHueIndex);
    }

    public int getSourceColorSpaceLightnessValueIndex() {
        return sourceColorSpaceLightnessValueIndex.get();
    }

    public void setSourceColorSpaceLightnessValueIndex(int sourceColorSpaceLightnessValueIndex) {
        this.sourceColorSpaceLightnessValueIndex.set(sourceColorSpaceLightnessValueIndex);
    }

    public int getSourceColorSpaceSaturationChromaIndex() {
        return sourceColorSpaceSaturationChromaIndex.get();
    }

    public void setSourceColorSpaceSaturationChromaIndex(int sourceColorSpaceSaturationChromaIndex) {
        this.sourceColorSpaceSaturationChromaIndex.set(sourceColorSpaceSaturationChromaIndex);
    }

    public NamedColor getTargetColor() {
        return targetColor.get();
    }

    public void setTargetColor(NamedColor targetColor) {
        this.targetColor.set(targetColor);
    }

    public NamedColorSpace getTargetColorSpace() {
        return targetColorSpace.get();
    }

    public void setTargetColorSpace(NamedColorSpace targetColorSpace) {
        this.targetColorSpace.set(targetColorSpace);
    }

    public ObservableList<NamedColorSpace> getTargetColorSpaces() {
        return targetColorSpaces.get();
    }

    public void setTargetColorSpaces(ObservableList<NamedColorSpace> targetColorSpaces) {
        this.targetColorSpaces.set(targetColorSpaces);
    }

    public ColorSyntax getTargetColorSyntax() {
        return targetColorSyntax.get();
    }

    public void setTargetColorSyntax(ColorSyntax targetColorSyntax) {
        this.targetColorSyntax.set(targetColorSyntax);
    }

    public ObservableList<ColorSyntax> getTargetColorSyntaxes() {
        return targetColorSyntaxes.get();
    }

    public void setTargetColorSyntaxes(ObservableList<ColorSyntax> targetColorSyntaxes) {
        this.targetColorSyntaxes.set(targetColorSyntaxes);
    }

    private final static SrgbColorSpace SRGB_COLOR_SPACE = new SrgbColorSpace();
    private final static ParametricHlsColorSpace HLS_COLOR_SPACE = new ParametricHlsColorSpace("HSL", SRGB_COLOR_SPACE);
    private final static ParametricHsvColorSpace HSV_COLOR_SPACE = new ParametricHsvColorSpace("HSV", SRGB_COLOR_SPACE);

    public void initWithDefaultValues() {
        DisplayP3ColorSpace displayP3ColorSpace = new DisplayP3ColorSpace();
        ProPhotoRgbColorSpace proPhotoRgbColorSpace = new ProPhotoRgbColorSpace();
        Rec2020ColorSpace rec2020ColorSpace = new Rec2020ColorSpace();
        displayColorSpaces.get().setAll(
                SRGB_COLOR_SPACE,
                displayP3ColorSpace,
                // new A98RgbColorSpace(), -> not a display
                rec2020ColorSpace,
                proPhotoRgbColorSpace
        );
        targetColorSpaces.get().setAll(
                // color()-function:
                SRGB_COLOR_SPACE,
                SRGB_COLOR_SPACE.getLinearColorSpace(),
                displayP3ColorSpace,
                new A98RgbColorSpace(),
                proPhotoRgbColorSpace,
                rec2020ColorSpace,
                new D50XyzColorSpace(),
                new D65XyzColorSpace(),
                // lab()-function:
                OK_LAB_COLOR_SPACE,
                CIE_LAB_COLOR_SPACE
        );
        targetColorSyntaxes.get().setAll(ColorSyntax.values());
        targetColorSyntax.set(ColorSyntax.HEX_COLOR);
        chooserTypes.get().setAll(ChooserType.values());
        chooserType.set(ChooserType.OK_HSL_SRGB);
        displayColorSpace.set(SRGB_COLOR_SPACE);
        targetColorSpace.set(SRGB_COLOR_SPACE);
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

    public @NonNull ObjectProperty<Color> previewColorProperty() {
        return previewColor;
    }

    public @NonNull StringProperty sourceColorFieldProperty() {
        return sourceColorField;
    }

    public void setComponent(int i, float value) {
        switch (i) {
            case 0 -> c0.set(value);
            case 1 -> c1.set(value);
            case 2 -> c2.set(value);
            default -> c3.setValue(value);
        }
    }

    public float getComponent(int i) {
        return switch (i) {
            case 0 -> c0.get();
            case 1 -> c1.get();
            case 2 -> c2.get();
            default -> c3.getValue();
        };
    }


    public @NonNull IntegerProperty sourceColorSpaceHueIndexProperty() {
        return sourceColorSpaceHueIndex;
    }

    public @NonNull IntegerProperty sourceColorSpaceLightnessValueIndexProperty() {
        return sourceColorSpaceLightnessValueIndex;
    }

    public @NonNull ObjectProperty<NamedColorSpace> sourceColorSpaceProperty() {
        return sourceColorSpace;
    }

    public @NonNull IntegerProperty sourceColorSpaceSaturationChromaIndexProperty() {
        return sourceColorSpaceSaturationChromaIndex;
    }

    public @NonNull ObjectProperty<NamedColor> targetColorProperty() {
        return targetColor;
    }

    public @NonNull ObjectProperty<NamedColorSpace> targetColorSpaceProperty() {
        return targetColorSpace;
    }

    public @NonNull ListProperty<NamedColorSpace> targetColorSpacesProperty() {
        return targetColorSpaces;
    }

    public @NonNull ObjectProperty<ColorSyntax> targetColorSyntaxProperty() {
        return targetColorSyntax;
    }

    public @NonNull ListProperty<ColorSyntax> targetColorSyntaxesProperty() {
        return targetColorSyntaxes;
    }

    private void updateSourceColorField(Observable o, Object oldv, Object newv) {

        String text = toCssString(getSourceColorSpace(), getC0(), getC1(), getC2(), getAlpha(), ColorSyntax.AUTOMATIC);
        setSourceColorField(text);
    }

    private void updateTargetColorField(Observable o, Object oldv, Object newv) {
        NamedColorSpace tcs = getTargetColorSpace();
        switch (getTargetColorSyntax()) {

            case AUTOMATIC, COLOR_FUNCTION, HWB_FUNCTION, HSL_FUNCTION, RGB_FUNCTION, NAMED_COLOR, HEX_COLOR -> {
            }
            case LAB_FUNCTION, LCH_FUNCTION -> tcs = CIE_LAB_COLOR_SPACE;
            case OKLAB_FUNCTION -> tcs = OK_LAB_COLOR_SPACE;
            case OKLCH_FUNCTION -> tcs = OK_LCH_COLOR_SPACE;
        }
        NamedColorSpace scs = getSourceColorSpace();
        if (tcs != null && scs != null) {
            float[] sComponent = {getC0(), getC1(), getC2()};
            float[] tComponent;
            if (tcs != scs) {
                tComponent = tcs.fromCIEXYZ(scs.toCIEXYZ(sComponent));
            } else {
                tComponent = sComponent;
            }
            String text = toCssString(tcs, tComponent[0], tComponent[1], tComponent[2], getAlpha(), getTargetColorSyntax());
            setTargetColorField(text);
        }
    }

    private final static Map<String, String> colorSpaceNameMap = Map.of(
            "Display P3", "display-p3",

            "sRGB", "srgb",
            "sRGB Linear", "srgb-linear",
            "display-p3", "display-p3",
            "a98-rgb", "a98-rgb",
            "ProPhoto RGB", "prophoto-rgb",
            "Rec 2020", "rec2020",
            "xyz", "xyz",
            "xyz-d50", "xyz-d50",
            "xyz-d65", "xyz-d65"
    );

    @NonNull
    private String toCssString(NamedColorSpace cs, float c0, float c1, float c2, float alpha, ColorSyntax colorSyntax) {
        StringBuilder b = new StringBuilder();
        if (cs == null) {
            b.append("none");
            return b.toString();
        }

        if (colorSyntax == ColorSyntax.AUTOMATIC) {
            switch (cs.getType()) {
                case NamedColorSpace.TYPE_LCH ->
                        colorSyntax = cs instanceof OKLchColorSpace ? ColorSyntax.OKLCH_FUNCTION : ColorSyntax.LCH_FUNCTION;
                case ColorSpace.TYPE_HLS -> colorSyntax = ColorSyntax.HSL_FUNCTION;
                case ColorSpace.TYPE_Lab ->
                        colorSyntax = cs instanceof OKLabColorSpace ? ColorSyntax.OKLAB_FUNCTION : ColorSyntax.LAB_FUNCTION;
                default -> colorSyntax = ColorSyntax.COLOR_FUNCTION;
            }
        }

        float[] components = {c0, c1, c2};
        switch (colorSyntax) {

            case NAMED_COLOR -> {
                // TODO lookup components
            }
            case HEX_COLOR, RGB_FUNCTION -> // requires sRGB components
                    cs.toRGB(components, components);
            case HSL_FUNCTION -> {
                // requires HLS components
                if (cs.getType() != ColorSpace.TYPE_HLS || cs instanceof ParametricHlsColorSpace hlsc
                        && !hlsc.getRgbColorSpace().getName().equals("sRGB")) {
                    HLS_COLOR_SPACE.fromCIEXYZ(cs.toCIEXYZ(components, components), components);
                }
            }
            case HWB_FUNCTION -> {
                // FIXME implement me
            }
            case LAB_FUNCTION -> {
                // requires LAB components
                if (cs.getType() != ColorSpace.TYPE_Lab || cs instanceof OKLabColorSpace) {
                    CIE_LAB_COLOR_SPACE.fromCIEXYZ(cs.toCIEXYZ(components, components), components);
                }
            }
            case OKLAB_FUNCTION -> {
                // requires OKLAB components
                if (!(cs instanceof OKLabColorSpace)) {
                    CIE_LAB_COLOR_SPACE.fromCIEXYZ(cs.toCIEXYZ(components, components), components);
                }
            }
            case LCH_FUNCTION -> {
                // requires OKLCH components
                if (!(cs instanceof OKLchColorSpace)) {
                    OK_LCH_COLOR_SPACE.fromCIEXYZ(cs.toCIEXYZ(components, components), components);
                }
            }
            case COLOR_FUNCTION -> {
            }
        }

        // Discard very small values
        for (int i = 0; i < components.length; i++) {
            if (Math.abs(components[i]) < 1e-5f) {
                components[i] = 0;
            }
        }


        switch (colorSyntax) {
            case HEX_COLOR, NAMED_COLOR -> {
                Map.Entry<String, ToIntFunction<Integer>> entry = displayBitDepth.get();
                int value = RgbBitConverters.rgbFloatToArgb32(components, alpha);
                int argb = entry == null ? value : entry.getValue().applyAsInt(value);
                String hexStr = "00000000" + Integer.toHexString(argb);
                hexStr = hexStr.substring(hexStr.length() - 8);
                // FIXME if NAMED_COLOR lookup map of named colors
                b.append(hexStr);
            }
            case RGB_FUNCTION -> {
                b.append("rgb(");
                components[0] = Math.round(255 * components[0]);
                components[1] = Math.round(255 * components[1]);
                components[2] = Math.round(255 * components[2]);
            }
            case HSL_FUNCTION -> {
                if (cs instanceof OKHlsColorSpace) {
                    b.append("okhsl(");
                } else {
                    b.append("hsl(");
                }
                b.append(number.toString(components[0]));
                b.append(' ');
                b.append(percentageNumber.toString(components[1] * 100));
                b.append("% ");
                b.append(percentageNumber.toString(components[2] * 100));
                b.append("% / ");
                b.append(number.toString(alpha * 100));
                b.append("%)");
            }
            case HWB_FUNCTION -> b.append("hwb(");
            case LAB_FUNCTION -> b.append("lab(");
            case OKLAB_FUNCTION -> b.append("oklab(");
            case LCH_FUNCTION -> b.append("lch(");
            case OKLCH_FUNCTION -> b.append("oklch(");
            case COLOR_FUNCTION -> {
                b.append("color(");
                b.append(colorSpaceNameMap.get(cs.getName()));
                b.append(' ');
            }
            default -> {
                b.append("color(\"");
                b.append(cs.getName());
                b.append("\" ");
            }
        }
        switch (colorSyntax) {

            case HEX_COLOR, NAMED_COLOR, HSL_FUNCTION -> {

            }
            default -> {
                b.append(number.toString(components[0]));
                b.append(' ');
                b.append(number.toString(components[1]));
                b.append(' ');
                b.append(number.toString(components[2]));
                b.append(" / ");
                b.append(number.toString(alpha * 100));
                b.append("%)");
            }
        }
        return b.toString();
    }


    private void updateSourceColorSpace(Observable o, Object oldv, Object newv) {
        ChooserType ct = chooserType.get();
        final NamedColorSpace tcs = targetColorSpace.get();
        if (ct == null || tcs == null) {
            sourceColorSpace.set(null);
            return;
        }
        NamedColorSpace oldScs = sourceColorSpace.get();
        switch (ct) {
            case OK_HSL_SRGB -> sourceColorSpace.set(new OKHlsColorSpace());
            case SLIDERS, SWATCHES -> sourceColorSpace.set(tcs);
            case HSL -> {
                NamedColorSpace cs;
                if (tcs.getType() == ColorSpace.TYPE_RGB) {
                    cs = tcs;
                } else {
                    cs = new SrgbColorSpace();
                }
                sourceColorSpace.set(new ParametricHlsColorSpace(cs.getName() + " HSL", cs));
            }
            case HSV -> {
                NamedColorSpace cs;
                if (tcs.getType() == ColorSpace.TYPE_RGB) {
                    cs = tcs;
                } else {
                    cs = new SrgbColorSpace();
                }
                sourceColorSpace.set(new ParametricHsvColorSpace(cs.getName() + " HSV", cs));
            }
            case CIE_LCH -> sourceColorSpace.set(CIE_LCH_COLOR_SPACE);
            case OK_LCH -> sourceColorSpace.set(new OKLchColorSpace());
            default -> {
                sourceColorSpace.set(tcs);
                hueSliderC0.set(0.5f);
                hueSliderC1.set(0.5f);
            }
        }
        NamedColorSpace newScs = sourceColorSpace.get();

        if (newScs instanceof OKLchColorSpace) {
            hueSliderC0.set(0.75f);
            hueSliderC1.set(0.12f);
            hueSliderC2.set(0.5f);
        } else {
            hueSliderC0.set(0.5f * (newScs.getMaxValue(0) - newScs.getMinValue(0)) + newScs.getMinValue(0));
            hueSliderC1.set(0.5f * (newScs.getMaxValue(1) - newScs.getMinValue(1)) + newScs.getMinValue(1));
            hueSliderC2.set(0.5f * (newScs.getMaxValue(2) - newScs.getMinValue(2)) + newScs.getMinValue(2));
        }

        // convert color components
        if (oldScs != null) {
            float[] oldComponents = {getC0(), getC1(), getC2()};
            float[] xyz = oldScs.toCIEXYZ(oldComponents);
            float[] newComponents = newScs.fromCIEXYZ(xyz);
            setC0(newComponents[0]);
            setC1(newComponents[1]);
            setC2(newComponents[2]);
        }
    }

    private void updateSourceColorSpaceProperties(Observable o, Object oldv, Object newv) {
        NamedColorSpace ncs = sourceColorSpace.get();
        if (ncs == null) {
            return;
        }

        hue.unbindBidirectional(c0);
        hue.unbindBidirectional(c1);
        hue.unbindBidirectional(c2);
        chroma.unbindBidirectional(c0);
        chroma.unbindBidirectional(c1);
        chroma.unbindBidirectional(c2);
        lightness.unbindBidirectional(c0);
        lightness.unbindBidirectional(c1);
        lightness.unbindBidirectional(c2);

        switch (ncs.getType()) {
            default -> {
                sourceColorSpaceHueIndex.set(0);
                sourceColorSpaceSaturationChromaIndex.set(1);
                sourceColorSpaceLightnessValueIndex.set(2);
                hue.bindBidirectional(c0);
                lightness.bindBidirectional(c1);
                chroma.bindBidirectional(c2);
            }
            case ColorSpace.TYPE_HLS -> {
                sourceColorSpaceHueIndex.set(0);
                sourceColorSpaceSaturationChromaIndex.set(2);
                sourceColorSpaceLightnessValueIndex.set(1);
                hue.bindBidirectional(c0);
                lightness.bindBidirectional(c1);
                chroma.bindBidirectional(c2);
            }
            case NamedColorSpace.TYPE_LCH -> {
                sourceColorSpaceHueIndex.set(2);
                sourceColorSpaceSaturationChromaIndex.set(1);
                sourceColorSpaceLightnessValueIndex.set(0);
                lightness.bindBidirectional(c0);
                chroma.bindBidirectional(c1);
                hue.bindBidirectional(c2);
            }
        }
    }

    private void updateDisplayColor(Observable o, Object oldv, Object newv) {
        float[] component = {c0.floatValue(), c1.floatValue(), c2.floatValue()};
        float[] rgb = new float[3];
        NamedColorSpace displayCs = displayColorSpace.get();
        NamedColorSpace sourceCs = sourceColorSpace.get();
        NamedColorSpace targetCs = targetColorSpace.get();
        if (displayCs == null || targetCs == null) {
            return;
        }
        Map.Entry<String, ToIntFunction<Integer>> entry = displayBitDepth.get();

        // compute display color
        {
            displayCs.fromRGB(sourceCs.toRGB(component, rgb), rgb);
            int value = RgbBitConverters.rgbFloatToArgb32(rgb, alpha.floatValue());
            int argb = entry == null ? value : entry.getValue().applyAsInt(value);
            Color previewColorValue = Color.rgb((argb >>> 16) & 0xff, (argb >>> 8) & 0xff, (argb) & 0xff, clamp(alpha.floatValue(), 0, 1));
            String hexStr = "00000000" + Integer.toHexString(argb);
            hexStr = hexStr.substring(hexStr.length() - 8);
            previewColor.set(previewColorValue);
            setDisplayColorField("#" + hexStr);
        }

        // compute target color
        /*{
            targetCs.fromRGB(sourceCs.toRGB(component, rgb), rgb);
            int value = RgbBitConverters.rgbFloatToArgb32(rgb, alpha.floatValue());
            int argb = entry == null ? value : entry.getValue().applyAsInt(value);
            String hexStr = "00000000" + Integer.toHexString(argb);
            hexStr = hexStr.substring(hexStr.length() - 8);
            setTargetColorField("#" + hexStr);
        }*/
        //hexRgbLabel.setText("#" + hexStr.substring(2) + hexStr.substring(0, 2));
    }

    public String getTargetColorField() {
        return targetColorField.get();
    }

    public @NonNull StringProperty targetColorFieldProperty() {
        return targetColorField;
    }

    public void setTargetColorField(String targetColorField) {
        this.targetColorField.set(targetColorField);
    }

    public enum ColorSyntax {
        AUTOMATIC,
        HEX_COLOR,
        NAMED_COLOR,
        RGB_FUNCTION,
        HSL_FUNCTION,
        HWB_FUNCTION,
        LAB_FUNCTION,
        OKLAB_FUNCTION,
        LCH_FUNCTION,
        OKLCH_FUNCTION,
        COLOR_FUNCTION,
    }

    public enum ChooserType {
        OK_HSL_SRGB,
        SLIDERS,
        SWATCHES,
        HSL,

        HSV,
        OK_LCH,
        CIE_LCH,
    }

    public String getDisplayColorField() {
        return displayColorField.get();
    }

    public @NonNull StringProperty displayColorFieldProperty() {
        return displayColorField;
    }

    public void setDisplayColorField(String displayColorField) {
        this.displayColorField.set(displayColorField);
    }

    public float getHue() {
        return hue.get();
    }

    public @NonNull FloatProperty hueProperty() {
        return hue;
    }

    public void setHue(float hue) {
        this.hue.set(hue);
    }

    public float getChroma() {
        return chroma.get();
    }

    public @NonNull FloatProperty chromaProperty() {
        return chroma;
    }

    public void setChroma(float chroma) {
        this.chroma.set(chroma);
    }

    public float getLightness() {
        return lightness.get();
    }

    public @NonNull FloatProperty lightnessProperty() {
        return lightness;
    }

    public void setLightness(float lightness) {
        this.lightness.set(lightness);
    }

    public float getHueSliderC0() {
        return hueSliderC0.get();
    }

    public @NonNull FloatProperty hueSliderC0Property() {
        return hueSliderC0;
    }

    public void setHueSliderC0(float lightness) {
        this.hueSliderC0.set(lightness);
    }

    public float getHueSliderC1() {
        return hueSliderC1.get();
    }

    public @NonNull FloatProperty hueSliderC1Property() {
        return hueSliderC1;
    }

    public void setHueSliderC1(float hueSliderC1) {
        this.hueSliderC1.set(hueSliderC1);
    }

    public float getHueSliderC2() {
        return hueSliderC2.get();
    }

    public @NonNull FloatProperty hueSliderC2Property() {
        return hueSliderC2;
    }

    public void setHueSliderC2(float hueSliderC2) {
        this.hueSliderC2.set(hueSliderC2);
    }
}
