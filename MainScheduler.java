package trial;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

public class MainScheduler {
    static final String PATIENTS_FILE = "patients.txt";
    static final String DOCTORS_FILE = "doctors.txt";
    static final String APPOINTMENTS_FILE = "appointments.txt";
    static final String USERS_FILE = "users.txt";
    static final String LOGS_FILE = "logs.txt";

    static final int USER_ADMIN = 1;
    static final int USER_DOCTOR = 2;
    static final int USER_PATIENT = 3;

    static HashMap<String, String[]> users = new HashMap<>();
    static ArrayList<String[]> patients = new ArrayList<>();
    static ArrayList<String[]> doctors = new ArrayList<>();
    static LinkedList<String[]> appointmentHistory = new LinkedList<>();
    static ArrayList<String> operationLogs = new ArrayList<>();
    static ArrayList<AppointmentNode> appointments = new ArrayList<>();
    static int nextAppointmentID = 1;

    static String loggedInUser = null;
    static int userRole = 0;
    static String userID = null;

    static Scanner scanner = new Scanner(System.in);
    static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    static SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("dd/MM/yyyy");

    static {
        dateOnlyFormat.setLenient(false);
        dateTimeFormat.setLenient(false);
    }

    static class AppointmentNode {
        String appointmentID;
        String patientID;
        String doctorID;
        Date appointmentDate;
        String appointmentReason;
        String appointmentStatus;

