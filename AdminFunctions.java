package trial;

import java.text.ParseException;
import java.util.*;

public class AdminFunctions {
    static void displayAdminMenu() {
        System.out.println("\n===== ADMIN MENU =====");
        System.out.println("1. Manage Doctors");
        System.out.println("2. Manage Patients");
        System.out.println("3. Approve Appointment Requests");
        System.out.println("4. View All Appointments");
        System.out.println("5. View Operation Logs");
        System.out.println("6. Logout");
        System.out.println("=====================");
        
        int choice = MainScheduler.getIntInput("Enter your choice: ");
        
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
                MainScheduler.logout();
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
        
        int choice = MainScheduler.getIntInput("Enter your choice: ");
        
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
        String username = MainScheduler.getMandatoryStringInput("Enter username for doctor: ");
        
        if (MainScheduler.users.containsKey(username)) {
            System.out.println("Username already exists!");
            return;
        }
        
        String password = MainScheduler.getValidPassword("Enter password: ");
        String specialty = MainScheduler.getMandatoryStringInput("Enter doctor's specialty: ");
        String phone = MainScheduler.getValidPhoneNumber("Enter doctor's phone number: ");
        String email = MainScheduler.getValidEmail("Enter doctor's email (optional, press Enter to skip): ");
        
        String doctorId = "D" + String.format("%03d", MainScheduler.doctors.size() + 1);
        
        String[] userData = {password, String.valueOf(MainScheduler.USER_DOCTOR), doctorId};
        MainScheduler.users.put(username, userData);
        
        String[] doctorData = {doctorId, phone, specialty, email, "Active"};
        MainScheduler.doctors.add(doctorData);
        
        MainScheduler.logOperation("New doctor added: " + username);
        System.out.println("Doctor added successfully!");
    }
    
    static void viewAllDoctors() {
        if (MainScheduler.doctors.isEmpty()) {
            System.out.println("No doctors found!");
            return;
        }
        
        System.out.println("\n----- All Doctors -----");
        System.out.printf("%-10s %-15s %-15s %-20s %-10s\n", 
            "ID", "Phone", "Specialty", "Email", "Status");
        System.out.println("----------------------------------------------------------");
        
        for (String[] doctor : MainScheduler.doctors) {
            System.out.printf("%-10s %-15s %-15s %-20s %-10s\n", 
                doctor[0], doctor[1], doctor[2], doctor[3], doctor[4]);
        }
    }
    
    static void updateDoctor() {
        viewAllDoctors();
        String doctorId = MainScheduler.getMandatoryStringInput("Enter Doctor ID to update: ");
        
        int index = findDoctorIndex(doctorId);
        if (index == -1) {
            System.out.println("Doctor not found!");
            return;
        }
        
        String[] doctor = MainScheduler.doctors.get(index);
        
        System.out.println("Current details:");
        System.out.printf("Phone: %s, Specialty: %s, Email: %s\n", 
            doctor[1], doctor[2], doctor[3]);
        
        String specialty = MainScheduler.getMandatoryStringInput("Enter new Specialty: ");
        doctor[2] = specialty;
        
        String phone = MainScheduler.getValidPhoneNumber("Enter new Phone: ");
        doctor[1] = phone;
        
        String email = MainScheduler.getValidEmail("Enter new Email (optional, press Enter to skip): ");
        doctor[3] = email;
        
        MainScheduler.logOperation("Doctor updated: " + doctorId);
        System.out.println("Doctor updated successfully!");
    }
    
    static void deactivateDoctor() {
        viewAllDoctors();
        String doctorId = MainScheduler.getMandatoryStringInput("Enter Doctor ID to deactivate: ");
        
        int index = findDoctorIndex(doctorId);
        if (index == -1) {
            System.out.println("Doctor not found!");
            return;
        }
        
        String[] doctor = MainScheduler.doctors.get(index);
        if (doctor[4].equals("Inactive")) {
            System.out.println("Doctor is already deactivated!");
            return;
        }
        
        doctor[4] = "Inactive";
        
        MainScheduler.logOperation("Doctor deactivated: " + doctorId);
        System.out.println("Doctor deactivated successfully!");
    }
    
