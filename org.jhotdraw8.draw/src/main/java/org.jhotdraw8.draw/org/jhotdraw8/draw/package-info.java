/*
 * @(#)package-info.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
/**
 * <p>
 * Defines a programming model for structured drawing editors and provides default
 * implementations.
 * <p>
 * Package Contents:
 * <p>
 * All key contracts of the framework are defined by Java interfaces. For each
 * interface exists an abstract class, which implements the event handling
 * contract of the interface. And finally, there is at least one default
 * implementation of each interface.
 * <p>
 * The key interfaces for the representation of a drawing are:
 * <ul>
 * <li>{@link org.jhotdraw8.draw.figure.Drawing}</li>
 * <li>{@link org.jhotdraw8.draw.figure.Figure}</li>
 * </ul>
 * <p>
 * The key interface for displaying a drawing on screen is:
 * <ul>
 * <li>{@link org.jhotdraw8.draw.DrawingView}</li>
 * </ul>
 * <p>
 * The key interfaces for editing a drawing are:
 * <ul>
 * <li>{@link org.jhotdraw8.draw.DrawingEditor}</li>
 * <li>{@link org.jhotdraw8.draw.tool.Tool} (in sub-package "tool")</li>
 * <li>{@link org.jhotdraw8.draw.handle.Handle} (in sub-package "handle")</li>
 * </ul>
 */
package org.jhotdraw8.draw;
