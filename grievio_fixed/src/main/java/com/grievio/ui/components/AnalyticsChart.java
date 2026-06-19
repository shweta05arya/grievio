package com.grievio.ui.components;

import javafx.geometry.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.*;

/**
 * Pure JavaFX chart components matching the Analytics & Insights design from the screenshot.
 * - buildStatCard(...)   → top metric card with colored circle
 * - buildLineChart(...)  → multi-series area line chart
 * - buildDonutChart(...) → donut chart with legend
 * - buildBarChart(...)   → horizontal bar chart
 */
public class AnalyticsChart {

    // ── Stat Card (top row) ────────────────────────────────────────────────────

    public static VBox buildStatCard(String title, String value, String circleColor, String valueColor) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color:white;-fx-background-radius:12;-fx-padding:20;" +
            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),8,0,0,2);");

        // Circle indicator
        Canvas circle = new Canvas(24, 24);
        GraphicsContext gc = circle.getGraphicsContext2D();
        gc.setFill(Color.web(circleColor));
        gc.fillOval(0, 0, 24, 24);

        Label valLbl = new Label(value);
        valLbl.setStyle("-fx-font-size:26px;-fx-font-weight:bold;-fx-text-fill:" + valueColor + ";");

        Label titleLbl = new Label(title.toUpperCase());
        titleLbl.setStyle("-fx-font-size:10px;-fx-text-fill:#888;-fx-letter-spacing:1px;");

        card.getChildren().addAll(circle, valLbl, titleLbl);
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    // ── Line Chart (Monthly Trend) ─────────────────────────────────────────────

    /**
     * @param labels  X-axis labels (months)
     * @param series  Map of seriesName -> double[] values (same length as labels)
     * @param colors  Map of seriesName -> "#hexcolor"
     */
    public static VBox buildLineChart(String chartTitle, List<String> labels,
                                      Map<String, double[]> series, Map<String, String> colors) {
        VBox wrapper = new VBox(10);
        wrapper.setStyle("-fx-background-color:white;-fx-background-radius:12;-fx-padding:20;" +
            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),8,0,0,2);");

        Label title = new Label(chartTitle);
        title.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#1a1a2e;");

        int W = 820, H = 260;
        Canvas canvas = new Canvas(W, H);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        drawLineChart(gc, W, H, labels, series, colors);

        // Legend
        HBox legend = new HBox(20);
        legend.setAlignment(Pos.CENTER_LEFT);
        for (Map.Entry<String, String> e : colors.entrySet()) {
            HBox item = new HBox(6);
            item.setAlignment(Pos.CENTER_LEFT);
            Canvas dot = new Canvas(10, 10);
            GraphicsContext dg = dot.getGraphicsContext2D();
            dg.setFill(Color.web(e.getValue()));
            dg.fillOval(0, 0, 10, 10);
            Label name = new Label(e.getKey());
            name.setStyle("-fx-font-size:11px;-fx-text-fill:#555;");
            item.getChildren().addAll(dot, name);
            legend.getChildren().add(item);
        }

        wrapper.getChildren().addAll(title, canvas, legend);
        return wrapper;
    }

    private static void drawLineChart(GraphicsContext gc, int W, int H,
                                       List<String> labels, Map<String, double[]> series,
                                       Map<String, String> colors) {
        int padL = 45, padR = 20, padT = 20, padB = 40;
        int chartW = W - padL - padR;
        int chartH = H - padT - padB;

        // Background
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, W, H);

        // Find max value
        double maxVal = 0;
        for (double[] vals : series.values())
            for (double v : vals) if (v > maxVal) maxVal = v;
        if (maxVal == 0) maxVal = 10;

        // Grid lines
        gc.setStroke(Color.rgb(230, 235, 245));
        gc.setLineWidth(1);
        int gridLines = 5;
        for (int i = 0; i <= gridLines; i++) {
            double y = padT + chartH - (i * chartH / gridLines);
            gc.strokeLine(padL, y, padL + chartW, y);
            gc.setFill(Color.rgb(150, 160, 180));
            gc.setFont(Font.font(9));
            gc.fillText(String.format("%.0f", i * maxVal / gridLines), 2, y + 4);
        }

        // X-axis labels
        int n = labels.size();
        gc.setFill(Color.rgb(130, 140, 160));
        gc.setFont(Font.font(9));
        for (int i = 0; i < n; i++) {
            double x = padL + i * (double) chartW / (n - 1);
            gc.fillText(labels.get(i), x - 12, H - 8);
        }

        // Draw each series
        for (Map.Entry<String, double[]> entry : series.entrySet()) {
            double[] vals = entry.getValue();
            Color lineColor = Color.web(colors.getOrDefault(entry.getKey(), "#1565c0"));
            int sz = Math.min(vals.length, n);

            double[] xs = new double[sz];
            double[] ys = new double[sz];
            for (int i = 0; i < sz; i++) {
                xs[i] = padL + i * (double) chartW / (n - 1);
                ys[i] = padT + chartH - (vals[i] / maxVal * chartH);
            }

            // Fill area
            gc.setFill(lineColor.deriveColor(0, 1, 1, 0.12));
            gc.beginPath();
            gc.moveTo(xs[0], padT + chartH);
            for (int i = 0; i < sz; i++) gc.lineTo(xs[i], ys[i]);
            gc.lineTo(xs[sz - 1], padT + chartH);
            gc.closePath();
            gc.fill();

            // Line
            gc.setStroke(lineColor);
            gc.setLineWidth(2.5);
            gc.beginPath();
            gc.moveTo(xs[0], ys[0]);
            for (int i = 1; i < sz; i++) gc.lineTo(xs[i], ys[i]);
            gc.stroke();

            // Dots
            gc.setFill(lineColor);
            for (int i = 0; i < sz; i++) {
                gc.fillOval(xs[i] - 4, ys[i] - 4, 8, 8);
            }
        }
    }

    // ── Donut Chart ────────────────────────────────────────────────────────────

    /**
     * @param title   Chart title
     * @param slices  Map of label -> value
     * @param colors  Map of label -> "#hexcolor"
     * @param centerLabel Big text in center (e.g. total count)
     * @param centerSub   Sub text in center (e.g. "Total")
     */
    public static VBox buildDonutChart(String title, Map<String, Double> slices,
                                        Map<String, String> colors,
                                        String centerLabel, String centerSub) {
        VBox wrapper = new VBox(10);
        wrapper.setStyle("-fx-background-color:white;-fx-background-radius:12;-fx-padding:20;" +
            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),8,0,0,2);");
        HBox.setHgrow(wrapper, Priority.ALWAYS);

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#1a1a2e;");

        int SIZE = 200;
        Canvas canvas = new Canvas(SIZE, SIZE);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        drawDonut(gc, SIZE, slices, colors, centerLabel, centerSub);

        // Legend
        VBox legend = new VBox(4);
        for (Map.Entry<String, Double> e : slices.entrySet()) {
            HBox item = new HBox(8);
            item.setAlignment(Pos.CENTER_LEFT);
            Canvas dot = new Canvas(12, 12);
            GraphicsContext dg = dot.getGraphicsContext2D();
            dg.setFill(Color.web(colors.getOrDefault(e.getKey(), "#aaa")));
            dg.fillRoundRect(0, 0, 12, 12, 3, 3);
            Label lbl = new Label(e.getKey() + "  (" + e.getValue().intValue() + ")");
            lbl.setStyle("-fx-font-size:10px;-fx-text-fill:#555;");
            item.getChildren().addAll(dot, lbl);
            legend.getChildren().add(item);
        }

        StackPane chartStack = new StackPane(canvas);
        chartStack.setAlignment(Pos.CENTER);

        HBox body = new HBox(16, chartStack, legend);
        body.setAlignment(Pos.CENTER_LEFT);

        wrapper.getChildren().addAll(titleLbl, body);
        return wrapper;
    }

    private static void drawDonut(GraphicsContext gc, int SIZE,
                                   Map<String, Double> slices, Map<String, String> colors,
                                   String centerLabel, String centerSub) {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, SIZE, SIZE);

        double total = slices.values().stream().mapToDouble(Double::doubleValue).sum();
        if (total == 0) total = 1;

        double cx = SIZE / 2.0, cy = SIZE / 2.0;
        double outer = SIZE * 0.44, inner = SIZE * 0.28;
        double startAngle = -90;

        for (Map.Entry<String, Double> e : slices.entrySet()) {
            double sweep = (e.getValue() / total) * 360;
            Color col = Color.web(colors.getOrDefault(e.getKey(), "#aaa"));
            gc.setFill(col);
            gc.fillArc(cx - outer, cy - outer, outer * 2, outer * 2,
                startAngle, sweep, javafx.scene.shape.ArcType.ROUND);
            startAngle += sweep;
        }

        // White center hole
        gc.setFill(Color.WHITE);
        gc.fillOval(cx - inner, cy - inner, inner * 2, inner * 2);

        // Center text
        gc.setFill(Color.rgb(30, 30, 60));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        double tw = centerLabel.length() * 11;
        gc.fillText(centerLabel, cx - tw / 2, cy + 7);

        gc.setFill(Color.rgb(130, 140, 160));
        gc.setFont(Font.font(10));
        double sw = centerSub.length() * 5.5;
        gc.fillText(centerSub, cx - sw / 2, cy + 22);
    }

    // ── Horizontal Bar Chart ───────────────────────────────────────────────────

    public static VBox buildBarChart(String title, Map<String, Integer> data, String barColor) {
        VBox wrapper = new VBox(10);
        wrapper.setStyle("-fx-background-color:#12294a;-fx-background-radius:12;-fx-padding:20;");

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:white;");
        wrapper.getChildren().add(titleLbl);

        int maxVal = data.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        if (maxVal == 0) maxVal = 1;

        for (Map.Entry<String, Integer> e : data.entrySet()) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            Label lbl = new Label(e.getKey());
            lbl.setStyle("-fx-font-size:11px;-fx-text-fill:#90caf9;");
            lbl.setMinWidth(150);

            double fillRatio = (double) e.getValue() / maxVal;
            StackPane barBg = new StackPane();
            barBg.setMinHeight(22);
            barBg.setStyle("-fx-background-color:rgba(255,255,255,0.08);-fx-background-radius:4;");
            HBox.setHgrow(barBg, Priority.ALWAYS);

            Region fill = new Region();
            fill.setStyle("-fx-background-color:" + barColor + ";-fx-background-radius:4;");
            fill.setMaxWidth(Double.MAX_VALUE);
            StackPane.setAlignment(fill, Pos.CENTER_LEFT);
            barBg.getChildren().add(fill);
            // Use a percentage-based approach via binding
            fill.prefWidthProperty().bind(barBg.widthProperty().multiply(fillRatio));

            Label cnt = new Label("" + e.getValue());
            cnt.setStyle("-fx-font-size:11px;-fx-text-fill:#e0e0e0;-fx-min-width:30;");
            cnt.setAlignment(Pos.CENTER_RIGHT);

            row.getChildren().addAll(lbl, barBg, cnt);
            wrapper.getChildren().add(row);
        }
        return wrapper;
    }
}