    static void removeDoctor() {
        viewAllDoctors();
        String doctorId = MainScheduler.getMandatoryStringInput("Enter Doctor ID to remove: ");
        
        int index = findDoctorIndex(doctorId);
        if (index == -1) {
            System.out.println("Doctor not found!");
            return;
        }
        
        ArrayList<MainScheduler.AppointmentNode> doctorAppointments = new ArrayList<>();
        UserFunctions.collectDoctorAppointments(MainScheduler.appointmentRoot, doctorId, doctorAppointments);
        
        if (!doctorAppointments.isEmpty()) {
            System.out.println("Cannot remove doctor: They have scheduled appointments!");
            return;
        }
        
        String[] doctor = MainScheduler.doctors.get(index);
        MainScheduler.doctors.remove(index);
        
        String usernameToRemove = null;
        for (Map.Entry<String, String[]> entry : MainScheduler.users.entrySet()) {
            if (entry.getValue()[2].equals(doctorId)) {
                usernameToRemove = entry.getKey();
                break;
            }
        }
        if (usernameToRemove != null) {
            MainScheduler.users.remove(usernameToRemove);
        }
        
        Queue<String[]> tempRequests = new LinkedList<>();
        while (!MainScheduler.appointmentRequests.isEmpty()) {
            String[] request = MainScheduler.appointmentRequests.poll();
            if (!request[2].equals(doctorId)) {
                tempRequests.add(request);
            } else {
                MainScheduler.logOperation("Appointment request cancelled due to doctor removal: " + request[0]);
            }
        }
        MainScheduler.appointmentRequests = tempRequests;
        
        MainScheduler.logOperation("Doctor removed: " + doctorId);
        System.out.println("Doctor removed successfully!");
    }
    
    static void activateDoctor() {
        boolean hasDeactivated = false;
        System.out.println("\n----- Deactivated Doctors -----");
        System.out.printf("%-10s %-15s %-15s %-20s %-10s\n", 
            "ID", "Phone", "Specialty", "Email", "Status");
        System.out.println("----------------------------------------------------------");
        
        for (String[] doctor : MainScheduler.doctors) {
            if (doctor[4].equals("Inactive")) {
                System.out.printf("%-10s %-15s %-15s %-20s %-10s\n", 
                    doctor[0], doctor[1], doctor[2], doctor[3], doctor[4]);
                hasDeactivated = true;
            }
        }
        
        if (!hasDeactivated) {
            System.out.println("No deactivated doctors found!");
            return;
        }
        
        String doctorId = MainScheduler.getMandatoryStringInput("Enter Doctor ID to activate: ");
        
        int index = findDoctorIndex(doctorId);
        if (index == -1) {
            System.out.println("Doctor not found!");
            return;
        }
        
        String[] doctor = MainScheduler.doctors.get(index);
        if (doctor[4].equals("Active")) {
            System.out.println("Doctor is already active!");
            return;
        }
        
        doctor[4] = "Active";
        
        MainScheduler.logOperation("Doctor activated: " + doctorId);
        System.out.println("Doctor activated successfully!");
    }
    
