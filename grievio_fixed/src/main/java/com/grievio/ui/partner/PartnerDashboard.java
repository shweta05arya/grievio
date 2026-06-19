package com.grievio.ui.partner;

import com.grievio.MainApp;
import com.grievio.database.DatabaseHelper;
import com.grievio.model.User;
import com.grievio.session.SessionManager;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import static com.grievio.ui.components.UIHelper.*;

public class PartnerDashboard {

    private final HBox root;
    private final StackPane contentArea;
    private final Label headerTitleLbl;
    private Button currentNavBtn;

    public PartnerDashboard() {
        root = new HBox();
        root.getStyleClass().add("root-bg");
        headerTitleLbl = lbl("Partner Dashboard", 20, "white", true);
        contentArea = new StackPane();
        contentArea.setStyle("-fx-padding:22;");
        root.getChildren().addAll(buildSidebar(), buildMain());
    }

    public HBox getRoot() { return root; }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(4);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(240);
        sidebar.setPadding(new Insets(20, 10, 20, 10));

        HBox brand = new HBox(10);
        brand.setAlignment(Pos.CENTER_LEFT);
        brand.setPadding(new Insets(0, 0, 14, 0));
        VBox brandText = new VBox(2, lbl("Grievio", 20, "white", true), lbl("Partner Portal", 9, "#a5d6a7"));
        brand.getChildren().addAll(lbl("🔧", 22, "white"), brandText);

        User user = SessionManager.getInstance().getCurrentUser();
        VBox userCard = new VBox(3);
        userCard.setStyle("-fx-background-color:rgba(46,125,50,0.15);-fx-background-radius:10;-fx-padding:10 12;" +
            "-fx-border-color:#66bb6a;-fx-border-radius:10;-fx-border-width:1;");
        userCard.getChildren().addAll(
            lbl("🔧  " + (user != null ? user.getName() : "Partner"), 12, "white", true),
            lbl("Field Worker / Partner", 10, "#a5d6a7"));

        Button bDash     = navBtn("🏠", "My Dashboard");
        Button bAssigned = navBtn("📋", "Assigned Work");
        Button bProgress = navBtn("🔄", "In Progress");
        Button bComplete = navBtn("✅", "Mark Complete");
        Button bHistory  = navBtn("📜", "Work History");
        Button bProfile  = navBtn("👤", "My Profile");

        bDash.setOnAction(e     -> show(buildPartnerDashSection(), bDash,     "Partner Dashboard"));
        bAssigned.setOnAction(e -> show(buildAssignedSection(),    bAssigned, "Assigned Work"));
        bProgress.setOnAction(e -> show(buildInProgressSection(),  bProgress, "In Progress Work"));
        bComplete.setOnAction(e -> show(buildCompleteSection(),    bComplete, "Mark as Complete"));
        bHistory.setOnAction(e  -> show(buildHistorySection(),     bHistory,  "Work History"));
        bProfile.setOnAction(e  -> show(buildProfileSection(),     bProfile,  "My Profile"));

        Button logoutBtn = new Button("🚪   Logout");
        logoutBtn.getStyleClass().add("nav-btn-logout");
        logoutBtn.setPrefWidth(220);
        logoutBtn.setOnAction(e -> { SessionManager.getInstance().logout(); MainApp.showLogin(); });

