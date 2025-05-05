package trial;

import java.text.ParseException;
import java.util.*;

public class UserFunctions {
    static void displayDoctorMenu() {
        System.out.println("\n===== DOCTOR MENU =====");
        System.out.println("1. View My Appointments");
        System.out.println("2. Cancel Appointment");
        System.out.println("3. View Patient History");
        System.out.println("4. Update My Profile");
        System.out.println("5. Logout");
        System.out.println("======================");
        
        int choice = MainScheduler.getIntInput("Enter your choice: ");
        
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
                MainScheduler.logout();
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }
    
    static void viewDoctorAppointments() {
        if (MainScheduler.appointmentRoot == null) {
            System.out.println("No appointments found!");
            return;
        }
        
        System.out.println("\n----- My Appointments -----");
        System.out.printf("%-10s %-15s %-20s %-15s %-10s\n", 
            "ID", "Patient ID", "Date & Time", "Reason", "Status");
        System.out.println("----------------------------------------------------------");
        
        ArrayList<MainScheduler.AppointmentNode> doctorAppointments = new ArrayList<>();
        collectDoctorAppointments(MainScheduler.appointmentRoot, MainScheduler.currentUserId, doctorAppointments);
        
        if (doctorAppointments.isEmpty()) {
            System.out.println("No appointments found for you!");
            return;
        }
        
        doctorAppointments.sort((a1, a2) -> a1.date.compareTo(a2.date));
        
        for (MainScheduler.AppointmentNode appointment : doctorAppointments) {
            System.out.printf("%-10s %-15s %-20s %-15s %-10s\n", 
                appointment.id, appointment.patientId, 
                MainScheduler.dateTimeFormat.format(appointment.date), 
                appointment.reason, appointment.status);
        }
    }
    
    static void collectDoctorAppointments(MainScheduler.AppointmentNode node, String doctorId, 
                                         ArrayList<MainScheduler.AppointmentNode> appointments) {
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
        if (MainScheduler.appointmentRoot == null) {
            System.out.println("No appointments found!");
            return;
        }
        
        ArrayList<MainScheduler.AppointmentNode> doctorAppointments = new ArrayList<>();
        collectDoctorAppointments(MainScheduler.appointmentRoot, MainScheduler.currentUserId, doctorAppointments);
        
        if (doctorAppointments.isEmpty()) {
            System.out.println("No appointments found for you!");
            return;
        }
        
        System.out.println("\n----- My Appointments -----");
        System.out.printf("%-5s %-10s %-15s %-20s %-15s\n", 
            "No.", "ID", "Patient ID", "Date & Time", "Reason");
        System.out.println("----------------------------------------------------------");
        
        for (int i = 0; i < doctorAppointments.size(); i++) {
            MainScheduler.AppointmentNode appointment = doctorAppointments.get(i);
            System.out.printf("%-5d %-10s %-15s %-20s %-15s\n", 
                i + 1, appointment.id, appointment.patientId, 
                MainScheduler.dateTimeFormat.format(appointment.date), appointment.reason);
        }
        
        int appointmentNumber = MainScheduler.getIntInput("Enter appointment number to cancel (0 to go back): ");
        
        if (appointmentNumber == 0) {
            return;
        }
        
        if (appointmentNumber > 0 && appointmentNumber <= doctorAppointments.size()) {
            MainScheduler.AppointmentNode appointment = doctorAppointments.get(appointmentNumber - 1);
            appointment.status = "Cancelled";
            
            for (String[] historyEntry : MainScheduler.appointmentHistory) {
                if (historyEntry[0].equals(appointment.id)) {
                    historyEntry[5] = "Cancelled";
                    historyEntry[6] = new Date().toString();
                    break;
                }
            }
            
            MainScheduler.logOperation("Appointment cancelled by doctor: " + appointment.id);
            System.out.println("Appointment cancelled successfully!");
        } else {
            System.out.println("Invalid appointment number!");
        }
    }
    
