package org.mirza.route;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.HttpStatus;
import org.mirza.controller.PostController;
import org.mirza.dto.ErrorResponse;

import static spark.Spark.*;

@Slf4j
public class RouteConfig {
    private final Gson gson;

    public RouteConfig(Gson gson) {
        this.gson = gson;
    }

    public void setUpRoute() {
        setUpBeforeRequestHandling();

        setUpAfterRequestHandling();

        setUpExceptionHandling();

        path("/", () -> {
            get("", PostController::getAllPosts);
            get(":id", PostController::getPostById);
            post("", PostController::createPost);
            put(":id", PostController::updatePost);
            delete(":id", PostController::deletePost);
        });

    }

    private static void setUpAfterRequestHandling() {
        after((req, res) -> {
            // Logic after the route handler (e.g., logging or cleaning up resources)
            res.header("content-type", "application/json");

            log.info("Response sent: {}", res.status());
            log.info("Response content: {}", res.body());
        });
    }

    private static void setUpBeforeRequestHandling() {
        before((req, res) -> {
            // Logic before the route handler
            log.info("Request received: {} {}", req.requestMethod(), req.uri());
        });
    }

    private void setUpExceptionHandling() {
        exception(Exception.class, (exception, req, res) -> {
            log.warn(exception.getMessage());
            res.status(500);
            res.type("application/json");
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR_500, exception.getMessage());
            res.body(gson.toJson(errorResponse));
        });
    }
}
