package trial;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class Utilities {
    static String getStringInput(String prompt) {
        System.out.print(prompt);
        String input = MainScheduler.scanner.nextLine().trim();
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
                String input = MainScheduler.scanner.nextLine().trim();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number!");
            }
        }
    }

    static boolean isValidUsername(String username) {
        return username.matches("^[a-zA-Z0-9\\s]+$");
    }

    static boolean isValidPhoneNumber(String phone) {
        return phone.matches("^\\d{10}$");
    }

    static String getValidPhoneNumber(String prompt) {
        while (true) {
            String phone = getStringInput(prompt);
            String digits = phone.replaceAll("[^0-9]", "");
            if (!isValidPhoneNumber(digits)) {
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
        return MainScheduler.patients.stream().noneMatch(p -> p[3].equals(phone)) &&
                MainScheduler.doctors.stream().noneMatch(d -> d[3].equals(phone));
    }

    static boolean isPhoneNumberUniqueForEdit(String phone, String id, boolean isDoctor) {
        for (String[] patient : MainScheduler.patients) {
            if (patient[3].equals(phone) && (isDoctor || !patient[0].equals(id))) {
                return false;
            }
        }
        for (String[] doctor : MainScheduler.doctors) {
            if (doctor[3].equals(phone) && (!isDoctor || !doctor[0].equals(id))) {
                return false;
            }
        }
        return true;
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

    static boolean isValidExperience(String input) {
        try {
            int experience = Integer.parseInt(input);
            return experience >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    static String getValidExperience(String prompt) {
        while (true) {
            String input = getStringInput(prompt);
            if (isValidExperience(input)) {
                return input;
            }
            System.out.println("Experience must be a non-negative number!");
        }
    }

    static String formatPhoneNumber(String phone) {
        if (phone.length() == 10) {
            return phone.substring(0, 3) + "-" + phone.substring(3, 6) + "-" + phone.substring(6);
        }
        return phone;
    }

    static boolean confirmAction(String action) {
        while (true) {
            String confirmation = getStringInput("Confirm " + action + "? (y/n): ");
            if (confirmation.equalsIgnoreCase("y")) {
                return true;
            } else if (confirmation.equalsIgnoreCase("n")) {
                System.out.println("Action cancelled!");
                return false;
            }
            System.out.println("Please enter 'y' or 'n'!");
        }
    }

    static String getValidFutureAppointmentDate(String prompt) {
        while (true) {
            String date = getStringInput(prompt);
            try {
                Date inputDate = MainScheduler.dateOnlyFormat.parse(date);
                Calendar today = Calendar.getInstance();
                today.set(Calendar.HOUR_OF_DAY, 0);
                today.set(Calendar.MINUTE, 0);
                today.set(Calendar.SECOND, 0);
                today.set(Calendar.MILLISECOND, 0);
                Calendar inputCal = Calendar.getInstance();
                inputCal.setTime(inputDate);
                inputCal.set(Calendar.HOUR_OF_DAY, 0);
                inputCal.set(Calendar.MINUTE, 0);
                inputCal.set(Calendar.SECOND, 0);
                inputCal.set(Calendar.MILLISECOND, 0);
                if (!inputCal.before(today)) {
                    return date;
                }
                System.out.println("Cannot book past or current dates!");
            } catch (ParseException e) {
                System.out.println("Please use a valid dd/MM/yyyy date!");
            }
        }
    }
}