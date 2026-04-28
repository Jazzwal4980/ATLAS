import java.util.*;

public class TriageSystem {
    // Heap to prioritize patients based on severity of findings (max danger score)
    private PriorityQueue<Patient> patientQueue = new PriorityQueue<>();
    private Map<String, Integer> totalRooms = new HashMap<>();
    private Map<String, Integer> occupiedRooms = new HashMap<>();
    private Map<String, Integer> unoccupiedRooms = new HashMap<>();
    private List<Patient> inTreatment = new ArrayList<>();

    public List<Patient> getCurrentlyInTreatment() {
        return inTreatment;
    }

    public void printInTreatment() {
        System.out.println("\n=== IN TREATMENT ===");

        PriorityQueue<Patient> tempQueue = new PriorityQueue<>(inTreatment);
        int rank = 1;
        if (tempQueue.isEmpty()) {
            System.out.println("No patients in system. Fill treatment rooms (4) to display.");
        }
        while (!tempQueue.isEmpty()) {
            Patient p = tempQueue.poll();
            System.out.println("\n-------" + rank + ". " + p.getName() + " | Total Abnormal Voxels: "
                    + p.getTotalAbnormalVoxels());
            System.out.println(p.getCriticalFinding());
            rank++;
        }
    }

    public void moveToTreatment(Patient p) {
        inTreatment.add(p);
    }

    public void removeFromTreatment(Patient p) {
        inTreatment.remove(p);
    }

    private void treatScan(MedicalScan scan) {
        LinkedList<Voxel> critAbnormal = scan.getCritAbnormalVoxelList();
        LinkedList<Voxel> midAbnormal = scan.getMidAbnormalVoxelList();

        if (critAbnormal.isEmpty() && midAbnormal.isEmpty())
            return;

        // find random number of abnormal voxels to remove
        Random rand = new Random();

        //no weightage, more difficult to remove as closer to seed/core of abnormal region
        int toRemoveCrit = critAbnormal.isEmpty() ? 0 : rand.nextInt(critAbnormal.size()) + 1;

        //higher weightage makes midAbnormal voxels treated easier
        int toRemoveMid =  midAbnormal.isEmpty() ? 0 : (int) Math.ceil(Math.pow(rand.nextDouble(), 0.5) * midAbnormal.size());

        // most critical voxels are removed first in each group
        for (int i = 0; i < toRemoveCrit && !critAbnormal.isEmpty(); i++) {
            Voxel v = critAbnormal.removeLast();
            scan.getVoxels().remove(v);
        }
        for (int i = 0; i < toRemoveMid; i++) {
            Voxel v = midAbnormal.removeLast();
            scan.getVoxels().remove(v);
        }
        scan.analyze();
        System.out.println("----" + scan.getPatient().getName() + " had " + toRemoveCrit
                + " critically abnormal voxels treated and " + toRemoveMid + " moderately abnormal voxels treated.\n"
                + scan.getCritAbnormalVoxelList().size() + " critically abnormal voxels remaining and "
                + scan.getMidAbnormalVoxelList().size() + " moderately abnormal voxels remaining.\n");
    }

    public int getTotalRooms(String roomType) {
        return totalRooms.getOrDefault(roomType, 0);
    }

    private void treatPatient(Patient p) {
        for (MedicalScan scan : p.getScans()) {
            treatScan(scan);
        }
    }

    public void applyTreatmentCycle() {
        if (inTreatment.isEmpty()) {
            System.out.println("No patients currently in treatment");
            return;
        }

        List<Patient> treated = new ArrayList<>();

        for (Patient p : inTreatment) {
            treatPatient(p);

            if (p.getTotalAbnormalVoxels() == 0) {
                treated.add(p);
            }
        }

        for (Patient p : treated) {
            // determine category or region with malignancy
            String region = p.getAssignedRoomType();

            releaseRoom(region);

            removeFromTreatment(p);

            System.out.println(p.getName() + " is fully treated and discharged!");
        }
    }

    public void releaseRoom(String roomType) {
        int occ = occupiedRooms.getOrDefault(roomType, 0);

        if (occ > 0) {
            occupiedRooms.put(roomType, occ - 1);

            int unocc = unoccupiedRooms.getOrDefault(roomType, 0);
            unoccupiedRooms.put(roomType, unocc + 1);
        }
    }

