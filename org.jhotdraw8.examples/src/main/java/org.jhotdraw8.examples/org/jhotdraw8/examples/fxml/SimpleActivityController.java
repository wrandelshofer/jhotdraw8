/*
 * @(#)SimpleActivityController.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.examples.fxml;


import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.Node;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.app.AbstractActivity;
import org.jhotdraw8.app.action.Action;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Sample Skeleton for 'SimpleActivity.fxml' Controller Class
 */
public class SimpleActivityController extends AbstractActivity {

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML
    private Node node;

    @Override
    public Node getNode() {
        return node;
    }

    @Override
    protected void initActions(@NonNull ObservableMap<String, Action> actionMap) {

    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initTitle() {

    }

    @FXML
    void initialize() {

    }

}
