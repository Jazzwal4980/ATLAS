import java.util.*;

public class VoxelGenerator {
    private static final Random RANDOM = new Random(); // non-fixed for random outcome

    // Helper class to represent coordinates (x,y,z) and distance from abnormal seed
    // (depth)
    private static class Cell {
        final int x, y, z, depth;

        Cell(int x, int y, int z, int depth) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.depth = depth;
        }
    }

    // Generator method
    public static ArrayList<Voxel> generateScan(String scanType, boolean includeAbnormalCluster) {
        //Bounding box dimensions (4-7 in each dimension)
        int xMax = 4 + RANDOM.nextInt(4); 
        int yMax = 4 + RANDOM.nextInt(4);
        int zMax = 4 + RANDOM.nextInt(4);
        int totalVoxels = xMax * yMax * zMax;

        //Determine number of abnormal voxels to generate (0 to half of total voxels)
        int abnormalCount = includeAbnormalCluster ? 1 + RANDOM.nextInt(Math.max(1, totalVoxels / 2)) : 0;
    
        //Generate Map of abnormal voxel coordinates and their depth from the seed
        Map<String, Integer> abnormalDepths = includeAbnormalCluster ? buildAbnormalCluster(xMax, yMax, zMax, abnormalCount) : new HashMap<>();
    
        //Determine maximum depth from the abnormal seed (creating a cluster scaling abnormality based on distance)
        int maxDepth = 0;
        for (int depth : abnormalDepths.values()) {
            if (depth > maxDepth) {
                maxDepth = depth;
            }
        }
        
        ArrayList<Voxel> voxels = new ArrayList<>();
        for (int x = 0; x < xMax; x++) { //Nested loops to fill entire scan volume
            for (int y = 0; y < yMax; y++) {
                for (int z = 0; z < zMax; z++) {
                    String key = key(x, y, z);
                    //checks if current voxel is part of the abnormal cluster and gets its depth
                    Integer depth = abnormalDepths.get(key);
                    boolean abnormal = depth != null;
                    double danger = 0.0;

                    //Generate abnormal voxel severity based on distance from the seed (closer to seed = more abnormal) 
                    if (abnormal) {
                        danger = (maxDepth == 0) 
                        ? 0.7 + RANDOM.nextDouble() * 0.3 //seed severity between 0.7-1.0 : 1.0 - ((double)depth / maxDepth); 
                        : 1.0 - ((double) depth / (maxDepth + 1)); //+1 to avoid division by zero when maxDepth is 0 (single abnormal voxel)   
                    }

                    Voxel voxel = buildVoxel(scanType, x, y, z, danger, abnormal);
                    voxels.add(voxel);
                }
            }
        }

        return voxels;
    }

    // Helper method to build normal voxel value scan
    public static ArrayList<Voxel> generateNormalScan(String scanType) {
        return generateScan(scanType, false);
    }

    // Helper method to build abnormal voxel value scan
    public static ArrayList<Voxel> generateAbnormalScan(String scanType) {
        return generateScan(scanType, true);
    }

    private static Map<String, Integer> buildAbnormalCluster(int xMax, int yMax, int zMax, int abnormalCount) {
        // Setting up BFS
        Map<String, Integer> depths = new HashMap<>(); // Stores coordinates of abnormal voxels and their depth from the
                                                       // seed
        Set<String> queued = new HashSet<>(); // Track which cells have been queued for processing to avoid duplicates
        ArrayDeque<Cell> queue = new ArrayDeque<>(); // Queue storing cells to process in BFS manner

        // Randomly select a seed voxel for the abnormal cluster
        int seedX = RANDOM.nextInt(xMax);
        int seedY = RANDOM.nextInt(yMax);
        int seedZ = RANDOM.nextInt(zMax);

        // Add seed to queue
        queue.add(new Cell(seedX, seedY, seedZ, 0));
        queued.add(key(seedX, seedY, seedZ));

        while (!queue.isEmpty() && depths.size() < abnormalCount) {
            // Remove next cell in queue
            Cell current = queue.removeFirst();
            String currentKey = key(current.x, current.y, current.z);

            // Skip if processed
            if (depths.containsKey(currentKey)) {
                continue; // Already processed this cell
            }

            // Add voxel to cluster with depth from seed
            depths.put(currentKey, current.depth);

            ArrayList<int[]> directions = new ArrayList<>(); // 6 possible directions in 3D grid
            directions.add(new int[] { 1, 0, 0 }); // +X
            directions.add(new int[] { -1, 0, 0 }); // -X
            directions.add(new int[] { 0, 1, 0 }); // +Y
            directions.add(new int[] { 0, -1, 0 }); // -Y
            directions.add(new int[] { 0, 0, 1 }); // +Z
            directions.add(new int[] { 0, 0, -1 }); // -Z

            // Randomize direction order for organic cluster growth
            Collections.shuffle(directions, RANDOM);

            // Explore connected neighbors
            for (int[] dir : directions) {
                int dx = current.x + dir[0];
                int dy = current.y + dir[1];
                int dz = current.z + dir[2];

                // Check bounds
                if (inBounds(dx, dy, dz, xMax, yMax, zMax)) {
                    String neighborKey = key(dx, dy, dz);

                    // Add to queue if not visited
                    if (!depths.containsKey(neighborKey) && queued.add(neighborKey)) {
                        queue.addLast(new Cell(dx, dy, dz, current.depth + 1));
                    }
                }
            }
        }

        return depths;
    }

    // Choose correct voxel builder based on scan type
    private static Voxel buildVoxel(String scanType, int x, int y, int z, double severity, boolean abnormal) {
        switch (scanType.toLowerCase()) {
            case "neuro":
                return buildNeuroVoxel(x, y, z, severity);
            case "pelvic":
                return buildPelvicVoxel(x, y, z, severity);
            case "musculoskeletal":
                return buildMuscSkelVoxel(x, y, z, severity);
            case "torso":
                return buildTorsoVoxel(x, y, z, severity);
            default:
                throw new IllegalArgumentException("Unknown scan type: " + scanType);
        }
    }

    // NeuroScan voxel generation with Cho, Naa, Tcr values based on severity
    private static Voxel buildNeuroVoxel(int x, int y, int z, double severity) {
        double cho = lerp(1.2, 2.8, severity); // Cho increases with severity
        double naa = lerp(1.3, 0.6, severity); // Naa decreases with severity
        double tcr = 1.0; // Tcr remains constant

        Voxel v = new Voxel(x, y, z, cho, naa, tcr, 0, 7.0, 0, 0, 0, 0, 1.0);
        v.setRegion("Brain");
        return v;
    }

    // PelvicScan voxel generation with Cho, Cit, and Cr values based on severity
    private static Voxel buildPelvicVoxel(int x, int y, int z, double severity) {
        double cho = lerp(1.0, 2.5, severity); // Cho increases with severity
        double cit = lerp(2.0, 0.8, severity); // Cit decreases with severity
        double cr = lerp(0.5, 1.5, severity); // Cr increases with severity

        Voxel v = new Voxel(x, y, z, cho, 0, 1.0, 0, 7.0, cit, 0, 0, 0, cr);
        v.setRegion("Pelvis");
        return v;
    }

    // Musculoskeletal scan voxel generation with Pcr, Pi, Imcl values based on
    // severity
    private static Voxel buildMuscSkelVoxel(int x, int y, int z, double severity) {
        double pcr = lerp(1.5, 0.5, severity); // Pcr decreases with severity
        double pi = lerp(0.8, 2.0, severity); // Pi increases with severity
        double imcl = lerp(1.0, 2.5, severity); // Imcl increases with severity

        Voxel v = new Voxel(x, y, z, 0, 0, 1.0, 0, 7.0, 0, pcr, pi, imcl, 1.0);
        v.setRegion("Musculoskeletal");
        return v;
    }

    // Torso scan voxel generation
    private static Voxel buildTorsoVoxel(int x, int y, int z, double severity) {
        double pdff = lerp(0.03, 0.25, severity);
        double ph = lerp(7.1, 6.5, severity);
        double imcl = lerp(0.6, 2.2, severity);

        Voxel v = new Voxel(x, y, z, 0, 0, 1.0, pdff, ph, 0, 0, 0, imcl, 1.0);
        v.setRegion("Torso");
        return v;
    }

    // Linear interpolation for smooth transition of voxel values based on severity
    private static double lerp(double normalValue, double abnormalValue, double severity) {
        return normalValue + (abnormalValue - normalValue) * severity;
    }

    // Check if coordinates are within bounds of the scan volume
    private static boolean inBounds(int x, int y, int z, int xMax, int yMax, int zMax) {
        return x >= 0 && x < xMax && y >= 0 && y < yMax && z >= 0 && z < zMax;
    }

    // Convert coordinates to a string key for map storage
    private static String key(int x, int y, int z) {
        return x + "," + y + "," + z;
    }
}