/*
 * @(#)IdentityObject.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.impl;

import java.io.Serial;
import java.io.Serializable;

/// `Mutability ownership` is a mechanism that decides who is allowed to change
/// a data object.
///
/// The [MutabilityOwnership] object is used as a token.
/// The data object stores the token in a field.
/// Methods that change the data object, use the token as a parameter.
/// The data object can only be changed, if the provided token matches the
/// stored token.
///
/// Example:
/// ```
/// class DataObject {
///   private final @Nullable MutabilityOwnership owner;
///   private String name;
///   public DataObject(MutabilityOwnership owner, String name) {
///      this.owner=owner; this.name=name;
///   }
///   public boolean isOwnedBy(MutabilityOwnership owner) {
///      return owner != null && owner == this.owner;
///   }
///   public DataObject settingName(MutabilityOwnership owner, String name) {
///     if (isOwnedBy(owner)) {
///         this.name=name;return this;
///     }
///     return new DataObject(owner, name);
///   }
/// }
///
/// ```
@SuppressWarnings("FinalClass")
public final class MutabilityOwnership implements Serializable {
    @Serial
    private static final long serialVersionUID = 0L;

    public MutabilityOwnership() {
    }
}