        Region spacer = new Region(); VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().addAll(brand, mkSep(), mkRegion(4), userCard, mkRegion(6),
            bDash, bAssigned, bProgress, bComplete, bHistory, bProfile, spacer, mkSep(), logoutBtn);
        setActive(bDash); currentNavBtn = bDash;
        return sidebar;
    }

    private Button navBtn(String icon, String label) {
        Button b = new Button(icon + "   " + label);
        b.getStyleClass().add("nav-btn"); b.setPrefWidth(220); return b;
    }

    private void setActive(Button btn) {
        if (currentNavBtn != null) currentNavBtn.getStyleClass().remove("nav-active");
        btn.getStyleClass().add("nav-active"); currentNavBtn = btn;
    }

    private VBox buildMain() {
        VBox main = new VBox(); HBox.setHgrow(main, Priority.ALWAYS);
        HBox header = new HBox();
        header.getStyleClass().add("top-header");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 25, 14, 25));
        Label roleLbl = lbl("🔧  Field Partner / Worker", 13, "#a5d6a7");
        roleLbl.setStyle(roleLbl.getStyle() + "-fx-background-color:rgba(46,125,50,0.2);-fx-background-radius:20;-fx-padding:5 14;");
        Region hSp = new Region(); HBox.setHgrow(hSp, Priority.ALWAYS);
        header.getChildren().addAll(headerTitleLbl, hSp, roleLbl);
        ScrollPane scroll = wrapScroll(contentArea);
        main.getChildren().addAll(header, scroll);
        contentArea.getChildren().setAll(buildPartnerDashSection());
        return main;
    }

    private void show(Node section, Button btn, String title) {
        headerTitleLbl.setText(title);
        setActive(btn);
        contentArea.getChildren().setAll(section);
        fadeIn(section);
    }

    private VBox buildPartnerDashSection() {
        VBox s = new VBox(20);
        User user = SessionManager.getInstance().getCurrentUser();
        String pName = user != null ? user.getName() : "Partner";

        int[] stats = {0, 0, 0};
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT status, COUNT(*) as c FROM complaints WHERE assigned_worker=? GROUP BY status")) {
            ps.setString(1, pName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int c = rs.getInt("c"); stats[0] += c;
                switch (rs.getString("status")) {
                    case "In Progress" -> stats[1] += c;
                    case "Completed"   -> stats[2] += c;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }

        HBox cards = new HBox(15,
            statCard("Total Assigned", stats[0], "#1565c0", "#0d47a1"),
            statCard("In Progress",    stats[1], "#e65100", "#bf360c"),
            statCard("Completed",      stats[2], "#2e7d32", "#1b5e20"));
        for (Node c : cards.getChildren()) HBox.setHgrow(c, Priority.ALWAYS);

        VBox welcomeCard = new VBox(10);
        welcomeCard.setStyle("-fx-background-color:linear-gradient(to right,#0a1f0a,#2e7d32);" +
            "-fx-background-radius:14;-fx-padding:20;");
        welcomeCard.getChildren().addAll(
            lbl("👋  Welcome, " + pName + "!", 18, "white", true),
            lbl("You are a field partner on the Grievio platform. View your assigned complaints, update progress, and mark completed work.", 13, "#a5d6a7"));

        s.getChildren().addAll(sectionTitle("Partner Dashboard"), cards, welcomeCard, buildAssignedSection());
        return s;
    }

    private VBox buildAssignedSection() {
        VBox section = new VBox(16);
        section.getChildren().add(lbl("📋  All Assigned Work", 16, "white", true));
        User user = SessionManager.getInstance().getCurrentUser();
        String pName = user != null ? user.getName() : null;
        if (pName == null) { section.getChildren().add(lbl("Not logged in.", 12, "#ef5350")); return section; }

        // First try exact match
        List<com.grievio.model.Complaint> list = new ArrayList<>();
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT * FROM complaints WHERE assigned_worker=? ORDER BY created_at DESC")) {
            ps.setString(1, pName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                section.getChildren().add(buildPartnerComplaintCard(rs, pName));
                list.add(null); // sentinel
            }
        } catch (SQLException e) { e.printStackTrace(); }

        // Fallback: if no assignments found for this partner, show all assigned complaints
        if (list.isEmpty()) {
            section.getChildren().add(lbl("ℹ  No complaints directly assigned to '" + pName +
                "'. Showing all assigned complaints below:", 12, "#ffb300"));
            try (Connection conn = DatabaseHelper.getConnection();
                 ResultSet rs = conn.createStatement().executeQuery(
                     "SELECT * FROM complaints WHERE assigned_worker IS NOT NULL ORDER BY created_at DESC")) {
                boolean any = false;
                while (rs.next()) { section.getChildren().add(buildPartnerComplaintCard(rs, pName)); any = true; }
                if (!any) section.getChildren().add(lbl("No work assigned yet.", 13, "#78909c"));
            } catch (SQLException e) { e.printStackTrace(); }
        }
        return section;
    }

    private VBox buildInProgressSection() {
        VBox section = new VBox(16);
        section.getChildren().add(sectionTitle("In Progress Work"));
        User user = SessionManager.getInstance().getCurrentUser();
        String pName = user != null ? user.getName() : null;
        if (pName == null) return section;

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT * FROM complaints WHERE assigned_worker=? AND status='In Progress' ORDER BY created_at DESC")) {
            ps.setString(1, pName);
            ResultSet rs = ps.executeQuery();
            boolean any = false;
            while (rs.next()) { section.getChildren().add(buildPartnerComplaintCard(rs, pName)); any = true; }
            if (!any) section.getChildren().add(lbl("No work currently in progress.", 13, "#78909c"));
        } catch (SQLException e) { e.printStackTrace(); }
        return section;
    }

    private VBox buildCompleteSection() {
        VBox section = new VBox(20);
        section.getChildren().add(sectionTitle("Mark Work as Completed"));

        VBox form = formCard("Update Work Status");
        User user = SessionManager.getInstance().getCurrentUser();
        String pName = user != null ? user.getName() : null;

        ComboBox<String> complaintBox = comboField();
        if (pName != null) {
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                     "SELECT complaint_id, title FROM complaints WHERE assigned_worker=? AND status='In Progress'")) {
                ps.setString(1, pName);
                ResultSet rs = ps.executeQuery();
                while (rs.next())
                    complaintBox.getItems().add(rs.getString("complaint_id") + " — " + rs.getString("title"));
            } catch (SQLException ignored) {}
        }

        TextArea noteArea = textArea("Describe work done (e.g., Pipe replaced, leak fixed)...");
        Label resultLbl = lbl("", 12, "#00c853", true);
        Button doneBtn = successBtn("✅  Mark as Completed");
        Button progressBtn = secondaryBtn("🔄  Still In Progress");

        doneBtn.setOnAction(e -> updateComplaintStatus(complaintBox, "Completed", noteArea, resultLbl, pName));
        progressBtn.setOnAction(e -> updateComplaintStatus(complaintBox, "In Progress", noteArea, resultLbl, pName));

        form.getChildren().addAll(fieldGroup("Select Your Assigned Complaint", complaintBox),
            fieldGroup("Work Done Notes", noteArea),
            new HBox(12, doneBtn, progressBtn), resultLbl);
        section.getChildren().add(form);
        return section;
    }

    private void updateComplaintStatus(ComboBox<String> cBox, String newStatus, TextArea noteArea, Label resultLbl, String pName) {
        if (cBox.getValue() == null) { showAlert("Please select a complaint."); return; }
        String cId = cBox.getValue().split(" — ")[0].trim();
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE complaints SET status=? WHERE complaint_id=?")) {
            ps.setString(1, newStatus); ps.setString(2, cId); ps.executeUpdate();
            // Add a comment
            String note = noteArea.getText().trim();
            if (!note.isEmpty()) {
                try (PreparedStatement ps2 = conn.prepareStatement(
                     "INSERT INTO comments (complaint_id, author_name, comment) VALUES (?,?,?)")) {
                    ps2.setString(1, cId); ps2.setString(2, pName + " (Partner)"); ps2.setString(3, note);
                    ps2.executeUpdate();
                }
            }
            resultLbl.setText("✅  " + cId + " marked as " + newStatus);
            noteArea.clear();
        } catch (SQLException ex) { showAlert("Error: " + ex.getMessage()); }
    }

    private VBox buildHistorySection() {
        VBox section = new VBox(16);
        section.getChildren().add(sectionTitle("Completed Work History"));
        User user = SessionManager.getInstance().getCurrentUser();
        String pName = user != null ? user.getName() : null;
        if (pName == null) return section;

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT * FROM complaints WHERE assigned_worker=? AND status='Completed' ORDER BY created_at DESC")) {
            ps.setString(1, pName);
            ResultSet rs = ps.executeQuery();
            boolean any = false;
            while (rs.next()) {
                HBox row = new HBox(12); row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-background-color:#0a1f0a;-fx-background-radius:10;-fx-padding:12;" +
                    "-fx-border-color:#2e7d32;-fx-border-width:0 0 0 3;");
                VBox info = new VBox(3,
                    lbl("✅  " + rs.getString("complaint_id") + "  —  " + rs.getString("title"), 13, "white", true),
                    lbl("📍 " + rs.getString("location") + "  |  🏷 " + rs.getString("complaint_type") +
                        "  |  📅 " + (rs.getString("created_at") != null ? rs.getString("created_at").substring(0, 10) : ""), 11, "#78909c"));
                HBox.setHgrow(info, Priority.ALWAYS);
                row.getChildren().addAll(info, statusBadge("Completed"));
                section.getChildren().add(row); any = true;
            }
            if (!any) section.getChildren().add(lbl("No completed work yet.", 13, "#78909c"));
        } catch (SQLException e) { e.printStackTrace(); }
        return section;
    }

    private VBox buildProfileSection() {
        VBox section = new VBox(20);
        section.getChildren().add(sectionTitle("My Profile"));
        User user = SessionManager.getInstance().getCurrentUser();

        VBox profileCard = formCard("Partner Information");
        if (user != null) {
            profileCard.getChildren().addAll(
                tableRow("Name",     user.getName()),
                tableRow("Email",    user.getEmail()),
                tableRow("Role",     user.getRole()),
                tableRow("Society",  user.getSociety() != null ? user.getSociety() : "—"),
                tableRow("Phone",    user.getPhone() != null ? user.getPhone() : "—"));
        }

        // Rating summary
        VBox ratingCard = formCard("My Ratings Summary");
        if (user != null) {
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                     "SELECT AVG(r.stars) as avg, COUNT(*) as cnt FROM ratings r " +
                     "JOIN complaints c ON r.complaint_id=c.complaint_id WHERE c.assigned_worker=?")) {
                ps.setString(1, user.getName());
                ResultSet rs = ps.executeQuery();
                double avg = rs.getDouble("avg");
                int cnt = rs.getInt("cnt");
                ratingCard.getChildren().addAll(
                    lbl(avg > 0 ? String.format("Average Rating: %.1f ⭐", avg) : "No ratings yet", 16, "#ffb300", true),
                    lbl("Based on " + cnt + " completed complaint(s)", 12, "#78909c"));
            } catch (SQLException e) { e.printStackTrace(); }
        }

        section.getChildren().addAll(profileCard, ratingCard);
        return section;
    }

    private VBox buildPartnerComplaintCard(ResultSet rs, String pName) throws SQLException {
        VBox card = new VBox(10);
        String stat = rs.getString("status");
        String borderColor = "Completed".equals(stat) ? "#2e7d32" : "#1565c0";
        card.setStyle("-fx-background-color:#12294a;-fx-background-radius:12;-fx-padding:16;" +
            "-fx-border-color:" + borderColor + ";-fx-border-width:0 0 0 4;-fx-border-radius:0 12 12 0;");

        String cId = rs.getString("complaint_id");
        HBox header = new HBox(10); header.setAlignment(Pos.CENTER_LEFT);
        Label titleLbl = lbl(cId + "  —  " + rs.getString("title"), 13, "white", true);
        HBox.setHgrow(titleLbl, Priority.ALWAYS);
        Region hSp = new Region(); HBox.setHgrow(hSp, Priority.ALWAYS);
        header.getChildren().addAll(titleLbl, hSp, statusBadge(stat), priorityBadge(rs.getString("priority")));

        HBox meta = new HBox(16,
            lbl("📍  " + rs.getString("location"), 11, "#78909c"),
            lbl("🏷  " + rs.getString("complaint_type"), 11, "#78909c"),
            lbl("👤  Filed by: " + (rs.getString("assigned_head") != null ? rs.getString("assigned_head") : "—"), 11, "#78909c"),
            lbl("📅  " + (rs.getString("created_at") != null ? rs.getString("created_at").substring(0, 10) : ""), 11, "#78909c"));

        card.getChildren().addAll(header, meta);
        if (rs.getString("description") != null) {
            Label desc = lbl(rs.getString("description"), 12, "#90a4ae"); desc.setWrapText(true);
            card.getChildren().add(desc);
        }
        return card;
    }
}
