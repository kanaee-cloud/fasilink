package id.ac.campusfacility;

import id.ac.campusfacility.config.AppConfig;
import id.ac.campusfacility.config.DatabaseInitializer;
import id.ac.campusfacility.session.UserSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

public abstract class BaseTest {

    @TempDir
    protected Path tempDir;

    @BeforeEach
    void setUpDatabase() {
        File testDb = tempDir.resolve("test_fasilink.db").toFile();
        AppConfig.setDatabasePath(testDb.getAbsolutePath());
        DatabaseInitializer.initialize();
        UserSession.getInstance().clear();
    }

    @AfterEach
    void tearDown() {
        UserSession.getInstance().clear();
    }
}
