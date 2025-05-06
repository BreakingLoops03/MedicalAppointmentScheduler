package trial;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

public class DataManager {
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
        String[] dataFiles = {
                MainScheduler.USERS_FILE,
                MainScheduler.PATIENTS_FILE,
                MainScheduler.DOCTORS_FILE,
                MainScheduler.APPOINTMENTS_FILE,
                MainScheduler.REQUESTS_FILE,
                MainScheduler.LOGS_FILE
        };
        for (String fileName : dataFiles) {
            try {
                new File(fileName).createNewFile();
            } catch (IOException e) {
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
        try (BufferedReader fileReader = new BufferedReader(new FileReader(MainScheduler.USERS_FILE))) {
            String fileLine;
            while ((fileLine = fileReader.readLine()) != null) {
                String[] lineParts = fileLine.split("\\|");
                if (lineParts.length == 4) {
                    String[] userData = { lineParts[1], lineParts[2], lineParts[3] };
                    MainScheduler.users.put(lineParts[0], userData);
                }
            }
        } catch (IOException e) {
        }
    }

    static void saveUserData() {
        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(MainScheduler.USERS_FILE))) {
            for (Map.Entry<String, String[]> entry : MainScheduler.users.entrySet()) {
                String[] userData = entry.getValue();
                fileWriter.write(entry.getKey() + "|" + userData[0] + "|" + userData[1] + "|" + userData[2]);
                fileWriter.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving users!");
        }
    }

    static void loadPatientData() {
        try (BufferedReader fileReader = new BufferedReader(new FileReader(MainScheduler.PATIENTS_FILE))) {
            String fileLine;
            while ((fileLine = fileReader.readLine()) != null) {
                String[] lineParts = fileLine.split("\\|");
                if (lineParts.length == 3) {
                    MainScheduler.patients.add(lineParts);
                }
            }
        } catch (IOException e) {
        }
    }

    static void savePatientData() {
        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(MainScheduler.PATIENTS_FILE))) {
            for (String[] patientData : MainScheduler.patients) {
                fileWriter.write(String.join("|", patientData));
                fileWriter.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving patients!");
        }
    }

    static void loadDoctorData() {
        try (BufferedReader fileReader = new BufferedReader(new FileReader(MainScheduler.DOCTORS_FILE))) {
            String fileLine;
            while ((fileLine = fileReader.readLine()) != null) {
                String[] lineParts = fileLine.split("\\|");
                if (lineParts.length == 5) {
                    MainScheduler.doctors.add(lineParts);
                }
            }
        } catch (IOException e) {
        }
    }

    static void saveDoctorData() {
        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(MainScheduler.DOCTORS_FILE))) {
            for (String[] doctorData : MainScheduler.doctors) {
                fileWriter.write(String.join("|", doctorData));
                fileWriter.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving doctors!");
        }
    }

    static void loadAppointmentData() {
        try (BufferedReader fileReader = new BufferedReader(new FileReader(MainScheduler.APPOINTMENTS_FILE))) {
            String fileLine;
            while ((fileLine = fileReader.readLine()) != null) {
                String[] lineParts = fileLine.split("\\|");
                if (lineParts.length >= 7) {
                    MainScheduler.appointmentHistory.add(lineParts);
                    if (!lineParts[5].equals("Cancelled")) {
                        try {
                            Date date = MainScheduler.dateTimeFormat.parse(lineParts[3]);
                            MainScheduler.AppointmentNode newNode = new MainScheduler.AppointmentNode(
                                    lineParts[0], lineParts[1], lineParts[2], date, lineParts[4], lineParts[5]);
                            insertAppointmentNode(newNode);
                        } catch (ParseException e) {
                        }
                    }
                }
            }
        } catch (IOException e) {
        }
    }

    static void saveAppointmentData() {
        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(MainScheduler.APPOINTMENTS_FILE))) {
            for (String[] appointmentData : MainScheduler.appointmentHistory) {
                fileWriter.write(String.join("|", appointmentData));
                fileWriter.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving appointments!");
        }
    }

    static void insertAppointmentNode(MainScheduler.AppointmentNode newNode) {
        if (MainScheduler.appointmentTreeRoot == null) {
            MainScheduler.appointmentTreeRoot = newNode;
        } else {
            insertTreeNode(MainScheduler.appointmentTreeRoot, newNode);
        }
    }

    static void insertTreeNode(MainScheduler.AppointmentNode currentNode, MainScheduler.AppointmentNode newNode) {
        if (newNode.appointmentDate.before(currentNode.appointmentDate)) {
            if (currentNode.leftChild == null) {
                currentNode.leftChild = newNode;
            } else {
                insertTreeNode(currentNode.leftChild, newNode);
            }
        } else {
            if (currentNode.rightChild == null) {
                currentNode.rightChild = newNode;
            } else {
                insertTreeNode(currentNode.rightChild, newNode);
            }
        }
    }

    static void loadRequestData() {
        try (BufferedReader fileReader = new BufferedReader(new FileReader(MainScheduler.REQUESTS_FILE))) {
            String fileLine;
            while ((fileLine = fileReader.readLine()) != null) {
                String[] lineParts = fileLine.split("\\|");
                if (lineParts.length == 5) {
                    MainScheduler.appointmentRequests.add(lineParts);
                }
            }
        } catch (IOException e) {
        }
    }

    static void saveRequestData() {
        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(MainScheduler.REQUESTS_FILE))) {
            for (String[] requestData : MainScheduler.appointmentRequests) {
                fileWriter.write(String.join("|", requestData));
                fileWriter.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving requests!");
        }
    }

    static void loadLogData() {
        try (BufferedReader fileReader = new BufferedReader(new FileReader(MainScheduler.LOGS_FILE))) {
            String fileLine;
            while ((fileLine = fileReader.readLine()) != null) {
                MainScheduler.operationLogs.push(fileLine);
            }
        } catch (IOException e) {
        }
    }

    static void saveLogData() {
        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(MainScheduler.LOGS_FILE))) {
            for (String logEntry : MainScheduler.operationLogs) {
                fileWriter.write(logEntry);
                fileWriter.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving logs!");
        }
    }
}