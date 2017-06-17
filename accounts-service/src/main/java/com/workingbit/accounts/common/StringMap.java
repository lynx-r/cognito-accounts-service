package com.workingbit.accounts.common;

import java.util.*;

/**
 * Created by Aleksey Popryaduhin on 22:49 11/06/2017.
 */
public class StringMap extends AbstractMap<String, Object> {

  private Map<String, Object> map;

  public StringMap() {
    map = new HashMap<>();
  }

  private StringMap(Map<String, Object> map) {
    this.map = map;
  }

  @Override
  public int size() {
    return map.size();
  }

  @Override
  public boolean isEmpty() {
    return map.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return map.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return map.containsValue(value);
  }

  @Override
  public Object get(Object key) {
    return map.get(key);
  }

  public <T> T get(Object key, Class<T> clazz) {
    return clazz.cast(get(key));
  }

  public String getString(Object key) {
    return get(key, String.class);
  }

  @Override
  public Object put(String key, Object value) {
    return map.put(key, value);
  }

  @Override
  public Object remove(Object key) {
    return map.remove(key);
  }

  @Override
  public void putAll(Map<? extends String, ?> m) {
    map.putAll(m);
  }

  @Override
  public void clear() {
    map.clear();
  }

  @Override
  public Set<String> keySet() {
    return map.keySet();
  }

  @Override
  public Collection<Object> values() {
    return map.values();
  }

  @Override
  public Set<Entry<String, Object>> entrySet() {
    return map.entrySet();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    StringMap stringMap = (StringMap) o;
    return Objects.equals(map, stringMap.map);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), map);
  }

  public static StringMap emptyMap() {
    return new StringMap();
  }

  public static StringMap fromMap(Map<String, Object> m) {
    return new StringMap(m);
  }
}
