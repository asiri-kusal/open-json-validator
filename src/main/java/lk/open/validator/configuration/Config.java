package lk.open.validator.configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@Configuration
@PropertySource("classpath:application.properties")
public class Config {

    private final static Logger LOGGER = LogManager.getLogger(Config.class);

    @Value("${validator.schema.name}")
    private String validationSchema;

    @Autowired
    private ResourceLoader resourceLoader;

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean(name = "validationSchema")
    public Map<String, Object> getValidationSchema() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:" + validationSchema);
        String text;
        StringBuffer sb = new StringBuffer();
        try (final Reader reader = new InputStreamReader(resource.getInputStream())) {
            BufferedReader br = new BufferedReader(reader);
            while ((text = br.readLine()) != null) {
                sb.append(text);
            }
        }
        return objectMapper().readValue(sb.toString(), Map.class);
    }

}
