package trial;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

public class DataManager {
    private static final String DELIMITER = "|";

    static void loadAllData() {
        try {
            initializeFiles();
            loadUserData();
            loadPatientData();
            loadDoctorData();
            loadAppointmentData();
            loadLogData();
        } catch (Exception e) {
            System.out.println("Error loading data: " + e.getMessage());
            System.exit(1);
        }
    }

    static void initializeFiles() {
        String[] files = {
                MainScheduler.USERS_FILE,
                MainScheduler.PATIENTS_FILE,
                MainScheduler.DOCTORS_FILE,
                MainScheduler.APPOINTMENTS_FILE,
                MainScheduler.LOGS_FILE
        };

        for (String file : files) {
            File f = new File(file);
            try {
                if (!f.exists()) {
                    f.createNewFile();
                }
            } catch (IOException e) {
                System.out.println("Error creating file " + file + ": " + e.getMessage());
                System.exit(1);
            }
        }
    }

    static void saveAllData() {
        try {
            saveUserData();
            savePatientData();
            saveDoctorData();
            saveAppointmentData();
            saveLogData();
        } catch (Exception e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

    static void loadUserData() {
        try (BufferedReader reader = new BufferedReader(new FileReader(MainScheduler.USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\" + DELIMITER);
                if (parts.length >= 2 && Utilities.isValidUsername(parts[0])) {
                    String[] userData = new String[parts.length - 1];
                    System.arraycopy(parts, 1, userData, 0, parts.length - 1);
                    MainScheduler.users.put(parts[0], userData);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading users: " + e.getMessage());
        }
    }

    static void saveUserData() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MainScheduler.USERS_FILE))) {
            for (var entry : MainScheduler.users.entrySet()) {
                String[] data = entry.getValue();
                String[] line = new String[data.length + 1];
                line[0] = entry.getKey();
                System.arraycopy(data, 0, line, 1, data.length);
                writer.write(String.join(DELIMITER, line));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving users: " + e.getMessage());
        }
    }

    static void loadPatientData() {
        try (BufferedReader reader = new BufferedReader(new FileReader(MainScheduler.PATIENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\" + DELIMITER);
                if ((parts.length == 5 || parts.length == 6) && Utilities.isValidPhoneNumber(parts[3]) &&
                        (parts[4].equals("Active") || parts[4].equals("Inactive")) &&
                        Utilities.isValidUsername(parts[1])) {
                    if (parts.length == 5) {
                        String[] newParts = new String[6];
                        System.arraycopy(parts, 0, newParts, 0, 5);
                        newParts[5] = parts[1];
                        MainScheduler.patients.add(newParts);
                    } else {
                        MainScheduler.patients.add(parts);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading patients: " + e.getMessage());
        }
    }

    static void savePatientData() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MainScheduler.PATIENTS_FILE))) {
            for (String[] patient : MainScheduler.patients) {
                writer.write(String.join(DELIMITER, patient));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving patients: " + e.getMessage());
        }
    }

    static void loadDoctorData() {
        try (BufferedReader reader = new BufferedReader(new FileReader(MainScheduler.DOCTORS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\" + DELIMITER);
                if ((parts.length == 7 || parts.length == 8) && Utilities.isValidPhoneNumber(parts[3]) &&
                        (parts[4].equals("Active") || parts[4].equals("Inactive")) &&
                        Utilities.isValidExperience(parts[6]) &&
                        Utilities.isValidUsername(parts[1])) {
                    if (parts.length == 7) {
                        String[] newParts = new String[8];
                        System.arraycopy(parts, 0, newParts, 0, 7);
                        newParts[7] = parts[1];
                        MainScheduler.doctors.add(newParts);
                    } else {
                        MainScheduler.doctors.add(parts);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading doctors: " + e.getMessage());
        }
    }

    static void saveDoctorData() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MainScheduler.DOCTORS_FILE))) {
            for (String[] doctor : MainScheduler.doctors) {
                writer.write(String.join(DELIMITER, doctor));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving doctors: " + e.getMessage());
        }
    }

    static void loadAppointmentData() {
        try (BufferedReader reader = new BufferedReader(new FileReader(MainScheduler.APPOINTMENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\" + DELIMITER);
                if (parts.length == 7 && isValidAppointment(parts)) {
                    boolean isDuplicate = false;
                    for (String[] existing : MainScheduler.appointmentHistory) {
                        if (existing[0].equals(parts[0])) {
                            isDuplicate = true;
                            break;
                        }
                    }
                    if (isDuplicate) {
                        continue;
                    }
                    MainScheduler.appointmentHistory.add(parts);
                    if (!parts[5].equals("Cancelled")) {
                        try {
                            Date date = MainScheduler.dateTimeFormat.parse(parts[3]);
                            MainScheduler.AppointmentNode node = new MainScheduler.AppointmentNode(
                                    parts[0], parts[1], parts[2], date, parts[4], parts[5]);
                            insertAppointmentNode(node);
                        } catch (ParseException e) {
                            System.out.println("Skipping invalid appointment date: " + line);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading appointments: " + e.getMessage());
        }
    }

    static boolean isValidAppointment(String[] parts) {
        boolean validDoctor = false;
        boolean validPatient = false;
        for (String[] doctor : MainScheduler.doctors) {
            if (doctor[0].equals(parts[2])) {
                validDoctor = true;
                break;
            }
        }
        for (String[] patient : MainScheduler.patients) {
            if (patient[0].equals(parts[1])) {
                validPatient = true;
                break;
            }
        }
        return validDoctor && validPatient;
    }

    static void saveAppointmentData() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MainScheduler.APPOINTMENTS_FILE))) {
            for (String[] appt : MainScheduler.appointmentHistory) {
                writer.write(String.join(DELIMITER, appt));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving appointments: " + e.getMessage());
        }
    }

    static void insertAppointmentNode(MainScheduler.AppointmentNode node) {
        MainScheduler.appointments.add(node);
        MainScheduler.appointments.sort((a, b) -> a.appointmentDate.compareTo(b.appointmentDate));
    }

    static void loadLogData() {
        try (BufferedReader reader = new BufferedReader(new FileReader(MainScheduler.LOGS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                MainScheduler.operationLogs.add(line);
            }
        } catch (IOException e) {
            System.out.println("Error loading logs: " + e.getMessage());
        }
    }

    static void saveLogData() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MainScheduler.LOGS_FILE))) {
            for (String log : MainScheduler.operationLogs) {
                writer.write(log);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving logs: " + e.getMessage());
        }
    }
}