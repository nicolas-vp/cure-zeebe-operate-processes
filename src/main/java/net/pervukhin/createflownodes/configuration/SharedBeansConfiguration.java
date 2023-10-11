package net.pervukhin.createflownodes.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class SharedBeansConfiguration {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public XmlMapper xmlMapper() {
        final XmlMapper xmlMapperObject = new XmlMapper();
        xmlMapperObject.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, false);
        xmlMapperObject.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        xmlMapperObject.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        xmlMapperObject.configure(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS, false);
        return xmlMapperObject;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
