package trial;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

public class UserFunctions {
    static void showDoctorMenu() {
        System.out.println("\n===== Doctor Menu =====");
        System.out.println("1. View Appointments");
        System.out.println("2. Cancel Appointment");
        System.out.println("3. Patient History");
        System.out.println("4. Update Profile");
        System.out.println("5. Logout");
        System.out.println("======================");
        int choice = Utilities.getIntegerInput("Choice: ");
        if (choice < 1 || choice > 5) {
            System.out.println("Invalid choice!");
            showDoctorMenu();
            return;
        }
        handleDoctorMenuChoice(choice);
    }

    static void handleDoctorMenuChoice(int choice) {
        switch (choice) {
            case 1:
                displayDoctorAppointments();
                break;
            case 2:
                cancelDoctorAppointment();
                break;
            case 3:
                displayPatientHistory();
                break;
            case 4:
                editDoctorProfile();
                break;
            case 5:
                MainScheduler.performLogout();
                break;
        }
    }

    static void displayDoctorAppointments() {
        ArrayList<MainScheduler.AppointmentNode> appointments = new ArrayList<>();
        collectDoctorAppointments(MainScheduler.userID, appointments);

        if (appointments.isEmpty()) {
            System.out.println("No active appointments!");
            return;
        }

        System.out.println("\n----- My Scheduled Appointments -----");
        System.out.printf("%-10s %-15s %-20s %-15s %-10s\n", "ID", "Patient", "Date", "Reason", "Status");
        System.out.println("------------------------------------------------------------");
        appointments.sort((a1, a2) -> a1.appointmentDate.compareTo(a2.appointmentDate));
        for (MainScheduler.AppointmentNode appt : appointments) {
            String patientDisplay = appt.patientID;
            for (String[] patient : MainScheduler.patients) {
                if (patient[0].equals(appt.patientID)) {
                    patientDisplay = patient.length > 5 ? patient[5] : patient[1];
                    break;
                }
            }
            System.out.printf("%-10s %-15s %-20s %-15s %-10s\n",
                    appt.appointmentID, patientDisplay,
                    MainScheduler.dateTimeFormat.format(appt.appointmentDate),
                    appt.appointmentReason, appt.appointmentStatus);
        }
    }

    static void collectDoctorAppointments(String doctorID, ArrayList<MainScheduler.AppointmentNode> appointments) {
        for (MainScheduler.AppointmentNode node : MainScheduler.appointments) {
            if (node.doctorID.equals(doctorID) && !node.appointmentStatus.equals("Cancelled")) {
                appointments.add(node);
            }
        }
    }

    static void cancelDoctorAppointment() {
        ArrayList<MainScheduler.AppointmentNode> appointments = new ArrayList<>();
        collectDoctorAppointments(MainScheduler.userID, appointments);

        if (appointments.isEmpty()) {
            System.out.println("No appointments to cancel!");
            return;
        }

        displayAppointmentsForCancellation(appointments);
        int choice = Utilities.getIntegerInput("Enter number to cancel (0 to back): ");
        if (choice == 0)
            return;

        if (choice > 0 && choice <= appointments.size()) {
            MainScheduler.AppointmentNode appt = appointments.get(choice - 1);
            if (Utilities.confirmAction("cancellation of appointment " + appt.appointmentID)) {
                cancelAppointment(appt);
                System.out.println("Appointment cancelled!");
            }
        } else {
            System.out.println("Invalid selection!");
            cancelDoctorAppointment();
        }
    }

    static void displayAppointmentsForCancellation(ArrayList<MainScheduler.AppointmentNode> appointments) {
        System.out.println("\n----- My Appointments -----");
        System.out.printf("%-5s %-10s %-15s %-20s %-15s\n", "No.", "ID", "Patient", "Date", "Reason");
        System.out.println("------------------------------------------------------------");
        for (int i = 0; i < appointments.size(); i++) {
            MainScheduler.AppointmentNode appt = appointments.get(i);
            String patientDisplay = appt.patientID;
            for (String[] patient : MainScheduler.patients) {
                if (patient[0].equals(appt.patientID)) {
                    patientDisplay = patient.length > 5 ? patient[5] : patient[1];
                    break;
                }
            }
            System.out.printf("%-5d %-10s %-15s %-20s %-15s\n",
                    i + 1, appt.appointmentID, patientDisplay,
                    MainScheduler.dateTimeFormat.format(appt.appointmentDate),
                    appt.appointmentReason);
        }
    }

