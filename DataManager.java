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
        loadRequestData();
        loadLogData();
    }

    static void initializeFiles() {
        String[] files = {
                MainScheduler.USERS_FILE,
                MainScheduler.PATIENTS_FILE,
                MainScheduler.DOCTORS_FILE,
                MainScheduler.APPOINTMENTS_FILE,
                MainScheduler.REQUESTS_FILE,
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
        saveRequestData();
        saveLogData();
    }

    static void loadUserData() {
        try (BufferedReader reader = new BufferedReader(new FileReader(MainScheduler.USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\" + DELIMITER);
                if (parts.length == 2) {
                    String[] userData = { parts[1] };
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
                writer.write(String.join(DELIMITER, entry.getKey(), data[0]));
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
                if (parts.length == 5) {
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
                if (parts.length >= 7) {
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

    static void loadRequestData() {
        try (BufferedReader reader = new BufferedReader(new FileReader(MainScheduler.REQUESTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\" + DELIMITER);
                if (parts.length == 5) {
                    MainScheduler.appointmentRequests.add(parts);
                } else {
                    System.out.println("Skipping invalid request data: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading requests: " + e.getMessage());
        }
    }

    static void saveRequestData() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MainScheduler.REQUESTS_FILE))) {
            for (String[] request : MainScheduler.appointmentRequests) {
                writer.write(String.join(DELIMITER, request));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving requests: " + e.getMessage());
        }
    }

    static void loadLogData() {
        try (BufferedReader reader = new BufferedReader(new FileReader(MainScheduler.LOGS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                MainScheduler.operationLogs.push(line);
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