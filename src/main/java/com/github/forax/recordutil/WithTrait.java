package com.github.forax.recordutil;

import com.github.forax.recordutil.TraitImpl.WithShape;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * An interface that provides several methods {@code with} that create a new record
 * from an existing record instance and a list of the record component names and values
 * that need to be updated
 *
 * Adding this interface add several methods {@code with} to any records.
 * <pre>
 *   record Person(String name, int age) implements WithTrait&lt;Person&gt; {}
 *   ...
 *   Person bob = new Person("Bob", 42);
 *   Person ana = bob.with("name", "Ana");
 * </pre>
 *
 * <p>
 * All methods may throw an error {@link IllegalAccessError} if the record is declared
 * in a package in a module which does not open the package to the module
 * {@code com.github.forax.recordutil}.
 * By example, if the record is declared in a module mymodule in a package mypackage,
 * the module-info of this module should contains the following declaration
 * <pre>
 *   module mymodule {
 *     ...
 *     open mypackage to com.github.forax.recordutil;
 *   }
 * </pre>
 *
 * <p>
 * This implementation is not very efficient, use {@link Wither} for a more cumbersome
 * but more performant implementation.
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
        array[i] = shape.getValue(i).invokeExact((Object) this);
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

  private static int slot(WithShape shape, String name) {
    var slot = shape.getSlot(name);
    if (slot == -1) {
      throw new IllegalStateException("record component " + name + "not found");
    }
    return slot;
  }

  /**
   * Returns a new record instance with the record component named {@code name} updated
   * to the value {@code value}.
   *
   * @param name a record component name
   * @param value the new value of the record component {@code name}
   * @return a new record instance with the record component value updated
   *
   * @throws NullPointerException if {@code name} is null
   * @throws ClassCastException if the value has not a class compatible with the record component type
   */
  @SuppressWarnings("unchecked")
  default R with(String name, Object value) {
    requireNonNull(name, "name is null");
    var shape = TraitImpl.withShape(getClass());
    var array = initArray(shape);
    array[slot(shape, name)] = value;
    return (R) invokeArray(shape, array);
  }

  /**
   * Returns a new record instance with the record components named {@code name1}
   * and {@code name2} respectively updated to the value {@code value1} and {@code value2}.
   *
   * @param name1 a record component name
   * @param value1 the new value of the record component {@code name1}
   * @param name2 a record component name
   * @param value2 the new value of the record component {@code name2}
   * @return a new record instance with the record component values updated
   *
   * @throws NullPointerException if {@code name1} or {@code name2} is null
   * @throws ClassCastException if one value has not a class compatible with its corresponding record component type
   */
  @SuppressWarnings("unchecked")
  default R with(String name1, Object value1, String name2, Object value2) {
    requireNonNull(name1, "name1 is null");
    requireNonNull(name2, "name2 is null");
    var shape = TraitImpl.withShape(getClass());
    var array = initArray(shape);
    array[slot(shape, name1)] = value1;
    array[slot(shape, name2)] = value2;
    return (R) invokeArray(shape, array);
  }

  /**
   * Returns a new record instance with the record components named {@code name1}
   * {@code name2} and {@code name3} respectively updated to the value {@code value1}, {@code value2}
   * and {@code value3}.
   *
   * @param name1 a record component name
   * @param value1 the new value of the record component {@code name1}
   * @param name2 a record component name
   * @param value2 the new value of the record component {@code name2}
   * @param name3 a record component name
   * @param value3 the new value of the record component {@code name3}
   * @return a new record instance with the record component values updated
   *
   * @throws NullPointerException if {@code name1}, {@code name2} or {@code name3} is null
   * @throws ClassCastException if one value has not a class compatible with its corresponding record component type
   */
  @SuppressWarnings("unchecked")
  default R with(String name1, Object value1, String name2, Object value2, String name3, Object value3) {
    requireNonNull(name1, "name1 is null");
    requireNonNull(name2, "name2 is null");
    requireNonNull(name3, "name3 is null");
    var shape = TraitImpl.withShape(getClass());
    var array = initArray(shape);
    array[slot(shape, name1)] = value1;
    array[slot(shape, name2)] = value2;
    array[slot(shape, name3)] = value3;
    return (R) invokeArray(shape, array);
  }

  /**
   * Returns a new record instance with the record components named {@code name1}
   * {@code name2}, {@code name3} and {@code name4} respectively updated to the value {@code value1},
   * {@code value2}, {@code value3} and {@code value4}.
   *
   * @param name1 a record component name
   * @param value1 the new value of the record component {@code name1}
   * @param name2 a record component name
   * @param value2 the new value of the record component {@code name2}
   * @param name3 a record component name
   * @param value3 the new value of the record component {@code name3}
   * @param name4 a record component name
   * @param value4 the new value of the record component {@code name4}
   * @return a new record instance with the record component values updated
   *
   * @throws NullPointerException if {@code name1}, {@code name2}, {@code name3}  or {@code name4} is null
   * @throws ClassCastException if one value has not a class compatible with its corresponding record component type.
   */
  @SuppressWarnings("unchecked")
  default R with(String name1, Object value1, String name2, Object value2, String name3, Object value3, String name4, Object value4) {
    requireNonNull(name1, "name1 is null");
    requireNonNull(name2, "name2 is null");
    requireNonNull(name3, "name3 is null");
    requireNonNull(name4, "name4 is null");
    var shape = TraitImpl.withShape(getClass());
    var array = initArray(shape);
    array[slot(shape, name1)] = value1;
    array[slot(shape, name2)] = value2;
    array[slot(shape, name3)] = value3;
    array[slot(shape, name4)] = value4;
    return (R) invokeArray(shape, array);
  }

  /**
   * Returns a new record instance with the record components whose names are .
   *
   * @param pairs an array of pair of record name/new value
   * @return a new record instance with the record component values updated
   *
   * @throws NullPointerException if one of the name of the pairs is null
   * @throws IllegalArgumentException is the pairs array length is odd or one name of the pairs is not a String
   * @throws ClassCastException if one value has not a class compatible with its corresponding record component type.
   */
  @SuppressWarnings("unchecked")
  default R with(Object... pairs) {
    if ((pairs.length & 1) != 0) {
      throw new IllegalArgumentException("invalid arguments, it should be pairs of name, value");
    }
    var shape = TraitImpl.withShape(getClass());
    var array = initArray(shape);
    for(var i = 0; i < pairs.length; i += 2) {
      var name = Objects.requireNonNull(pairs[i], "name " + i + " is null");
      if (!(name instanceof String key)) {
        throw new IllegalArgumentException("name " + i + " is not a String: " + name);
      }
      var value = pairs[i + 1];
      array[slot(shape, key)] = value;
    }
    return (R) invokeArray(shape, array);
  }
}