    static int findDoctorIndex(String doctorId) {
        for (int i = 0; i < MainScheduler.doctors.size(); i++) {
            if (MainScheduler.doctors.get(i)[0].equals(doctorId)) {
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
        
        int choice = MainScheduler.getIntInput("Enter your choice: ");
        
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
        if (MainScheduler.patients.isEmpty()) {
            System.out.println("No patients found!");
            return;
        }
        
        System.out.println("\n----- All Patients -----");
        System.out.printf("%-10s %-15s %-20s %-20s %-10s\n", 
            "ID", "Phone", "Email", "Address", "Status");
        System.out.println("----------------------------------------------------------");
        
        for (String[] patient : MainScheduler.patients) {
            System.out.printf("%-10s %-15s %-20s %-20s %-10s\n", 
                patient[0], patient[1], patient[2], patient[3], patient[4]);
        }
    }
    
    static void updatePatient() {
        viewAllPatients();
        String patientId = MainScheduler.getMandatoryStringInput("Enter Patient ID to update: ");
        
        int index = findPatientIndex(patientId);
        if (index == -1) {
            System.out.println("Patient not found!");
            return;
        }
        
        String[] patient = MainScheduler.patients.get(index);
        
        System.out.println("Current details:");
        System.out.printf("Phone: %s, Email: %s, Address: %s\n", 
            patient[1], patient[2], patient[3]);
        
        String phone = MainScheduler.getValidPhoneNumber("Enter new Phone: ");
        patient[1] = phone;
        
        String email = MainScheduler.getValidEmail("Enter new Email (optional, press Enter to skip): ");
        patient[2] = email;
        
        String address = MainScheduler.getStringInput("Enter new Address (optional, press Enter to skip): ");
        patient[3] = address;
        
        MainScheduler.logOperation("Patient updated: " + patientId);
        System.out.println("Patient updated successfully!");
    }
    
    static void deactivatePatient() {
        viewAllPatients();
        String patientId = MainScheduler.getMandatoryStringInput("Enter Patient ID to deactivate: ");
        
        int index = findPatientIndex(patientId);
        if (index == -1) {
            System.out.println("Patient not found!");
            return;
        }
        
        String[] patient = MainScheduler.patients.get(index);
        patient[4] = "Inactive";
        
        MainScheduler.logOperation("Patient deactivated: " + patientId);
        System.out.println("Patient deactivated successfully!");
    }
    
    static int findPatientIndex(String patientId) {
        for (int i = 0; i < MainScheduler.patients.size(); i++) {
            if (MainScheduler.patients.get(i)[0].equals(patientId)) {
                return i;
            }
        }
        return -1;
    }
    
    static void approveAppointmentRequests() {
        if (MainScheduler.appointmentRequests.isEmpty()) {
            System.out.println("No pending appointment requests!");
            return;
        }
        
        System.out.println("\n----- Pending Appointment Requests -----");
        System.out.printf("%-5s %-10s %-10s %-20s %-15s\n", 
            "No.", "Patient", "Doctor", "Date & Time", "Reason");
        System.out.println("----------------------------------------------------------");
        
        ArrayList<String[]> tempRequests = new ArrayList<>();
        int count = 1;
        
        while (!MainScheduler.appointmentRequests.isEmpty()) {
            String[] request = MainScheduler.appointmentRequests.poll();
            tempRequests.add(request);
            
            String patientId = request[1];
            String doctorId = request[2];
            
            System.out.printf("%-5d %-10s %-10s %-20s %-15s\n", 
                count++, patientId, doctorId, request[3], request[4]);
        }
        
        int requestNumber = MainScheduler.getIntInput("Enter request number to approve (0 to approve all, -1 to reject all): ");
        
        if (requestNumber == 0) {
            for (String[] request : tempRequests) {
                approveRequest(request);
            }
            System.out.println("All requests approved!");
        } else if (requestNumber == -1) {
            for (String[] request : tempRequests) {
                MainScheduler.logOperation("Appointment request rejected: " + request[0]);
            }
            System.out.println("All requests rejected!");
        } else if (requestNumber > 0 && requestNumber <= tempRequests.size()) {
            approveRequest(tempRequests.get(requestNumber - 1));
            
            for (int i = 0; i < tempRequests.size(); i++) {
                if (i != requestNumber - 1) {
                    MainScheduler.appointmentRequests.add(tempRequests.get(i));
                }
            }
            System.out.println("Request approved!");
        } else {
            System.out.println("Invalid request number!");
            for (String[] request : tempRequests) {
                MainScheduler.appointmentRequests.add(request);
            }
        }
    }
    
    static void approveRequest(String[] request) {
        String appointmentId = "A" + String.format("%03d", getNextAppointmentId());
        
        try {
            Date appointmentDate = MainScheduler.dateTimeFormat.parse(request[3]);
            MainScheduler.AppointmentNode newNode = new MainScheduler.AppointmentNode(
                appointmentId, request[1], request[2], appointmentDate, request[4], "Scheduled");
            
            if (MainScheduler.appointmentRoot == null) {
                MainScheduler.appointmentRoot = newNode;
            } else {
                insertAppointment(MainScheduler.appointmentRoot, newNode);
            }
            
            String[] historyEntry = {
                appointmentId, request[1], request[2], request[3], request[4], "Scheduled", 
                new Date().toString()
            };
            MainScheduler.appointmentHistory.add(historyEntry);
            
            MainScheduler.logOperation("Appointment approved: " + appointmentId);
        } catch (ParseException e) {
            System.out.println("Error parsing date: " + e.getMessage());
        }
    }
    
    static int getNextAppointmentId() {
        if (MainScheduler.appointmentRoot == null) {
            return 1;
        }
        
        return countNodes(MainScheduler.appointmentRoot) + 1;
    }
    
    static int countNodes(MainScheduler.AppointmentNode node) {
        if (node == null) {
            return 0;
        }
        return 1 + countNodes(node.left) + countNodes(node.right);
    }
    
    static void insertAppointment(MainScheduler.AppointmentNode root, MainScheduler.AppointmentNode newNode) {
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
        if (MainScheduler.appointmentRoot == null) {
            System.out.println("No appointments found!");
            return;
        }
        
        System.out.println("\n----- All Appointments -----");
        System.out.printf("%-10s %-15s %-15s %-20s %-15s %-10s\n", 
            "ID", "Patient", "Doctor", "Date & Time", "Reason", "Status");
        System.out.println("-------------------------------------------------------------------------");
        
        inorderTraversal(MainScheduler.appointmentRoot);
    }
    
    static void inorderTraversal(MainScheduler.AppointmentNode node) {
        if (node == null) {
            return;
        }
        
        inorderTraversal(node.left);
        
        System.out.printf("%-10s %-15s %-15s %-20s %-15s %-10s\n", 
            node.id, node.patientId, node.doctorId, 
            MainScheduler.dateTimeFormat.format(node.date), node.reason, node.status);
        
        inorderTraversal(node.right);
    }
    
    static void viewOperationLogs() {
        if (MainScheduler.operationLogs.isEmpty()) {
            System.out.println("No operation logs found!");
            return;
        }
        
        System.out.println("\n----- Operation Logs -----");
        
        Stack<String> tempStack = new Stack<>();
        while (!MainScheduler.operationLogs.isEmpty()) {
            String log = MainScheduler.operationLogs.pop();
            System.out.println(log);
            tempStack.push(log);
        }
        
        while (!tempStack.isEmpty()) {
            MainScheduler.operationLogs.push(tempStack.pop());
        }
    }
}