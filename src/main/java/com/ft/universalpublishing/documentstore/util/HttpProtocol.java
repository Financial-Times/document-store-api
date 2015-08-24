package com.ft.universalpublishing.documentstore.util;

public enum HttpProtocol {
    
    HTTP("http"), HTTPS("https");
    
    HttpProtocol(String protocol){
        this.protocol = protocol;
    }

    private String protocol;
    
    public String getValue(){
        return protocol;
    }
    
    public static HttpProtocol fromString(String protocol) {
        if (protocol == null) {
            return null;
        }
        for (HttpProtocol httpProtocol : HttpProtocol.values()) {
            if (httpProtocol.protocol.equalsIgnoreCase(protocol)) {
                return httpProtocol;
            }
        }
        return null;
      }
}
