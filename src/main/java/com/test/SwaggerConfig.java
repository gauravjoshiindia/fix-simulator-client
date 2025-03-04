package com.test;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SwaggerConfig {
    public SwaggerConfig(MappingJackson2HttpMessageConverter converter){
        List supportedMediaType = new ArrayList(converter.getSupportedMediaTypes());
        supportedMediaType.add(new MediaType("application", "octet-stream"));
        converter.setSupportedMediaTypes(supportedMediaType);
    }
}