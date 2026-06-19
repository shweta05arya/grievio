package com.grievio.ui.sector;

import com.grievio.MainApp;
import com.grievio.database.DatabaseHelper;
import com.grievio.model.User;
import com.grievio.session.SessionManager;
import com.grievio.ui.components.AnalyticsChart;
import com.grievio.util.PdfExporter;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.io.File;
import java.sql.*;
import java.util.*;
import static com.grievio.ui.components.UIHelper.*;

public class SectorDashboard {

    private final HBox root;
    private final StackPane contentArea;
    private final Label headerTitleLbl;
    private Button currentNavBtn;

    public SectorDashboard() {
        root = new HBox();
        root.getStyleClass().add("root-bg");
        headerTitleLbl = lbl("Sector Dashboard", 20, "white", true);
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
        VBox brandText = new VBox(2, lbl("Grievio", 20, "white", true), lbl("Sector Admin", 9, "#ffd54f"));
        brand.getChildren().addAll(lbl("🏢", 22, "white"), brandText);

        User user = SessionManager.getInstance().getCurrentUser();
        VBox userCard = new VBox(3);
        userCard.setStyle("-fx-background-color:rgba(255,213,79,0.1);-fx-background-radius:10;-fx-padding:10 12;" +
            "-fx-border-color:#ffd54f;-fx-border-radius:10;-fx-border-width:1;");
        userCard.getChildren().addAll(
            lbl("🏢  " + (user != null ? user.getName() : "Sector Head"), 12, "white", true),
            lbl("Sector Administrator", 10, "#ffd54f"));

        Button bDash      = navBtn("🏠", "Dashboard");
        Button bLocal     = navBtn("📋", "Society Complaints");
        Button bPending   = navBtn("🔴", "Pending Assignments");
        Button bAssign    = navBtn("🔧", "Assign Workers");
        Button bPetitions = navBtn("🗣", "Petitions (Received)");
        Button bForward   = navBtn("📤", "Forward to Gov");
        Button bHistory   = navBtn("📜", "Complaint History");
        Button bAnalytics = navBtn("📊", "Analytics");

        bDash.setOnAction(e      -> show(buildDashSection(),       bDash,      "Sector Dashboard"));
        bLocal.setOnAction(e     -> show(buildLocalSection(),      bLocal,     "Society Complaints"));
        bPending.setOnAction(e   -> show(buildPendingSection(),    bPending,   "Pending Assignments"));
        bAssign.setOnAction(e    -> show(buildAssignSection(),     bAssign,    "Assign Workers"));
        bPetitions.setOnAction(e -> show(buildPetitionsSection(),  bPetitions, "Petitions Received"));
        bForward.setOnAction(e   -> show(buildForwardSection(),    bForward,   "Forward Petition to Government"));
        bHistory.setOnAction(e   -> show(buildHistorySection(),    bHistory,   "Complaint History"));
        bAnalytics.setOnAction(e -> show(buildAnalyticsSection(),  bAnalytics, "Analytics & Insights"));

        Button logoutBtn = new Button("🚪   Logout");
        logoutBtn.getStyleClass().add("nav-btn-logout");
        logoutBtn.setPrefWidth(220);
        logoutBtn.setOnAction(e -> { SessionManager.getInstance().logout(); MainApp.showLogin(); });

        Region spacer = new Region(); VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().addAll(brand, mkSep(), mkRegion(4), userCard, mkRegion(6),
            bDash, bLocal, bPending, bAssign, bPetitions, bForward, bHistory, bAnalytics,
            spacer, mkSep(), logoutBtn);
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
        Label roleLbl = lbl("🏢  Sector Administrator", 13, "#ffd54f");
        roleLbl.setStyle(roleLbl.getStyle() + "-fx-background-color:rgba(255,213,79,0.1);-fx-background-radius:20;-fx-padding:5 14;");
        Region hSp = new Region(); HBox.setHgrow(hSp, Priority.ALWAYS);
        header.getChildren().addAll(headerTitleLbl, hSp, roleLbl);
        ScrollPane scroll = wrapScroll(contentArea);
        main.getChildren().addAll(header, scroll);
        contentArea.getChildren().setAll(buildDashSection());
        return main;
    }

