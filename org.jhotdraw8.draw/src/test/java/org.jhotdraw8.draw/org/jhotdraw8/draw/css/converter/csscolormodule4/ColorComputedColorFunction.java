package org.jhotdraw8.draw.css.converter.csscolormodule4;

import org.junit.jupiter.api.Test;

import static org.jhotdraw8.draw.css.converter.csscolormodule4.ComputeTestcommon.test_computed_value;

/**
 * References:
 * <dl>
 *     <dt>CSS Color Module Level 4.</dt>
 *     <dd><a href="https://www.w3.org/TR/2022/CRD-css-color-4-20221101">w3.org</a></dd>
 *
 *     <dt>web-platform-tests / wpt / color-computed-color-function.html</dt>
 *     <dd><a href="https://github.com/web-platform-tests/wpt/blob/f69cc2c952a97e745446a6026559292a96340fd8/css/css-color/parsing/color-computed-color-function.html">github.com</a></dd>
 * </dl>
 */
public class ColorComputedColorFunction {
    @Test
    public void shouldComputeExpectedValue() {
        for (var colorSpace : new String[]{"srgb", "srgb-linear", "a98-rgb", "rec2020", "prophoto-rgb", "display-p3"}) {
            test_computed_value("color", "color(" + colorSpace + " 0% 0% 0%)", "color(" + colorSpace + " 0% 0% 0%)");
            test_computed_value("color", "color(" + colorSpace + " 10% 10% 10%)", "color(" + colorSpace + " 10% 10% 10%)");
            test_computed_value("color", "color(" + colorSpace + " .2 .2 25%)", "color(" + colorSpace + " 0.2 0.2 25%)");
            test_computed_value("color", "color(" + colorSpace + " 0 0 0 / 1)", "color(" + colorSpace + " 0 0 0 / 1)");
            test_computed_value("color", "color(" + colorSpace + " 0% 0 0 / 0.5)", "color(" + colorSpace + " 0% 0 0 / 0.5)");
            test_computed_value("color", "color(" + colorSpace + " 20% 0 10/0.5)", "color(" + colorSpace + " 20% 0 10 / 0.5)");
            test_computed_value("color", "color(" + colorSpace + " 20% 0 10/50%)", "color(" + colorSpace + " 20% 0 10 / 50%)");
            test_computed_value("color", "color(" + colorSpace + " 400% 0 10/50%)", "color(" + colorSpace + " 400% 0 10 / 50%)");
            test_computed_value("color", "color(" + colorSpace + " 50% -160 160)", "color(" + colorSpace + " 50% -160 160)");
            test_computed_value("color", "color(" + colorSpace + " 50% -200 200)", "color(" + colorSpace + " 50% -200 200)");
            test_computed_value("color", "color(" + colorSpace + " 0 0 0 / -10%)", "color(" + colorSpace + " 0 0 0 / -10%)");
            test_computed_value("color", "color(" + colorSpace + " 0 0 0 / 110%)", "color(" + colorSpace + " 0 0 0 / 110%)");
            test_computed_value("color", "color(" + colorSpace + " 0 0 0 / 300%)", "color(" + colorSpace + " 0 0 0 / 300%)");
            test_computed_value("color", "color(" + colorSpace + " 200 200 200)", "color(" + colorSpace + " 200 200 200)");
            test_computed_value("color", "color(" + colorSpace + " 200 200 200 / 200)", "color(" + colorSpace + " 200 200 200 / 200)");
            test_computed_value("color", "color(" + colorSpace + " -200 -200 -200)", "color(" + colorSpace + " -200 -200 -200)");
            test_computed_value("color", "color(" + colorSpace + " -200 -200 -200 / -200)", "color(" + colorSpace + " -200 -200 -200 / -200)");
            test_computed_value("color", "color(" + colorSpace + " 200% 200% 200%)", "color(" + colorSpace + " 200% 200% 200%)");
            test_computed_value("color", "color(" + colorSpace + " 200% 200% 200% / 200%)", "color(" + colorSpace + " 200% 200% 200% / 200%)");
            test_computed_value("color", "color(" + colorSpace + " -200% -200% -200% / -200%)", "color(" + colorSpace + " -200% -200% -200% / -200%)");
        }

        for (var colorSpace : new String[]{"xyz", "xyz-d50", "xyz-d65"}) {

            test_computed_value("color", "color(" + colorSpace + " 0 0 0)", "color(" + colorSpace + " 0 0 0)");
            test_computed_value("color", "color(" + colorSpace + " 0 0 0 / 1)", "color(" + colorSpace + " 0 0 0 / 1)");
            test_computed_value("color", "color(" + colorSpace + " 1 1 1)", "color(" + colorSpace + " 1 1 1)");
            test_computed_value("color", "color(" + colorSpace + " 1 1 1 / 1)", "color(" + colorSpace + " 1 1 1 / 1)");
            test_computed_value("color", "color(" + colorSpace + " -1 -1 -1)", "color(" + colorSpace + " -1 -1 -1)");
            test_computed_value("color", "color(" + colorSpace + " 0.1 0.1 0.1)", "color(" + colorSpace + " 0.1 0.1 0.1)");
            test_computed_value("color", "color(" + colorSpace + " 10 10 10)", "color(" + colorSpace + " 10 10 10)");
            test_computed_value("color", "color(" + colorSpace + " .2 .2 .25)", "color(" + colorSpace + " 0.2 0.2 0.25)");
            test_computed_value("color", "color(" + colorSpace + " 0 0 0 / 0.5)", "color(" + colorSpace + " 0 0 0 / 0.5)");
            test_computed_value("color", "color(" + colorSpace + " .20 0 10/0.5)", "color(" + colorSpace + " 0.2 0 10 / 0.5)");
            test_computed_value("color", "color(" + colorSpace + " .20 0 10/50%)", "color(" + colorSpace + " 0.2 0 10 / 50%)");
            test_computed_value("color", "color(" + colorSpace + " 0 0 0 / -10%)", "color(" + colorSpace + " 0 0 0 / -10%)");
            test_computed_value("color", "color(" + colorSpace + " 0 0 0 / 110%)", "color(" + colorSpace + " 0 0 0 / 110%)");
            test_computed_value("color", "color(" + colorSpace + " 0 0 0 / 300%)", "color(" + colorSpace + " 0 0 0 / 300%)");

            test_computed_value("color", "color(" + colorSpace + " 1.00 0.50 0.200)", "color(" + colorSpace + " 1 0.5 0.2)", "[all numbers]");
            test_computed_value("color", "color(" + colorSpace + " 100% 50% 20%)", "color(" + colorSpace + " 100% 50% 20%)", "[all percent]");
            test_computed_value("color", "color(" + colorSpace + " 100% 0.5 20%)", "color(" + colorSpace + " 100% 0.5 20%)", "[mixed number and percent]");
            test_computed_value("color", "color(" + colorSpace + " 1.00 50% 0.2)", "color(" + colorSpace + " 1 50% 0.2)", "[mixed number and percent 2]");
        }

        // Tests basic parsing of the color function
        test_computed_value("color", "color(srgb 1 1 1)", "color(srgb 1 1 1)", "[Basic sRGB white]");
        test_computed_value("color", "color(    srgb         1      1 1       )", "color(srgb 1 1 1)", "[White with lots of space]");
        test_computed_value("color", "color(srgb 0.25 0.5 0.75)", "color(srgb 0.25 0.5 0.75)", "[sRGB color]");
        test_computed_value("color", "color(SrGb 0.25 0.5 0.75)", "color(srgb 0.25 0.5 0.75)", "[Different case for sRGB]");
        test_computed_value("color", "color(srgb 1.00000 0.500000 0.20)", "color(srgb 1 0.5 0.2)", "[sRGB color with unnecessary decimals]");
        test_computed_value("color", "color(srgb 1 1 1 / 0.5)", "color(srgb 1 1 1 / 0.5)", "[sRGB white with 0.5 alpha]");
        test_computed_value("color", "color(srgb 1 1 1 / 0)", "color(srgb 1 1 1 / 0)", "[sRGB white with 0 alpha]");
        test_computed_value("color", "color(srgb 1 1 1 / 50%)", "color(srgb 1 1 1 / 50%)", "[sRGB white with 50% alpha]");
        test_computed_value("color", "color(srgb 1 1 1 / 0%)", "color(srgb 1 1 1 / 0%)", "[sRGB white with 0% alpha]");
        test_computed_value("color", "color(display-p3 0.6 0.7 0.8)", "color(display-p3 0.6 0.7 0.8)", "[Display P3 color]");
        test_computed_value("color", "color(dIspLaY-P3 0.6 0.7 0.8)", "color(display-p3 0.6 0.7 0.8)", "[Different case for Display P3]");

        test_computed_value("color", "color(srgb -0.25 0.5 0.75)", "color(srgb -0.25 0.5 0.75)", "[sRGB color with negative component should not clamp to 0]");
        test_computed_value("color", "color(srgb 0.25 1.5 0.75)", "color(srgb 0.25 1.5 0.75)", "[sRGB color with component > 1 should not clamp]");
        test_computed_value("color", "color(display-p3 0.5 -199 0.75)", "color(display-p3 0.5 -199 0.75)", "[Display P3 color with negative component should not clamp to 0]");
        test_computed_value("color", "color(display-p3 184 1.00001 1103)", "color(display-p3 184 1.00001 1103)", "[Display P3 color with component > 1 should not clamp]");
        test_computed_value("color", "color(srgb 0.1 0.2 0.3 / 1.9)", "color(srgb 0.1 0.2 0.3 / 1.9)", "[Alpha > 1 should clamp (but in an editor we want to preserve the input!)]");
        test_computed_value("color", "color(srgb 1 1 1 / -0.2)", "color(srgb 1 1 1 / -0.2)", "[Negative alpha should clamp  (but in an editor we want to preserve the input!)]");
    }
}
