package trial;

import java.io.*;
import java.text.ParseException;
import java.util.*;   

public class DataManager {
    static void loadData() {
        loadUsers();
        loadPatients();
        loadDoctors();
        loadAppointments();
        loadRequests();
        loadLogs();
    }
    
    static void saveData() {
        saveUsers();
        savePatients();
        saveDoctors();
        saveAppointments();
        saveRequests();
        saveLogs();
    }
    
    static void loadUsers() {
        try (BufferedReader reader = new BufferedReader(new FileReader(MainScheduler.USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 4) {
                    String[] userData = {parts[1], parts[2], parts[3]};
                    MainScheduler.users.put(parts[0], userData);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Users file not found. Starting with empty users.");
        } catch (IOException e) {
            System.out.println("Error loading users: " + e.getMessage());
        }
    }
    
    static void saveUsers() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MainScheduler.USERS_FILE))) {
            for (Map.Entry<String, String[]> entry : MainScheduler.users.entrySet()) {
                String[] data = entry.getValue();
                writer.write(entry.getKey() + "|" + data[0] + "|" + data[1] + "|" + data[2]);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving users: " + e.getMessage());
        }
    }
    
    static void loadPatients() {
        try (BufferedReader reader = new BufferedReader(new FileReader(MainScheduler.PATIENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 5) {
                    MainScheduler.patients.add(parts);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Patients file not found. Starting with empty patients.");
        } catch (IOException e) {
            System.out.println("Error loading patients: " + e.getMessage());
        }
    }
    
    static void savePatients() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MainScheduler.PATIENTS_FILE))) {
            for (String[] patient : MainScheduler.patients) {
                writer.write(String.join("|", patient));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving patients: " + e.getMessage());
        }
    }
    
    static void loadDoctors() {
        try (BufferedReader reader = new BufferedReader(new FileReader(MainScheduler.DOCTORS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 5) {
                    MainScheduler.doctors.add(parts);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Doctors file not found. Starting with empty doctors.");
        } catch (IOException e) {
            System.out.println("Error loading doctors: " + e.getMessage());
        }
    }
    
    static void saveDoctors() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MainScheduler.DOCTORS_FILE))) {
            for (String[] doctor : MainScheduler.doctors) {
                writer.write(String.join("|", doctor));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving doctors: " + e.getMessage());
        }
    }
    
    static void loadAppointments() {
        try (BufferedReader reader = new BufferedReader(new FileReader(MainScheduler.APPOINTMENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 7) {
                    MainScheduler.appointmentHistory.add(parts);
                    if (!parts[5].equals("Cancelled")) {
                        try {
                            Date date = MainScheduler.dateTimeFormat.parse(parts[3]);
                            MainScheduler.AppointmentNode node = new MainScheduler.AppointmentNode(
                                parts[0], parts[1], parts[2], date, parts[4], parts[5]);
                            if (MainScheduler.appointmentRoot == null) {
                                MainScheduler.appointmentRoot = node;
                            } else {
                                AdminFunctions.insertAppointment(MainScheduler.appointmentRoot, node);
                            }
                        } catch (ParseException e) {
                            System.out.println("Error parsing date: " + e.getMessage());
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Appointments file not found. Starting with empty appointments.");
        } catch (IOException e) {
            System.out.println("Error loading appointments: " + e.getMessage());
        }
    }
    
    static void saveAppointments() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MainScheduler.APPOINTMENTS_FILE))) {
            for (String[] appointment : MainScheduler.appointmentHistory) {
                writer.write(String.join("|", appointment));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving appointments: " + e.getMessage());
        }
    }
    
    static void loadRequests() {
        try (BufferedReader reader = new BufferedReader(new FileReader(MainScheduler.REQUESTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 5) {
                    MainScheduler.appointmentRequests.add(parts);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Requests file not found. Starting with empty requests.");
        } catch (IOException e) {
            System.out.println("Error loading requests: " + e.getMessage());
        }
    }
    
    static void saveRequests() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MainScheduler.REQUESTS_FILE))) {
            for (String[] request : MainScheduler.appointmentRequests) {
                writer.write(String.join("|", request));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving requests: " + e.getMessage());
        }
    }
    
    static void loadLogs() {
        try (BufferedReader reader = new BufferedReader(new FileReader(MainScheduler.LOGS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                MainScheduler.operationLogs.push(line);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Logs file not found. Starting with empty logs.");
        } catch (IOException e) {
            System.out.println("Error loading logs: " + e.getMessage());
        }
    }
    
    static void saveLogs() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MainScheduler.LOGS_FILE))) {
            Stack<String> tempStack = new Stack<>();
            while (!MainScheduler.operationLogs.isEmpty()) {
                String log = MainScheduler.operationLogs.pop();
                writer.write(log);
                writer.newLine();
                tempStack.push(log);
            }
            while (!tempStack.isEmpty()) {
                MainScheduler.operationLogs.push(tempStack.pop());
            }
        } catch (IOException e) {
            System.out.println("Error saving logs: " + e.getMessage());
        }
    }
}