package com.gigaspaces.gigapro.alerting;

import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.AlertSeverity;
import org.openspaces.admin.alert.AlertStatus;
import org.openspaces.admin.internal.alert.InternalAlert;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

public class GSACountAlert implements InternalAlert {

    private String machineUid;

    private String hostname;

    private String ipAddress;

    private String description;

    private String alertUid;

    private Map<String, String> properties = new HashMap<>();

    public GSACountAlert(String machineUid, String hostname, String ipAddress, String description) {
        this.machineUid = machineUid;
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.description = description;
        properties.put("host-name", hostname);
        properties.put("host-address", ipAddress);
    }

    @Override
    public String getName() {
        return "GSA Count Alert";
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public long getTimestamp() {
        return System.currentTimeMillis();
    }

    @Override
    public AlertSeverity getSeverity() {
        return AlertSeverity.WARNING;
    }

    @Override
    public AlertStatus getStatus() {
        return AlertStatus.RAISED;
    }

    @Override
    public String getAlertUid() {
        return alertUid;
    }

    @Override
    public String getGroupUid() {
        return "custom";
    }

    @Override
    public String getComponentUid() {
        return machineUid;
    }

    @Override
    public String getComponentDescription() {
        return String.format("%s (%s)", hostname, ipAddress);
    }

    @Override
    public Map<String, String> getConfig() {
        return new HashMap<>();
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public void writeExternal(ObjectOutput objectOutput) throws IOException {
        objectOutput.writeObject(this);
    }

    @Override
    public void readExternal(ObjectInput objectInput) throws IOException, ClassNotFoundException {
        //TODO
    }

    @Override
    public void setAlertUid(String alertUid) {
        this.alertUid = alertUid;
    }
}
