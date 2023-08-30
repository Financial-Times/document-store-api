package com.ft.universalpublishing.documentstore;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.jupiter.api.Test;

/**
 * MongoConfigTest
 *
 * @author Simon.Gibbs
 */
public class MongoConfigTest {

  private final MongoConfig config = new MongoConfig();

  @Test
  public void shouldFormatParameters() {
    config.setAddress("mongo-atlas-db:27017");
    config.setDb("database");
    config.setUsername("username");
    config.setPassword("password");

    assertThat(
        config.serverURI(),
        is(
            "mongodb+srv://username:password@mongo-atlas-db:27017/database?retryWrite=true&w=majority"));
  }
}
