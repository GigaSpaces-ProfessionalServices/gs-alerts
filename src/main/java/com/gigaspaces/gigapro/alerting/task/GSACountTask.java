package com.gigaspaces.gigapro.alerting.task;

import com.gigaspaces.gigapro.alerting.GSACountAlert;
import org.openspaces.admin.Admin;
import org.openspaces.admin.machine.Machine;

import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

public class GSACountTask extends TimerTask {

    private Map<String, Integer> gsaCount = new HashMap<>();

    private Admin admin;

    public GSACountTask(Admin admin) {
        this.admin = admin;
    }

    @Override
    public void run() {
        System.out.println("Run GSA count task");
        System.out.println("gsaCount size = " + gsaCount.size());
        System.out.println("machines = " + admin.getMachines().getMachines().length);
        for (Machine machine : admin.getMachines().getMachines()){
            String hostAddress = machine.getHostAddress();
            int currentGsaCount = machine.getGridServiceAgents().getAgents().length;
            System.out.println(machine.getHostAddress() + " " + currentGsaCount);
            Integer previousGsaCount = gsaCount.get(hostAddress);
            if (previousGsaCount != null && previousGsaCount > currentGsaCount){
                String machineUid = machine.getUid();
                String hostname = machine.getHostName();
                String ipAddress = machine.getHostAddress();
                String description = String.format("GSA count decreased from %d to %d on machine %s", previousGsaCount, currentGsaCount, hostname);
                GSACountAlert gsaCountAlert = new GSACountAlert(machineUid, hostname, ipAddress, description);
                admin.getAlertManager().triggerAlert(gsaCountAlert);
            }
            gsaCount.put(hostAddress, currentGsaCount);
        }
    }
}
