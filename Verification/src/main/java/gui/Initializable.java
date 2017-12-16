package gui;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URL;
import java.util.ResourceBundle;

public interface Initializable extends javafx.fxml.Initializable {
    @Override
    void initialize(@Nonnull URL url, @Nullable ResourceBundle resources);
}