    private void show(Node section, Button btn, String title) {
        headerTitleLbl.setText(title);
        setActive(btn);
        contentArea.getChildren().setAll(section);
        fadeIn(section);
    }

    // ── Dashboard ──────────────────────────────────────────────────────────────

    private VBox buildDashSection() {
        VBox s = new VBox(20);
        User user = SessionManager.getInstance().getCurrentUser();
        String society = user != null ? user.getSociety() : null;
        int[] stats = fetchStats(society);

        HBox cards = new HBox(15,
            statCard("Total",      stats[0], "#1565c0", "#0d47a1"),
            statCard("Pending",    stats[1], "#c62828", "#b71c1c"),
            statCard("In Progress",stats[2], "#e65100", "#bf360c"),
            statCard("Completed",  stats[3], "#2e7d32", "#1b5e20"));
        for (Node c : cards.getChildren()) HBox.setHgrow(c, Priority.ALWAYS);

        VBox infoCard = new VBox(10);
        infoCard.setStyle("-fx-background-color:linear-gradient(to right,#1a2a0a,#2e7d32);" +
            "-fx-background-radius:14;-fx-padding:20;");
        infoCard.getChildren().addAll(
            lbl("🏢  Sector Admin Panel", 18, "white", true),
            lbl("Society: " + (society != null ? society : "All Societies"), 13, "#a5d6a7"),
            lbl("Manage complaints, receive petitions, forward to government, assign workers.", 12, "#c8e6c9"));

        s.getChildren().addAll(sectionTitle("Sector Overview"), cards, infoCard, buildLocalSection());
        return s;
    }

