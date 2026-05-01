public class PelvicScan extends MedicalScan {
    public PelvicScan(Patient patient, String scanId, String scanDate) {
        super(patient, scanId, scanDate, "Pelvis");
    }

    @Override
    public double calculateDangerScore(Voxel voxel) {
        double score = 0.0;

        //(Cho + Cr) / Cit ratio is a common metric in pelvic scans for identifying abnormalities
        double chocrCit = voxel.calculateRatio(voxel.getCho() + voxel.getCr(), voxel.getCit());

        //Thresholds: >= 0.6 -> suspicious, >= 0.8 high probability malignancy
        if (chocrCit >= 0.8) {
            score += 7.0; //strong abnormal signal
        } else if (chocrCit >= 0.6) {
            score += 4.0; //moderate abnormal signal
        }
        return Math.min(score, 10.0);
    }

    @Override
    public String explainVoxel(Voxel voxel) {
        double chocrCit = voxel.calculateRatio(voxel.getCho() + voxel.getCr(), voxel.getCit());

        StringBuilder explanation = new StringBuilder();
        if (chocrCit >= 0.8) {
            explanation.append("(Cho+Cr)/Cit HIGH (>= 0.8, elevated choline consistent with high liklihood of malignancy)\n");
        } else if (chocrCit >= 0.6) {
            explanation.append("(Cho+Cr)/Cit ELEVATED (>= 0.6, suggests suspicious metabolic activity)\n");
        }

        if(explanation.length() == 0) {
            return "No significant abnormalities detected.\n";
        }

        return explanation.toString().trim();
    }
    @Override
    public String analyze() {
        //Compute danger scores for all voxels
        for (Voxel voxel : voxels) {
            voxel.setDangerScore(calculateDangerScore(voxel));
        }

        //Sort voxels from least -> most dangerous
        sortByDangerScore();
        buildCriticallyAbnormalVoxelList(7.0);
        buildMidAbnormalVoxelList(4.0, 7.0);
        buildTotalAbnormalVoxelList(4.0, 7.0);


        //Return summary of analysis results
        return "PelvicScan analysis complete. Total voxels: " 
            + voxels.size() 
            + ", Critically abnormal voxels: " 
            + critAbnormalVoxelList.size()
            + ", Mid abnormal voxels: "
            + midAbnormalVoxelList.size()
            + ", Total abnormal voxels: "
            + totalAbnormalVoxelList.size();
    }
}