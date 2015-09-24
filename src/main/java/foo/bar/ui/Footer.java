package foo.bar.ui;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;

/**
 * @author Heiko Braun
 * @since 24/09/15
 */
public class Footer extends HBox {

    private final ProgressBar progress;

    public Footer() {


        setPadding(new Insets(5, 5, 5, 5));
        setHeight(10);

        progress = new ProgressBar();
        setProgressVisible(false);
        getChildren().add(progress);

    }

    public void setProgressVisible(boolean b) {

        double val = b ? -1.0 : 0.0;
        progress.setProgress(val);
    }
}
