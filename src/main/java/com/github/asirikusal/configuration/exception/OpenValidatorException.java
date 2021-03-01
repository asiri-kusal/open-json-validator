package com.github.asirikusal.configuration.exception;

import com.github.asirikusal.model.ErrorWrapper;

public class OpenValidatorException extends RuntimeException {

    private ErrorWrapper errorWrapper;

    public OpenValidatorException(ErrorWrapper errorWrapper) {
        this.errorWrapper = errorWrapper;
    }

    public ErrorWrapper getErrorWrapper() {
        return errorWrapper;
    }

}
