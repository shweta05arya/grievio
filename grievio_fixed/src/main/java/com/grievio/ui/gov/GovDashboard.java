package com.grievio.ui.gov;

import com.grievio.MainApp;
import com.grievio.database.DatabaseHelper;
import com.grievio.session.SessionManager;
import com.grievio.model.User;
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

public class GovDashboard {

    private final HBox root;
    private final StackPane contentArea;
    private final Label headerTitleLbl;
    private Button currentNavBtn;

    public GovDashboard() {
        root = new HBox();
        root.getStyleClass().add("root-bg");
        headerTitleLbl = lbl("Government Dashboard", 20, "white", true);
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
        VBox brandText = new VBox(2, lbl("Grievio", 20, "white", true), lbl("Government Portal", 9, "#80cbc4"));
        brand.getChildren().addAll(lbl("🏛", 22, "white"), brandText);

        User user = SessionManager.getInstance().getCurrentUser();
        VBox userCard = new VBox(3);
        userCard.setStyle("-fx-background-color:rgba(0,150,136,0.15);-fx-background-radius:10;-fx-padding:10 12;" +
            "-fx-border-color:#4db6ac;-fx-border-radius:10;-fx-border-width:1;");
        userCard.getChildren().addAll(
            lbl("🏛  " + (user != null ? user.getName() : "Gov Admin"), 12, "white", true),
            lbl("Government Officer", 10, "#80cbc4"));

        Button bDash     = navBtn("🏠", "Dashboard");
        Button bPublic   = navBtn("📋", "Public Complaints");
        Button bEscalate = navBtn("🔺", "Escalated Issues");
        Button bPetition = navBtn("🗣", "Public Petitions");
        Button bForward  = navBtn("📤", "Forward to Dept");
        Button bAnalytics= navBtn("📊", "Analytics");

        bDash.setOnAction(e      -> show(buildGovDashSection(),    bDash,      "Government Dashboard"));
        bPublic.setOnAction(e    -> show(buildPublicSection(),     bPublic,    "Public Complaints"));
        bEscalate.setOnAction(e  -> show(buildEscalatedSection(),  bEscalate,  "Escalated Issues"));
        bPetition.setOnAction(e  -> show(buildPetitionSection(),   bPetition,  "Public Petitions"));
        bForward.setOnAction(e   -> show(buildForwardSection(),    bForward,   "Forward to Department"));
        bAnalytics.setOnAction(e -> show(buildAnalyticsSection(),  bAnalytics, "Analytics & Insights"));

        Button logoutBtn = new Button("🚪   Logout");
        logoutBtn.getStyleClass().add("nav-btn-logout");
        logoutBtn.setPrefWidth(220);
        logoutBtn.setOnAction(e -> { SessionManager.getInstance().logout(); MainApp.showLogin(); });

        Region spacer = new Region(); VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().addAll(brand, mkSep(), mkRegion(4), userCard, mkRegion(6),
            bDash, bPublic, bEscalate, bPetition, bForward, bAnalytics, spacer, mkSep(), logoutBtn);
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
        Label roleLbl = lbl("🏛  Government Officer", 13, "#80cbc4");
        roleLbl.setStyle(roleLbl.getStyle() + "-fx-background-color:rgba(0,150,136,0.2);-fx-background-radius:20;-fx-padding:5 14;");
        Region hSp = new Region(); HBox.setHgrow(hSp, Priority.ALWAYS);
        header.getChildren().addAll(headerTitleLbl, hSp, roleLbl);
        ScrollPane scroll = wrapScroll(contentArea);
        main.getChildren().addAll(header, scroll);
        contentArea.getChildren().setAll(buildGovDashSection());
        return main;
    }

    private void show(Node section, Button btn, String title) {
        headerTitleLbl.setText(title); setActive(btn);
        contentArea.getChildren().setAll(section); fadeIn(section);
    }

