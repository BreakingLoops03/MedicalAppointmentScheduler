import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MedicalAppointmentScheduler {
    // Constants
    static final String PATIENTS_FILE = "patients.txt";
    static final String DOCTORS_FILE = "doctors.txt";
    static final String APPOINTMENTS_FILE = "appointments.txt";
    static final String USERS_FILE = "users.txt";
    static final String REQUESTS_FILE = "requests.txt";
    static final String LOGS_FILE = "logs.txt";
    
    // User types
    static final int USER_ADMIN = 1;
    static final int USER_DOCTOR = 2;
    static final int USER_PATIENT = 3;
    
    // Data structures
    static HashMap<String, String[]> users = new HashMap<>();
    static ArrayList<String[]> patients = new ArrayList<>();
    static ArrayList<String[]> doctors = new ArrayList<>();
    static LinkedList<String[]> appointmentHistory = new LinkedList<>();
    static Queue<String[]> appointmentRequests = new LinkedList<>();
    static Stack<String> operationLogs = new Stack<>();
    static AppointmentNode appointmentRoot = null;
    
    // Current logged in user
    static String currentUsername = null;
    static int currentUserType = 0;
    static String currentUserId = null;
    
    static Scanner scanner = new Scanner(System.in);
    static SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    
    // Appointment Node class for binary tree
    static class AppointmentNode implements Serializable {
        private static final long serialVersionUID = 1L;
        String id;
        String patientId;
        String doctorId;
        Date date;
        String reason;
        String status;
        AppointmentNode left;
        AppointmentNode right;
        
        AppointmentNode(String id, String patientId, String doctorId, Date date, String reason, String status) {
            this.id = id;
            this.patientId = patientId;
            this.doctorId = doctorId;
            this.date = date;
            this.reason = reason;
            this.status = status;
            this.left = null;
            this.right = null;
        }
    }
    
    public static void main(String[] args) {
        loadData();
        
        if (!hasAdmin()) {
            createDefaultAdmin();
        }
        
        boolean running = true;
        while (running) {
            if (currentUsername == null) {
                displayLoginMenu();
            } else {
                switch (currentUserType) {
                    case USER_ADMIN:
                        displayAdminMenu();
                        break;
                    case USER_DOCTOR:
                        displayDoctorMenu();
                        break;
                    case USER_PATIENT:
                        displayPatientMenu();
                        break;
                }
            }
        }
        
        saveData();
        scanner.close();
    }
    
    static boolean hasAdmin() {
        for (Map.Entry<String, String[]> entry : users.entrySet()) {
            if (Integer.parseInt(entry.getValue()[1]) == USER_ADMIN) {
                return true;
            }
        }
        return false;
    }
    
    static void createDefaultAdmin() {
        String[] adminData = {"admin123", "1", "A001"};
        users.put("admin", adminData);
        logOperation("Default admin account created");
        System.out.println("Default admin account created. Username: admin, Password: admin123");
    }
    
    static void displayLoginMenu() {
        System.out.println("\n===== MEDICAL APPOINTMENT SCHEDULER =====");
        System.out.println("1. Login");
        System.out.println("2. Register as Patient");
        System.out.println("3. Exit");
        System.out.println("=========================================");
        
        int choice = getIntInput("Enter your choice: ");
        
        switch (choice) {
            case 1:
                login();
                break;
            case 2:
                registerPatient();
                break;
            case 3:
                saveData();
                System.out.println("Thank you for using Medical Scheduler!");
                System.exit(0);
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }
    
    static void login() {
        String username = getMandatoryStringInput("Enter username: ");
        String password = getMandatoryStringInput("Enter password: ");
        
        if (users.containsKey(username)) {
            String[] userData = users.get(username);
            if (userData[0].equals(password)) {
                currentUsername = username;
                currentUserType = Integer.parseInt(userData[1]);
                currentUserId = userData[2];
                logOperation(username + " logged in");
                System.out.println("Login successful!");
            } else {
                System.out.println("Incorrect password!");
            }
        } else {
            System.out.println("Username not found!");
        }
    }
    
    static void registerPatient() {
        String username = getMandatoryStringInput("Enter username: ");
        
        if (users.containsKey(username)) {
            System.out.println("Username already exists!");
            return;
        }
        
        String password = getMandatoryStringInput("Enter password: ");
        String name = getMandatoryStringInput("Enter your full name: ");
        String phone = getMandatoryStringInput("Enter your phone number: ");
        String email = getStringInput("Enter your email (optional): ");
        String address = getStringInput("Enter your address: ");
        
        String patientId = "P" + String.format("%03d", patients.size() + 1);
        
        String[] userData = {password, String.valueOf(USER_PATIENT), patientId};
        users.put(username, userData);
        
        String[] patientData = {patientId, name, phone, email, address, "Active"};
        patients.add(patientData);
        
        logOperation("New patient registered: " + username);
        System.out.println("Registration successful! You can now login.");
    }
    
    static void displayAdminMenu() {
        System.out.println("\n===== ADMIN MENU =====");
        System.out.println("1. Manage Doctors");
        System.out.println("2. Manage Patients");
        System.out.println("3. Approve Appointment Requests");
        System.out.println("4. View All Appointments");
        System.out.println("5. View Operation Logs");
        System.out.println("6. Logout");
        System.out.println("=====================");
        
        int choice = getIntInput("Enter your choice: ");
        
        switch (choice) {
            case 1:
                manageDoctors();
                break;
            case 2:
                managePatients();
                break;
            case 3:
                approveAppointmentRequests();
                break;
            case 4:
                viewAllAppointments();
                break;
            case 5:
                viewOperationLogs();
                break;
            case 6:
                logout();
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }
    
    static void manageDoctors() {
        System.out.println("\n===== MANAGE DOCTORS =====");
        System.out.println("1. Add New Doctor");
        System.out.println("2. View All Doctors");
        System.out.println("3. Update Doctor");
        System.out.println("4. Deactivate Doctor");
        System.out.println("5. Remove Doctor");
        System.out.println("6. Activate Doctor");
        System.out.println("7. Back to Admin Menu");
        System.out.println("=========================");
        
        int choice = getIntInput("Enter your choice: ");
        
        switch (choice) {
            case 1:
                addDoctor();
                break;
            case 2:
                viewAllDoctors();
                break;
            case 3:
                updateDoctor();
                break;
            case 4:
                deactivateDoctor();
                break;
            case 5:
                removeDoctor();
                break;
            case 6:
                activateDoctor();
                break;
            case 7:
                return;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }
    
    static void addDoctor() {
        String username = getMandatoryStringInput("Enter username for doctor: ");
        
        if (users.containsKey(username)) {
            System.out.println("Username already exists!");
            return;
        }
        
        String password = getMandatoryStringInput("Enter password: ");
        String name = getMandatoryStringInput("Enter doctor's full name: ");
        String specialty = getMandatoryStringInput("Enter doctor's specialty: ");
        String phone = getMandatoryStringInput("Enter doctor's phone number: ");
        String email = getStringInput("Enter doctor's email (optional): ");
        
        String doctorId = "D" + String.format("%03d", doctors.size() + 1);
        
        String[] userData = {password, String.valueOf(USER_DOCTOR), doctorId};
        users.put(username, userData);
        
        String[] doctorData = {doctorId, name, specialty, phone, email, "Active"};
        doctors.add(doctorData);
        
        logOperation("New doctor added: " + username);
        System.out.println("Doctor added successfully!");
    }
    
    static void viewAllDoctors() {
        if (doctors.isEmpty()) {
            System.out.println("No doctors found!");
            return;
        }
        
        System.out.println("\n----- All Doctors -----");
        System.out.printf("%-10s %-20s %-15s %-15s %-20s %-10s\n", 
            "ID", "Name", "Specialty", "Phone", "Email", "Status");
        System.out.println("-------------------------------------------------------------------------");
        
        for (String[] doctor : doctors) {
            System.out.printf("%-10s %-20s %-15s %-15s %-20s %-10s\n", 
                doctor[0], doctor[1], doctor[2], doctor[3], doctor[4], doctor[5]);
        }
    }
    
    static void updateDoctor() {
        viewAllDoctors();
        String doctorId = getMandatoryStringInput("Enter Doctor ID to update: ");
        
        int index = findDoctorIndex(doctorId);
        if (index == -1) {
            System.out.println("Doctor not found!");
            return;
        }
        
        String[] doctor = doctors.get(index);
        
        System.out.println("Current details:");
        System.out.printf("Name: %s, Specialty: %s, Phone: %s, Email: %s\n", 
            doctor[1], doctor[2], doctor[3], doctor[4]);
        
        String name = getMandatoryStringInput("Enter new Name: ");
        doctor[1] = name;
        
        String specialty = getMandatoryStringInput("Enter new Specialty: ");
        doctor[2] = specialty;
        
        String phone = getMandatoryStringInput("Enter new Phone: ");
        doctor[3] = phone;
        
        String email = getStringInput("Enter new Email (optional): ");
        doctor[4] = email;
        
        logOperation("Doctor updated: " + doctorId);
        System.out.println("Doctor updated successfully!");
    }
    
    static void deactivateDoctor() {
        viewAllDoctors();
        String doctorId = getMandatoryStringInput("Enter Doctor ID to deactivate: ");
        
        int index = findDoctorIndex(doctorId);
        if (index == -1) {
            System.out.println("Doctor not found!");
            return;
        }
        
        String[] doctor = doctors.get(index);
        if (doctor[5].equals("Inactive")) {
            System.out.println("Doctor is already deactivated!");
            return;
        }
        
        doctor[5] = "Inactive";
        
        logOperation("Doctor deactivated: " + doctorId);
        System.out.println("Doctor deactivated successfully!");
    }
    
    static void removeDoctor() {
        viewAllDoctors();
        String doctorId = getMandatoryStringInput("Enter Doctor ID to remove: ");
        
        int index = findDoctorIndex(doctorId);
        if (index == -1) {
            System.out.println("Doctor not found!");
            return;
        }
        
        // Check for scheduled appointments
        ArrayList<AppointmentNode> doctorAppointments = new ArrayList<>();
        collectDoctorAppointments(appointmentRoot, doctorId, doctorAppointments);
        
        if (!doctorAppointments.isEmpty()) {
            System.out.println("Cannot remove doctor: They have scheduled appointments!");
            return;
        }
        
        // Remove from doctors list
        String[] doctor = doctors.get(index);
        doctors.remove(index);
        
        // Remove from users map
        String usernameToRemove = null;
        for (Map.Entry<String, String[]> entry : users.entrySet()) {
            if (entry.getValue()[2].equals(doctorId)) {
                usernameToRemove = entry.getKey();
                break;
            }
        }
        if (usernameToRemove != null) {
            users.remove(usernameToRemove);
        }
        
        // Cancel any pending appointment requests
        Queue<String[]> tempRequests = new LinkedList<>();
        while (!appointmentRequests.isEmpty()) {
            String[] request = appointmentRequests.poll();
            if (!request[2].equals(doctorId)) {
                tempRequests.add(request);
            } else {
                logOperation("Appointment request cancelled due to doctor removal: " + request[0]);
            }
        }
        appointmentRequests = tempRequests;
        
        logOperation("Doctor removed: " + doctorId);
        System.out.println("Doctor removed successfully!");
    }
    
    static void activateDoctor() {
        // Display only deactivated doctors
        boolean hasDeactivated = false;
        System.out.println("\n----- Deactivated Doctors -----");
        System.out.printf("%-10s %-20s %-15s %-15s %-20s %-10s\n", 
            "ID", "Name", "Specialty", "Phone", "Email", "Status");
        System.out.println("-------------------------------------------------------------------------");
        
        for (String[] doctor : doctors) {
            if (doctor[5].equals("Inactive")) {
                System.out.printf("%-10s %-20s %-15s %-15s %-20s %-10s\n", 
                    doctor[0], doctor[1], doctor[2], doctor[3], doctor[4], doctor[5]);
                hasDeactivated = true;
            }
        }
        
        if (!hasDeactivated) {
            System.out.println("No deactivated doctors found!");
            return;
        }
        
        String doctorId = getMandatoryStringInput("Enter Doctor ID to activate: ");
        
        int index = findDoctorIndex(doctorId);
        if (index == -1) {
            System.out.println("Doctor not found!");
            return;
        }
        
        String[] doctor = doctors.get(index);
        if (doctor[5].equals("Active")) {
            System.out.println("Doctor is already active!");
            return;
        }
        
        doctor[5] = "Active";
        
        logOperation("Doctor activated: " + doctorId);
        System.out.println("Doctor activated successfully!");
    }
    
    static int findDoctorIndex(String doctorId) {
        for (int i = 0; i < doctors.size(); i++) {
            if (doctors.get(i)[0].equals(doctorId)) {
                return i;
            }
        }
        return -1;
    }
    
    static void managePatients() {
        System.out.println("\n===== MANAGE PATIENTS =====");
        System.out.println("1. View All Patients");
        System.out.println("2. Update Patient");
        System.out.println("3. Deactivate Patient");
        System.out.println("4. Back to Admin Menu");
        System.out.println("==========================");
        
        int choice = getIntInput("Enter your choice: ");
        
        switch (choice) {
            case 1:
                viewAllPatients();
                break;
            case 2:
                updatePatient();
                break;
            case 3:
                deactivatePatient();
                break;
            case 4:
                return;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }
    
    static void viewAllPatients() {
        if (patients.isEmpty()) {
            System.out.println("No patients found!");
            return;
        }
        
        System.out.println("\n----- All Patients -----");
        System.out.printf("%-10s %-20s %-15s %-20s %-20s %-10s\n", 
            "ID", "Name", "Phone", "Email", "Address", "Status");
        System.out.println("-------------------------------------------------------------------------");
        
        for (String[] patient : patients) {
            System.out.printf("%-10s %-20s %-15s %-20s %-20s %-10s\n", 
                patient[0], patient[1], patient[2], patient[3], patient[4], patient[5]);
        }
    }
    
    static void updatePatient() {
        viewAllPatients();
        String patientId = getMandatoryStringInput("Enter Patient ID to update: ");
        
        int index = findPatientIndex(patientId);
        if (index == -1) {
            System.out.println("Patient not found!");
            return;
        }
        
        String[] patient = patients.get(index);
        
        System.out.println("Current details:");
        System.out.printf("Name: %s, Phone: %s, Email: %s, Address: %s\n", 
            patient[1], patient[2], patient[3], patient[4]);
        
        String name = getMandatoryStringInput("Enter new Name: ");
        patient[1] = name;
        
        String phone = getMandatoryStringInput("Enter new Phone: ");
        patient[2] = phone;
        
        String email = getStringInput("Enter new Email (optional): ");
        patient[3] = email;
        
        String address = getStringInput("Enter new Address: ");
        patient[4] = address;
        
        logOperation("Patient updated: " + patientId);
        System.out.println("Patient updated successfully!");
    }
    
    static void deactivatePatient() {
        viewAllPatients();
        String patientId = getMandatoryStringInput("Enter Patient ID to deactivate: ");
        
        int index = findPatientIndex(patientId);
        if (index == -1) {
            System.out.println("Patient not found!");
            return;
        }
        
        String[] patient = patients.get(index);
        patient[5] = "Inactive";
        
        logOperation("Patient deactivated: " + patientId);
        System.out.println("Patient deactivated successfully!");
    }
    
    static int findPatientIndex(String patientId) {
        for (int i = 0; i < patients.size(); i++) {
            if (patients.get(i)[0].equals(patientId)) {
                return i;
            }
        }
        return -1;
    }
    
    static void approveAppointmentRequests() {
        if (appointmentRequests.isEmpty()) {
            System.out.println("No pending appointment requests!");
            return;
        }
        
        System.out.println("\n----- Pending Appointment Requests -----");
        System.out.printf("%-5s %-10s %-10s %-20s %-15s\n", 
            "No.", "Patient", "Doctor", "Date & Time", "Reason");
        System.out.println("----------------------------------------------------------");
        
        ArrayList<String[]> tempRequests = new ArrayList<>();
        int count = 1;
        
        while (!appointmentRequests.isEmpty()) {
            String[] request = appointmentRequests.poll();
            tempRequests.add(request);
            
            String patientName = getPatientName(request[1]);
            String doctorName = getDoctorName(request[2]);
            
            System.out.printf("%-5d %-10s %-10s %-20s %-15s\n", 
                count++, patientName, doctorName, request[3], request[4]);
        }
        
        int requestNumber = getIntInput("Enter request number to approve (0 to approve all, -1 to reject all): ");
        
        if (requestNumber == 0) {
            for (String[] request : tempRequests) {
                approveRequest(request);
            }
            System.out.println("All requests approved!");
        } else if (requestNumber == -1) {
            for (String[] request : tempRequests) {
                logOperation("Appointment request rejected: " + request[0]);
            }
            System.out.println("All requests rejected!");
        } else if (requestNumber > 0 && requestNumber <= tempRequests.size()) {
            approveRequest(tempRequests.get(requestNumber - 1));
            
            for (int i = 0; i < tempRequests.size(); i++) {
                if (i != requestNumber - 1) {
                    appointmentRequests.add(tempRequests.get(i));
                }
            }
            System.out.println("Request approved!");
        } else {
            System.out.println("Invalid request number!");
            for (String[] request : tempRequests) {
                appointmentRequests.add(request);
            }
        }
    }
    
    static void approveRequest(String[] request) {
        String appointmentId = "A" + String.format("%03d", getNextAppointmentId());
        
        try {
            Date appointmentDate = dateTimeFormat.parse(request[3]);
            AppointmentNode newNode = new AppointmentNode(
                appointmentId, request[1], request[2], appointmentDate, request[4], "Scheduled");
            
            if (appointmentRoot == null) {
                appointmentRoot = newNode;
            } else {
                insertAppointment(appointmentRoot, newNode);
            }
            
            String[] historyEntry = {
                appointmentId, request[1], request[2], request[3], request[4], "Scheduled", 
                new Date().toString()
            };
            appointmentHistory.add(historyEntry);
            
            logOperation("Appointment approved: " + appointmentId);
        } catch (ParseException e) {
            System.out.println("Error parsing date: " + e.getMessage());
        }
    }
    
    static int getNextAppointmentId() {
        if (appointmentRoot == null) {
            return 1;
        }
        
        return countNodes(appointmentRoot) + 1;
    }
    
    static int countNodes(AppointmentNode node) {
        if (node == null) {
            return 0;
        }
        return 1 + countNodes(node.left) + countNodes(node.right);
    }
    
    static void insertAppointment(AppointmentNode root, AppointmentNode newNode) {
        if (newNode.date.before(root.date)) {
            if (root.left == null) {
                root.left = newNode;
            } else {
                insertAppointment(root.left, newNode);
            }
        } else {
            if (root.right == null) {
                root.right = newNode;
            } else {
                insertAppointment(root.right, newNode);
            }
        }
    }
    
    static void viewAllAppointments() {
        if (appointmentRoot == null) {
            System.out.println("No appointments found!");
            return;
        }
        
        System.out.println("\n----- All Appointments -----");
        System.out.printf("%-10s %-15s %-15s %-20s %-15s %-10s\n", 
            "ID", "Patient", "Doctor", "Date & Time", "Reason", "Status");
        System.out.println("-------------------------------------------------------------------------");
        
        inorderTraversal(appointmentRoot);
    }
    
    static void inorderTraversal(AppointmentNode node) {
        if (node == null) {
            return;
        }
        
        inorderTraversal(node.left);
        
        String patientName = getPatientName(node.patientId);
        String doctorName = getDoctorName(node.doctorId);
        
        System.out.printf("%-10s %-15s %-15s %-20s %-15s %-10s\n", 
            node.id, patientName, doctorName, 
            dateTimeFormat.format(node.date), node.reason, node.status);
        
        inorderTraversal(node.right);
    }
    
    static String getPatientName(String patientId) {
        for (String[] patient : patients) {
            if (patient[0].equals(patientId)) {
                return patient[1];
            }
        }
        return "Unknown";
    }
    
    static String getDoctorName(String doctorId) {
        for (String[] doctor : doctors) {
            if (doctor[0].equals(doctorId)) {
                return doctor[1];
            }
        }
        return "Unknown";
    }
    
    static void viewOperationLogs() {
        if (operationLogs.isEmpty()) {
            System.out.println("No operation logs found!");
            return;
        }
        
        System.out.println("\n----- Operation Logs -----");
        
        Stack<String> tempStack = new Stack<>();
        while (!operationLogs.isEmpty()) {
            String log = operationLogs.pop();
            System.out.println(log);
            tempStack.push(log);
        }
        
        while (!tempStack.isEmpty()) {
            operationLogs.push(tempStack.pop());
        }
    }
    
    static void displayDoctorMenu() {
        System.out.println("\n===== DOCTOR MENU =====");
        System.out.println("1. View My Appointments");
        System.out.println("2. Cancel Appointment");
        System.out.println("3. View Patient History");
        System.out.println("4. Update My Profile");
        System.out.println("5. Logout");
        System.out.println("======================");
        
        int choice = getIntInput("Enter your choice: ");
        
        switch (choice) {
            case 1:
                viewDoctorAppointments();
                break;
            case 2:
                cancelDoctorAppointment();
                break;
            case 3:
                viewPatientHistory();
                break;
            case 4:
                updateDoctorProfile();
                break;
            case 5:
                logout();
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }
    
    static void viewDoctorAppointments() {
        if (appointmentRoot == null) {
            System.out.println("No appointments found!");
            return;
        }
        
        System.out.println("\n----- My Appointments -----");
        System.out.printf("%-10s %-15s %-20s %-15s %-10s\n", 
            "ID", "Patient", "Date & Time", "Reason", "Status");
        System.out.println("----------------------------------------------------------");
        
        ArrayList<AppointmentNode> doctorAppointments = new ArrayList<>();
        collectDoctorAppointments(appointmentRoot, currentUserId, doctorAppointments);
        
        if (doctorAppointments.isEmpty()) {
            System.out.println("No appointments found for you!");
            return;
        }
        
        doctorAppointments.sort((a1, a2) -> a1.date.compareTo(a2.date));
        
        for (AppointmentNode appointment : doctorAppointments) {
            String patientName = getPatientName(appointment.patientId);
            
            System.out.printf("%-10s %-15s %-20s %-15s %-10s\n", 
                appointment.id, patientName, 
                dateTimeFormat.format(appointment.date), 
                appointment.reason, appointment.status);
        }
    }
    
    static void collectDoctorAppointments(AppointmentNode node, String doctorId, 
                                         ArrayList<AppointmentNode> appointments) {
        if (node == null) {
            return;
        }
        
        collectDoctorAppointments(node.left, doctorId, appointments);
        
        if (node.doctorId.equals(doctorId) && !node.status.equals("Cancelled")) {
            appointments.add(node);
        }
        
        collectDoctorAppointments(node.right, doctorId, appointments);
    }
    
    static void cancelDoctorAppointment() {
        if (appointmentRoot == null) {
            System.out.println("No appointments found!");
            return;
        }
        
        ArrayList<AppointmentNode> doctorAppointments = new ArrayList<>();
        collectDoctorAppointments(appointmentRoot, currentUserId, doctorAppointments);
        
        if (doctorAppointments.isEmpty()) {
            System.out.println("No appointments found for you!");
            return;
        }
        
        System.out.println("\n----- My Appointments -----");
        System.out.printf("%-5s %-10s %-15s %-20s %-15s\n", 
            "No.", "ID", "Patient", "Date & Time", "Reason");
        System.out.println("----------------------------------------------------------");
        
        for (int i = 0; i < doctorAppointments.size(); i++) {
            AppointmentNode appointment = doctorAppointments.get(i);
            String patientName = getPatientName(appointment.patientId);
            
            System.out.printf("%-5d %-10s %-15s %-20s %-15s\n", 
                i + 1, appointment.id, patientName, 
                dateTimeFormat.format(appointment.date), appointment.reason);
        }
        
        int appointmentNumber = getIntInput("Enter appointment number to cancel (0 to go back): ");
        
        if (appointmentNumber == 0) {
            return;
        }
        
        if (appointmentNumber > 0 && appointmentNumber <= doctorAppointments.size()) {
            AppointmentNode appointment = doctorAppointments.get(appointmentNumber - 1);
            appointment.status = "Cancelled";
            
            for (String[] historyEntry : appointmentHistory) {
                if (historyEntry[0].equals(appointment.id)) {
                    historyEntry[5] = "Cancelled";
                    historyEntry[6] = new Date().toString();
                    break;
                }
            }
            
            logOperation("Appointment cancelled by doctor: " + appointment.id);
            System.out.println("Appointment cancelled successfully!");
        } else {
            System.out.println("Invalid appointment number!");
        }
    }
    
    static void viewPatientHistory() {
        viewAllPatients();
        String patientId = getMandatoryStringInput("Enter Patient ID to view history: ");
        
        int index = findPatientIndex(patientId);
        if (index == -1) {
            System.out.println("Patient not found!");
            return;
        }
        
        System.out.println("\n----- Patient History -----");
        System.out.printf("%-10s %-15s %-20s %-15s %-10s %-20s\n", 
            "ID", "Doctor", "Date & Time", "Reason", "Status", "Last Updated");
        System.out.println("-------------------------------------------------------------------------");
        
        boolean found = false;
        for (String[] historyEntry : appointmentHistory) {
            if (historyEntry[1].equals(patientId)) {
                String doctorName = getDoctorName(historyEntry[2]);
                
                System.out.printf("%-10s %-15s %-20s %-15s %-10s %-20s\n", 
                    historyEntry[0], doctorName, historyEntry[3], 
                    historyEntry[4], historyEntry[5], historyEntry[6]);
                found = true;
            }
        }
        
        if (!found) {
            System.out.println("No appointment history found for this patient!");
        }
    }
    
    static void updateDoctorProfile() {
        int index = findDoctorIndex(currentUserId);
        if (index == -1) {
            System.out.println("Error: Doctor profile not found!");
            return;
        }
        
        String[] doctor = doctors.get(index);
        
        System.out.println("\n----- Update My Profile -----");
        System.out.println("Current details:");
        System.out.printf("Name: %s, Specialty: %s, Phone: %s, Email: %s\n", 
            doctor[1], doctor[2], doctor[3], doctor[4]);
        
        String phone = getMandatoryStringInput("Enter new Phone: ");
        doctor[3] = phone;
        
        String email = getStringInput("Enter new Email (optional): ");
        doctor[4] = email;
        
        logOperation("Doctor updated profile: " + currentUserId);
        System.out.println("Profile updated successfully!");
    }
    
    static void displayPatientMenu() {
        System.out.println("\n===== PATIENT MENU =====");
        System.out.println("1. View Available Doctors");
        System.out.println("2. Book Appointment");
        System.out.println("3. View My Appointments");
        System.out.println("4. Cancel Appointment");
        System.out.println("5. Update My Profile");
        System.out.println("6. Logout");
        System.out.println("=======================");
        
        int choice = getIntInput("Enter your choice: ");
        
        switch (choice) {
            case 1:
                viewAvailableDoctors();
                break;
            case 2:
                bookAppointment();
                break;
            case 3:
                viewPatientAppointments();
                break;
            case 4:
                cancelPatientAppointment();
                break;
            case 5:
                updatePatientProfile();
                break;
            case 6:
                logout();
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }
    
    static void viewAvailableDoctors() {
        if (doctors.isEmpty()) {
            System.out.println("No doctors found!");
            return;
        }
        
        System.out.println("\n----- Available Doctors -----");
        System.out.printf("%-10s %-20s %-15s\n", "ID", "Name", "Specialty");
        System.out.println("----------------------------------------");
        
        for (String[] doctor : doctors) {
            if (doctor[5].equals("Active")) {
                System.out.printf("%-10s %-20s %-15s\n", doctor[0], doctor[1], doctor[2]);
            }
        }
    }
    
    static void bookAppointment() {
        if (doctors.isEmpty()) {
            System.out.println("No doctors available for booking!");
            return;
        }
        
        System.out.println("\n----- Book Appointment -----");
        
        viewAvailableDoctors();
        String doctorId = getMandatoryStringInput("Enter Doctor ID: ");
        
        int doctorIndex = findDoctorIndex(doctorId);
        if (doctorIndex == -1) {
            System.out.println("Doctor not found!");
            return;
        }
        
        if (!doctors.get(doctorIndex)[5].equals("Active")) {
            System.out.println("This doctor is not currently available!");
            return;
        }
        
        String dateTimeStr = getMandatoryStringInput("Enter Date and Time (dd/MM/yyyy HH:mm): ");
        String reason = getMandatoryStringInput("Enter Reason for Appointment: ");
        
        try {
            Date appointmentDateTime = dateTimeFormat.parse(dateTimeStr);
            if (appointmentDateTime.before(new Date())) {
                System.out.println("Cannot book appointments in the past!");
                return;
            }
            
            if (hasTimeConflict(doctorId, appointmentDateTime)) {
                System.out.println("Doctor is not available at this time. Please choose another time.");
                return;
            }
            
            String requestId = "R" + String.format("%03d", appointmentRequests.size() + 1);
            
            String[] request = {requestId, currentUserId, doctorId, dateTimeStr, reason};
            appointmentRequests.add(request);
            
            logOperation("Appointment request submitted: " + requestId);
            System.out.println("Appointment request submitted successfully! Waiting for admin approval.");
        } catch (ParseException e) {
            System.out.println("Invalid date format! Please use dd/MM/yyyy HH:mm");
        }
    }
    
    static boolean hasTimeConflict(String doctorId, Date appointmentDateTime) {
        if (appointmentRoot == null) {
            return false;
        }
        
        ArrayList<AppointmentNode> doctorAppointments = new ArrayList<>();
        collectDoctorAppointments(appointmentRoot, doctorId, doctorAppointments);
        
        for (AppointmentNode appointment : doctorAppointments) {
            if (!appointment.status.equals("Cancelled")) {
                long timeDiff = Math.abs(appointment.date.getTime() - appointmentDateTime.getTime());
                if (timeDiff < 3600000) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    static void viewPatientAppointments() {
        if (appointmentRoot == null) {
            System.out.println("No appointments found!");
            return;
        }
        
        System.out.println("\n----- My Appointments -----");
        System.out.printf("%-10s %-15s %-20s %-15s %-10s\n", 
            "ID", "Doctor", "Date & Time", "Reason", "Status");
        System.out.println("----------------------------------------------------------");
        
        ArrayList<AppointmentNode> patientAppointments = new ArrayList<>();
        collectPatientAppointments(appointmentRoot, currentUserId, patientAppointments);
        
        if (patientAppointments.isEmpty()) {
            System.out.println("No appointments found for you!");
            
            boolean hasPendingRequests = false;
            for (String[] request : appointmentRequests) {
                if (request[1].equals(currentUserId)) {
                    if (!hasPendingRequests) {
                        System.out.println("\n----- Pending Requests -----");
                        System.out.printf("%-10s %-15s %-20s %-15s\n", 
                            "ID", "Doctor", "Date & Time", "Reason");
                        System.out.println("----------------------------------------------------------");
                        hasPendingRequests = true;
                    }
                    
                    String doctorName = getDoctorName(request[2]);
                    System.out.printf("%-10s %-15s %-20s %-15s\n", 
                        request[0], doctorName, request[3], request[4]);
                }
            }
            
            return;
        }
        
        patientAppointments.sort((a1, a2) -> a1.date.compareTo(a2.date));
        
        for (AppointmentNode appointment : patientAppointments) {
            String doctorName = getDoctorName(appointment.doctorId);
            
            System.out.printf("%-10s %-15s %-20s %-15s %-10s\n", 
                appointment.id, doctorName, 
                dateTimeFormat.format(appointment.date), 
                appointment.reason, appointment.status);
        }
    }
    
    static void collectPatientAppointments(AppointmentNode node, String patientId, 
                                          ArrayList<AppointmentNode> appointments) {
        if (node == null) {
            return;
        }
        
        collectPatientAppointments(node.left, patientId, appointments);
        
        if (node.patientId.equals(patientId)) {
            appointments.add(node);
        }
        
        collectPatientAppointments(node.right, patientId, appointments);
    }
    
    static void cancelPatientAppointment() {
        if (appointmentRoot == null) {
            System.out.println("No appointments found!");
            return;
        }
        
        ArrayList<AppointmentNode> patientAppointments = new ArrayList<>();
        collectPatientAppointments(appointmentRoot, currentUserId, patientAppointments);
        
        if (patientAppointments.isEmpty()) {
            System.out.println("No appointments found for you!");
            return;
        }
        
        System.out.println("\n----- My Appointments -----");
        System.out.printf("%-5s %-10s %-15s %-20s %-15s %-10s\n", 
            "No.", "ID", "Doctor", "Date & Time", "Reason", "Status");
        System.out.println("------------------------------------------------------------------");
        
        ArrayList<AppointmentNode> scheduledAppointments = new ArrayList<>();
        for (AppointmentNode appointment : patientAppointments) {
            if (appointment.status.equals("Scheduled")) {
                scheduledAppointments.add(appointment);
            }
        }
        
        if (scheduledAppointments.isEmpty()) {
            System.out.println("No scheduled appointments found that can be cancelled!");
            return;
        }
        
        for (int i = 0; i < scheduledAppointments.size(); i++) {
            AppointmentNode appointment = scheduledAppointments.get(i);
            String doctorName = getDoctorName(appointment.doctorId);
            
            System.out.printf("%-5d %-10s %-15s %-20s %-15s %-10s\n", 
                i + 1, appointment.id, doctorName, 
                dateTimeFormat.format(appointment.date), 
                appointment.reason, appointment.status);
        }
        
        int appointmentNumber = getIntInput("Enter appointment number to cancel (0 to go back): ");
        
        if (appointmentNumber == 0) {
            return;
        }
        
        if (appointmentNumber > 0 && appointmentNumber <= scheduledAppointments.size()) {
            AppointmentNode appointment = scheduledAppointments.get(appointmentNumber - 1);
            
            long timeDiff = appointment.date.getTime() - new Date().getTime();
            if (timeDiff < 86400000) {
                System.out.println("Cannot cancel appointments within 24 hours of scheduled time!");
                return;
            }
            
            appointment.status = "Cancelled";
            
            for (String[] historyEntry : appointmentHistory) {
                if (historyEntry[0].equals(appointment.id)) {
                    historyEntry[5] = "Cancelled";
                    historyEntry[6] = new Date().toString();
                    break;
                }
            }
            
            logOperation("Appointment cancelled by patient: " + appointment.id);
            System.out.println("Appointment cancelled successfully!");
        } else {
            System.out.println("Invalid appointment number!");
        }
    }
    
    static void updatePatientProfile() {
        int index = findPatientIndex(currentUserId);
        if (index == -1) {
            System.out.println("Error: Patient profile not found!");
            return;
        }
        
        String[] patient = patients.get(index);
        
        System.out.println("\n----- Update My Profile -----");
        System.out.println("Current details:");
        System.out.printf("Name: %s, Phone: %s, Email: %s, Address: %s\n", 
            patient[1], patient[2], patient[3], patient[4]);
        
        String phone = getMandatoryStringInput("Enter new Phone: ");
        patient[2] = phone;
        
        String email = getStringInput("Enter new Email (optional): ");
        patient[3] = email;
        
        String address = getStringInput("Enter new Address: ");
        patient[4] = address;
        
        logOperation("Patient updated profile: " + currentUserId);
        System.out.println("Profile updated successfully!");
    }
    
    static void logout() {
        logOperation(currentUsername + " logged out");
        currentUsername = null;
        currentUserType = 0;
        currentUserId = null;
        System.out.println("Logged out successfully!");
    }
    
    static void logOperation(String operation) {
        String timestamp = new Date().toString();
        operationLogs.push(timestamp + " - " + operation);
    }
    
    static void loadData() {
        loadUsers();
        loadPatients();
        loadDoctors();
        loadAppointments();
        loadRequests();
        loadLogs();
    }
    
    static void saveData() {
        saveUsers();
        savePatients();
        saveDoctors();
        saveAppointments();
        saveRequests();
        saveLogs();
    }
    
    static void loadUsers() {
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 4) {
                    String[] userData = {parts[1], parts[2], parts[3]};
                    users.put(parts[0], userData);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Users file not found. Starting with empty users.");
        } catch (IOException e) {
            System.out.println("Error loading users: " + e.getMessage());
        }
    }
    
    static void saveUsers() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE))) {
            for (Map.Entry<String, String[]> entry : users.entrySet()) {
                String[] data = entry.getValue();
                writer.write(entry.getKey() + "|" + data[0] + "|" + data[1] + "|" + data[2]);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving users: " + e.getMessage());
        }
    }
    
    static void loadPatients() {
        try (BufferedReader reader = new BufferedReader(new FileReader(PATIENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 6) {
                    patients.add(parts);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Patients file not found. Starting with empty patients.");
        } catch (IOException e) {
            System.out.println("Error loading patients: " + e.getMessage());
        }
    }
    
    static void savePatients() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PATIENTS_FILE))) {
            for (String[] patient : patients) {
                writer.write(String.join("|", patient));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving patients: " + e.getMessage());
        }
    }
    
    static void loadDoctors() {
        try (BufferedReader reader = new BufferedReader(new FileReader(DOCTORS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 6) {
                    doctors.add(parts);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Doctors file not found. Starting with empty doctors.");
        } catch (IOException e) {
            System.out.println("Error loading doctors: " + e.getMessage());
        }
    }
    
    static void saveDoctors() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DOCTORS_FILE))) {
            for (String[] doctor : doctors) {
                writer.write(String.join("|", doctor));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving doctors: " + e.getMessage());
        }
    }
    
    static void loadAppointments() {
        try (BufferedReader reader = new BufferedReader(new FileReader(APPOINTMENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 7) {
                    appointmentHistory.add(parts);
                    if (!parts[5].equals("Cancelled")) {
                        try {
                            Date date = dateTimeFormat.parse(parts[3]);
                            AppointmentNode node = new AppointmentNode(
                                parts[0], parts[1], parts[2], date, parts[4], parts[5]);
                            if (appointmentRoot == null) {
                                appointmentRoot = node;
                            } else {
                                insertAppointment(appointmentRoot, node);
                            }
                        } catch (ParseException e) {
                            System.out.println("Error parsing date: " + e.getMessage());
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Appointments file not found. Starting with empty appointments.");
        } catch (IOException e) {
            System.out.println("Error loading appointments: " + e.getMessage());
        }
    }
    
    static void saveAppointments() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(APPOINTMENTS_FILE))) {
            for (String[] appointment : appointmentHistory) {
                writer.write(String.join("|", appointment));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving appointments: " + e.getMessage());
        }
    }
    
    static void loadRequests() {
        try (BufferedReader reader = new BufferedReader(new FileReader(REQUESTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 5) {
                    appointmentRequests.add(parts);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Requests file not found. Starting with empty requests.");
        } catch (IOException e) {
            System.out.println("Error loading requests: " + e.getMessage());
        }
    }
    
    static void saveRequests() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(REQUESTS_FILE))) {
            for (String[] request : appointmentRequests) {
                writer.write(String.join("|", request));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving requests: " + e.getMessage());
        }
    }
    
    static void loadLogs() {
        try (BufferedReader reader = new BufferedReader(new FileReader(LOGS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                operationLogs.push(line);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Logs file not found. Starting with empty logs.");
        } catch (IOException e) {
            System.out.println("Error loading logs: " + e.getMessage());
        }
    }
    
    static void saveLogs() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOGS_FILE))) {
            Stack<String> tempStack = new Stack<>();
            while (!operationLogs.isEmpty()) {
                String log = operationLogs.pop();
                writer.write(log);
                writer.newLine();
                tempStack.push(log);
            }
            while (!tempStack.isEmpty()) {
                operationLogs.push(tempStack.pop());
            }
        } catch (IOException e) {
            System.out.println("Error saving logs: " + e.getMessage());
        }
    }
    
    static String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
    
    static String getMandatoryStringInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("This field is mandatory. Please enter a valid value.");
        }
    }
    
    static int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number!");
            }
        }
    }
}