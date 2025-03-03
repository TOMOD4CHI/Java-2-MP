package org.cpi2.controllers;

import java.util.Scanner;

public class Verification {
    public static String getValidName(String name) {
        while (true) {
            name=name.trim();
            if (name.isEmpty()) {
                System.out.println(" cannot be empty.");
            } else if (!name.matches("^[A-Za-zÀ-ÿ\\s'-]+$")) {
                System.out.println("Invalid name.");
            } else {
                return name;
            }
        }
    }

    public static int getValidPhoneNumber(String phoneInput) {
        while (true) {
            try {
                phoneInput=phoneInput.trim();
                if (!phoneInput.matches("\\d{8}")) {
                    System.out.println("Invalid phone number. Must be 8 digits.");
                    continue;
                }

                return Integer.parseInt(phoneInput);
            } catch (NumberFormatException e) {
                System.out.println("Invalid phone number. Please enter 8 digits.");
            }
        }
    }

    public static String getValidAddress(String address) {
        while (true) {
            address=address.trim();
            if (address.isEmpty()) {
                System.out.println("Address cannot be empty.");
            } else if (address.length() < 5) {
                System.out.println("Address must be at least 5 characters long.");
            } else {
                return address;
            }
        }
    }

    public static int getValidCIN(String cinInput) {
        while (true) {
            try {
                cinInput=cinInput.trim();
                if (!cinInput.matches("\\d{1,8}")) {
                    System.out.println("Invalid CIN. Must be a number up to 8 digits.");
                    continue;
                }

                return Integer.parseInt(cinInput);
            } catch (NumberFormatException e) {
                System.out.println("Invalid CIN. Please enter a valid number.");
            }
        }
    }
    public static String getValidEmail(String email) {
        while (true) {
            email=email.trim();
            String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
            if (email.isEmpty()) {
                System.out.println("Email cannot be empty.");
            } else if (!email.matches(emailRegex)) {
                System.out.println("Invalid email format. Please enter a valid email address.");
            } else if (email.contains("..")) {
                System.out.println("Email cannot contain consecutive dots.");
            } else if (email.startsWith(".") || email.endsWith(".")) {
                System.out.println("Email cannot start or end with a dot.");
            } else {
                return email;
            }
        }
    }
    public static String getValidDateOfBirth(String dateInput) {
        while (true) {
            dateInput=dateInput.trim();
            if (!dateInput.matches("\\d{2}/\\d{2}/\\d{4}")) {
                System.out.println("Invalid date format. Use DD/MM/YYYY.");
                continue;
            }String[] parts = dateInput.split("/");
            int d = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            if (m < 1 || m > 12) {
                System.out.println("Invalid month  must be between 1 and 12.");
                continue;
            }
            int[] dsInm = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

            if (y % 4 == 0 && (y % 100 != 0 || y % 400 == 0)) {
                dsInm[1] = 29;
            }
            if (d < 1 || d > dsInm[m - 1]) {
                System.out.println("Invalid day for the given month.");
                continue;
            }
            return dateInput;
        }
    }
}


