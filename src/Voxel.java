public class Voxel {
    private double cho;
    private double naa;
    private double tcr;
    private double pdff;
    private double ph;
    private double cit;
    private double pcr;
    private double pi;
    private double imcl;
    private double cr;
    private double dangerScore;

    private int x;
    private int y;
    private int z;
    private String region;

    public Voxel(int x, int y, int z, double cho, double naa, double tcr, double pdff, double ph, double cit, double pcr, double pi, double imcl, double cr) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.cho = cho;
        this.naa = naa;
        this.tcr = tcr;
        this.pdff = pdff;
        this.ph = ph;
        this.cit = cit;
        this.pcr = pcr;
        this.pi = pi;
        this.imcl = imcl;
        this.cr = cr;
    }

    public double getCho() {
        return cho;
    }

    public double getNaa() {
        return naa;
    }

    public double getTcr() {
        return tcr;
    }

    public double getPdff() {
        return pdff;
    }

    public double getPh() {
        return ph;
    }

    public double getCit() {
        return cit;
    }

    public double getPcr() {
        return pcr;
    }

    public double getPi() {
        return pi;
    }

    public double getImcl() {
        return imcl;
    }

    public double getCr() {
        return cr;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public double getDangerScore() {
        return dangerScore;
    }

    public String getRegion() {
        return region;
    }

    public void setDangerScore(double score) {
        this.dangerScore = score;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public double calculateRatio(double numerator, double denominator) {
        if (denominator == 0) {
            return 0.0; // Avoid division by zero
        }
        return numerator / denominator;
    }

    public String toString() {
        return String.format("Voxel(%d, %d, %d) in %s - Cho: %.2f, Naa: %.2f, TCr: %.2f, PDFF: %.2f, pH: %.2f, Cit: %.2f, PCR: %.2f, Pi: %.2f, ImCL: %.2f, Cr: %.2f, Danger Score: %.2f",
                x, y, z, region, cho, naa, tcr, pdff, ph, cit, pcr, pi, imcl, cr, dangerScore);
    }
}