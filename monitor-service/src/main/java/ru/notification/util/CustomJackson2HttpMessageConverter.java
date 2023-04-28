package ru.notification.util;

import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.ArrayList;
import java.util.List;

public class CustomJackson2HttpMessageConverter extends MappingJackson2HttpMessageConverter {
    public CustomJackson2HttpMessageConverter(){
        List<MediaType> mediaTypes = new ArrayList<>();
        mediaTypes.add(MediaType.APPLICATION_FORM_URLENCODED); //Add text/html type support
        setSupportedMediaTypes(mediaTypes);
    }
}