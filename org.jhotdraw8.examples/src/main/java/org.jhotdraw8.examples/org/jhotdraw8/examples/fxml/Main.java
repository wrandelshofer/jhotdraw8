package org.jhotdraw8.examples.fxml;

public class Main extends FxmlApplication {
    @Override
    public void initApplication() throws Exception {
        setFxml(Main.class.getResource("SimpleActivity.fxml"));
    }
}
