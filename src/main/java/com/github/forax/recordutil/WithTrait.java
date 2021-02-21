package com.github.forax.recordutil;

import com.github.forax.recordutil.TraitImpl.WithShape;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 *
 * @param <R> type of the record
 *
 * @see Wither
 */
public interface WithTrait<R> {
  private Object[] initArray(WithShape shape) {
    var array = new Object[shape.size()];
    try {
      for (var i = 0; i < shape.size(); i++) {
        array[i] = shape.getValue(i).invokeExact(this);
      }
      return array;
    } catch (RuntimeException | Error e) {
      throw e;
    } catch (Throwable throwable) {
      throw new UndeclaredThrowableException(throwable);
    }
  }

  private static Object invokeArray(WithShape shape, Object[] array) {
    try {
      return shape.constructor().invokeExact(array);
    } catch (RuntimeException | Error e) {
      throw e;
    } catch (Throwable throwable) {
      throw new UndeclaredThrowableException(throwable);
    }
  }

  private static int slot(WithShape shape, String key) {
    var slot = shape.getSlot(key);
    if (slot == -1) {
      throw new IllegalStateException("record component " + key + "not found");
    }
    return slot;
  }

  @SuppressWarnings("unchecked")
  default R with(String key, Object value) {
    requireNonNull(key, "key is null");
    var shape = TraitImpl.withShape(getClass());
    var array = initArray(shape);
    array[slot(shape, key)] = value;
    return (R) invokeArray(shape, array);
  }

  @SuppressWarnings("unchecked")
  default R with(String key1, Object value1, String key2, Object value2) {
    requireNonNull(key1, "key1 is null");
    requireNonNull(key2, "key2 is null");
    var shape = TraitImpl.withShape(getClass());
    var array = initArray(shape);
    array[slot(shape, key1)] = value1;
    array[slot(shape, key2)] = value2;
    return (R) invokeArray(shape, array);
  }

  @SuppressWarnings("unchecked")
  default R with(String key1, Object value1, String key2, Object value2, String key3, Object value3) {
    requireNonNull(key1, "key1 is null");
    requireNonNull(key2, "key2 is null");
    requireNonNull(key3, "key3 is null");
    var shape = TraitImpl.withShape(getClass());
    var array = initArray(shape);
    array[slot(shape, key1)] = value1;
    array[slot(shape, key2)] = value2;
    array[slot(shape, key3)] = value3;
    return (R) invokeArray(shape, array);
  }

  @SuppressWarnings("unchecked")
  default R with(String key1, Object value1, String key2, Object value2, String key3, Object value3, String key4, Object value4) {
    requireNonNull(key1, "key1 is null");
    requireNonNull(key2, "key2 is null");
    requireNonNull(key3, "key3 is null");
    requireNonNull(key4, "key4 is null");
    var shape = TraitImpl.withShape(getClass());
    var array = initArray(shape);
    array[slot(shape, key1)] = value1;
    array[slot(shape, key2)] = value2;
    array[slot(shape, key3)] = value3;
    array[slot(shape, key4)] = value4;
    return (R) invokeArray(shape, array);
  }

  @SuppressWarnings("unchecked")
  default R with(Object... pairs) {
    if ((pairs.length & 1) != 0) {
      throw new IllegalArgumentException("invalid arguments, it should be pairs of key, value");
    }
    var shape = TraitImpl.withShape(getClass());
    var array = initArray(shape);
    for(var i = 0; i < pairs.length; i += 2) {
      var key = Objects.requireNonNull(pairs[i], "key " + i + " is null");
      if (!(key instanceof String s)) {
        throw new IllegalArgumentException("key " + i + " is not a String: " + key);
      }
      var value = pairs[i + 1];
      array[slot(shape, s)] = value;
    }
    return (R) invokeArray(shape, array);
  }
}
