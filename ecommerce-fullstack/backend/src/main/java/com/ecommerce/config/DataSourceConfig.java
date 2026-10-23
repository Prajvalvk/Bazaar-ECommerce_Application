package com.ecommerce.config;

import com.zaxxer.hikari.HikariDataSource;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Builds the PostgreSQL DataSource purely from environment variables.
 * Accepts either DATABASE_URL (postgres://user:pass@host:port/db, as given by Render/Neon/Railway)
 * or DB_URL (jdbc:postgresql://...) with DB_USER and DB_PASSWORD.
 */
@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource dataSource(@Value("${DATABASE_URL:}") String databaseUrl,
                                 @Value("${DB_URL:}") String dbUrl,
                                 @Value("${DB_USER:}") String dbUser,
                                 @Value("${DB_PASSWORD:}") String dbPassword) {
        String url = dbUrl, user = dbUser, pass = dbPassword;

        if (!databaseUrl.isBlank()) {
            String raw = databaseUrl.trim();
            if (raw.startsWith("jdbc:")) {
                url = raw;
            } else {
                URI uri = URI.create(raw.replaceFirst("^postgres(ql)?://", "http://"));
                String host = uri.getHost();
                int port = uri.getPort() == -1 ? 5432 : uri.getPort();
                String query = uri.getQuery();
                if (uri.getUserInfo() != null) {
                    String[] ui = uri.getUserInfo().split(":", 2);
                    user = URLDecoder.decode(ui[0], StandardCharsets.UTF_8);
                    pass = ui.length > 1 ? URLDecoder.decode(ui[1], StandardCharsets.UTF_8) : "";
                }
                url = "jdbc:postgresql://" + host + ":" + port + uri.getPath();
                // external hosts need SSL; Render internal hosts (no dot) do not
                if (query == null && host.contains(".")) query = "sslmode=require";
                if (query != null) url += "?" + query;
            }
        }
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("Database not configured. Set DATABASE_URL, or DB_URL + DB_USER + DB_PASSWORD.");
        }
        HikariDataSource ds = DataSourceBuilder.create().type(HikariDataSource.class)
                .url(url).username(user).password(pass).driverClassName("org.postgresql.Driver").build();
        ds.setMaximumPoolSize(5);
        ds.setMinimumIdle(1);
        return ds;
    }
}
