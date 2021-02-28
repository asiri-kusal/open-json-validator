package lk.open.validator.configuration;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;


@Component
@ConfigurationProperties(prefix = "validator")
@RefreshScope
public class Config {

    private final static Logger LOGGER = LogManager.getLogger(Config.class);

    @Value("${validator.schema.name}")
    private String validationSchema;

    @Value("${validator.schema.location}")
    private String validationSchemaLocation;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private ObjectMapper objectMapper;

    @Bean(name = "validationSchema")
    @RefreshScope
    public Map<String, Object> getValidationSchema() throws IOException {
        Map<String, Object> schemaMap = new HashMap<>();
        String[] validationSchemaList = validationSchema.split(",");
        LOGGER.info(validationSchema);
        if (validationSchemaList != null) {
            for (String schema : validationSchemaList) {
                String text;
                StringBuffer sb = new StringBuffer();
                try (final Reader reader = new InputStreamReader(getInputStream(schema))) {
                    BufferedReader br = new BufferedReader(reader);
                    while ((text = br.readLine()) != null) {
                        sb.append(text);
                    }
                }
                schemaMap.put(schema, objectMapper.readValue(sb.toString(), Map.class));
            }
        }
        return schemaMap;
    }

    public InputStream getInputStream(String schema) throws IOException {
        if (validationSchemaLocation.equalsIgnoreCase("classpath")) {
            Resource resource = resourceLoader.getResource("classpath:" + schema);
            return resource.getInputStream();
        }
        return new FileInputStream(schema);
    }

}
