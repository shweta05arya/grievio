Grievio Final

How to run in IntelliJ:
1. Open the extracted GrievioFinal folder (not the ZIP itself).
2. Let IntelliJ import Maven dependencies from pom.xml.
3. Run src/main/java/com/grievio/Main.java

What is included:
- Intro + role selection + admin type selection
- Resident/Admin/Partner login pages
- Separate Resident/Admin/Partner registration pages
- Real email OTP flow through EmailService.java
- Google Maps opening from the login / registration pages (opens real Google Maps in the default browser)
- Resident registration now auto-fills the resident login email + password after account creation
- Smoother scrolling on the resident registration form
- Resident sector / society fields now support manual entry with placeholder text
- SQLite local database stored at: ~/grievio.db

Default sample credentials:
- Resident: resident@grievio.com / Resident@123
- Partner: partner@grievio.com / Admin@123
- Society Admin: societyadmin@grievio.com / Admin@123
- Sector Admin: sectoradmin@grievio.com / Admin@123
- Government Admin: govadmin@grievio.com / Admin@123

Important:
- ParticleBackground.java was not changed.
- EmailService.java currently contains the Gmail sender + app password used for OTP.
- If OTP sending fails later, regenerate a new Gmail app password and update EmailService.java.
- If you later want a fully embedded in-app Google Maps webview instead of browser opening, share a Google Maps API key and browser-embedding preference.


MAP SETUP
1) Open src/main/resources/map-config.properties
2) Paste your Google Maps JavaScript API key after google.maps.api.key=
3) Enable these APIs in Google Cloud: Maps JavaScript API, Places API, Geocoding API
4) Then rebuild/run in IntelliJ

Demo logins
Society Admin: societyadmin@grievio.com / 1234
Sector Admin: sectoradmin@grievio.com / 1234
Government: governmentadmin@grievio.com / 1234
Partner: raj.kumar@gravio.com / password123
