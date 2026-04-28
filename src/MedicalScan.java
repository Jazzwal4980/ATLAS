import java.util.*;

public abstract class MedicalScan {
    protected Patient patient;
    protected String scanId;
    protected String scanDate;
    protected String bodyRegion;
    protected ArrayList<Voxel> voxels;
    protected LinkedList<Voxel> midAbnormalVoxelList;
    protected LinkedList<Voxel> critAbnormalVoxelList;
    protected LinkedList<Voxel> totalAbnormalVoxelList;

    public MedicalScan(Patient patient, String scanId, String scanDate, String bodyRegion) {
        this.patient = patient; // patient for this scan
        this.scanId = scanId; // id for the scan
        this.scanDate = scanDate; // scan date in YYYY-MM-DD format
        this.bodyRegion = bodyRegion; // body region being scanned
        this.voxels = new ArrayList<>(); // ALL voxels in scan
        this.midAbnormalVoxelList = new LinkedList<>(); // only suspiciously abnormal voxels
        this.critAbnormalVoxelList = new LinkedList<>(); // only critically abnormal voxels
        this.totalAbnormalVoxelList = new LinkedList<>(); // all abnormal voxels
    }

    // Getters for ENCAPSULATION and DATA ACCESS
    public Patient getPatient() {
        return patient;
    }

    public String getScanId() {
        return scanId;
    }

    public String getScanDate() {
        return scanDate;
    }

    public String getBodyRegion() {
        return bodyRegion;
    }

    public ArrayList<Voxel> getVoxels() {
        return voxels;
    }

    public LinkedList<Voxel> getCritAbnormalVoxelList() {
        return critAbnormalVoxelList;
    }

    public LinkedList<Voxel> getMidAbnormalVoxelList() {
        return midAbnormalVoxelList;
    }

    public LinkedList<Voxel> getTotalAbnormalVoxelList() {
        return totalAbnormalVoxelList;
    }

    // ===== Basic DATA MANIPULATION

    // Add voxel to the scan's voxel list
    public void addVoxel(Voxel voxel) {
        this.voxels.add(voxel);
    }

    // Remove voxel from the scan's voxel list
    public void removeVoxel(Voxel voxel) {
        this.voxels.remove(voxel);
    }

    // Get voxel by index in array list
    public Voxel getVoxel(int index) {
        if (index >= 0 && index < voxels.size()) {
            return voxels.get(index);
        }
        return null; // Index out of bounds
    }

    // Get voxel by 3D coordinates (x, y, z)
    public Voxel getVoxel(int x, int y, int z) {
        for (Voxel voxel : voxels) {
            if (voxel.getX() == x && voxel.getY() == y && voxel.getZ() == z) {
                return voxel;
            }
        }
        return null; // Voxel not found
    }

    // MERGE SORT
    public void sortByDangerScore() {
        voxels = mergeSort(voxels);
    }

    private ArrayList<Voxel> mergeSort(ArrayList<Voxel> list) {
        // Base Case
        if (list.size() <= 1) {
            return list;
        }

        // Split list into halves
        int mid = list.size() / 2;
        ArrayList<Voxel> left = new ArrayList<>(list.subList(0, mid));
        ArrayList<Voxel> right = new ArrayList<>(list.subList(mid, list.size()));

        // Recursive sort on each half
        left = mergeSort(left);
        right = mergeSort(right);

        // Merge sorted halves
        return merge(left, right);
    }

    // Combine two sorted lists into one sorted list based on danger score
    private ArrayList<Voxel> merge(ArrayList<Voxel> left, ArrayList<Voxel> right) {
        ArrayList<Voxel> merged = new ArrayList<>();
        int i = 0, j = 0;

        // Compare elements from both lists and insert smaller danger score first
        while (i < left.size() && j < right.size()) {
            if (left.get(i).getDangerScore() <= right.get(j).getDangerScore()) {
                merged.add(left.get(i));
                i++;
            } else {
                merged.add(right.get(j));
                j++;
            }
        }
        // Add remaining elements from left list if any
        while (i < left.size()) {
            merged.add(left.get(i));
            i++;
        }
        // Add remaining elements from right list if any
        while (j < right.size()) {
            merged.add(right.get(j));
            j++;
        }
        return merged;
    }

    // BINARY SEARCH to find index of first voxel with danger score >= threshold
    public int binarySearchCutoff(double threshold) {
        int low = 0;
        int high = voxels.size() - 1;
        int result = -1;

        // Standard binary search pattern
        while (low <= high) {
            int mid = (low + high) / 2;

            if (voxels.get(mid).getDangerScore() >= threshold) {
                result = mid;
                high = mid - 1; // Search in the left half
            } else {
                low = mid + 1; // Search in the right half
            }
        }
        return result; // Index of the first voxel with danger score >= threshold, or -1 if none found
    }

    // Filter voxels to build list of abnormal voxels based on a danger score
    // threshold

    // Build list of abnormal voxels by iterating through all voxels and adding
    // those with danger score above threshold to the abnormalVoxelList
    public void buildCriticallyAbnormalVoxelList(double thresholdCrit) {
        critAbnormalVoxelList.clear(); // reset previous results

        for (Voxel voxel : voxels) {
            if (voxel.getDangerScore() >= thresholdCrit) {
                critAbnormalVoxelList.add(voxel);
            }
        }
    }

    public void buildMidAbnormalVoxelList(double thresholdMid, double thresholdCrit) {
    midAbnormalVoxelList.clear();

    for (Voxel voxel : voxels) {
        double score = voxel.getDangerScore();

        if (score >= thresholdMid && score < thresholdCrit) {
            midAbnormalVoxelList.add(voxel);
        }
    }
}
    public void buildTotalAbnormalVoxelList(double thresholdMid, double thresholdCrit) {
        totalAbnormalVoxelList.clear();

        buildCriticallyAbnormalVoxelList(thresholdCrit);
        buildMidAbnormalVoxelList(thresholdMid, thresholdCrit);

        totalAbnormalVoxelList.addAll(critAbnormalVoxelList);
        totalAbnormalVoxelList.addAll(midAbnormalVoxelList);

    }

    public Voxel getMostSevereVoxel() {
        if (voxels.isEmpty()) {
            return null;
        }
        return voxels.get(voxels.size() - 1); // Last voxel after sorting is the most severe
    }

    // Polymorphism --> Each scan type will have its own method for calculating
    // danger score based on the specific metabolites relevant to that body region,
    // and its own analysis method to process the scan data and identify
    // abnormalities.
    public abstract double calculateDangerScore(Voxel voxel);

    public abstract String explainVoxel(Voxel voxel);

    public abstract String analyze();
}