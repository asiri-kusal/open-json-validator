package lk.open.validator.configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application.properties")
public class Config {

    private final static Logger LOGGER = LogManager.getLogger(Config.class);

    @Value("${validator.schema.name}")
    private String validationSchema;

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean(name = "validationSchema")
    public Map<String, Object> getValidationSchema() throws IOException {

        ClassLoader classLoader = Config.class.getClassLoader();
        File file = new File(classLoader.getResource(validationSchema).getFile());
        LOGGER.info("ValidationSchema File Found : " + file.exists());
        //Read File Content
        String content = new String(Files.readAllBytes(file.toPath()));
        LOGGER.info("Validation Schema content : " + file.exists());

        return objectMapper().readValue(content, Map.class);
    }

}
