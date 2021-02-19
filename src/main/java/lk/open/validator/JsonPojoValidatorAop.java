package lk.open.validator;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import lk.open.validator.configuration.exception.OpenValidatorException;
import lk.open.validator.model.ErrorWrapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Aspect
@Component
@ConditionalOnExpression("${aspect.enabled:true}")
public class JsonPojoValidatorAop {

    @Autowired
    private DynamicJsonValidator jsonValidator;

    @Autowired
    private ObjectMapper objectMapper;

    @Around("@annotation(lk.open.validator.JsonPojoValidator)")
    public Object validatePojo(ProceedingJoinPoint point) throws Throwable {
        Object[] arguments = point.getArgs();
        if (arguments == null) {
            throw new RuntimeException("Json binding failure");
        }
        String json = objectMapper.writeValueAsString(arguments[0]);
        Map<String, Object> jsonMap = objectMapper.readValue(json, Map.class);
        ErrorWrapper errorWrapper = jsonValidator.errorList(jsonMap);
        if (!CollectionUtils.isEmpty(errorWrapper.getErrorList())) {
            throw new OpenValidatorException(errorWrapper);
        }
        return point.proceed();
    }
}
