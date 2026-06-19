package com.partnersdashboard.ui;

import com.partnersdashboard.ui.common.UIStyles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/** Optional landing screen kept partner-only. */
public class HomeScreen {
    private static final int W = 1100;
    private static final int H = 680;

    public HomeScreen(Stage stage) {
        stage.setTitle("Gravio Partner Dashboard");
        stage.setScene(createScene(stage));
        stage.show();
    }

    public Scene createScene(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + UIStyles.COLOR_BG + ";");

        VBox center = new VBox(18);
        center.setPadding(new Insets(40));
        center.setAlignment(Pos.CENTER);

        Label title = new Label("Gravio Partner Dashboard");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + UIStyles.COLOR_TEXT_DARK + ";");
        Label sub = new Label("Partner-only build. Login, resident, and admin screens are removed in this package.");
        sub.setStyle("-fx-font-size: 14px; -fx-text-fill: " + UIStyles.COLOR_TEXT_MID + ";");
        Button open = new Button("Open Partner Dashboard");
        open.setStyle(UIStyles.STYLE_BTN_PRIMARY);
        open.setOnAction(e -> new PartnerDashboard(stage));

        center.getChildren().addAll(title, sub, open);
        root.setCenter(center);
        return new Scene(root, W, H);
    }
}
