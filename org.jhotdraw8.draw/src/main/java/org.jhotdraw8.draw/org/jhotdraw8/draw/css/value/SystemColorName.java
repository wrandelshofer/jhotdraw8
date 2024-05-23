/*
 * @(#)SystemColorName.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.draw.css.value;


/**
 * Specifies the name of a system color in a cascading stylesheet.
 * <p>
 * All names are given in lower case. System color names are not case sensitive.
 * <p>
 * References:
 * <dl>
 *     <dt>CSS Color Module Level 4, System Color</dt>
 *     <dd><a href="https://www.w3.org/TR/css-color-4/#css-system-colors">w3.org/<a></a></a></dd>
 * </dl>
 */
public interface SystemColorName {
    String CANVAS = "canvas";
    String CANVAS_TEXT = "canvastext";
    String LINK_TEXT = "linktext";
    String VISITED_TEXT = "visitedtext";
    String ACTIVE_TEXT = "activetext";
    String BUTTON_FACE = "buttonface";
    String BUTTON_TEXT = "buttontext";
    String BUTTON_BORDER = "buttonborder";
    String FIELD = "field";
    String FIELD_TEXT = "fieldtext";
    String HIGHLIGHT = "highlight";
    String HIGHLIGHT_TEXT = "highlighttext";
    String MARK = "mark";
    String MARK_TEXT = "marktext";
    String GRAY_TEXT = "graytext";

    // deprecated system colors

    /**
     * Active window border. Same as ButtonBorder.
     */
    String ACTIVE_BORDER = "activeborder";
    /**
     * Active window caption. Same as CanvasText.
     */
    String ACTIVE_CAPTION = "activecaption";
    /**
     * Background color of multiple document interface. Same as Canvas.
     */
    String APP_WORKSPACE = "appworkspace";
    /**
     * Desktop background. Same as Canvas.
     */
    String BACKGROUND = "background";
    /**
     * The color of the border facing the light source for 3-D elements that appear 3-D due to one layer of surrounding border. Same as ButtonFace.
     */
    String BUTTON_HIGHLIGHT = "buttonhighlight";
    /**
     * The color of the border away from the light source for 3-D elements that appear 3-D due to one layer of surrounding border. Same as ButtonFace.
     */
    String BUTTON_SHADOW = "buttonshadow";
    /**
     * Text in caption, size box, and scrollbar arrow box. Same as CanvasText.
     */
    String CAPTION_TEXT = "captiontext";
    /**
     * Inactive window border. Same as ButtonBorder.
     */
    String INACTIVE_BORDER = "inactiveborder";
    /**
     * Inactive window caption. Same as Canvas.
     */
    String INACTIVE_CAPTION = "inactivecaption";
    /**
     * Color of text in an inactive caption. Same as GrayText.
     */
    String INACTIVE_CAPTION_TEXT = "inactivecaptiontext";
    /**
     * Background color for tooltip controls. Same as Canvas.
     */
    String INFO_BACKGROUND = "infobackground";
    /**
     * Text color for tooltip controls. Same as CanvasText.
     */
    String INFO_TEXT = "infotext";
    /**
     * Menu background. Same as Canvas.
     */
    String MENU = "menu";
    /**
     * Text in menus. Same as CanvasText.
     */
    String MENU_TEXT = "menutext";
    /**
     * Scroll bar gray area. Same as Canvas.
     */
    String SCROLLBAR = "scrollbar";
    /**
     * The color of the darker (generally outer) of the two borders away from the light source for 3-D elements that appear 3-D due to two concentric layers of surrounding border. Same as ButtonBorder.
     */
    String THREE_D_DARK_SHADOW = "threeddarkshadow";
    /**
     * The face background color for 3-D elements that appear 3-D due to two concentric layers of surrounding border. Same as ButtonFace.
     */
    String THREE_D_FACE = "threedface";
    /**
     * The color of the lighter (generally outer) of the two borders facing the light source for 3-D elements that appear 3-D due to two concentric layers of surrounding border. Same as ButtonBorder.
     */
    String THREE_D_HIGHLIGHT = "threedhighlight";
    /**
     * The color of the darker (generally inner) of the two borders facing the light source for 3-D elements that appear 3-D due to two concentric layers of surrounding border. Same as ButtonBorder.
     */
    String THREE_D_LIGHT_SHADOW = "threedlightshadow";
    /**
     * The color of the lighter (generally inner) of the two borders away from the light source for 3-D elements that appear 3-D due to two concentric layers of surrounding border. Same as ButtonBorder.
     */
    String THREE_D_SHADOW = "threedshadow";
    /**
     * Window background. Same as Canvas.
     */
    String WINDOW = "window";

    /**
     * Window frame. Same as ButtonBorder.
     */
    String WINDOW_FRAME = "windowframe";
    /**
     * Text in windows. Same as CanvasText.
     */
    String WINDOW_TEXT = "windowtext";

}
