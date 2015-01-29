belk-alerts
===========

* Emails are sent using the logging API.

To execute the application the command is as follows:

java -jar BelkAlerts.jar \<lookup-service\> \<path-to-alert-xml-configuration\> \<username\> \<password\>

* SNMP traps environment set up instructions for Ubuntu 14.04:

1. sudo apt-get install snmp snmpd snmptt
2. vim /etc/snmp/snmptrapd.conf and set: 
disableAuthorization yes
traphandle default /usr/sbin/snmptt
3. vim /etc/default/snmpd and set:
TRAPDRUN=yes
TRAPDOPTS='-On -Lsd -p /var/run/snmptrapd.pid'
4. vim /etc/snmp/snmptt.ini and set:
unknown\_trap\_log\_enable = 1
5. sudo service snmpd restart

Now you should have SNMP trap receiver working. Traps are logged in the following files: /var/log/snmptt/snmptt.log (recognized traps) and /var/log/snmptt/snmpttunknown.log (unknown traps). 

Traps can be defined in the /etc/snmp/snmptt.conf file.

To start sending traps, put the following arguments in the command line -alert sample-alerts.xml -log src/main/resources/logback.xml -l localhost:10098
You might want to change sample-alerts.xml file to define lower alert bounds to easier observer alerts.

Logback configuration: change ManagementHost in the logback.xml file to machine that has SNMP trap receiver installed.
