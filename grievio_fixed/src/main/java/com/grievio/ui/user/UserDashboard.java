package com.grievio.ui.user;

import com.grievio.MainApp;
import com.grievio.database.DatabaseHelper;
import com.grievio.model.Complaint;
import com.grievio.model.User;
import com.grievio.session.SessionManager;
import com.grievio.ui.components.MapHelper;
import com.grievio.ui.components.UIHelper;
import com.grievio.util.AIRouter;
import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.grievio.ui.components.UIHelper.*;

public class UserDashboard {

    private final HBox root;
    private final StackPane contentArea;
    private final Label headerTitleLbl;
    private Button currentNavBtn;

    private int selectedStars = 0;
    private HBox starsHBox;

    public UserDashboard() {
        root = new HBox();
        root.getStyleClass().add("root-bg");
        headerTitleLbl = lbl("Dashboard", 20, "white", true);
        contentArea = new StackPane();
        contentArea.setStyle("-fx-padding:22;");
        root.getChildren().addAll(buildSidebar(), buildMain());
    }

    public HBox getRoot() { return root; }

    // ── SIDEBAR ───────────────────────────────────────────────────────────────

    private VBox buildSidebar() {
        VBox sidebar = new VBox(4);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(240);
        sidebar.setPadding(new Insets(20, 10, 20, 10));

        HBox brand = new HBox(10);
        brand.setAlignment(Pos.CENTER_LEFT);
        brand.setPadding(new Insets(0, 0, 14, 0));
        VBox brandText = new VBox(2, lbl("Grievio", 20, "white", true), lbl("AI Complaint System", 9, "#64b5f6"));
        brand.getChildren().addAll(lbl("🔵", 22, "white"), brandText);

        User user = SessionManager.getInstance().getCurrentUser();
        VBox userCard = new VBox(3);
        userCard.setStyle("-fx-background-color:rgba(21,101,192,0.15);-fx-background-radius:10;-fx-padding:10 12;" +
            "-fx-border-color:#1565c0;-fx-border-radius:10;-fx-border-width:1;");
        userCard.getChildren().addAll(
            lbl("👤  " + (user != null ? user.getName() : "User"), 12, "white", true),
            lbl("Resident Portal", 10, "#64b5f6"));

        Button bDash     = navBtn("🏠", "Dashboard");
        Button bRequest  = navBtn("📝", "New Request");
        Button bCompl    = navBtn("📋", "My Complaints");
        Button bComments = navBtn("💬", "Comments");
        Button bRating   = navBtn("⭐", "Rate Service");
        Button bPetition = navBtn("🗣", "Public Petition");
        Button bReceipt  = navBtn("🧾", "Receipt");
        Button bChatbot  = navBtn("🤖", "AI Chatbot");
        Button bHelpline = navBtn("📞", "Helpline");

        bDash.setOnAction(e     -> show(buildDashboardSection(),     bDash,     "Dashboard"));
        bRequest.setOnAction(e  -> show(buildRequestSection(),        bRequest,  "New Service Request"));
        bCompl.setOnAction(e    -> show(buildComplaintsSection(),      bCompl,    "My Complaints"));
        bComments.setOnAction(e -> show(buildCommentsSection(),        bComments, "Comments & Updates"));
        bRating.setOnAction(e   -> show(buildRatingSection(),          bRating,   "Rate Resolution"));
        bPetition.setOnAction(e -> show(buildPetitionSection(),        bPetition, "Public Petition"));
        bReceipt.setOnAction(e  -> show(buildReceiptSection(),         bReceipt,  "Complaint Receipt"));
        bChatbot.setOnAction(e  -> show(buildChatbotSection(),         bChatbot,  "AI Chatbot"));
        bHelpline.setOnAction(e -> show(buildHelplineSection(),        bHelpline, "Helpline & Support"));

        Button bLogout = new Button("🚪   Logout");
        bLogout.getStyleClass().add("nav-btn-logout");
        bLogout.setPrefWidth(220);
        bLogout.setOnAction(e -> { SessionManager.getInstance().logout(); MainApp.showLogin(); });

        Region spacer = new Region(); VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().addAll(brand, mkSep(), mkRegion(4), userCard, mkRegion(6),
            bDash, bRequest, bCompl, bComments, bRating, bPetition, bReceipt, bChatbot, bHelpline,
            spacer, mkSep(), bLogout);

        setActive(bDash); currentNavBtn = bDash;
        return sidebar;
    }

    private Button navBtn(String icon, String label) {
        Button b = new Button(icon + "   " + label);
        b.getStyleClass().add("nav-btn");
        b.setPrefWidth(220);
        return b;
    }

    private void setActive(Button btn) {
        if (currentNavBtn != null) currentNavBtn.getStyleClass().remove("nav-active");
        btn.getStyleClass().add("nav-active");
        currentNavBtn = btn;
    }

    // ── MAIN LAYOUT ───────────────────────────────────────────────────────────

