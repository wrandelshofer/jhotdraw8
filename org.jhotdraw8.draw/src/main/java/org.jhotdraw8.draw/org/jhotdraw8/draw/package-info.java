/*
 * @(#)package-info.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
///
/// Defines a programming model for structured drawing editors and provides default
/// implementations.
///
/// Package Contents:
///
/// All key contracts of the framework are defined by Java interfaces. For each
/// interface exists an abstract class, which implements the event handling
/// contract of the interface. And finally, there is at least one default
/// implementation of each interface.
///
/// The key interfaces for the representation of a drawing are:
///
///   - [org.jhotdraw8.draw.figure.Drawing]
///   - [org.jhotdraw8.draw.figure.Figure]
///
///
/// The key interface for displaying a drawing on screen is:
///
///   - [org.jhotdraw8.draw.DrawingView]
///
///
/// The key interfaces for editing a drawing are:
///
///   - [org.jhotdraw8.draw.DrawingEditor]
///   - [org.jhotdraw8.draw.tool.Tool] (in sub-package "tool")
///   - [org.jhotdraw8.draw.handle.Handle] (in sub-package "handle")
///
package org.jhotdraw8.draw;

