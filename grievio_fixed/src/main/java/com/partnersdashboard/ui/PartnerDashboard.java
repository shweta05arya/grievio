package com.partnersdashboard.ui;

import com.partnersdashboard.config.AppConfig;
import com.partnersdashboard.dao.FeedbackDAO;
import com.partnersdashboard.dao.PartnerDAO;
import com.partnersdashboard.dao.ResidentDAO;
import com.partnersdashboard.model.Complaint;
import com.partnersdashboard.model.Feedback;
import com.partnersdashboard.model.Partner;
import com.partnersdashboard.model.Resident;
import com.partnersdashboard.service.ComplaintService;
import com.partnersdashboard.service.OtpService;
import com.partnersdashboard.ui.common.UIStyles;
import com.partnersdashboard.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Partner-only dashboard with task lifecycle, proof upload, OTP verification and profile/review panels. */
public class PartnerDashboard extends Component {

    private static final int W = 1200;
    private static final int H = 720;

    private BorderPane root;
    private Partner partner;
    private final Stage stage;
    private String currentView = "DASHBOARD";
    private String pendingProofPath;

    // Demo-only local workflow state so buttons work without database updates.
    private final Map<Integer, String> demoStatusOverrides = new HashMap<>();
    private final Map<Integer, String> demoOtpCodes = new HashMap<>();
    private final Map<Integer, String> demoProofPaths = new HashMap<>();
    private List<Complaint> complaintList = new ArrayList<>();

    private final ComplaintService complaintService = new ComplaintService();
    private final OtpService otpService = new OtpService();
    private final FeedbackDAO feedbackDAO = new FeedbackDAO();
    private final ResidentDAO residentDAO = new ResidentDAO();
    private final PartnerDAO partnerDAO = new PartnerDAO();
    private Object inProgressPanel;


    public PartnerDashboard(Stage stage) {
        this.stage = stage;
        Scene scene = createScene();
        stage.setTitle("Partner Dashboard");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }

    public Scene createScene() {
        partner = SessionManager.getInstance().getPartner();
        if (partner == null) {
            partner = partnerDAO.getByAccountId(AppConfig.DEMO_PARTNER_ACCOUNT_ID);
        }

        root = new BorderPane();
        root.setLeft(buildSidebar());
        root.setCenter(buildOverviewPanel());
        root.setStyle("-fx-background-color: " + UIStyles.COLOR_BG + ";");
        return new Scene(root, W, H);
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(4);
        sidebar.setPrefWidth(230);
        sidebar.setPadding(new Insets(0, 12, 20, 12));
        sidebar.setStyle(UIStyles.STYLE_SIDEBAR);

        VBox header = new VBox(4);
        header.setPadding(new Insets(28, 10, 20, 10));
        Label logo = new Label("🏢 Gravio");
        logo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label roleTag = new Label("Partner Portal");
        roleTag.setStyle("-fx-font-size: 11px; -fx-text-fill: " + UIStyles.COLOR_GOLD + ";");
        header.getChildren().addAll(logo, roleTag);

        VBox userInfo = new VBox(3);
        userInfo.setPadding(new Insets(8, 10, 8, 10));
        Label nameLbl = new Label(partner != null ? partner.getFullName() : "Demo Partner");
        nameLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label skillLbl = new Label(partner != null ? "🔧 " + safe(partner.getSkill()) : "🔧 Worker");
        skillLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: " + UIStyles.COLOR_GOLD + ";");
        String avail = partner != null ? safe(partner.getAvailability()) : "AVAILABLE";
        Label availBadge = new Label("● " + avail.replace("_", " "));
        String availColor = "AVAILABLE".equalsIgnoreCase(avail) ? UIStyles.COLOR_SUCCESS
                : "BUSY".equalsIgnoreCase(avail) ? UIStyles.COLOR_WARNING : "#9CA3AF";
        availBadge.setStyle("-fx-font-size: 11px; -fx-text-fill: " + availColor + ";");
        userInfo.getChildren().addAll(nameLbl, skillLbl, availBadge);

        Button[] navBtns = {
                UIHelper.sidebarButton("📊  Dashboard", true),
                UIHelper.sidebarButton("📋  All Tasks", false),
                UIHelper.sidebarButton("⏳  Pending Tasks", false),
                UIHelper.sidebarButton("⚡  In Progress", false),
                UIHelper.sidebarButton("✅  Completed Tasks", false),
                UIHelper.sidebarButton("⭐  My Ratings", false),
                UIHelper.sidebarButton("👤  My Profile", false)
        };

        navBtns[0].setOnAction(e -> { currentView = "DASHBOARD"; switchContent(buildOverviewPanel(), navBtns, 0); });
        navBtns[1].setOnAction(e -> { currentView = "ALL"; switchContent(buildTasksPanel("ALL"), navBtns, 1); });
        navBtns[2].setOnAction(e -> { currentView = "PENDING"; switchContent(buildTasksPanel("PENDING"), navBtns, 2); });
        navBtns[3].setOnAction(e -> { currentView = "IN_PROGRESS"; switchContent(buildTasksPanel("IN_PROGRESS"), navBtns, 3); });
        navBtns[4].setOnAction(e -> { currentView = "COMPLETED"; switchContent(buildTasksPanel("COMPLETED"), navBtns, 4); });
        navBtns[5].setOnAction(e -> { currentView = "RATINGS"; switchContent(buildRatingsPanel(), navBtns, 5); });
        navBtns[6].setOnAction(e -> { currentView = "PROFILE"; switchContent(buildProfilePanel(), navBtns, 6); });

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        Button refreshBtn = UIHelper.sidebarButton("🔄  Refresh", false);
        refreshBtn.setOnAction(e -> refreshCurrentView());

        Button logoutBtn = UIHelper.sidebarButton("🚪  Reset Demo Session", false);
        logoutBtn.setStyle(logoutBtn.getStyle() + " -fx-text-fill: #ff7979;");
        logoutBtn.setOnAction(e -> logout());

        sidebar.getChildren().addAll(header, UIHelper.separator(), userInfo, UIHelper.separator());
        sidebar.getChildren().addAll(navBtns);
        sidebar.getChildren().addAll(spacer, refreshBtn, logoutBtn);
        return sidebar;
    }

