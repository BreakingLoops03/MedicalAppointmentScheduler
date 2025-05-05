package trial;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainScheduler {
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
        DataManager.loadData();
        
        // Add shutdown hook to save data on premature exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            DataManager.saveData();
            System.out.println("Data saved due to unexpected termination.");
        }));
        
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
                        AdminFunctions.displayAdminMenu();
                        break;
                    case USER_DOCTOR:
                        UserFunctions.displayDoctorMenu();
                        break;
                    case USER_PATIENT:
                        UserFunctions.displayPatientMenu();
                        break;
                }
            }
        }
        
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
        
        String password = getValidPassword("Enter password: ");
        String phone = getValidPhoneNumber("Enter your phone number: ");
        String email = getValidEmail("Enter your email (optional, press Enter to skip): ");
        String address = getStringInput("Enter your address (optional, press Enter to skip): ");
        
        String patientId = "P" + String.format("%03d", patients.size() + 1);
        
        String[] userData = {password, String.valueOf(USER_PATIENT), patientId};
        users.put(username, userData);
        
        String[] patientData = {patientId, phone, email, address, "Active"};
        patients.add(patientData);
        
        logOperation("New patient registered: " + username);
        System.out.println("Registration successful! You can now login.");
    }
    
    static void logout() {
        logOperation(currentUsername + " logged out");
        currentUsername = null;
        currentUserType = 0;
        currentUserId = null;
        DataManager.saveData();
        System.out.println("Logged out successfully!");
    }
    
    static void logOperation(String operation) {
        String timestamp = new Date().toString();
        operationLogs.push(timestamp + " - " + operation);
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
    
    static String getValidPhoneNumber(String prompt) {
        while (true) {
            System.out.print(prompt);
            String phone = scanner.nextLine().trim();
            
            // Remove any non-digit characters
            String digitsOnly = phone.replaceAll("[^0-9]", "");
            
            // Validate length and digits
            if (digitsOnly.length() == 10 && digitsOnly.matches("\\d{10}")) {
                if (isPhoneNumberUnique(digitsOnly)) {
                    return digitsOnly; // Return plain 10-digit number
                } else {
                    System.out.println("This phone number is already registered. Please enter a different number.");
                }
            } else {
                System.out.println("Invalid phone number. Please enter exactly 10 digits (you can use spaces or dashes).");
                System.out.println("Example: 123 456 7890 or 123-456-7890");
            }
        }
    }
    
    static boolean isPhoneNumberUnique(String phone) {
        for (String[] patient : patients) {
            if (patient[1].equals(phone)) {
                return false;
            }
        }
        for (String[] doctor : doctors) {
            if (doctor[1].equals(phone)) {
                return false;
            }
        }
        return true;
    }
    
    static String getValidEmail(String prompt) {
        while (true) {
            System.out.print(prompt);
            String email = scanner.nextLine().trim();
            if (email.isEmpty() || isValidEmail(email)) {
                return email;
            }
            System.out.println("Invalid email format. Must contain '@' and '.'. Please try again or press Enter to skip.");
        }
    }
    
    static boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".") && email.indexOf("@") < email.lastIndexOf(".");
    }
    
    static String getValidPassword(String prompt) {
        while (true) {
            System.out.print(prompt);
            String password = scanner.nextLine().trim();
            if (password.length() >= 6) {
                return password;
            }
            System.out.println("Password must be at least 6 characters long. Please try again.");
        }
    }
}