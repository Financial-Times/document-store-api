package com.ft.universalpublishing.documentstore.mongo;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.ft.ws.lib.serialization.datetime.ISODateTimeWithMillisFormatter;

public class JsonDateTimeWithMillisDeserializer extends JsonDeserializer<DateTime> {
    
    private DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTime();

    @Override
    public DateTime deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        String dateStr = null;
        String fieldName = null;
        
        while (jp.hasCurrentToken()) {
             JsonToken token = jp.nextToken();
             if (token == JsonToken.FIELD_NAME) {
                  fieldName = jp.getCurrentName();
             } else if (token == JsonToken.VALUE_STRING) {
                  dateStr = jp.getValueAsString();
             } else if (token == JsonToken.END_OBJECT) {
                  break;
             }
        }
        if (dateStr != null ) {
            return dateTimeFormatter.parseDateTime(dateStr);
         }
      return null;
    }

}
