package com.belk.alert;


import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.AlertManager;
import org.openspaces.admin.alert.config.parser.XmlAlertConfigurationParser;
import org.openspaces.admin.alert.events.AlertTriggeredEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;


public class BelkGigaSpacesAlertLogback {

    private static Logger logger = LoggerFactory.getLogger("tutu");

    public static void main(String[] args) {
        Admin admin = new AdminFactory().addLocator("localhost:4174").createAdmin();
        AlertManager alertManager = admin.getAlertManager();
        alertManager.configure(new XmlAlertConfigurationParser("belk-alerts.xml").parse());
        alertManager.getAlertTriggered().add(new BelkAlertTriggeredEventListener());
    }

    private static class BelkAlertTriggeredEventListener implements AlertTriggeredEventListener{

        @Override
        public void alertTriggered(Alert alert) {

            System.out.println("ALERT");

            String alertLevel = alert.getSeverity().getName();
            String name = alert.getName();
            String componentDescription = alert.getComponentDescription();
            String longMessage = alert.getDescription();
            String hostName = alert.getProperties().get("host-name");
            String ipInfo = alert.getProperties().get("host-address");
            String status = alert.getStatus().getName();
            Date date = new Date(alert.getTimestamp());
            String threshold = alert.getConfig().get("high-threshold-perc");

            String message = "{} \n" +
                    "Host info: {} \n" +
                    "IP info: {} \n" +
                    "Alert name: {} \n" +
                    "Alert status: {} \n" +
                    "Time: {} \n" +
                    "Component Name: {} \n" +
                    "Metric Name: {} \n" +
                    "Metric Graph: {} \n" +
                    "Previous Alert State: {} \n" +
                    "Has alert state changed: {} \n" +
                    "Threshold: {} \n" +
                    "Current Value: {} \n" +
                    "Short Message: {} \n" +
                    "Long Message: {} \n";
            logger.error(message, alertLevel, hostName, ipInfo, name, status, date, componentDescription, null, null, null, null, threshold, null, null, longMessage);

            System.out.println("MAIL SENT");
        }
    }

}