    private VBox buildMain() {
        VBox main = new VBox();
        HBox.setHgrow(main, Priority.ALWAYS);

        HBox header = new HBox();
        header.getStyleClass().add("top-header");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 25, 14, 25));

        User user = SessionManager.getInstance().getCurrentUser();
        Label userLbl = lbl("👤  " + (user != null ? user.getName() : "User"), 13, "#64b5f6");
        userLbl.setStyle(userLbl.getStyle() +
            "-fx-background-color:rgba(21,101,192,0.2);-fx-background-radius:20;-fx-padding:5 14;");
        Region hSp = new Region(); HBox.setHgrow(hSp, Priority.ALWAYS);
        header.getChildren().addAll(headerTitleLbl, hSp, userLbl);

        ScrollPane scroll = wrapScroll(contentArea);
        main.getChildren().addAll(header, scroll);
        contentArea.getChildren().setAll(buildDashboardSection());
        return main;
    }

    private void show(Node section, Button btn, String title) {
        headerTitleLbl.setText(title);
        setActive(btn);
        contentArea.getChildren().setAll(section);
        fadeIn(section);
    }

    // ── 1. DASHBOARD ──────────────────────────────────────────────────────────

    private VBox buildDashboardSection() {
        VBox s = new VBox(20);
        int[] stats = fetchStats();

        HBox cards = new HBox(15,
            statCard("Total", stats[0], "#1565c0", "#0d47a1"),
            statCard("Pending", stats[1], "#e65100", "#bf360c"),
            statCard("In Progress", stats[2], "#f57f17", "#e65100"),
            statCard("Completed", stats[3], "#2e7d32", "#1b5e20"));
        for (Node c : cards.getChildren()) HBox.setHgrow(c, Priority.ALWAYS);

        VBox welcome = new VBox(10);
        welcome.setStyle("-fx-background-color:linear-gradient(to right,#12294a,#1565c0);" +
            "-fx-background-radius:14;-fx-padding:24;");
        User user = SessionManager.getInstance().getCurrentUser();
        welcome.getChildren().addAll(
            lbl("👋  Welcome, " + (user != null ? user.getName() : "User") + "!", 20, "white", true),
            lbl("Submit, track, and manage complaints with AI-powered routing & real-time status updates.", 13, "#90caf9"),
            new HBox(12, primaryBtn("📝  New Complaint"), secondaryBtn("📋  View All")));
        ((Button) ((HBox) welcome.getChildren().get(2)).getChildren().get(0))
            .setOnAction(e -> show(buildRequestSection(),
                (Button) ((VBox) root.getChildren().get(0)).getChildren().get(5), "New Service Request"));

        // Recent complaints
        VBox recentCard = formCard("📋  Recent Complaints");
        List<Complaint> recent = fetchComplaints(3);
        if (recent.isEmpty()) {
            recentCard.getChildren().add(lbl("No complaints yet. Submit your first complaint!", 13, "#78909c"));
        } else {
            for (Complaint c : recent) recentCard.getChildren().add(buildCompactRow(c));
        }

        // Tips
        HBox tips = new HBox(15,
            infoCard("🗺  Map Location Picker", "Click '🗺 Pick on Map' to pin your exact location on an interactive map."),
            infoCard("🤖  AI Chatbot", "Use the AI Chatbot for instant help on complaints and features."),
            infoCard("🗣  Public Petition", "Support public issues by voting on petitions in your locality."));
        for (Node t : tips.getChildren()) HBox.setHgrow(t, Priority.ALWAYS);

        s.getChildren().addAll(sectionTitle("Overview"), cards, welcome, recentCard, tips);
        return s;
    }

    private HBox buildCompactRow(Complaint c) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color:#12294a;-fx-background-radius:10;-fx-padding:12;");
        VBox info = new VBox(3,
            lbl(c.getComplaintId() + "  —  " + c.getTitle(), 13, "white", true),
            lbl(c.getComplaintType() + "  |  " + c.getLocation(), 11, "#78909c"));
        HBox.setHgrow(info, Priority.ALWAYS);
        row.getChildren().addAll(info, statusBadge(c.getStatus()), priorityBadge(c.getPriority()));
        return row;
    }

    // ── 2. SERVICE REQUEST ────────────────────────────────────────────────────

    private VBox buildRequestSection() {
        VBox section = new VBox(20);
        section.getChildren().add(sectionTitle("File a New Complaint"));

        VBox form = formCard("Complaint Details");

        TextField titleField = inputField("Brief title for your complaint");
        form.getChildren().addAll(formLbl("Complaint Title *"), titleField);

        User user = SessionManager.getInstance().getCurrentUser();
        TextField societyField = inputField("Type your society / locality name");
        if (user != null && user.getSociety() != null) societyField.setText(user.getSociety());

        Button socMapBtn = new Button("🗺  Pick");
        socMapBtn.setStyle("-fx-background-color:#1565c0;-fx-text-fill:white;-fx-font-weight:bold;" +
            "-fx-background-radius:8;-fx-padding:9 14;-fx-cursor:hand;");
        socMapBtn.setOnAction(e -> {
            String result = MapHelper.showMapPicker("Pick Society / Locality", "Use This Location");
            if (result != null) societyField.setText(result);
        });
        HBox socRow = new HBox(8, societyField, socMapBtn);
        socRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(societyField, Priority.ALWAYS);

        ComboBox<String> typeBox = comboField();
        typeBox.getItems().addAll("Plumbing", "Electrical", "Sanitation", "Road",
            "Security", "Water Supply", "Lift/Elevator", "Parking", "Garbage", "Other");

        VBox socGroup = new VBox(5, formLbl("Society / Locality *"), socRow);
        HBox row1 = new HBox(12, socGroup, fieldGroup("Complaint Type *", typeBox));
        for (Node n : row1.getChildren()) HBox.setHgrow(n, Priority.ALWAYS);
        form.getChildren().add(row1);

        TextField locationField = inputField("Type locality OR click 'Pick on Map'");
        Button mapPickBtn = new Button("🗺  Pick on Map");
        mapPickBtn.setStyle("-fx-background-color:#1565c0;-fx-text-fill:white;-fx-font-weight:bold;" +
            "-fx-background-radius:8;-fx-padding:9 16;-fx-cursor:hand;");
        mapPickBtn.setOnAction(e -> {
            String result = MapHelper.showMapPicker("Pick Complaint Location", "Confirm This Location");
            if (result != null) locationField.setText(result);
        });
        HBox locRow = new HBox(10, locationField, mapPickBtn);
        locRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(locationField, Priority.ALWAYS);
        form.getChildren().addAll(formLbl("Location * (click map or type manually)"), locRow);

        TextArea descField = textArea("Describe your complaint in detail...");
        ComboBox<String> priorityBox = comboField();
        priorityBox.getItems().addAll("Urgent", "High", "Medium", "Low");

        HBox row2 = new HBox(12, fieldGroup("Description *", descField), fieldGroup("Priority *", priorityBox));
        for (Node n : row2.getChildren()) HBox.setHgrow(n, Priority.ALWAYS);
        form.getChildren().add(row2);

        // AI Analysis box
        Label catLbl = lbl("", 12, "#e0e0e0");
        Label headLbl = lbl("", 12, "#e0e0e0");
        Label daysLbl = lbl("", 12, "#e0e0e0");
        VBox aiBox = new VBox(5, lbl("🤖  AI Analysis", 13, "#64b5f6", true), catLbl, headLbl, daysLbl);
        aiBox.setStyle("-fx-background-color:rgba(21,101,192,0.15);-fx-border-color:#1565c0;" +
            "-fx-border-width:1;-fx-border-radius:8;-fx-background-radius:8;-fx-padding:12;");
        form.getChildren().add(aiBox);

        // Receipt pane
        VBox receiptPane = new VBox(10);
        receiptPane.setStyle("-fx-background-color:#091524;-fx-background-radius:10;" +
            "-fx-border-color:#00e676;-fx-border-width:1;-fx-border-radius:10;-fx-padding:16;");
        receiptPane.setVisible(false); receiptPane.setManaged(false);

        Button analyzeBtn = secondaryBtn("🔍  Analyze with AI");
        Button submitBtn  = primaryBtn("✅  Submit Complaint");

        analyzeBtn.setOnAction(e -> {
            String type = typeBox.getValue(), desc = descField.getText(), prio = priorityBox.getValue();
            if (type == null || desc.isBlank() || prio == null) {
                showAlert("Please fill complaint type, description and priority first."); return;
            }
            String cat  = AIRouter.classify(type, desc);
            String head = AIRouter.assignHead(cat, type);
            int days    = AIRouter.predictDays(prio);
            catLbl.setText("🤖  Classification: " + cat + "  →  " + (cat.equals("Society-Level") ? "🏢 Society Admin" : "🏛 Government Dept"));
            headLbl.setText("👤  Assigned To: " + head);
            daysLbl.setText("📅  Estimated Resolution: " + days + " days");
        });

        submitBtn.setOnAction(e -> {
            String t = titleField.getText().trim(), soc = societyField.getText().trim();
            String loc = locationField.getText().trim(), type = typeBox.getValue();
            String desc = descField.getText().trim(), prio = priorityBox.getValue();
            if (t.isEmpty() || soc.isEmpty() || loc.isEmpty() || type == null || desc.isEmpty() || prio == null) {
                showAlert("Please fill all required fields (*).");  return;
            }
            String cat  = AIRouter.classify(type, desc);
            String head = AIRouter.assignHead(cat, type);
            int days    = AIRouter.predictDays(prio);
            String cId  = AIRouter.generateComplaintId();
            int userId  = user != null ? user.getId() : 1;

            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO complaints (complaint_id,user_id,title,society_name,location," +
                     "complaint_type,description,priority,category,assigned_head,predicted_days,is_public)" +
                     " VALUES (?,?,?,?,?,?,?,?,?,?,?,?)")) {
                ps.setString(1, cId); ps.setInt(2, userId); ps.setString(3, t);
                ps.setString(4, soc); ps.setString(5, loc); ps.setString(6, type);
                ps.setString(7, desc); ps.setString(8, prio); ps.setString(9, cat);
                ps.setString(10, head); ps.setInt(11, days);
                ps.setInt(12, "Public-Area".equals(cat) ? 1 : 0);
                ps.executeUpdate();
            } catch (SQLException ex) { showAlert("Error: " + ex.getMessage()); return; }

            receiptPane.getChildren().clear();
            receiptPane.getChildren().addAll(
                lbl("✅  Complaint Submitted Successfully!", 16, "#00e676", true),
                mkSep(),
                tableRow("Complaint ID", cId), tableRow("Title", t),
                tableRow("Society", soc), tableRow("Location", loc),
                tableRow("Type", type), tableRow("Category", cat),
                tableRow("Assigned To", head), tableRow("Priority", prio),
                tableRow("Est. Days", days + " working days"),
                tableRow("Submitted", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"))));
            receiptPane.setVisible(true); receiptPane.setManaged(true);
            fadeIn(receiptPane);
            titleField.clear(); locationField.clear(); descField.clear();
            typeBox.setValue(null); priorityBox.setValue(null);
            catLbl.setText(""); headLbl.setText(""); daysLbl.setText("");
        });

        form.getChildren().addAll(new HBox(12, analyzeBtn, submitBtn), receiptPane);
        section.getChildren().add(form);
        return section;
    }

    // ── 3. MY COMPLAINTS ──────────────────────────────────────────────────────

    private VBox buildComplaintsSection() {
        VBox section = new VBox(20);
        section.getChildren().add(sectionTitle("My Complaints"));

        ComboBox<String> filterStatus = comboField();
        filterStatus.getItems().addAll("All", "Pending", "In Progress", "Completed");
        filterStatus.setValue("All");
        filterStatus.setMaxWidth(180);

        ComboBox<String> filterPriority = comboField();
        filterPriority.getItems().addAll("All Priorities", "Urgent", "High", "Medium", "Low");
        filterPriority.setValue("All Priorities");
        filterPriority.setMaxWidth(180);

        VBox listBox = new VBox(10);
        Runnable refresh = () -> {
            listBox.getChildren().clear();
            String sf = filterStatus.getValue(), pf = filterPriority.getValue();
            List<Complaint> complaints = fetchComplaints(-1);
            boolean any = false;
            for (Complaint c : complaints) {
                if ((sf == null || sf.equals("All") || sf.equals(c.getStatus())) &&
                    (pf == null || pf.equals("All Priorities") || pf.equals(c.getPriority()))) {
                    listBox.getChildren().add(buildComplaintCard(c));
                    any = true;
                }
            }
            if (!any) listBox.getChildren().add(lbl("No complaints match the selected filter.", 13, "#78909c"));
        };
        filterStatus.setOnAction(e -> refresh.run());
        filterPriority.setOnAction(e -> refresh.run());
        refresh.run();

        HBox filterRow = new HBox(12, lbl("Filter by Status:", 12, "#90caf9"), filterStatus,
            lbl("Priority:", 12, "#90caf9"), filterPriority);
        filterRow.setAlignment(Pos.CENTER_LEFT);
        section.getChildren().addAll(filterRow, listBox);
        return section;
    }

    private VBox buildComplaintCard(Complaint c) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color:#12294a;-fx-background-radius:12;-fx-padding:16;" +
            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.3),8,0,0,2);");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label idLbl = lbl(c.getComplaintId(), 13, "#64b5f6", true);
        Label titleLbl = lbl(c.getTitle(), 13, "white", true);
        HBox.setHgrow(titleLbl, Priority.ALWAYS);
        Region hSp = new Region(); HBox.setHgrow(hSp, Priority.ALWAYS);
        header.getChildren().addAll(idLbl, titleLbl, hSp, statusBadge(c.getStatus()), priorityBadge(c.getPriority()));

        HBox meta = new HBox(20,
            lbl("📍  " + c.getLocation(), 11, "#78909c"),
            lbl("🏷  " + c.getComplaintType(), 11, "#78909c"),
            lbl("🤖  " + (c.getCategory() != null ? c.getCategory() : "—"), 11, "#78909c"),
            lbl("📅  " + (c.getCreatedAt() != null ? c.getCreatedAt().substring(0, 10) : "—"), 11, "#78909c"));

        HBox info2 = new HBox(20,
            lbl("👤  Assigned: " + (c.getAssignedHead() != null ? c.getAssignedHead() : "Pending"), 11, "#90caf9"),
            lbl("⏱  Est: " + c.getPredictedDays() + " days", 11, "#90caf9"));

        card.getChildren().addAll(header, meta, info2);
        if (c.getDescription() != null && !c.getDescription().isEmpty()) {
            Label desc = lbl(c.getDescription(), 12, "#90a4ae");
            desc.setWrapText(true);
            card.getChildren().add(desc);
        }
        return card;
    }

    // ── 4. COMMENTS ───────────────────────────────────────────────────────────

    private VBox buildCommentsSection() {
        VBox section = new VBox(20);
        section.getChildren().add(sectionTitle("Comments & Updates"));

        VBox form = formCard("Post a Comment");
        ComboBox<String> complaintBox = comboField();
        loadComplaintCombo(complaintBox);
        TextArea commentArea = textArea("Write your comment or question...");

        VBox historyBox = new VBox(8);
        historyBox.setStyle("-fx-background-color:#091524;-fx-background-radius:10;-fx-padding:14;");

        Runnable loadHistory = () -> {
            historyBox.getChildren().clear();
            historyBox.getChildren().add(lbl("💬  Comment History", 14, "#90caf9", true));
            String cId = extractId(complaintBox.getValue());
            if (cId == null) return;
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM comments WHERE complaint_id=? ORDER BY created_at")) {
                ps.setString(1, cId);
                ResultSet rs = ps.executeQuery();
                boolean any = false;
                while (rs.next()) {
                    VBox bubble = new VBox(3);
                    bubble.setStyle("-fx-background-color:#12294a;-fx-background-radius:8;-fx-padding:10;");
                    bubble.getChildren().addAll(
                        lbl("💬  " + rs.getString("author_name"), 11, "#64b5f6", true),
                        lbl(rs.getString("comment"), 12, "#e0e0e0"),
                        lbl(rs.getString("created_at"), 10, "#546e7a"));
                    historyBox.getChildren().add(bubble);
                    any = true;
                }
                if (!any) historyBox.getChildren().add(lbl("No comments yet on this complaint.", 12, "#78909c"));
            } catch (SQLException e) { e.printStackTrace(); }
        };

        complaintBox.setOnAction(e -> loadHistory.run());

        Button postBtn = primaryBtn("💬  Post Comment");
        postBtn.setOnAction(e -> {
            String cId = extractId(complaintBox.getValue());
            String txt = commentArea.getText().trim();
            User user = SessionManager.getInstance().getCurrentUser();
            if (cId == null || txt.isEmpty()) { showAlert("Select a complaint and write a comment."); return; }
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO comments (complaint_id,user_id,author_name,comment) VALUES (?,?,?,?)")) {
                ps.setString(1, cId); ps.setInt(2, user != null ? user.getId() : 1);
                ps.setString(3, user != null ? user.getName() : "User"); ps.setString(4, txt);
                ps.executeUpdate();
                commentArea.clear(); loadHistory.run();
            } catch (SQLException ex) { showAlert("Error: " + ex.getMessage()); }
        });

        form.getChildren().addAll(fieldGroup("Select Complaint", complaintBox),
            fieldGroup("Your Comment", commentArea), postBtn);
        section.getChildren().addAll(form, historyBox);
        return section;
    }

    // ── 5. RATING ─────────────────────────────────────────────────────────────

    private VBox buildRatingSection() {
        VBox section = new VBox(20);
        section.getChildren().add(sectionTitle("Rate Resolution"));

        VBox form = formCard("Submit Feedback & Rating");
        ComboBox<String> complaintBox = comboField();
        loadComplaintCombo(complaintBox);

        // Stars
        starsHBox = new HBox(8);
        selectedStars = 0;
        Label[] stars = new Label[5];
        for (int i = 0; i < 5; i++) {
            final int star = i + 1;
            stars[i] = lbl("☆", 28, "#ffb300");
            stars[i].setStyle(stars[i].getStyle() + "-fx-cursor:hand;");
            stars[i].setOnMouseClicked(e -> {
                selectedStars = star;
                updateStars(stars, star);
            });
        }
        starsHBox.getChildren().addAll(stars);

        TextArea feedbackArea = textArea("Optional: Write your feedback about resolution...");
        Label thankLbl = lbl("", 13, "#00e676", true);

        Button submitBtn = primaryBtn("⭐  Submit Rating");
        submitBtn.setOnAction(e -> {
            String cId = extractId(complaintBox.getValue());
            if (cId == null || selectedStars == 0) {
                showAlert("Please select a complaint and give a star rating."); return;
            }
            User user = SessionManager.getInstance().getCurrentUser();
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                     "INSERT OR REPLACE INTO ratings (complaint_id,user_id,stars,feedback) VALUES (?,?,?,?)")) {
                ps.setString(1, cId); ps.setInt(2, user != null ? user.getId() : 1);
                ps.setInt(3, selectedStars); ps.setString(4, feedbackArea.getText().trim());
                ps.executeUpdate();
                thankLbl.setText("✅  Thank you! You rated " + selectedStars + " ⭐ for " + cId);
                feedbackArea.clear(); selectedStars = 0;
                updateStars(stars, 0);
            } catch (SQLException ex) { showAlert("Error: " + ex.getMessage()); }
        });

        form.getChildren().addAll(fieldGroup("Select Complaint", complaintBox),
            lbl("Your Rating *", 12, "#90caf9", true), starsHBox,
            fieldGroup("Feedback (optional)", feedbackArea), thankLbl, submitBtn);
        section.getChildren().add(form);
        return section;
    }

    private void updateStars(Label[] stars, int filled) {
        for (int i = 0; i < 5; i++)
            stars[i].setText(i < filled ? "★" : "☆");
    }

    // ── 6. PUBLIC PETITION ────────────────────────────────────────────────────

    private VBox buildPetitionSection() {
        VBox section = new VBox(20);
        section.getChildren().add(sectionTitle("Public Petition"));

        VBox info = new VBox(6);
        info.setStyle("-fx-background-color:rgba(21,101,192,0.15);-fx-border-color:#1565c0;" +
            "-fx-border-width:1;-fx-border-radius:10;-fx-background-radius:10;-fx-padding:14;");
        info.getChildren().addAll(
            lbl("🗣  What is a Petition?", 14, "#64b5f6", true),
            lbl("Public complaints become petitions when they are visible to the community.", 12, "#e0e0e0"),
            lbl("Vote on petitions to increase their priority. Complaints with more votes get faster attention.", 12, "#e0e0e0"));

        // Filter by society
        ComboBox<String> societyFilter = comboField();
        societyFilter.getItems().add("All Societies");
        societyFilter.setValue("All Societies");
        try (Connection conn = DatabaseHelper.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(
                 "SELECT DISTINCT society_name FROM complaints WHERE is_public=1 AND society_name IS NOT NULL")) {
            while (rs.next()) societyFilter.getItems().add(rs.getString(1));
        } catch (SQLException ignored) {}

        VBox petitionsBox = new VBox(12);
        // Use array wrapper so lambda can self-reference before assignment (Java compiler requirement)
        Runnable[] loadPetitionsRef = new Runnable[1];
        loadPetitionsRef[0] = () -> {
            petitionsBox.getChildren().clear();
            String soc = societyFilter.getValue();
            User user = SessionManager.getInstance().getCurrentUser();
            int userId = user != null ? user.getId() : 1;
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                     "SELECT c.*, " +
                     "(SELECT COUNT(*) FROM votes WHERE complaint_id=c.complaint_id) as vcount," +
                     "(SELECT COUNT(*) FROM votes WHERE complaint_id=c.complaint_id AND user_id=?) as myvote " +
                     "FROM complaints c WHERE c.is_public=1 " +
                     (soc != null && !soc.equals("All Societies") ? "AND c.society_name=? " : "") +
                     "ORDER BY vcount DESC")) {
                ps.setInt(1, userId);
                if (soc != null && !soc.equals("All Societies")) ps.setString(2, soc);
                ResultSet rs = ps.executeQuery();
                boolean any = false;
                while (rs.next()) {
                    String cId = rs.getString("complaint_id");
                    int votes = rs.getInt("vcount");
                    boolean myVote = rs.getInt("myvote") > 0;
                    petitionsBox.getChildren().add(buildPetitionCard(cId, rs, votes, myVote, userId, loadPetitionsRef[0]));
                    any = true;
                }
                if (!any) petitionsBox.getChildren().add(lbl("No public petitions yet.", 13, "#78909c"));
            } catch (SQLException e) { e.printStackTrace(); }
        };
        Runnable loadPetitions = loadPetitionsRef[0];
        societyFilter.setOnAction(e -> loadPetitions.run());
        loadPetitions.run();

        HBox filterRow = new HBox(12, lbl("Filter by Society:", 12, "#90caf9"), societyFilter);
        filterRow.setAlignment(Pos.CENTER_LEFT);
        section.getChildren().addAll(info, filterRow, petitionsBox);
        return section;
    }

    private VBox buildPetitionCard(String cId, ResultSet rs, int votes, boolean myVote, int userId, Runnable refresh) {
        try {
            VBox card = new VBox(10);
            card.setStyle("-fx-background-color:#12294a;-fx-background-radius:12;-fx-padding:16;" +
                "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.3),8,0,0,2);");

            HBox top = new HBox(10);
            top.setAlignment(Pos.CENTER_LEFT);
            Label titleLbl = lbl(rs.getString("title"), 14, "white", true);
            HBox.setHgrow(titleLbl, Priority.ALWAYS);
            Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
            top.getChildren().addAll(lbl("🏛", 16, "white"), titleLbl, sp, statusBadge(rs.getString("status")));

            HBox meta = new HBox(16,
                lbl("📍  " + rs.getString("location"), 11, "#78909c"),
                lbl("🏷  " + rs.getString("complaint_type"), 11, "#78909c"),
                lbl("🏢  " + rs.getString("society_name"), 11, "#78909c"),
                lbl("📅  " + (rs.getString("created_at") != null ? rs.getString("created_at").substring(0, 10) : ""), 11, "#78909c"));

            Label voteLbl = lbl("👍  " + votes + " votes", 13, "#ffb300", true);
            Button voteBtn = myVote ? successBtn("✅  Voted") : primaryBtn("👍  Vote");
            if (myVote) voteBtn.setDisable(true);
            voteBtn.setOnAction(e -> {
                try (Connection conn = DatabaseHelper.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                         "INSERT OR IGNORE INTO votes (complaint_id,user_id) VALUES (?,?)")) {
                    ps.setString(1, cId); ps.setInt(2, userId);
                    ps.executeUpdate();
                    refresh.run();
                } catch (SQLException ex) { ex.printStackTrace(); }
            });

            HBox voteRow = new HBox(12, voteLbl, voteBtn);
            voteRow.setAlignment(Pos.CENTER_LEFT);
            card.getChildren().addAll(top, meta, voteRow);
            return card;
        } catch (SQLException e) { return new VBox(); }
    }

    // ── 7. RECEIPT ────────────────────────────────────────────────────────────

    private VBox buildReceiptSection() {
        VBox section = new VBox(20);
        section.getChildren().add(sectionTitle("Complaint Receipt"));

        VBox form = formCard("View Complaint Receipt");
        ComboBox<String> complaintBox = comboField();
        loadComplaintCombo(complaintBox);

        VBox receiptDisplay = new VBox(10);
        receiptDisplay.setStyle("-fx-background-color:#091524;-fx-background-radius:12;-fx-padding:20;" +
            "-fx-border-color:#1565c0;-fx-border-width:1;-fx-border-radius:12;");
        receiptDisplay.getChildren().add(lbl("Select a complaint above to view its receipt.", 13, "#78909c"));

        complaintBox.setOnAction(e -> {
            String cId = extractId(complaintBox.getValue());
            if (cId == null) return;
            receiptDisplay.getChildren().clear();
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM complaints WHERE complaint_id=?")) {
                ps.setString(1, cId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    receiptDisplay.getChildren().addAll(
                        lbl("🧾  Official Complaint Receipt", 16, "#64b5f6", true),
                        lbl("Grievio — AI Smart Complaint System", 11, "#546e7a"),
                        mkSep(),
                        tableRow("Complaint ID",    rs.getString("complaint_id")),
                        tableRow("Title",           rs.getString("title")),
                        tableRow("Society",         rs.getString("society_name")),
                        tableRow("Location",        rs.getString("location")),
                        tableRow("Type",            rs.getString("complaint_type")),
                        tableRow("Category",        rs.getString("category")),
                        tableRow("Priority",        rs.getString("priority")),
                        tableRow("Status",          rs.getString("status")),
                        tableRow("Assigned Head",   rs.getString("assigned_head")),
                        tableRow("Worker Assigned", rs.getString("assigned_worker") != null ? rs.getString("assigned_worker") : "Not yet"),
                        tableRow("Est. Days",       rs.getInt("predicted_days") + " working days"),
                        tableRow("Filed On",        rs.getString("created_at")),
                        mkSep(),
                        lbl("Note: Keep this receipt for tracking your complaint.", 11, "#546e7a"));
                }
            } catch (SQLException ex) { ex.printStackTrace(); }
        });

        Button refreshBtn = secondaryBtn("🔄  Refresh");
        refreshBtn.setOnAction(e -> complaintBox.getOnAction().handle(null));

        // PDF Export button for receipt
        Button pdfExportBtn = new Button("📄  Download PDF Receipt");
        pdfExportBtn.setStyle("-fx-background-color:#1565c0;-fx-text-fill:white;-fx-font-weight:bold;" +
            "-fx-background-radius:8;-fx-padding:10 22;-fx-cursor:hand;-fx-font-size:12px;");
        pdfExportBtn.setOnAction(e -> {
            String cId = extractId(complaintBox.getValue());
            if (cId == null) { showAlert("Please select a complaint first."); return; }
            try {
                java.io.File outFile = com.grievio.util.PdfExporter.chooseSaveFile(cId + "_receipt.pdf");
                if (outFile == null) return;
                java.util.Map<String, String> fields = new java.util.LinkedHashMap<>();
                try (Connection conn = DatabaseHelper.getConnection();
                     PreparedStatement ps = conn.prepareStatement("SELECT * FROM complaints WHERE complaint_id=?")) {
                    ps.setString(1, cId);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        fields.put("Complaint ID",    rs.getString("complaint_id"));
                        fields.put("Title",           rs.getString("title"));
                        fields.put("Society",         rs.getString("society_name"));
                        fields.put("Location",        rs.getString("location"));
                        fields.put("Type",            rs.getString("complaint_type"));
                        fields.put("Category",        rs.getString("category"));
                        fields.put("Priority",        rs.getString("priority"));
                        fields.put("Status",          rs.getString("status"));
                        fields.put("Assigned Head",   rs.getString("assigned_head") != null ? rs.getString("assigned_head") : "—");
                        fields.put("Worker Assigned", rs.getString("assigned_worker") != null ? rs.getString("assigned_worker") : "Not yet");
                        fields.put("Est. Days",       rs.getInt("predicted_days") + " working days");
                        fields.put("Filed On",        rs.getString("created_at"));
                    }
                }
                java.util.List<String> comments = new java.util.ArrayList<>();
                try (Connection conn = DatabaseHelper.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                         "SELECT author_name, comment FROM comments WHERE complaint_id=? ORDER BY created_at")) {
                    ps.setString(1, cId);
                    ResultSet cr = ps.executeQuery();
                    while (cr.next()) comments.add("[" + cr.getString("author_name") + "] " + cr.getString("comment"));
                } catch (SQLException ignored2) {}
                com.grievio.util.PdfExporter.exportComplaintDetail(fields, comments, outFile);
                showAlert("✅  Receipt PDF saved: " + outFile.getName());
            } catch (Exception ex) { showAlert("Export failed: " + ex.getMessage()); ex.printStackTrace(); }
        });

        form.getChildren().addAll(fieldGroup("Select Complaint", complaintBox), new HBox(12, refreshBtn, pdfExportBtn));
        section.getChildren().addAll(form, receiptDisplay);
        return section;
    }

    // ── 8. AI CHATBOT ─────────────────────────────────────────────────────────

    private VBox buildChatbotSection() {
        VBox section = new VBox(20);
        section.getChildren().add(sectionTitle("AI Chatbot"));

        VBox chatBox = new VBox();
        chatBox.setStyle("-fx-background-color:#091524;-fx-background-radius:12;-fx-padding:16;" +
            "-fx-border-color:#1565c0;-fx-border-width:1;-fx-border-radius:12;");

        VBox messages = new VBox(10);
        messages.setPadding(new Insets(8));

        ScrollPane scroll = new ScrollPane(messages);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:#091524;-fx-background:#091524;");
        scroll.setPrefHeight(380);

        addBotMsg(messages, "👋  Hi! I'm Grievio AI Assistant. Ask me about complaints, status, routing, or any feature of the system.");

        TextField inputField = inputField("Type your question...");
        Button sendBtn = primaryBtn("Send →");

        Runnable sendMsg = () -> {
            String msg = inputField.getText().trim();
            if (msg.isEmpty()) return;
            addUserMsg(messages, msg);
            inputField.clear();
            String response = generateBotResponse(msg);
            addBotMsg(messages, response);
            scroll.setVvalue(1.0);
        };
        sendBtn.setOnAction(e -> sendMsg.run());
        inputField.setOnAction(e -> sendMsg.run());

        HBox inputRow = new HBox(10, inputField, sendBtn);
        inputRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(inputField, Priority.ALWAYS);
        inputRow.setPadding(new Insets(10, 0, 0, 0));
        inputRow.setStyle("-fx-border-color:#1565c0;-fx-border-width:1 0 0 0;-fx-padding:10 0 0 0;");

        chatBox.getChildren().addAll(scroll, inputRow);
        section.getChildren().add(chatBox);
        return section;
    }

    private void addBotMsg(VBox messages, String text) {
        Label l = lbl("🤖  " + text, 12, "#e0e0e0");
        l.setWrapText(true);
        VBox bubble = new VBox(l);
        bubble.setStyle("-fx-background-color:#12294a;-fx-background-radius:10;-fx-padding:10 14;");
        bubble.setMaxWidth(520);
        HBox row = new HBox(bubble);
        row.setAlignment(Pos.CENTER_LEFT);
        messages.getChildren().add(row);
    }

    private void addUserMsg(VBox messages, String text) {
        Label l = lbl(text, 12, "white");
        l.setWrapText(true);
        VBox bubble = new VBox(l);
        bubble.setStyle("-fx-background-color:#1565c0;-fx-background-radius:10;-fx-padding:10 14;");
        bubble.setMaxWidth(420);
        HBox row = new HBox(bubble);
        row.setAlignment(Pos.CENTER_RIGHT);
        messages.getChildren().add(row);
    }

    private String generateBotResponse(String msg) {
        String m = msg.toLowerCase();
        if (m.contains("submit") || m.contains("complaint") || m.contains("new request"))
            return "To submit a complaint, click '📝 New Request' in the sidebar. Fill in the title, society, location (use the map picker!), type, description, and priority. Click 'Analyze with AI' to auto-classify, then 'Submit Complaint'.";
        if (m.contains("status") || m.contains("track"))
            return "Go to '📋 My Complaints' to track your complaint status: Pending → In Progress → Completed. Each card shows the assigned officer and estimated resolution days.";
        if (m.contains("priority") || m.contains("urgent") || m.contains("high"))
            return "Priority levels: 🔴 Urgent (1-2 days), 🟠 High (2-4 days), 🟡 Medium (5-9 days), 🟢 Low (10-20 days). AI automatically suggests priority based on complaint type and description.";
        if (m.contains("routing") || m.contains("ai") || m.contains("assign"))
            return "Grievio's AI classifies complaints into:\n• Society-Level → Water leakage, lift, security, parking\n• Public-Area → Roads, electricity, sanitation, drainage\nRouting is direct and automated — no manual step needed!";
        if (m.contains("petition") || m.contains("vote") || m.contains("public"))
            return "Public Petitions (🗣 sidebar) shows all public-area complaints. Vote to increase visibility — complaints with more votes get prioritized by municipal authorities!";
        if (m.contains("receipt") || m.contains("document") || m.contains("proof"))
            return "Go to '🧾 Receipt' in the sidebar. Select your complaint to view its full official receipt with assigned officer, category, predicted days, and filing date.";
        if (m.contains("rating") || m.contains("feedback") || m.contains("star"))
            return "After your complaint is resolved, go to '⭐ Rate Service'. Select your complaint, give a star rating (1-5), and optionally write feedback. This helps improve service quality!";
        if (m.contains("comment") || m.contains("update"))
            return "Go to '💬 Comments' to view all updates from assigned officers and post your own queries on any complaint.";
        if (m.contains("helpline") || m.contains("contact") || m.contains("emergency"))
            return "📞 Emergency Helpline Numbers:\n• Police: 100 | Fire: 101 | Ambulance: 108\n• National Helpline: 1800-111-555\n• Municipal Corp: 1533 | Water Board: 1916\nCheck the Helpline section for more!";
        if (m.contains("hello") || m.contains("hi") || m.contains("hey"))
            return "Hello! 👋 How can I help you today? You can ask me about submitting complaints, tracking status, understanding AI routing, petitions, ratings, or anything else about Grievio!";
        return "I can help you with: submitting complaints, tracking status, understanding AI routing, voting on petitions, ratings, receipts, and helpline info. What would you like to know?";
    }

    // ── 9. HELPLINE ───────────────────────────────────────────────────────────

    private VBox buildHelplineSection() {
        VBox section = new VBox(20);
        section.getChildren().add(sectionTitle("Helpline & Support"));

        HBox row1 = new HBox(15,
            helplineCard("🚨  Emergency", "#ef5350", "Police", "100", "Fire", "101", "Ambulance", "108", "Women Helpline", "1091"),
            helplineCard("🏛  Municipal", "#1565c0", "Municipal Corp", "1533", "Water Board", "1916", "Electricity Board", "1912", "Sanitation", "1800-123-4567"),
            helplineCard("📞  Grievio Support", "#2e7d32", "Email", "official.grievio@gmail.com", "National", "1800-111-555", "Grievio Helpdesk", "+91-9876543210", "Whatsapp", "+91-9876543210"));
        for (Node c : row1.getChildren()) HBox.setHgrow(c, Priority.ALWAYS);

        VBox faqCard = formCard("Frequently Asked Questions");
        faqCard.getChildren().addAll(
            faqRow("How long does it take to resolve a complaint?", "Depends on priority: Urgent 1-2 days, High 2-4 days, Medium 5-9 days, Low 10-20 days."),
            faqRow("Can I edit my complaint after submission?", "Currently complaints cannot be edited after submission. Contact support if needed."),
            faqRow("How does AI routing work?", "AI analyzes keywords in your complaint type and description to classify it as Society-Level or Public-Area and auto-assigns the appropriate officer."),
            faqRow("What is the difference between Society-Level and Public-Area?", "Society-Level issues (lift, leakage, parking) go to Society Admin. Public-Area issues (roads, electricity, sanitation) go directly to Government departments."),
            faqRow("How do I check who is handling my complaint?", "Go to '🧾 Receipt' or '📋 My Complaints' to see the assigned head and worker for your complaint."));

        section.getChildren().addAll(row1, faqCard);
        return section;
    }

    private VBox helplineCard(String title, String color, String... items) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color:#12294a;-fx-background-radius:12;-fx-padding:16;" +
            "-fx-border-color:" + color + ";-fx-border-width:0 0 0 4;-fx-border-radius:0 12 12 0;");
        card.getChildren().add(lbl(title, 14, color, true));
        for (int i = 0; i < items.length - 1; i += 2) {
            HBox row = new HBox(8, lbl(items[i] + ":", 11, "#78909c"), lbl(items[i + 1], 11, "white", true));
            card.getChildren().add(row);
        }
        return card;
    }

    private VBox faqRow(String q, String a) {
        VBox row = new VBox(4);
        row.setStyle("-fx-padding:8 0;-fx-border-color:transparent transparent rgba(255,255,255,0.07) transparent;-fx-border-width:0 0 1 0;");
        row.getChildren().addAll(lbl("❓  " + q, 12, "#90caf9", true), lbl(a, 12, "#90a4ae"));
        return row;
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private int[] fetchStats() {
        int[] r = {0, 0, 0, 0};
        User u = SessionManager.getInstance().getCurrentUser();
        if (u == null) return r;
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT status, COUNT(*) as c FROM complaints WHERE user_id=? GROUP BY status")) {
            ps.setInt(1, u.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int c = rs.getInt("c"); r[0] += c;
                switch (rs.getString("status")) {
                    case "Pending"     -> r[1] += c;
                    case "In Progress" -> r[2] += c;
                    case "Completed"   -> r[3] += c;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return r;
    }

    private List<Complaint> fetchComplaints(int limit) {
        List<Complaint> list = new ArrayList<>();
        User u = SessionManager.getInstance().getCurrentUser();
        if (u == null) return list;
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT * FROM complaints WHERE user_id=? ORDER BY created_at DESC" +
                 (limit > 0 ? " LIMIT " + limit : ""))) {
            ps.setInt(1, u.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Complaint c = new Complaint();
                c.setComplaintId(rs.getString("complaint_id"));
                c.setTitle(rs.getString("title")); c.setLocation(rs.getString("location"));
                c.setComplaintType(rs.getString("complaint_type")); c.setStatus(rs.getString("status"));
                c.setPriority(rs.getString("priority")); c.setCategory(rs.getString("category"));
                c.setAssignedHead(rs.getString("assigned_head")); c.setDescription(rs.getString("description"));
                c.setPredictedDays(rs.getInt("predicted_days")); c.setCreatedAt(rs.getString("created_at"));
                c.setSocietyName(rs.getString("society_name")); c.setAssignedWorker(rs.getString("assigned_worker"));
                list.add(c);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private void loadComplaintCombo(ComboBox<String> box) {
        User u = SessionManager.getInstance().getCurrentUser();
        if (u == null) return;
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT complaint_id, title FROM complaints WHERE user_id=? ORDER BY created_at DESC")) {
            ps.setInt(1, u.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) box.getItems().add(rs.getString("complaint_id") + " — " + rs.getString("title"));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private String extractId(String val) {
        if (val == null || !val.contains(" — ")) return null;
        return val.split(" — ")[0].trim();
    }
}
