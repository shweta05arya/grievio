package com.partnersdashboard.ui.common;

/**
 * UIStyles - Central style constants for Gravio JavaFX application.
 * All colors, fonts, and reusable inline-style strings live here.
 * This ensures visual consistency across all screens.
 */
public final class UIStyles {

    // ── Brand Colors ─────────────────────────────────────────────────────────
    public static final String COLOR_NAVY        = "#0B1533";
    public static final String COLOR_NAVY_LIGHT  = "#101B46";
    public static final String COLOR_BLUE        = "#2F6BFF";
    public static final String COLOR_BLUE_DARK   = "#1E5EFF";
    public static final String COLOR_GOLD        = "#E4B84C";
    public static final String COLOR_WHITE       = "#FFFFFF";
    public static final String COLOR_BG          = "#F4F6FB";
    public static final String COLOR_TEXT_DARK   = "#1B2235";
    public static final String COLOR_TEXT_MID    = "#8C94A6";
    public static final String COLOR_TEXT_LIGHT  = "#B8C4D9";
    public static final String COLOR_BORDER      = "#E2E8F0";
    public static final String COLOR_SUCCESS     = "#22C55E";
    public static final String COLOR_WARNING     = "#F59E0B";
    public static final String COLOR_DANGER      = "#EF4444";
    public static final String COLOR_INFO        = "#3B82F6";

    // ── Status Badge Colors ──────────────────────────────────────────────────
    public static final String BADGE_SUBMITTED    = "#3B82F6";
    public static final String BADGE_ASSIGNED     = "#8B5CF6";
    public static final String BADGE_ACCEPTED     = "#06B6D4";
    public static final String BADGE_ON_THE_WAY   = "#F59E0B";
    public static final String BADGE_IN_PROGRESS  = "#F97316";
    public static final String BADGE_VERIFY_PEND  = "#EF4444";
    public static final String BADGE_COMPLETED    = "#22C55E";
    public static final String BADGE_REJECTED     = "#6B7280";

    // ── Common Inline Styles ─────────────────────────────────────────────────
    public static final String STYLE_CARD = String.format(
            "-fx-background-color: %s; -fx-background-radius: 12; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0, 0, 4);",
            COLOR_WHITE
    );

    public static final String STYLE_SIDEBAR = String.format(
            "-fx-background-color: %s;", COLOR_NAVY
    );

    public static final String STYLE_BTN_PRIMARY = String.format(
            "-fx-background-color: linear-gradient(to right, %s, %s); " +
                    "-fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; " +
                    "-fx-cursor: hand; -fx-font-size: 14px;",
            COLOR_BLUE, COLOR_BLUE_DARK
    );

    public static final String STYLE_BTN_DANGER = String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
                    "-fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 14px;",
            COLOR_DANGER
    );

    public static final String STYLE_BTN_SUCCESS = String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
                    "-fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 14px;",
            COLOR_SUCCESS
    );

    public static final String STYLE_INPUT = String.format(
            "-fx-background-color: %s; -fx-border-color: %s; -fx-border-radius: 8; " +
                    "-fx-background-radius: 8; -fx-padding: 10 14; -fx-font-size: 13px;",
            COLOR_BG, COLOR_BORDER
    );

    public static final String STYLE_LABEL_HEADING = String.format(
            "-fx-text-fill: %s; -fx-font-size: 22px; -fx-font-weight: bold;",
            COLOR_TEXT_DARK
    );

    public static final String STYLE_LABEL_SUBTEXT = String.format(
            "-fx-text-fill: %s; -fx-font-size: 13px;",
            COLOR_TEXT_MID
    );

    /** Returns colored badge style for a given complaint status string. */
    public static String badgeStyle(String status) {
        String color = switch (status != null ? status : "") {
            case "SUBMITTED"            -> BADGE_SUBMITTED;
            case "ASSIGNED"             -> BADGE_ASSIGNED;
            case "ACCEPTED"             -> BADGE_ACCEPTED;
            case "ON_THE_WAY"           -> BADGE_ON_THE_WAY;
            case "IN_PROGRESS"          -> BADGE_IN_PROGRESS;
            case "VERIFICATION_PENDING" -> BADGE_VERIFY_PEND;
            case "COMPLETED"            -> BADGE_COMPLETED;
            case "REJECTED"             -> BADGE_REJECTED;
            default                     -> "#9CA3AF";
        };
        return String.format(
                "-fx-background-color: %s; -fx-text-fill: white; -fx-background-radius: 20; " +
                        "-fx-padding: 3 10; -fx-font-size: 11px; -fx-font-weight: bold;",
                color);
    }

    /** Returns display-friendly status label. */
    public static String statusLabel(String status) {
        return switch (status != null ? status : "") {
            case "SUBMITTED"            -> "Submitted";
            case "ASSIGNED"             -> "Assigned";
            case "ACCEPTED"             -> "Accepted";
            case "ON_THE_WAY"           -> "On the Way";
            case "IN_PROGRESS"          -> "In Progress";
            case "VERIFICATION_PENDING" -> "Verification Pending";
            case "COMPLETED"            -> "Completed";
            case "REJECTED"             -> "Rejected";
            default                     -> status;
        };
    }

    private UIStyles() {}
}