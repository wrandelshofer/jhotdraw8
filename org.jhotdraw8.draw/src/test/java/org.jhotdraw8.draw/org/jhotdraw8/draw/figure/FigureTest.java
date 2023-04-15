/*
 * @(#)FigureTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.StyleOrigin;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.transform.Transform;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.event.Listener;
import org.jhotdraw8.collection.champ.ChampSet;
import org.jhotdraw8.collection.readonly.ReadOnlySet;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.connector.Connector;
import org.jhotdraw8.draw.css.value.CssRectangle2D;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.fxbase.styleable.AbstractStyleablePropertyBean;
import org.jhotdraw8.fxbase.styleable.StyleableBean;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.geom.FXTransforms;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author werni
 */
public class FigureTest {


    /**
     * Test of getDeclaredAndInheritedMapAccessors method, of class Figure.
     */
    @Test
    public void testGetDeclaredAndInheritedKeys() {
        Set<MapAccessor<?>> figureKeys = Figure.getDeclaredAndInheritedMapAccessors(Figure.class);
        Set<MapAccessor<?>> rectangleFigureKeys = Figure.getDeclaredAndInheritedMapAccessors(RectangleFigure.class);
        //System.out.println("rr:" + rectangleFigureKeys);
        Set<MapAccessor<?>> intersection = new HashSet<>(figureKeys);
        intersection.retainAll(rectangleFigureKeys);
        //System.out.println("ri:" + intersection);
        assertEquals(figureKeys, intersection);
    }

    public static class FigureImpl extends AbstractStyleablePropertyBean implements Figure {

        @Override
        public void removeLayoutSubject(Figure targetFigure) {
        }

        @Override
        public void removeAllLayoutSubjects() {
        }

        @Override
        public @NonNull ObjectProperty<Figure> parentProperty() {
            return null;
        }

        @Override
        public @NonNull Bounds getLayoutBounds() {
            return null;
        }

        @Override
        public @NonNull CssRectangle2D getCssLayoutBounds() {
            return new CssRectangle2D(getLayoutBounds());
        }

        @Override
        public void reshapeInLocal(Transform transform) {
        }

        @Override
        public void reshapeInLocal(@NonNull CssSize x, @NonNull CssSize y, @NonNull CssSize width, @NonNull CssSize height) {
            // empty
        }

        @Override
        public @NonNull Node createNode(@NonNull RenderContext ctx) {
            return null;
        }

        @Override
        public void updateNode(@NonNull RenderContext ctx, @NonNull Node node) {
        }

        @Override
        public boolean isAllowsChildren() {
            return false;
        }

        @Override
        public boolean isSuitableParent(@NonNull Figure newParent) {
            return false;
        }

        @Override
        public boolean isSuitableChild(@NonNull Figure newChild) {
            return false;
        }

        @Override
        public boolean isSelectable() {
            return false;
        }

        @Override
        public boolean isDeletable() {
            return false;
        }

        @Override
        public boolean isEditable() {
            return false;
        }

        public @Nullable Connector findConnector(Point2D pointInLocal, Figure prototype) {
            return null;
        }

        @Override
        public void updateCss(RenderContext ctx) {
        }

        @Override
        public void addedToDrawing(@NonNull Drawing drawing) {
        }

        @Override
        public void removedFromDrawing(Drawing drawing) {
        }

        @Override
        public @NonNull ObservableList<Figure> getChildren() {
            return FXCollections.emptyObservableList();
        }

        @Override
        public @NonNull Set<Figure> getLayoutObservers() {
            return Collections.emptySet();
        }

        @Override
        public @NonNull ReadOnlySet<Figure> getReadOnlyLayoutObservers() {
            return ChampSet.of();
        }

        @Override
        public @NonNull Transform getParentToLocal() {
            return FXTransforms.IDENTITY;
        }

        @Override
        public @NonNull Bounds getBoundsInLocal() {
            return getLayoutBounds();
        }

        @Override
        public @NonNull Transform getLocalToParent() {
            return FXTransforms.IDENTITY;
        }

        @Override
        public @NonNull Transform getWorldToLocal() {
            return FXTransforms.IDENTITY;
        }

        @Override
        public @NonNull Transform getWorldToParent() {
            return FXTransforms.IDENTITY;
        }

        @Override
        public @NonNull Transform getLocalToWorld() {
            return FXTransforms.IDENTITY;
        }

        @Override
        public @NonNull Transform getParentToWorld() {
            return FXTransforms.IDENTITY;
        }

        @Override
        public void invalidateTransforms() {

        }

        @Override
        public @Nullable CopyOnWriteArrayList<Listener<FigurePropertyChangeEvent>> getPropertyChangeListeners() {
            return null;
        }

        @Override
        public boolean hasPropertyChangeListeners() {
            return false;
        }

        @Override
        public <T> T getStyled(@NonNull MapAccessor<T> key) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public <T> T setStyled(@NonNull StyleOrigin origin, @NonNull MapAccessor<T> key, T value) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public <T> T remove(@NonNull StyleOrigin origin, @NonNull MapAccessor<T> key) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void removeAll(@NonNull StyleOrigin origin) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public @NonNull String getTypeSelector() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public @NonNull String getId() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public @NonNull ReadOnlyProperty<String> idProperty() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public @NonNull ReadOnlySet<String> getStyleClasses() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public @NonNull String getStyle() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public @NonNull StyleableBean getStyleableParent() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public @NonNull ReadOnlySet<String> getPseudoClassStates() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Figure getParent() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void reshapeInParent(@NonNull Transform transform) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void transformInParent(@NonNull Transform transform) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void transformInLocal(@NonNull Transform transform) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

}