        AppointmentNode(String appointmentID, String patientID, String doctorID, Date appointmentDate,
                String appointmentReason, String appointmentStatus) {
            this.appointmentID = appointmentID;
            this.patientID = patientID;
            this.doctorID = doctorID;
            this.appointmentDate = appointmentDate;
            this.appointmentReason = appointmentReason;
            this.appointmentStatus = appointmentStatus;
        }
    }

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Saving data before exit...");
            DataManager.saveAllData();
            if (scanner != null) {
                scanner.close();
            }
        }));

        DataManager.loadAllData();
        if (!hasAdminUser()) {
            createAdminUser();
        }
        if (doctors.isEmpty()) {
            preloadDoctors();
        }
        while (true) {
            if (loggedInUser == null) {
                displayMainMenu();
                handleMainMenuChoice(Utilities.getIntegerInput("Choice: "));
            } else {
                switch (userRole) {
                    case USER_ADMIN:
                        AdminFunctions.showAdminMenu();
                        break;
                    case USER_DOCTOR:
                        UserFunctions.showDoctorMenu();
                        break;
                    case USER_PATIENT:
                        UserFunctions.showPatientMenu();
                        break;
                }
            }
        }
    }

    static boolean hasAdminUser() {
        for (String[] userData : users.values()) {
            if (userData[0].equals(String.valueOf(USER_ADMIN))) {
                return true;
            }
        }
        return false;
    }

    static void createAdminUser() {
        String[] admin = { String.valueOf(USER_ADMIN), "admin123" };
        users.put("admin", admin);
        recordLog("Admin account created");
        DataManager.saveUserData();
    }

    static void preloadDoctors() {
        String[][] doctorData = {
                { "D001", "anilsharma", "Anil@123", "9876543210", "Active", "Cardiology", "5", "Anil Sharma" },
                { "D002", "priyapatel", "Priya@123", "8765432109", "Active", "Cardiology", "10", "Priya Patel" },
                { "D003", "vikrammehta", "Vikram@123", "7654321098", "Active", "Cardiology", "15", "Vikram Mehta" },
                { "D004", "neharani", "Neha@123", "6543210987", "Active", "Cardiology", "20", "Neha Rani" },
                { "D005", "rohitgupta", "Rohit@123", "9876543211", "Active", "Cardiology", "25", "Rohit Gupta" },
                { "D006", "rahulverma", "Rahul@123", "8765432110", "Active", "Neurology", "7", "Rahul Verma" },
                { "D007", "swetasingh", "Sweta@123", "7654321099", "Active", "Neurology", "12", "Sweta Singh" },
                { "D008", "arunkumar", "Arun@123", "6543210988", "Active", "Neurology", "18", "Arun Kumar" },
                { "D009", "meenakshi", "Meenakshi@123", "9876543212", "Active", "Neurology", "22", "Meenakshi" },
                { "D010", "sanjayjain", "Sanjay@123", "8765432111", "Active", "Neurology", "27", "Sanjay Jain" },
                { "D011", "kavitashah", "Kavita@123", "7654321100", "Active", "Orthopedics", "6", "Kavita Shah" },
                { "D012", "manishdesai", "Manish@123", "6543210989", "Active", "Orthopedics", "11", "Manish Desai" },
                { "D013", "anjalinair", "Anjali@123", "9876543213", "Active", "Orthopedics", "16", "Anjali Nair" },
                { "D014", "deepakjoshi", "Deepak@123", "8765432112", "Active", "Orthopedics", "21", "Deepak Joshi" },
                { "D015", "ritikapandey", "Ritika@123", "7654321101", "Active", "Orthopedics", "26", "Ritika Pandey" },
                { "D016", "sureshreddy", "Suresh@123", "6543210990", "Active", "Pediatrics", "8", "Suresh Reddy" },
                { "D017", "poojamishra", "Pooja@123", "9876543214", "Active", "Pediatrics", "13", "Pooja Mishra" },
                { "D018", "amitabhpal", "Amitabh@123", "8765432113", "Active", "Pediatrics", "19", "Amitabh Pal" },
                { "D019", "shraddhadas", "Shraddha@123", "7654321102", "Active", "Pediatrics", "24", "Shraddha Das" },
                { "D020", "naveenroy", "Naveen@123", "6543210991", "Active", "Pediatrics", "29", "Naveen Roy" },
                { "D021", "lakshmirao", "Lakshmi@123", "9876543215", "Active", "General Medicine", "9", "Lakshmi Rao" },
                { "D022", "vivekbansal", "Vivek@123", "8765432114", "Active", "General Medicine", "14",
                        "Vivek Bansal" },
                { "D023", "sonalimishra", "Sonali@123", "7654321103", "Active", "General Medicine", "17",
                        "Sonali Mishra" },
                { "D024", "rajeshkhanna", "Rajesh@123", "6543210992", "Active", "General Medicine", "23",
                        "Rajesh Khanna" },
                { "D025", "preetisingh", "Preeti@123", "9876543216", "Active", "General Medicine", "28",
                        "Preeti Singh" }
        };

        for (String[] data : doctorData) {
            String[] userData = { String.valueOf(USER_DOCTOR), data[2] };
            users.put(data[1], userData);
            doctors.add(data);
            recordLog("Added doctor: " + data[7]);
        }
        DataManager.saveAllData();
    }

    static void displayMainMenu() {
        System.out.println("\n===== Medical Appointment Scheduler =====");
        System.out.println("1. Login");
        System.out.println("2. Register Patient");
        System.out.println("3. Exit");
        System.out.println("========================================");
    }

    static void handleMainMenuChoice(int choice) {
        switch (choice) {
            case 1:
                performLogin();
                break;
            case 2:
                registerPatient();
                break;
            case 3:
                System.out.println("Exiting system...");
                DataManager.saveAllData();
                System.exit(0);
            default:
                System.out.println("Invalid choice!");
        }
    }

    static void performLogin() {
        String input = Utilities.getStringInput("Username or Name: ");
        String password = Utilities.getStringInput("Password: ");
        String normalizedInput = input.replaceAll("\\s+", "").toLowerCase();

        // Check for admin login
        if (normalizedInput.equals("admin") && users.containsKey("admin")) {
            String[] userData = users.get("admin");
            if (password.equals(userData[1])) {
                handleLoginSuccess("admin", USER_ADMIN, "A001");
                return;
            } else {
                System.out.println("Incorrect password!");
                performLogin();
                return;
            }
        }

        // Check for doctor login
        String doctorUsername = null;
        String doctorID = null;
        for (String[] doctor : doctors) {
            if (doctor.length > 7 && (doctor[7].replaceAll("\\s+", "").toLowerCase().equals(normalizedInput)
                    || doctor[1].equals(normalizedInput))) {
                doctorUsername = doctor[1];
                doctorID = doctor[0];
                break;
            }
        }
        if (doctorUsername != null && users.containsKey(doctorUsername)) {
            String[] userData = users.get(doctorUsername);
            for (String[] doctor : doctors) {
                if (doctor[1].equals(doctorUsername) && doctor[2].equals(password)) {
                    if (doctor[4].equals("Inactive")) {
                        System.out.println("Account deactivated! Contact admin.");
                        performLogin();
                        return;
                    }
                    handleLoginSuccess(doctorUsername, USER_DOCTOR, doctorID);
                    return;
                }
            }
            System.out.println("Incorrect password!");
            performLogin();
            return;
        }

        // Check for patient login
        String patientUsername = null;
        String patientID = null;
        for (String[] patient : patients) {
            if ((patient[1].equals(normalizedInput) || (patient.length > 5
                    && patient[5].replaceAll("\\s+", "").toLowerCase().equals(normalizedInput)))) {
                patientUsername = patient[1];
                patientID = patient[0];
                break;
            }
        }
        if (patientUsername != null && users.containsKey(patientUsername)) {
            String[] userData = users.get(patientUsername);
            for (String[] patient : patients) {
                if (patient[1].equals(patientUsername) && patient[2].equals(password)) {
                    if (patient[4].equals("Inactive")) {
                        System.out.println("Account deactivated! Contact admin.");
                        performLogin();
                        return;
                    }
                    handleLoginSuccess(patientUsername, USER_PATIENT, patientID);
                    return;
                }
            }
            System.out.println("Incorrect password!");
            performLogin();
            return;
        }

        System.out.println("User not found!");
        performLogin();
    }

    static void handleLoginSuccess(String username, int role, String id) {
        loggedInUser = username;
        userRole = role;
        userID = id;
        recordLog(username + " logged in");
        System.out.println("Login successful!");
    }

    static void registerPatient() {
        String usernameInput = Utilities.getStringInput("Enter username: ");
        String normalizedUsername = usernameInput.replaceAll("\\s+", "").toLowerCase();
        if (!Utilities.isValidUsername(usernameInput)) {
            System.out.println("Username must be alphanumeric or contain spaces!");
            registerPatient();
            return;
        }
        if (users.containsKey(normalizedUsername)) {
            System.out.println("Username already taken!");
            registerPatient();
            return;
        }
        String displayName = Utilities.getStringInput("Enter full name: ");
        String password = Utilities.getValidPassword("Enter password: ");
        String phone = Utilities.getValidPhoneNumber("Enter phone number: ");
        String patientID = "P" + String.format("%03d", patients.size() + 1);

        String[] userData = { String.valueOf(USER_PATIENT), password };
        String[] patientData = { patientID, normalizedUsername, password, phone, "Active", displayName };

        users.put(normalizedUsername, userData);
        patients.add(patientData);
        recordLog("New patient registered: " + displayName);
        DataManager.saveAllData();
        System.out.println("Registration successful! Your login username is: " + normalizedUsername);
    }

    static void recordLog(String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        operationLogs.add(timestamp + " - " + message);
    }

    static void performLogout() {
        if (loggedInUser != null) {
            recordLog(loggedInUser + " logged out");
            loggedInUser = null;
            userRole = 0;
            userID = null;
            DataManager.saveAllData();
            System.out.println("Logged out successfully!");
        }
    }
}