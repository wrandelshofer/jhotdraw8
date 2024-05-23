/*
 * @(#)NamedColorName.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.draw.css.value;


/**
 * Specifies the name of a named color in a cascading stylesheet.
 * <p>
 * All names are given in lower case. Although the color names are not case sensitive.
 * <p>
 * References:
 * <dl>
 *     <dt>CSS Color Module Level 4, System Color</dt>
 *     <dd><a href="https://www.w3.org/TR/css-color-4/#css-system-colors">w3.org/<a></a></a></dd>
 * </dl>
 */
public interface NamedColorName {


    String TRANSPARENT = "transparent";

    String ALICEBLUE = "aliceblue";
    String ANTIQUEWHITE = "antiquewhite";
    String AQUA = "aqua";
    String AQUAMARINE = "aquamarine";
    String AZURE = "azure";
    String BEIGE = "beige";
    String BISQUE = "bisque";
    String BLACK = "black";
    String BLANCHEDALMOND = "blanchedalmond";
    String BLUE = "blue";
    String BLUEVIOLET = "blueviolet";
    String BROWN = "brown";
    String BURLYWOOD = "burlywood";
    String CADETBLUE = "cadetblue";
    String CHARTREUSE = "chartreuse";
    String CHOCOLATE = "chocolate";
    String CORAL = "coral";
    String CORNFLOWERBLUE = "cornflowerblue";
    String CORNSILK = "cornsilk";
    String CRIMSON = "crimson";
    String CYAN = "cyan";
    String DARKBLUE = "darkblue";
    String DARKCYAN = "darkcyan";
    String DARKGOLDENROD = "darkgoldenrod";
    String DARKGRAY = "darkgray";
    String DARKGREEN = "darkgreen";
    String DARKGREY = "darkgrey";
    String DARKKHAKI = "darkkhaki";
    String DARKMAGENTA = "darkmagenta";
    String DARKOLIVEGREEN = "darkolivegreen";
    String DARKORANGE = "darkorange";
    String DARKORCHID = "darkorchid";
    String DARKRED = "darkred";
    String DARKSALMON = "darksalmon";
    String DARKSEAGREEN = "darkseagreen";
    String DARKSLATEBLUE = "darkslateblue";
    String DARKSLATEGRAY = "darkslategray";
    String DARKSLATEGREY = "darkslategrey";
    String DARKTURQUOISE = "darkturquoise";
    String DARKVIOLET = "darkviolet";
    String DEEPPINK = "deeppink";
    String DEEPSKYBLUE = "deepskyblue";
    String DIMGRAY = "dimgray";
    String DIMGREY = "dimgrey";
    String DODGERBLUE = "dodgerblue";
    String FIREBRICK = "firebrick";
    String FLORALWHITE = "floralwhite";
    String FORESTGREEN = "forestgreen";
    String FUCHSIA = "fuchsia";
    String GAINSBORO = "gainsboro";
    String GHOSTWHITE = "ghostwhite";
    String GOLD = "gold";
    String GOLDENROD = "goldenrod";
    String GRAY = "gray";
    String GREEN = "green";
    String GREENYELLOW = "greenyellow";
    String GREY = "grey";
    String HONEYDEW = "honeydew";
    String HOTPINK = "hotpink";
    String INDIANRED = "indianred";
    String INDIGO = "indigo";
    String IVORY = "ivory";
    String KHAKI = "khaki";
    String LAVENDER = "lavender";
    String LAVENDERBLUSH = "lavenderblush";
    String LAWNGREEN = "lawngreen";
    String LEMONCHIFFON = "lemonchiffon";
    String LIGHTBLUE = "lightblue";
    String LIGHTCORAL = "lightcoral";
    String LIGHTCYAN = "lightcyan";
    String LIGHTGOLDENRODYELLOW = "lightgoldenrodyellow";
    String LIGHTGRAY = "lightgray";
    String LIGHTGREEN = "lightgreen";
    String LIGHTGREY = "lightgrey";
    String LIGHTPINK = "lightpink";
    String LIGHTSALMON = "lightsalmon";
    String LIGHTSEAGREEN = "lightseagreen";
    String LIGHTSKYBLUE = "lightskyblue";
    String LIGHTSLATEGRAY = "lightslategray";
    String LIGHTSLATEGREY = "lightslategrey";
    String LIGHTSTEELBLUE = "lightsteelblue";
    String LIGHTYELLOW = "lightyellow";
    String LIME = "lime";
    String LIMEGREEN = "limegreen";
    String LINEN = "linen";
    String MAGENTA = "magenta";
    String MAROON = "maroon";
    String MEDIUMAQUAMARINE = "mediumaquamarine";
    String MEDIUMBLUE = "mediumblue";
    String MEDIUMORCHID = "mediumorchid";
    String MEDIUMPURPLE = "mediumpurple";
    String MEDIUMSEAGREEN = "mediumseagreen";
    String MEDIUMSLATEBLUE = "mediumslateblue";
    String MEDIUMSPRINGGREEN = "mediumspringgreen";
    String MEDIUMTURQUOISE = "mediumturquoise";
    String MEDIUMVIOLETRED = "mediumvioletred";
    String MIDNIGHTBLUE = "midnightblue";
    String MINTCREAM = "mintcream";
    String MISTYROSE = "mistyrose";
    String MOCCASIN = "moccasin";
    String NAVAJOWHITE = "navajowhite";
    String NAVY = "navy";
    String OLDLACE = "oldlace";
    String OLIVE = "olive";
    String OLIVEDRAB = "olivedrab";
    String ORANGE = "orange";
    String ORANGERED = "orangered";
    String ORCHID = "orchid";
    String PALEGOLDENROD = "palegoldenrod";
    String PALEGREEN = "palegreen";
    String PALETURQUOISE = "paleturquoise";
    String PALEVIOLETRED = "palevioletred";
    String PAPAYAWHIP = "papayawhip";
    String PEACHPUFF = "peachpuff";
    String PERU = "peru";
    String PINK = "pink";
    String PLUM = "plum";
    String POWDERBLUE = "powderblue";
    String PURPLE = "purple";
    String REBECCAPURPLE = "rebeccapurple";
    String RED = "red";
    String ROSYBROWN = "rosybrown";
    String ROYALBLUE = "royalblue";
    String SADDLEBROWN = "saddlebrown";
    String SALMON = "salmon";
    String SANDYBROWN = "sandybrown";
    String SEAGREEN = "seagreen";
    String SEASHELL = "seashell";
    String SIENNA = "sienna";
    String SILVER = "silver";
    String SKYBLUE = "skyblue";
    String SLATEBLUE = "slateblue";
    String SLATEGRAY = "slategray";
    String SLATEGREY = "slategrey";
    String SNOW = "snow";
    String SPRINGGREEN = "springgreen";
    String STEELBLUE = "steelblue";
    String TAN = "tan";
    String TEAL = "teal";
    String THISTLE = "thistle";
    String TOMATO = "tomato";
    String TURQUOISE = "turquoise";
    String VIOLET = "violet";
    String WHEAT = "wheat";
    String WHITE = "white";
    String WHITESMOKE = "whitesmoke";
    String YELLOW = "yellow";
    String YELLOWGREEN = "yellowgreen";


}