    private VBox buildLocalSection() {
        VBox section = new VBox(16);
        section.getChildren().add(sectionTitle("Society Complaints"));
        User user = SessionManager.getInstance().getCurrentUser();
        String society = user != null ? user.getSociety() : null;

        Button exportBtn = new Button("📄  Export PDF");
        exportBtn.setStyle("-fx-background-color:#1565c0;-fx-text-fill:white;-fx-font-weight:bold;" +
            "-fx-background-radius:8;-fx-padding:8 18;-fx-cursor:hand;");
        exportBtn.setOnAction(e -> exportComplaintsPdf(society));
        HBox topBar = new HBox(12, exportBtn);
        topBar.setAlignment(Pos.CENTER_RIGHT);
        section.getChildren().add(topBar);

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT * FROM complaints WHERE category='Society-Level'" +
                 (society != null ? " AND society_name=?" : "") + " ORDER BY created_at DESC")) {
            if (society != null) ps.setString(1, society);
            ResultSet rs = ps.executeQuery();
            boolean any = false;
            while (rs.next()) { section.getChildren().add(buildAssignableRow(rs)); any = true; }
            if (!any) section.getChildren().add(lbl("No society complaints yet.", 13, "#78909c"));
        } catch (SQLException e) { e.printStackTrace(); }
        return section;
    }

    private VBox buildPendingSection() {
        VBox section = new VBox(16);
        section.getChildren().add(sectionTitle("Pending Assignments"));
        try (Connection conn = DatabaseHelper.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(
                 "SELECT * FROM complaints WHERE status='Pending' AND category='Society-Level' ORDER BY created_at DESC")) {
            boolean any = false;
            while (rs.next()) { section.getChildren().add(buildAssignableRow(rs)); any = true; }
            if (!any) section.getChildren().add(lbl("No pending complaints.", 13, "#78909c"));
        } catch (SQLException e) { e.printStackTrace(); }
        return section;
    }

    private VBox buildAssignSection() {
        VBox section = new VBox(20);
        section.getChildren().add(sectionTitle("Assign Workers"));
        VBox form = formCard("Assign a Worker to Complaint");
        ComboBox<String> complaintBox = comboField();
        ComboBox<String> workerBox = comboField();
        workerBox.getItems().addAll("Plumber Ravi", "Electrician Suresh", "Maintenance Amit",
            "Security Staff Raj", "Carpenter Vinod", "Lift Technician Kumar");

        try (Connection conn = DatabaseHelper.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(
                 "SELECT complaint_id, title FROM complaints WHERE category='Society-Level' ORDER BY created_at DESC")) {
            while (rs.next()) complaintBox.getItems().add(rs.getString("complaint_id") + " — " + rs.getString("title"));
        } catch (SQLException ignored) {}

        ComboBox<String> statusBox = comboField();
        statusBox.getItems().addAll("Pending", "In Progress", "Completed");
        Label resultLbl = lbl("", 12, "#00c853");
        Button assignBtn = primaryBtn("🔧  Assign & Update");
        assignBtn.setOnAction(e -> {
            String cVal = complaintBox.getValue(), worker = workerBox.getValue(), status = statusBox.getValue();
            if (cVal == null || worker == null) { showAlert("Select a complaint and worker."); return; }
            String cId = cVal.split(" — ")[0].trim();
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                     "UPDATE complaints SET assigned_worker=?, status=? WHERE complaint_id=?")) {
                ps.setString(1, worker); ps.setString(2, status != null ? status : "In Progress");
                ps.setString(3, cId); ps.executeUpdate();
                resultLbl.setText("✅  Assigned " + worker + " to " + cId);
            } catch (SQLException ex) { showAlert("Error: " + ex.getMessage()); }
        });

        // Delete complaint option
        VBox deleteCard = formCard("Delete Complaint");
        ComboBox<String> deleteBox = comboField();
        Label delResult = lbl("", 12, "#ef5350");
        try (Connection conn = DatabaseHelper.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(
                 "SELECT complaint_id, title FROM complaints WHERE category='Society-Level' ORDER BY created_at DESC")) {
            while (rs.next()) deleteBox.getItems().add(rs.getString("complaint_id") + " — " + rs.getString("title"));
        } catch (SQLException ignored) {}
        Button deleteBtn = dangerBtn("🗑  Delete Selected");
        deleteBtn.setOnAction(e -> {
            if (deleteBox.getValue() == null) { showAlert("Select a complaint to delete."); return; }
            String cId = deleteBox.getValue().split(" — ")[0].trim();
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete complaint " + cId + "? This cannot be undone.", ButtonType.YES, ButtonType.NO);
            confirm.setTitle("Confirm Delete");
            confirm.showAndWait().ifPresent(resp -> {
                if (resp == ButtonType.YES) {
                    try (Connection conn = DatabaseHelper.getConnection();
                         PreparedStatement ps = conn.prepareStatement("DELETE FROM complaints WHERE complaint_id=?")) {
                        ps.setString(1, cId); ps.executeUpdate();
                        deleteBox.getItems().remove(deleteBox.getValue());
                        complaintBox.getItems().removeIf(s -> s.startsWith(cId));
                        delResult.setText("🗑  Deleted: " + cId);
                    } catch (SQLException ex) { showAlert("Error: " + ex.getMessage()); }
                }
            });
        });
        deleteCard.getChildren().addAll(fieldGroup("Select Complaint to Delete", deleteBox), deleteBtn, delResult);

        HBox r = new HBox(12, fieldGroup("Complaint", complaintBox), fieldGroup("Worker", workerBox), fieldGroup("Status", statusBox));
        for (Node n : r.getChildren()) HBox.setHgrow(n, Priority.ALWAYS);
        form.getChildren().addAll(r, resultLbl, assignBtn);
        section.getChildren().addAll(form, deleteCard);
        return section;
    }

    // ── Petitions (from User Side) ─────────────────────────────────────────────

    private VBox buildPetitionsSection() {
        VBox section = new VBox(16);
        section.getChildren().add(sectionTitle("Petitions Received (from Residents)"));

        Button exportBtn = new Button("📄  Export Petitions PDF");
        exportBtn.setStyle("-fx-background-color:#1565c0;-fx-text-fill:white;-fx-font-weight:bold;" +
            "-fx-background-radius:8;-fx-padding:8 18;-fx-cursor:hand;");
        exportBtn.setOnAction(e -> exportPetitionsPdf());
        HBox topBar = new HBox(12, exportBtn);
        topBar.setAlignment(Pos.CENTER_RIGHT);
        section.getChildren().add(topBar);

        try (Connection conn = DatabaseHelper.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(
                 "SELECT c.*, (SELECT COUNT(*) FROM votes WHERE complaint_id=c.complaint_id) as vcount " +
                 "FROM complaints c WHERE c.is_public=1 ORDER BY vcount DESC, c.created_at DESC")) {
            boolean any = false;
            while (rs.next()) {
                HBox row = new HBox(12); row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-background-color:#12294a;-fx-background-radius:10;-fx-padding:12;");
                int votes = rs.getInt("vcount");
                VBox info = new VBox(3,
                    lbl(rs.getString("complaint_id") + "  —  " + rs.getString("title"), 13, "white", true),
                    lbl("📍 " + rs.getString("location") + "  |  🏷 " + rs.getString("complaint_type") +
                        "  |  📅 " + (rs.getString("created_at") != null ? rs.getString("created_at").substring(0, 10) : ""), 11, "#78909c"));
                HBox.setHgrow(info, Priority.ALWAYS);
                Label voteLbl = lbl("👍  " + votes + " votes", 13, votes >= 10 ? "#ef5350" : "#ffb300", true);
                row.getChildren().addAll(info, voteLbl, statusBadge(rs.getString("status")));
                section.getChildren().add(row); any = true;
            }
            if (!any) section.getChildren().add(lbl("No petitions received yet. Residents can submit public petitions.", 13, "#78909c"));
        } catch (SQLException e) { e.printStackTrace(); }
        return section;
    }

    // ── Forward Petition to Government ─────────────────────────────────────────

    private VBox buildForwardSection() {
        VBox section = new VBox(20);
        section.getChildren().add(sectionTitle("Forward Petition to Government"));

        VBox form = formCard("Select Petition & Forward");
        ComboBox<String> petitionBox = comboField();
        TextArea noteArea = textArea("Add forwarding note for the government...");
        Label resultLbl = lbl("", 12, "#00c853");
        Button forwardBtn = primaryBtn("📤  Forward to Government");

        try (Connection conn = DatabaseHelper.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(
                 "SELECT complaint_id, title, vote_count FROM complaints WHERE is_public=1 ORDER BY vote_count DESC")) {
            while (rs.next())
                petitionBox.getItems().add(rs.getString("complaint_id") + " — " + rs.getString("title") +
                    " (👍 " + rs.getInt("vote_count") + " votes)");
        } catch (SQLException ignored) {}

        forwardBtn.setOnAction(e -> {
            if (petitionBox.getValue() == null) { showAlert("Select a petition to forward."); return; }
            String cId = petitionBox.getValue().split(" — ")[0].trim();
            String note = noteArea.getText().trim();
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                     "UPDATE complaints SET assigned_head=?, status='In Progress', category='Public-Area' WHERE complaint_id=?")) {
                ps.setString(1, "Government — Forwarded by Sector Admin" + (note.isEmpty() ? "" : ": " + note.substring(0, Math.min(60, note.length()))));
                ps.setString(2, cId); ps.executeUpdate();
                // Log as comment
                try (PreparedStatement ps2 = conn.prepareStatement(
                     "INSERT INTO comments (complaint_id, author_name, comment) VALUES (?,?,?)")) {
                    ps2.setString(1, cId); ps2.setString(2, "Sector Admin");
                    ps2.setString(3, "Forwarded to Government. Note: " + (note.isEmpty() ? "—" : note));
                    ps2.executeUpdate();
                }
                resultLbl.setText("✅  Petition " + cId + " forwarded to Government.");
                noteArea.clear();
            } catch (SQLException ex) { showAlert("Error: " + ex.getMessage()); }
        });

        form.getChildren().addAll(fieldGroup("Select Petition", petitionBox),
            fieldGroup("Forwarding Note", noteArea), forwardBtn, resultLbl);
        section.getChildren().add(form);
        return section;
    }

    // ── History ───────────────────────────────────────────────────────────────

    private VBox buildHistorySection() {
        VBox section = new VBox(16);
        section.getChildren().add(sectionTitle("Complaint History (Completed)"));
        User user = SessionManager.getInstance().getCurrentUser();
        String society = user != null ? user.getSociety() : null;

        Button exportBtn = new Button("📄  Export PDF");
        exportBtn.setStyle("-fx-background-color:#1565c0;-fx-text-fill:white;-fx-font-weight:bold;" +
            "-fx-background-radius:8;-fx-padding:8 18;-fx-cursor:hand;");
        exportBtn.setOnAction(e -> exportHistoryPdf(society));
        HBox topBar = new HBox(12, exportBtn); topBar.setAlignment(Pos.CENTER_RIGHT);
        section.getChildren().add(topBar);

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT * FROM complaints WHERE status='Completed' AND category='Society-Level'" +
                 (society != null ? " AND society_name=?" : "") + " ORDER BY updated_at DESC")) {
            if (society != null) ps.setString(1, society);
            ResultSet rs = ps.executeQuery();
            boolean any = false;
            while (rs.next()) {
                HBox row = new HBox(12); row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-background-color:#0a1f0a;-fx-background-radius:10;-fx-padding:12;" +
                    "-fx-border-color:#2e7d32;-fx-border-width:0 0 0 3;");
                VBox info = new VBox(3,
                    lbl("✅  " + rs.getString("complaint_id") + "  —  " + rs.getString("title"), 13, "white", true),
                    lbl("Worker: " + (rs.getString("assigned_worker") != null ? rs.getString("assigned_worker") : "—") +
                        "  |  " + rs.getString("complaint_type") + "  |  " + rs.getString("location"), 11, "#78909c"));
                HBox.setHgrow(info, Priority.ALWAYS);
                row.getChildren().addAll(info, statusBadge("Completed"));
                section.getChildren().add(row); any = true;
            }
            if (!any) section.getChildren().add(lbl("No completed complaints yet.", 13, "#78909c"));
        } catch (SQLException e) { e.printStackTrace(); }
        return section;
    }

    // ── Analytics ─────────────────────────────────────────────────────────────

    private VBox buildAnalyticsSection() {
        VBox section = new VBox(20);
        section.getChildren().add(sectionTitle("Analytics & Insights"));
        User user = SessionManager.getInstance().getCurrentUser();
        String society = user != null ? user.getSociety() : null;

        int[] stats = fetchStats(society);
        int total = stats[0], pending = stats[1], inProg = stats[2], done = stats[3];
        double resRate = total > 0 ? (done * 100.0 / total) : 0;

        HBox statCards = new HBox(15,
            AnalyticsChart.buildStatCard("Total Complaints", String.valueOf(total), "#1565c0", "#1565c0"),
            AnalyticsChart.buildStatCard("Resolution Rate", String.format("%.0f%%", resRate), "#4caf50", "#4caf50"),
            AnalyticsChart.buildStatCard("Pending", String.valueOf(pending), "#f44336", "#f44336"),
            AnalyticsChart.buildStatCard("Completed", String.valueOf(done), "#9c27b0", "#9c27b0"));
        for (Node c : statCards.getChildren()) HBox.setHgrow(c, Priority.ALWAYS);

        // Monthly chart
        List<String> months = Arrays.asList("Jun", "Jul", "Aug", "Sep", "Oct", "Nov");
        Map<String, double[]> series = new LinkedHashMap<>();
        Map<String, String> colors = new LinkedHashMap<>();
        double t = total, d = done, p = pending;
        series.put("Total",    new double[]{t*0.4, t*0.5, t*0.65, t*0.72, t*0.85, t});
        series.put("Resolved", new double[]{d*0.3, d*0.45, d*0.55, d*0.65, d*0.8, d});
        series.put("Pending",  new double[]{p*0.2, p*0.35, p*0.4, p*0.45, p*0.6, p});
        colors.put("Total", "#1565c0"); colors.put("Resolved", "#4caf50"); colors.put("Pending", "#ff9800");
        VBox lineChart = AnalyticsChart.buildLineChart("Monthly Trend — Total vs Resolved vs Pending", months, series, colors);

        // Priority donut
        Map<String, Double> prioSlices = fetchPriorityBreakdown(society);
        Map<String, String> prioColors = new LinkedHashMap<>();
        prioColors.put("Urgent","#f44336"); prioColors.put("High","#ff9800");
        prioColors.put("Medium","#ffc107"); prioColors.put("Low","#4caf50");
        VBox prioDonut = AnalyticsChart.buildDonutChart("Priority Distribution", prioSlices, prioColors, String.valueOf(total), "Total");

        // Status donut
        Map<String, Double> statSlices = new LinkedHashMap<>();
        statSlices.put("Pending", (double)pending); statSlices.put("In Progress", (double)inProg); statSlices.put("Resolved", (double)done);
        Map<String, String> statColors = new LinkedHashMap<>();
        statColors.put("Pending","#1565c0"); statColors.put("In Progress","#ff9800"); statColors.put("Resolved","#4caf50");
        VBox statDonut = AnalyticsChart.buildDonutChart("Status Breakdown", statSlices, statColors, String.valueOf(total), "Total");

        HBox donutRow = new HBox(15, prioDonut, statDonut);
        for (Node n : donutRow.getChildren()) HBox.setHgrow(n, Priority.ALWAYS);

        section.getChildren().addAll(statCards, lineChart, donutRow);
        return section;
    }

    // ── PDF Exports ────────────────────────────────────────────────────────────

    private void exportComplaintsPdf(String society) {
        try {
            File outFile = PdfExporter.chooseSaveFile("sector_complaints.pdf");
            if (outFile == null) return;
            String[] headers = {"ID", "Title", "Type", "Location", "Priority", "Status", "Worker", "Filed"};
            List<String[]> rows = new ArrayList<>();
            String sql = "SELECT * FROM complaints WHERE category='Society-Level'" +
                (society != null ? " AND society_name=?" : "") + " ORDER BY created_at DESC";
            try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                if (society != null) ps.setString(1, society);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) rows.add(new String[]{
                    rs.getString("complaint_id"), rs.getString("title"), rs.getString("complaint_type"),
                    rs.getString("location"), rs.getString("priority"), rs.getString("status"),
                    rs.getString("assigned_worker") != null ? rs.getString("assigned_worker") : "—",
                    rs.getString("created_at") != null ? rs.getString("created_at").substring(0,10) : ""});
            }
            PdfExporter.exportComplaintList("Sector Complaints Report", headers, rows, outFile);
            showAlert("✅  PDF exported: " + outFile.getName());
        } catch (Exception ex) { showAlert("Export failed: " + ex.getMessage()); ex.printStackTrace(); }
    }

    private void exportPetitionsPdf() {
        try {
            File outFile = PdfExporter.chooseSaveFile("sector_petitions.pdf");
            if (outFile == null) return;
            String[] headers = {"ID", "Title", "Type", "Location", "Votes", "Status", "Filed"};
            List<String[]> rows = new ArrayList<>();
            try (Connection conn = DatabaseHelper.getConnection();
                 ResultSet rs = conn.createStatement().executeQuery(
                     "SELECT c.*, (SELECT COUNT(*) FROM votes WHERE complaint_id=c.complaint_id) as vcount " +
                     "FROM complaints c WHERE c.is_public=1 ORDER BY vcount DESC")) {
                while (rs.next()) rows.add(new String[]{
                    rs.getString("complaint_id"), rs.getString("title"), rs.getString("complaint_type"),
                    rs.getString("location"), String.valueOf(rs.getInt("vcount")), rs.getString("status"),
                    rs.getString("created_at") != null ? rs.getString("created_at").substring(0,10) : ""});
            }
            PdfExporter.exportComplaintList("Sector Petitions Report", headers, rows, outFile);
            showAlert("✅  Petitions PDF exported: " + outFile.getName());
        } catch (Exception ex) { showAlert("Export failed: " + ex.getMessage()); ex.printStackTrace(); }
    }

    private void exportHistoryPdf(String society) {
        try {
            File outFile = PdfExporter.chooseSaveFile("sector_history.pdf");
            if (outFile == null) return;
            String[] headers = {"ID", "Title", "Type", "Status", "Worker", "Priority", "Filed"};
            List<String[]> rows = new ArrayList<>();
            String sql = "SELECT * FROM complaints WHERE status='Completed' AND category='Society-Level'" +
                (society != null ? " AND society_name=?" : "") + " ORDER BY updated_at DESC";
            try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                if (society != null) ps.setString(1, society);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) rows.add(new String[]{
                    rs.getString("complaint_id"), rs.getString("title"), rs.getString("complaint_type"),
                    rs.getString("status"), rs.getString("assigned_worker") != null ? rs.getString("assigned_worker") : "—",
                    rs.getString("priority"), rs.getString("created_at") != null ? rs.getString("created_at").substring(0,10) : ""});
            }
            PdfExporter.exportComplaintList("Sector History Report", headers, rows, outFile);
            showAlert("✅  History PDF exported: " + outFile.getName());
        } catch (Exception ex) { showAlert("Export failed: " + ex.getMessage()); ex.printStackTrace(); }
    }

    // ── Data Helpers ──────────────────────────────────────────────────────────

    private int[] fetchStats(String society) {
        int[] r = {0, 0, 0, 0};
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT status, COUNT(*) as c FROM complaints WHERE category='Society-Level'" +
                 (society != null ? " AND society_name=?" : "") + " GROUP BY status")) {
            if (society != null) ps.setString(1, society);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int c = rs.getInt("c"); r[0] += c;
                switch (rs.getString("status")) {
                    case "Pending" -> r[1] += c;
                    case "In Progress" -> r[2] += c;
                    case "Completed" -> r[3] += c;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return r;
    }

    private Map<String, Double> fetchPriorityBreakdown(String society) {
        Map<String, Double> map = new LinkedHashMap<>();
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT priority, COUNT(*) as cnt FROM complaints WHERE category='Society-Level'" +
                 (society != null ? " AND society_name=?" : "") + " GROUP BY priority")) {
            if (society != null) ps.setString(1, society);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) map.put(rs.getString("priority"), (double)rs.getInt("cnt"));
        } catch (SQLException e) { e.printStackTrace(); }
        if (map.isEmpty()) { map.put("Urgent", 1.0); map.put("High", 1.0); }
        return map;
    }

    private HBox buildAssignableRow(ResultSet rs) throws SQLException {
        HBox row = new HBox(12); row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color:#12294a;-fx-background-radius:10;-fx-padding:12;");
        VBox info = new VBox(3,
            lbl(rs.getString("complaint_id") + "  —  " + rs.getString("title"), 13, "white", true),
            lbl("📍 " + rs.getString("location") + "  |  🏷 " + rs.getString("complaint_type") +
                "  |  👤 Assigned: " + (rs.getString("assigned_worker") != null ? rs.getString("assigned_worker") : "Not yet"), 11, "#78909c"));
        HBox.setHgrow(info, Priority.ALWAYS);
        row.getChildren().addAll(info, statusBadge(rs.getString("status")), priorityBadge(rs.getString("priority")));
        return row;
    }
}
