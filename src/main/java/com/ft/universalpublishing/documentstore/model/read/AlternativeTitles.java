package com.ft.universalpublishing.documentstore.model.read;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import java.util.Objects;

public class AlternativeTitles {
	
  @JsonInclude(JsonInclude.Include.NON_NULL)	
  private final String promotionalTitle;
  
  public static Builder builder() {
    return new Builder();
  }
  
  private AlternativeTitles(@JsonProperty("promotionalTitle") String promotionalTitle) {
    this.promotionalTitle = promotionalTitle;
  }
  
  public String getPromotionalTitle() {
    return promotionalTitle;
  }
  
  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("promotionalTitle", promotionalTitle)
        .toString();
  }
  
  @Override
  public boolean equals(Object o) {
    if ((o == null) || (o.getClass() != AlternativeTitles.class)) {
      return false;
    }
    
    AlternativeTitles that = (AlternativeTitles)o;
    return Objects.equals(this.promotionalTitle, that.promotionalTitle);
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(promotionalTitle);
  }
  
  public static class Builder {
    private String promotionalTitle;
    
    public Builder withPromotionalTitle(String title) {
        this.promotionalTitle = title;
        return this;
    }
    
    public Builder withValuesFrom(AlternativeTitles titles) {
      return withPromotionalTitle(titles.getPromotionalTitle());
    }

    public AlternativeTitles build() {
        return new AlternativeTitles(promotionalTitle);
    }
  }
}
