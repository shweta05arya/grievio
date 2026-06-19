package com.grievio.util;

public class AIRouter {

    private static final String[] PUBLIC_KEYWORDS = {
        "road","street","light","garbage","water supply","sewage","park",
        "traffic","electricity","public","municipal","footpath","drainage",
        "pothole","bus","infrastructure","transformer","flood","storm"
    };

    private static final String[] SOCIETY_KEYWORDS = {
        "lift","elevator","security","gate","parking","intercom","roof",
        "terrace","corridor","staircase","maintenance","common area",
        "water tank","pump","generator","building","society","block","tower","flat"
    };

    public static String classify(String type, String description) {
        String combined = (type + " " + description).toLowerCase();
        int pub = 0, soc = 0;
        for (String kw : PUBLIC_KEYWORDS) if (combined.contains(kw)) pub++;
        for (String kw : SOCIETY_KEYWORDS) if (combined.contains(kw)) soc++;
        if (pub > soc) return "Public-Area";
        if (soc > pub) return "Society-Level";
        return switch (type.toLowerCase()) {
            case "sanitation","road","water supply","electricity" -> "Public-Area";
            case "plumbing","electrical","security","lift/elevator","parking" -> "Society-Level";
            default -> "Public-Area";
        };
    }

    public static String assignHead(String category, String complaintType) {
        if ("Society-Level".equals(category)) {
            return switch (complaintType.toLowerCase()) {
                case "plumbing" -> "Society Plumbing Head";
                case "electrical" -> "Society Electrical Head";
                case "security" -> "Society Security Officer";
                case "lift/elevator" -> "Society Maintenance Head";
                default -> "Society Admin";
            };
        } else {
            return switch (complaintType.toLowerCase()) {
                case "sanitation","garbage" -> "Municipal Sanitation Officer";
                case "road" -> "PWD Road Officer";
                case "electrical","electricity" -> "DISCOM Officer";
                case "water supply" -> "Jal Board Officer";
                default -> "Municipal Corporation Officer";
            };
        }
    }

    public static int predictDays(String priority) {
        return switch (priority.toLowerCase()) {
            case "urgent" -> 1 + (int)(Math.random() * 2);
            case "high"   -> 2 + (int)(Math.random() * 3);
            case "medium" -> 5 + (int)(Math.random() * 5);
            case "low"    -> 10 + (int)(Math.random() * 10);
            default -> 7;
        };
    }

    public static String generateComplaintId() {
        long ts = System.currentTimeMillis() % 100000;
        return "GRV-" + ts;
    }

    public static String priorityColor(String priority) {
        return switch (priority == null ? "" : priority.toLowerCase()) {
            case "urgent" -> "#ef5350";
            case "high"   -> "#ff7043";
            case "medium" -> "#ffb300";
            case "low"    -> "#66bb6a";
            default       -> "#90a4ae";
        };
    }

    public static String statusColor(String status) {
        return switch (status == null ? "" : status.toLowerCase()) {
            case "completed"   -> "#00c853";
            case "in progress" -> "#ffb300";
            case "pending"     -> "#ef5350";
            default            -> "#90a4ae";
        };
    }
}