    static void cancelAppointment(MainScheduler.AppointmentNode appt) {
        appt.appointmentStatus = "Cancelled";
        for (MainScheduler.AppointmentNode node : MainScheduler.appointments) {
            if (node.appointmentID.equals(appt.appointmentID)) {
                node.appointmentStatus = "Cancelled";
                break;
            }
        }
        for (String[] record : MainScheduler.appointmentHistory) {
            if (record[0].equals(appt.appointmentID)) {
                record[5] = "Cancelled";
                record[6] = new Date().toString();
                break;
            }
        }
        MainScheduler.recordLog("Appointment cancelled: " + appt.appointmentID);
        DataManager.saveAllData();
    }

    static void displayPatientHistory() {
        AdminFunctions.displayPatients();
        String patientID = AdminFunctions.getValidPatientID("Enter Patient ID: ");

        System.out.println("\n----- Patient History -----");
        System.out.printf("%-10s %-15s %-20s %-15s %-10s %-20s\n", "ID", "Doctor", "Date", "Reason", "Status", "Updated");
        System.out.println("--------------------------------------------------------------------");
        boolean hasHistory = false;
        for (String[] record : MainScheduler.appointmentHistory) {
            if (record[1].equals(patientID)) {
                String doctorDisplay = record[2];
                for (String[] doctor : MainScheduler.doctors) {
                    if (doctor[0].equals(record[2])) {
                        doctorDisplay = doctor.length > 7 ? doctor[7] : doctor[1];
                        break;
                    }
                }
                System.out.printf("%-10s %-15s %-20s %-15s %-10s %-20s\n",
                        record[0], doctorDisplay, record[3], record[4], record[5], record[6]);
                hasHistory = true;
            }
        }
        if (!hasHistory) {
            System.out.println("No appointment history!");
        }
    }

    static void editDoctorProfile() {
        int index = AdminFunctions.findDoctorIndex(MainScheduler.userID);
        if (index == -1) {
            System.out.println("Doctor profile not found!");
            return;
        }

        String[] doctor = MainScheduler.doctors.get(index);
        String displayName = doctor.length > 7 ? doctor[7] : doctor[1];
        System.out.println("Current: Phone: " + doctor[3] + ", Specialty: " + doctor[5] + ", Experience: " + doctor[6] + ", Name: " + displayName);

        String phone = Utilities.getValidPhoneNumber("New Phone: ");
        if (!Utilities.isPhoneNumberUniqueForEdit(phone, MainScheduler.userID, true)) {
            System.out.println("Phone number already in use!");
            editDoctorProfile();
            return;
        }
        String specialty = AdminFunctions.selectSpecialty();
        String experience = Utilities.getValidExperience("New Years of Experience: ");
        String newDisplayName = Utilities.getStringInput("New Full Name: ");

        doctor[3] = phone;
        doctor[5] = specialty;
        doctor[6] = experience;
        doctor[7] = newDisplayName;

        MainScheduler.recordLog("Doctor profile updated: " + newDisplayName);
        DataManager.saveAllData();
        System.out.println("Profile updated successfully!");
    }

