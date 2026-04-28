import java.util.*;

public class PatientGenerator {
    private static final Random RANDOM = new Random(42);

    // Sample names
    private static final String[] FIRST_NAMES = { "Ryan", "Emma", "Cillian", "Scarlett", "Robert",
            "Natalie", "Chris", "Margot", "Tom", "Zendaya" };
    private static final String[] LAST_NAMES = { "Gosling", "Stone", "Murphy", "Johansson", "Downey",
            "Portman", "Evans", "Robbie", "Holland", "Coleman" };

    private static final String[] SEXES = {"M", "F"};

    public static Patient generatePatient(String id) {
    Random random = new Random();

    String name = randomName();
    int age = 18 + RANDOM.nextInt(80);
    String sex = SEXES[RANDOM.nextInt(SEXES.length)];

    Patient patient = new Patient(id, name, age, sex);

    // Create scans
    NeuroScan neuroScan = new NeuroScan(patient, "N." + id, "04/13/2029");
    PelvicScan pelvicScan = new PelvicScan(patient, "P." + id, "04/13/2029");
    MuscSkelScan muscSkelScan = new MuscSkelScan(patient, "M." + id, "04/13/2029");
    TorsoScan torsoScan = new TorsoScan(patient, "T." + id, "04/13/2029");

    // Randomly add scans
    if (random.nextBoolean()) {
        fillScan(neuroScan, "neuro");
        neuroScan.analyze();
        patient.addScan(neuroScan);
    }

    if (random.nextBoolean()) {
        fillScan(pelvicScan, "pelvic");
        pelvicScan.analyze();
        patient.addScan(pelvicScan);
    }

    if (random.nextBoolean()) {
        fillScan(muscSkelScan, "musculoskeletal");
        muscSkelScan.analyze();
        patient.addScan(muscSkelScan);
    }

    if (random.nextBoolean()) {
        fillScan(torsoScan, "torso");
        torsoScan.analyze();
        patient.addScan(torsoScan);
    }

    // Ensure at least one scan exists
    if (patient.getScans().isEmpty()) {
        fillScan(neuroScan, "neuro");
        neuroScan.analyze();
        patient.addScan(neuroScan);
    }

    return patient;
}


    public static ArrayList<Patient> generatePatients(int count) {
        ArrayList<Patient> patients = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            patients.add(generatePatient("P" + i));
        }

        return patients;
    }

    private static String randomName() {
        String firstName = FIRST_NAMES[RANDOM.nextInt(FIRST_NAMES.length)];
        String lastName = LAST_NAMES[RANDOM.nextInt(LAST_NAMES.length)];
        return firstName + " " + lastName;
    }

    private static void fillScan(MedicalScan scan, String type) {
        ArrayList<Voxel> voxels = VoxelGenerator.generateScan(type, true);
        for (Voxel voxel : voxels) {
            scan.addVoxel(voxel);
        }
    }
}