    static void viewPatientHistory() {
        AdminFunctions.viewAllPatients();
        String patientId = MainScheduler.getMandatoryStringInput("Enter Patient ID to view history: ");
        
        int index = AdminFunctions.findPatientIndex(patientId);
        if (index == -1) {
            System.out.println("Patient not found!");
            return;
        }
        
        System.out.println("\n----- Patient History -----");
        System.out.printf("%-10s %-15s %-20s %-15s %-10s %-20s\n", 
            "ID", "Doctor ID", "Date & Time", "Reason", "Status", "Last Updated");
        System.out.println("-------------------------------------------------------------------------");
        
        boolean found = false;
        for (String[] historyEntry : MainScheduler.appointmentHistory) {
            if (historyEntry[1].equals(patientId)) {
                System.out.printf("%-10s %-15s %-20s %-15s %-10s %-20s\n", 
                    historyEntry[0], historyEntry[2], historyEntry[3], 
                    historyEntry[4], historyEntry[5], historyEntry[6]);
                found = true;
            }
        }
        
        if (!found) {
            System.out.println("No appointment history found for this patient!");
        }
    }
    
    static void updateDoctorProfile() {
        int index = AdminFunctions.findDoctorIndex(MainScheduler.currentUserId);
        if (index == -1) {
            System.out.println("Error: Doctor profile not found!");
            return;
        }
        
        String[] doctor = MainScheduler.doctors.get(index);
        
        System.out.println("\n----- Update My Profile -----");
        System.out.println("Current details:");
        System.out.printf("Phone: %s, Specialty: %s, Email: %s\n", 
            doctor[1], doctor[2], doctor[3]);
        
        String phone = MainScheduler.getValidPhoneNumber("Enter new Phone: ");
        doctor[1] = phone;
        
        String email = MainScheduler.getValidEmail("Enter new Email (optional, press Enter to skip): ");
        doctor[3] = email;
        
        MainScheduler.logOperation("Doctor updated profile: " + MainScheduler.currentUserId);
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
        
        int choice = MainScheduler.getIntInput("Enter your choice: ");
        
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
                MainScheduler.logout();
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }
    
    static void viewAvailableDoctors() {
        if (MainScheduler.doctors.isEmpty()) {
            System.out.println("No doctors found!");
            return;
        }
        
        System.out.println("\n----- Available Doctors -----");
        System.out.printf("%-10s %-15s %-15s\n", "ID", "Specialty", "Phone");
        System.out.println("----------------------------------------");
        
        for (String[] doctor : MainScheduler.doctors) {
            if (doctor[4].equals("Active")) {
                System.out.printf("%-10s %-15s %-15s\n", doctor[0], doctor[2], doctor[1]);
            }
        }
    }
    
    static void bookAppointment() {
        if (MainScheduler.doctors.isEmpty()) {
            System.out.println("No doctors available for booking!");
            return;
        }
        
        System.out.println("\n----- Book Appointment -----");
        
        viewAvailableDoctors();
        String doctorId = MainScheduler.getMandatoryStringInput("Enter Doctor ID: ");
        
        int doctorIndex = AdminFunctions.findDoctorIndex(doctorId);
        if (doctorIndex == -1) {
            System.out.println("Doctor not found!");
            return;
        }
        
        if (!MainScheduler.doctors.get(doctorIndex)[4].equals("Active")) {
            System.out.println("This doctor is not currently available!");
            return;
        }
        
        String dateTimeStr = getValidAppointmentTime("Enter Date and Time (dd/MM/yyyy HH:mm): ");
        String reason = MainScheduler.getMandatoryStringInput("Enter Reason for Appointment: ");
        
        try {
            Date appointmentDateTime = MainScheduler.dateTimeFormat.parse(dateTimeStr);
            if (appointmentDateTime.before(new Date())) {
                System.out.println("Cannot book appointments in the past!");
                return;
            }
            
            if (hasTimeConflict(doctorId, appointmentDateTime)) {
                System.out.println("Doctor is not available at this time. Please choose another time.");
                return;
            }
            
            String requestId = "R" + String.format("%03d", MainScheduler.appointmentRequests.size() + 1);
            
            String[] request = {requestId, MainScheduler.currentUserId, doctorId, dateTimeStr, reason};
            MainScheduler.appointmentRequests.add(request);
            
            MainScheduler.logOperation("Appointment request submitted: " + requestId);
            System.out.println("Appointment request submitted successfully! Waiting for admin approval.");
        } catch (ParseException e) {
            System.out.println("Invalid date format! Please use dd/MM/yyyy HH:mm");
        }
    }
    
