package com.github.asirikusal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import com.github.asirikusal.model.ErrorMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class ValidationService {
    private List<Map<String, Map<Integer, Boolean>>> mapList = new ArrayList<>();
    private String validationLevel;
    private String schemaValidationKey;
    private String validationMsg;
    private Object lastIteratedKey;
    private String type;
    private boolean isFieldExist = false;
    private List<String> readCompletedKeys = new ArrayList<>();

    @Async
    public CompletableFuture<List<ErrorMessage>> validateJsonMap(Map<String, Object> stringObjectMap,
                                                                 Map<String, Object> validationMap,
                                                                 String subLevelKey, List<ErrorMessage> errorList,
                                                                 Integer index) {
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
                    Object type = validation.get("type");
                    Object valueOfColumn = validation.get("valueColumnName");
                    if (validationValue.equalsIgnoreCase(subLevelKey) && validationKey.equals("common")) {
                        generateError(subLevelKey, key, value, validation, index, errorList);
                    } else if (validationValue.equalsIgnoreCase(subLevelKey) && value.equals(validationKey)) {
                        if (Objects.nonNull(type) && type.equals("eav")) {
                            key = getStringValue(value);
                            value = getStringValue(stringObjectMap.get(valueOfColumn));
                        }
                        generateError(subLevelKey, key, value, validation, index, errorList);
                    }
                }
            }

        }
        return CompletableFuture.completedFuture(errorList);
    }

    @Async
    public CompletableFuture<List<ErrorMessage>> validateMandatoryFieldsInJsonMap(Map<String, Object> stringObjectMap,
                                                                                  Map<String, Object> validationMap,
                                                                                  List<ErrorMessage> errorList) {

        List<Map<String, String>> validationSubMap = (List<Map<String, String>>) validationMap.get("mandatoryFields");

        if (!CollectionUtils.isEmpty(validationSubMap)) {
            for (Map<String, String> validation : validationSubMap) {
                validationLevel = validation.get("level");
                schemaValidationKey = validation.get("key");
                validationMsg = validation.get("message");
                type = validation.get("type");
                checkMandatoryFields(stringObjectMap, validationLevel, 0);
                readCompletedKeys.add(schemaValidationKey);
                if (!CollectionUtils.isEmpty(mapList) && !type.equalsIgnoreCase("eav")) {
                    for (Map<String, Map<Integer, Boolean>> matchingMap : mapList) {
                        Map<Integer, Boolean> matches = matchingMap.get(schemaValidationKey);
                        if (Objects.nonNull(matches) && matches.size() != 0) {
                            for (Map.Entry<Integer, Boolean> match : matches.entrySet()) {
                                if (!match.getValue()) {
                                    ErrorMessage message = new ErrorMessage();
                                    message.setJsonBlock(validationLevel);
                                    message.setJsonField(schemaValidationKey);
                                    message.setMessage(validationMsg);
                                    message.setArrayIndex(match.getKey());
                                    errorList.add(message);
                                }
                            }
                        }
                    }
                } else if (!isFieldExist) {
                    ErrorMessage message = new ErrorMessage();
                    message.setJsonBlock(validationLevel);
                    message.setJsonField(schemaValidationKey);
                    message.setMessage(validationMsg);
                    errorList.add(message);
                }
                mapList = new ArrayList<>();
                isFieldExist = false;
            }
            readCompletedKeys = new ArrayList<>();
        }
        return CompletableFuture.completedFuture(errorList);
    }

    private Boolean checkMandatoryFields(Map<String, Object> stringObjectMap, String level, int arrayIndex) {
        Iterator<Map.Entry<String, Object>> itr = stringObjectMap.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, Object> entry = itr.next();
            String key = entry.getKey();
            Object value = entry.getValue();
            if (type.equals("eav")) {
                lastIteratedKey = value;
            } else {
                lastIteratedKey = key;
            }
            if (type.equals("eav") && readCompletedKeys.contains(value)) {
                return false;
            }

            String subLevelKey = level;
            if (value instanceof List) {
                if (type.equals("list") && key.equals(schemaValidationKey)) {
                    isFieldExist = true;
                    return true;
                }
                List<Map<String, Object>> valueMap = (List<Map<String, Object>>) value;
                int idx = 0;
                Map<Integer, Boolean> integerBooleanMap;
                Map<String, Map<Integer, Boolean>> matchingMap;
                ListIterator<Map<String, Object>> iterator = valueMap.listIterator();
                boolean isContainsKey = valueMap.stream().anyMatch(
                    stringObjectMap1 -> stringObjectMap1.containsKey(schemaValidationKey));
                while (iterator.hasNext()) {
                    integerBooleanMap = new HashMap<>();
                    matchingMap = new HashMap<>();
                    subLevelKey = key;
                    Map<String, Object> subMap = iterator.next();
                    if (isContainsKey) {
                        boolean isExist = checkMandatoryFields(subMap, subLevelKey, idx);
                        if (subLevelKey.equals(validationLevel)) {
                            integerBooleanMap.put(idx, isExist);
                            matchingMap.put(schemaValidationKey, integerBooleanMap);
                            mapList.add(matchingMap);
                        }
                        isFieldExist = false;
                    }
                    idx++;
                }

                subLevelKey = "root";
            }
            if (value instanceof Map) {
                if (type.equals("object") && key.equals(schemaValidationKey)) {
                    isFieldExist = true;
                    return true;
                }
                subLevelKey = key;
                Map<String, Object> subMap = (Map<String, Object>) value;
                checkMandatoryFields(subMap, subLevelKey, arrayIndex);
                subLevelKey = "root";
            }
            if (type.equals("eav") && Objects.nonNull(value) && value.equals(schemaValidationKey) && subLevelKey
                .equals(validationLevel)) {
                isFieldExist = true;
            }
            if (type.equals("object-list") && Objects.nonNull(value) && key
                .equals(schemaValidationKey) && subLevelKey
                    .equals(validationLevel)) {
                isFieldExist = true;

            } else if (key.equals(schemaValidationKey) && subLevelKey.equals(validationLevel)) {
                isFieldExist = true;
            }
        }
        return isFieldExist;
    }

    private void generateError(String subLevelKey, String key, Object value,
                               Map<String, String> validation, Integer index, List<ErrorMessage> errorList) {
        if (!checkIsValid(value, validation.get("pattern"))) {
            ErrorMessage message = new ErrorMessage();
            message.setJsonBlock(subLevelKey);
            message.setJsonField(key);
            message.setValue(value);
            message.setMessage(validation.get("message"));
            message.setArrayIndex(index);
            errorList.add(message);
        }
    }

    private String getStringValue(Object obj) {
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
            return value;
        }
        return value;
    }

    private boolean checkIsValid(Object value, String pattern) {
        if (pattern.contains("\\") || pattern.contains("$") || pattern.contains("(")) {
            return Pattern.matches(pattern, getStringValue(value));
        }
        return false;
    }

}
