package org.geektimes.configuration.microprofile.config.source;

import org.eclipse.microprofile.config.spi.ConfigSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class MapBasedConfigSource implements ConfigSource {

    private final String name;

    private final int ordinal;

    private final Map<String,String> source;

    public MapBasedConfigSource(String name, int ordinal) {
        this.name = name;
        this.ordinal = ordinal;
        this.source = getProperties();
    }

    @Override
    public Map<String, String> getProperties() {
        Map<String, String> map = new HashMap<>();
        try{
             prepareConfigData(map);
        }catch (Throwable e){
            throw new IllegalStateException("Problem with config data", e);
        }
        return Collections.unmodifiableMap(map);
    }


    protected abstract void prepareConfigData(Map configData) throws Throwable;

    @Override
    public int getOrdinal() {
        return ordinal;
    }

    @Override
    public Set<String> getPropertyNames() {
        return source.keySet();
    }

    @Override
    public String getValue(String propertyName) {
        return source.get(propertyName);
    }

    @Override
    public String getName() {
        return name;
    }
}
