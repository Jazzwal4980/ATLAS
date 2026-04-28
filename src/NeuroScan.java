public class NeuroScan extends MedicalScan {
    public NeuroScan(Patient patient, String scanId, String scanDate) {
        super(patient, scanId, scanDate, "Brain");
    }

    @Override
    public double calculateDangerScore(Voxel voxel) {
        double score = 0.0;

        double choTcr = voxel.calculateRatio(voxel.getCho(), voxel.getTcr());
        double naaTcr = voxel.calculateRatio(voxel.getNaa(), voxel.getTcr());

        //Thresholds based on typical neuro scan values and clinical significance
        if (choTcr > 2.0) {
            score += 7.0;
        } else if (choTcr >= 1.5) {
            score += 4.0;
        }

        if (naaTcr < 1.0) {
            score += 7.0;
        } else if (naaTcr <= 1.2) {
            score += 4.0;
        }

        return Math.min(score, 10.0);
    }

    @Override
    public String explainVoxel(Voxel voxel) {
        double choTcr = voxel.calculateRatio(voxel.getCho(), voxel.getTcr());
        double naaTcr = voxel.calculateRatio(voxel.getNaa(), voxel.getTcr());

        StringBuilder explanation = new StringBuilder();
        if (choTcr > 2.0) {
            explanation.append("Cho/tCr HIGH (> 2.0, indicates malignancy, possible tumor presence)\n");
        } else if (choTcr >= 1.5) {
            explanation.append("Cho/tCr ELEVATED (≥ 1.5, potential abnormality, further evaluation needed)\n");
        }

        if (naaTcr < 1.0) {
            explanation.append("NAA/tCr VERY LOW (< 1.0, indicates severe neuronal loss or dysfunction)\n");
        } else if (naaTcr < 1.2) {
            explanation.append("NAA/tCr REDUCED (≤ 1.2, suggested neuronal compromise, further evaluation needed)\n");
        }

        if(explanation.length() == 0) {
            return "No significant abnormalities detected.\n";
        }

        return explanation.toString().trim();
    }

    @Override
    public String analyze() {
        for (Voxel voxel : voxels) {
            voxel.setDangerScore(calculateDangerScore(voxel));
        }

        sortByDangerScore();
        buildCriticallyAbnormalVoxelList(7.0);
        buildMidAbnormalVoxelList(4.0, 7.0);
        buildTotalAbnormalVoxelList(4.0, 7.0);

        return "NeuroScan analysis complete. Total voxels: " 
            + voxels.size() 
            + ", Critically abnormal voxels: " 
            + critAbnormalVoxelList.size()
            + ", Mid abnormal voxels: "
            + midAbnormalVoxelList.size()
            + ", Total abnormal voxels: "
            + totalAbnormalVoxelList.size();
    }
}