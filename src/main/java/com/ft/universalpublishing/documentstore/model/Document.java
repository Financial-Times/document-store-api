package com.ft.universalpublishing.documentstore.model;

public abstract class Document {
    
    protected static final String IDENTIFIER_TEMPLATE = "http://api.ft.com/thing/";
    protected static final String API_URL_TEMPLATE = "http://%s/%s/%s";
    
    private String _id;
    private String uuid;
    
    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getUuid() {
        return uuid;
    }
    
    public abstract void addIds();
    
    public abstract void addApiUrls(String apiPath);
    
    public abstract void removePrivateFields();

}
