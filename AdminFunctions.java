package trial;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class AdminFunctions {
    static void showAdminMenu() {
        while (true) {
            System.out.println("\n===== Admin Menu =====");
            System.out.println("1. Manage Doctors");
            System.out.println("2. Manage Patients");
            System.out.println("3. Approve Requests");
            System.out.println("4. View Appointments");
            System.out.println("5. View Logs");
            System.out.println("6. Logout");
            System.out.println("=====================");
            int menuChoice = MainScheduler.getIntegerInput("Choice: ");
            if (!handleMenuChoice(menuChoice)) {
                break;
            }
        }
    }

    static boolean handleMenuChoice(int menuChoice) {
        try {
            switch (menuChoice) {
                case 1:
                    manageDoctorRecords();
                    break;
                case 2:
                    managePatientRecords();
                    break;
                case 3:
                    processAppointmentRequests();
                    break;
                case 4:
                    displayAllAppointments();
                    break;
                case 5:
                    displayLogs();
                    break;
                case 6:
                    MainScheduler.performLogout();
                    return false;
                default:
                    System.out.println("Wrong choice!");
            }
            return true;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return true;
        }
    }

    static void manageDoctorRecords() {
        System.out.println("\n----- Manage Doctors -----");
        System.out.println("1. Add Doctor");
        System.out.println("2. View Doctors");
        System.out.println("3. Update Doctor");
        System.out.println("4. Deactivate Doctor");
        System.out.println("5. Remove Doctor");
        System.out.println("6. Activate Doctor");
        System.out.println("7. Back");
        System.out.println("-------------------------");
        int menuChoice = MainScheduler.getIntegerInput("Choice: ");
        switch (menuChoice) {
            case 1:
                createDoctor();
                break;
            case 2:
                displayDoctors();
                break;
            case 3:
                editDoctor();
                break;
            case 4:
                deactivateDoctorAccount();
                break;
            case 5:
                deleteDoctor();
                break;
            case 6:
                activateDoctorAccount();
                break;
            case 7:
                return;
            default:
                System.out.println("Wrong choice!");
        }
    }

    static void createDoctor() {
        String newUsername = MainScheduler.getStringInput("Username: ");
        if (MainScheduler.users.containsKey(newUsername)) {
            System.out.println("Username taken!");
            return;
        }
        String newPassword = MainScheduler.getValidPassword("Password: ");
        String doctorSpecialty = selectSpecialty();
        String phoneNumber = MainScheduler.getValidPhoneNumber("Phone: ");
        String newDoctorID = "D" + String.format("%03d", MainScheduler.doctors.size() + 1);
        String[] newUserData = { newPassword, String.valueOf(MainScheduler.USER_DOCTOR), newDoctorID };
        MainScheduler.users.put(newUsername, newUserData);
        String[] newDoctorData = { newDoctorID, newUsername, doctorSpecialty, phoneNumber, "Active" };
        MainScheduler.doctors.add(newDoctorData);
        MainScheduler.recordLog("Added doctor: " + newUsername);
        System.out.println("Doctor added!");
    }

    static String selectSpecialty() {
        System.out.println("\nSpecialties:");
        System.out.println("1. Cardiology");
        System.out.println("2. Neurology");
        System.out.println("3. Orthopedics");
        System.out.println("4. Pediatrics");
        System.out.println("5. General Medicine");
        while (true) {
            String doctorSpecialty = MainScheduler.getStringInput("Enter specialty: ");
            if (doctorSpecialty.equalsIgnoreCase("Cardiology") ||
                    doctorSpecialty.equalsIgnoreCase("Neurology") ||
                    doctorSpecialty.equalsIgnoreCase("Orthopedics") ||
                    doctorSpecialty.equalsIgnoreCase("Pediatrics") ||
                    doctorSpecialty.equalsIgnoreCase("General Medicine")) {
                return doctorSpecialty;
            }
            System.out.println("Pick from list!");
        }
    }

    static void displayDoctors() {
        if (MainScheduler.doctors.isEmpty()) {
            System.out.println("No doctors!");
            return;
        }
        System.out.println("\n----- Doctors List -----");
        System.out.printf("%-10s %-15s %-15s %-15s %-10s\n", "ID", "Name", "Phone", "Specialty", "Status");
        System.out.println("--------------------------------------------");
        for (int i = 0; i < MainScheduler.doctors.size(); i++) {
            String[] doctorData = MainScheduler.doctors.get(i);
            String formattedPhone;
            if (doctorData[3].length() == 10) {
                formattedPhone = doctorData[3].substring(0, 3) + "-" + doctorData[3].substring(3, 6) + "-"
                        + doctorData[3].substring(6);
            } else {
                formattedPhone = doctorData[3];
            }
            System.out.printf("%-10s %-15s %-15s %-15s %-10s\n", doctorData[0], doctorData[1], formattedPhone,
                    doctorData[2], doctorData[4]);
        }
    }

    static void editDoctor() {
        displayDoctors();
        String doctorID = MainScheduler.getStringInput("Enter Doctor ID: ");
        int doctorIndex = findDoctorIndex(doctorID);
        if (doctorIndex == -1) {
            System.out.println("Doctor not found!");
            return;
        }
        String[] doctorData = MainScheduler.doctors.get(doctorIndex);
        System.out.println("Current: Phone: " + doctorData[3] + ", Specialty: " + doctorData[2]);
        String doctorSpecialty = MainScheduler.getStringInput("New Specialty: ");
        if (doctorSpecialty.isEmpty()) {
            doctorSpecialty = doctorData[2];
        }
        doctorData[2] = doctorSpecialty;
        String phoneNumber = MainScheduler.getValidPhoneNumber("New Phone: ");
        doctorData[3] = phoneNumber;
        MainScheduler.recordLog("Doctor updated: " + doctorID);
        System.out.println("Doctor updated!");
    }

    static void deactivateDoctorAccount() {
        displayDoctors();
        String doctorID = MainScheduler.getStringInput("Enter Doctor ID: ");
        int doctorIndex = findDoctorIndex(doctorID);
        if (doctorIndex == -1) {
            System.out.println("Doctor not found!");
            return;
        }
        if (MainScheduler.doctors.get(doctorIndex)[4].equals("Inactive")) {
            System.out.println("Already deactivated!");
            return;
        }
        String confirmation = MainScheduler.getStringInput("Confirm deactivation of doctor " + doctorID + "? (y/n): ");
        if (!confirmation.equalsIgnoreCase("y")) {
            System.out.println("Deactivation cancelled!");
            return;
        }
        MainScheduler.doctors.get(doctorIndex)[4] = "Inactive";
        ArrayList<MainScheduler.AppointmentNode> doctorAppointments = new ArrayList<>();
        UserFunctions.collectDoctorAppointments(MainScheduler.appointmentTreeRoot, doctorID, doctorAppointments);
        for (MainScheduler.AppointmentNode appointment : doctorAppointments) {
            if (!appointment.appointmentStatus.equals("Cancelled")) {
                appointment.appointmentStatus = "Cancelled";
                for (String[] appointmentRecord : MainScheduler.appointmentHistory) {
                    if (appointmentRecord[0].equals(appointment.appointmentID)) {
                        appointmentRecord[5] = "Cancelled";
                        appointmentRecord[6] = new Date().toString();
                        break;
                    }
                }
                MainScheduler
                        .recordLog("Appointment cancelled due to doctor deactivation: " + appointment.appointmentID);
            }
        }
        MainScheduler.recordLog("Doctor deactivated: " + doctorID);
        System.out.println("Doctor deactivated and all appointments cancelled!");
    }

    static void deleteDoctor() {
        displayDoctors();
        String doctorID = MainScheduler.getStringInput("Enter Doctor ID: ");
        int doctorIndex = findDoctorIndex(doctorID);
        if (doctorIndex == -1) {
            System.out.println("Doctor not found!");
            return;
        }
        ArrayList<MainScheduler.AppointmentNode> doctorAppointments = new ArrayList<>();
        UserFunctions.collectDoctorAppointments(MainScheduler.appointmentTreeRoot, doctorID, doctorAppointments);
        if (!doctorAppointments.isEmpty()) {
            System.out.println("Cannot remove: Has appointments!");
            return;
        }
        String confirmation = MainScheduler.getStringInput("Confirm removal of doctor " + doctorID + "? (y/n): ");
        if (!confirmation.equalsIgnoreCase("y")) {
            System.out.println("Removal cancelled!");
            return;
        }
        MainScheduler.doctors.remove(doctorIndex);
        String username = null;
        for (Map.Entry<String, String[]> entry : MainScheduler.users.entrySet()) {
            if (entry.getValue()[2].equals(doctorID)) {
                username = entry.getKey();
                break;
            }
        }
        if (username != null) {
            MainScheduler.users.remove(username);
        }
        Queue<String[]> tempRequests = new LinkedList<>();
        for (int i = 0; i < MainScheduler.appointmentRequests.size(); i++) {
            String[] requestData = MainScheduler.appointmentRequests.poll();
            if (!requestData[2].equals(doctorID)) {
                tempRequests.add(requestData);
            } else {
                MainScheduler.recordLog("Request cancelled: " + requestData[0]);
            }
        }
        MainScheduler.appointmentRequests = tempRequests;
        MainScheduler.recordLog("Doctor removed: " + doctorID);
        System.out.println("Doctor removed!");
    }

    static void activateDoctorAccount() {
        boolean hasInactiveDoctors = false;
        System.out.println("\n----- Inactive Doctors -----");
        System.out.printf("%-10s %-15s %-15s %-10s\n", "ID", "Phone", "Specialty", "Status");
        System.out.println("--------------------------------------------");
        for (int i = 0; i < MainScheduler.doctors.size(); i++) {
            String[] doctorData = MainScheduler.doctors.get(i);
            if (doctorData[4].equals("Inactive")) {
                System.out.printf("%-10s %-15s %-15s %-10s\n", doctorData[0], doctorData[3], doctorData[2],
                        doctorData[4]);
                hasInactiveDoctors = true;
            }
        }
        if (!hasInactiveDoctors) {
            System.out.println("No inactive doctors!");
            return;
        }
        String doctorID = MainScheduler.getStringInput("Enter Doctor ID: ");
        int doctorIndex = findDoctorIndex(doctorID);
        if (doctorIndex == -1) {
            System.out.println("Doctor not found!");
            return;
        }
        String[] doctorData = MainScheduler.doctors.get(doctorIndex);
        if (doctorData[4].equals("Active")) {
            System.out.println("Already active!");
            return;
        }
        String confirmation = MainScheduler.getStringInput("Confirm activation of doctor " + doctorID + "? (y/n): ");
        if (!confirmation.equalsIgnoreCase("y")) {
            System.out.println("Activation cancelled!");
            return;
        }
        doctorData[4] = "Active";
        MainScheduler.recordLog("Doctor activated: " + doctorID);
        System.out.println("Doctor activated!");
    }

    static int findDoctorIndex(String doctorID) {
        for (int i = 0; i < MainScheduler.doctors.size(); i++) {
            if (MainScheduler.doctors.get(i)[0].equals(doctorID)) {
                return i;
            }
        }
        return -1;
    }

    static void managePatientRecords() {
        System.out.println("\n----- Manage Patients -----");
        System.out.println("1. View Patients");
        System.out.println("2. Update Patient");
        System.out.println("3. Deactivate Patient");
        System.out.println("4. Back");
        System.out.println("--------------------------");
        int menuChoice = MainScheduler.getIntegerInput("Choice: ");
        switch (menuChoice) {
            case 1:
                displayPatients();
                break;
            case 2:
                editPatient();
                break;
            case 3:
                deactivatePatientAccount();
                break;
            case 4:
                return;
            default:
                System.out.println("Wrong choice!");
        }
    }

    static void displayPatients() {
        if (MainScheduler.patients.isEmpty()) {
            System.out.println("No patients!");
            return;
        }
        System.out.println("\n----- Patients List -----");
        System.out.printf("%-10s %-15s %-10s\n", "ID", "Phone", "Status");
        System.out.println("--------------------------------------------");
        for (int i = 0; i < MainScheduler.patients.size(); i++) {
            String[] patientData = MainScheduler.patients.get(i);
            System.out.printf("%-10s %-15s %-10s\n", patientData[0], patientData[1], patientData[2]);
        }
    }

    static void editPatient() {
        displayPatients();
        String patientID = MainScheduler.getStringInput("Enter Patient ID: ");
        int patientIndex = findPatientIndex(patientID);
        if (patientIndex == -1) {
            System.out.println("Patient not found!");
            return;
        }
        String[] patientData = MainScheduler.patients.get(patientIndex);
        System.out.println("Current: Phone: " + patientData[1]);
        String phoneNumber = MainScheduler.getValidPhoneNumber("New Phone: ");
        patientData[1] = phoneNumber;
        MainScheduler.recordLog("Patient updated: " + patientID);
        System.out.println("Patient updated!");
    }

    static void deactivatePatientAccount() {
        displayPatients();
        String patientID = MainScheduler.getStringInput("Enter Patient ID: ");
        int patientIndex = findPatientIndex(patientID);
        if (patientIndex == -1) {
            System.out.println("Patient not found!");
            return;
        }
        String[] patientData = MainScheduler.patients.get(patientIndex);
        if (patientData[2].equals("Inactive")) {
            System.out.println("Already deactivated!");
            return;
        }
        String confirmation = MainScheduler
                .getStringInput("Confirm deactivation of patient " + patientID + "? (y/n): ");
        if (!confirmation.equalsIgnoreCase("y")) {
            System.out.println("Deactivation cancelled!");
            return;
        }
        patientData[2] = "Inactive";
        ArrayList<MainScheduler.AppointmentNode> patientAppointments = new ArrayList<>();
        UserFunctions.collectPatientAppointments(MainScheduler.appointmentTreeRoot, patientID, patientAppointments);
        for (MainScheduler.AppointmentNode appointment : patientAppointments) {
            if (!appointment.appointmentStatus.equals("Cancelled")) {
                appointment.appointmentStatus = "Cancelled";
                for (String[] appointmentRecord : MainScheduler.appointmentHistory) {
                    if (appointmentRecord[0].equals(appointment.appointmentID)) {
                        appointmentRecord[5] = "Cancelled";
                        appointmentRecord[6] = new Date().toString();
                        break;
                    }
                }
                MainScheduler
                        .recordLog("Appointment cancelled due to patient deactivation: " + appointment.appointmentID);
            }
        }
        MainScheduler.recordLog("Patient deactivated: " + patientID);
        System.out.println("Patient deactivated and all appointments cancelled!");
    }

    static int findPatientIndex(String patientID) {
        for (int i = 0; i < MainScheduler.patients.size(); i++) {
            if (MainScheduler.patients.get(i)[0].equals(patientID)) {
                return i;
            }
        }
        return -1;
    }

    static void processAppointmentRequests() {
        if (MainScheduler.appointmentRequests.isEmpty()) {
            System.out.println("No requests!");
            return;
        }
        System.out.println("\n----- Pending Requests -----");
        System.out.printf("%-5s %-10s %-10s %-20s %-15s\n", "No.", "Patient", "Doctor", "Date", "Reason");
        System.out.println("--------------------------------------------");
        ArrayList<String[]> tempRequests = new ArrayList<>();
        int requestCount = 1;
        for (String[] requestData : MainScheduler.appointmentRequests) {
            tempRequests.add(requestData);
            System.out.printf("%-5d %-10s %-10s %-20s %-15s\n", requestCount++, requestData[1], requestData[2],
                    requestData[3], requestData[4]);
        }
        System.out.println("\nOptions:");
        System.out.println("0: Approve all");
        System.out.println("-1: Reject all");
        System.out.println("1 to " + tempRequests.size() + ": Approve specific request");
        int requestNumber = MainScheduler.getIntegerInput("Enter choice: ");
        if (requestNumber == 0) {
            String confirmation = MainScheduler.getStringInput("Confirm approve all requests? (y/n): ");
            if (!confirmation.equalsIgnoreCase("y")) {
                System.out.println("Approval cancelled!");
                MainScheduler.appointmentRequests.addAll(tempRequests);
                return;
            }
            for (String[] requestData : tempRequests) {
                approveRequest(requestData);
            }
            System.out.println("All approved!");
        } else if (requestNumber == -1) {
            String confirmation = MainScheduler.getStringInput("Confirm reject all requests? (y/n): ");
            if (!confirmation.equalsIgnoreCase("y")) {
                System.out.println("Rejection cancelled!");
                MainScheduler.appointmentRequests.addAll(tempRequests);
                return;
            }
            for (String[] requestData : tempRequests) {
                MainScheduler.recordLog("Request rejected: " + requestData[0]);
            }
            System.out.println("All rejected!");
        } else if (requestNumber > 0 && requestNumber <= tempRequests.size()) {
            String confirmation = MainScheduler
                    .getStringInput("Confirm approve request " + requestNumber + "? (y/n): ");
            if (!confirmation.equalsIgnoreCase("y")) {
                System.out.println("Approval cancelled!");
                MainScheduler.appointmentRequests.addAll(tempRequests);
                return;
            }
            approveRequest(tempRequests.get(requestNumber - 1));
            for (int i = 0; i < tempRequests.size(); i++) {
                if (i != requestNumber - 1) {
                    MainScheduler.appointmentRequests.add(tempRequests.get(i));
                }
            }
            System.out.println("Request approved!");
        } else {
            System.out.println("Invalid choice!");
            MainScheduler.appointmentRequests.addAll(tempRequests);
        }
    }

    static void approveRequest(String[] requestData) {
        String newAppointmentID = "A" + String.format("%03d", getNextAppointmentID());
        try {
            Date date = MainScheduler.dateTimeFormat.parse(requestData[3]);
            MainScheduler.AppointmentNode newNode = new MainScheduler.AppointmentNode(
                    newAppointmentID, requestData[1], requestData[2], date, requestData[4], "Scheduled");
            if (MainScheduler.appointmentTreeRoot == null) {
                MainScheduler.appointmentTreeRoot = newNode;
            } else {
                insertAppointmentNode(MainScheduler.appointmentTreeRoot, newNode);
            }
            String[] appointmentRecord = { newAppointmentID, requestData[1], requestData[2], requestData[3],
                    requestData[4], "Scheduled", new Date().toString() };
            MainScheduler.appointmentHistory.add(appointmentRecord);
            MainScheduler.recordLog("Appointment approved: " + newAppointmentID);
        } catch (ParseException e) {
            System.out.println("Bad date!");
        }
    }

    static int getNextAppointmentID() {
        return countTreeNodes(MainScheduler.appointmentTreeRoot) + 1;
    }

    static int countTreeNodes(MainScheduler.AppointmentNode node) {
        if (node == null) {
            return 0;
        }
        return 1 + countTreeNodes(node.leftChild) + countTreeNodes(node.rightChild);
    }

    static void insertAppointmentNode(MainScheduler.AppointmentNode currentNode,
            MainScheduler.AppointmentNode newNode) {
        if (newNode.appointmentDate.before(currentNode.appointmentDate)) {
            if (currentNode.leftChild == null) {
                currentNode.leftChild = newNode;
            } else {
                insertAppointmentNode(currentNode.leftChild, newNode);
            }
        } else {
            if (currentNode.rightChild == null) {
                currentNode.rightChild = newNode;
            } else {
                insertAppointmentNode(currentNode.rightChild, newNode);
            }
        }
    }

    static void displayAllAppointments() {
        if (MainScheduler.appointmentTreeRoot == null) {
            System.out.println("No appointments!");
            return;
        }
        System.out.println("\n----- All Appointments -----");
        System.out.printf("%-10s %-15s %-15s %-20s %-15s %-10s\n", "ID", "Patient", "Doctor", "Date", "Reason",
                "Status");
        System.out.println("--------------------------------------------");
        traverseInorder(MainScheduler.appointmentTreeRoot);
    }

    static void traverseInorder(MainScheduler.AppointmentNode node) {
        if (node == null) {
            return;
        }
        traverseInorder(node.leftChild);
        System.out.printf("%-10s %-15s %-15s %-20s %-15s %-10s\n",
                node.appointmentID, node.patientID, node.doctorID,
                MainScheduler.dateTimeFormat.format(node.appointmentDate),
                node.appointmentReason, node.appointmentStatus);
        traverseInorder(node.rightChild);
    }

    static void displayLogs() {
        if (MainScheduler.operationLogs.isEmpty()) {
            System.out.println("No logs!");
            return;
        }
        System.out.println("\n----- Operation Logs -----");
        for (int i = 0; i < MainScheduler.operationLogs.size(); i++) {
            System.out.println(MainScheduler.operationLogs.get(i));
        }
    }
}