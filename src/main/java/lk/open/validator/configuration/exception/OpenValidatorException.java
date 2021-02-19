package lk.open.validator.configuration.exception;

import lk.open.validator.model.ErrorWrapper;

public class OpenValidatorException extends RuntimeException {

    private ErrorWrapper errorWrapper;

    public OpenValidatorException(ErrorWrapper errorWrapper) {
        this.errorWrapper = errorWrapper;
    }

    public ErrorWrapper getErrorWrapper() {
        return errorWrapper;
    }

}
