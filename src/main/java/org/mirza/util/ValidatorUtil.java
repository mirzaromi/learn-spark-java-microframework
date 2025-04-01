package org.mirza.util;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.mirza.entity.Post;
import org.mirza.exception.ValidationException;
import spark.Request;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class ValidatorUtil {
    private static final Validator validator;

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private ValidatorUtil() {
        throw new AssertionError("No instance allowed");
    }

    public static Post parseAndValidatePostRequest(Request req, Gson gson) {
        Post post = gson.fromJson(req.body(), Post.class);
        validateObject(post);
        return post;
    }

    private static void validateObject(Object object) {
        Set<ConstraintViolation<Object>> violations = validator.validate(object);

        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("; "));

            throw new ValidationException("Validation failed: " + errorMessage);
        }
    }
}
