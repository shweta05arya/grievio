package com.partnersdashboard;

import com.partnersdashboard.config.AppConfig;
import com.partnersdashboard.dao.PartnerDAO;
import com.partnersdashboard.model.Account;
import com.partnersdashboard.model.Partner;
import com.partnersdashboard.ui.PartnerDashboard;
import com.partnersdashboard.util.SessionManager;
import javafx.application.Application;
import javafx.stage.Stage;

/** Launches the partner dashboard directly without login. */
public class Main extends Application {

    @Override
    public void start(Stage stage) {
        bootstrapDemoPartnerSession();
        new PartnerDashboard(stage);
    }

    private void bootstrapDemoPartnerSession() {
        SessionManager session = SessionManager.getInstance();
        if (session.getPartner() != null) return;

        PartnerDAO dao = new PartnerDAO();
        Partner partner = dao.getByAccountId(AppConfig.DEMO_PARTNER_ACCOUNT_ID);
        if (partner != null) {
            Account account = new Account();
            account.setId(partner.getAccountId());
            account.setEmail(partner.getEmail());
            account.setRole("PARTNER");
            account.setActive(true);
            session.setAccount(account);
            session.setPartner(partner);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
