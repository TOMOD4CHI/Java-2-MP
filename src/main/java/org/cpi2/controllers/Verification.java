package org.cpi2.controllers;

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
}



