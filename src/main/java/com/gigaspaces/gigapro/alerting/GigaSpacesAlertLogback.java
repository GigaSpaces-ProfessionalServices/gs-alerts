package com.gigaspaces.gigapro.alerting;

import com.gigaspaces.gigapro.alerting.task.GSACountTask;
import org.apache.commons.cli.*;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.AlertManager;
import org.openspaces.admin.alert.config.parser.XmlAlertConfigurationParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Timer;

public class GigaSpacesAlertLogback {

    public static final String LOOKUP_LOCATORS_OPTION = "l";
    public static final String CHECK_INTERVAL_OPTION = "interval";
    public static final String USERNAME_OPTION = "username";
    public static final String PASSWORD_OPTION = "password";
    public static final String COMMAND_LINE_NAME = "gs-alerting.jar";
    public static final String ALERT_CONFIGURATION = "alert";
    public static final String LOG_CONFIGURATION = "log";

    public static void main(String[] args) {

        CommandLine commandLine = buildCommandLine(args);
        if(commandLine == null || !validateArguments(commandLine)){
            return;
        }

        Logger logger = LoggerFactory.getLogger("alert-logger");

        Admin admin = createAdminApi(commandLine);

        AlertManager alertManager = admin.getAlertManager();
        alertManager.configure(new XmlAlertConfigurationParser(commandLine.getOptionValue(ALERT_CONFIGURATION)).parse());
        alertManager.getAlertTriggered().add(new AlertTriggeredEventListener(logger));

        Timer timer = new Timer();
        GSACountTask gsaCountTask = new GSACountTask(admin);
        Long checkFrequency = Long.parseLong(commandLine.getOptionValue(CHECK_INTERVAL_OPTION));
        timer.schedule(gsaCountTask, 0, checkFrequency);

    }

    private static Admin createAdminApi(CommandLine commandLine) {
        AdminFactory factory = new AdminFactory();
        factory.addLocators(commandLine.getOptionValue(LOOKUP_LOCATORS_OPTION));

        if(commandLine.hasOption(USERNAME_OPTION)){
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
            exception.printStackTrace();
            return null;
        }
    }

    private static Options buildOptions() {
        Options options = new Options();
        options.addOption(LOOKUP_LOCATORS_OPTION, true, "GigaSpaces lookup locators.");
        options.addOption(USERNAME_OPTION, true, "Username to connect to the grid. Required when grid is secured.");
        options.addOption(PASSWORD_OPTION, true, "Password to connect to the grid. Required when grid is secured.");
        options.addOption(ALERT_CONFIGURATION, true, "Configuration file for alerting threshold.");
        options.addOption(CHECK_INTERVAL_OPTION, true, "Period of GSA count check, ms.");
        options.addOption(LOG_CONFIGURATION, true, "Configuration file for logging.");
        return options;
    }

    private static boolean validateArguments(CommandLine commandLine) {
        if(areSecureOptionsValid(commandLine) && areConfigOptionsValid(commandLine)) {
            return true;
        } else {
            HelpFormatter helpFormatter = new HelpFormatter();
            Options options = buildOptions();
            helpFormatter.printHelp(COMMAND_LINE_NAME, options, true);
            return false;
        } 
    }

    private static boolean areSecureOptionsValid(CommandLine commandLine) {
        return bothSecurityOptionsSpecified(commandLine) || noneOfSecurityOptionsSpecified(commandLine);
    }

    private static boolean bothSecurityOptionsSpecified(CommandLine commandLine) {
        return commandLine.hasOption(USERNAME_OPTION) && commandLine.hasOption(PASSWORD_OPTION);
    }

    private static boolean noneOfSecurityOptionsSpecified(CommandLine commandLine) {
        return !(commandLine.hasOption(USERNAME_OPTION) || commandLine.hasOption(PASSWORD_OPTION));
    }

    private static boolean areConfigOptionsValid(CommandLine commandLine) {
        return commandLine.hasOption(CHECK_INTERVAL_OPTION) && commandLine.hasOption(ALERT_CONFIGURATION) && System.getProperty("logback.configurationFile") != null;
    }

    private static class AlertTriggeredEventListener implements org.openspaces.admin.alert.events.AlertTriggeredEventListener {

        private Logger logger;

        public AlertTriggeredEventListener(Logger logger) {
            this.logger = logger;
        }

        @Override
        public void alertTriggered(Alert alert) {
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
            logger.info(message, introduction, hostName, ipInfo, name, status, date, componentDescription, name, null, alert.getAlertUid(), null, threshold, null, null, longMessage);
        }
    }
}