    private void switchContent(Node content, Button[] btns, int active) {
        for (int i = 0; i < btns.length; i++) {
            btns[i].setStyle(i == active
                    ? "-fx-background-color: rgba(255,255,255,0.15); -fx-text-fill: white; " +
                    "-fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-alignment: CENTER_LEFT; -fx-padding: 12 20;"
                    : "-fx-background-color: transparent; -fx-text-fill: " + UIStyles.COLOR_TEXT_LIGHT + "; " +
                    "-fx-font-size: 13px; -fx-background-radius: 8; -fx-cursor: hand; -fx-alignment: CENTER_LEFT; -fx-padding: 12 20;");
        }
        root.setCenter(content);
    }

    private ScrollPane buildOverviewPanel() {
        refreshPartner();

        VBox panel = new VBox(24);
        panel.setPadding(new Insets(30));

        Label title = UIHelper.headingLabel("Partner Dashboard 🔧");
        Label subtitle = UIHelper.subtextLabel("Welcome, " + (partner != null ? partner.getFullName() : "Partner") +
                " | Skill: " + (partner != null ? safe(partner.getSkill()) : "—"));

        List<Complaint> all = getMyComplaints("ALL");
        long pending = all.stream().filter(c -> List.of("ASSIGNED", "ACCEPTED", "ON_THE_WAY").contains(c.getStatus())).count();
        long inProgress = all.stream().filter(c -> List.of("IN_PROGRESS", "VERIFICATION_PENDING").contains(c.getStatus())).count();
        long completed = all.stream().filter(c -> "COMPLETED".equals(c.getStatus())).count();
        double rating = partner != null ? partner.getAvgRating() : 0.0;

        HBox metrics = new HBox(16);
        metrics.getChildren().addAll(
                UIHelper.metricCard(String.valueOf(all.size()), "Total Tasks", UIStyles.COLOR_BLUE),
                UIHelper.metricCard(String.valueOf(pending), "Pending", UIStyles.COLOR_WARNING),
                UIHelper.metricCard(String.valueOf(inProgress), "In Progress", UIStyles.COLOR_INFO),
                UIHelper.metricCard(String.valueOf(completed), "Completed", UIStyles.COLOR_SUCCESS),
                UIHelper.metricCard(String.format("%.1f★", rating), "Avg Rating", UIStyles.COLOR_GOLD)
        );

        Label todayTitle = new Label("Recently Assigned Tasks");
        todayTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + UIStyles.COLOR_TEXT_DARK + ";");
        VBox taskCards = new VBox(12);
        all.stream().filter(c -> !"COMPLETED".equals(c.getStatus())).limit(3).forEach(c -> taskCards.getChildren().add(buildTaskCard(c)));
        if (taskCards.getChildren().isEmpty()) {
            taskCards.getChildren().add(UIHelper.subtextLabel("No active tasks assigned."));
        }

