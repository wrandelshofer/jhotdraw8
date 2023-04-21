/*
 * @(#)Main.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.examples.fxml;

public class Main extends FxmlApplication {
    @Override
    public void initApplication() throws Exception {
        setFxml(Main.class.getResource("SimpleActivity.fxml"));
    }
}
