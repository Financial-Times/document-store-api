package com.ft.universalpublishing.documentstore.transform;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.MustacheException;
import com.samskivert.mustache.Template;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class UriBuilder {

    private static final String ID_PARAMETER = "id";

    private final Map<String, Template> contentTypeTemplates;

    public UriBuilder(final Map<String, String> templates) {
        if (templates == null || templates.isEmpty()) {
            contentTypeTemplates = Collections.emptyMap();
            return;
        }
        Mustache.Compiler compiler = Mustache.compiler();
        final HashMap<String, Template> templateMap = new HashMap<>(1);
        for(Map.Entry<String,String> templateEntry : templates.entrySet()) {
            Template template = compiler.compile(templateEntry.getValue());
            templateMap.put(templateEntry.getKey(),template);
        }
        contentTypeTemplates = Collections.unmodifiableMap(templateMap);
    }

    public String mergeUrl(String type, String id) {
        final Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put(ID_PARAMETER, id);
        final Template template = contentTypeTemplates.get(type);
        if (template == null) {
            throw new BodyTransformationException("could not find type: \"" + type + "\" in types:\n" + contentTypeTemplates.keySet());
        }
        try {
            return template.execute(parameters);
        } catch (final MustacheException e) {
            throw new BodyTransformationException("Failed to parse the url template", e);
        }
    }
}
