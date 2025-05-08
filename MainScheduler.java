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
                handleMainMenuChoice(getIntegerInput("Choice: "));
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
            {"D001", "anilsharma", "Anil@123", "9876543210", "Active", "Cardiology", "5"},
            {"D002", "priyapatel", "Priya@123", "8765432109", "Active", "Cardiology", "10"},
            {"D003", "vikrammehta", "Vikram@123", "7654321098", "Active", "Cardiology", "15"},
            {"D004", "neharani", "Neha@123", "6543210987", "Active", "Cardiology", "20"},
            {"D005", "rohitgupta", "Rohit@123", "9876543211", "Active", "Cardiology", "25"},
            {"D006", "rahulverma", "Rahul@123", "8765432110", "Active", "Neurology", "7"},
            {"D007", "swetasingh", "Sweta@123", "7654321099", "Active", "Neurology", "12"},
            {"D008", "arunkumar", "Arun@123", "6543210988", "Active", "Neurology", "18"},
            {"D009", "meenakshi", "Meenakshi@123", "9876543212", "Active", "Neurology", "22"},
            {"D010", "sanjayjain", "Sanjay@123", "8765432111", "Active", "Neurology", "27"},
            {"D011", "kavitashah", "Kavita@123", "7654321100", "Active", "Orthopedics", "6"},
            {"D012", "manishdesai", "Manish@123", "6543210989", "Active", "Orthopedics", "11"},
            {"D013", "anjalinair", "Anjali@123", "9876543213", "Active", "Orthopedics", "16"},
            {"D014", "deepakjoshi", "Deepak@123", "8765432112", "Active", "Orthopedics", "21"},
            {"D015", "ritikapandey", "Ritika@123", "7654321101", "Active", "Orthopedics", "26"},
            {"D016", "sureshreddy", "Suresh@123", "6543210990", "Active", "Pediatrics", "8"},
            {"D017", "poojamishra", "Pooja@123", "9876543214", "Active", "Pediatrics", "13"},
            {"D018", "amitabhpal", "Amitabh@123", "8765432113", "Active", "Pediatrics", "19"},
            {"D019", "shraddhadas", "Shraddha@123", "7654321102", "Active", "Pediatrics", "24"},
            {"D020", "naveenroy", "Naveen@123", "6543210991", "Active", "Pediatrics", "29"},
            {"D021", "lakshmirao", "Lakshmi@123", "9876543215", "Active", "General Medicine", "9"},
            {"D022", "vivekbansal", "Vivek@123", "8765432114", "Active", "General Medicine", "14"},
            {"D023", "sonalimishra", "Sonali@123", "7654321103", "Active", "General Medicine", "17"},
            {"D024", "rajeshkhanna", "Rajesh@123", "6543210992", "Active", "General Medicine", "23"},
            {"D025", "preetisingh", "Preeti@123", "9876543216", "Active", "General Medicine", "28"}
        };

        for (String[] data : doctorData) {
            String[] userData = { String.valueOf(USER_DOCTOR), data[2] };
            users.put(data[1], userData);
            doctors.add(data);
            recordLog("Added doctor: " + data[1]);
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
        String username = getValidUsername("Username: ");
        String password = getStringInput("Password: ");

        if (!users.containsKey(username)) {
            System.out.println("User not found!");
            performLogin();
            return;
        }

        String[] userData = users.get(username);
        int role = Integer.parseInt(userData[0]);

        if (username.equals("admin") && password.equals(userData[1])) {
            handleLoginSuccess(username, USER_ADMIN, "A001");
            return;
        }

        if (role == USER_PATIENT) {
            boolean validCredentials = false;
            String patientID = null;
            for (String[] patient : patients) {
                if (patient[1].equals(username) && patient[2].equals(password)) {
                    validCredentials = true;
                    patientID = patient[0];
                    if (patient[4].equals("Inactive")) {
                        System.out.println("Account deactivated! Contact admin.");
                        performLogin();
                        return;
                    }
                    break;
                }
            }
            if (!validCredentials) {
                System.out.println("Incorrect password!");
                performLogin();
                return;
            }
            handleLoginSuccess(username, USER_PATIENT, patientID);
        } else if (role == USER_DOCTOR) {
            boolean validCredentials = false;
            String doctorID = null;
            for (String[] doctor : doctors) {
                if (doctor[1].equals(username) && doctor[2].equals(password)) {
                    validCredentials = true;
                    doctorID = doctor[0];
                    if (doctor[4].equals("Inactive")) {
                        System.out.println("Account deactivated! Contact admin.");
                        performLogin();
                        return;
                    }
                    break;
                }
            }
            if (!validCredentials) {
                System.out.println("Incorrect password!");
                performLogin();
                return;
            }
            handleLoginSuccess(username, USER_DOCTOR, doctorID);
        } else {
            System.out.println("Invalid role!");
            performLogin();
        }
    }

    static void handleLoginSuccess(String username, int role, String id) {
        loggedInUser = username;
        userRole = role;
        userID = id;
        recordLog(username + " logged in");
        System.out.println("Login successful!");
    }

    static void registerPatient() {
        String username = getValidNewUsername("Enter username: ");
        String password = getValidPassword("Enter password: ");
        String phone = getValidPhoneNumber("Enter phone number: ");
        String patientID = "P" + String.format("%03d", patients.size() + 1);

        String[] userData = { String.valueOf(USER_PATIENT), password };
        String[] patientData = { patientID, username, password, phone, "Active" };

        users.put(username, userData);
        patients.add(patientData);
        recordLog("New patient registered: " + username);
        DataManager.saveAllData();
        System.out.println("Registration successful!");
    }

    static void recordLog(String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        operationLogs.add(timestamp + " - " + message);
    }

    static String getStringInput(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            System.out.println("Input cannot be empty!");
            return getStringInput(prompt);
        }
        return input;
    }

    static int getIntegerInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                String input = scanner.nextLine().trim();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number!");
            }
        }
    }

    static String getValidUsername(String prompt) {
        while (true) {
            String username = getStringInput(prompt);
            if (users.containsKey(username)) {
                System.out.println("Username already taken!");
                continue;
            }
            return username;
        }
    }

    static String getValidNewUsername(String prompt) {
        while (true) {
            String username = getStringInput(prompt);
            if (users.containsKey(username)) {
                System.out.println("Username already taken!");
                continue;
            }
            return username;
        }
    }

    static String getValidPhoneNumber(String prompt) {
        while (true) {
            String phone = getStringInput(prompt);
            String digits = phone.replaceAll("[^0-9]", "");
            if (digits.length() != 10) {
                System.out.println("Phone number must be 10 digits!");
                continue;
            }
            if (isPhoneNumberUnique(digits)) {
                return digits;
            }
            System.out.println("Phone number already in use!");
        }
    }

    static boolean isPhoneNumberUnique(String phone) {
        return patients.stream().noneMatch(p -> p[3].equals(phone)) &&
               doctors.stream().noneMatch(d -> d[3].equals(phone));
    }

    static String getValidPassword(String prompt) {
        while (true) {
            String password = getStringInput(prompt);
            if (password.length() >= 6) {
                return password;
            }
            System.out.println("Password must be at least 6 characters!");
        }
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