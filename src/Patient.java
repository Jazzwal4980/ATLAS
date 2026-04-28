import java.util.*;

public class Patient implements Comparable<Patient> {
    private String id;
    private String name;
    private int age;
    private String sex;
    private ArrayList<MedicalScan> scans;

    public Patient(String id, String name, int age, String sex) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.sex = sex;
        this.scans = new ArrayList<>();
    }

    // GETTERS
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getSex() {
        return sex;
    }

    public ArrayList<MedicalScan> getScans() {
        return scans;
    }

    public void addScan(MedicalScan scan) {
        this.scans.add(scan);
    }

    //Maybe add an age severity multiplier to increase priority based on patient age
    // private double ageMultiplier() {
    //     if (age < 30) {
    //         return 0.8;
    //     } else if (age < 50) {
    //         return 1.0;
    //     } else if (age < 70) {
    //         return 1.2;
    //     } else {
    //         return 1.5;
    //     }
    // }

    // Patient-level severity (highest danger score across all scans)
    public double getMaxDangerScore() {
        double maxScore = 0.0;
        for (MedicalScan scan : scans) {
            for (Voxel v : scan.getVoxels()) {
                if (v.getDangerScore() > maxScore) {
                    maxScore = v.getDangerScore();
                }
            }
        }
        return maxScore;
    }

    // Total abnormal voxels across all scans
    public int getTotalAbnormalVoxels() {
        int total = 0;
        for (MedicalScan scan : scans) {
            total += scan.getCritAbnormalVoxelList().size() + scan.getMidAbnormalVoxelList().size();
        }
        return total;
    }

    public int getTotalCritVoxels() {
        int total = 0;
        for (MedicalScan scan : scans) {
            total += scan.getCritAbnormalVoxelList().size();
        }
        return total;
    }

    public int getTotalMidVoxels() {
        int total = 0;
        for (MedicalScan scan : scans) {
            total += scan.getMidAbnormalVoxelList().size();
        }
        return total;
    }

    public MedicalScan getCriticalScanRegion() {
        MedicalScan criticalScan = null;
        double maxScore = 0.0;

        for (MedicalScan scan : scans) {
            for (Voxel v : scan.getVoxels()) {
                if (v.getDangerScore() > maxScore) {
                    maxScore = v.getDangerScore();
                    criticalScan = scan;
                }
            }
        }

        return criticalScan;
    }

    public String getCriticalFinding() {
        double maxScore = 0.0;
        Voxel criticalVoxel = null;
        MedicalScan criticalScan = null;

        for (MedicalScan scan : scans) {
            Voxel v = scan.getMostSevereVoxel();
            if (v != null && v.getDangerScore() > maxScore) {
                maxScore = v.getDangerScore();
                criticalVoxel = v;
                criticalScan = scan;
            }
        }

        if (criticalVoxel == null) {
            return "No abnormal findings.";
        }

        return "Max Danger Score: " + maxScore + "\nAbnormal Region: " + criticalScan.getBodyRegion()
                + " | Core Abnormal Voxel Coordinates: ("
                + criticalVoxel.getX() + ", " + criticalVoxel.getY() + ", " + criticalVoxel.getZ() + ")"
                + "\n-------Findings:\n" + criticalScan.explainVoxel(criticalVoxel);
    }

    @Override
    public String toString() {
        return "Patient{id='" + id + "', name='" + name + "', age='" + age + "', sex='" + sex + "'}";
    }

    @Override
    public int compareTo(Patient other) {
        int cmp = Double.compare(other.getMaxDangerScore(), this.getMaxDangerScore());
        if (cmp != 0) {
            return cmp; // Higher max danger score first
        }
        
        cmp = Integer.compare(other.getTotalAbnormalVoxels(), this.getTotalAbnormalVoxels());
        if (cmp != 0) {
            return cmp; // Based on number of abnormal voxels if max danger scores are equal
        }

        return 0; // Equal priority
    }
}
