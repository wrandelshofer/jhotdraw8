/*
 * @(#)CssTransformConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.css.converter;

import javafx.geometry.Point3D;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Shear;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.css.converter.AbstractCssConverter;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.CssTokenizer;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * CssTransformConverter.
 * <p>
 * Parses a transform given in the following EBNF:
 * <pre>
 * Transform     = ( Affine | Matrix | Translate | Scale | Rotate | Shear ) ;
 *
 * Affine        = "affine(" , [S] ,
 *                  ( mxx , C, mxy, C, tx, C, myx, C, myy, C, ty
 *                  | mxx , C, mxy, C, mxz, C, tx, C, myx, C, myy, C, myz, C, tz, C, mzx, C, mzy, C, mzz, C, tz)
 *                  ) , [S], ")" ;
 * Matrix        = "matrix(" , [S] ,
 *                  ( mxx , C, myx, C, mxy, C, myy, C, tx, C, ty
 *                  | mxx , C, myx, C, mzx, C, mxy, C, myy, C, mzy, C, mxz, C, myz, C, mzz, C, tx, C, ty, C, tz)
 *                  ) , [S], ")" ;
 * Translate     = "translate(" , [S] , tx , [ C , ty, [ C , tz ] ] , [S], ")" ;
 * Scale         = "scale(" , [S] ,
 *                 ( sx , [ C , sy, [ C , Pivot2D ] ]
 *                 | sx , C , sy, C , sz, [ C , Pivot ]
 *                 ) , [S], ")" ;
 * Rotate        = "rotate(" , [S] ,
 *                  rotate-angle , [ C , Pivot , [ C, Axis ] , [S], ")" ;
 *
 * Shear         = "shear(" , [S] , x , Sep , y , [ Sep , Pivot2D ], [S], ")" ;
 *
 * Axis          =  axisX, Sep , axisY, Sep , axisZ ;
 * Pivot         =  pivotX, Sep , pivotY, [ Sep , pivotZ ] ;
 * Pivot2D       =  pivotX, Sep , pivotY] ;
 *
 * C             = ( S , { S } | { S } , "," , { S } ) ;
 * S             = (* white space *)
 * </pre>
 *
 * @author Werner Randelshofer
 */
public class TransformCssConverter extends AbstractCssConverter<Transform> {

    public TransformCssConverter() {
        this(true);
    }

    public TransformCssConverter(boolean nullable) {
        super(nullable);
    }

