package com.github.forax.recordutil;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * An interface that provides an implementation for all methods of an unmodifiable {@link Map}
 * if the class that implements that interface is a {@link Record record}.
 *
 * Implementing this interface transform any records to a {@link Map}.
 * <pre>
 *   record Person(String name, int age) implements MapTrait { }
 *   ...
 *   Map&lt;String, Object&gt; map = new Person("Bob", 42);
 * </pre>
 *
 * Sadly, an interface can not override the method {@code equals], {@code hashCode}
 * or {@code toString} using a default method so to get an implementation
 * of {@link Map} compatible with {@link java.util.AbstractMap}, those methods has
 * to be overridden by hand.
 * <pre>
 *   record Person(String name, int age) implements MapTrait {
 *     @Override
 *     public boolean equals(Object o) {
 *       return MapTrait.super.equalsOfMap(o);
 *     }
 *
 *     @Override
 *     public int hashCode() {
 *       return MapTrait.super.hashCodeOfMap();
 *     }
 *
 *     @Override
 *     public String toString() {
 *       return MapTrait.super.toStringOfMap();
 *     }
 *   }
 * </pre>
 *
 * This implementation guarantee that the methods {@link #entrySet()}, {@link #keySet()}, {@link #keys()}
 * and {@link #values()} provide the keys and values in the record components order.
 *
 * That's why the method {@link #keys()} and {@link #values()} returns a {@link List} instead of returning
 * a {@link java.util.Collection}.
 */
public interface MapTrait extends java.util.Map<String, Object> {
  @Override
  default int size() {
    return TraitImpl.mapShape(getClass()).size();
  }
  @Override
  default boolean isEmpty() {
    return TraitImpl.mapShape(getClass()).isEmpty();
  }

  @Override
  default Object get(Object key) {
    return getOrDefault(key, null);
  }
  @Override
  default Object getOrDefault(Object key, Object defaultValue) {
    if (!(key instanceof String s)) {
      return defaultValue;
    }
    var shape = TraitImpl.mapShape(getClass());
    var getter = shape.getValue(s);
    if (getter == null) {
      return defaultValue;
    }
    return invokeValue(getter);
  }

  private Object invokeValue(MethodHandle getter) {
    try {
      return getter.invokeExact(this);
    } catch(RuntimeException | Error e) {
      throw e;
    } catch (Throwable t) {
      throw new UndeclaredThrowableException(t);
    }
  }

  @Override
  default boolean containsKey(Object key) {
    if (!(key instanceof String s)) {
      return false;
    }
    var shape = TraitImpl.mapShape(getClass());
    return shape.containsKey(s);
  }

  @Override
  default boolean containsValue(Object value) {
    var shape = TraitImpl.mapShape(getClass());
    for(var i = 0; i < shape.size(); i++) {
      var getter = shape.getValue(i);
      if (Objects.equals(invokeValue(getter), value)) {
        return true;
      }
    }
    return false;
  }

  /**
   * The default implementation of this method comes from {@code java.lang.Record}
   * thus does not obey to the general contract of {@link Map#equals(Object)}.
   *
   * In your record, you can change the implementation by adding
   * <pre>
   *   public boolean equals(Object o) {
   *     return MapTrait.super.equalsOfMap(o);
   *   }
   * </pre>
   * to get an implementation that behave like a {@link Map}.
   *
   * {@inheritDoc}
   *
   * @see #equalsOfMap(Object)
   */
  @Override
  boolean equals(Object o);

  /**
   * The default implementation of this method comes from {@code java.lang.Record}
   * thus does not obey to the general contract of {@link Map#hashCode()}.
   *
   * In your record, you can change the implementation by adding
   * <pre>
   *   public int hashCode() {
   *     return MapTrait.super.hashCodeOfMap();
   *   }
   * </pre>
   * to get an implementation that behave like a {@link Map}.
   *
   * {@inheritDoc}
   *
   * @see #hashCodeOfMap()
   */
  @Override
  int hashCode();

  /**
   * The default implementation of this method comes from {@code java.lang.Record}
   * thus does not obey to the general contract of {@link java.util.AbstractMap#toString()}.
   *
   * In your record, you can change the implementation by adding
   * <pre>
   *   public String toString() {
   *     return MapTrait.super.toStringOfMap();
   *   }
   * </pre>
   * to get an implementation that behave like a {@link Map}.
   *
   * {@inheritDoc}
   *
   * @see #toStringOfMap()
   */
  @Override
  String toString();

  /**
   * Compares the specified object with this map for equality.  Returns
   * {@code true} if the given object is also a map and the two maps
   * represent the same mappings.  More formally, two maps {@code m1} and
   * {@code m2} represent the same mappings if
   * {@code m1.entrySet().equals(m2.entrySet())}.  This ensures that the
   * {@code equals} method works properly across different implementations
   * of the {@code Map} interface.
   *
   * @param o object to be compared for equality with this map
   * @return {@code true} if the specified object is equal to this map
   *
   * @see #equals(Object)
   * @see Map#equals(Object)
   */
  default boolean equalsOfMap(Object o) {
    return o instanceof Map<?,?> map && equalsOfMap(map);
  }

  private boolean equalsOfMap(Map<?,?> map) {
    var shape = TraitImpl.mapShape(getClass());
    for(var i = 0; i < shape.size(); i++) {
      var value = map.get(shape.getKey(i));
      if (!Objects.equals(invokeValue(shape.getValue(i)), value)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns the hash code value for this map.  The hash code of a map is
   * defined to be the sum of the hash codes of each entry in the map's
   * {@code entrySet()} view.  This ensures that {@code m1.equals(m2)}
   * implies that {@code m1.hashCode()==m2.hashCode()} for any two maps
   * {@code m1} and {@code m2}, as required by the general contract of
   * {@link Object#hashCode}.
   *
   * @return the hash code value for this map
   *
   * @see #hashCode()
   * @see Map#hashCode()
   */
  default int hashCodeOfMap() {
    var shape = TraitImpl.mapShape(getClass());
    var h = 0;
    for (var i = 0; i < shape.size(); i++) {
      var value = invokeValue(shape.getValue(i));
      h += shape.getKey(i).hashCode() ^ Objects.hashCode(value);
    }
    return h;
  }

  /**
   * Returns a string representation of this map.  The string representation
   * consists of a list of key-value mappings in the order returned by the
   * map's {@code entrySet} view's iterator, enclosed in braces
   * ({@code "{}"}).  Adjacent mappings are separated by the characters
   * {@code ", "} (comma and space).  Each key-value mapping is rendered as
   * the key followed by an equals sign ({@code "="}) followed by the
   * associated value.  Keys and values are converted to strings as by
   * {@link String#valueOf(Object)}.
   *
   * @return a string representation of this map
   *
   * @see #toString()
   * @see Map#toString()
   */
  default String toStringOfMap() {
    var shape = TraitImpl.mapShape(getClass());
    var joiner = new StringJoiner(", ", "{", "}");
    for (var i = 0; i < shape.size(); i++) {
      joiner.add(shape.getKey(i) + "=" + invokeValue(shape.getValue(i)));
    }
    return joiner.toString();
  }

  @Override
  default void forEach(BiConsumer<? super String, ? super Object> action) {
    var shape = TraitImpl.mapShape(getClass());
    for (var i = 0; i < shape.size(); i++) {
      action.accept(shape.getKey(i), invokeValue(shape.getValue(i)));
    }
  }

  /**
   * Returns an unmodifiable {@link Set} view of the mappings contained in this map.
   *
   * @return a set view of the mappings contained in this map
   */
  @Override
  default Set<Entry<String, Object>> entrySet() {
    var shape = TraitImpl.mapShape(getClass());
    return new AbstractSet<>() {
      @Override
      public int size() {
        return shape.size();
      }

      @Override
      public Iterator<Entry<String, Object>> iterator() {
        return new Iterator<>() {
          private int index;

          @Override
          public boolean hasNext() {
            return index < shape.size();
          }

          @Override
          public Entry<String, Object> next() {
            if (!hasNext()) {
              throw new NoSuchElementException();
            }
            return Map.entry(shape.getKey(index), invokeValue(shape.getValue(index++)));
          }
        };
      }

      @Override
      public boolean contains(Object o) {
        if (!(o instanceof Map.Entry<?,?> e)) {
          return false;
        }
        return Objects.equals(get(e.getKey()), e.getValue());
      }
    };
  }

  /**
   * Returns an unmodifiable {@link Set} view of the keys contained in this map.
   *
   * @return a set view of the keys contained in this map
   */
  @Override
  default Set<String> keySet() {
    var shape =  TraitImpl.mapShape(getClass());
    return new AbstractSet<>() {
      @Override
      public int size() {
        return shape.size();
      }

      @Override
      public Iterator<String> iterator() {
        return new Iterator<>() {
          private int index;

          @Override
          public boolean hasNext() {
            return index < shape.size();
          }

          @Override
          public String next() {
            if (!hasNext()) {
              throw new NoSuchElementException();
            }
            return shape.getKey(index++);
          }
        };
      }

      @Override
      public boolean contains(Object o) {
        return containsKey(o);
      }
    };
  }

  /**
   * Returns an unmodifiable {@link List} view of the values contained in this map.
   *
   * @return an unmodifiable list of the values contained in this map
   */
  @Override
  default List<Object> values() {
    var shape =  TraitImpl.mapShape(getClass());
    return new AbstractList<>() {
      @Override
      public int size() {
        return shape.size();
      }

      @Override
      public Object get(int index) {
        Objects.checkIndex(index, shape.size());
        return invokeValue(shape.getValue(index));
      }
    };
  }

  /**
   * Returns an unmodifiable {@link List} view of the keys contained in this map.
   *
   * @return an unmodifiable list of the keys contained in this map
   */
  default List<String> keys() {
    var shape =  TraitImpl.mapShape(getClass());
    return new AbstractList<>() {
      @Override
      public int size() {
        return shape.size();
      }

      @Override
      public String get(int index) {
        Objects.checkIndex(index, shape.size());
        return shape.getKey(index);
      }

      @Override
      public boolean contains(Object o) {
        return containsKey(o);
      }
    };
  }

  @Override
  default void clear() {
    throw new UnsupportedOperationException();
  }
  @Override
  default Object put(String key, Object value) {
    throw new UnsupportedOperationException();
  }
  @Override
  default void putAll(Map<? extends String, ?> map) {
    throw new UnsupportedOperationException();
  }

  @Override
  default Object remove(Object key) {
    throw new UnsupportedOperationException();
  }
  @Override
  default boolean remove(Object key, Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  default Object replace(String key, Object value) {
    throw new UnsupportedOperationException();
  }
  @Override
  default boolean replace(String key, Object oldValue, Object newValue) {
    throw new UnsupportedOperationException();
  }
  @Override
  default void replaceAll(BiFunction<? super String, ? super Object, ?> function) {
    throw new UnsupportedOperationException();
  }

  @Override
  default Object compute(String key, BiFunction<? super String, ? super Object, ?> remappingFunction) {
    throw new UnsupportedOperationException();
  }
  @Override
  default Object computeIfAbsent(String key, Function<? super String, ?> mappingFunction) {
    throw new UnsupportedOperationException();
  }
  @Override
  default Object computeIfPresent(String key, BiFunction<? super String, ? super Object, ?> remappingFunction) {
    throw new UnsupportedOperationException();
  }
}