    private VBox buildGovDashSection() {
        VBox s = new VBox(20);
        int[] stats = fetchStats();
        HBox cards = new HBox(15,
            statCard("Public Issues", stats[0], "#00695c", "#004d40"),
            statCard("Pending",       stats[1], "#c62828", "#b71c1c"),
            statCard("In Progress",   stats[2], "#e65100", "#bf360c"),
            statCard("Resolved",      stats[3], "#2e7d32", "#1b5e20"));
        for (Node c : cards.getChildren()) HBox.setHgrow(c, Priority.ALWAYS);

        VBox infoCard = new VBox(10);
        infoCard.setStyle("-fx-background-color:linear-gradient(to right,#0a2a28,#00695c);" +
            "-fx-background-radius:14;-fx-padding:20;");
        infoCard.getChildren().addAll(
            lbl("🏛  Government / Municipal Portal", 18, "white", true),
            lbl("Manage all public-area complaints routed by AI: roads, electricity, sanitation, drainage, water supply.", 13, "#a7f3d0"),
            lbl("Petitions with high votes are automatically escalated for urgent resolution.", 12, "#80cbc4"));

        s.getChildren().addAll(sectionTitle("Government Overview"), cards, infoCard, buildPublicSection());
        return s;
    }

    private VBox buildPublicSection() {
        VBox section = new VBox(16);
        section.getChildren().add(lbl("Public-Area Complaints", 16, "white", true));

        Button exportBtn = new Button("📄  Export PDF");
        exportBtn.setStyle("-fx-background-color:#00695c;-fx-text-fill:white;-fx-font-weight:bold;" +
            "-fx-background-radius:8;-fx-padding:8 18;-fx-cursor:hand;");
        exportBtn.setOnAction(e -> exportComplaintsPdf(null));

        ComboBox<String> typeFilter = comboField(); typeFilter.setMaxWidth(200);
        typeFilter.getItems().addAll("All Types","Road","Electrical","Sanitation","Water Supply","Garbage","Other");
        typeFilter.setValue("All Types");

        VBox listBox = new VBox(8);
        Runnable refresh = () -> {
            listBox.getChildren().clear();
            String tf = typeFilter.getValue();
            String sql = "SELECT * FROM complaints WHERE category='Public-Area'" +
                (tf != null && !tf.equals("All Types") ? " AND complaint_type=?" : "") +
                " ORDER BY vote_count DESC, created_at DESC";
            try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                if (tf != null && !tf.equals("All Types")) ps.setString(1, tf);
                ResultSet rs = ps.executeQuery();
                boolean any = false;
                while (rs.next()) { listBox.getChildren().add(buildGovComplaintRow(rs)); any = true; }
                if (!any) listBox.getChildren().add(lbl("No public-area complaints found.", 13, "#78909c"));
            } catch (SQLException e) { e.printStackTrace(); }
        };
        typeFilter.setOnAction(e -> refresh.run());
        refresh.run();

