package com.grievio.ui.components;

import com.grievio.MainApp;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.web.*;
import javafx.stage.*;
import netscape.javascript.JSObject;

public class MapHelper {

    public static class MapBridge {
        public Label coordLbl;
        public Button confirmBtn;
        public double[] latLng;
        public String[] addr;

        public MapBridge(Label coordLbl, Button confirmBtn, double[] latLng, String[] addr) {
            this.coordLbl = coordLbl; this.confirmBtn = confirmBtn;
            this.latLng = latLng; this.addr = addr;
        }

        public void locationSelected(double lat, double lng, String address) {
            latLng[0] = lat; latLng[1] = lng;
            addr[0] = (address != null) ? address : "";
            Platform.runLater(() -> {
                try {
                    String display = addr[0].isEmpty()
                        ? String.format("%.5f, %.5f", lat, lng) : addr[0];
                    coordLbl.setText("📍  " + display);
                    confirmBtn.setDisable(false);
                } catch (Exception ex) {
                    System.err.println("[MapBridge] UI update error: " + ex.getMessage());
                }
            });
        }
    }

    public static String showMapPicker(String title, String buttonLabel) {
        try {
            Stage mapStage = new Stage();
            mapStage.initModality(Modality.APPLICATION_MODAL);
            if (MainApp.primaryStage != null) {
                try { mapStage.initOwner(MainApp.primaryStage); } catch (Exception ignored) {}
            }
            mapStage.setTitle(title);
            mapStage.setWidth(1200);
            mapStage.setHeight(700);

            WebView wv = new WebView();
            WebEngine engine = wv.getEngine();
            engine.setJavaScriptEnabled(true);

            double[] latLng = {28.6139, 77.2090};
            String[] addr = {""};
            String[] result = {null};

            Label coordLbl = new Label("📍  Click on the map or search to select location");
            coordLbl.setStyle("-fx-font-size:12px;-fx-text-fill:#64b5f6;-fx-background-color:#091524;" +
                "-fx-padding:6 16;-fx-min-width:500;");

            Button confirmBtn = new Button("✅  " + buttonLabel);
            confirmBtn.setStyle("-fx-background-color:#00c853;-fx-text-fill:white;-fx-font-weight:bold;" +
                "-fx-background-radius:8;-fx-padding:10 28;-fx-cursor:hand;-fx-font-size:13px;");
            confirmBtn.setDisable(true);

            Button cancelBtn = new Button("✖  Cancel");
            cancelBtn.setStyle("-fx-background-color:#546e7a;-fx-text-fill:white;-fx-font-weight:bold;" +
                "-fx-background-radius:8;-fx-padding:10 20;-fx-cursor:hand;-fx-font-size:13px;");
            cancelBtn.setOnAction(e -> mapStage.close());

            Region hSp = new Region(); HBox.setHgrow(hSp, Priority.ALWAYS);
            HBox topBar = new HBox(12, coordLbl, hSp, confirmBtn, cancelBtn);
            topBar.setAlignment(Pos.CENTER_LEFT);
            topBar.setPadding(new Insets(8, 14, 8, 14));
            topBar.setStyle("-fx-background-color:#091524;-fx-border-color:#1565c0;-fx-border-width:0 0 1 0;");

            VBox layout = new VBox(0, topBar, wv);
            VBox.setVgrow(wv, Priority.ALWAYS);
            layout.setStyle("-fx-background-color:#0d1b2a;");

            try {
                engine.loadContent(buildMapHtml(28.6139, 77.2090));
            } catch (Exception ex) {
                System.err.println("[MapHelper] Failed to load map HTML: " + ex.getMessage());
                engine.loadContent("<html><body style='background:#0d1b2a;color:white;font-family:Arial;" +
                    "padding:40px;text-align:center;'><h2>Map unavailable</h2>" +
                    "<p>Please type your location manually.</p></body></html>");
            }

            engine.getLoadWorker().stateProperty().addListener((obs, old, nw) -> {
                if (nw == javafx.concurrent.Worker.State.SUCCEEDED) {
                    try {
                        JSObject win = (JSObject) engine.executeScript("window");
                        win.setMember("javaBridge", new MapBridge(coordLbl, confirmBtn, latLng, addr));
                    } catch (Exception ex) {
                        System.err.println("[MapHelper] JS bridge error: " + ex.getMessage());
                    }
                } else if (nw == javafx.concurrent.Worker.State.FAILED) {
                    System.err.println("[MapHelper] WebEngine load failed.");
                }
            });

            confirmBtn.setOnAction(e -> {
                try {
                    String display = addr[0].isEmpty()
                        ? String.format("%.5f, %.5f", latLng[0], latLng[1]) : addr[0];
                    if (display.contains(",")) {
                        String[] parts = display.split(",");
                        int take = Math.min(3, parts.length);
                        StringBuilder sb2 = new StringBuilder();
                        for (int i = 0; i < take; i++) {
                            if (i > 0) sb2.append(", ");
                            sb2.append(parts[i].trim());
                        }
                        display = sb2.toString();
                    }
                    result[0] = display;
                } catch (Exception ex) {
                    result[0] = String.format("%.5f, %.5f", latLng[0], latLng[1]);
                }
                mapStage.close();
            });

            Scene scene = new Scene(layout);
            try {
                String css = MainApp.class.getResource("/com/grievio/css/styles.css").toExternalForm();
                scene.getStylesheets().add(css);
            } catch (Exception ignored) {}

            mapStage.setScene(scene);
            mapStage.showAndWait();
            return result[0];

        } catch (Exception ex) {
            System.err.println("[MapHelper] Map dialog error: " + ex.getMessage());
            ex.printStackTrace();
            UIHelper.showAlert("Map could not be opened. Please type your location manually.");
            return null;
        }
    }

