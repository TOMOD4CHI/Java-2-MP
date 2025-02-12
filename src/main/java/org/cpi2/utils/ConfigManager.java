package org.cpi2.utils;

import java.io.*;

public class ConfigManager {
    private static final String FILE_PATH = "config.txt";

    public static void saveLastMenu(String menu) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            writer.write(menu);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String loadLastMenu() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            return reader.readLine();
        } catch (IOException e) {
            return "profile"; // Default menu
        }
    }
}
