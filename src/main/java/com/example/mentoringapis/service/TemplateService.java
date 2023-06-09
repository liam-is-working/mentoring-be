package com.example.mentoringapis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.pebbletemplates.pebble.PebbleEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TemplateService {
    private final PebbleEngine pebbleEngine;
    private final ObjectMapper om;

    public Object render(String templateName, Map<String, Object> context) throws IOException {
        return om.readValue(renderAsString(templateName, context), Object.class);
    }

    public String renderAsString(String templateName, Map<String, Object> context) throws IOException {
        var stringWriter = new StringWriter();
        pebbleEngine.getTemplate(templateName).evaluate(stringWriter, context);
        return stringWriter.toString();
    }
}
