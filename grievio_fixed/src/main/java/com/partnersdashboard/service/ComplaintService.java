package com.partnersdashboard.service;

import com.partnersdashboard.dao.AssignmentDAO;
import com.partnersdashboard.dao.ComplaintDAO;
import com.partnersdashboard.dao.NotificationDAO;
import com.partnersdashboard.dao.PartnerDAO;
import com.partnersdashboard.dao.ResidentDAO;
import com.partnersdashboard.model.Complaint;

import java.util.List;

/** Business logic for complaint lifecycle and partner workflow. */
public class ComplaintService {

    private final ComplaintDAO complaintDAO = new ComplaintDAO();
    private final AssignmentDAO assignmentDAO = new AssignmentDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final ResidentDAO residentDAO = new ResidentDAO();
    private final PartnerDAO partnerDAO = new PartnerDAO();

    public int submitComplaint(Complaint c) {
        int id = complaintDAO.submitComplaint(c);
        if (id > 0) {
            notificationDAO.create(2, "New Complaint #" + id,
                    "A new complaint has been submitted: " + c.getTitle());
        }
        return id;
    }

    public boolean assignPartner(int complaintId, int partnerId, int adminAccountId,
                                 String visitDate, String visitSlot, String notes) {
        boolean assigned = assignmentDAO.assign(complaintId, partnerId, adminAccountId, visitDate, visitSlot, notes);
        if (assigned) {
            complaintDAO.updateStatus(complaintId, Complaint.STATUS_ASSIGNED, adminAccountId, "Partner assigned");
            partnerDAO.updateStats(partnerId);
        }
        return assigned;
    }

    public boolean acceptTask(int complaintId, int partnerAccountId) {
        return complaintDAO.updateStatus(complaintId, Complaint.STATUS_ACCEPTED, partnerAccountId, "Partner accepted task");
    }

    public boolean markOnTheWay(int complaintId, int partnerAccountId) {
        return complaintDAO.updateStatus(complaintId, Complaint.STATUS_ON_THE_WAY, partnerAccountId, "Partner is on the way");
    }

    public boolean markInProgress(int complaintId, int partnerAccountId) {
        return complaintDAO.updateStatus(complaintId, Complaint.STATUS_IN_PROGRESS, partnerAccountId, "Work in progress");
    }

    public boolean markVerificationPending(int complaintId, int partnerAccountId) {
        return complaintDAO.updateStatus(complaintId, Complaint.STATUS_VERIFICATION_PENDING,
                partnerAccountId, "Waiting for resident OTP verification");
    }

    public boolean markCompleted(int complaintId, int partnerAccountId, String proofImagePath) {
        if (proofImagePath != null && !proofImagePath.isBlank()) {
            assignmentDAO.saveProofImage(complaintId, proofImagePath);
        }
        assignmentDAO.saveCompletionTime(complaintId);

        boolean updated = complaintDAO.updateStatus(complaintId, Complaint.STATUS_COMPLETED,
                partnerAccountId, "Work verified and completed");

        if (updated) {
            Complaint c = complaintDAO.getById(complaintId);
            if (c != null) {
                var resident = residentDAO.getById(c.getResidentId());
                if (resident != null) {
                    notificationDAO.create(resident.getAccountId(),
                            "Complaint #" + complaintId + " Completed",
                            "Your complaint '" + c.getTitle() + "' has been resolved. Please provide feedback.");
                }
                int partnerId = assignmentDAO.getPartnerIdForComplaint(complaintId);
                if (partnerId > 0) partnerDAO.updateStats(partnerId);
            }
        }
        return updated;
    }

    public boolean markCompleted(int complaintId, int partnerAccountId) {
        return markCompleted(complaintId, partnerAccountId, null);
    }

    public List<Complaint> getComplaintsForResident(int residentId) { return complaintDAO.getByResidentId(residentId); }
    public List<Complaint> getComplaintsForSociety(int societyId) { return complaintDAO.getBySocietyId(societyId); }
    public List<Complaint> getAllComplaints() { return complaintDAO.getAll(); }
    public List<Complaint> getComplaintsForPartner(int partnerId) { return complaintDAO.getByPartnerId(partnerId); }
    public Complaint getComplaintById(int id) { return complaintDAO.getById(id); }
    public boolean updatePriority(int complaintId, String priority) { return complaintDAO.updatePriority(complaintId, priority); }
    public int countByStatus(int societyId, String status) { return complaintDAO.countByStatus(societyId, status); }
}
