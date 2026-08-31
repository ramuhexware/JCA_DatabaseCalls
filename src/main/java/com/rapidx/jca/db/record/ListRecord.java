package com.rapidx.jca.db.record;

import jakarta.resource.cci.IndexedRecord;

import java.util.*;

/**
 * JCA CCI IndexedRecord implementation representing lists of database records (e.g. ResultSet rows).
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class ListRecord implements IndexedRecord {
    private String recordName;
    private String recordShortDescription;
    private final List listData = new ArrayList();

    public ListRecord() {
        this("ListRecord", "JCA Database Indexed Record List");
    }

    public ListRecord(String recordName, String recordShortDescription) {
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
        return listData.size();
    }

    @Override
    public boolean isEmpty() {
        return listData.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return listData.contains(o);
    }

    @Override
    public Iterator iterator() {
        return listData.iterator();
    }

    @Override
    public Object[] toArray() {
        return listData.toArray();
    }

    @Override
    public Object[] toArray(Object[] a) {
        return listData.toArray(a);
    }

    @Override
    public boolean add(Object e) {
        return listData.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return listData.remove(o);
    }

    @Override
    public boolean containsAll(Collection c) {
        return listData.containsAll(c);
    }

    @Override
    public boolean addAll(Collection c) {
        return listData.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection c) {
        return listData.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection c) {
        return listData.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection c) {
        return listData.retainAll(c);
    }

    @Override
    public void clear() {
        listData.clear();
    }

    @Override
    public Object get(int index) {
        return listData.get(index);
    }

    @Override
    public Object set(int index, Object element) {
        return listData.set(index, element);
    }

    @Override
    public void add(int index, Object element) {
        listData.add(index, element);
    }

    @Override
    public Object remove(int index) {
        return listData.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return listData.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return listData.lastIndexOf(o);
    }

    @Override
    public ListIterator listIterator() {
        return listData.listIterator();
    }

    @Override
    public ListIterator listIterator(int index) {
        return listData.listIterator(index);
    }

    @Override
    public List subList(int fromIndex, int toIndex) {
        return listData.subList(fromIndex, toIndex);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        ListRecord copy = new ListRecord(this.recordName, this.recordShortDescription);
        copy.addAll(this.listData);
        return copy;
    }

    @Override
    public String toString() {
        return "ListRecord{" +
                "name='" + recordName + '\'' +
                ", size=" + listData.size() +
                ", records=" + listData +
                '}';
    }
}
