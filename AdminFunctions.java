package trial;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;

public class AdminFunctions {
    static void showAdminMenu() {
        System.out.println("\n===== Admin Menu =====");
        System.out.println("1. Manage Doctors");
        System.out.println("2. Manage Patients");
        System.out.println("3. Approve Requests");
        System.out.println("4. View Appointments");
        System.out.println("5. View Logs");
        System.out.println("6. Logout");
        System.out.println("=====================");
        handleAdminMenuChoice(MainScheduler.getIntegerInput("Choice: "));
    }

    static void handleAdminMenuChoice(int choice) {
        try {
            switch (choice) {
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
                    break;
                default:
                    System.out.println("Invalid choice!");
            }
        } catch (Exception e) {
            System.out.println("Error occurred: " + e.getMessage());
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
        handleDoctorMenuChoice(MainScheduler.getIntegerInput("Choice: "));
    }

    static void handleDoctorMenuChoice(int choice) {
        switch (choice) {
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
                break;
            default:
                System.out.println("Invalid choice!");
        }
    }

    static void createDoctor() {
        String username = MainScheduler.getStringInput("Username: ");
        if (MainScheduler.users.containsKey(username)) {
            System.out.println("Username already taken!");
            return;
        }

        String password = MainScheduler.getValidPassword("Password: ");
        String specialty = selectSpecialty();
        String phone = MainScheduler.getValidPhoneNumber("Phone: ");
        String doctorID = "D" + String.format("%03d", MainScheduler.doctors.size() + 1);

        String[] userData = { String.valueOf(MainScheduler.USER_DOCTOR) };
        String[] doctorData = { doctorID, username, password, phone, "Active" };

        MainScheduler.users.put(username, userData);
        MainScheduler.doctors.add(doctorData);
        MainScheduler.recordLog("Added doctor: " + username);
        DataManager.saveAllData();
        System.out.println("Doctor added successfully!");
    }

    static String selectSpecialty() {
        String[] specialties = { "Cardiology", "Neurology", "Orthopedics", "Pediatrics", "General Medicine" };
        System.out.println("\nAvailable Specialties:");
        for (int i = 0; i < specialties.length; i++) {
            System.out.println((i + 1) + ". " + specialties[i]);
        }

        while (true) {
            int choice = MainScheduler.getIntegerInput("Select specialty number: ");
            if (choice >= 1 && choice <= specialties.length) {
                return specialties[choice - 1];
            }
            System.out.println("Please select a valid specialty!");
        }
    }

    static void displayDoctors() {
        if (MainScheduler.doctors.isEmpty()) {
            System.out.println("No doctors registered!");
            return;
        }

        System.out.println("\n----- Doctors List -----");
        System.out.printf("%-10s %-15s %-15s %-15s %-10s\n", "ID", "Name", "Phone", "Specialty", "Status");
        System.out.println("-------------------------------------------------");
        for (String[] doctor : MainScheduler.doctors) {
            String phone = formatPhoneNumber(doctor[3]);
            System.out.printf("%-10s %-15s %-15s %-15s %-10s\n",
                    doctor[0], doctor[1], phone, doctor[2], doctor[4]);
        }
    }

    static String formatPhoneNumber(String phone) {
        if (phone.length() == 10) {
            return phone.substring(0, 3) + "-" + phone.substring(3, 6) + "-" + phone.substring(6);
        }
        return phone;
    }

    static void editDoctor() {
        displayDoctors();
        String doctorID = MainScheduler.getStringInput("Enter Doctor ID: ");
        int index = findDoctorIndex(doctorID);

        if (index == -1) {
            System.out.println("Doctor not found!");
            return;
        }

        String[] doctor = MainScheduler.doctors.get(index);
        System.out.println("Current: Phone: " + doctor[3] + ", Specialty: " + doctor[2]);

        String phone = MainScheduler.getValidPhoneNumber("New Phone: ");
        if (!MainScheduler.isPhoneNumberUnique(phone)) {
            System.out.println("Phone number already in use!");
            return;
        }
        String specialty = selectSpecialty();

        doctor[3] = phone;
        doctor[2] = specialty;

        MainScheduler.recordLog("Doctor updated: " + doctorID);
        DataManager.saveAllData();
        System.out.println("Doctor updated successfully!");
    }

    static void deactivateDoctorAccount() {
        displayDoctors();
        String doctorID = MainScheduler.getStringInput("Enter Doctor ID: ");
        int index = findDoctorIndex(doctorID);

        if (index == -1) {
            System.out.println("Doctor not found!");
            return;
        }

        String[] doctor = MainScheduler.doctors.get(index);
        if (doctor[4].equals("Inactive")) {
            System.out.println("Doctor already deactivated!");
            return;
        }

        if (!confirmAction("deactivation of doctor " + doctorID)) {
            return;
        }

        doctor[4] = "Inactive";
        cancelDoctorAppointments(doctorID);

        MainScheduler.recordLog("Doctor deactivated: " + doctorID);
        DataManager.saveAllData();
        System.out.println("Doctor deactivated and all appointments cancelled!");
    }

    static void deleteDoctor() {
        displayDoctors();
        String doctorID = MainScheduler.getStringInput("Enter Doctor ID: ");
        int index = findDoctorIndex(doctorID);

        if (index == -1) {
            System.out.println("Doctor not found!");
            return;
        }

        ArrayList<MainScheduler.AppointmentNode> appointments = new ArrayList<>();
        UserFunctions.collectDoctorAppointments(doctorID, appointments);

        if (!appointments.isEmpty()) {
            System.out.println("Cannot remove: Doctor has active appointments!");
            return;
        }

        if (!confirmAction("removal of doctor " + doctorID)) {
            return;
        }

        MainScheduler.doctors.remove(index);
        String username = MainScheduler.users.entrySet().stream()
                .filter(e -> e.getValue()[0].equals(String.valueOf(MainScheduler.USER_DOCTOR)))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        if (username != null) {
            MainScheduler.users.remove(username);
        }

        LinkedList<String[]> remainingRequests = new LinkedList<>();
        for (String[] request : MainScheduler.appointmentRequests) {
            if (!request[2].equals(doctorID)) {
                remainingRequests.add(request);
            } else {
                MainScheduler.recordLog("Request cancelled: " + request[0]);
            }
        }
        MainScheduler.appointmentRequests = remainingRequests;

        MainScheduler.recordLog("Doctor removed: " + doctorID);
        DataManager.saveAllData();
        System.out.println("Doctor removed successfully!");
    }

    static void activateDoctorAccount() {
        ArrayList<String[]> inactiveDoctors = new ArrayList<>();
        for (String[] doctor : MainScheduler.doctors) {
            if (doctor[4].equals("Inactive")) {
                inactiveDoctors.add(doctor);
            }
        }

        if (inactiveDoctors.isEmpty()) {
            System.out.println("No inactive doctors!");
            return;
        }

        System.out.println("\n----- Inactive Doctors -----");
        System.out.printf("%-10s %-15s %-15s %-10s\n", "ID", "Phone", "Specialty", "Status");
        System.out.println("--------------------------------------------");
        for (String[] doctor : inactiveDoctors) {
            System.out.printf("%-10s %-15s %-15s %-10s\n", doctor[0], doctor[3], doctor[2], doctor[4]);
        }

        String doctorID = MainScheduler.getStringInput("Enter Doctor ID: ");
        int index = findDoctorIndex(doctorID);

        if (index == -1) {
            System.out.println("Doctor not found!");
            return;
        }

        String[] doctor = MainScheduler.doctors.get(index);
        if (doctor[4].equals("Active")) {
            System.out.println("Doctor already active!");
            return;
        }

        if (!confirmAction("activation of doctor " + doctorID)) {
            return;
        }

        doctor[4] = "Active";
        MainScheduler.recordLog("Doctor activated: " + doctorID);
        DataManager.saveAllData();
        System.out.println("Doctor activated successfully!");
    }

    static boolean confirmAction(String action) {
        String confirmation = MainScheduler.getStringInput(
                "Confirm " + action + "? (y/n): ");
        if (!confirmation.equalsIgnoreCase("y")) {
            System.out.println("Action cancelled!");
            return false;
        }
        return true;
    }

    static void cancelDoctorAppointments(String doctorID) {
        ArrayList<MainScheduler.AppointmentNode> appointments = new ArrayList<>();
        UserFunctions.collectDoctorAppointments(doctorID, appointments);

        for (MainScheduler.AppointmentNode appt : appointments) {
            if (!appt.appointmentStatus.equals("Cancelled")) {
                appt.appointmentStatus = "Cancelled";
                for (String[] record : MainScheduler.appointmentHistory) {
                    if (record[0].equals(appt.appointmentID)) {
                        record[5] = "Cancelled";
                        record[6] = new Date().toString();
                        break;
                    }
                }
                MainScheduler.recordLog("Appointment cancelled due to doctor deactivation: " + appt.appointmentID);
            }
        }
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
        handlePatientMenuChoice(MainScheduler.getIntegerInput("Choice: "));
    }

    static void handlePatientMenuChoice(int choice) {
        switch (choice) {
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
                break;
            default:
                System.out.println("Invalid choice!");
        }
    }

    static void displayPatients() {
        if (MainScheduler.patients.isEmpty()) {
            System.out.println("No patients registered!");
            return;
        }

        System.out.println("\n----- Patients List -----");
        System.out.printf("%-10s %-15s %-15s %-10s\n", "ID", "Username", "Phone", "Status");
        System.out.println("--------------------------------------------");
        for (String[] patient : MainScheduler.patients) {
            System.out.printf("%-10s %-15s %-15s %-10s\n", patient[0], patient[1], patient[3], patient[4]);
        }
    }

    static void editPatient() {
        displayPatients();
        String patientID = MainScheduler.getStringInput("Enter Patient ID: ");
        int index = findPatientIndex(patientID);

        if (index == -1) {
            System.out.println("Patient not found!");
            return;
        }

        String[] patient = MainScheduler.patients.get(index);
        System.out.println("Current: Phone: " + patient[3]);

        String phone = MainScheduler.getValidPhoneNumber("New Phone: ");
        if (!MainScheduler.isPhoneNumberUnique(phone)) {
            System.out.println("Phone number already in use!");
            return;
        }
        patient[3] = phone;

        MainScheduler.recordLog("Patient updated: " + patientID);
        DataManager.saveAllData();
        System.out.println("Patient updated successfully!");
    }

    static void deactivatePatientAccount() {
        displayPatients();
        String patientID = MainScheduler.getStringInput("Enter Patient ID: ");
        int index = findPatientIndex(patientID);

        if (index == -1) {
            System.out.println("Patient not found!");
            return;
        }

        String[] patient = MainScheduler.patients.get(index);
        if (patient[4].equals("Inactive")) {
            System.out.println("Patient already deactivated!");
            return;
        }

        if (!confirmAction("deactivation of patient " + patientID)) {
            return;
        }

        patient[4] = "Inactive";
        ArrayList<MainScheduler.AppointmentNode> appointments = new ArrayList<>();
        UserFunctions.collectPatientAppointments(patientID, appointments);

        for (MainScheduler.AppointmentNode appt : appointments) {
            if (!appt.appointmentStatus.equals("Cancelled")) {
                appt.appointmentStatus = "Cancelled";
                for (String[] record : MainScheduler.appointmentHistory) {
                    if (record[0].equals(appt.appointmentID)) {
                        record[5] = "Cancelled";
                        record[6] = new Date().toString();
                        break;
                    }
                }
                MainScheduler.recordLog("Appointment cancelled due to patient deactivation: " + appt.appointmentID);
            }
        }

        MainScheduler.recordLog("Patient deactivated: " + patientID);
        DataManager.saveAllData();
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
            System.out.println("No pending requests!");
            return;
        }

        ArrayList<String[]> requests = new ArrayList<>(MainScheduler.appointmentRequests);
        MainScheduler.appointmentRequests.clear();

        System.out.println("\n----- Pending Requests -----");
        System.out.printf("%-5s %-10s %-10s %-20s %-15s\n", "No.", "Patient", "Doctor", "Date", "Reason");
        System.out.println("-------------------------------------------------");
        for (int i = 0; i < requests.size(); i++) {
            String[] request = requests.get(i);
            System.out.printf("%-5d %-10s %-10s %-20s %-15s\n",
                    i + 1, request[1], request[2], request[3], request[4]);
        }

        System.out.println("\nOptions:");
        System.out.println("0: Approve all");
        System.out.println("-1: Reject all");
        System.out.println("1 to " + requests.size() + ": Approve specific request");

        int choice = MainScheduler.getIntegerInput("Enter choice: ");
        if (choice == 0) {
            if (confirmAction("approve all requests")) {
                for (String[] request : requests) {
                    approveRequest(request);
                }
                System.out.println("All requests approved!");
            } else {
                MainScheduler.appointmentRequests.addAll(requests);
            }
        } else if (choice == -1) {
            if (confirmAction("reject all requests")) {
                for (String[] request : requests) {
                    MainScheduler.recordLog("Request rejected: " + request[0]);
                }
                System.out.println("All requests rejected!");
            } else {
                MainScheduler.appointmentRequests.addAll(requests);
            }
        } else if (choice > 0 && choice <= requests.size()) {
            if (confirmAction("approve request " + choice)) {
                approveRequest(requests.get(choice - 1));
                for (int i = 0; i < requests.size(); i++) {
                    if (i != choice - 1) {
                        MainScheduler.appointmentRequests.add(requests.get(i));
                    }
                }
                System.out.println("Request approved!");
            } else {
                MainScheduler.appointmentRequests.addAll(requests);
            }
        } else {
            System.out.println("Invalid choice!");
            MainScheduler.appointmentRequests.addAll(requests);
        }

        DataManager.saveAllData();
    }

