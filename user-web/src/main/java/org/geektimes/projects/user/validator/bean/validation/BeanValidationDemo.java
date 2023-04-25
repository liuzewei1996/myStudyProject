package org.geektimes.projects.user.validator.bean.validation;

import org.geektimes.projects.user.domain.User;

import javax.validation.*;
import java.util.Set;

public class BeanValidationDemo {
    public static void main(String[] args) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        User user = new User();
        user.setPassword("admin");

        validator.validate(user).forEach(System.out::println);

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        violations.forEach(c -> System.out.println(c.getMessage()));

    }
}
