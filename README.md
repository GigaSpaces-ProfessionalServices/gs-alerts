SNMP alerts
===========

This project integrates XAP alerts with SNMP traps. Each XAP alert will be intercepted by listener that will log it. There can be multiple log appenders and SNMP appender is one of them (it is used by default - please see logback.xml file). To receive alerts, you need to have an SNMP trap receiver installed and listening - please follow installation instructions.

## Ubuntu 14.04 installation instructions:

1. sudo apt-get install snmp snmpd snmptt
2. vim /etc/snmp/snmptrapd.conf and set:
disableAuthorization yes.
Also set
traphandle default /usr/sbin/snmptt
3. vim /etc/default/snmpd and set
TRAPDRUN=yes.
Also set
TRAPDOPTS='-On -Lsd -p /var/run/snmptrapd.pid'
4. vim /etc/snmp/snmptt.ini and set:
unknown\_trap\_log\_enable = 1
5. sudo service snmpd restart

## CentOS 7 installation instructions:
1. sudo yum -y install net-snmp
2. vim /etc/snmp/snmptrapd.conf and set: disableAuthorization yes
3. vim /etc/sysconfig/snmptrapd: OPTIONS="-On -Lf /var/log/snmp/snmp.log"
4. sudo mkdir /var/log/snmp
5. sudo service snmpd restart
6. sudo service snmptrapd restart

Traps are logged in the following files: /var/log/snmptt/snmptt.log (recognized traps) and /var/log/snmptt/snmpttunknown.log (unknown traps).
Traps can be defined in the /etc/snmp/snmptt.conf file.
You can test if traps are properly sent by running command:

snmptrap -v 1 -c public \<SNMP_trap_machine_IP_address\> "" "" 1 1  ""

and check if new entries appeared in /var/log/snmptt/snmptt.log or /var/log/snmptt/snmpttunknown.log.

## Execution

To execute the application the command is as follows:

java -jar gs-alerting.jar -alert \<path-to-alert-xml-configuration\> -log \<path-to-logback-configuration\> -l \<lookup-service\> -secure -username \<username\> -password \<password\>

## Example

To start sending traps, (without space authentication):
java -jar gs-alerting.jar -alert sample-alerts.xml -log src/main/resources/logback.xml -l localhost:10098

## Remarks

1. You might want to change sample-alerts.xml file to define lower alert bounds to easier observe alerts.
2. Logback configuration: change ManagementHost in the logback.xml file to a machine that has a SNMP trap receiver installed.
