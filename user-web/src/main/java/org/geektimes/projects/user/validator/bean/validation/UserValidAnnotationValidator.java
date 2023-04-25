package org.geektimes.projects.user.validator.bean.validation;

import org.geektimes.projects.user.domain.User;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UserValidAnnotationValidator implements ConstraintValidator<UserValid, User> {

    private int idRange;

    @Override
    public void initialize(UserValid constraintAnnotation) {
        idRange = constraintAnnotation.idRange();
    }

    @Override
    public boolean isValid(User value, ConstraintValidatorContext context) {
        context.getDefaultConstraintMessageTemplate();
        return false;
    }
}
