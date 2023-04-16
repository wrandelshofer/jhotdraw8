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
import org.jhotdraw8.base.converter.NumberConverter;
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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.ToIntFunction;

import static org.jhotdraw8.base.util.MathUtil.clamp;

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
    private final static NumberConverter number = new NumberConverter(Float.class, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1, false, null,
            new DecimalFormat("#################0.###", new DecimalFormatSymbols(Locale.ENGLISH)),
            new DecimalFormat("0.0###E0", new DecimalFormatSymbols(Locale.ENGLISH)));

    public final @NonNull FloatProperty alpha = new SimpleFloatProperty(this, "alpha");
    public final @NonNull FloatProperty c0 = new SimpleFloatProperty(this, "c0");
    public final @NonNull FloatProperty c1 = new SimpleFloatProperty(this, "c1");
    public final @NonNull FloatProperty c2 = new SimpleFloatProperty(this, "c2");
    public final @NonNull FloatProperty hue = new SimpleFloatProperty(this, "hue");
    public final @NonNull FloatProperty chroma = new SimpleFloatProperty(this, "chroma");
    public final @NonNull FloatProperty lightness = new SimpleFloatProperty(this, "lightness");
    public final @NonNull FloatProperty c3 = new SimpleFloatProperty(this, "c3");
    public final @NonNull ObjectProperty<NamedColor> sourceColor = new SimpleObjectProperty<>(this, "chooserColor");
    public final @NonNull ObjectProperty<ChooserType> chooserType = new SimpleObjectProperty<>(this, "ChooserType");
    public final @NonNull ListProperty<ChooserType> chooserTypes = new SimpleListProperty<>(this, "ChooserTypes", FXCollections.observableArrayList());
    public final @NonNull ObjectProperty<Map.Entry<String, ToIntFunction<Integer>>> displayBitDepth = new SimpleObjectProperty(this, "displayBitDepth");
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
            updateTargetColor(o, oldv, newv);
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

        String text = toCssString(getSourceColorSpace(), getC0(), getC1(), getC2(), getAlpha());
        setSourceColorField(text);
    }

    private void updateTargetColorField(Observable o, Object oldv, Object newv) {
        NamedColorSpace tcs = getTargetColorSpace();
        NamedColorSpace scs = getSourceColorSpace();
        if (tcs != null && scs != null) {
            float[] sComponent = {getC0(), getC1(), getC2()};
            float[] tComponent;
            if (tcs != scs) {
                tComponent = tcs.fromCIEXYZ(scs.toCIEXYZ(sComponent));
            } else {
                tComponent = sComponent;
            }
            String text = toCssString(tcs, tComponent[0], tComponent[1], tComponent[2], getAlpha());
            setTargetColorField(text);
        }
    }

    @NonNull
    private String toCssString(NamedColorSpace cs, float c0, float c1, float c2, float alpha) {
        StringBuilder b = new StringBuilder();
        if (cs != null) {
            b.append("color(\"");
            b.append(cs.getName());
            b.append("\" ");
            b.append(number.toString(c0));
            b.append(' ');
            b.append(number.toString(c1));
            b.append(' ');
            b.append(number.toString(c2));
            b.append(" / ");
            b.append(number.toString(alpha * 100));
            b.append("%)");
        }
        String text = b.toString();
        return text;
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
            case OK_HSL_SRGB -> {
                sourceColorSpace.set(new OKHlsColorSpace());
            }
            case SLIDERS, SWATCHES -> {
                sourceColorSpace.set(tcs);
            }
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
            case CIE_LCH -> {
                sourceColorSpace.set(new ParametricLchColorSpace("CIE LCH", new CieLabColorSpace()));
            }
            case OK_LCH -> {
                sourceColorSpace.set(new OKLchColorSpace());
            }
            default -> sourceColorSpace.set(tcs);
        }
        NamedColorSpace newScs = sourceColorSpace.get();

        // convert color components
        if (oldScs != null && newScs != null) {
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
        if (ncs == null) return;

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

    private void updateTargetColor(Observable o, Object oldv, Object newv) {
        float[] component = {c0.floatValue(), c1.floatValue(), c2.floatValue()};
        float[] rgb = new float[3];
        NamedColorSpace displayCs = displayColorSpace.get();
        NamedColorSpace sourceCs = sourceColorSpace.get();
        NamedColorSpace targetCs = targetColorSpace.get();
        if (displayCs == null || targetCs == null) return;
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
}
