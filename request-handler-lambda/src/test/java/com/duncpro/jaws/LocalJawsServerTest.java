package com.duncpro.jaws;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LocalJawsServerTest {
    @Test
    public void canParseEmptyQueryArgs() {
        final var args = LocalJawsServer.parseQueryParams("");
        assertEquals(0, args.size());
    }

    @Test
    public void canParseSingleQueryArg() {
        final var args = LocalJawsServer.parseQueryParams("hello=world");
        assertEquals(1, args.size());
        assertTrue(args.containsKey("hello"));
        assertEquals("world", args.get("hello"));
    }

    @Test
    public void canParseMultipleQueryArgs() {
        final var args = LocalJawsServer.parseQueryParams("hello=world&foo=bar");
        assertEquals(2, args.size());
        assertTrue(args.containsKey("hello"));
        assertEquals("world", args.get("hello"));
        assertTrue(args.containsKey("foo"));
        assertEquals("bar", args.get("foo"));
    }
}
