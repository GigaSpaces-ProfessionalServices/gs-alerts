package com.gigaspaces.gigapro.alerting;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;


public class SNMPTrapLogbackAppender extends AppenderBase<ILoggingEvent> {

    protected Layout<ILoggingEvent> layout;

    private String managementHost = "127.0.0.1";
    private int managementHostTrapListenPort = 162;
    private String enterpriseOID = "1.3.6.1.2.1.2.0";
    private String localIPAddress = "127.0.0.1";
    private int localTrapSendPort = 1611;
    private int genericTrapType = 6;
    private int specificTrapType = 1;

    private long appenderLoadedTime = System.currentTimeMillis();

    @Override
    protected void append(ILoggingEvent iLoggingEvent) {
        final SNMPTrapSender sender = new SNMPTrapSender(this);
        String msg = this.layout.doLayout(iLoggingEvent);
        sender.addTrapMessageVariable(msg);
        sender.sendTrap();
    }

    public long getSysUpTime() {
        return System.currentTimeMillis() - appenderLoadedTime;
    }

    public String getManagementHost() {
        return managementHost;
    }

    public void setManagementHost(String managementHost) {
        this.managementHost = managementHost;
    }

    public int getManagementHostTrapListenPort() {
        return managementHostTrapListenPort;
    }

    public void setManagementHostTrapListenPort(int managementHostTrapListenPort) {
        this.managementHostTrapListenPort = managementHostTrapListenPort;
    }

    public String getEnterpriseOID() {
        return enterpriseOID;
    }

    public void setEnterpriseOID(String enterpriseOID) {
        this.enterpriseOID = enterpriseOID;
    }

    public String getLocalIPAddress() {
        return localIPAddress;
    }

    public void setLocalIPAddress(String localIPAddress) {
        this.localIPAddress = localIPAddress;
    }

    public int getLocalTrapSendPort() {
        return localTrapSendPort;
    }

    public void setLocalTrapSendPort(int localTrapSendPort) {
        this.localTrapSendPort = localTrapSendPort;
    }

    public int getGenericTrapType() {
        return genericTrapType;
    }

    public void setGenericTrapType(int genericTrapType) {
        this.genericTrapType = genericTrapType;
    }

    public int getSpecificTrapType() {
        return specificTrapType;
    }

    public void setSpecificTrapType(int specificTrapType) {
        this.specificTrapType = specificTrapType;
    }

    public Layout<ILoggingEvent> getLayout() {
        return layout;
    }

    public void setLayout(Layout<ILoggingEvent> layout) {
        this.layout = layout;
    }
}
