package com.github.jsonmapvalidator.configuration.exception;

import com.github.jsonmapvalidator.model.ErrorWrapper;

public class OpenValidatorException extends RuntimeException {

    private ErrorWrapper errorWrapper;

    public OpenValidatorException(ErrorWrapper errorWrapper) {
        this.errorWrapper = errorWrapper;
    }

    public ErrorWrapper getErrorWrapper() {
        return errorWrapper;
    }

}