    static void showPatientMenu() {
        System.out.println("\n===== Patient Menu =====");
        System.out.println("1. View Doctors");
        if (!MainScheduler.doctors.isEmpty()) {
            System.out.println("2. Book Appointment");
        }
        System.out.println((MainScheduler.doctors.isEmpty() ? "2" : "3") + ". View Appointments");
        System.out.println((MainScheduler.doctors.isEmpty() ? "3" : "4") + ". Cancel Appointment");
        System.out.println((MainScheduler.doctors.isEmpty() ? "4" : "5") + ". Update Profile");
        System.out.println((MainScheduler.doctors.isEmpty() ? "5" : "6") + ". Logout");
        System.out.println("=======================");
        int choice = Utilities.getIntegerInput("Choice: ");
        int maxChoice = MainScheduler.doctors.isEmpty() ? 5 : 6;
        if (choice < 1 || choice > maxChoice) {
            System.out.println("Invalid choice!");
            showPatientMenu();
            return;
        }
        handlePatientMenuChoice(choice);
    }

    static void handlePatientMenuChoice(int choice) {
        int adjustedChoice = choice;
        if (MainScheduler.doctors.isEmpty()) {
            if (choice > 1)
                adjustedChoice++;
        }
        switch (adjustedChoice) {
            case 1:
                displayDoctors();
                break;
            case 2:
                if (!MainScheduler.doctors.isEmpty()) {
                    bookAppointment();
                } else {
                    displayPatientAppointments();
                }
                break;
            case 3:
                displayPatientAppointments();
                break;
            case 4:
                cancelPatientAppointment();
                break;
            case 5:
                editPatientProfile();
                break;
            case 6:
                MainScheduler.performLogout();
                break;
        }
    }

    static void displayDoctors() {
        if (MainScheduler.doctors.isEmpty()) {
            System.out.println("No doctors available!");
            return;
        }

        String specialty = AdminFunctions.selectSpecialty();
        ArrayList<String[]> specialtyDoctors = new ArrayList<>();
        for (String[] doctor : MainScheduler.doctors) {
            if (doctor[4].equals("Active") && doctor[5].equals(specialty)) {
                specialtyDoctors.add(doctor);
            }
        }

        if (specialtyDoctors.isEmpty()) {
            System.out.println("No active doctors available for " + specialty + "!");
            return;
        }

        System.out.println("\n----- Available " + specialty + " Doctors -----");
        System.out.printf("%-10s %-20s %-15s %-15s %-10s\n", "ID", "Name", "Phone", "Specialty", "Exp (Yrs)");
        System.out.println("------------------------------------------------------------");
        for (String[] doctor : specialtyDoctors) {
            String phone = Utilities.formatPhoneNumber(doctor[3]);
            String displayName = doctor.length > 7 ? doctor[7] : doctor[1];
            System.out.printf("%-10s %-20s %-15s %-15s %-10s\n",
                    doctor[0], displayName, phone, doctor[5], doctor[6]);
        }
    }

    static void bookAppointment() {
        System.out.println("Note: Appointments are 30 minutes long.");
        String specialty = AdminFunctions.selectSpecialty();
        ArrayList<String[]> specialtyDoctors = new ArrayList<>();
        for (String[] doctor : MainScheduler.doctors) {
            if (doctor[4].equals("Active") && doctor[5].equals(specialty)) {
                specialtyDoctors.add(doctor);
            }
        }

        if (specialtyDoctors.isEmpty()) {
            System.out.println("No active doctors available for " + specialty + "!");
            return;
        }

        System.out.println("\n----- Available " + specialty + " Doctors -----");
        System.out.printf("%-10s %-20s %-15s %-15s %-10s\n", "ID", "Name", "Phone", "Specialty", "Exp (Yrs)");
        System.out.println("------------------------------------------------------------");
        for (String[] doctor : specialtyDoctors) {
            String phone = Utilities.formatPhoneNumber(doctor[3]);
            String displayName = doctor.length > 7 ? doctor[7] : doctor[1];
            System.out.printf("%-10s %-20s %-15s %-15s %-10s\n",
                    doctor[0], displayName, phone, doctor[5], doctor[6]);
        }

        String doctorID = getValidDoctorIDForSpecialty("Doctor ID: ", specialty);
        String dateStr = Utilities.getValidFutureAppointmentDate("Date (dd/MM/yyyy): ");
        Date date;
        try {
            date = MainScheduler.dateOnlyFormat.parse(dateStr);
        } catch (ParseException e) {
            System.out.println("Invalid date format!");
            bookAppointment();
            return;
        }

        String time = getAvailableSlot(doctorID, date);
        if (time == null) {
            System.out.println("No available slots!");
            bookAppointment();
            return;
        }

        String dateTimeStr = dateStr + " " + time;
        String reason = Utilities.getStringInput("Reason: ");
        if (reason.trim().isEmpty()) {
            System.out.println("Reason cannot be empty!");
            bookAppointment();
            return;
        }

        AdminFunctions.createAppointment(MainScheduler.userID, doctorID, dateTimeStr, reason);
        DataManager.saveAllData();
        System.out.println("Appointment booked successfully!");
    }