    static String getValidAppointmentTime(String prompt) {
        while (true) {
            System.out.print(prompt);
            String dateTimeStr = MainScheduler.scanner.nextLine().trim();
            try {
                Date dateTime = MainScheduler.dateTimeFormat.parse(dateTimeStr);
                Calendar cal = Calendar.getInstance();
                cal.setTime(dateTime);
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                int minute = cal.get(Calendar.MINUTE);
                
                if (hour < 9 || hour > 17 || (hour == 17 && minute > 0)) {
                    System.out.println("Appointments must be between 9:00 AM and 5:00 PM. Please try again.");
                    continue;
                }
                
                if (minute != 0 && minute != 30) {
                    System.out.println("Appointments must be on the hour or half-hour (e.g., 09:00, 09:30). Please try again.");
                    continue;
                }
                
                return dateTimeStr;
            } catch (ParseException e) {
                System.out.println("Invalid date format! Please use dd/MM/yyyy HH:mm");
            }
        }
    }
    
    static boolean hasTimeConflict(String doctorId, Date appointmentDateTime) {
        if (MainScheduler.appointmentRoot == null) {
            return false;
        }
        
        ArrayList<MainScheduler.AppointmentNode> doctorAppointments = new ArrayList<>();
        collectDoctorAppointments(MainScheduler.appointmentRoot, doctorId, doctorAppointments);
        
        for (MainScheduler.AppointmentNode appointment : doctorAppointments) {
            if (!appointment.status.equals("Cancelled")) {
                long timeDiff = Math.abs(appointment.date.getTime() - appointmentDateTime.getTime());
                if (timeDiff < 1800000) { // 30 minutes
                    return true;
                }
            }
        }
        
        return false;
    }
    
    static void viewPatientAppointments() {
        if (MainScheduler.appointmentRoot == null) {
            System.out.println("No appointments found!");
            return;
        }
        
        System.out.println("\n----- My Appointments -----");
        System.out.printf("%-10s %-15s %-20s %-15s %-10s\n", 
            "ID", "Doctor ID", "Date & Time", "Reason", "Status");
        System.out.println("----------------------------------------------------------");
        
        ArrayList<MainScheduler.AppointmentNode> patientAppointments = new ArrayList<>();
        collectPatientAppointments(MainScheduler.appointmentRoot, MainScheduler.currentUserId, patientAppointments);
        
        if (patientAppointments.isEmpty()) {
            System.out.println("No appointments found for you!");
            
            boolean hasPendingRequests = false;
            for (String[] request : MainScheduler.appointmentRequests) {
                if (request[1].equals(MainScheduler.currentUserId)) {
                    if (!hasPendingRequests) {
                        System.out.println("\n----- Pending Requests -----");
                        System.out.printf("%-10s %-15s %-20s %-15s\n", 
                            "ID", "Doctor ID", "Date & Time", "Reason");
                        System.out.println("----------------------------------------------------------");
                        hasPendingRequests = true;
                    }
                    
                    System.out.printf("%-10s %-15s %-20s %-15s\n", 
                        request[0], request[2], request[3], request[4]);
                }
            }
            
            return;
        }
        
        patientAppointments.sort((a1, a2) -> a1.date.compareTo(a2.date));
        
        for (MainScheduler.AppointmentNode appointment : patientAppointments) {
            System.out.printf("%-10s %-15s %-20s %-15s %-10s\n", 
                appointment.id, appointment.doctorId, 
                MainScheduler.dateTimeFormat.format(appointment.date), 
                appointment.reason, appointment.status);
        }
    }
    
