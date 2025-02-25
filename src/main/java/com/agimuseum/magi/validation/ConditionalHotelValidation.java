package com.agimuseum.magi.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = HotelValidator.class)
public @interface ConditionalHotelValidation {
    String message() default "Hotel information required when staying overnight";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}