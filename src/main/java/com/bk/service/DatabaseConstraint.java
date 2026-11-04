package com.bk.service;

import org.hibernate.exception.ConstraintViolationException;

final class DatabaseConstraint {

    private DatabaseConstraint() {
    }

    static boolean matches(Throwable throwable, String constraintName) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof ConstraintViolationException violation
                    && constraintName.equalsIgnoreCase(violation.getConstraintName())) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