    static String getValidDoctorIDForSpecialty(String prompt, String specialty) {
        while (true) {
            String doctorID = Utilities.getStringInput(prompt);
            int index = AdminFunctions.findDoctorIndex(doctorID);
            if (index != -1 && MainScheduler.doctors.get(index)[4].equals("Active") &&
                    MainScheduler.doctors.get(index)[5].equals(specialty)) {
                return doctorID;
            }
            System.out.println("Invalid or inactive doctor for selected specialty!");
        }
    }

    static String getAvailableSlot(String doctorID, Date date) {
        String[] slots = { "09:00", "09:30", "10:00", "10:30", "11:00", "11:30", "12:00", "12:30",
                "13:00", "13:30", "14:00", "14:30", "15:00", "15:30", "16:00", "16:30", "17:00" };
        ArrayList<String> available = new ArrayList<>();

        for (String slot : slots) {
            try {
                Date slotTime = MainScheduler.dateTimeFormat.parse(
                        MainScheduler.dateOnlyFormat.format(date) + " " + slot);
                if (!hasSchedulingConflict(doctorID, slotTime)) {
                    available.add(slot);
                }
            } catch (ParseException e) {
            }
        }

        if (available.isEmpty()) {
            return null;
        }

        System.out.println("\n----- Available Slots -----");
        for (int i = 0; i < available.size(); i++) {
            System.out.println((i + 1) + ". " + available.get(i));
        }

        while (true) {
            int choice = Utilities.getIntegerInput("Select slot number (0 to back): ");
            if (choice == 0) {
                return null;
            }
            if (choice >= 1 && choice <= available.size()) {
                return available.get(choice - 1);
            }
            System.out.println("Please select a valid slot number (1-" + available.size() + ") or 0 to back!");
        }
    }

    static boolean hasSchedulingConflict(String doctorID, Date date) {
        ArrayList<MainScheduler.AppointmentNode> appointments = new ArrayList<>();
        collectDoctorAppointments(doctorID, appointments);

        for (MainScheduler.AppointmentNode appt : appointments) {
            if (!appt.appointmentStatus.equals("Cancelled")) {
                long diff = Math.abs(appt.appointmentDate.getTime() - date.getTime());
                if (diff < 1800000) {
                    return true;
                }
            }
        }
        return false;
    }

    static void displayPatientAppointments() {
        ArrayList<MainScheduler.AppointmentNode> appointments = new ArrayList<>();
        collectPatientAppointments(MainScheduler.userID, appointments);

        if (appointments.isEmpty()) {
            System.out.println("No appointments!");
            return;
        }

        System.out.println("\n----- My Appointments -----");
        System.out.printf("%-10s %-15s %-20s %-15s %-10s\n", "ID", "Doctor", "Date", "Reason", "Status");
        System.out.println("------------------------------------------------------------");
        appointments.sort((a1, a2) -> a1.appointmentDate.compareTo(a2.appointmentDate));
        for (MainScheduler.AppointmentNode appt : appointments) {
            String doctorDisplay = appt.doctorID;
            for (String[] doctor : MainScheduler.doctors) {
                if (doctor[0].equals(appt.doctorID)) {
                    doctorDisplay = doctor.length > 7 ? doctor[7] : doctor[1];
                    break;
                }
            }
            System.out.printf("%-10s %-15s %-20s %-15s %-10s\n",
                    appt.appointmentID, doctorDisplay,
                    MainScheduler.dateTimeFormat.format(appt.appointmentDate),
                    appt.appointmentReason, appt.appointmentStatus);
        }
    }

