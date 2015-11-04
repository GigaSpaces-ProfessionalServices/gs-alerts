package com.gigaspaces.gigapro.alerting;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.opennms.protocols.snmp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SNMPTrapSender {
    public static final int TRAP_VERSION = SnmpSMI.SNMPV1;

    public static final String XAP_ALERT_MSG_OID = "1.2.3.3.25.29.3";
    
    public static final String XAP_COMMUNITY = "XAP-Events";

    private static final Logger logger = LoggerFactory.getLogger(SNMPTrapSender.class);

    private final Queue<String> trapQueue = new ConcurrentLinkedQueue<String>();

    private String managementHost;
    private int managementHostTrapListenPort;
    private String enterpriseOID;
    private String localIPAddress;
    private int localTrapSendPort;
    private int genericTrapType;
    private int specificTrapType;
    private long sysUpTime;

    public SNMPTrapSender(final SNMPTrapLogbackAppender appender) {
        trapQueue.clear();
        
        managementHost = appender.getManagementHost();
        managementHostTrapListenPort = appender.getManagementHostTrapListenPort();
        enterpriseOID = appender.getEnterpriseOID();
        localIPAddress = appender.getLocalIPAddress();
        localTrapSendPort = appender.getLocalTrapSendPort();
        sysUpTime = appender.getSysUpTime();
        genericTrapType = appender.getGenericTrapType();
        specificTrapType = appender.getSpecificTrapType();
    }

    public void addTrapMessageVariable(final String value) {
        trapQueue.add(value);
    }

    public void sendTrap() {
        String trapVal = trapQueue.poll();
        if (trapVal == null) {
            return;
        }

        SnmpTrapSession session = null;
        try {
            session = new SnmpTrapSession(new NopSnmpTrapHandler(), localTrapSendPort);
            final SnmpPeer peer = createSnmpPeer();
            SnmpPduTrap pdu = createPDU(trapVal);
            session.send(peer, pdu);
        } catch (Exception e) {
            logger.error("Unknown exception during sending SNMP trap.", e);
        } finally {
            if (null != session && !session.isClosed()) {
                session.close();
            }
        }
    }
    
    private SnmpPduTrap createPDU(String trapVal) throws UnknownHostException {
        SnmpPduTrap pdu = new SnmpPduTrap();
        final SnmpObjectId oid = new SnmpObjectId(XAP_ALERT_MSG_OID);
        final SnmpOctetString msg = new SnmpOctetString();
        msg.setString(trapVal);
        pdu.addVarBind(new SnmpVarBind(oid, msg));

        final SnmpOctetString addr = new SnmpOctetString();
        addr.setString(InetAddress.getByName(localIPAddress).getAddress());
        final SnmpIPAddress ipAddr = new SnmpIPAddress(addr);
        pdu.setAgentAddress(ipAddr);
        
        pdu.setEnterprise(enterpriseOID);
        pdu.setGeneric(genericTrapType);
        pdu.setSpecific(specificTrapType);
        pdu.setTimeStamp(sysUpTime);
        
        return pdu;
    }
    
    private SnmpPeer createSnmpPeer() throws UnknownHostException {
        final SnmpPeer peer = new SnmpPeer(InetAddress.getByName(managementHost));
        peer.setPort(managementHostTrapListenPort);
        final SnmpParameters snmpParms = new SnmpParameters();
        snmpParms.setReadCommunity(XAP_COMMUNITY);
        snmpParms.setVersion(TRAP_VERSION);
        peer.setParameters(snmpParms);
        return peer;
    }

    private static class NopSnmpTrapHandler implements SnmpTrapHandler {
        private static final Logger logger = LoggerFactory.getLogger(NopSnmpTrapHandler.class);

        public void snmpReceivedTrap(final SnmpTrapSession session,
                                     final InetAddress agent,
                                     final int port,
                                     final SnmpOctetString community,
                                     final SnmpPduPacket pdu) {
            logger.error("Handling traps not supported.");
        }

        public void snmpReceivedTrap(final SnmpTrapSession session,
                                     final InetAddress agent,
                                     final int port,
                                     final SnmpOctetString community,
                                     final SnmpPduTrap pdu) {
            logger.error("Handling traps not supported.");
        }

        public void snmpTrapSessionError(final SnmpTrapSession session,
                                         final int error,
                                         final Object ref) {
            logger.error("Handling trap session error not supported.");
        }
    }
}