        HBox filterRow = new HBox(12, lbl("Filter by Type:", 12, "#90caf9"), typeFilter, new Region(), exportBtn);
        HBox.setHgrow(filterRow.getChildren().get(2), Priority.ALWAYS);
        filterRow.setAlignment(Pos.CENTER_LEFT);
        section.getChildren().addAll(filterRow, listBox);
        return section;
    }

    private HBox buildGovComplaintRow(ResultSet rs) throws SQLException {
        HBox row = new HBox(12); row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color:#12294a;-fx-background-radius:10;-fx-padding:12;");
        String cId = rs.getString("complaint_id");
        VBox info = new VBox(3,
            lbl(cId + "  —  " + rs.getString("title"), 13, "white", true),
            lbl("📍 " + rs.getString("location") + "  |  🏷 " + rs.getString("complaint_type") +
                "  |  👍 " + rs.getInt("vote_count") + " votes", 11, "#78909c"),
            lbl("👤 Assigned: " + (rs.getString("assigned_head") != null ? rs.getString("assigned_head") : "Pending"), 11, "#64b5f6"));
        HBox.setHgrow(info, Priority.ALWAYS);

        Button pdfBtn = new Button("📄");
        pdfBtn.setStyle("-fx-background-color:#00695c;-fx-text-fill:white;-fx-background-radius:5;" +
            "-fx-padding:4 8;-fx-cursor:hand;-fx-font-size:10px;");
        pdfBtn.setOnAction(e -> exportSingleComplaintPdf(rs));

        ComboBox<String> statusBox = comboField(); statusBox.setMaxWidth(150);
        statusBox.getItems().addAll("Pending","In Progress","Completed");
        statusBox.setValue(rs.getString("status"));
        Button saveBtn = new Button("💾");
        saveBtn.setStyle("-fx-background-color:#1565c0;-fx-text-fill:white;-fx-background-radius:6;-fx-padding:6 10;-fx-cursor:hand;");
        saveBtn.setOnAction(e -> {
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement ps = conn.prepareStatement("UPDATE complaints SET status=? WHERE complaint_id=?")) {
                ps.setString(1, statusBox.getValue()); ps.setString(2, cId); ps.executeUpdate();
                showAlert("Status updated.");
            } catch (SQLException ex) { showAlert("Error: " + ex.getMessage()); }
        });
        HBox statusRow = new HBox(6, statusBox, saveBtn, pdfBtn); statusRow.setAlignment(Pos.CENTER_RIGHT);
        row.getChildren().addAll(info, statusBadge(rs.getString("status")), priorityBadge(rs.getString("priority")), statusRow);
        return row;
    }

    private VBox buildEscalatedSection() {
        VBox section = new VBox(16);
        section.getChildren().add(sectionTitle("Escalated Issues (Urgent / High Priority)"));
        Button exportBtn = new Button("📄  Export PDF");
        exportBtn.setStyle("-fx-background-color:#00695c;-fx-text-fill:white;-fx-font-weight:bold;" +
            "-fx-background-radius:8;-fx-padding:8 18;-fx-cursor:hand;");
        exportBtn.setOnAction(e -> exportComplaintsPdf("Urgent"));
        HBox topBar = new HBox(12, exportBtn); topBar.setAlignment(Pos.CENTER_RIGHT);
        section.getChildren().add(topBar);

        try (Connection conn = DatabaseHelper.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(
                 "SELECT * FROM complaints WHERE category='Public-Area' AND priority IN ('Urgent','High') ORDER BY vote_count DESC")) {
            boolean any = false;
            while (rs.next()) { section.getChildren().add(buildGovComplaintRow(rs)); any = true; }
            if (!any) section.getChildren().add(lbl("No escalated issues.", 13, "#78909c"));
        } catch (SQLException e) { e.printStackTrace(); }
        return section;
    }

    private VBox buildPetitionSection() {
        VBox section = new VBox(16);
        section.getChildren().add(sectionTitle("Public Petitions (by Vote Count)"));

        Button exportBtn = new Button("📄  Export Petitions PDF");
        exportBtn.setStyle("-fx-background-color:#00695c;-fx-text-fill:white;-fx-font-weight:bold;" +
            "-fx-background-radius:8;-fx-padding:8 18;-fx-cursor:hand;");
        exportBtn.setOnAction(e -> exportPetitionsPdf());
        HBox topBar = new HBox(12, exportBtn); topBar.setAlignment(Pos.CENTER_RIGHT);
        section.getChildren().add(topBar);

        try (Connection conn = DatabaseHelper.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(
                 "SELECT c.*, (SELECT COUNT(*) FROM votes WHERE complaint_id=c.complaint_id) as vcount " +
                 "FROM complaints c WHERE c.is_public=1 ORDER BY vcount DESC")) {
            boolean any = false;
            while (rs.next()) {
                HBox row = new HBox(12); row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-background-color:#12294a;-fx-background-radius:10;-fx-padding:12;");
                int votes = rs.getInt("vcount");
                VBox info = new VBox(3,
                    lbl(rs.getString("complaint_id") + "  —  " + rs.getString("title"), 13, "white", true),
                    lbl("📍 " + rs.getString("location") + "  |  🏷 " + rs.getString("complaint_type"), 11, "#78909c"));
                HBox.setHgrow(info, Priority.ALWAYS);
                Label voteLbl = lbl("👍  " + votes + " votes", 13, votes >= 10 ? "#ef5350" : "#ffb300", true);
                row.getChildren().addAll(info, voteLbl, statusBadge(rs.getString("status")));
                section.getChildren().add(row); any = true;
            }
            if (!any) section.getChildren().add(lbl("No petitions yet.", 13, "#78909c"));
        } catch (SQLException e) { e.printStackTrace(); }
        return section;
    }

    private VBox buildForwardSection() {
        VBox section = new VBox(20);
        section.getChildren().add(sectionTitle("Forward Complaint to Department"));
        VBox form = formCard("Forward a Complaint");
        ComboBox<String> complaintBox = comboField();
        ComboBox<String> deptBox = comboField();
        deptBox.getItems().addAll("PWD - Roads & Infrastructure","DISCOM - Electricity Board",
            "Jal Board - Water Department","Municipal Corp - Sanitation",
            "Traffic Department","Urban Development Authority");
        TextArea noteArea = textArea("Add forwarding note / instructions...");
        Label resultLbl = lbl("", 12, "#00c853");
        Button forwardBtn = primaryBtn("📤  Forward Complaint");

        try (Connection conn = DatabaseHelper.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(
                 "SELECT complaint_id, title FROM complaints WHERE category='Public-Area' ORDER BY created_at DESC")) {
            while (rs.next()) complaintBox.getItems().add(rs.getString("complaint_id") + " — " + rs.getString("title"));
        } catch (SQLException ignored) {}

        forwardBtn.setOnAction(e -> {
            if (complaintBox.getValue() == null || deptBox.getValue() == null) {
                showAlert("Select a complaint and department."); return;
            }
            String cId = complaintBox.getValue().split(" — ")[0].trim();
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                     "UPDATE complaints SET assigned_head=?, status='In Progress' WHERE complaint_id=?")) {
                ps.setString(1, deptBox.getValue()); ps.setString(2, cId); ps.executeUpdate();
                resultLbl.setText("✅  Forwarded " + cId + " to " + deptBox.getValue());
            } catch (SQLException ex) { showAlert("Error: " + ex.getMessage()); }
        });

        HBox r = new HBox(12, fieldGroup("Complaint", complaintBox), fieldGroup("Department", deptBox));
        for (Node n : r.getChildren()) HBox.setHgrow(n, Priority.ALWAYS);
        form.getChildren().addAll(r, fieldGroup("Forwarding Note", noteArea), forwardBtn, resultLbl);
        section.getChildren().add(form);
        return section;
    }

    private VBox buildAnalyticsSection() {
        VBox section = new VBox(20);
        section.getChildren().add(sectionTitle("Analytics & Insights"));

        int[] stats = fetchStats();
        int total = stats[0], pending = stats[1], inProg = stats[2], done = stats[3];
        double resRate = total > 0 ? (done * 100.0 / total) : 0;
        int urgent = fetchUrgentCount();

        HBox statCards = new HBox(15,
            AnalyticsChart.buildStatCard("Total Complaints", String.valueOf(total), "#1565c0", "#1565c0"),
            AnalyticsChart.buildStatCard("Resolution Rate", String.format("%.0f%%", resRate), "#4caf50", "#4caf50"),
            AnalyticsChart.buildStatCard("Avg Resolution", "5.2d", "#9c27b0", "#9c27b0"),
            AnalyticsChart.buildStatCard("Urgent + Escalated", String.valueOf(urgent), "#f44336", "#f44336"));
        for (Node c : statCards.getChildren()) HBox.setHgrow(c, Priority.ALWAYS);

        List<String> months = Arrays.asList("Jun", "Jul", "Aug", "Sep", "Oct", "Nov");
        Map<String, double[]> series = new LinkedHashMap<>();
        Map<String, String> colors = new LinkedHashMap<>();
        double t = total, d = done, p = pending;
        series.put("Total",    new double[]{t*0.4, t*0.5, t*0.65, t*0.72, t*0.85, t});
        series.put("Resolved", new double[]{d*0.3, d*0.45, d*0.55, d*0.65, d*0.8, d});
        series.put("Pending",  new double[]{p*0.2, p*0.35, p*0.4, p*0.45, p*0.6, p});
        colors.put("Total","#1565c0"); colors.put("Resolved","#4caf50"); colors.put("Pending","#ff9800");
        VBox lineChart = AnalyticsChart.buildLineChart("Monthly Trend — Total vs Resolved vs Pending", months, series, colors);

        Map<String, Double> typeSlices = fetchTypeBreakdown();
        Map<String, String> typeColors = new LinkedHashMap<>();
        typeColors.put("Road","#f44336"); typeColors.put("Electrical","#ff9800");
        typeColors.put("Sanitation","#4caf50"); typeColors.put("Water Supply","#2196f3");
        typeColors.put("Other","#9c27b0");
        VBox typeDonut = AnalyticsChart.buildDonutChart("Complaint Types", typeSlices, typeColors, String.valueOf(total), "Total");

        Map<String, Double> statSlices = new LinkedHashMap<>();
        statSlices.put("Pending",(double)pending); statSlices.put("In Progress",(double)inProg); statSlices.put("Resolved",(double)done);
        Map<String, String> statColors = new LinkedHashMap<>();
        statColors.put("Pending","#1565c0"); statColors.put("In Progress","#ff9800"); statColors.put("Resolved","#4caf50");
        VBox statDonut = AnalyticsChart.buildDonutChart("Status Breakdown", statSlices, statColors, String.valueOf(total), "Total");

        HBox donutRow = new HBox(15, typeDonut, statDonut);
        for (Node n : donutRow.getChildren()) HBox.setHgrow(n, Priority.ALWAYS);

        section.getChildren().addAll(statCards, lineChart, donutRow);
        return section;
    }

    // ── PDF Exports ────────────────────────────────────────────────────────────

    private void exportComplaintsPdf(String priorityFilter) {
        try {
            File outFile = PdfExporter.chooseSaveFile("gov_complaints.pdf");
            if (outFile == null) return;
            String[] headers = {"ID","Title","Type","Location","Votes","Priority","Status","Dept/Head","Filed"};
            List<String[]> rows = new ArrayList<>();
            String sql = "SELECT * FROM complaints WHERE category='Public-Area'" +
                (priorityFilter != null ? " AND priority IN ('Urgent','High')" : "") +
                " ORDER BY vote_count DESC, created_at DESC";
            try (Connection conn = DatabaseHelper.getConnection();
                 ResultSet rs = conn.createStatement().executeQuery(sql)) {
                while (rs.next()) rows.add(new String[]{
                    rs.getString("complaint_id"), rs.getString("title"), rs.getString("complaint_type"),
                    rs.getString("location"), String.valueOf(rs.getInt("vote_count")),
                    rs.getString("priority"), rs.getString("status"),
                    rs.getString("assigned_head") != null ? rs.getString("assigned_head") : "—",
                    rs.getString("created_at") != null ? rs.getString("created_at").substring(0,10) : ""});
            }
            PdfExporter.exportComplaintList("Government Public Complaints Report", headers, rows, outFile);
            showAlert("✅  PDF exported: " + outFile.getName());
        } catch (Exception ex) { showAlert("Export failed: " + ex.getMessage()); ex.printStackTrace(); }
    }

    private void exportPetitionsPdf() {
        try {
            File outFile = PdfExporter.chooseSaveFile("gov_petitions.pdf");
            if (outFile == null) return;
            String[] headers = {"ID","Title","Type","Location","Votes","Status","Filed"};
            List<String[]> rows = new ArrayList<>();
            try (Connection conn = DatabaseHelper.getConnection();
                 ResultSet rs = conn.createStatement().executeQuery(
                     "SELECT c.*, (SELECT COUNT(*) FROM votes WHERE complaint_id=c.complaint_id) as vcount " +
                     "FROM complaints c WHERE c.is_public=1 ORDER BY vcount DESC")) {
                while (rs.next()) rows.add(new String[]{
                    rs.getString("complaint_id"), rs.getString("title"), rs.getString("complaint_type"),
                    rs.getString("location"), String.valueOf(rs.getInt("vcount")),
                    rs.getString("status"), rs.getString("created_at") != null ? rs.getString("created_at").substring(0,10) : ""});
            }
            PdfExporter.exportComplaintList("Government Public Petitions Report", headers, rows, outFile);
            showAlert("✅  Petitions PDF exported: " + outFile.getName());
        } catch (Exception ex) { showAlert("Export failed: " + ex.getMessage()); ex.printStackTrace(); }
    }

    private void exportSingleComplaintPdf(ResultSet rs) {
        try {
            Map<String, String> fields = new LinkedHashMap<>();
            fields.put("Complaint ID",  rs.getString("complaint_id"));
            fields.put("Title",         rs.getString("title"));
            fields.put("Location",      rs.getString("location"));
            fields.put("Type",          rs.getString("complaint_type"));
            fields.put("Category",      rs.getString("category"));
            fields.put("Priority",      rs.getString("priority"));
            fields.put("Status",        rs.getString("status"));
            fields.put("Vote Count",    String.valueOf(rs.getInt("vote_count")));
            fields.put("Dept/Head",     rs.getString("assigned_head") != null ? rs.getString("assigned_head") : "—");
            fields.put("Filed On",      rs.getString("created_at"));

            File outFile = PdfExporter.chooseSaveFile(rs.getString("complaint_id") + "_gov_detail.pdf");
            if (outFile == null) return;

            List<String> comments = new ArrayList<>();
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                     "SELECT author_name, comment FROM comments WHERE complaint_id=? ORDER BY created_at")) {
                ps.setString(1, rs.getString("complaint_id"));
                ResultSet cr = ps.executeQuery();
                while (cr.next()) comments.add("[" + cr.getString("author_name") + "] " + cr.getString("comment"));
            } catch (SQLException ignored) {}

            PdfExporter.exportComplaintDetail(fields, comments, outFile);
            showAlert("✅  PDF saved: " + outFile.getName());
        } catch (Exception ex) { showAlert("Export failed: " + ex.getMessage()); ex.printStackTrace(); }
    }

    // ── Data Helpers ──────────────────────────────────────────────────────────

    private int[] fetchStats() {
        int[] r = {0, 0, 0, 0};
        try (Connection conn = DatabaseHelper.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(
                 "SELECT status, COUNT(*) as c FROM complaints WHERE category='Public-Area' GROUP BY status")) {
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

    private int fetchUrgentCount() {
        try (Connection conn = DatabaseHelper.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(
                 "SELECT COUNT(*) FROM complaints WHERE category='Public-Area' AND priority IN ('Urgent','High')")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private Map<String, Double> fetchTypeBreakdown() {
        Map<String, Double> map = new LinkedHashMap<>();
        try (Connection conn = DatabaseHelper.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(
                 "SELECT complaint_type, COUNT(*) as cnt FROM complaints WHERE category='Public-Area' GROUP BY complaint_type ORDER BY cnt DESC")) {
            while (rs.next()) map.put(rs.getString("complaint_type"), (double) rs.getInt("cnt"));
        } catch (SQLException e) { e.printStackTrace(); }
        if (map.isEmpty()) { map.put("Road", 1.0); map.put("Other", 1.0); }
        return map;
    }
}
