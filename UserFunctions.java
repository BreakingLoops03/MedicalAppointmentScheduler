package trial;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
        int menuChoice = MainScheduler.getIntegerInput("Choice: ");
        switch (menuChoice) {
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
                System.out.println("Wrong choice!");
        }
    }

    static void displayDoctorAppointments() {
        if (MainScheduler.appointmentTreeRoot == null) {
            System.out.println("No appointments!");
            return;
        }
        ArrayList<MainScheduler.AppointmentNode> appointments = new ArrayList<>();
        collectDoctorAppointments(MainScheduler.appointmentTreeRoot, MainScheduler.userID, appointments);
        if (appointments.isEmpty()) {
            System.out.println("No appointments!");
            return;
        }
        System.out.println("\n----- My Appointments -----");
        System.out.printf("%-10s %-15s %-20s %-15s %-10s\n", "ID", "Patient", "Date", "Reason", "Status");
        System.out.println("--------------------------------------------");
        appointments.sort((a1, a2) -> a1.appointmentDate.compareTo(a2.appointmentDate));
        for (MainScheduler.AppointmentNode appointment : appointments) {
            System.out.printf("%-10s %-15s %-20s %-15s %-10s\n",
                    appointment.appointmentID, appointment.patientID,
                    MainScheduler.dateTimeFormat.format(appointment.appointmentDate),
                    appointment.appointmentReason, appointment.appointmentStatus);
        }
    }

    static void collectDoctorAppointments(MainScheduler.AppointmentNode node, String doctorID,
            ArrayList<MainScheduler.AppointmentNode> appointments) {
        if (node == null) {
            return;
        }
        collectDoctorAppointments(node.leftChild, doctorID, appointments);
        if (node.doctorID.equals(doctorID) && !node.appointmentStatus.equals("Cancelled")) {
            appointments.add(node);
        }
        collectDoctorAppointments(node.rightChild, doctorID, appointments);
    }

    static void cancelDoctorAppointment() {
        ArrayList<MainScheduler.AppointmentNode> appointments = new ArrayList<>();
        collectDoctorAppointments(MainScheduler.appointmentTreeRoot, MainScheduler.userID, appointments);
        if (appointments.isEmpty()) {
            System.out.println("No appointments!");
            return;
        }
        System.out.println("\n----- My Appointments -----");
        System.out.printf("%-5s %-10s %-15s %-20s %-15s\n", "No.", "ID", "Patient", "Date", "Reason");
        System.out.println("--------------------------------------------");
        for (int i = 0; i < appointments.size(); i++) {
            MainScheduler.AppointmentNode appointment = appointments.get(i);
            System.out.printf("%-5d %-10s %-15s %-20s %-15s\n",
                    i + 1, appointment.appointmentID, appointment.patientID,
                    MainScheduler.dateTimeFormat.format(appointment.appointmentDate),
                    appointment.appointmentReason);
        }
        int appointmentNumber = MainScheduler.getIntegerInput("Enter number to cancel (0 to back): ");
        if (appointmentNumber == 0) {
            return;
        }
        if (appointmentNumber > 0 && appointmentNumber <= appointments.size()) {
            MainScheduler.AppointmentNode appointment = appointments.get(appointmentNumber - 1);
            String confirmation = MainScheduler
                    .getStringInput("Confirm cancellation of appointment " + appointment.appointmentID + "? (y/n): ");
            if (!confirmation.equalsIgnoreCase("y")) {
                System.out.println("Cancellation cancelled!");
                return;
            }
            appointment.appointmentStatus = "Cancelled";
            for (String[] appointmentRecord : MainScheduler.appointmentHistory) {
                if (appointmentRecord[0].equals(appointment.appointmentID)) {
                    appointmentRecord[5] = "Cancelled";
                    appointmentRecord[6] = new Date().toString();
                    break;
                }
            }
            MainScheduler.recordLog("Appointment cancelled: " + appointment.appointmentID);
            System.out.println("Cancelled!");
        } else {
            System.out.println("Wrong number!");
        }
    }

    static void displayPatientHistory() {
        AdminFunctions.displayPatients();
        String patientID = MainScheduler.getStringInput("Enter Patient ID: ");
        if (AdminFunctions.findPatientIndex(patientID) == -1) {
            System.out.println("Patient not found!");
            return;
        }
        System.out.println("\n----- Patient History -----");
        System.out.printf("%-10s %-15s %-20s %-15s %-10s %-20s\n", "ID", "Doctor", "Date", "Reason", "Status",
                "Updated");
        System.out.println("--------------------------------------------");
        boolean hasHistory = false;
        for (String[] appointmentRecord : MainScheduler.appointmentHistory) {
            if (appointmentRecord[1].equals(patientID)) {
                System.out.printf("%-10s %-15s %-20s %-15s %-10s %-20s\n",
                        appointmentRecord[0], appointmentRecord[2], appointmentRecord[3], appointmentRecord[4],
                        appointmentRecord[5], appointmentRecord[6]);
                hasHistory = true;
            }
        }
        if (!hasHistory) {
            System.out.println("No history!");
        }
    }

    static void editDoctorProfile() {
        int doctorIndex = AdminFunctions.findDoctorIndex(MainScheduler.userID);
        if (doctorIndex == -1) {
            System.out.println("Profile not found!");
            return;
        }
        String[] doctorData = MainScheduler.doctors.get(doctorIndex);
        System.out.println("Current: Phone: " + doctorData[3] + ", Specialty: " + doctorData[2]);
        String phoneNumber = MainScheduler.getValidPhoneNumber("New Phone: ");
        doctorData[3] = phoneNumber;
        String doctorSpecialty = AdminFunctions.selectSpecialty();
        doctorData[2] = doctorSpecialty;
        MainScheduler.recordLog("Doctor updated: " + MainScheduler.userID);
        System.out.println("Profile updated!");
    }

    static void showPatientMenu() {
        System.out.println("\n===== Patient Menu =====");
        System.out.println("1. View Doctors");
        System.out.println("2. Book Appointment");
        System.out.println("3. View Appointments");
        System.out.println("4. Cancel Appointment");
        System.out.println("5. Update Profile");
        System.out.println("6. Logout");
        System.out.println("=======================");
        int menuChoice = MainScheduler.getIntegerInput("Choice: ");
        switch (menuChoice) {
            case 1:
                displayDoctors();
                break;
            case 2:
                bookAppointment();
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
                System.out.println("Wrong choice!");
        }
    }

    static void displayDoctors() {
        if (MainScheduler.doctors.isEmpty()) {
            System.out.println("No doctors!");
            return;
        }
        System.out.println("\n----- Available Doctors -----");
        System.out.printf("%-10s %-15s %-15s\n", "ID", "Specialty", "Phone");
        System.out.println("--------------------------------------------");
        for (String[] doctorData : MainScheduler.doctors) {
            if (doctorData[4].equals("Active")) {
                System.out.printf("%-10s %-15s %-15s\n", doctorData[0], doctorData[2], doctorData[3]);
            }
        }
    }

    static void bookAppointment() {
        if (MainScheduler.doctors.isEmpty()) {
            System.out.println("No doctors!");
            return;
        }
        displayDoctors();
        String selectedDoctorID = MainScheduler.getStringInput("Doctor ID: ");
        int doctorIndex = AdminFunctions.findDoctorIndex(selectedDoctorID);
        if (doctorIndex == -1 || !MainScheduler.doctors.get(doctorIndex)[4].equals("Active")) {
            System.out.println("Invalid or inactive doctor!");
            return;
        }
        String dateInput = getValidAppointmentDate("Date (dd/MM/yyyy): ");
        try {
            Date appointmentDate = MainScheduler.dateOnlyFormat.parse(dateInput);
            if (appointmentDate.before(new Date())) {
                System.out.println("No past bookings!");
                return;
            }
            String appointmentTime = getAvailableSlot(selectedDoctorID, appointmentDate);
            if (appointmentTime == null) {
                System.out.println("No available slots!");
                return;
            }
            String dateTimeString = dateInput + " " + appointmentTime;
            Date parsedDateTime = MainScheduler.dateTimeFormat.parse(dateTimeString);
            String appointmentReason = MainScheduler.getStringInput("Reason: ");
            String requestID = "R" + (MainScheduler.appointmentRequests.size() + 1);
            String[] requestData = { requestID, MainScheduler.userID, selectedDoctorID, dateTimeString,
                    appointmentReason };
            MainScheduler.appointmentRequests.add(requestData);
            MainScheduler.recordLog("New request: " + requestID);
            System.out.println("Request sent!");
        } catch (ParseException e) {
            System.out.println("Bad date format!");
        }
    }

    static String getValidAppointmentDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String dateInput = MainScheduler.scanner.nextLine().trim();
            try {
                MainScheduler.dateOnlyFormat.parse(dateInput);
                return dateInput;
            } catch (ParseException e) {
                System.out.println("Use dd/MM/yyyy!");
            }
        }
    }

    static String getAvailableSlot(String doctorID, Date appointmentDate) {
        String[] timeSlots = { "09:00", "09:30", "10:00", "10:30", "11:00", "11:30", "12:00", "12:30",
                "13:00", "13:30", "14:00", "14:30", "15:00", "15:30", "16:00", "16:30", "17:00" };
        ArrayList<String> availableSlots = new ArrayList<>();
        SimpleDateFormat dateTimeFormat = MainScheduler.dateTimeFormat;

        for (String timeSlot : timeSlots) {
            try {
                Date slotDateTime = dateTimeFormat
                        .parse(MainScheduler.dateOnlyFormat.format(appointmentDate) + " " + timeSlot);
                if (!hasSchedulingConflict(doctorID, slotDateTime)) {
                    availableSlots.add(timeSlot);
                }
            } catch (ParseException e) {
            }
        }

        if (availableSlots.isEmpty()) {
            return null;
        }

        System.out.println("\n----- Available Slots -----");
        for (int i = 0; i < availableSlots.size(); i++) {
            System.out.println((i + 1) + ". " + availableSlots.get(i));
        }
        int choice = MainScheduler.getIntegerInput("Select slot number (0 to back): ");
        if (choice == 0 || choice < 1 || choice > availableSlots.size()) {
            return null;
        }
        return availableSlots.get(choice - 1);
    }

    static boolean hasSchedulingConflict(String doctorID, Date appointmentDate) {
        ArrayList<MainScheduler.AppointmentNode> appointments = new ArrayList<>();
        collectDoctorAppointments(MainScheduler.appointmentTreeRoot, doctorID, appointments);
        for (MainScheduler.AppointmentNode appointment : appointments) {
            if (!appointment.appointmentStatus.equals("Cancelled")) {
                long timeDifference = Math.abs(appointment.appointmentDate.getTime() - appointmentDate.getTime());
                if (timeDifference < 1800000) {
                    return true;
                }
            }
        }
        return false;
    }

    static void displayPatientAppointments() {
        if (MainScheduler.appointmentTreeRoot == null) {
            System.out.println("No appointments!");
            return;
        }
        ArrayList<MainScheduler.AppointmentNode> appointments = new ArrayList<>();
        collectPatientAppointments(MainScheduler.appointmentTreeRoot, MainScheduler.userID, appointments);
        if (appointments.isEmpty()) {
            System.out.println("No appointments!");
            boolean hasPendingRequests = false;
            for (String[] requestData : MainScheduler.appointmentRequests) {
                if (requestData[1].equals(MainScheduler.userID)) {
                    if (!hasPendingRequests) {
                        System.out.println("\n----- Pending Requests -----");
                        System.out.printf("%-10s %-15s %-20s %-15s\n", "ID", "Doctor", "Date", "Reason");
                        System.out.println("--------------------------------------------");
                        hasPendingRequests = true;
                    }
                    System.out.printf("%-10s %-15s %-20s %-15s\n", requestData[0], requestData[2], requestData[3],
                            requestData[4]);
                }
            }
            return;
        }
        System.out.println("\n----- My Appointments -----");
        System.out.printf("%-10s %-15s %-20s %-15s %-10s\n", "ID", "Doctor", "Date", "Reason", "Status");
        System.out.println("--------------------------------------------");
        appointments.sort((a1, a2) -> a1.appointmentDate.compareTo(a2.appointmentDate));
        for (MainScheduler.AppointmentNode appointment : appointments) {
            System.out.printf("%-10s %-15s %-20s %-15s %-10s\n",
                    appointment.appointmentID, appointment.doctorID,
                    MainScheduler.dateTimeFormat.format(appointment.appointmentDate),
                    appointment.appointmentReason, appointment.appointmentStatus);
        }
    }

    static void collectPatientAppointments(MainScheduler.AppointmentNode node, String patientID,
            ArrayList<MainScheduler.AppointmentNode> appointments) {
        if (node == null) {
            return;
        }
        collectPatientAppointments(node.leftChild, patientID, appointments);
        if (node.patientID.equals(patientID)) {
            appointments.add(node);
        }
        collectPatientAppointments(node.rightChild, patientID, appointments);
    }

    static void cancelPatientAppointment() {
        ArrayList<MainScheduler.AppointmentNode> appointments = new ArrayList<>();
        collectPatientAppointments(MainScheduler.appointmentTreeRoot, MainScheduler.userID, appointments);
        if (appointments.isEmpty()) {
            System.out.println("No appointments!");
            return;
        }
        ArrayList<MainScheduler.AppointmentNode> scheduledAppointments = new ArrayList<>();
        for (MainScheduler.AppointmentNode appointment : appointments) {
            if (appointment.appointmentStatus.equals("Scheduled")) {
                scheduledAppointments.add(appointment);
            }
        }
        if (scheduledAppointments.isEmpty()) {
            System.out.println("No scheduled appointments!");
            return;
        }
        System.out.println("\n----- My Appointments -----");
        System.out.printf("%-5s %-10s %-15s %-20s %-15s %-10s\n", "No.", "ID", "Doctor", "Date", "Reason", "Status");
        System.out.println("--------------------------------------------");
        for (int i = 0; i < scheduledAppointments.size(); i++) {
            MainScheduler.AppointmentNode appointment = scheduledAppointments.get(i);
            System.out.printf("%-5d %-10s %-15s %-20s %-15s %-10s\n",
                    i + 1, appointment.appointmentID, appointment.doctorID,
                    MainScheduler.dateTimeFormat.format(appointment.appointmentDate),
                    appointment.appointmentReason, appointment.appointmentStatus);
        }
        int appointmentNumber = MainScheduler.getIntegerInput("Enter number to cancel (0 to back): ");
        if (appointmentNumber == 0) {
            return;
        }
        if (appointmentNumber > 0 && appointmentNumber <= scheduledAppointments.size()) {
            MainScheduler.AppointmentNode appointment = scheduledAppointments.get(appointmentNumber - 1);
            String confirmation = MainScheduler
                    .getStringInput("Confirm cancellation of appointment " + appointment.appointmentID + "? (y/n): ");
            if (!confirmation.equalsIgnoreCase("y")) {
                System.out.println("Cancellation cancelled!");
                return;
            }
            long timeDifference = appointment.appointmentDate.getTime() - new Date().getTime();
            if (timeDifference < 86400000) {
                System.out.println("Too late to cancel!");
                return;
            }
            appointment.appointmentStatus = "Cancelled";
            for (String[] appointmentRecord : MainScheduler.appointmentHistory) {
                if (appointmentRecord[0].equals(appointment.appointmentID)) {
                    appointmentRecord[5] = "Cancelled";
                    appointmentRecord[6] = new Date().toString();
                    break;
                }
            }
            MainScheduler.recordLog("Appointment cancelled: " + appointment.appointmentID);
            System.out.println("Cancelled!");
        } else {
            System.out.println("Wrong number!");
        }
    }

    static void editPatientProfile() {
        int patientIndex = AdminFunctions.findPatientIndex(MainScheduler.userID);
        if (patientIndex == -1) {
            System.out.println("Profile not found!");
            return;
        }
        String[] patientData = MainScheduler.patients.get(patientIndex);
        System.out.println("Current: Phone: " + patientData[1]);
        String phoneNumber = MainScheduler.getValidPhoneNumber("New Phone: ");
        patientData[1] = phoneNumber;
        MainScheduler.recordLog("Patient updated: " + MainScheduler.userID);
        System.out.println("Profile updated!");
    }
}