    public void printRoomStatus() {
        System.out.println("\n=== ROOM AVAILABILITY ===\n");

        for (String type : totalRooms.keySet()) {
            int total = totalRooms.get(type);
            int occ = occupiedRooms.get(type);
            int unocc = unoccupiedRooms.get(type);
            System.out.println(type + ": " + occ + "/" + total + " occupied (" + unocc + " free)");
        }
    }

    public int countPatientsWaitingFor(String region) {
        int count = 0;

        for (Patient p : patientQueue) {//NOTE THIS IS IN AN ARBITRARY ORDER AS A PRIORITY QUEUE===>DO NOT OUTPUT IN ORDER
            MedicalScan scan = p.getCriticalScanRegion(); //gets region
            if (scan != null && scan.getBodyRegion().equals(region)) {
                count++; // if theres a scan and its in the same as the region being checked then add one to wait list
            }
        }

        return count;
    }

    public boolean assignToRoom(String roomType) {
        int unocc = unoccupiedRooms.getOrDefault(roomType, 0);

        if (unocc > 0) {
            // consume one unoccupied room
            unoccupiedRooms.put(roomType, unocc - 1);
            // increase occupied count for room type
            int occ = occupiedRooms.getOrDefault(roomType, 0);
            occupiedRooms.put(roomType, occ + 1);
            return true;
        }

        return false;
    }

    public void addRoom(String roomType, int count) {
        int total = totalRooms.getOrDefault(roomType, 0);
        int unocc = unoccupiedRooms.getOrDefault(roomType, 0);

        totalRooms.put(roomType, total + count);
        unoccupiedRooms.put(roomType, unocc + count);

        // ensure occupied map has an entry
        occupiedRooms.putIfAbsent(roomType, 0);
    }

    public void removeRoom(String roomType, int count) {
        int total = totalRooms.getOrDefault(roomType, 0);
        int unocc = unoccupiedRooms.getOrDefault(roomType, 0);
        int occ = occupiedRooms.getOrDefault(roomType, 0);

        if (count > total) {
            System.out.println("Cannot remove " + count + " " + roomType +
                    " rooms because only " + total + " exist.");
            return;
        }

        if (count > unocc) {
            System.out.println("Cannot remove " + count + " " + roomType +
                    " rooms because " + occ + " are occupied.");
            return;
        }

        totalRooms.put(roomType, total - count);
        unoccupiedRooms.put(roomType, unocc - count);

        if (total - count == 0) {
            totalRooms.remove(roomType);
            unoccupiedRooms.remove(roomType);
            occupiedRooms.remove(roomType);
            System.out.println("All " + roomType + " rooms removed.");
            return;
        }

        System.out.println(count + " " + roomType + " room(s) removed.");
    }

    public void initializeRooms(int brain, int pelvis, int musc, int torso) {
        totalRooms.put("Brain", brain);
        totalRooms.put("Pelvis", pelvis);
        totalRooms.put("Musculoskeletal", musc);
        totalRooms.put("Torso", torso);

        unoccupiedRooms.put("Brain", brain);
        unoccupiedRooms.put("Pelvis", pelvis);
        unoccupiedRooms.put("Musculoskeletal", musc);
        unoccupiedRooms.put("Torso", torso);

        occupiedRooms.put("Brain", 0);
        occupiedRooms.put("Pelvis", 0);
        occupiedRooms.put("Musculoskeletal", 0);
        occupiedRooms.put("Torso", 0);
    }

    // Add patient after scan is analyzed
    public void addPatient(Patient p) {
        patientQueue.add(p);
    }

    // Peek highest priority patient (most severe) without removing
    public Patient peekNextPatient() {
        return patientQueue.peek();
    }

    // Pop highest priority patient (most severe) for treatment
    public Patient getNext() {
        return patientQueue.poll();
    }

    // Print without destroying original queue
    public void printQueue() {
        System.out.println("\n=== WAITING QUEUE ===");

        PriorityQueue<Patient> tempQueue = new PriorityQueue<>(patientQueue);
        int rank = 1;
        if (tempQueue.isEmpty()) {
            System.out.println("No patients in system. Generate patients (1) to display.");
        }
        while (!tempQueue.isEmpty()) {
            Patient p = tempQueue.poll();
            System.out.println("\n-------" + rank + ". " + p.getName() + " | Total Abnormal Voxels: "
                    + p.getTotalAbnormalVoxels() + " | " + "Critically Abnormal Voxels: " + p.getTotalCritVoxels()
                    + ", Moderately Abnormal Voxels: " + p.getTotalMidVoxels());
            System.out.println(p.getCriticalFinding());
            rank++;
        }

    }
}
