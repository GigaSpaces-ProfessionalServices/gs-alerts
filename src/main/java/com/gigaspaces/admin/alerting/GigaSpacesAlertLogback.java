package com.gigaspaces.admin.alerting;

import org.apache.commons.cli.*;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.AlertManager;
import org.openspaces.admin.alert.config.AlertConfiguration;
import org.openspaces.admin.alert.config.parser.XmlAlertConfigurationParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

public class GigaSpacesAlertLogback {

    public static final String LOOKUP_LOCATORS_OPTION = "l";
    public static final String SECURE_SPACE_OPTION = "secure";
    public static final String USERNAME_OPTION = "username";
    public static final String PASSWORD_OPTION = "password";
    public static final String COMMAND_LINE_NAME = "gs-alerting.jar";
    public static final String ALERT_CONFIGURATION = "alert";
    public static final String LOG_CONFIGURATION = "log";

    public static void main(String[] args) {

        CommandLine commandLine = buildCommandLine(args);

        if(!validateArguments(commandLine)){
            return;
        }

        Logger logger = LoggerFactory.getLogger("snmp-logger");

        Admin admin = createAdminApi(commandLine);

        XmlAlertConfigurationParser xmlParser = new XmlAlertConfigurationParser(commandLine.getOptionValue(ALERT_CONFIGURATION));
        AlertConfiguration[] acs = xmlParser.parse();
        for (AlertConfiguration ac: acs) {
            Map<String, String> props = ac.getProperties();
        }

        AlertManager alertManager = admin.getAlertManager();
        alertManager.configure(new XmlAlertConfigurationParser(commandLine.getOptionValue(ALERT_CONFIGURATION)).parse());
        alertManager.getAlertTriggered().add(new AlertTriggeredEventListener(logger));
        System.out.println("Alert listener installed");
    }

    private static Admin createAdminApi(CommandLine commandLine) {
        AdminFactory factory = new AdminFactory();
        factory.addLocators(commandLine.getOptionValue(LOOKUP_LOCATORS_OPTION));

        if(commandLine.hasOption(SECURE_SPACE_OPTION)){
            factory.credentials(commandLine.getOptionValue(USERNAME_OPTION), commandLine.getOptionValue(PASSWORD_OPTION));
        }

        return factory.createAdmin();
    }

    private static CommandLine buildCommandLine(String[] args) {
        CommandLineParser parser = new BasicParser();
        Options options = buildOptions();
        try {
            return parser.parse(options, args);
        } catch (ParseException exception) {
            System.out.println(exception.getStackTrace());
            return null;
        }
    }

    private static Options buildOptions() {
        Options options = new Options();
        options.addOption(LOOKUP_LOCATORS_OPTION, true, "GigaSpaces lookup locators.");
        options.addOption(SECURE_SPACE_OPTION, false, "Connecting to a secure grid.");
        options.addOption(USERNAME_OPTION, true, "Username to connect to the grid. Required when grid is secured.");
        options.addOption(PASSWORD_OPTION, true, "Password to connect to the grid. Required when grid is secured.");
        options.addOption(ALERT_CONFIGURATION, true, "Configuration file for alerting threshold.");
        options.addOption(LOG_CONFIGURATION, true, "Configuration file for logging.");
        return options;
    }

    private static boolean validateArguments(CommandLine commandLine) {
        if(commandLine == null) {
            return false;
        }
        
        boolean output = true;
        Options options = buildOptions();

        if(commandLine.hasOption(SECURE_SPACE_OPTION)
                && !(commandLine.hasOption(USERNAME_OPTION) && commandLine.hasOption(PASSWORD_OPTION))){
            output = false;
        }

        if(output && !(commandLine.hasOption(ALERT_CONFIGURATION) && commandLine.hasOption(LOG_CONFIGURATION))){
            output = false;
        }

        if(!output){
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp(COMMAND_LINE_NAME, options, true);
        }

        return output;
    }

    private static class AlertTriggeredEventListener implements org.openspaces.admin.alert.events.AlertTriggeredEventListener {

        private Logger logger;

        public AlertTriggeredEventListener(Logger logger) {
            this.logger = logger;
        }

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

            String introduction = String.format("%s %s", alertLevel, name);
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
            logger.error(message, introduction, hostName, ipInfo, name, status, date, componentDescription, name, null, alert.getAlertUid(), null, threshold, null, null, longMessage);

            System.out.println("MAIL SENT");
        }
    }

}
