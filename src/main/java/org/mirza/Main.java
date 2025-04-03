package org.mirza;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.HttpStatus;
import org.mirza.config.DatabaseConfig;
import org.mirza.controller.PostController;
import org.mirza.dto.BaseResponse;
import org.mirza.dto.ErrorResponse;
import org.mirza.dto.pagination.PaginationDto;
import org.mirza.dto.pagination.PaginationRequestDto;
import org.mirza.repository.PostRepositoryImpl;
import org.mirza.dto.response.PostResponseDto;
import org.mirza.entity.Post;
import org.mirza.exception.DatabaseException;
import org.mirza.route.RouteConfig;
import org.mirza.util.ValidatorUtil;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;

import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static spark.Spark.*;

@Slf4j
public class Main {

    public static final String DATABASE_ERROR = "Database error";

    public static void main(String[] args) {

        try {
            Gson gson = new Gson();

            Properties props = loadProperties();
            int appPort = Integer.parseInt(props.getProperty("app.port", "8080"));

            port(appPort);

            RouteConfig routeConfig = new RouteConfig(gson);

            routeConfig.setUpRoute();

            // wait for server to be initialized
            awaitInitialization();
            log.info("Application started successfully on port {}", appPort);

            // Add shutdown hook to close resource
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Shutting down application...");
                stop();
                DatabaseConfig.closeDataSource();
                log.info("Application stopped successfully");
            }));


        } catch (Exception e) {
            log.error("Error starting application {}", e.getMessage(), e);
            System.exit(1);
        }


    }

    private static Properties loadProperties() throws IOException {
        Properties props = new Properties();
        try (InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (inputStream != null) {
                props.load(inputStream);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return props;
    }
}
