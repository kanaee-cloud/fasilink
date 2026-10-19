package id.ac.campusfacility.config;

import java.io.File;

public class AppConfig {
    private static String databasePath = "data/campus_facility.db";

    public static synchronized String getDatabasePath() {
        File dataDir = new File(databasePath).getParentFile();
        if (dataDir != null && !dataDir.exists()) {
            dataDir.mkdirs();
        }
        return databasePath;
    }

    public static synchronized void setDatabasePath(String path) {
        databasePath = path;
    }

    public static String getJdbcUrl() {
        return "jdbc:sqlite:" + getDatabasePath();
    }
}
