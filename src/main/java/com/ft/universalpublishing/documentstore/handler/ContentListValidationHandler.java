package com.ft.universalpublishing.documentstore.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.universalpublishing.documentstore.model.read.ContentList;
import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.validators.ContentListValidator;

public class ContentListValidationHandler implements Handler {

  private ContentListValidator validator;

  public ContentListValidationHandler(ContentListValidator validator) {
    this.validator = validator;
  }

  @Override
  public void handle(Context context) {
    ContentList contentList =
        new ObjectMapper().convertValue(context.getContentMap(), ContentList.class);
    validator.validate(context.getUuid(), contentList);
  }
}
