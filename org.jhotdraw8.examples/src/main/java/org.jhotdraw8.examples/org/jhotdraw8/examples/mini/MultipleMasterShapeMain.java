package org.jhotdraw8.examples.mini;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.jhotdraw8.geom.FXSvgPaths;

import java.util.Map;

public class MultipleMasterShapeMain extends Application {
    @Override
    public void start(Stage stage) {
        Map.Entry<Parent, MultipleMasterShapeController> entry = MultipleMasterShapeController.newInstance();

        MultipleMasterShapeController ctrl = entry.getValue();

        // The following shapes are from
        // https://github.com/marella/material-symbols
        // Apache 2.0 License
        // MaterialSymbols/material-symbols/svg/100/sharp/key.svg
        ctrl.defaultSvgPathProperty().set("M280.212-444Q265-444 254.5-454.288q-10.5-10.287-10.5-25.5Q244-495 254.288-505.5q10.287-10.5 25.5-10.5Q295-516 305.5-505.712q10.5 10.287 10.5 25.5Q316-465 305.712-454.5q-10.287 10.5-25.5 10.5ZM280-292q-78 0-133-55T92-480q0-78 55-133t133-55q61 0 109.5 34t67.25 91H831l66 66-108 102-67-50-73 52-64-44H457q-18 54-66 89.5T280-292Zm0-22q60 0 103.5-37.5t55.306-87.5H593l56 39 74-52 66 50 77-76-43-43H439q-11-49-54.304-87-43.305-38-104.435-38Q211-646 162.5-597.659q-48.5 48.34-48.5 117.5Q114-411 162.387-362.5 210.775-314 280-314Z");
        // MaterialSymbols/material-symbols/svg/700/sharp/key.svg
        ctrl.dim1SvgPathProperty().set("M280.248-401Q248-401 224.5-424.252t-23.5-55.5Q201-512 224.252-535.5t55.5-23.5Q312-559 335.5-535.748t23.5 55.5Q359-448 335.748-424.5t-55.5 23.5ZM280-217q-108.667 0-185.833-77.235Q17-371.471 17-480.235 17-589 94.167-666 171.333-743 280-743q80 0 140 38t91.083 111H863l125 124-183 169-89-66-87 63-79-62h-39q-25 63-84.332 106Q367.335-217 280-217Zm0-83q61 0 110.5-41T452-448h127l50 42 88-63 82 60 73-60-42-43H452q-9-60-58.596-104Q343.809-660 280-660q-75 0-127.5 52.5T100-480q0 75 52.5 127.5T280-300Z");
        // MaterialSymbols/material-symbols/svg/100/sharp/key-fill.svg
        //ctrl.dim2SvgPathProperty().set("M280-417q26.5 0 44.75-18.25T343-480q0-26.5-18.25-44.75T280-543q-26.5 0-44.75 18.25T217-480q0 26.5 18.25 44.75T280-417Zm0 111q-74 0-124-50t-50-124q0-74 50-124t124-50q65 0 110.5 36t56.5 85h373l52 53-83 84-66-49-72 52-54-35H447q-9 49-55.5 85.5T280-306Z");


        ctrl.scaleFactorProperty().set(-2);

        stage.setScene(new Scene(entry.getKey()));
        stage.setTitle("MultipleMasterShape");
        stage.sizeToScene();
        stage.show();

        Text tx = new Text("a");
        tx.setFont(Font.font("Helvetica Neue Bold", 144f));
        Path path = (Path) Shape.subtract(tx, new Rectangle());
        System.out.println(FXSvgPaths.doubleSvgStringFromPathElements(path.getElements()));
    }
}