    @Override
    protected <TT extends Transform> void produceTokensNonNull(TT tx, @Nullable IdSupplier idSupplier, Consumer<CssToken> out) {
        if (tx instanceof Translate tr) {
            out.accept(new CssToken(CssTokenType.TT_FUNCTION, "translate"));
            out.accept(new CssToken(CssTokenType.TT_NUMBER, tr.getTx()));
            out.accept(new CssToken(CssTokenType.TT_COMMA));
            out.accept(new CssToken(CssTokenType.TT_NUMBER, tr.getTy()));
            if (tr.getTz() != 0.0) {
                out.accept(new CssToken(CssTokenType.TT_COMMA));
                out.accept(new CssToken(CssTokenType.TT_NUMBER, tr.getTz()));
            }
            out.accept(new CssToken(CssTokenType.TT_RIGHT_BRACKET));
        } else if (tx instanceof Scale ts) {
            out.accept(new CssToken(CssTokenType.TT_FUNCTION, "scale"));
            out.accept(new CssToken(CssTokenType.TT_NUMBER, ts.getX()));
            if (ts.getY() != ts.getX() || ts.getZ() != 1 || ts.getPivotX() != 0 || ts.getPivotY() != 0) {
                out.accept(new CssToken(CssTokenType.TT_COMMA));
                out.accept(new CssToken(CssTokenType.TT_NUMBER, ts.getY()));
            }
            if (ts.getZ() != 1) {
                out.accept(new CssToken(CssTokenType.TT_COMMA));
                out.accept(new CssToken(CssTokenType.TT_NUMBER, ts.getZ()));
                if (ts.getPivotX() != 0 || ts.getPivotY() != 0 || ts.getPivotZ() != 0) {
                    out.accept(new CssToken(CssTokenType.TT_COMMA));
                    out.accept(new CssToken(CssTokenType.TT_S, " "));
                    out.accept(new CssToken(CssTokenType.TT_NUMBER, ts.getPivotX()));
                    out.accept(new CssToken(CssTokenType.TT_COMMA));
                    out.accept(new CssToken(CssTokenType.TT_NUMBER, ts.getPivotY()));
                    out.accept(new CssToken(CssTokenType.TT_COMMA));
                    out.accept(new CssToken(CssTokenType.TT_NUMBER, ts.getPivotZ()));
                }
            } else if (ts.getPivotX() != 0 || ts.getPivotY() != 0) {
                out.accept(new CssToken(CssTokenType.TT_COMMA));
                out.accept(new CssToken(CssTokenType.TT_S, " "));
                out.accept(new CssToken(CssTokenType.TT_NUMBER, ts.getPivotX()));
                out.accept(new CssToken(CssTokenType.TT_COMMA));
                out.accept(new CssToken(CssTokenType.TT_NUMBER, ts.getPivotY()));
            }
            out.accept(new CssToken(CssTokenType.TT_RIGHT_BRACKET));

        } else if (tx instanceof Rotate tr) {
            out.accept(new CssToken(CssTokenType.TT_FUNCTION, "rotate"));
            out.accept(new CssToken(CssTokenType.TT_NUMBER, tr.getAngle()));
            if (tr.getPivotX() != 0.0 || tr.getPivotY() != 0.0 || tr.getPivotZ() != 0.0
                    || !tr.getAxis().equals(Rotate.Z_AXIS)) {
                out.accept(new CssToken(CssTokenType.TT_COMMA));
                out.accept(new CssToken(CssTokenType.TT_S, " "));
                out.accept(new CssToken(CssTokenType.TT_NUMBER, tr.getPivotX()));
                out.accept(new CssToken(CssTokenType.TT_COMMA));
                out.accept(new CssToken(CssTokenType.TT_NUMBER, tr.getPivotY()));
            }
            if (tr.getPivotZ() != 0.0) {
                out.accept(new CssToken(CssTokenType.TT_COMMA));
                out.accept(new CssToken(CssTokenType.TT_NUMBER, tr.getPivotZ()));
            }
            if (!tr.getAxis().equals(Rotate.Z_AXIS)) {
                Point3D a = tr.getAxis();
                out.accept(new CssToken(CssTokenType.TT_COMMA));
                out.accept(new CssToken(CssTokenType.TT_NUMBER, a.getX()));
                out.accept(new CssToken(CssTokenType.TT_COMMA));
                out.accept(new CssToken(CssTokenType.TT_NUMBER, a.getY()));
                out.accept(new CssToken(CssTokenType.TT_COMMA));
                out.accept(new CssToken(CssTokenType.TT_NUMBER, a.getZ()));
            }
            out.accept(new CssToken(CssTokenType.TT_RIGHT_BRACKET));
        } else if (tx instanceof Shear tr) {
            out.accept(new CssToken(CssTokenType.TT_FUNCTION, "shear"));
            out.accept(new CssToken(CssTokenType.TT_NUMBER, tr.getX()));
            out.accept(new CssToken(CssTokenType.TT_COMMA));
            out.accept(new CssToken(CssTokenType.TT_NUMBER, tr.getY()));
            if (tr.getPivotX() != 0.0 || tr.getPivotY() != 0.0) {
                out.accept(new CssToken(CssTokenType.TT_COMMA));
                out.accept(new CssToken(CssTokenType.TT_NUMBER, tr.getPivotX()));
                out.accept(new CssToken(CssTokenType.TT_COMMA));
                out.accept(new CssToken(CssTokenType.TT_NUMBER, tr.getPivotY()));
            }
            out.accept(new CssToken(CssTokenType.TT_RIGHT_BRACKET));
        } else if (tx.isType2D()) {
            out.accept(new CssToken(CssTokenType.TT_FUNCTION, "matrix"));
            out.accept(new CssToken(CssTokenType.TT_NUMBER, tx.getMxx()));
            out.accept(new CssToken(CssTokenType.TT_COMMA));
            out.accept(new CssToken(CssTokenType.TT_NUMBER, tx.getMyx()));
            out.accept(new CssToken(CssTokenType.TT_COMMA));
            out.accept(new CssToken(CssTokenType.TT_NUMBER, tx.getMxy()));
            out.accept(new CssToken(CssTokenType.TT_COMMA));
            out.accept(new CssToken(CssTokenType.TT_NUMBER, tx.getMyy()));
            out.accept(new CssToken(CssTokenType.TT_COMMA));
            out.accept(new CssToken(CssTokenType.TT_NUMBER, tx.getTx()));
            out.accept(new CssToken(CssTokenType.TT_COMMA));
            out.accept(new CssToken(CssTokenType.TT_NUMBER, tx.getTy()));
            out.accept(new CssToken(CssTokenType.TT_RIGHT_BRACKET));
        } else {
            out.accept(new CssToken(CssTokenType.TT_FUNCTION, "matrix"));
            out.accept(new CssToken(CssTokenType.TT_NUMBER, tx.getMxx()));
            out.accept(new CssToken(CssTokenType.TT_COMMA));
            out.accept(new CssToken(CssTokenType.TT_NUMBER, tx.getMyx()));
            out.accept(new CssToken(CssTokenType.TT_COMMA));
            out.accept(new CssToken(CssTokenType.TT_NUMBER, tx.getMzx()));
            out.accept(new CssToken(CssTokenType.TT_COMMA));
            out.accept(new CssToken(CssTokenType.TT_NUMBER, tx.getMxy()));
            out.accept(new CssToken(CssTokenType.TT_COMMA));
            out.accept(new CssToken(CssTokenType.TT_NUMBER, tx.getMyy()));
            out.accept(new CssToken(CssTokenType.TT_COMMA));
            out.accept(new CssToken(CssTokenType.TT_NUMBER, tx.getMzy()));
            out.accept(new CssToken(CssTokenType.TT_COMMA));
            out.accept(new CssToken(CssTokenType.TT_NUMBER, tx.getMxz()));
            out.accept(new CssToken(CssTokenType.TT_COMMA));
            out.accept(new CssToken(CssTokenType.TT_NUMBER, tx.getMyz()));
            out.accept(new CssToken(CssTokenType.TT_COMMA));
            out.accept(new CssToken(CssTokenType.TT_NUMBER, tx.getMzz()));
            out.accept(new CssToken(CssTokenType.TT_COMMA));
            out.accept(new CssToken(CssTokenType.TT_NUMBER, tx.getTx()));
            out.accept(new CssToken(CssTokenType.TT_COMMA));
            out.accept(new CssToken(CssTokenType.TT_NUMBER, tx.getTy()));
            out.accept(new CssToken(CssTokenType.TT_COMMA));
            out.accept(new CssToken(CssTokenType.TT_NUMBER, tx.getTz()));
            out.accept(new CssToken(CssTokenType.TT_RIGHT_BRACKET));
        }
    }