        panel.getChildren().addAll(title, subtitle, metrics, UIHelper.separator(), todayTitle, taskCards);
        return scroll(panel);
    }

    private ScrollPane buildTasksPanel(String filter) {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(30));

        String titleText = switch (filter) {
            case "PENDING" -> "Pending Tasks ⏳";
            case "IN_PROGRESS" -> "In Progress ⚡";
            case "COMPLETED" -> "Completed Tasks ✅";
            default -> "All Tasks 📋";
        };
        panel.getChildren().add(UIHelper.headingLabel(titleText));

        List<Complaint> complaints = getMyComplaints(filter);
        if (complaints.isEmpty()) {
            panel.getChildren().add(UIHelper.subtextLabel("No tasks in this category."));
        } else {
            complaints.forEach(c -> panel.getChildren().add(buildTaskCard(c)));
        }
        return scroll(panel);
    }

    private VBox buildTaskCard(Complaint c) {
        VBox card = UIHelper.card(20);
        card.setMaxWidth(860);

        HBox headerRow = new HBox(10);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        Label idLbl = new Label("#" + c.getId());
        idLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + UIStyles.COLOR_BLUE + ";");
        Label titleLbl = new Label(safe(c.getTitle()));
        titleLbl.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + UIStyles.COLOR_TEXT_DARK + ";");
        titleLbl.setWrapText(true);
        HBox.setHgrow(titleLbl, Priority.ALWAYS);
        headerRow.getChildren().addAll(idLbl, titleLbl, UIHelper.priorityBadge(c.getPriority()), UIHelper.statusBadge(getEffectiveStatus(c)));

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(6);
        addGridRow(grid, 0, "Resident", safe(c.getResidentName()));
        addGridRow(grid, 1, "Email", safe(c.getResidentEmail()));
        addGridRow(grid, 2, "Society", safe(c.getSocietyName()));
        addGridRow(grid, 3, "Address", "Block " + safe(c.getResidentBlock()) + ", Flat " + safe(c.getResidentFlat()));
        addGridRow(grid, 4, "Category", safe(c.getCategory()));
        addGridRow(grid, 5, "Visit Slot", c.getVisitSlot() != null ? c.getVisitSlot() : "Not set");
        addGridRow(grid, 6, "Partner", c.getPartnerName() != null ? c.getPartnerName() : "Assigned to you");

        Label descLbl = new Label(safe(c.getDescription()));
        descLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: " + UIStyles.COLOR_TEXT_MID + ";");
        descLbl.setWrapText(true);

        HBox actions = buildTaskActions(c);
        card.getChildren().addAll(headerRow, UIHelper.separator(), grid, descLbl, actions);
        return card;
    }

    private HBox buildTaskActions(Complaint c) {
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);
        String status = getEffectiveStatus(c);

        switch (status) {
            case "ASSIGNED" -> {
                Button acceptBtn = UIHelper.successButton("✔ Accept Task");
                acceptBtn.setOnAction(e -> {
                    setDemoStatus(c, "ACCEPTED");
                    UIHelper.showSuccess("Task Accepted", "Complaint #" + c.getId() + " is now accepted.");
                    refreshCurrentView();
                });
                actions.getChildren().add(acceptBtn);
            }
            case "ACCEPTED" -> {
                Button onWayBtn = UIHelper.primaryButton("🚗 Mark On the Way");
                onWayBtn.setMaxWidth(220);
                onWayBtn.setOnAction(e -> {
                    setDemoStatus(c, "ON_THE_WAY");
                    UIHelper.showSuccess("Status Updated", "Complaint #" + c.getId() + " marked On the Way.");
                    refreshCurrentView();
                });
                actions.getChildren().add(onWayBtn);
            }
            case "ON_THE_WAY" -> {
                Button inProgressBtn = UIHelper.primaryButton("⚡ Mark In Progress");
                inProgressBtn.setMaxWidth(220);
                inProgressBtn.setOnAction(e -> {
                    setDemoStatus(c, "IN_PROGRESS");
                    UIHelper.showSuccess("Status Updated", "Complaint #" + c.getId() + " marked In Progress.");
                    refreshCurrentView();
                });
                actions.getChildren().add(inProgressBtn);
            }
            case "IN_PROGRESS" -> {
                Button completeBtn = new Button("🏁 Complete Work");
                completeBtn.setStyle("-fx-background-color: " + UIStyles.COLOR_GOLD +
                        "; -fx-text-fill: #1B2235; -fx-font-weight: bold; -fx-background-radius: 8; " +
                        "-fx-cursor: hand; -fx-font-size: 14px; -fx-padding: 8 20;");
                completeBtn.setOnAction(e -> initiateCompletionFlow(c));
                actions.getChildren().add(completeBtn);
            }
            case "VERIFICATION_PENDING" -> {
                Label pending = new Label("⏳ Waiting for OTP confirmation");
                pending.setStyle("-fx-font-size: 12px; -fx-text-fill: " + UIStyles.COLOR_WARNING + ";");
                Button enterOtpBtn = UIHelper.primaryButton("🔑 Enter OTP");
                enterOtpBtn.setMaxWidth(160);
                int accountId = 0;
                enterOtpBtn.setOnAction(e -> showOtpDialog(c, accountId, demoProofPaths.get(c.getId())));
                actions.getChildren().addAll(pending, enterOtpBtn);
            }
            case "COMPLETED" -> {
                Label done = new Label("✅ Task Completed Successfully");
                done.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + UIStyles.COLOR_SUCCESS + ";");
                actions.getChildren().add(done);
            }
            default -> actions.getChildren().add(UIHelper.subtextLabel("No actions available for current status."));
        }
        return actions;
    }

    private void initiateCompletionFlow(Complaint c) {
        if (!UIHelper.confirm("Complete Work",
                "Confirm that physical work is done for Complaint #" + c.getId() + "?\n\nThis will ask for proof and then send an OTP to the resident.")) {
            return;
        }

        Dialog<ButtonType> proofDialog = new Dialog<>();
        proofDialog.setTitle("Step 1 of 2 – Proof Upload");
        proofDialog.setHeaderText("Complaint #" + c.getId() + " – Upload completion proof");

        VBox content = new VBox(14);
        content.setPadding(new Insets(20));

        Label info = new Label("Upload a photo proof, or use the camera placeholder, before sending the OTP.");
        info.setWrapText(true);
        info.setStyle("-fx-font-size: 13px; -fx-text-fill: " + UIStyles.COLOR_TEXT_MID + ";");

        Label filePathLbl = new Label("No proof selected yet");
        filePathLbl.setWrapText(true);
        filePathLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: " + UIStyles.COLOR_TEXT_MID + "; -fx-background-color: " + UIStyles.COLOR_BG + "; -fx-padding: 8 12; -fx-background-radius: 6;");

        Button uploadBtn = UIHelper.primaryButton("📁 Upload Proof Image");
        uploadBtn.setOnAction(e -> {
            String path = chooseAndCopyProofFile();
            if (path != null) {
                pendingProofPath = path;
                filePathLbl.setText("✅ Saved proof: " + path);
                filePathLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: " + UIStyles.COLOR_SUCCESS + "; -fx-background-color: #F0FFF4; -fx-padding: 8 12; -fx-background-radius: 6;");
            }
        });

        Button cameraBtn = new Button("📷 Camera Placeholder");
        cameraBtn.setStyle(UIStyles.STYLE_BTN_SUCCESS);
        cameraBtn.setOnAction(e -> {
            String placeholder = createCameraPlaceholderProof(c.getId());
            pendingProofPath = placeholder;
            filePathLbl.setText("✅ Camera placeholder created: " + placeholder);
            filePathLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: " + UIStyles.COLOR_SUCCESS + "; -fx-background-color: #F0FFF4; -fx-padding: 8 12; -fx-background-radius: 6;");
        });

        Label hint = new Label("You can continue without proof, but proof is recommended for a better completion record.");
        hint.setWrapText(true);
        hint.setStyle("-fx-font-size: 11px; -fx-text-fill: " + UIStyles.COLOR_TEXT_LIGHT + ";");

        content.getChildren().addAll(info, uploadBtn, cameraBtn, filePathLbl, hint);
        proofDialog.getDialogPane().setContent(content);
        proofDialog.getDialogPane().getButtonTypes().addAll(new ButtonType("Next: Send OTP →", ButtonBar.ButtonData.OK_DONE), ButtonType.CANCEL);

        proofDialog.showAndWait().ifPresent(result -> {
            if (result.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                sendOtpAndShowDialog(c);
            }
        });
    }

    private void sendOtpAndShowDialog(Complaint c) {

        // 1. Generate OTP
        String otp = String.valueOf((int)(100000 + Math.random() * 900000));

        // 2. Show OTP popup (for demo)
        JOptionPane.showMessageDialog(
                this,
                "Demo OTP: " + otp,
                "OTP Generated",
                JOptionPane.INFORMATION_MESSAGE
        );

        // 3. Ask user to enter OTP
        String enteredOtp = JOptionPane.showInputDialog(
                this,
                "Enter OTP to complete the complaint:"
        );

        if (enteredOtp == null) return;

        // 4. Verify OTP
        if (enteredOtp.equals(otp)) {

            // ✅ Mark complaint as completed (UI only)
            c.setStatus("COMPLETED");

            JOptionPane.showMessageDialog(
                    this,
                    "Complaint marked as COMPLETED ✅",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );

            // 5. Refresh UI (VERY IMPORTANT)
            refreshDashboard();

        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Invalid OTP ❌",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void refreshDashboard() {
        DefaultListCellRenderer assignedPanel = null;
        assignedPanel.removeAll();
        DefaultListCellRenderer inProgressPanel = null;
        inProgressPanel.removeAll();
        DefaultListCellRenderer completedPanel = null;
        completedPanel.removeAll();

        for (Complaint c : complaintList) {
            if ("ASSIGNED".equals(c.getStatus()) || "ACCEPTED".equals(c.getStatus())) {
                assignedPanel.add(createComplaintCard(c));
            } else if ("ON_THE_WAY".equals(c.getStatus()) || "IN_PROGRESS".equals(c.getStatus())) {
                inProgressPanel.add(createComplaintCard(c));
            } else if ("COMPLETED".equals(c.getStatus())) {
                completedPanel.add(createComplaintCard(c));
            }
        }

        assignedPanel.revalidate();
        assignedPanel.repaint();

        inProgressPanel.revalidate();
        inProgressPanel.repaint();

        completedPanel.revalidate();
        completedPanel.repaint();
    }

    private Component createComplaintCard(Complaint c) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(10, 10));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 90, 120), 1, true),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        card.setBackground(new Color(20, 30, 48));

        JPanel topPanel = new JPanel(new GridLayout(0, 1, 4, 4));
        topPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(c.getTitle() != null ? c.getTitle() : "Complaint");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);

        JLabel idLabel = new JLabel("Complaint ID: " + c.getId());
        idLabel.setForeground(new Color(180, 200, 230));

        JLabel statusLabel = new JLabel("Status: " + c.getStatus());
        statusLabel.setForeground(new Color(120, 220, 170));

        topPanel.add(titleLabel);
        topPanel.add(idLabel);
        topPanel.add(statusLabel);

        // Optional extra fields if available in your Complaint class
        try {
            if (c.getDescription() != null && !c.getDescription().isEmpty()) {
                JLabel descLabel = new JLabel("<html><body style='width:260px'>" + c.getDescription() + "</body></html>");
                descLabel.setForeground(new Color(210, 220, 235));
                topPanel.add(descLabel);
            }
        } catch (Exception ignored) {}

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttonPanel.setOpaque(false);

        JButton acceptBtn = new JButton("Accept Task");
        JButton onWayBtn = new JButton("Mark On The Way");
        JButton progressBtn = new JButton("Mark In Progress");
        JButton completeBtn = new JButton("Complete Work");

        if ("ASSIGNED".equals(c.getStatus())) {
            buttonPanel.add(acceptBtn);
        } else if ("ACCEPTED".equals(c.getStatus())) {
            buttonPanel.add(onWayBtn);
        } else if ("ON_THE_WAY".equals(c.getStatus())) {
            buttonPanel.add(progressBtn);
        } else if ("IN_PROGRESS".equals(c.getStatus())) {
            buttonPanel.add(completeBtn);
        } else if ("COMPLETED".equals(c.getStatus())) {
            JLabel doneLabel = new JLabel("Completed ✅");
            doneLabel.setForeground(new Color(120, 220, 170));
            doneLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            buttonPanel.add(doneLabel);
        }

        acceptBtn.addActionListener(e -> {
            c.setStatus("ACCEPTED");
            refreshDashboard();
        });

        onWayBtn.addActionListener(e -> {
            c.setStatus("ON_THE_WAY");
            refreshDashboard();
        });

        progressBtn.addActionListener(e -> {
            c.setStatus("IN_PROGRESS");
            refreshDashboard();
        });

        completeBtn.addActionListener(e -> {
            sendOtpAndShowDialog(c);
        });

        card.add(topPanel, BorderLayout.CENTER);
        card.add(buttonPanel, BorderLayout.SOUTH);

        return card;
    }

    private void sendOtpAndShowDialog(Complaint c, int accountId) {
        Resident resident = residentDAO.getById(c.getResidentId());
        if (resident == null) {
            UIHelper.showError("Error", "Cannot find resident details. Cannot send OTP.");
            return;
        }

        boolean sent = otpService.generateAndSendOtp(c, resident);
        complaintService.markVerificationPending(c.getId(), accountId);

        if (sent) {
            UIHelper.showSuccess("OTP Sent ✉",
                    "A 6-digit OTP has been generated and emailed to:\n" + resident.getEmail() +
                            "\n\nAsk the resident to share the OTP to confirm work completion.");
        } else {
            UIHelper.showAlert("OTP Generated",
                    "OTP was saved to the database, but email sending failed.\nCheck SMTP settings in AppConfig.java.",
                    Alert.AlertType.WARNING);
        }

        refreshCurrentView();
        showOtpDialog(c, accountId, pendingProofPath);
    }

    private void showOtpDialog(Complaint c, int accountId, String proofPath) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Enter Verification OTP");
        dialog.setHeaderText("Complaint #" + c.getId() + " – Resident verification");

        VBox content = new VBox(14);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER_LEFT);

        Label info = new Label("Enter the 6-digit OTP received by the resident.\nOnly after correct OTP will the task be marked completed.");
        info.setWrapText(true);
        info.setStyle("-fx-font-size: 13px; -fx-text-fill: " + UIStyles.COLOR_TEXT_MID + ";");

        TextField otpField = new TextField();
        otpField.setPromptText("Enter 6-digit OTP");
        otpField.setStyle(UIStyles.STYLE_INPUT + " -fx-font-size: 22px; -fx-font-weight: bold; -fx-alignment: center;");
        otpField.setMaxWidth(220);
        otpField.textProperty().addListener((obs, old, val) -> {
            if (!val.matches("\\d*") || val.length() > 6) otpField.setText(old);
        });

        Label proofLbl = new Label(proofPath != null ? "Proof path: " + proofPath : "No proof file attached");
        proofLbl.setWrapText(true);
        proofLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: " + UIStyles.COLOR_TEXT_LIGHT + ";");

        Label statusLbl = new Label();
        statusLbl.setWrapText(true);

        content.getChildren().addAll(info, otpField, proofLbl, statusLbl);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText("Verify & Complete");
        okButton.setStyle(UIStyles.STYLE_BTN_PRIMARY);

        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            String entered = otpField.getText().trim();
            if (entered.length() != 6) {
                statusLbl.setStyle("-fx-text-fill: " + UIStyles.COLOR_DANGER + ";");
                statusLbl.setText("❌ Please enter a valid 6-digit OTP.");
                event.consume();
                return;
            }

            String expectedOtp = demoOtpCodes.get(c.getId());
            boolean verified = entered.equals(expectedOtp);
            if (verified) {
                if (proofPath != null) {
                    demoProofPaths.put(c.getId(), proofPath);
                }
                setDemoStatus(c, "COMPLETED");
                demoOtpCodes.remove(c.getId());
                statusLbl.setStyle("-fx-text-fill: " + UIStyles.COLOR_SUCCESS + ";");
                statusLbl.setText("✅ OTP verified. Complaint marked as completed.");
            } else {
                statusLbl.setStyle("-fx-text-fill: " + UIStyles.COLOR_DANGER + ";");
                statusLbl.setText("❌ Incorrect or expired OTP. Please try again.");
                event.consume();
            }
        });

        dialog.showAndWait();
        refreshCurrentView();
    }

    private ScrollPane buildRatingsPanel() {
        refreshPartner();
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(30));
        panel.getChildren().add(UIHelper.headingLabel("My Ratings & Feedback ⭐"));

        VBox summaryCard = UIHelper.card(20);
        summaryCard.setMaxWidth(500);
        double avg = partner != null ? partner.getAvgRating() : 0.0;
        Label avgLbl = new Label(String.format("%.1f / 5.0", avg));
        avgLbl.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: " + UIStyles.COLOR_GOLD + ";");
        Label avgSub = new Label("Average Rating  |  " + (partner != null ? partner.getCompletedTasks() : 0) + " completed tasks");
        avgSub.setStyle("-fx-font-size: 13px; -fx-text-fill: " + UIStyles.COLOR_TEXT_MID + ";");
        summaryCard.getChildren().addAll(avgLbl, avgSub);
        panel.getChildren().addAll(summaryCard, UIHelper.separator());

        List<Feedback> feedbacks = partner != null ? feedbackDAO.getByPartnerId(partner.getId()) : List.of();
        if (feedbacks.isEmpty()) {
            panel.getChildren().add(UIHelper.subtextLabel("No feedback received yet."));
        } else {
            for (Feedback fb : feedbacks) {
                VBox fbCard = UIHelper.card(16);
                fbCard.setMaxWidth(700);
                Label stars = new Label(fb.getStarDisplay());
                stars.setStyle("-fx-font-size: 18px; -fx-text-fill: " + UIStyles.COLOR_GOLD + ";");
                Label fromLbl = new Label("from " + safe(fb.getResidentName()));
                fromLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + UIStyles.COLOR_TEXT_DARK + ";");
                Label complaintRef = new Label("Complaint: " + safe(fb.getComplaintTitle()));
                complaintRef.setStyle("-fx-font-size: 12px; -fx-text-fill: " + UIStyles.COLOR_TEXT_MID + ";");
                Label comment = new Label(fb.getComment() != null && !fb.getComment().isBlank() ? '"' + fb.getComment() + '"' : "No comment.");
                comment.setStyle("-fx-font-size: 13px; -fx-text-fill: " + UIStyles.COLOR_TEXT_DARK + "; -fx-font-style: italic;");
                comment.setWrapText(true);
                fbCard.getChildren().addAll(stars, fromLbl, complaintRef, comment);
                panel.getChildren().add(fbCard);
            }
        }
        return scroll(panel);
    }

    private ScrollPane buildProfilePanel() {
        refreshPartner();
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(30));
        panel.getChildren().add(UIHelper.headingLabel("My Profile 👤"));

        VBox card = UIHelper.card(28);
        card.setMaxWidth(520);
        if (partner != null) {
            card.getChildren().addAll(
                    profileRow("Name", partner.getFullName()),
                    profileRow("Email", partner.getEmail()),
                    profileRow("Phone", partner.getPhone()),
                    profileRow("Skill", partner.getSkill()),
                    profileRow("Society", partner.getSocietyName()),
                    profileRow("Availability", partner.getAvailability()),
                    profileRow("Total Tasks", String.valueOf(partner.getTotalTasks())),
                    profileRow("Completed", String.valueOf(partner.getCompletedTasks())),
                    profileRow("Rating", String.format("%.2f / 5.00 ★", partner.getAvgRating())),
                    profileRow("Completion %", String.format("%.0f%%", partner.getCompletionRate()))
            );
        } else {
            card.getChildren().add(UIHelper.subtextLabel("Partner record could not be loaded from the database."));
        }
        panel.getChildren().add(card);
        return scroll(panel);
    }

    private HBox profileRow(String label, String value) {
        HBox row = new HBox(10);
        row.setPadding(new Insets(6, 0, 6, 0));
        Label lbl = new Label(label + ":");
        lbl.setMinWidth(110);
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: " + UIStyles.COLOR_TEXT_MID + ";");
        Label val = new Label(value != null ? value : "—");
        val.setStyle("-fx-text-fill: " + UIStyles.COLOR_TEXT_DARK + ";");
        row.getChildren().addAll(lbl, val);
        return row;
    }

    private List<Complaint> getMyComplaints(String filter) {
        if (partner == null) return List.of();
        List<Complaint> all = complaintService.getComplaintsForPartner(partner.getId());
        all.forEach(c -> c.setStatus(getEffectiveStatus(c)));
        return switch (filter) {
            case "PENDING" -> all.stream().filter(c -> List.of("ASSIGNED", "ACCEPTED", "ON_THE_WAY").contains(c.getStatus())).collect(Collectors.toList());
            case "IN_PROGRESS" -> all.stream().filter(c -> List.of("IN_PROGRESS", "VERIFICATION_PENDING").contains(c.getStatus())).collect(Collectors.toList());
            case "COMPLETED" -> all.stream().filter(c -> "COMPLETED".equals(c.getStatus())).collect(Collectors.toList());
            default -> all;
        };
    }


    private String getEffectiveStatus(Complaint complaint) {
        return demoStatusOverrides.getOrDefault(complaint.getId(), safe(complaint.getStatus()));
    }

    private void setDemoStatus(Complaint complaint, String newStatus) {
        demoStatusOverrides.put(complaint.getId(), newStatus);
        complaint.setStatus(newStatus);
    }

    private void addGridRow(GridPane grid, int row, String label, String value) {
        Label lbl = new Label(label + ":");
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: " + UIStyles.COLOR_TEXT_MID + "; -fx-font-size: 12px;");
        Label val = new Label(value != null ? value : "—");
        val.setStyle("-fx-font-size: 12px; -fx-text-fill: " + UIStyles.COLOR_TEXT_DARK + ";");
        val.setWrapText(true);
        grid.add(lbl, 0, row);
        grid.add(val, 1, row);
    }

    private ScrollPane scroll(VBox panel) {
        ScrollPane sp = new ScrollPane(panel);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: " + UIStyles.COLOR_BG + "; -fx-background: " + UIStyles.COLOR_BG + ";");
        return sp;
    }

    private String chooseAndCopyProofFile() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Proof Image");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        File file = fc.showOpenDialog(stage);
        if (file == null) return null;

        try {
            Path dir = Path.of(AppConfig.PROOF_UPLOAD_DIR);
            Files.createDirectories(dir);
            String name = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + "_" + file.getName().replaceAll("\\s+", "_");
            Path dest = dir.resolve(name);
            Files.copy(file.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
            return dest.toString();
        } catch (IOException ex) {
            UIHelper.showError("Upload Failed", "Could not save proof file: " + ex.getMessage());
            return null;
        }
    }

    private String createCameraPlaceholderProof(int complaintId) {
        try {
            Path dir = Path.of(AppConfig.PROOF_UPLOAD_DIR);
            Files.createDirectories(dir);
            Path dest = dir.resolve("camera_placeholder_complaint_" + complaintId + "_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt");
            String body = "Camera placeholder proof\nComplaint ID: " + complaintId +
                    "\nCreated at: " + LocalDateTime.now() +
                    "\nReplace this placeholder with real webcam capture integration later.";
            Files.writeString(dest, body);
            return dest.toString();
        } catch (IOException ex) {
            UIHelper.showError("Camera Placeholder Failed", ex.getMessage());
            return null;
        }
    }

    private void refreshPartner() {
        if (partner != null) {
            Partner latest = partnerDAO.getById(partner.getId());
            if (latest != null) {
                partner = latest;
                SessionManager.getInstance().setPartner(latest);
            }
        }
    }

    private void refreshCurrentView() {
        switch (currentView) {
            case "ALL" -> root.setCenter(buildTasksPanel("ALL"));
            case "PENDING" -> root.setCenter(buildTasksPanel("PENDING"));
            case "IN_PROGRESS" -> root.setCenter(buildTasksPanel("IN_PROGRESS"));
            case "COMPLETED" -> root.setCenter(buildTasksPanel("COMPLETED"));
            case "RATINGS" -> root.setCenter(buildRatingsPanel());
            case "PROFILE" -> root.setCenter(buildProfilePanel());
            default -> root.setCenter(buildOverviewPanel());
        }
    }

    @FXML
    private void handleLogout() {
        logout();
    }

    private void logout() {
        SessionManager.getInstance().clear();
        new PartnerDashboard(stage);
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }
}
