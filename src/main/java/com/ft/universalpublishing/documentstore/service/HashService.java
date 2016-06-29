package com.ft.universalpublishing.documentstore.service;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.base.Strings;

public class HashService {
  public String hash(Map<String, Object> content) {
    StringBuilder sb = new StringBuilder();
    serializeObject(content, sb);
    
    String json = sb.toString();
//    System.err.println("hashing json:=>" + json);
    
    return hash(json);
  }
  
  private void serializeObject(Map<String, Object> jsonObject, StringBuilder buffer) {
    buffer.append('{');
    Map<String, Object> sorted = new TreeMap<>(jsonObject);
    boolean first = true;
    
    for (Map.Entry<String, Object> en : sorted.entrySet()) {
      if (!first) {
        buffer.append(',');
      } else {
        first = false;
      }
      
      buffer.append('"').append(en.getKey()).append("\":");
      
      serializeValue(en.getValue(), buffer);
    }
    buffer.append('}');
  }
  
  private void serializeCollection(Collection<Object> jsonCollection, StringBuilder buffer) {
    buffer.append('[');
    boolean first = true;
    
    for (Object value : jsonCollection) {
      if (!first) {
        buffer.append(',');
      } else {
        first = false;
      }
      
      serializeValue(value, buffer);
    }
    buffer.append(']');
  }
  
  @SuppressWarnings({"unchecked", "rawtypes"})
  private void serializeValue(Object value, StringBuilder buffer) {
    if (value instanceof String) {
      buffer.append('"').append(value).append('"');
    } else if ((value instanceof Number) || (value instanceof Boolean)) {
      buffer.append(value);
    } else if (value instanceof Map) {
      serializeObject((Map)value, buffer);
    } else if (value instanceof Collection) {
      serializeCollection((Collection)value, buffer);
    } else if (value == null) {
      buffer.append("null");
    } else {
      throw new IllegalArgumentException("unsupported object for JSON serialization: " + value);
    }
  }
  
  private String hash(String content) {
    byte[] raw = content.getBytes(UTF_8);
    util.hash.MurmurHash3.LongPair hash = new util.hash.MurmurHash3.LongPair(); 
    util.hash.MurmurHash3.murmurhash3_x64_128(raw, 0, raw.length, 0, hash);
    StringBuilder sb = new StringBuilder();
    sb.append(Strings.padStart(Long.toHexString(hash.val1), 16, '0'))
      .append(Strings.padStart(Long.toHexString(hash.val2), 16, '0'));
    
    return sb.toString();
  }
}
