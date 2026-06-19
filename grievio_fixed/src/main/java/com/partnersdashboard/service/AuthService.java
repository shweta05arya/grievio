package com.partnersdashboard.service;

import com.partnersdashboard.dao.AccountDAO;
import com.partnersdashboard.dao.AdminDAO;
import com.partnersdashboard.dao.PartnerDAO;
import com.partnersdashboard.dao.ResidentDAO;
import com.partnersdashboard.model.Account;
import com.partnersdashboard.model.Partner;
import com.partnersdashboard.model.Resident;
import com.partnersdashboard.util.SessionManager;

/**
 * AuthService - Handles login logic for all roles.
 * Populates SessionManager with role-specific data after successful login.
 */
public class AuthService {

    private final AccountDAO  accountDAO  = new AccountDAO();
    private final ResidentDAO residentDAO = new ResidentDAO();
    private final PartnerDAO  partnerDAO  = new PartnerDAO();
    private final AdminDAO    adminDAO    = new AdminDAO();

    /**
     * Attempt login. Returns the Account if successful, null otherwise.
     * Populates SessionManager with profile data for the logged-in user.
     *
     * @param email         user email
     * @param plainPassword plain-text password
     * @param expectedRole  one of: RESIDENT, PARTNER, SOCIETY_ADMIN, MAIN_ADMIN (or null to accept any)
     */
    public Account login(String email, String plainPassword, String expectedRole) {
        Account acc = accountDAO.login(email, plainPassword);
        if (acc == null) return null;

        // If a role filter is provided, enforce it
        if (expectedRole != null && !acc.getRole().equals(expectedRole)) return null;

        // Populate session
        SessionManager session = SessionManager.getInstance();
        session.setAccount(acc);

        switch (acc.getRole()) {
            case "RESIDENT" -> {
                Resident r = residentDAO.getByAccountId(acc.getId());
                session.setResident(r);
            }
            case "PARTNER" -> {
                Partner p = partnerDAO.getByAccountId(acc.getId());
                session.setPartner(p);
            }
            case "SOCIETY_ADMIN" -> {
                int sid = adminDAO.getSocietyIdByAccountId(acc.getId());
                session.setAdminSocietyId(sid);
                session.setAdminName(adminDAO.getAdminName(acc.getId()));
            }
            case "MAIN_ADMIN" -> {
                session.setAdminSocietyId(-1);
                session.setAdminName(adminDAO.getAdminName(acc.getId()));
            }
        }
        return acc;
    }

    /** Register a new resident. Returns true on success. */
    public boolean registerResident(String email, String password, Resident profile) {
        // Check email uniqueness
        if (accountDAO.emailExists(email)) return false;

        // Create account
        int accountId = accountDAO.createAccount(email, password, "RESIDENT");
        if (accountId == -1) return false;

        // Create profile
        profile.setAccountId(accountId);
        return new ResidentDAO().createResident(profile);
    }

    public void logout() {
        SessionManager.getInstance().clear();
    }
}