    private static String buildMapHtml(double lat, double lng) {
        return ("<!DOCTYPE html><html><head>" +
            "<meta charset='utf-8'/>" +
            "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>" +
            "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>" +
            "<style>" +
            "*{margin:0;padding:0;box-sizing:border-box;}" +
            "html,body{width:100%;height:100%;overflow:hidden;background:#0d1b2a;font-family:Arial,sans-serif;}" +
            "#searchBox{position:absolute;top:12px;left:50%;transform:translateX(-50%);" +
            "width:560px;max-width:90vw;z-index:1000;background:#0d2137;border:1.5px solid #1565c0;" +
            "border-radius:10px;box-shadow:0 4px 24px rgba(0,0,0,0.55);overflow:hidden;}" +
            "#searchRow{display:flex;background:#0d2137;}" +
            "#si{flex:1;background:#12294a;color:white;border:none;padding:11px 16px;font-size:14px;outline:none;}" +
            "#si::placeholder{color:#546e7a;}" +
            "#sb{background:#1565c0;color:white;border:none;padding:11px 22px;cursor:pointer;font-weight:bold;font-size:14px;}" +
            "#sb:hover{background:#1976d2;}" +
            "#results{max-height:220px;overflow-y:auto;display:none;border-top:1px solid #1565c0;}" +
            ".ri{padding:10px 14px;color:#e0e0e0;font-size:13px;cursor:pointer;border-bottom:1px solid #1a3a5c;}" +
            ".ri:hover{background:#1565c0;color:white;}" +
            ".ri-name{font-weight:bold;color:white;margin-bottom:2px;}" +
            ".ri-detail{font-size:11px;color:#90a4ae;}" +
            "#statusPill{position:absolute;bottom:16px;left:50%;transform:translateX(-50%);" +
            "background:rgba(13,33,55,0.92);color:#64b5f6;padding:8px 20px;border-radius:20px;" +
            "font-size:12px;border:1px solid #1565c0;z-index:1000;pointer-events:none;}" +
            "#map{position:absolute;top:0;left:0;width:100%;height:100%;z-index:1;}" +
            ".leaflet-container{cursor:crosshair !important;}" +
            "</style></head><body>" +
            "<div id='map'></div>" +
            "<div id='searchBox'><div id='searchRow'>" +
            "<input id='si' placeholder='Search city, area, locality...' onkeydown='if(event.keyCode===13){event.preventDefault();doSearch();}'/>" +
            "<button id='sb' onclick='doSearch()'>Search</button></div><div id='results'></div></div>" +
            "<div id='statusPill'>Click anywhere on the map to pin your location</div>" +
            "<script>" +
            "var map=L.map('map',{zoomControl:true}).setView([LAT,LNG],12);" +
            "L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png'," +
            "{attribution:'© OpenStreetMap © CartoDB',maxZoom:19}).addTo(map);" +
            "var marker=null;" +
            "var CustomIcon=L.divIcon({className:''," +
            "html:'<div style=\"background:#1565c0;width:22px;height:22px;border-radius:50%;border:3px solid white;box-shadow:0 0 12px rgba(21,101,192,0.8);\"></div>'," +
            "iconSize:[22,22],iconAnchor:[11,11]});" +
            "map.on('click',function(e){" +
            "var lat=e.latlng.lat,lng=e.latlng.lng;" +
            "if(marker)map.removeLayer(marker);" +
            "marker=L.marker([lat,lng],{icon:CustomIcon}).addTo(map);" +
            "document.getElementById('statusPill').textContent='Loading address...';" +
            "fetch('https://nominatim.openstreetmap.org/reverse?lat='+lat+'&lon='+lng+'&format=json')" +
            ".then(function(r){return r.json();}).then(function(d){" +
            "var addr=d.display_name||'';" +
            "document.getElementById('statusPill').textContent='📍 '+addr.substring(0,80);" +
            "try{if(window.javaBridge)window.javaBridge.locationSelected(lat,lng,addr);}catch(e2){}" +
            "}).catch(function(){" +
            "document.getElementById('statusPill').textContent='📍 '+lat.toFixed(5)+', '+lng.toFixed(5);" +
            "try{if(window.javaBridge)window.javaBridge.locationSelected(lat,lng,'');}catch(e2){}" +
            "});});" +
            "function doSearch(){" +
            "var q=document.getElementById('si').value.trim();if(!q)return;" +
            "fetch('https://nominatim.openstreetmap.org/search?q='+encodeURIComponent(q)+'&format=json&limit=6')" +
            ".then(function(r){return r.json();}).then(function(data){" +
            "var res=document.getElementById('results');" +
            "res.innerHTML='';res.style.display=data.length?'block':'none';" +
            "data.forEach(function(item){" +
            "var d=document.createElement('div');d.className='ri';" +
            "var pts=item.display_name.split(',');" +
            "d.innerHTML='<div class=\"ri-name\">'+pts[0]+'</div><div class=\"ri-detail\">'+pts.slice(1,4).join(',')+'</div>';" +
            "d.onclick=function(){" +
            "res.style.display='none';" +
            "map.setView([item.lat,item.lon],15);" +
            "if(marker)map.removeLayer(marker);" +
            "marker=L.marker([item.lat,item.lon],{icon:CustomIcon}).addTo(map);" +
            "document.getElementById('statusPill').textContent='📍 '+item.display_name.substring(0,80);" +
            "try{if(window.javaBridge)window.javaBridge.locationSelected(parseFloat(item.lat),parseFloat(item.lon),item.display_name);}catch(e2){}" +
            "};res.appendChild(d);" +
            "});}).catch(function(){});}" +
            "</script></body></html>")
            .replace("LAT", String.valueOf(lat))
            .replace("LNG", String.valueOf(lng));
    }
}
