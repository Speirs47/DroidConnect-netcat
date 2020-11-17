package io.fyko.droidconnect;

import java.io.*;
import java.util.Properties;

public class Config {
    private static String configDir = System.getProperty("user.home") + "/";
    private static String configFile = ".droidconnect-netcat.xml";
    private static String[] settings = { "host", "port", "content", "key" };

    public static String getConfigDir() {
        return configDir;
    }

    private static void createConfig() {
        Properties config = new Properties();
        // if file exists, load it
        // if not, create it
        try {
            if(!(new File(configDir).exists()))
                if(!(new File(configDir).mkdirs()))
                    return;

            if((new File(configDir + configFile).exists()))
                config.loadFromXML(new FileInputStream(configDir + configFile));
            else
                if(!(new File(configDir + configFile).createNewFile()))
                    return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // checking if all settings are present in file
        // if not, add them to that file
        for(String s : settings) {
            if(config.containsKey(s)) continue;
            config.setProperty(s, "");
        }

        // saving updated config
        try {
            config.storeToXML(new FileOutputStream(configDir + configFile), "");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getSetting(String setting) {
        Properties config = new Properties();

        // check if config exists
        // if not, create it
        if(!(new File(configDir + configFile).exists())) {
            createConfig();
        }

        // check if given setting key is valid
        // if not, return empty String
        if (!isValid(setting)) return "";

        try {
            config.loadFromXML(new FileInputStream(configDir + configFile));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return config.getProperty(setting);
    }

    public static void saveSetting(String setting, String value) {
        Properties config = new Properties();

        // check if config exists
        // if not, create it
        if(!(new File(configDir + configFile).exists())) {
            createConfig();
        }

        // check if given setting key is valid
        // if not, return empty String
        if (!isValid(setting)) return;

        // load config from file
        try {
            config.loadFromXML(new FileInputStream(configDir + configFile));
        } catch (IOException e) {
            e.printStackTrace();
        }

        config.setProperty(setting, value);

        // saving updated config file
        try {
            config.storeToXML(new FileOutputStream(configDir + configFile), "");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean isValid(String setting) {
        boolean valid = false;
        for (String s: settings) {
            if (s.equals(setting)) {
                valid = true;
                break;
            }
        }
        return valid;
    }

}
