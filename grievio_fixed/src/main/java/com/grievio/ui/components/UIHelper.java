package com.grievio.ui.components;

import com.grievio.util.AIRouter;
import javafx.animation.FadeTransition;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

public class UIHelper {

    // ─── Labels ──────────────────────────────────────────────────────────────

    public static Label lbl(String text, double size, String color) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:" + size + "px;-fx-text-fill:" + color + ";");
        return l;
    }

    public static Label lbl(String text, double size, String color, boolean bold) {
        Label l = lbl(text, size, color);
        if (bold) l.setStyle(l.getStyle() + "-fx-font-weight:bold;");
        return l;
    }

    public static Label sectionTitle(String t) {
        return lbl(t, 18, "white", true);
    }

    public static Label cardTitle(String t) {
        return lbl(t, 15, "#90caf9", true);
    }

    // ─── Inputs ──────────────────────────────────────────────────────────────

    public static TextField inputField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.getStyleClass().add("input-field");
        tf.setMaxWidth(Double.MAX_VALUE);
        return tf;
    }

    public static ComboBox<String> comboField() {
        ComboBox<String> cb = new ComboBox<>();
        cb.getStyleClass().add("combo-field");
        cb.setMaxWidth(Double.MAX_VALUE);
        return cb;
    }

    public static TextArea textArea(String prompt) {
        TextArea ta = new TextArea();
        ta.setPromptText(prompt);
        ta.setPrefHeight(90);
        ta.getStyleClass().add("text-area-field");
        return ta;
    }

    public static Label formLbl(String text) {
        return lbl(text, 12, "#90caf9", true);
    }

    public static VBox fieldGroup(String label, Node field) {
        VBox g = new VBox(5, formLbl(label), field);
        g.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(g, Priority.ALWAYS);
        return g;
    }

    // ─── Buttons ─────────────────────────────────────────────────────────────

    public static Button primaryBtn(String text) {
        Button b = new Button(text);
        b.getStyleClass().add("btn-primary");
        return b;
    }

    public static Button secondaryBtn(String text) {
        Button b = new Button(text);
        b.getStyleClass().add("btn-secondary");
        return b;
    }

    public static Button dangerBtn(String text) {
        Button b = new Button(text);
        b.getStyleClass().add("btn-danger");
        return b;
    }

    public static Button successBtn(String text) {
        Button b = new Button(text);
        b.getStyleClass().add("btn-success");
        return b;
    }

    // ─── Cards ───────────────────────────────────────────────────────────────

    public static VBox statCard(String title, Object value, String color1, String color2) {
        VBox card = new VBox(6);
        card.setStyle("-fx-background-color:linear-gradient(to bottom right," + color1 + "," + color2 + ");" +
            "-fx-background-radius:14;-fx-padding:18;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.4),10,0,0,3);");
        card.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(card, Priority.ALWAYS);
        card.getChildren().addAll(
            lbl(String.valueOf(value), 28, "white", true),
            lbl(title, 12, "rgba(255,255,255,0.85)")
        );
        return card;
    }

    public static VBox formCard(String title) {
        VBox card = new VBox(14);
        card.getStyleClass().add("form-card");
        if (title != null && !title.isEmpty()) card.getChildren().add(cardTitle(title));
        return card;
    }

    public static VBox infoCard(String title, String subtitle) {
        VBox card = new VBox(4);
        card.setStyle("-fx-background-color:#12294a;-fx-background-radius:12;-fx-padding:16;" +
            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.3),8,0,0,2);");
        card.getChildren().addAll(lbl(title, 13, "white", true), lbl(subtitle, 11, "#78909c"));
        return card;
    }

    // ─── Tables ──────────────────────────────────────────────────────────────

    public static VBox tableRow(String label, String value) {
        HBox row = new HBox();
        row.setStyle("-fx-padding:8 0;-fx-border-color:transparent transparent rgba(255,255,255,0.07) transparent;" +
            "-fx-border-width:0 0 1 0;");
        Label lbl1 = lbl(label, 12, "#78909c");
        Label lbl2 = lbl(value != null ? value : "—", 12, "#e0e0e0");
        lbl1.setMinWidth(140);
        row.getChildren().addAll(lbl1, lbl2);
        VBox v = new VBox(row);
        return v;
    }

    // ─── Status/Priority Badges ───────────────────────────────────────────────

    public static Label statusBadge(String status) {
        Label l = new Label(status);
        String color = AIRouter.statusColor(status);
        l.setStyle("-fx-background-color:" + color + "22;-fx-text-fill:" + color + ";" +
            "-fx-background-radius:20;-fx-padding:3 10;-fx-font-size:11px;-fx-font-weight:bold;");
        return l;
    }

    public static Label priorityBadge(String priority) {
        Label l = new Label(priority);
        String color = AIRouter.priorityColor(priority);
        l.setStyle("-fx-background-color:" + color + "22;-fx-text-fill:" + color + ";" +
            "-fx-background-radius:20;-fx-padding:3 10;-fx-font-size:11px;-fx-font-weight:bold;");
        return l;
    }

    // ─── Animations ──────────────────────────────────────────────────────────

    public static void fadeIn(Node node) {
        node.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(300), node);
        ft.setToValue(1);
        ft.play();
    }

    // ─── Separators ──────────────────────────────────────────────────────────

    public static Separator mkSep() {
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color:#1565c0;-fx-opacity:0.3;");
        return sep;
    }

    public static Region mkRegion(double h) {
        Region r = new Region();
        r.setMinHeight(h);
        return r;
    }

    // ─── Alert ───────────────────────────────────────────────────────────────

    public static void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.setTitle("Grievio");
        DialogPane dp = a.getDialogPane();
        dp.setStyle("-fx-background-color:#0d1b2a;-fx-text-fill:white;");
        a.showAndWait();
    }

    public static void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.setTitle("Error");
        a.showAndWait();
    }

    // ─── Scroll Pane ─────────────────────────────────────────────────────────

    public static ScrollPane wrapScroll(Node content) {
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color:#0d1b2a;-fx-background:#0d1b2a;");
        VBox.setVgrow(sp, Priority.ALWAYS);
        return sp;
    }
}
