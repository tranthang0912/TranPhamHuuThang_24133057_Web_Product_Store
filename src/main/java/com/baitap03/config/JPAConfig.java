package com.baitap03.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;
import com.baitap03.util.Constants;

public class JPAConfig {

    private static final EntityManagerFactory factory =
            createFactory();

    private static EntityManagerFactory createFactory() {
        Map<String, Object> settings = new HashMap<>();
        override(settings, "APP_JDBC_URL", "jakarta.persistence.jdbc.url");
        override(settings, "APP_JDBC_USER", "jakarta.persistence.jdbc.user");
        override(settings, "APP_JDBC_PASSWORD", "jakarta.persistence.jdbc.password");
        override(settings, "APP_JDBC_DRIVER", "jakarta.persistence.jdbc.driver");
        override(settings, "APP_DB_DIALECT", "hibernate.dialect");
        return Persistence.createEntityManagerFactory("dataSource", settings);
    }

    private static void override(Map<String, Object> settings, String environment, String property) {
        String value = Constants.readSetting(environment, null);
        if (value != null) settings.put(property, value);
    }


    public static EntityManager getEntityManager() {

        return factory.createEntityManager();
    }


    public static void shutdown() {

        if (factory != null && factory.isOpen()) {

            factory.close();
        }
    }
}
