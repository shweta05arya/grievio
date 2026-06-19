# Grievio Integrated Merge

This package merges:
- Project 1 login / intro / register flow as the startup
- Project 2 complaint system dashboards behind that login
- Project 3 partner dashboard launch through partner login

## Main entry
- `com.grievio.Main`

## What was changed
- `com.grievio.Main`
- `com.grievio.MainApp`
- `com.grievio.session.SessionManager`
- `com.grievio.ui.login.LoginPage`
- `com.grievio.database.DatabaseHelper`
- `com.grievio.db.DatabaseManager`
- added `com.grievio.integration.PortalLauncher`
- merged JavaFX dashboard source from the full system
- merged partner dashboard source from the partner module

## Notes
- Project 2 login startup is bypassed; logout returns to the Swing role/login flow.
- Partner portal launch uses the partner module and its own database configuration.
- Admin analytics are preserved from the full complaint system analytics section.

## Demo credentials
- User: `user@grievio.com / password123`
- Society Admin: `societyadmin@grievio.com / admin123`
- Sector Admin: `sectoradmin@grievio.com / admin123`
- Government: `governmentadmin@grievio.com / admin123`
- Partner: `raj.kumar@gravio.com / password123`
