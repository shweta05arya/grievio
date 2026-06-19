package com.partnersdashboard.util;

import com.partnersdashboard.model.Account;
import com.partnersdashboard.model.Partner;
import com.partnersdashboard.model.Resident;

/** Global singleton holding the current in-app session. */
public class SessionManager {

    private static SessionManager instance;

    private Account currentAccount;
    private Resident currentResident;
    private Partner currentPartner;
    private int adminSocietyId = -1;
    private String adminName;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public void clear() {
        currentAccount = null;
        currentResident = null;
        currentPartner = null;
        adminSocietyId = -1;
        adminName = null;
    }

    public Account getAccount() { return currentAccount; }
    public void setAccount(Account a) { this.currentAccount = a; }

    public Resident getResident() { return currentResident; }
    public void setResident(Resident r) { this.currentResident = r; }

    public Partner getPartner() { return currentPartner; }
    public void setPartner(Partner p) { this.currentPartner = p; }

    public int getAdminSocietyId() { return adminSocietyId; }
    public void setAdminSocietyId(int id) { this.adminSocietyId = id; }

    public String getAdminName() { return adminName; }
    public void setAdminName(String name) { this.adminName = name; }

    public boolean isLoggedIn() { return currentAccount != null; }
    public String getRole() { return currentAccount != null ? currentAccount.getRole() : null; }
    public int getAccountId() { return currentAccount != null ? currentAccount.getId() : -1; }
    public boolean isMainAdmin() { return "MAIN_ADMIN".equals(getRole()) || adminSocietyId == -1; }

    public void setAccountId(int id) {
        if (currentAccount != null) {
            currentAccount.setId(id);
        }
    }
}
