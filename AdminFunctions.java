package trial;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

public class AdminFunctions {
    static void showAdminMenu() {
        System.out.println("\n===== Admin Menu =====");
        System.out.println("1. Manage Doctors");
        System.out.println("2. Manage Patients");
        System.out.println("3. View Appointments");
        System.out.println("4. View Logs");
        System.out.println("5. Logout");
        System.out.println("=====================");
        int choice = Utilities.getIntegerInput("Choice: ");
        if (choice < 1 || choice > 5) {
            System.out.println("Invalid choice!");
            showAdminMenu();
            return;
        }
        handleAdminMenuChoice(choice);
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
                    displayAllAppointments();
                    break;
                case 4:
                    displayLogs();
                    break;
                case 5:
                    MainScheduler.performLogout();
                    break;
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
        int choice = Utilities.getIntegerInput("Choice: ");
        if (choice < 1 || choice > 7) {
            System.out.println("Invalid choice!");
            manageDoctorRecords();
            return;
        }
        handleDoctorMenuChoice(choice);
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
        }
    }

    static void createDoctor() {
        String displayName = Utilities.getStringInput("Full Name: ");
        String username = displayName.replaceAll("\\s+", "").toLowerCase();
        if (!Utilities.isValidUsername(username)) {
            System.out.println("Generated username must be alphanumeric or contain spaces! Try a different name.");
            createDoctor();
            return;
        }
        if (MainScheduler.users.containsKey(username)) {
            System.out.println("Generated username already taken! Try a different name.");
            createDoctor();
            return;
        }
        String password = Utilities.getValidPassword("Password: ");
        String specialty = selectSpecialty();
        String phone = Utilities.getValidPhoneNumber("Phone: ");
        String experience = Utilities.getValidExperience("Years of Experience: ");
        String doctorID = "D" + String.format("%03d", MainScheduler.doctors.size() + 1);

        String[] userData = { String.valueOf(MainScheduler.USER_DOCTOR), password };
        String[] doctorData = { doctorID, username, password, phone, "Active", specialty, experience, displayName };

        MainScheduler.users.put(username, userData);
        MainScheduler.doctors.add(doctorData);
        MainScheduler.recordLog("Added doctor: " + displayName);
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
            int choice = Utilities.getIntegerInput("Select specialty number: ");
            if (choice >= 1 && choice <= specialties.length) {
                return specialties[choice - 1];
            }
            System.out.println("Please select a valid specialty (1-" + specialties.length + ")!");
        }
    }

    static void displayDoctors() {
        if (MainScheduler.doctors.isEmpty()) {
            System.out.println("No doctors registered!");
            return;
        }

        System.out.println("\n----- Doctors List -----");
        System.out.printf("%-10s %-20s %-15s %-15s %-10s %-10s\n", "ID", "Name", "Phone", "Specialty", "Status", "Exp (Yrs)");
        System.out.println("------------------------------------------------------------------------");
        for (String[] doctor : MainScheduler.doctors) {
            String phone = Utilities.formatPhoneNumber(doctor[3]);
            String displayName = doctor.length > 7 ? doctor[7] : doctor[1];
            System.out.printf("%-10s %-20s %-15s %-15s %-10s %-10s\n",
                    doctor[0], displayName, phone, doctor[5], doctor[4], doctor[6]);
        }
    }

    static void editDoctor() {
        displayDoctors();
        String doctorID = getValidDoctorID("Enter Doctor ID: ");
        int index = findDoctorIndex(doctorID);

        String[] doctor = MainScheduler.doctors.get(index);
        String displayName = doctor.length > 7 ? doctor[7] : doctor[1];
        System.out.println("Current: Phone: " + doctor[3] + ", Specialty: " + doctor[5] + ", Experience: " + doctor[6] + ", Name: " + displayName);

        String phone = Utilities.getValidPhoneNumber("New Phone: ");
        if (!Utilities.isPhoneNumberUniqueForEdit(phone, doctorID, true)) {
            System.out.println("Phone number already in use!");
            editDoctor();
            return;
        }
        String specialty = selectSpecialty();
        String experience = Utilities.getValidExperience("New Years of Experience: ");
        String newDisplayName = Utilities.getStringInput("New Full Name: ");

        doctor[3] = phone;
        doctor[5] = specialty;
        doctor[6] = experience;
        doctor[7] = newDisplayName;

        MainScheduler.recordLog("Doctor updated: " + newDisplayName);
        DataManager.saveAllData();
        System.out.println("Doctor updated successfully!");
    }

    static String getValidDoctorID(String prompt) {
        while (true) {
            String doctorID = Utilities.getStringInput(prompt);
            if (findDoctorIndex(doctorID) != -1) {
                return doctorID;
            }
            System.out.println("Doctor not found!");
        }
    }

    static void deactivateDoctorAccount() {
        displayDoctors();
        String doctorID = getValidDoctorID("Enter Doctor ID: ");
        int index = findDoctorIndex(doctorID);

        String[] doctor = MainScheduler.doctors.get(index);
        if (doctor[4].equals("Inactive")) {
            System.out.println("Doctor already deactivated!");
            return;
        }

        String displayName = doctor.length > 7 ? doctor[7] : doctor[1];
        if (!Utilities.confirmAction("deactivation of doctor " + displayName)) {
            return;
        }

        doctor[4] = "Inactive";
        cancelDoctorAppointments(doctorID);

        MainScheduler.recordLog("Doctor deactivated: " + displayName);
        DataManager.saveAllData();
        System.out.println("Doctor deactivated and all appointments cancelled!");
    }

    static void deleteDoctor() {
        displayDoctors();
        String doctorID = getValidDoctorID("Enter Doctor ID: ");
        int index = findDoctorIndex(doctorID);

        ArrayList<MainScheduler.AppointmentNode> appointments = new ArrayList<>();
        UserFunctions.collectDoctorAppointments(doctorID, appointments);

        if (!appointments.isEmpty()) {
            System.out.println("Cannot remove: Doctor has active appointments!");
            return;
        }

        String[] doctor = MainScheduler.doctors.get(index);
        String displayName = doctor.length > 7 ? doctor[7] : doctor[1];
        if (!Utilities.confirmAction("removal of doctor " + displayName)) {
            return;
        }

        String username = doctor[1];
        MainScheduler.doctors.remove(index);
        MainScheduler.users.remove(username);

        MainScheduler.recordLog("Doctor removed: " + displayName);
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
        System.out.printf("%-10s %-20s %-15s %-15s %-10s\n", "ID", "Name", "Phone", "Specialty", "Exp (Yrs)");
        System.out.println("------------------------------------------------------------");
        for (String[] doctor : inactiveDoctors) {
            String displayName = doctor.length > 7 ? doctor[7] : doctor[1];
            System.out.printf("%-10s %-20s %-15s %-15s %-10s\n", doctor[0], displayName, doctor[3], doctor[5], doctor[6]);
        }

        String doctorID = getValidDoctorID("Enter Doctor ID: ");
        int index = findDoctorIndex(doctorID);

        String[] doctor = MainScheduler.doctors.get(index);
        if (doctor[4].equals("Active")) {
            System.out.println("Doctor already active!");
            return;
        }

        String displayName = doctor.length > 7 ? doctor[7] : doctor[1];
        if (!Utilities.confirmAction("activation of doctor " + displayName)) {
            return;
        }

        doctor[4] = "Active";
        MainScheduler.recordLog("Doctor activated: " + displayName);
        DataManager.saveAllData();
        System.out.println("Doctor activated successfully!");
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
        System.out.println("4. Delete Patient");
        System.out.println("5. Back");
        System.out.println("--------------------------");
        int choice = Utilities.getIntegerInput("Choice: ");
        if (choice < 1 || choice > 5) {
            System.out.println("Invalid choice!");
            managePatientRecords();
            return;
        }
        handlePatientMenuChoice(choice);
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
                deletePatient();
                break;
            case 5:
                break;
        }
    }

    static void displayPatients() {
        if (MainScheduler.patients.isEmpty()) {
            System.out.println("No patients registered!");
            return;
        }

        System.out.println("\n----- Patients List -----");
        System.out.printf("%-10s %-20s %-20s %-15s %-10s\n", "ID", "Username", "Name", "Phone", "Status");
        System.out.println("----------------------------------------------------------------");
        for (String[] patient : MainScheduler.patients) {
            String displayName = patient.length > 5 ? patient[5] : patient[1];
            System.out.printf("%-10s %-20s %-20s %-15s %-10s\n", patient[0], patient[1], displayName, patient[3], patient[4]);
        }
    }

    static void editPatient() {
        displayPatients();
        String patientID = getValidPatientID("Enter Patient ID: ");
        int index = findPatientIndex(patientID);

        String[] patient = MainScheduler.patients.get(index);
        String displayName = patient.length > 5 ? patient[5] : patient[1];
        System.out.println("Current: Phone: " + patient[3] + ", Name: " + displayName);

        String phone = Utilities.getValidPhoneNumber("New Phone: ");
        if (!Utilities.isPhoneNumberUniqueForEdit(phone, patientID, false)) {
            System.out.println("Phone number already in use!");
            editPatient();
            return;
        }
        String newDisplayName = Utilities.getStringInput("New Full Name: ");

        patient[3] = phone;
        patient[5] = newDisplayName;

        MainScheduler.recordLog("Patient updated: " + newDisplayName);
        DataManager.saveAllData();
        System.out.println("Patient updated successfully!");
    }

    static String getValidPatientID(String prompt) {
        while (true) {
            String patientID = Utilities.getStringInput(prompt);
            if (findPatientIndex(patientID) != -1) {
                return patientID;
            }
            System.out.println("Patient not found!");
        }
    }

    static void deactivatePatientAccount() {
        displayPatients();
        String patientID = getValidPatientID("Enter Patient ID: ");
        int index = findPatientIndex(patientID);

        String[] patient = MainScheduler.patients.get(index);
        if (patient[4].equals("Inactive")) {
            System.out.println("Patient already deactivated!");
            return;
        }

        String displayName = patient.length > 5 ? patient[5] : patient[1];
        if (!Utilities.confirmAction("deactivation of patient " + displayName)) {
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

        MainScheduler.recordLog("Patient deactivated: " + displayName);
        DataManager.saveAllData();
        System.out.println("Patient deactivated and all appointments cancelled!");
    }

    static void deletePatient() {
        displayPatients();
        String patientID = getValidPatientID("Enter Patient ID: ");
        int index = findPatientIndex(patientID);

        ArrayList<MainScheduler.AppointmentNode> appointments = new ArrayList<>();
        UserFunctions.collectPatientAppointments(patientID, appointments);

        if (!appointments.isEmpty()) {
            System.out.println("Cannot remove: Patient has appointments!");
            return;
        }

        String[] patient = MainScheduler.patients.get(index);
        String displayName = patient.length > 5 ? patient[5] : patient[1];
        if (!Utilities.confirmAction("removal of patient " + displayName)) {
            return;
        }

        String username = patient[1];
        MainScheduler.patients.remove(index);
        MainScheduler.users.remove(username);

        MainScheduler.recordLog("Patient removed: " + displayName);
        DataManager.saveAllData();
        System.out.println("Patient removed successfully!");
    }

    static int findPatientIndex(String patientID) {
        for (int i = 0; i < MainScheduler.patients.size(); i++) {
            if (MainScheduler.patients.get(i)[0].equals(patientID)) {
                return i;
            }
        }
        return -1;
    }

    static void createAppointment(String patientID, String doctorID, String dateTimeStr, String reason) {
        try {
            Date date = MainScheduler.dateTimeFormat.parse(dateTimeStr);
            if (UserFunctions.hasSchedulingConflict(doctorID, date)) {
                MainScheduler.recordLog("Appointment rejected due to scheduling conflict for patient: " + patientID);
                System.out.println("Scheduling conflict! Please choose another time.");
                return;
            }
            for (String[] existing : MainScheduler.appointmentHistory) {
                if (existing[1].equals(patientID) && existing[2].equals(doctorID) && existing[3].equals(dateTimeStr)) {
                    MainScheduler.recordLog("Duplicate appointment rejected for patient: " + patientID);
                    System.out.println("Appointment already exists!");
                    return;
                }
            }
            String appointmentID = "A" + String.format("%03d", MainScheduler.nextAppointmentID++);
            MainScheduler.AppointmentNode node = new MainScheduler.AppointmentNode(
                    appointmentID, patientID, doctorID, date, reason, "Scheduled");

            DataManager.insertAppointmentNode(node);

            String[] record = { appointmentID, patientID, doctorID, dateTimeStr, reason, "Scheduled", new Date().toString() };
            MainScheduler.appointmentHistory.add(record);

            MainScheduler.recordLog("Appointment created: " + appointmentID);
        } catch (ParseException e) {
            MainScheduler.recordLog("Failed to create appointment for patient " + patientID + ": Invalid date format");
            System.out.println("Invalid date format!");
        }
    }

    static void displayAllAppointments() {
        if (MainScheduler.appointments.isEmpty()) {
            System.out.println("No appointments!");
            return;
        }

        System.out.println("\n----- All Appointments -----");
        System.out.printf("%-10s %-15s %-15s %-20s %-15s %-10s\n", "ID", "Patient", "Doctor", "Date", "Reason", "Status");
        System.out.println("------------------------------------------------------------");
        for (MainScheduler.AppointmentNode node : MainScheduler.appointments) {
            String patientDisplay = node.patientID;
            String doctorDisplay = node.doctorID;
            for (String[] patient : MainScheduler.patients) {
                if (patient[0].equals(node.patientID)) {
                    patientDisplay = patient.length > 5 ? patient[5] : patient[1];
                    break;
                }
            }
            for (String[] doctor : MainScheduler.doctors) {
                if (doctor[0].equals(node.doctorID)) {
                    doctorDisplay = doctor.length > 7 ? doctor[7] : doctor[1];
                    break;
                }
            }
            System.out.printf("%-10s %-15s %-15s %-20s %-15s %-10s\n",
                    node.appointmentID, patientDisplay, doctorDisplay,
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