    @Override
    public @Nullable String getHelpText() {
        return """
               Format of ⟨Transform⟩: ⟨Affine⟩｜⟨Translate⟩｜⟨Scale⟩｜⟨Rotate⟩｜⟨Shear⟩｜⟨Matrix⟩
               Format of ⟨Affine⟩: affine(⟨xx⟩,⟨xy⟩,⟨tx⟩, ⟨yx⟩,⟨yy⟩,⟨ty⟩)
                                 ｜affine(⟨xx⟩,⟨xy⟩,⟨xz⟩,⟨tx⟩, ⟨yx⟩,⟨yy⟩,⟨yz⟩,⟨ty⟩, ⟨zx⟩,⟨zy⟩,⟨zz⟩,⟨tz⟩)
               Format of ⟨Translate⟩: translate(⟨tx⟩,⟨ty⟩)
               Format of ⟨Scale⟩: scale(⟨sx⟩,⟨sy⟩［,⟨pivotx⟩,⟨pivoty⟩］)
               Format of ⟨Rotate⟩: rotate(⟨angle⟩［,⟨pivotx⟩,⟨pivoty⟩］)
               Format of ⟨Shear⟩: shear(⟨shx⟩,⟨shy⟩［,⟨pivotx⟩,⟨pivoty⟩］)
                                | skew(⟨shx⟩,⟨shy⟩［,⟨pivotx⟩,⟨pivoty⟩］)
               Format of ⟨Matrix⟩: matrix(⟨xx⟩,⟨yx⟩, ⟨xy⟩,⟨yy⟩, ⟨tx⟩,⟨ty⟩)
                                 ｜matrix(⟨xx⟩,⟨yx⟩,⟨zx⟩, ⟨xy⟩,⟨yy⟩,⟨zy⟩, ⟨xz⟩,⟨yz⟩,⟨zz⟩, ⟨tx⟩,⟨zx⟩,⟨tz⟩)"""
                ;
    }

