package com.github.asirikusal.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorWrapper {
    private List<ErrorMessage> errorList;

    public List<ErrorMessage> getErrorList() {
        return errorList;
    }

    public void setErrorList(List<ErrorMessage> errorList) {
        this.errorList = errorList;
    }
}
