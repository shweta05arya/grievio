package com.partnersdashboard.service;

import com.partnersdashboard.config.AppConfig;
import com.partnersdashboard.dao.OtpDAO;
import com.partnersdashboard.model.Complaint;
import com.partnersdashboard.model.Resident;
import com.partnersdashboard.util.OtpUtil;

/** Handles generation, persistence, email delivery and verification of completion OTPs. */
public class OtpService {

    private final OtpDAO otpDAO = new OtpDAO();
    private final EmailService emailService = new EmailService();

    public boolean generateAndSendOtp(Complaint complaint, Resident resident) {
        String otp = OtpUtil.generateOtp();
        System.out.println("[OtpService] Generated OTP for complaint #" + complaint.getId());

        boolean saved = otpDAO.saveOtp(complaint.getId(), otp);
        if (!saved) {
            System.err.println("[OtpService] Failed to save OTP to DB");
            return false;
        }

        boolean sent = emailService.sendOtpEmail(resident.getEmail(), resident.getFullName(), complaint.getId(), otp);
        if (!sent) {
            System.err.println("[OtpService] Email send failed. OTP is still saved in DB for manual testing.");
        }
        return true;
    }

    public boolean verifyOtp(int complaintId, String enteredOtp) {
        return otpDAO.verifyOtp(complaintId, enteredOtp, AppConfig.OTP_VALIDITY_MINUTES);
    }
}
