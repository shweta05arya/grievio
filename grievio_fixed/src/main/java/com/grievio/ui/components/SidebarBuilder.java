package com.grievio.ui.components;

import com.grievio.session.SessionManager;
import com.grievio.model.User;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

import static com.grievio.ui.components.UIHelper.*;

public class SidebarBuilder {

    public record NavItem(String icon, String label, Runnable action) {}

    public static VBox build(String appTitle, String appSubtitle, List<NavItem> items,
                              Runnable onLogout, Button[] activeRef) {
        VBox sidebar = new VBox(4);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(240);
        sidebar.setPadding(new Insets(20, 10, 20, 10));

        // Brand
        HBox brand = new HBox(10);
        brand.setAlignment(Pos.CENTER_LEFT);
        brand.setPadding(new Insets(0, 0, 16, 0));
        VBox brandText = new VBox(2,
            lbl("Grievio", 20, "white", true),
            lbl(appSubtitle, 9, "#64b5f6"));
        brand.getChildren().addAll(lbl("🔵", 22, "white"), brandText);

        // User info
        User user = SessionManager.getInstance().getCurrentUser();
        VBox userInfo = new VBox(3);
        userInfo.setStyle("-fx-background-color:rgba(21,101,192,0.15);-fx-background-radius:10;" +
            "-fx-padding:10 12;-fx-border-color:#1565c0;-fx-border-radius:10;-fx-border-width:1;");
        userInfo.getChildren().addAll(
            lbl("👤  " + (user != null ? user.getName() : "User"), 12, "white", true),
            lbl(appTitle, 10, "#64b5f6")
        );

        sidebar.getChildren().addAll(brand, mkSep(), mkRegion(4), userInfo, mkRegion(8));

        Button[] current = {null};
        for (NavItem item : items) {
            Button btn = navBtn(item.icon() + "   " + item.label());
            btn.setOnAction(e -> {
                if (current[0] != null) current[0].getStyleClass().remove("nav-active");
                btn.getStyleClass().add("nav-active");
                current[0] = btn;
                if (activeRef != null) activeRef[0] = btn;
                item.action().run();
            });
            sidebar.getChildren().add(btn);
        }

        if (!items.isEmpty()) {
            // Activate first item
            Button first = (Button) sidebar.getChildren().get(sidebar.getChildren().size() - items.size());
            first.getStyleClass().add("nav-active");
            current[0] = first;
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("🚪   Logout");
        logoutBtn.getStyleClass().add("nav-btn-logout");
        logoutBtn.setPrefWidth(220);
        logoutBtn.setOnAction(e -> { SessionManager.getInstance().logout(); onLogout.run(); });

        sidebar.getChildren().addAll(spacer, mkSep(), logoutBtn);
        return sidebar;
    }

    private static Button navBtn(String text) {
        Button b = new Button(text);
        b.getStyleClass().add("nav-btn");
        b.setPrefWidth(220);
        return b;
    }
}
