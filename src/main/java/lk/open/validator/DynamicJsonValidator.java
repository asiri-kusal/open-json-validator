package lk.open.validator;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;

import lk.open.validator.model.ErrorMessage;
import lk.open.validator.model.ErrorWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Component
@RefreshScope
public class DynamicJsonValidator {
    private final static Logger LOGGER = LogManager.getLogger(DynamicJsonValidator.class);

    @Autowired
    @Qualifier("validationSchema")
    private Map<String, Object> ValidationSchema;

    @Autowired
    private ValidationService validationService;

    public ErrorWrapper errorList(Map<String, Object> jsonMap, String schema)
        throws ExecutionException, InterruptedException {

        List<ErrorMessage> errorList = new CopyOnWriteArrayList<>();
        ErrorWrapper errorWrapper = new ErrorWrapper();
        Map<String, Object> validationMap = (Map<String, Object>) ValidationSchema.get("validationSchema");
        Map<String, Object> validationSubMap;
        if (validationMap == null) {
            validationSubMap = (Map<String, Object>) ValidationSchema.get(schema);
        } else {
            validationSubMap = (Map<String, Object>) validationMap.get(schema);
        }
        CompletableFuture<List<ErrorMessage>> mandatoryFields = validationService
            .validateMandatoryFieldsInJsonMap(jsonMap, validationSubMap,
                                              new CopyOnWriteArrayList<>());
        CompletableFuture<List<ErrorMessage>> validationFields = validationService
            .validateJsonMap(jsonMap, validationSubMap, "root",
                             new CopyOnWriteArrayList<>(), null);

        CompletableFuture.allOf(mandatoryFields, validationFields).join();
        errorList.addAll(mandatoryFields.get());
        errorList.addAll(validationFields.get());
        errorWrapper.setErrorList(errorList);
        return errorWrapper;
    }
}