    static void collectPatientAppointments(String patientID, ArrayList<MainScheduler.AppointmentNode> appointments) {
        for (MainScheduler.AppointmentNode node : MainScheduler.appointments) {
            if (node.patientID.equals(patientID)) {
                appointments.add(node);
            }
        }
    }

    static void cancelPatientAppointment() {
        ArrayList<MainScheduler.AppointmentNode> appointments = new ArrayList<>();
        collectPatientAppointments(MainScheduler.userID, appointments);

        ArrayList<MainScheduler.AppointmentNode> cancellable = new ArrayList<>();
        for (MainScheduler.AppointmentNode appt : appointments) {
            if (appt.appointmentStatus.equals("Scheduled")) {
                cancellable.add(appt);
            }
        }

        if (cancellable.isEmpty()) {
            System.out.println("No scheduled appointments to cancel!");
            return;
        }

        System.out.println("\n----- My Appointments -----");
        System.out.printf("%-5s %-10s %-15s %-20s %-15s %-10s\n", "No.", "ID", "Doctor", "Date", "Reason", "Status");
        System.out.println("--------------------------------------------------------------------");
        for (int i = 0; i < cancellable.size(); i++) {
            MainScheduler.AppointmentNode appt = cancellable.get(i);
            String doctorDisplay = appt.doctorID;
            for (String[] doctor : MainScheduler.doctors) {
                if (doctor[0].equals(appt.doctorID)) {
                    doctorDisplay = doctor.length > 7 ? doctor[7] : doctor[1];
                    break;
                }
            }
            System.out.printf("%-5d %-10s %-15s %-20s %-15s %-10s\n",
                    i + 1, appt.appointmentID, doctorDisplay,
                    MainScheduler.dateTimeFormat.format(appt.appointmentDate),
                    appt.appointmentReason, appt.appointmentStatus);
        }

        int choice = Utilities.getIntegerInput("Enter number to cancel (0 to back): ");
        if (choice == 0)
            return;

        if (choice > 0 && choice <= cancellable.size()) {
            MainScheduler.AppointmentNode appt = cancellable.get(choice - 1);
            if (Utilities.confirmAction("cancellation of appointment " + appt.appointmentID)) {
                long timeDiff = appt.appointmentDate.getTime() - new Date().getTime();
                if (timeDiff < 86400000) {
                    System.out.println("Cannot cancel: Less than 24 hours until appointment!");
                    cancelPatientAppointment();
                    return;
                }
                cancelAppointment(appt);
                System.out.println("Appointment cancelled!");
            }
        } else {
            System.out.println("Invalid selection!");
            cancelPatientAppointment();
        }
    }

    static void editPatientProfile() {
        int index = AdminFunctions.findPatientIndex(MainScheduler.userID);
        if (index == -1) {
            System.out.println("Patient profile not found!");
            return;
        }

        String[] patient = MainScheduler.patients.get(index);
        String displayName = patient.length > 5 ? patient[5] : patient[1];
        System.out.println("Current: Phone: " + patient[3] + ", Name: " + displayName);

        String phone = Utilities.getValidPhoneNumber("New Phone: ");
        if (!Utilities.isPhoneNumberUniqueForEdit(phone, MainScheduler.userID, false)) {
            System.out.println("Phone number already in use!");
            editPatientProfile();
            return;
        }
        String newDisplayName = Utilities.getStringInput("New Full Name: ");

        patient[3] = phone;
        patient[5] = newDisplayName;

        MainScheduler.recordLog("Patient profile updated: " + newDisplayName);
        DataManager.saveAllData();
        System.out.println("Profile updated successfully!");
    }
}