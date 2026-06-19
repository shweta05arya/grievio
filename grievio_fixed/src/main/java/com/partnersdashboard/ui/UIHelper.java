package com.partnersdashboard.ui;

import com.partnersdashboard.ui.common.UIStyles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * UIHelper - Factory for reusable styled JavaFX components.
 * Keeps UI code DRY and consistent across all screens.
 */
public final class UIHelper {

    /** Create a styled primary button. */
    public static Button primaryButton(String text) {
        Button btn = new Button(text);
        btn.setStyle(UIStyles.STYLE_BTN_PRIMARY);
        btn.setPrefHeight(42);
        btn.setMaxWidth(Double.MAX_VALUE);
        addHoverEffect(btn, UIStyles.COLOR_BLUE_DARK, UIStyles.STYLE_BTN_PRIMARY);
        return btn;
    }

    /** Create a styled danger button. */
    public static Button dangerButton(String text) {
        Button btn = new Button(text);
        btn.setStyle(UIStyles.STYLE_BTN_DANGER);
        btn.setPrefHeight(38);
        return btn;
    }

    /** Create a styled success button. */
    public static Button successButton(String text) {
        Button btn = new Button(text);
        btn.setStyle(UIStyles.STYLE_BTN_SUCCESS);
        btn.setPrefHeight(38);
        return btn;
    }

    /** Create a styled text field. */
    public static TextField styledTextField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle(UIStyles.STYLE_INPUT);
        tf.setPrefHeight(42);
        return tf;
    }

    /** Create a styled password field. */
    public static PasswordField styledPasswordField(String prompt) {
        PasswordField pf = new PasswordField();
        pf.setPromptText(prompt);
        pf.setStyle(UIStyles.STYLE_INPUT);
        pf.setPrefHeight(42);
        return pf;
    }

    /** Create a heading label. */
    public static Label headingLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle(UIStyles.STYLE_LABEL_HEADING);
        return lbl;
    }

    /** Create a subtext/caption label. */
    public static Label subtextLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle(UIStyles.STYLE_LABEL_SUBTEXT);
        return lbl;
    }

    /** Create a white section card with padding. */
    public static VBox card(double padding) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(padding));
        card.setStyle(UIStyles.STYLE_CARD);
        return card;
    }

    /** Create a status badge label for complaints. */
    public static Label statusBadge(String status) {
        Label badge = new Label(UIStyles.statusLabel(status));
        badge.setStyle(UIStyles.badgeStyle(status));
        return badge;
    }

    /** Create a priority badge. */
    public static Label priorityBadge(String priority) {
        String color = switch (priority != null ? priority : "") {
            case "HIGH"   -> UIStyles.COLOR_DANGER;
            case "MEDIUM" -> UIStyles.COLOR_WARNING;
            case "LOW"    -> UIStyles.COLOR_SUCCESS;
            default       -> "#9CA3AF";
        };
        Label badge = new Label(priority != null ? priority : "—");
        badge.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: white; -fx-background-radius: 20; " +
                        "-fx-padding: 3 10; -fx-font-size: 11px; -fx-font-weight: bold;", color));
        return badge;
    }

    /** Build a metric card for dashboards (number + label). */
    public static VBox metricCard(String value, String label, String accentColor) {
        VBox card = card(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMinWidth(160);

        Label valLbl = new Label(value);
        valLbl.setStyle(String.format(
                "-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: %s;", accentColor));

        Label nameLbl = new Label(label);
        nameLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: " + UIStyles.COLOR_TEXT_MID + ";");

        // Colored accent bar at top
        Region bar = new Region();
        bar.setPrefHeight(4);
        bar.setMaxWidth(40);
        bar.setStyle("-fx-background-color: " + accentColor + "; -fx-background-radius: 2;");

        card.getChildren().addAll(bar, valLbl, nameLbl);
        return card;
    }

    /** Show a styled information alert. */
    public static void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /** Show error alert. */
    public static void showError(String title, String message) {
        showAlert(title, message, Alert.AlertType.ERROR);
    }

    /** Show success alert. */
    public static void showSuccess(String title, String message) {
        showAlert(title, message, Alert.AlertType.INFORMATION);
    }

    /** Show confirmation dialog. Returns true if user clicks OK. */
    public static boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait()
                .map(btn -> btn == ButtonType.OK)
                .orElse(false);
    }

    /** Show a TextInputDialog and return the entered value (or null). */
    public static String showInputDialog(String title, String prompt, String header) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(prompt);
        return dialog.showAndWait().orElse(null);
    }

    /** Create sidebar navigation button. */
    public static Button sidebarButton(String text, boolean active) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(12, 20, 12, 20));
        btn.setStyle(active
                ? "-fx-background-color: rgba(255,255,255,0.15); -fx-text-fill: white; " +
                "-fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;"
                : "-fx-background-color: transparent; -fx-text-fill: " + UIStyles.COLOR_TEXT_LIGHT + "; " +
                "-fx-font-size: 13px; -fx-background-radius: 8; -fx-cursor: hand;"
        );
        return btn;
    }

    /** Separator line. */
    public static Separator separator() {
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: " + UIStyles.COLOR_BORDER + ";");
        return sep;
    }

    // ── Private Helpers ──────────────────────────────────────────────────────
    private static void addHoverEffect(Button btn, String hoverColor, String normalStyle) {
        String hoverStyle = normalStyle.replace(UIStyles.COLOR_BLUE, hoverColor)
                .replace(UIStyles.COLOR_BLUE_DARK, hoverColor);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(normalStyle));
    }

    private UIHelper() {}
}