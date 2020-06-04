package com.ft.universalpublishing.documentstore.handler;

import com.ft.universalpublishing.documentstore.model.read.Context;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PreSaveFieldRemovalHandler implements Handler {

  private static final List<String> FIELDS_TO_REMOVE =
      Arrays.asList("storyPackage", "contentPackage");

  @Override
  public void handle(final Context context) {
    final Map<String, Object> contentMap = context.getContentMap();

    for (final String fieldToRemove : FIELDS_TO_REMOVE) {
      contentMap.remove(fieldToRemove);
    }
  }
}
