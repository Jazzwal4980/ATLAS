import java.util.*;

public class Main {
    public static void main(String[] args) {

        TriageSystem triageSystem = new TriageSystem();
        Scanner scnr = new Scanner(System.in);

        boolean running = true;

        System.out.println("Initializing ATLAS System... Enter number of treatment rooms...");
        int brain = getValidInt(scnr, "Enter number of NeuroRooms (Brain Treatment): ");
        int pelvic = getValidInt(scnr, "Enter number of PelvicRoom (Pelvic Treatment): ");
        int musc = getValidInt(scnr, "Enter number of OrthoRooms (Musculoskeletal Treatment): ");
        int torso = getValidInt(scnr, "Enter number of TraumaRooms (Torso Treatment): ");
        triageSystem.initializeRooms(brain, pelvic, musc, torso);
        System.out.println("~~~ ATLAS ROOMS INITIALIZED ~~~");
        while (running) {
            System.out.println("\n=== ATLAS: Automated Triage for Labelless Abnormal Signatures ===");
            System.out.println("1. Generate patients");
            System.out.println("2. View waiting queue");
            System.out.println("3. Modify treatment rooms");
            System.out.println("4. Fill treatment rooms");
            System.out.println("5. Apply treatment cycle");
            System.out.println("6. Display room availability");
            System.out.println("7. Display patients currently in treatment");
            System.out.println("8. Exit");
            switch (getValidInt(scnr, "Enter Selection: ")) {
                case 1:
                    generatePatients(scnr, triageSystem);
                    break;
                case 2:
                    triageSystem.printQueue();
                    break;
                case 3:
                    pickRoomType(scnr, triageSystem);
                    break;
                case 4:
                    fillTreatmentRooms(triageSystem);
                    break;
                case 5:
                    triageSystem.applyTreatmentCycle();
                    break;
                case 6:
                    triageSystem.printRoomStatus();
                    break;
                case 7:
                    triageSystem.printInTreatment();
                    break;
                case 8:
                    running = false;
                    System.out.println("Exiting ATLAS... Have a great day.");
                    break;
            }
        }

    }

    private static int getValidInt(Scanner scnr, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = scnr.nextInt();
                scnr.nextLine();
                if(value < 0) {
                    System.out.println("Invalid input. Please enter a valid number.");
                    continue;
                }
                return value;
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid number.");
                scnr.nextLine();
            }
        }
    }

    private static void generatePatients(Scanner scnr, TriageSystem triageSystem) {
        int count = getValidInt(scnr, "\nEnter number of patients to simulate: ");
        for (Patient p : PatientGenerator.generatePatients(count)) {
            triageSystem.addPatient(p);
        }
        System.out.println(count + " patients generated.");
    }

    private static void pickRoomType(Scanner scnr, TriageSystem triageSystem) {
        boolean modifying = true;

        while (modifying) {
            System.out.println("\n=== MODIFY TREATMENT ROOMS ===");
            System.out.println("Choose room type to modify:");
            System.out.println("1. NeuroRoom (Brain)");
            System.out.println("2. PelvicRoom (Pelvic)");
            System.out.println("3. OrthoRoom (Musculoskeletal)");
            System.out.println("4. TraumaRoom (Torso)");
            System.out.println("5. BACK TO MAIN MENU");

            int choice = getValidInt(scnr,"Enter Choice:");
            scnr.nextLine(); // flush newline

            switch (choice) {
                case 1:
                    modifyRoomType("Brain", scnr, triageSystem);
                    break;
                case 2:
                    modifyRoomType("Pelvic", scnr, triageSystem);
                    break;
                case 3:
                    modifyRoomType("Musculoskeletal", scnr, triageSystem);
                    break;
                case 4:
                    modifyRoomType("Torso", scnr, triageSystem);
                    break;
                case 5:
                    modifying = false;
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void modifyRoomType(String roomType, Scanner scnr, TriageSystem triageSystem) {
        System.out.println("\nModifying: " + roomType);
        System.out.print("Add or remove rooms? (+ / -): ");
        String op = scnr.nextLine().trim();

        int count = getValidInt(scnr, "How many rooms? ");
        scnr.nextLine();
        if (op.equals("+")) {
            triageSystem.addRoom(roomType, count);
            System.out.println("Added " + count + " " + roomType + " treatment rooms. " + triageSystem.getTotalRooms(roomType) + " total " + roomType + " treatment rooms in system.");
        } else if (op.equals("-")) {
            triageSystem.removeRoom(roomType, count);
            System.out.println("Removed " + count + " " + roomType + " treatment rooms. " + triageSystem.getTotalRooms(roomType) + " total " + roomType + " treatment rooms in system.");
        } else {
            System.out.println("Invalid operation.");
        }
    }

    private static void fillTreatmentRooms(TriageSystem triageSystem) {
        System.out.println("=== Filling Treatment Rooms... ===");

        Set<String> fullCategories = new HashSet<>();// stores categories that are full
        List<Patient> buffer = new ArrayList<>();// patients in full categories

        Patient p;

        while ((p = triageSystem.getNext()) != null) {
            String category = p.getCriticalScanRegion().getBodyRegion(); //get region that requires treatment

            if (!fullCategories.contains(category) && triageSystem.assignToRoom(category)) { //if the fullCategories set doesn't have the region being treated and there is an empty room for the region, continue
                triageSystem.getCurrentlyInTreatment().add(p); //adds to patient to inTreatment List
                p.setAssignedRoomType(category);
                System.out.println(p.getName() + " assigned to " + category + " treatment room.");
            } else {
                // mark category as full if assignment failed
                if (!fullCategories.contains(category)) {
                    fullCategories.add(category);

                    int waiting = triageSystem.countPatientsWaitingFor(category);
                    System.out.println(category + " Treatment Rooms at capacity (" + waiting + " waiting)"); //report capacity and current waiting
                }

                buffer.add(p); //store for later
            }
        }

        //Add everyone back (priority queue restores priority automatically)
        for (Patient patient : buffer) {
            triageSystem.addPatient(patient);
        }
        triageSystem.printRoomStatus();
    }
}