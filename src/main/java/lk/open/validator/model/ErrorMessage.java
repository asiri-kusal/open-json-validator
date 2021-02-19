package lk.open.validator.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorMessage {

    private Object jsonBlock;
    private Object jsonField;
    private Object arrayIndex;
    private Object value;
    private Object message;

    public Object getJsonBlock() {
        return jsonBlock;
    }

    public void setJsonBlock(Object jsonBlock) {
        this.jsonBlock = jsonBlock;
    }

    public Object getJsonField() {
        return jsonField;
    }

    public void setJsonField(Object jsonField) {
        this.jsonField = jsonField;
    }

    public Object getArrayIndex() {
        return arrayIndex;
    }

    public void setArrayIndex(Object arrayIndex) {
        this.arrayIndex = arrayIndex;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }
}
