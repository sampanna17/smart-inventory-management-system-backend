package com.smartinventorysystem.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnvLoader {

    static {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        System.setProperty("PORT", dotenv.get("PORT"));

        System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET"));

        System.setProperty("AUTH_DB_URL", dotenv.get("AUTH_DB_URL"));
        System.setProperty("AUTH_DB_USERNAME", dotenv.get("AUTH_DB_USERNAME"));
        System.setProperty("AUTH_DB_PASSWORD", dotenv.get("AUTH_DB_PASSWORD"));

        System.setProperty("DB_URL", dotenv.get("DB_URL"));
        System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
        System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));

        System.setProperty("MAIL_USERNAME", dotenv.get("MAIL_USERNAME"));
        System.setProperty("MAIL_PASSWORD", dotenv.get("MAIL_PASSWORD"));

        System.setProperty("GOOGLE_CLIENT_ID", dotenv.get("GOOGLE_CLIENT_ID"));
        System.setProperty("GOOGLE_CLIENT_SECRET", dotenv.get("GOOGLE_CLIENT_SECRET"));

        System.setProperty("CLOUDINARY_CLOUD_NAME", dotenv.get("CLOUDINARY_CLOUD_NAME"));
        System.setProperty("CLOUDINARY_API_KEY", dotenv.get("CLOUDINARY_API_KEY"));
        System.setProperty("CLOUDINARY_API_SECRET", dotenv.get("CLOUDINARY_API_SECRET"));
    }
}