package lk.open.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import lk.open.validator.model.ErrorMessage;
import lk.open.validator.model.ErrorWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class DynamicJsonValidator {

    @Autowired
    @Qualifier("validationSchema")
    Map<String, Object> getValidationSchema;

    private String validationLevel;
    private String schemaValidationKey;
    private String validationMsg;
    private String isEav;
    private boolean isFieldExist = false;

    public ErrorWrapper errorList(Map<String, Object> jsonMap) {
        List<ErrorMessage> errorList = new ArrayList<>();
        ErrorWrapper errorWrapper = new ErrorWrapper();
        validateMandatoryFieldsInJsonMap(jsonMap, (Map<String, Object>) getValidationSchema.get("validationSchema"),
                                         errorList);
        validateJsonMap(jsonMap, (Map<String, Object>) getValidationSchema.get("validationSchema"), "root",
                        errorList, null);
        errorWrapper.setErrorList(errorList);
        return errorWrapper;
    }

    public List<ErrorMessage> validateJsonMap(Map<String, Object> stringObjectMap, Map<String, Object> validationMap,
                                              String subLevelKey, List<ErrorMessage> errorList, Integer index) {
        for (Map.Entry<String, Object> entry : stringObjectMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            List<Map<String, String>> validationSubMap = (List<Map<String, String>>) validationMap.get(key);

            if (value instanceof List) {
                List<Map<String, Object>> valueMap = (List<Map<String, Object>>) value;
                int arrayIndex = 0;
                for (Map<String, Object> map : valueMap) {
                    subLevelKey = key;
                    validateJsonMap(map, validationMap, subLevelKey, errorList, arrayIndex);
                    arrayIndex++;
                }
                subLevelKey = "root";
            }
            if (value instanceof Map) {
                subLevelKey = key;
                Map<String, Object> subMap = (Map<String, Object>) value;
                validateJsonMap(subMap, validationMap, subLevelKey, errorList, null);
                subLevelKey = "root";
            } else if (!CollectionUtils.isEmpty(validationSubMap)) {
                for (Map<String, String> validation : validationSubMap) {
                    String validationValue = validation.get("level");
                    String validationKey = validation.get("key");
                    if (validationValue.equalsIgnoreCase(subLevelKey) && validationKey.equals("common")) {
                        generateError(subLevelKey, key, value, validation, index, errorList);
                    } else if (validationValue.equalsIgnoreCase(subLevelKey) && value.equals(validationKey)) {
                        generateError(subLevelKey, key, value, validation, index, errorList);
                    }
                }
            }

        }
        return errorList;
    }

    public List<ErrorMessage> validateMandatoryFieldsInJsonMap(Map<String, Object> stringObjectMap,
                                                               Map<String, Object> validationMap,
                                                               List<ErrorMessage> errorList) {

        List<Map<String, String>> validationSubMap = (List<Map<String, String>>) validationMap.get("mandatoryFields");

        if (!CollectionUtils.isEmpty(validationSubMap)) {
            for (Map<String, String> validation : validationSubMap) {
                validationLevel = validation.get("level");
                schemaValidationKey = validation.get("key");
                validationMsg = validation.get("message");
                isEav = validation.get("isEav");
                checkMandatoryFields(stringObjectMap, validationLevel);
                if (!isFieldExist) {
                    ErrorMessage message = new ErrorMessage();
                    message.setJsonBlock(validationLevel);
                    message.setJsonField(schemaValidationKey);
                    message.setMessage(validationMsg);
                    errorList.add(message);
                }
                isFieldExist =false;
            }
        }
        return errorList;
    }

    private void checkMandatoryFields(Map<String, Object> stringObjectMap, String level) {
        for (Map.Entry<String, Object> entry : stringObjectMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            String subLevelKey = level;
            if (value instanceof List) {
                List<Map<String, Object>> valueMap = (List<Map<String, Object>>) value;
                int arrayIndex = 0;
                for (Map<String, Object> map : valueMap) {
                    subLevelKey = key;
                    checkMandatoryFields(map, subLevelKey);
                    arrayIndex++;
                }
                subLevelKey = "root";
            }
            if (value instanceof Map) {
                subLevelKey = key;
                Map<String, Object> subMap = (Map<String, Object>) value;
                checkMandatoryFields(subMap, subLevelKey);
                subLevelKey = "root";
            }
            if (isEav.equals("true") && value.equals(schemaValidationKey) && subLevelKey.equals(validationLevel)) {
                isFieldExist = true;
            } else if (key.equals(schemaValidationKey) && subLevelKey.equals(validationLevel)) {
                isFieldExist = true;
            }

        }
    }

    private void generateError(String subLevelKey, String key, Object value,
                               Map<String, String> validation, Integer index, List<ErrorMessage> errorList) {
        if (!validator(value, validation.get("pattern"))) {
            ErrorMessage message = new ErrorMessage();
            message.setJsonBlock(subLevelKey);
            message.setJsonField(key);
            message.setValue(value);
            message.setMessage(validation.get("message"));
            message.setArrayIndex(index);
            errorList.add(message);
        }
    }

    private boolean validator(Object obj, String pattern) {
        String value = "";
        if (obj instanceof Integer) {
            value = Integer.toString((Integer) obj); //Convert int to String
        }
        if (obj instanceof Double) {
            value = Double.toString((Double) obj); // Convert double to String
        }
        if (obj instanceof Float) {
            value = Float.toString((Float) obj); //Convert float to String
        }
        if (obj instanceof Long) {
            value = Long.toString((Long) obj);  //Convert long to String
        }
        if (obj instanceof String) {
            value = (String) obj;  //Convert long to String
        }
        if (Objects.isNull(value)) {
            return false;
        }
        if (pattern.contains("\\") || pattern.contains("$") || pattern.contains("(")) {
            return Pattern.matches(pattern, value);
        }
        return false;
    }

}
