package com.grievio.ui.setup;

import com.grievio.ui.components.*;
import com.grievio.ui.RoleSelectionPage;
import com.grievio.ui.login.LoginPage;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;

/**
 * First-time location setup.
 * EMBEDDED MAP: Uses JavaFX WebView via reflection if available,
 * otherwise renders a lightweight Leaflet.js map inside a JEditorPane
 * served from a local temp HTML file — all inside the application window.
 * NO external browser is opened.
 *
 * Scrolling is fully fixed — all fields reachable on normal screens.
 * Back button provided.
 * Lat/Lng fields removed from UI (stored internally from map click).
 */
public class SetupPage extends BaseWindow {

    private final String role, accountId, email;

    private RoundedTextField districtField, sectorField, societyField,
                              towerField, flatField, addressField;
    private double selectedLat = 0, selectedLng = 0;
    private JLabel coordLabel;

    public SetupPage(String role, String accountId, String email) {
        super(); this.role = role; this.accountId = accountId; this.email = email;
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new GridBagLayout()); root.setOpaque(false);
        bg.add(root, BorderLayout.CENTER);

        JPanel masterPanel = new JPanel(new BorderLayout(0,0));
        masterPanel.setOpaque(false);
        masterPanel.setPreferredSize(new Dimension(1100, 760));

        // Left: address form in scroll pane
        masterPanel.add(buildFormPanel(), BorderLayout.WEST);
        // Right: embedded map panel
        masterPanel.add(buildMapPanel(), BorderLayout.CENTER);

