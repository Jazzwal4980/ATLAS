public class TorsoScan extends MedicalScan {
    public TorsoScan(Patient patient, String scanId, String scanDate) {
        super(patient, scanId, scanDate, "Torso");
    }

    @Override
    public double calculateDangerScore(Voxel voxel) {
        double score = 0.0;

       //fat percentage (PDFF)
       double pdff = voxel.getPdff();
       //pH level
       double ph = voxel.getPh();

        //Thresholds based on typical torso scan values and clinical significance
        //higher = worse
        if (pdff > 0.20) {
            score += 7.0; //strong abnormal signal
        } else if (pdff >= 0.05) {
            score += 4.0; //moderate abnormal signal
        }

        //lower = worse
        if (ph < 6.8) {
            score += 7.0; //strong abnormal signal
        } else if (ph < 7.0) {
            score += 4.0; //moderate abnormal signal
        }

        return Math.min(score, 10.0);
    }

    @Override
    public String explainVoxel(Voxel voxel) {
        double pdff = voxel.getPdff();
        double ph = voxel.getPh();

        StringBuilder explanation = new StringBuilder();
        if (pdff > 0.20) {
            explanation.append("PDFF VERY HIGH (> 20%, indicates severe abnormal fat accumulation)\n");
        } else if (pdff >= 0.05) {
            explanation.append("PDFF ELEVATED (5-20%, suggests abnormal lipid deposition)\n");
        }

        if (ph < 6.8) {
            explanation.append("pH LOW (< 6.8, indicates significant acidity and possible hypoxic stress)\n");
        } else if (ph < 7.0) {
            explanation.append("pH SLIGHTLY LOW ( < 7.0, indicates mild metabolic imbalance, further evaluation needed)\n");
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

        sortByDangerScore();
        buildCriticallyAbnormalVoxelList(7.0);
        buildMidAbnormalVoxelList(4.0, 7.0);
        buildTotalAbnormalVoxelList(4.0, 7.0);

        return "TorsoScan analysis complete. Total voxels: " 
            + voxels.size() 
            + ", Critically abnormal voxels: " 
            + critAbnormalVoxelList.size()
            + ", Mid abnormal voxels: "
            + midAbnormalVoxelList.size()
            + ", Total abnormal voxels: "
            + totalAbnormalVoxelList.size();
    }
}