    static void approveRequest(String[] request) {
        try {
            Date date = MainScheduler.dateTimeFormat.parse(request[3]);
            if (UserFunctions.hasSchedulingConflict(request[2], date)) {
                MainScheduler.recordLog("Request rejected due to scheduling conflict: " + request[0]);
                return;
            }

            String appointmentID = "A" + String.format("%03d", MainScheduler.nextAppointmentID++);
            MainScheduler.AppointmentNode node = new MainScheduler.AppointmentNode(
                    appointmentID, request[1], request[2], date, request[4], "Scheduled");

            DataManager.insertAppointmentNode(node);

            String[] record = { appointmentID, request[1], request[2], request[3], request[4], "Scheduled",
                    new Date().toString() };
            MainScheduler.appointmentHistory.add(record);

            MainScheduler.recordLog("Appointment approved: " + appointmentID);
        } catch (ParseException e) {
            MainScheduler.recordLog("Failed to approve request " + request[0] + ": Invalid date format");
        }
    }

    static void displayAllAppointments() {
        if (MainScheduler.appointments.isEmpty()) {
            System.out.println("No appointments!");
            return;
        }

        System.out.println("\n----- All Appointments -----");
        System.out.printf("%-10s %-15s %-15s %-20s %-15s %-10s\n",
                "ID", "Patient", "Doctor", "Date", "Reason", "Status");
        System.out.println("-------------------------------------------------");
        for (MainScheduler.AppointmentNode node : MainScheduler.appointments) {
            System.out.printf("%-10s %-15s %-15s %-20s %-15s %-10s\n",
                    node.appointmentID, node.patientID, node.doctorID,
                    MainScheduler.dateTimeFormat.format(node.appointmentDate),
                    node.appointmentReason, node.appointmentStatus);
        }
    }

    static void displayLogs() {
        if (MainScheduler.operationLogs.isEmpty()) {
            System.out.println("No logs available!");
            return;
        }

        System.out.println("\n----- Operation Logs -----");
        for (String log : MainScheduler.operationLogs) {
            System.out.println(log);
        }
    }
}