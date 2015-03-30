package com.ft.universalpublishing.documentstore.exception;

import java.util.UUID;

@SuppressWarnings("serial")
public class ContentNotFoundException extends RuntimeException {
	
	private final UUID uuid; 

	public ContentNotFoundException(UUID uuid) {
		super();
		this.uuid = uuid;
	}

	public UUID getUuid() {
		return uuid;
	}
	
	@Override
	public String getMessage(){
		return String.format("Content with uuid : %s not found!", uuid);  
	}

}
