package trial;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.Stack;

public class MainScheduler {
    static final String PATIENTS_FILE = "patients.txt";
    static final String DOCTORS_FILE = "doctors.txt";
    static final String APPOINTMENTS_FILE = "appointments.txt";
    static final String USERS_FILE = "users.txt";
    static final String REQUESTS_FILE = "requests.txt";
    static final String LOGS_FILE = "logs.txt";

    static final int USER_ADMIN = 1;
    static final int USER_DOCTOR = 2;
    static final int USER_PATIENT = 3;

    static HashMap<String, String[]> users = new HashMap<>();
    static ArrayList<String[]> patients = new ArrayList<>();
    static ArrayList<String[]> doctors = new ArrayList<>();
    static LinkedList<String[]> appointmentHistory = new LinkedList<>();
    static Queue<String[]> appointmentRequests = new LinkedList<>();
    static Stack<String> operationLogs = new Stack<>();
    static AppointmentNode appointmentTreeRoot = null;

    static String loggedInUser = null;
    static int userRole = 0;
    static String userID = null;

    static Scanner scanner = new Scanner(System.in);
    static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    static SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("dd/MM/yyyy");

    static class AppointmentNode {
        String appointmentID;
        String patientID;
        String doctorID;
        Date appointmentDate;
        String appointmentReason;
        String appointmentStatus;
        AppointmentNode leftChild;
        AppointmentNode rightChild;

        AppointmentNode(String appointmentID, String patientID, String doctorID, Date appointmentDate,
                String appointmentReason, String appointmentStatus) {
            this.appointmentID = appointmentID;
            this.patientID = patientID;
            this.doctorID = doctorID;
            this.appointmentDate = appointmentDate;
            this.appointmentReason = appointmentReason;
            this.appointmentStatus = appointmentStatus;
            this.leftChild = null;
            this.rightChild = null;
        }
    }

    public static void main(String args[]) {
        DataManager.loadAllData();
        if (!hasAdminUser()) {
            createAdminUser();
        }
        try {
            while (true) {
                if (loggedInUser == null) {
                    displayMainMenu();
                } else {
                    if (userRole == USER_ADMIN) {
                        AdminFunctions.showAdminMenu();
                    } else if (userRole == USER_DOCTOR) {
                        UserFunctions.showDoctorMenu();
                    } else {
                        UserFunctions.showPatientMenu();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Goodbye!");
        } finally {
            DataManager.saveAllData();
        }
    }

    static boolean hasAdminUser() {
        for (Map.Entry<String, String[]> entry : users.entrySet()) {
            if (entry.getValue()[1].equals(String.valueOf(USER_ADMIN))) {
                return true;
            }
        }
        return false;
    }

    static void createAdminUser() {
        String[] admin = { "admin123", String.valueOf(USER_ADMIN), "A001" };
        users.put("admin", admin);
        recordLog("Admin account created");
    }

    static void displayMainMenu() {
        System.out.println("\n===== Medical Appointment Scheduler =====");
        System.out.println("1. Login");
        System.out.println("2. Register Patient");
        System.out.println("3. Exit");
        System.out.println("========================================");
        int menuChoice = getIntegerInput("Choice: ");
        if (menuChoice == 1) {
            performLogin();
        } else if (menuChoice == 2) {
            registerPatient();
        } else if (menuChoice == 3) {
            System.out.println("Goodbye!");
            System.exit(0);
        } else {
            System.out.println("Wrong choice!");
        }
    }

    static void performLogin() {
        String inputUsername = getStringInput("Username: ");
        String inputPassword = getStringInput("Password: ");
        if (users.containsKey(inputUsername)) {
            String[] userData = users.get(inputUsername);
            if (inputUsername.equals("admin") && inputPassword.equals("admin123")) {
                handleLoginSuccess(inputUsername, USER_ADMIN, "A001");
                return;
            }
            if (userData[0].equals(inputPassword)) {
                if (userData[1].equals(String.valueOf(USER_DOCTOR))) {
                    for (String[] doctorData : doctors) {
                        if (doctorData[0].equals(userData[2]) && doctorData[4].equals("Inactive")) {
                            System.out.println("Account deactivated! Contact admin.");
                            return;
                        }
                    }
                } else if (userData[1].equals(String.valueOf(USER_PATIENT))) {
                    for (String[] patientData : patients) {
                        if (patientData[0].equals(userData[2]) && patientData[2].equals("Inactive")) {
                            System.out.println("Account deactivated! Contact admin.");
                            return;
                        }
                    }
                }
                handleLoginSuccess(inputUsername, Integer.parseInt(userData[1]), userData[2]);
            } else {
                System.out.println("Wrong password!");
            }
        } else {
            System.out.println("User not found!");
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
        String newUsername = getStringInput("Enter username: ");
        if (users.containsKey(newUsername)) {
            System.out.println("Username taken!");
            return;
        }
        String newPassword = getValidPassword("Enter password: ");
        String phoneNumber = getValidPhoneNumber("Enter phone number: ");
        String newPatientID = "P" + String.format("%03d", patients.size() + 1);
        String[] newUserData = { newPassword, String.valueOf(USER_PATIENT), newPatientID };
        users.put(newUsername, newUserData);
        String[] newPatientData = { newPatientID, phoneNumber, "Active" };
        patients.add(newPatientData);
        recordLog("New patient registered: " + newUsername);
        System.out.println("Registration successful!");
    }

    static void recordLog(String logMessage) {
        String logTime = new Date().toString();
        operationLogs.push(logTime + " - " + logMessage);
    }

    static String getStringInput(String inputPrompt) {
        System.out.print(inputPrompt);
        return scanner.nextLine().trim();
    }

    static int getIntegerInput(String inputPrompt) {
        while (true) {
            try {
                System.out.print(inputPrompt);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Enter a number!");
            }
        }
    }

    static String getValidPhoneNumber(String inputPrompt) {
        while (true) {
            System.out.print(inputPrompt);
            String phoneNumber = scanner.nextLine().trim();
            String phoneDigits = phoneNumber.replaceAll("[^0-9]", "");
            if (phoneDigits.length() != 10) {
                System.out.println("Need 10 digits!");
                continue;
            }
            if (isPhoneNumberUnique(phoneDigits)) {
                return phoneDigits;
            } else {
                System.out.println("Phone already used!");
            }
        }
    }

    static boolean isPhoneNumberUnique(String phoneNumber) {
        for (String[] patientData : patients) {
            if (patientData[1].equals(phoneNumber)) {
                return false;
            }
        }
        for (String[] doctorData : doctors) {
            if (doctorData[3].equals(phoneNumber)) {
                return false;
            }
        }
        return true;
    }

    static String getValidPassword(String inputPrompt) {
        while (true) {
            System.out.print(inputPrompt);
            String inputPassword = scanner.nextLine().trim();
            if (inputPassword.length() >= 6) {
                return inputPassword;
            }
            System.out.println("Password too short!");
        }
    }

    static void performLogout() {
        recordLog(loggedInUser + " logged out");
        loggedInUser = null;
        userRole = 0;
        userID = null;
        System.out.println("Logged out!");
    }
}