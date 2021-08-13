package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;

public class MainModule extends AbstractModule {
    @Override
    public void configure() {
        bind(ObjectMapper.class).toInstance(new ObjectMapper());
    }
}