    @Override
    public Transform parseNonNull(CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        tt.requireNextToken(CssTokenType.TT_FUNCTION, "⟨Transform⟩: function expected");
        String func = tt.currentStringNonNull();
        int funcPos = tt.getStartPosition();
        List<Double> m = new ArrayList<>();
        while (tt.next() != ')' && tt.current() != CssTokenType.TT_EOF) {
            if (tt.current() != ',') {
                tt.pushBack();
            }
            if (tt.next() != CssTokenType.TT_NUMBER) {
                throw new ParseException("coefficient nb " + m.size() + " expected: \"" + tt.currentString() + "\"", tt.getStartPosition());
            }
            m.add(tt.currentNumberNonNull().doubleValue());
        }
        if (tt.current() != ')') {
            throw new ParseException("')' expected: \"" + tt.currentString() + "\"", tt.getStartPosition());
        }
        return switch (func) {
            case "affine" -> switch (m.size()) {
                case 0 -> new Affine(//
                        1, 0, 0,//
                        0, 1, 0//
                );
                case 6 -> new Affine(//
                        m.get(0), m.get(1), m.get(2),//
                        m.get(3), m.get(4), m.get(5)//
                );
                case 12 -> new Affine(//
                        m.get(0), m.get(1), m.get(2), m.get(3),//
                        m.get(4), m.get(5), m.get(6), m.get(7),//
                        m.get(8), m.get(9), m.get(10), m.get(11)//
                );
                default ->
                        throw new ParseException("6 or 12 coefficients expected, but found " + m.size(), tt.getStartPosition());
            };
            case "matrix" -> switch (m.size()) {
                case 0 -> new Affine(//
                        1, 0, 0,//
                        0, 1, 0//
                );
                case 6 -> new Affine(//
                        m.get(0), m.get(2), m.get(4),//
                        m.get(1), m.get(3), m.get(5)//
                );
                case 12 -> new Affine(//
                        m.get(0), m.get(3), m.get(6), m.get(9),//
                        m.get(1), m.get(4), m.get(7), m.get(10),//
                        m.get(2), m.get(5), m.get(8), m.get(11)//
                );
                default ->
                        throw new ParseException("6 or 12 coefficients expected, but found " + m.size(), tt.getStartPosition());
            };
            case "skew", "shear" -> switch (m.size()) {
                case 0 -> Transform.shear(0, 0);
                case 2 -> Transform.shear(m.get(0), m.get(1));
                case 4 -> Transform.shear(
                        m.get(0), m.get(1), m.get(2), m.get(3)//
                );
                default ->
                        throw new ParseException("2 or 4 coefficients expected, but found " + m.size(), tt.getStartPosition());
            };
            case "translate" -> switch (m.size()) {
                case 0 -> Transform.translate(//
                        0, 0//
                );//
                case 1 -> Transform.translate(
                        m.get(0), 0//
                );
                case 2 -> Transform.translate(
                        m.get(0), m.get(1)//
                );
                case 3 -> new Translate(
                        m.get(0), m.get(1), m.get(2)//
                );
                default ->
                        throw new ParseException("1, 2 or 3 coefficients expected, but found " + m.size(), tt.getStartPosition());
            };
            case "scale" -> switch (m.size()) {
                case 0 -> Transform.scale(//
                        1, 1//
                );
                case 1 -> Transform.scale(//
                        m.get(0), m.get(0)//
                );
                case 2 -> Transform.scale(
                        m.get(0), m.get(1)//
                );
                case 3 -> new Scale(
                        m.get(0), m.get(0), m.get(1), m.get(2)//
                );
                case 4 -> Transform.scale(
                        m.get(0), m.get(1), m.get(2), m.get(3)//
                );
                case 6 -> new Scale(
                        m.get(0), m.get(1), m.get(2),//
                        m.get(3), m.get(4), m.get(5)//

                );
                default ->
                        throw new ParseException("1, 2, 3, 4, or 6 coefficients expected, but found " + m.size(), tt.getStartPosition());
            };
            case "rotate" -> switch (m.size()) {
                case 0 -> Transform.rotate(//
                        0,//
                        0, 0//
                );
                case 1 -> Transform.rotate(//
                        m.get(0),//
                        0, 0//
                );
                case 3 -> Transform.rotate(//
                        m.get(0),//
                        m.get(1), m.get(2)//
                );
                case 4 -> new Rotate(//
                        m.get(0),//
                        m.get(1), m.get(2), m.get(3)//
                );
                case 6 -> new Rotate(//
                        m.get(0),//
                        m.get(1), m.get(2), 0,//
                        new Point3D(m.get(3), m.get(4), m.get(5))//
                );
                case 7 -> new Rotate(//
                        m.get(0),//
                        m.get(1), m.get(2), m.get(3),//
                        new Point3D(m.get(4), m.get(5), m.get(6))//
                );
                default ->
                        throw new ParseException("1, 3, 4, 6 or 7 coefficients expected, but found " + m.size(), tt.getStartPosition());
            };
            default -> throw new ParseException("unsupported function: \"" + func + "\"", funcPos);
        };

    }

}
