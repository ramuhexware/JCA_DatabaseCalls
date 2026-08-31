package com.rapidx.jca.db.record;

import jakarta.resource.cci.MappedRecord;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * JCA CCI MappedRecord implementation representing key-value database records.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class MapRecord implements MappedRecord {
    private String recordName;
    private String recordShortDescription;
    private final Map mapData = new HashMap();

    public MapRecord() {
        this("MapRecord", "JCA Database Mapped Record");
    }

    public MapRecord(String recordName, String recordShortDescription) {
        this.recordName = recordName;
        this.recordShortDescription = recordShortDescription;
    }

    @Override
    public String getRecordName() {
        return recordName;
    }

    @Override
    public void setRecordName(String name) {
        this.recordName = name;
    }

    @Override
    public String getRecordShortDescription() {
        return recordShortDescription;
    }

    @Override
    public void setRecordShortDescription(String description) {
        this.recordShortDescription = description;
    }

    @Override
    public int size() {
        return mapData.size();
    }

    @Override
    public boolean isEmpty() {
        return mapData.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return mapData.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return mapData.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return mapData.get(key);
    }

    @Override
    public Object put(Object key, Object value) {
        return mapData.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return mapData.remove(key);
    }

    @Override
    public void putAll(Map m) {
        mapData.putAll(m);
    }

    @Override
    public void clear() {
        mapData.clear();
    }

    @Override
    public Set keySet() {
        return mapData.keySet();
    }

    @Override
    public Collection values() {
        return mapData.values();
    }

    @Override
    public Set entrySet() {
        return mapData.entrySet();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        MapRecord copy = new MapRecord(this.recordName, this.recordShortDescription);
        copy.putAll(this.mapData);
        return copy;
    }

    @Override
    public String toString() {
        return "MapRecord{" +
                "name='" + recordName + '\'' +
                ", data=" + mapData +
                '}';
    }
}
