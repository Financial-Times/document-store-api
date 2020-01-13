package com.ft.universalpublishing.documentstore;

import com.mongodb.ServerAddress;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * MongoConfigTest
 *
 * @author Simon.Gibbs
 */
public class MongoConfigTest {

    private final MongoConfig config = new MongoConfig();

    @Test
    public void shouldConvertSingularHostFieldToServerAddress() {
        config.setHost("a.example.com");

        List<ServerAddress> result = config.toServerAddresses();
        
        assertThat(result.get(0).getHost(),is("a.example.com"));
    }

    @Test
      public void shouldConvertPluralHostsFieldToServerAddresses() {

        config.setHosts(Arrays.asList("a.example.com","b.example.com"));

        List<ServerAddress> result = config.toServerAddresses();

        assertThat(result.get(0).getHost(),is("a.example.com"));
        assertThat(result.get(1).getHost(),is("b.example.com"));
    }

    @Test
    public void shouldCombineSingularAndPluralHostFields() {

        config.setHosts(Arrays.asList("a.example.com","b.example.com"));
        config.setHost("c.example.com");

        List<ServerAddress> result = config.toServerAddresses();

        assertThat(result.get(0).getHost(),is("a.example.com"));
        assertThat(result.get(1).getHost(),is("b.example.com"));
        assertThat(result.get(2).getHost(),is("c.example.com"));
    }

    @Test
    public void shouldDedupeSingularAndPluralHostFields() {

        config.setHosts(Arrays.asList("a.example.com","b.example.com"));
        config.setHost("a.example.com");

        List<ServerAddress> result = config.toServerAddresses();

        assertThat(result.get(0).getHost(),is("a.example.com"));
        assertThat(result.get(1).getHost(),is("b.example.com"));
        assertThat(result.size(),is(2));
    }

    @Test
    public void shouldUsePortFieldWithSingularHostField() {

        config.setHost("a.example.com");
        config.setPort(999);

        List<ServerAddress> result = config.toServerAddresses();

        assertThat(result.get(0).getPort(),is(999));
    }

    @Test
    public void shouldUsePortFieldWithPluralHostField() {

        config.setHosts(Arrays.asList("a.example.com","b.example.com"));
        config.setPort(999);

        List<ServerAddress> result = config.toServerAddresses();

        assertThat(result.get(0).getPort(),is(999));
        assertThat(result.get(1).getPort(),is(999));
    }

    @Test
    public void shouldParseAddresses() {
    	config.setAddresses(Arrays.asList("a.example.com:1234", "b.example.com:2345"));

        List<ServerAddress> result = config.toServerAddresses();

        assertThat(result.get(0).getHost(),is("a.example.com"));
        assertThat(result.get(0).getPort(),is(1234));
        assertThat(result.get(1).getHost(),is("b.example.com"));
        assertThat(result.get(1).getPort(),is(2345));
    }
}

