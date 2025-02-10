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
import org.jhotdraw8.base.event.Listener;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.connector.Connector;
import org.jhotdraw8.draw.css.value.CssRectangle2D;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.fxbase.styleable.AbstractStyleablePropertyBean;
import org.jhotdraw8.fxbase.styleable.StyleableBean;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.geom.FXTransforms;
import org.jhotdraw8.icollection.ChampSet;
import org.jhotdraw8.icollection.persistent.PersistentSet;
import org.jhotdraw8.icollection.readable.ReadableSet;
import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 */
public class FigureTest {


    /**
     * Test of getDeclaredAndInheritedMapAccessors method, of class Figure.
     */
    @Test
    public void testGetDeclaredAndInheritedKeys() {
        PersistentSet<MapAccessor<?>> figureKeys = Figure.getDeclaredAndInheritedMapAccessors(Figure.class);
        PersistentSet<MapAccessor<?>> rectangleFigureKeys = Figure.getDeclaredAndInheritedMapAccessors(RectangleFigure.class);
        //System.out.println("rr:" + rectangleFigureKeys.asSet());
        PersistentSet<MapAccessor<?>> intersection = figureKeys.retainAll(rectangleFigureKeys.asSet());
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
        public ObjectProperty<Figure> parentProperty() {
            return null;
        }

        @Override
        public Bounds getLayoutBounds() {
            return null;
        }

        @Override
        public CssRectangle2D getCssLayoutBounds() {
            return new CssRectangle2D(getLayoutBounds());
        }

        @Override
        public void reshapeInLocal(Transform transform) {
        }

        @Override
        public void reshapeInLocal(CssSize x, CssSize y, CssSize width, CssSize height) {
            // empty
        }

        @Override
        public Node createNode(RenderContext ctx) {
            return null;
        }

        @Override
        public void updateNode(RenderContext ctx, Node node) {
        }

        @Override
        public boolean isAllowsChildren() {
            return false;
        }

        @Override
        public boolean isSuitableParent(Figure newParent) {
            return false;
        }

        @Override
        public boolean isSuitableChild(Figure newChild) {
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
        public void addedToDrawing(Drawing drawing) {
        }

        @Override
        public void removedFromDrawing(Drawing drawing) {
        }

        @Override
        public ObservableList<Figure> getChildren() {
            return FXCollections.emptyObservableList();
        }

        @Override
        public Set<Figure> getLayoutObservers() {
            return Collections.emptySet();
        }

        @Override
        public ReadableSet<Figure> getReadOnlyLayoutObservers() {
            return ChampSet.of();
        }

        @Override
        public Transform getParentToLocal() {
            return FXTransforms.IDENTITY;
        }

        @Override
        public Bounds getBoundsInLocal() {
            return getLayoutBounds();
        }

        @Override
        public Transform getLocalToParent() {
            return FXTransforms.IDENTITY;
        }

        @Override
        public Transform getWorldToLocal() {
            return FXTransforms.IDENTITY;
        }

        @Override
        public Transform getWorldToParent() {
            return FXTransforms.IDENTITY;
        }

        @Override
        public Transform getLocalToWorld() {
            return FXTransforms.IDENTITY;
        }

        @Override
        public Transform getParentToWorld() {
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
        public <T> T getStyled(MapAccessor<T> key) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public <T> T setStyled(StyleOrigin origin, MapAccessor<T> key, T value) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public <T> T remove(StyleOrigin origin, MapAccessor<T> key) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void removeAll(StyleOrigin origin) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getTypeSelector() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getId() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public ReadOnlyProperty<String> idProperty() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ReadableSet<String> getStyleClasses() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getStyle() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public StyleableBean getStyleableParent() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public ReadableSet<String> getPseudoClassStates() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Figure getParent() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void reshapeInParent(Transform transform) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void transformInParent(Transform transform) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void transformInLocal(Transform transform) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

}
