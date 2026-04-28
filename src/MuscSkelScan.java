public class MuscSkelScan extends MedicalScan {
    public MuscSkelScan(Patient patient, String scanId, String scanDate) {
        super(patient, scanId, scanDate, "Musculoskeletal");
    }

    @Override
    public double calculateDangerScore(Voxel voxel) {
        double score = 0.0;
        //Energy metabolism ratio: Pcr / Pi
        //Lower value = impaired energy metabolism :(
        double pcrPi = voxel.calculateRatio(voxel.getPcr(), voxel.getPi());
        //Fat accumulation: IMCL / Cr ratio
        //Higher value = abnormal fat accumulation :(
        double imclCr = voxel.calculateRatio(voxel.getImcl(), voxel.getCr());

        //Thresholds based on typical musculoskeletal scan values and clinical significance
        
        //lower = worse
        if (pcrPi < 1.0) {
            score += 7.0; //strong abnormal signal
        } else if (pcrPi < 2.0) {
            score += 4.0; //moderate abnormal signal
        }

        //higher = worse
        if (imclCr > 2.0) {
            score += 7.0; //strong abnormal signal
        } else if (imclCr > 1.5) {
            score += 4.0; //moderate abnormal signal
        }

        return Math.min(score, 10.0);
    }

    @Override
    public String explainVoxel(Voxel voxel) {
        double pcrPi = voxel.calculateRatio(voxel.getPcr(), voxel.getPi());
        double imclCr = voxel.calculateRatio(voxel.getImcl(), voxel.getCr());

        StringBuilder explanation = new StringBuilder();

        if (pcrPi < 1.0) {
            explanation.append("Pcr/Pi VERY LOW (< 1.0, suggests severely impaired cellular metabolism)\n");
        } else if (pcrPi < 2.0) {
            explanation.append("Pcr/Pi REDUCED (< 2.0, indicates possible metabolic dysfunction)\n");
        }

        if (imclCr > 2.0) {
            explanation.append(" IMCL/Cr HIGH (> 2.0, indicates significant fat accumulation)\n");
        } else if (imclCr > 1.5) {
            explanation.append(" IMCL/Cr ELEVATED (> 1.5, suggests increased fat deposition in muscle tissue)\n");
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

        return "MuscSkelScan analysis complete. Total voxels: " 
            + voxels.size() 
            + ", Critically abnormal voxels: " 
            + critAbnormalVoxelList.size()
            + ", Mid abnormal voxels: "
            + midAbnormalVoxelList.size()
            + ", Total abnormal voxels: "
            + totalAbnormalVoxelList.size();
    }
}