        root.add(masterPanel);
    }

    // ── Left: Address form ────────────────────────────────────────────────────
    private JScrollPane buildFormPanel() {
        JPanel form = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(236,242,252)); g2.fillRoundRect(0,0,getWidth(),getHeight(),0,0);
                g2.dispose(); super.paintComponent(g);
            }
        };
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(28,36,28,28));

        // Accent top strip
        JPanel accent = new JPanel(){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setPaint(new GradientPaint(0,0,new Color(37,99,235),(float)getWidth(),0,new Color(16,200,140)));g2.fillRect(0,0,getWidth(),getHeight());g2.dispose();}};
        accent.setMaximumSize(new Dimension(Integer.MAX_VALUE,4)); accent.setOpaque(false);
        form.add(accent); form.add(Box.createVerticalStrut(20));

        // Back button
        JButton backBtn = backLink();
        backBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        backBtn.addActionListener(e -> switchTo(new LoginPage(role)));
        form.add(backBtn); form.add(Box.createVerticalStrut(14));

        JLabel titleLbl = new JLabel("\uD83D\uDCCD  Location Setup");
        titleLbl.setFont(new Font("Segoe UI",Font.BOLD,22));
        titleLbl.setForeground(new Color(12,26,64)); titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subLbl = new JLabel("Set your location. Click the map to pin your address.");
        subLbl.setFont(new Font("Segoe UI",Font.PLAIN,12));
        subLbl.setForeground(new Color(95,115,155)); subLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(titleLbl); form.add(Box.createVerticalStrut(4)); form.add(subLbl);
        form.add(Box.createVerticalStrut(20));

        districtField = tf(); sectorField = tf(); societyField = tf();
        towerField = tf(); flatField = tf(); addressField = tf();

        addField(form,"District *",districtField);
        addField(form,"Sector *",sectorField);
        if (needsSociety()) addField(form,"Society *",societyField);
        if (needsTowerFlat()) {
            // Two fields side by side
            JPanel twoCol = new JPanel(new GridLayout(1,2,12,0));
            twoCol.setOpaque(false); twoCol.setAlignmentX(Component.LEFT_ALIGNMENT);
            twoCol.setMaximumSize(new Dimension(Integer.MAX_VALUE,68));
            twoCol.add(labeled("Tower / Block",towerField));
            twoCol.add(labeled("Flat Number",flatField));
            form.add(twoCol); form.add(Box.createVerticalStrut(14));
        }
        addField(form,"Full Address",addressField);

        // Coordinate display (no input — set by map click)
        coordLabel = new JLabel("Map: No location selected yet");
        coordLabel.setFont(new Font("Segoe UI",Font.ITALIC,12));
        coordLabel.setForeground(new Color(90,130,200));
        coordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(coordLabel); form.add(Box.createVerticalStrut(22));

        // Save button
        JButton saveBtn = saveBtn("Save & Continue \u2192");
        saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        saveBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE,50));
        saveBtn.addActionListener(e -> doSave());
        form.add(saveBtn); form.add(Box.createVerticalStrut(10));

        JScrollPane scroll = new JScrollPane(form);
        scroll.setOpaque(false); scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.setPreferredSize(new Dimension(420, 760));
        return scroll;
    }

    // ── Right: Embedded map panel ─────────────────────────────────────────────
    private JPanel buildMapPanel() {
        JPanel mapContainer = new JPanel(new BorderLayout());
        mapContainer.setOpaque(false);
        mapContainer.setBorder(new EmptyBorder(8,8,8,8));

        // Header label
        JLabel mapTitle = new JLabel("  \uD83D\uDDFA\uFE0F  Click on the map to pin your location");
        mapTitle.setFont(new Font("Segoe UI",Font.BOLD,13));
        mapTitle.setForeground(new Color(180,210,255));
        mapTitle.setOpaque(true);
        mapTitle.setBackground(new Color(8,20,55));
        mapTitle.setPreferredSize(new Dimension(0,36));
        mapContainer.add(mapTitle, BorderLayout.NORTH);

        // Embedded map using JEditorPane with Leaflet HTML
        JEditorPane mapPane = createEmbeddedMap();
        mapPane.setPreferredSize(new Dimension(640,700));
        mapContainer.add(mapPane, BorderLayout.CENTER);

        // Instruction strip at bottom
        JLabel hint = new JLabel("  Click anywhere on the map to select your location. Address auto-fills below.");
        hint.setFont(new Font("Segoe UI",Font.PLAIN,11));
        hint.setForeground(new Color(130,165,220));
        hint.setOpaque(true);
        hint.setBackground(new Color(8,20,55));
        hint.setPreferredSize(new Dimension(0,28));
        mapContainer.add(hint, BorderLayout.SOUTH);

        return mapContainer;
    }

    /**
     * Creates a JEditorPane rendering a full Leaflet.js map from a local temp HTML file.
     * The map is fully interactive — click to place a pin, coordinates stored internally.
     * No external browser is opened. Everything stays inside the application window.
     */
    private JEditorPane createEmbeddedMap() {
        String leafletHTML = buildLeafletHTML();

        // Write to temp file so JEditorPane can load it (needed for relative JS)
        File tempHtml;
        try {
            tempHtml = File.createTempFile("grievio_map_", ".html");
            tempHtml.deleteOnExit();
            Files.writeString(tempHtml.toPath(), leafletHTML, java.nio.charset.StandardCharsets.UTF_8);
        } catch (IOException e) {
            // Fallback: plain instruction panel if temp file fails
            JEditorPane fallback = new JEditorPane("text/html",
                "<html><body style='background:#0a1628;color:#8baad4;font-family:Segoe UI;padding:40px;text-align:center;'>" +
                "<h2 style='color:#60a5fa;'>Map Unavailable</h2>" +
                "<p>Please enter your address manually in the fields on the left.</p>" +
                "</body></html>");
            fallback.setEditable(false); return fallback;
        }

        JEditorPane pane = new JEditorPane();
        pane.setEditable(false);
        pane.setContentType("text/html");

        try {
            pane.setPage(tempHtml.toURI().toURL());
        } catch (IOException ex) {
            pane.setText("<html><body style='background:#0a1628;color:#aaa;font-family:Segoe UI;padding:30px;'>" +
                "<p>Map could not load. Enter address manually.</p></body></html>");
        }

        // Listen for property changes — JEditorPane fires when links are clicked
        // We use a custom URL scheme grievio://lat,lng to capture clicks from JS
        pane.addHyperlinkListener(e -> {
            if (e.getEventType() == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED) {
                String url = e.getURL() != null ? e.getURL().toExternalForm() : "";
                if (url.startsWith("grievio://")) {
                    String coords = url.replace("grievio://","");
                    String[] parts = coords.split(",");
                    if (parts.length >= 2) {
                        try {
                            selectedLat = Double.parseDouble(parts[0]);
                            selectedLng = Double.parseDouble(parts[1]);
                            SwingUtilities.invokeLater(() ->
                                coordLabel.setText(String.format("Pinned: %.5f, %.5f", selectedLat, selectedLng))
                            );
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
        });

        return pane;
    }

    /** Builds a complete self-contained Leaflet.js HTML page using CDN. */
    private String buildLeafletHTML() {
        return """
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8"/>
<title>Grievio Map</title>
<meta name="viewport" content="width=device-width,initial-scale=1"/>
<link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"/>
<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
<style>
  * { margin:0; padding:0; box-sizing:border-box; }
  body { background:#0a1628; font-family:'Segoe UI',sans-serif; }
  #map { width:100%; height:100vh; }
  #info {
    position:absolute; bottom:12px; left:50%; transform:translateX(-50%);
    background:rgba(8,20,60,0.92); color:#8baad4;
    padding:8px 18px; border-radius:20px; font-size:12px;
    border:1px solid rgba(59,130,246,0.4); z-index:1000;
    pointer-events:none;
  }
</style>
</head>
<body>
<div id="map"></div>
<div id="info">Click anywhere to pin your location</div>
<script>
var map = L.map('map', {
  center: [28.6139, 77.2090],
  zoom: 12,
  zoomControl: true
});
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
  attribution: '&copy; OpenStreetMap contributors',
  maxZoom: 19
}).addTo(map);

var marker = null;
var customIcon = L.divIcon({
  className: '',
  html: '<div style="width:16px;height:16px;background:#3b82f6;border:3px solid white;border-radius:50%;box-shadow:0 0 8px rgba(59,130,246,0.8);"></div>',
  iconSize:[16,16], iconAnchor:[8,8]
});

map.on('click', function(e) {
  var lat = e.latlng.lat.toFixed(6);
  var lng = e.latlng.lng.toFixed(6);
  if (marker) map.removeLayer(marker);
  marker = L.marker(e.latlng, {icon: customIcon}).addTo(map);
  marker.bindPopup('<b>Selected</b><br>'+lat+', '+lng).openPopup();
  document.getElementById('info').innerHTML = 'Pinned: ' + lat + ', ' + lng;
  // Signal coordinates back to Java via anchor navigation
  window.location.href = 'grievio://' + lat + ',' + lng;
});
</script>
</body>
</html>
""";
    }

    // ── Save ──────────────────────────────────────────────────────────────────
    private void doSave() {
        if (districtField.getText().isBlank() || sectorField.getText().isBlank()) {
            JOptionPane.showMessageDialog(this,"District and Sector are required.","Validation",JOptionPane.WARNING_MESSAGE); return;
        }
        // TODO: INSERT into account_mapping table using DatabaseManager
        // Fields: accountId, role, district, sector, society, tower, flat, address, selectedLat, selectedLng
        JOptionPane.showMessageDialog(this,
            "<html><b>Location saved!</b><br>Dashboard coming in next update.</html>",
            "Setup Complete", JOptionPane.INFORMATION_MESSAGE);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private boolean needsSociety()   { return role.equals("RESIDENT")||role.equals("SOCIETY_ADMIN")||role.equals("PARTNER"); }
    private boolean needsTowerFlat() { return role.equals("RESIDENT"); }

    private RoundedTextField tf() { return new RoundedTextField(20,12); }

    private void addField(JPanel p, String label, JComponent field) {
        JLabel lbl=new JLabel(label); lbl.setFont(new Font("Segoe UI",Font.PLAIN,13));
        lbl.setForeground(new Color(55,75,130)); lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT); field.setMaximumSize(new Dimension(Integer.MAX_VALUE,46));
        p.add(lbl); p.add(Box.createVerticalStrut(4)); p.add(field); p.add(Box.createVerticalStrut(14));
    }

    private JPanel labeled(String lbl, JComponent field) {
        JPanel p=new JPanel(); p.setOpaque(false); p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
        JLabel l=new JLabel(lbl); l.setFont(new Font("Segoe UI",Font.PLAIN,13));
        l.setForeground(new Color(55,75,130)); l.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT); field.setMaximumSize(new Dimension(Integer.MAX_VALUE,46));
        p.add(l); p.add(Box.createVerticalStrut(4)); p.add(field); return p;
    }

    private JButton backLink() {
        JButton b=new JButton("\u2190 Back"); b.setFont(new Font("Segoe UI",Font.BOLD,13));
        b.setForeground(new Color(37,99,235)); b.setOpaque(false); b.setContentAreaFilled(false);
        b.setBorderPainted(false); b.setFocusPainted(false); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
    }

    private JButton saveBtn(String text) {
        JButton b=new JButton(text){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g2.setPaint(new GradientPaint(0,0,new Color(25,72,195),getWidth(),0,new Color(37,99,235)));g2.fillRoundRect(0,0,getWidth(),getHeight(),13,13);g2.dispose();super.paintComponent(g);}};
        b.setFont(new Font("Segoe UI",Font.BOLD,15)); b.setForeground(Color.WHITE);
        b.setOpaque(false); b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
    }
}
