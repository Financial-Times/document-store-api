package com.ft.universalpublishing.documentstore.handler;

import java.util.List;
import java.util.Map;

import com.ft.universalpublishing.documentstore.model.read.Context;

public class StoryPackgeRemoveHandler implements Handler {

	public static final String STORY_PACKAGE = "storyPackage";

	@Override
    public void handle(Context context) {
		Map<String,Object> contentMap = context.getContentMap();
		contentMap.remove(STORY_PACKAGE) ;
		context.setContentMap(contentMap);
	}
}
