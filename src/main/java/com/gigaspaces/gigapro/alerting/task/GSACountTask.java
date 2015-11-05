package com.gigaspaces.gigapro.alerting.task;

import com.gigaspaces.gigapro.alerting.GSACountAlert;
import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.machine.Machine;

import java.util.*;

public class GSACountTask extends TimerTask {

    public static final String DISAPPEARED_GSA_MESSAGE = "GSA %s disappeared from machine %s";
    public static final String DISAPPEARED_MACHINE_MESSAGE = "Machine %s disappeared. GSA: %s";
    private Map<String, List<GSADetails>> machineToGSAs = new HashMap<>();

    private Admin admin;

    public GSACountTask(Admin admin) {
        this.admin = admin;
    }

    @Override
    public void run() {
        Set<String> previousCheckMachines = new HashSet<>(machineToGSAs.keySet());
        for (Machine machine : admin.getMachines().getMachines()){
            String machineUid = machine.getUid();
            String hostname = machine.getHostName();
            String ipAddress = machine.getHostAddress();
            String hostAddress = machine.getHostAddress();
            previousCheckMachines.remove(hostAddress);

            GridServiceAgent[] agents = machine.getGridServiceAgents().getAgents();
            int currentGsaCount = agents.length;
            List<GSADetails> previousCheckGSAs = getPreviousGSAs(hostAddress);
            List<GSADetails> currentGSAs = new ArrayList<>();
            boolean gsaCountDecreased=gsaCountDecreased(currentGsaCount, previousCheckGSAs);

            //collect existing GSAs
            for (GridServiceAgent agent : agents){
                GSADetails availableGSA = new GSADetails(agent.getUid(), agent.getExactZones().getZones(), hostname, ipAddress);
                currentGSAs.add(availableGSA);
                previousCheckGSAs.remove(availableGSA);
            }
            machineToGSAs.put(hostAddress, currentGSAs);

            // trigger alert only if GSA count decreased
            // ignore substitution of one GSA by another
            if (gsaCountDecreased){
                for (GSADetails disappearedGSA : previousCheckGSAs){
                    String description = String.format(DISAPPEARED_GSA_MESSAGE, disappearedGSA, hostname);
                    GSACountAlert gsaCountAlert = new GSACountAlert(machineUid, hostname, ipAddress, description);
                    admin.getAlertManager().triggerAlert(gsaCountAlert);
                }
            }
        }
        alertsForDisappearedMachines(previousCheckMachines);
    }

    private void alertsForDisappearedMachines(Set<String> previousMachines) {
        for (String disappearedMachine : previousMachines){
            for (GSADetails gsaDetails : machineToGSAs.get(disappearedMachine)){
                String description = String.format(DISAPPEARED_MACHINE_MESSAGE, disappearedMachine, gsaDetails);
                GSACountAlert gsaCountAlert = new GSACountAlert(disappearedMachine, gsaDetails.hostname, gsaDetails.ipAddress, description);
                admin.getAlertManager().triggerAlert(gsaCountAlert);
            }
            machineToGSAs.remove(disappearedMachine);
        }
    }

    private List<GSADetails> getPreviousGSAs(String hostAddress) {
        List<GSADetails> previousGSAs = machineToGSAs.get(hostAddress);
        return previousGSAs != null ? previousGSAs : new ArrayList<GSADetails>();
    }

    private boolean gsaCountDecreased(int currentGsaCount, List<GSADetails> previousGSAs) {
        return previousGSAs != null && previousGSAs.size() > currentGsaCount;
    }

    private static class GSADetails {

        private String gsaUid;

        private Set<String> zones;

        private String hostname;

        private String ipAddress;

        public GSADetails(String gsaUid, Set<String> zones, String hostname, String ipAddress) {
            this.gsaUid = gsaUid;
            this.zones = zones;
            this.hostname = hostname;
            this.ipAddress = ipAddress;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            GSADetails that = (GSADetails) o;

            if (!gsaUid.equals(that.gsaUid)) return false;
            if (!zones.equals(that.zones)) return false;
            if (!hostname.equals(that.hostname)) return false;
            return ipAddress.equals(that.ipAddress);

        }

        @Override
        public int hashCode() {
            int result = gsaUid.hashCode();
            result = 31 * result + zones.hashCode();
            result = 31 * result + hostname.hashCode();
            result = 31 * result + ipAddress.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return String.format("UID=%s, zones=%s, hostname=%s, ip=%s", gsaUid, Arrays.toString(zones.toArray()), hostname, ipAddress);
        }

    }

}
