package com.baitap03.util;

public class Constants {

    public static final String UPLOAD_DIRECTORY =
            readSetting(
                    "APP_UPLOAD_DIR",
                    System.getProperty("user.home")
                            + java.io.File.separator
                            + "loginapp-uploads"
            );

    public static final int OTP_EXPIRE_MINUTES = 5;

    private Constants() {
    }

    public static String readSetting(String name, String defaultValue) {
        String value = System.getProperty(name);
        if (value == null || value.isBlank()) {
            value = System.getenv(name);
        }
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }
}