    static void collectPatientAppointments(MainScheduler.AppointmentNode node, String patientId, 
                                          ArrayList<MainScheduler.AppointmentNode> appointments) {
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
        if (MainScheduler.appointmentRoot == null) {
            System.out.println("No appointments found!");
            return;
        }
        
        ArrayList<MainScheduler.AppointmentNode> patientAppointments = new ArrayList<>();
        collectPatientAppointments(MainScheduler.appointmentRoot, MainScheduler.currentUserId, patientAppointments);
        
        if (patientAppointments.isEmpty()) {
            System.out.println("No appointments found for you!");
            return;
        }
        
        System.out.println("\n----- My Appointments -----");
        System.out.printf("%-5s %-10s %-15s %-20s %-15s %-10s\n", 
            "No.", "ID", "Doctor ID", "Date & Time", "Reason", "Status");
        System.out.println("------------------------------------------------------------------");
        
        ArrayList<MainScheduler.AppointmentNode> scheduledAppointments = new ArrayList<>();
        for (MainScheduler.AppointmentNode appointment : patientAppointments) {
            if (appointment.status.equals("Scheduled")) {
                scheduledAppointments.add(appointment);
            }
        }
        
        if (scheduledAppointments.isEmpty()) {
            System.out.println("No scheduled appointments found that can be cancelled!");
            return;
        }
        
        for (int i = 0; i < scheduledAppointments.size(); i++) {
            MainScheduler.AppointmentNode appointment = scheduledAppointments.get(i);
            System.out.printf("%-5d %-10s %-15s %-20s %-15s %-10s\n", 
                i + 1, appointment.id, appointment.doctorId, 
                MainScheduler.dateTimeFormat.format(appointment.date), 
                appointment.reason, appointment.status);
        }
        
        int appointmentNumber = MainScheduler.getIntInput("Enter appointment number to cancel (0 to go back): ");
        
        if (appointmentNumber == 0) {
            return;
        }
        
        if (appointmentNumber > 0 && appointmentNumber <= scheduledAppointments.size()) {
            MainScheduler.AppointmentNode appointment = scheduledAppointments.get(appointmentNumber - 1);
            
            long timeDiff = appointment.date.getTime() - new Date().getTime();
            if (timeDiff < 86400000) {
                System.out.println("Cannot cancel appointments within 24 hours of scheduled time!");
                return;
            }
            
            appointment.status = "Cancelled";
            
            for (String[] historyEntry : MainScheduler.appointmentHistory) {
                if (historyEntry[0].equals(appointment.id)) {
                    historyEntry[5] = "Cancelled";
                    historyEntry[6] = new Date().toString();
                    break;
                }
            }
            
            MainScheduler.logOperation("Appointment cancelled by patient: " + appointment.id);
            System.out.println("Appointment cancelled successfully!");
        } else {
            System.out.println("Invalid appointment number!");
        }
    }
    
    static void updatePatientProfile() {
        int index = AdminFunctions.findPatientIndex(MainScheduler.currentUserId);
        if (index == -1) {
            System.out.println("Error: Patient profile not found!");
            return;
        }
        
        String[] patient = MainScheduler.patients.get(index);
        
        System.out.println("\n----- Update My Profile -----");
        System.out.println("Current details:");
        System.out.printf("Phone: %s, Email: %s, Address: %s\n", 
            patient[1], patient[2], patient[3]);
        
        String phone = MainScheduler.getValidPhoneNumber("Enter new Phone: ");
        patient[1] = phone;
        
        String email = MainScheduler.getValidEmail("Enter new Email (optional, press Enter to skip): ");
        patient[2] = email;
        
        String address = MainScheduler.getStringInput("Enter new Address (optional, press Enter to skip): ");
        patient[3] = address;
        
        MainScheduler.logOperation("Patient updated profile: " + MainScheduler.currentUserId);
        System.out.println("Profile updated successfully!");
    }
}