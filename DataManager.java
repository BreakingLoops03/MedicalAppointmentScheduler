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
        initializeFiles();
        loadUserData();
        loadPatientData();
        loadDoctorData();
        loadAppointmentData();
        loadLogData();
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
                if (!f.exists() && !f.createNewFile()) {
                    System.out.println("Cannot create file: " + file);
                    System.exit(1);
                }
            } catch (IOException e) {
                System.out.println("Error creating file: " + file + " - " + e.getMessage());
                System.exit(1);
            }
        }
    }

    static void saveAllData() {
        saveUserData();
        savePatientData();
        saveDoctorData();
        saveAppointmentData();
        saveLogData();
    }

    static void loadUserData() {
        try (BufferedReader reader = new BufferedReader(new FileReader(MainScheduler.USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\" + DELIMITER);
                if (parts.length >= 2) {
                    String[] userData = new String[parts.length - 1];
                    System.arraycopy(parts, 1, userData, 0, parts.length - 1);
                    MainScheduler.users.put(parts[0], userData);
                } else {
                    System.out.println("Skipping invalid user data: " + line);
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
                if (parts.length == 5 && parts[3].matches("\\d{10}") &&
                        (parts[4].equals("Active") || parts[4].equals("Inactive"))) {
                    MainScheduler.patients.add(parts);
                } else {
                    System.out.println("Skipping invalid patient data: " + line);
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
                if (parts.length == 7 && parts[3].matches("\\d{10}") &&
                        (parts[4].equals("Active") || parts[4].equals("Inactive")) &&
                        parts[6].matches("\\d+")) {
                    MainScheduler.doctors.add(parts);
                } else {
                    System.out.println("Skipping invalid doctor data: " + line);
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
                if (parts.length == 7) {
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
                    if (!validDoctor || !validPatient) {
                        System.out.println("Skipping invalid appointment data: " + line);
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
                } else {
                    System.out.println("Skipping invalid appointment data: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading appointments: " + e.getMessage());
        }
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