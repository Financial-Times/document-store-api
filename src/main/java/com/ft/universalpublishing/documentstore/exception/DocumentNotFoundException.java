package com.ft.universalpublishing.documentstore.exception;

import java.util.UUID;

@SuppressWarnings("serial")
public class DocumentNotFoundException extends RuntimeException {
	
	private final UUID uuid; 

	public DocumentNotFoundException(UUID uuid) {
		super();
		this.uuid = uuid;
	}

	public UUID getUuid() {
		return uuid;
	}
	
	@Override
	public String getMessage(){
		return String.format("Document with uuid : %s not found!", uuid);  
	}

}
