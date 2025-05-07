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
        handleDoctorMenuChoice(MainScheduler.getIntegerInput("Choice: "));
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
            default:
                System.out.println("Invalid choice!");
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
        System.out.println("-------------------------------------------------");
        appointments.sort((a1, a2) -> a1.appointmentDate.compareTo(a2.appointmentDate));
        for (MainScheduler.AppointmentNode appt : appointments) {
            System.out.printf("%-10s %-15s %-20s %-15s %-10s\n",
                    appt.appointmentID, appt.patientID,
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
        int choice = MainScheduler.getIntegerInput("Enter number to cancel (0 to back): ");
        if (choice == 0)
            return;

        if (choice > 0 && choice <= appointments.size()) {
            MainScheduler.AppointmentNode appt = appointments.get(choice - 1);
            if (confirmCancellation(appt.appointmentID)) {
                cancelAppointment(appt);
                System.out.println("Appointment cancelled!");
            }
        } else {
            System.out.println("Invalid selection!");
        }
    }

    static void displayAppointmentsForCancellation(ArrayList<MainScheduler.AppointmentNode> appointments) {
        System.out.println("\n----- My Appointments -----");
        System.out.printf("%-5s %-10s %-15s %-20s %-15s\n", "No.", "ID", "Patient", "Date", "Reason");
        System.out.println("-------------------------------------------------");
        for (int i = 0; i < appointments.size(); i++) {
            MainScheduler.AppointmentNode appt = appointments.get(i);
            System.out.printf("%-5d %-10s %-15s %-20s %-15s\n",
                    i + 1, appt.appointmentID, appt.patientID,
                    MainScheduler.dateTimeFormat.format(appt.appointmentDate),
                    appt.appointmentReason);
        }
    }

    static boolean confirmCancellation(String appointmentID) {
        String confirmation = MainScheduler.getStringInput(
                "Confirm cancellation of appointment " + appointmentID + "? (y/n): ");
        if (!confirmation.equalsIgnoreCase("y")) {
            System.out.println("Cancellation cancelled!");
            return false;
        }
        return true;
    }

    static void cancelAppointment(MainScheduler.AppointmentNode appt) {
        appt.appointmentStatus = "Cancelled";
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
        String patientID = MainScheduler.getStringInput("Enter Patient ID: ");
        if (AdminFunctions.findPatientIndex(patientID) == -1) {
            System.out.println("Patient not found!");
            return;
        }

        System.out.println("\n----- Patient History -----");
        System.out.printf("%-10s %-15s %-20s %-15s %-10s %-20s\n",
                "ID", "Doctor", "Date", "Reason", "Status", "Updated");
        System.out.println("-------------------------------------------------");

        boolean hasHistory = false;
        for (String[] record : MainScheduler.appointmentHistory) {
            if (record[1].equals(patientID)) {
                System.out.printf("%-10s %-15s %-20s %-15s %-10s %-20s\n",
                        record[0], record[2], record[3], record[4], record[5], record[6]);
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
        System.out.println("Current: Phone: " + doctor[3] + ", Specialty: " + doctor[2]);

        String phone = MainScheduler.getValidPhoneNumber("New Phone: ");
        if (!MainScheduler.isPhoneNumberUnique(phone)) {
            System.out.println("Phone number already in use!");
            return;
        }
        String specialty = AdminFunctions.selectSpecialty();

        doctor[3] = phone;
        doctor[2] = specialty;

        MainScheduler.recordLog("Doctor profile updated: " + MainScheduler.userID);
        DataManager.saveAllData();
        System.out.println("Profile updated successfully!");
    }

    static void showPatientMenu() {
        System.out.println("\n===== Patient Menu =====");
        System.out.println("1. View Doctors");
        if (!MainScheduler.doctors.isEmpty()) {
            System.out.println("2. Book Appointment");
        }
        System.out.println("3. View Appointments");
        System.out.println("4. Cancel Appointment");
        System.out.println("5. Update Profile");
        System.out.println("6. Logout");
        System.out.println("=======================");
        handlePatientMenuChoice(MainScheduler.getIntegerInput("Choice: "));
    }

    static void handlePatientMenuChoice(int choice) {
        switch (choice) {
            case 1:
                displayDoctors();
                break;
            case 2:
                if (!MainScheduler.doctors.isEmpty()) {
                    bookAppointment();
                } else {
                    System.out.println("Invalid choice!");
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
            default:
                System.out.println("Invalid choice!");
        }
    }

    static void displayDoctors() {
        if (MainScheduler.doctors.isEmpty()) {
            System.out.println("No doctors available!");
            return;
        }

        System.out.println("\n----- Available Doctors -----");
        System.out.printf("%-10s %-15s %-15s\n", "ID", "Specialty", "Phone");
        System.out.println("--------------------------------------------");
        for (String[] doctor : MainScheduler.doctors) {
            if (doctor[4].equals("Active")) {
                System.out.printf("%-10s %-15s %-15s\n", doctor[0], doctor[2], doctor[3]);
            }
        }
    }

    static void bookAppointment() {
        if (MainScheduler.doctors.isEmpty()) {
            System.out.println("No doctors available!");
            return;
        }

        System.out.println("Note: Appointments are 30 minutes long.");
        displayDoctors();
        String doctorID = MainScheduler.getStringInput("Doctor ID: ");
        int index = AdminFunctions.findDoctorIndex(doctorID);

        if (index == -1 || !MainScheduler.doctors.get(index)[4].equals("Active")) {
            System.out.println("Invalid or inactive doctor!");
            return;
        }

        String dateStr = getValidAppointmentDate("Date (dd/MM/yyyy): ");
        try {
            Date date = MainScheduler.dateOnlyFormat.parse(dateStr);
            if (date.before(new Date())) {
                System.out.println("Cannot book past dates!");
                return;
            }

            String time = getAvailableSlot(doctorID, date);
            if (time == null) {
                System.out.println("No available slots!");
                return;
            }

            String dateTimeStr = dateStr + " " + time;
            String reason = MainScheduler.getStringInput("Reason: ");
            String requestID = "R" + String.format("%03d", MainScheduler.appointmentRequests.size() + 1);

            String[] request = { requestID, MainScheduler.userID, doctorID, dateTimeStr, reason };
            MainScheduler.appointmentRequests.add(request);

            MainScheduler.recordLog("New appointment request: " + requestID);
            DataManager.saveAllData();
            System.out.println("Appointment request submitted!");
        } catch (ParseException e) {
            System.out.println("Invalid date format!");
        }
    }

    static String getValidAppointmentDate(String prompt) {
        while (true) {
            String date = MainScheduler.getStringInput(prompt);
            try {
                MainScheduler.dateOnlyFormat.parse(date);
                return date;
            } catch (ParseException e) {
                System.out.println("Please use a valid dd/MM/yyyy date!");
            }
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

        int choice = MainScheduler.getIntegerInput("Select slot number (0 to back): ");
        if (choice == 0 || choice < 1 || choice > available.size()) {
            return null;
        }
        return available.get(choice - 1);
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

        if (!appointments.isEmpty()) {
            System.out.println("\n----- My Appointments -----");
            System.out.printf("%-10s %-15s %-20s %-15s %-10s\n", "ID", "Doctor", "Date", "Reason", "Status");
            System.out.println("-------------------------------------------------");
            appointments.sort((a1, a2) -> a1.appointmentDate.compareTo(a2.appointmentDate));
            for (MainScheduler.AppointmentNode appt : appointments) {
                System.out.printf("%-10s %-15s %-20s %-15s %-10s\n",
                        appt.appointmentID, appt.doctorID,
                        MainScheduler.dateTimeFormat.format(appt.appointmentDate),
                        appt.appointmentReason, appt.appointmentStatus);
            }
        }

        boolean hasRequests = false;
        for (String[] request : MainScheduler.appointmentRequests) {
            if (request[1].equals(MainScheduler.userID)) {
                if (!hasRequests) {
                    System.out.println("\n----- Pending Requests -----");
                    System.out.printf("%-10s %-15s %-20s %-15s\n", "ID", "Doctor", "Date", "Reason");
                    System.out.println("--------------------------------------------");
                    hasRequests = true;
                }
                System.out.printf("%-10s %-15s %-20s %-15s\n",
                        request[0], request[2], request[3], request[4]);
            }
        }

        if (appointments.isEmpty() && !hasRequests) {
            System.out.println("No appointments or pending requests!");
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
        System.out.println("-------------------------------------------------");
        for (int i = 0; i < cancellable.size(); i++) {
            MainScheduler.AppointmentNode appt = cancellable.get(i);
            System.out.printf("%-5d %-10s %-15s %-20s %-15s %-10s\n",
                    i + 1, appt.appointmentID, appt.doctorID,
                    MainScheduler.dateTimeFormat.format(appt.appointmentDate),
                    appt.appointmentReason, appt.appointmentStatus);
        }

        int choice = MainScheduler.getIntegerInput("Enter number to cancel (0 to back): ");
        if (choice == 0)
            return;

        if (choice > 0 && choice <= cancellable.size()) {
            MainScheduler.AppointmentNode appt = cancellable.get(choice - 1);
            if (confirmCancellation(appt.appointmentID)) {
                long timeDiff = appt.appointmentDate.getTime() - new Date().getTime();
                if (timeDiff < 86400000) {
                    System.out.println("Cannot cancel: Less than 24 hours until appointment!");
                    return;
                }
                cancelAppointment(appt);
                System.out.println("Appointment cancelled!");
            }
        } else {
            System.out.println("Invalid selection!");
        }
    }

    static void editPatientProfile() {
        int index = AdminFunctions.findPatientIndex(MainScheduler.userID);
        if (index == -1) {
            System.out.println("Patient profile not found!");
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

        MainScheduler.recordLog("Patient profile updated: " + MainScheduler.userID);
        DataManager.saveAllData();
        System.out.println("Profile updated successfully!");
    }
}