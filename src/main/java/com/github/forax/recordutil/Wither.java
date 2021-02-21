package com.github.forax.recordutil;

import java.lang.invoke.MethodHandles.Lookup;

import static java.util.Objects.requireNonNull;

/**
 * An efficient mechanism to create a record from an existing record object by updating several components.
 *
 * To be efficient, a {@code Wither} should be stored as a constant, by example as a static final field.
 * <pre>
 *   record Person(String name, int age) {}
 *   ...
 *   private static final Wither&lt;Person&gt; wither = Wither.of(MethodHandles.lookup(), Person.class);
 *   ...
 *   var bob = new Person("Bob", 42);
 *   var ana = wither.with(bob, "name", "Ana"); // create a Person with the name "Ana" and the same age as bob
 * </pre>
 *
 * <p>
 * The implementation first extract the "shape" of the call to {@code with} (the number of components
 * and the name of each component to update) and generates a specialized code that calls all the getters
 * of the components that are not updated in the order of the record declaration then
 * re-create a new record by calling the constructor with all the values.
 *
 * Two calls with the same "shape" will reuse the same code, if there are different shapes,
 * multiple specialized codes are generated and the JIT will only keep the right specialized code for a call.
 *
 * Given the fact that the implementation keeps all specialized codes that have been seen at least once,
 * one {code Wither} may store a lot of metadata to the point the JIT will give up to try to optimize the code.
 * This should not be the code with handwritten code but can be problematic with generated Java codes.
 *
 * @param <R> the type of the record to update.
 *
 * @see WithTrait
 */
@FunctionalInterface
public interface Wither<R> {
  /**
   * Returns a new record instance using the {@code nameCount} names and values to update the record instance
   * taken as first parameter.
   * This method should not be called directly, it's better to use one of the method {@code with} variants.
   *
   * @param record a record instance
   * @param nameCount the number of names that will used
   * @param name0 a name of a record component
   * @param value0 the value of the record component {@code name0}
   * @param name1 a name of a record component
   * @param value1 the value of the record component {@code name1}
   * @param name2 a name of a record component
   * @param value2 the value of the record component {@code name2}
   * @param name3 a name of a record component
   * @param value3 the value of the record component {@code name3}
   * @param name4 a name of a record component
   * @param value4 the value of the record component {@code name4}
   * @param name5 a name of a record component
   * @param value5 the value of the record component {@code name5}
   * @param name6 a name of a record component
   * @param value6 the value of the record component {@code name6}
   * @param name7 a name of a record component
   * @param value7 the value of the record component {@code name7}
   * @param name8 a name of a record component
   * @param value8 the value of the record component {@code name8}
   *
   * @throws NullPointerException if the record is null or one of the names is null
   * @throws IllegalArgumentException if {@code nameCount} is not between 1 and 9, if a name is not the name of one
   *         of the record component of the record, if a name is not a constant string or if two names are the same.
   * @throws ClassCastException if a value class is not compatible with the record component type
   *
   * @return a new record instance with the updated values
   * 
   * @see #with(Object, String, Object, String, Object, String, Object, String, Object, String, Object, String, Object, String, Object) 
   */
  R invoke(R record, int nameCount,
           String name0, Object value0, String name1, Object value1, String name2, Object value2,
           String name3, Object value3, String name4, Object value4, String name5, Object value5,
           String name6, Object value6, String name7, Object value7, String name8, Object value8);

  /**
   * Returns a new record instance with the record components named {@code name0}, {@code name1}, {@code name2},
   * {@code name3}, {@code name4}, {@code name5}, {@code name6}, {@code name7} and {@code name8} being updated to
   * the value {@code value0}, {@code value1}, {@code value2}, {@code value3}, {@code value4}, {@code value5},
   * {@code value6}, {@code value7} and {@code value8} respectively.
   *
   * @param record a record instance
   * @param name0 a name of a record component
   * @param value0 the value of the record component {@code name0}
   * @param name1 a name of a record component
   * @param value1 the value of the record component {@code name1}
   * @param name2 a name of a record component
   * @param value2 the value of the record component {@code name2}
   * @param name3 a name of a record component
   * @param value3 the value of the record component {@code name3}
   * @param name4 a name of a record component
   * @param value4 the value of the record component {@code name4}
   * @param name5 a name of a record component
   * @param value5 the value of the record component {@code name5}
   * @param name6 a name of a record component
   * @param value6 the value of the record component {@code name6}
   * @param name7 a name of a record component
   * @param value7 the value of the record component {@code name7}
   * @param name8 a name of a record component
   * @param value8 the value of the record component {@code name8}
   *
   * @throws NullPointerException if the record is null or one of the names is null
   * @throws IllegalArgumentException if a name is not the name of one of the record component of the record,
   *         if a name is not a constant string or if two names are the same.
   * @throws ClassCastException if a value class is not compatible with the record component type
   *
   * @return a new record instance with the updated values
   */
  default R with(R record, String name0, Object value0, String name1, Object value1, String name2, Object value2,
                 String name3, Object value3, String name4, Object value4, String name5, Object value5,
                 String name6, Object value6, String name7, Object value7, String name8, Object value8) {
    return invoke(record, 9, name0, value0, name1, value1, name2, value2, name3, value3, name4, value4, name5, value5, name6, value6, name7, value7, name8, value8);
  }

  /**
   * Returns a new record instance with the record components named {@code name0}, {@code name1}, {@code name2},
   * {@code name3}, {@code name4}, {@code name5}, {@code name6} and {@code name7} being updated to the value
   * {@code value0}, {@code value1}, {@code value2}, {@code value3}, {@code value4}, {@code value5}, {@code value6}
   * and {@code value7} respectively.
   *
   * @param record a record instance
   * @param name0 a name of a record component
   * @param value0 the value of the record component {@code name0}
   * @param name1 a name of a record component
   * @param value1 the value of the record component {@code name1}
   * @param name2 a name of a record component
   * @param value2 the value of the record component {@code name2}
   * @param name3 a name of a record component
   * @param value3 the value of the record component {@code name3}
   * @param name4 a name of a record component
   * @param value4 the value of the record component {@code name4}
   * @param name5 a name of a record component
   * @param value5 the value of the record component {@code name5}
   * @param name6 a name of a record component
   * @param value6 the value of the record component {@code name6}
   * @param name7 a name of a record component
   * @param value7 the value of the record component {@code name7}
   *
   * @throws NullPointerException if the record is null or one of the names is null
   * @throws IllegalArgumentException if a name is not the name of one of the record component of the record,
   *         if a name is not a constant string or if two names are the same.
   * @throws ClassCastException if a value class is not compatible with the record component type
   *
   * @return a new record instance with the updated values
   */
  default R with(R record, String name0, Object value0, String name1, Object value1, String name2, Object value2,
         String name3, Object value3, String name4, Object value4, String name5, Object value5,
         String name6, Object value6, String name7, Object value7) {
    return invoke(record, 8, name0, value0, name1, value1, name2, value2, name3, value3, name4, value4, name5, value5, name6, value6, name7, value7, null, null);
  }

  /**
   * Returns a new record instance with the record components named {@code name0}, {@code name1}, {@code name2},
   * {@code name3}, {@code name4}, {@code name5} and {@code name6} being updated to the value {@code value0},
   * {@code value1}, {@code value2}, {@code value3}, {@code value4}, {@code value5} and {@code value6} respectively.
   *
   * @param record a record instance
   * @param name0 a name of a record component
   * @param value0 the value of the record component {@code name0}
   * @param name1 a name of a record component
   * @param value1 the value of the record component {@code name1}
   * @param name2 a name of a record component
   * @param value2 the value of the record component {@code name2}
   * @param name3 a name of a record component
   * @param value3 the value of the record component {@code name3}
   * @param name4 a name of a record component
   * @param value4 the value of the record component {@code name4}
   * @param name5 a name of a record component
   * @param value5 the value of the record component {@code name5}
   * @param name6 a name of a record component
   * @param value6 the value of the record component {@code name6}
   *
   * @throws NullPointerException if the record is null or one of the names is null
   * @throws IllegalArgumentException if a name is not the name of one of the record component of the record,
   *         if a name is not a constant string or if two names are the same.
   * @throws ClassCastException if a value class is not compatible with the record component type
   *
   * @return a new record instance with the updated values
   */
  default R with(R record, String name0, Object value0, String name1, Object value1, String name2, Object value2,
                 String name3, Object value3, String name4, Object value4, String name5, Object value5,
                 String name6, Object value6) {
    return invoke(record, 7, name0, value0, name1, value1, name2, value2, name3, value3, name4, value4, name5, value5, name6, value6, null, null, null, null);
  }

  /**
   * Returns a new record instance with the record components named {@code name0}, {@code name1}, {@code name2},
   * {@code name3}, {@code name4} and {@code name5} being updated to the value {@code value0}, {@code value1},
   * {@code value2}, {@code value3}, {@code value4} and {@code value5} respectively.
   *
   * @param record a record instance
   * @param name0 a name of a record component
   * @param value0 the value of the record component {@code name0}
   * @param name1 a name of a record component
   * @param value1 the value of the record component {@code name1}
   * @param name2 a name of a record component
   * @param value2 the value of the record component {@code name2}
   * @param name3 a name of a record component
   * @param value3 the value of the record component {@code name3}
   * @param name4 a name of a record component
   * @param value4 the value of the record component {@code name4}
   * @param name5 a name of a record component
   * @param value5 the value of the record component {@code name5}
   *
   * @throws NullPointerException if the record is null or one of the names is null
   * @throws IllegalArgumentException if a name is not the name of one of the record component of the record,
   *         if a name is not a constant string or if two names are the same.
   * @throws ClassCastException if a value class is not compatible with the record component type
   *
   * @return a new record instance with the updated values
   */
  default R with(R record, String name0, Object value0, String name1, Object value1, String name2, Object value2,
                 String name3, Object value3, String name4, Object value4, String name5, Object value5) {
    return invoke(record, 6, name0, value0, name1, value1, name2, value2, name3, value3, name4, value4, name5, value5, null, null, null, null, null, null);
  }

  /**
   * Returns a new record instance with the record components named {@code name0}, {@code name1}, {@code name2},
   * {@code name3} and {@code name4} being updated to the value {@code value0}, {@code value1}, {@code value2},
   * {@code value3} and {@code value4} respectively.
   *
   * @param record a record instance
   * @param name0 a name of a record component
   * @param value0 the value of the record component {@code name0}
   * @param name1 a name of a record component
   * @param value1 the value of the record component {@code name1}
   * @param name2 a name of a record component
   * @param value2 the value of the record component {@code name2}
   * @param name3 a name of a record component
   * @param value3 the value of the record component {@code name3}
   * @param name4 a name of a record component
   * @param value4 the value of the record component {@code name4}
   *
   * @throws NullPointerException if the record is null or one of the names is null
   * @throws IllegalArgumentException if a name is not the name of one of the record component of the record,
   *         if a name is not a constant string or if two names are the same.
   * @throws ClassCastException if a value class is not compatible with the record component type
   *
   * @return a new record instance with the updated values
   */
  default R with(R record, String name0, Object value0, String name1, Object value1, String name2, Object value2,
                 String name3, Object value3, String name4, Object value4) {
    return invoke(record, 5, name0, value0, name1, value1, name2, value2, name3, value3, name4, value4, null, null, null, null, null, null, null, null);
  }

  /**
   * Returns a new record instance with the record components named {@code name0}, {@code name1}, {@code name2} and
   * {@code name3} being updated to the value {@code value0}, {@code value1}, {@code value2} and
   * {@code value3} respectively.
   *
   * @param record a record instance
   * @param name0 a name of a record component
   * @param value0 the value of the record component {@code name0}
   * @param name1 a name of a record component
   * @param value1 the value of the record component {@code name1}
   * @param name2 a name of a record component
   * @param value2 the value of the record component {@code name2}
   * @param name3 a name of a record component
   * @param value3 the value of the record component {@code name3}
   *
   * @throws NullPointerException if the record is null or one of the names is null
   * @throws IllegalArgumentException if a name is not the name of one of the record component of the record,
   *         if a name is not a constant string or if two names are the same.
   * @throws ClassCastException if a value class is not compatible with the record component type
   *
   * @return a new record instance with the updated values
   */
  default R with(R record, String name0, Object value0, String name1, Object value1, String name2, Object value2,
                 String name3, Object value3) {
    return invoke(record, 4, name0, value0, name1, value1, name2, value2, name3, value3, null, null, null, null, null, null, null, null, null, null);
  }

  /**
   * Returns a new record instance with the record components named {@code name0}, {@code name1} and {@code name2}
   * being updated to the value {@code value0}, {@code value1} and {@code value2} respectively.
   *
   * @param record a record instance
   * @param name0 a name of a record component
   * @param value0 the value of the record component {@code name0}
   * @param name1 a name of a record component
   * @param value1 the value of the record component {@code name1}
   * @param name2 a name of a record component
   * @param value2 the value of the record component {@code name2}
   *
   * @throws NullPointerException if the record is null or one of the names is null
   * @throws IllegalArgumentException if a name is not the name of one of the record component of the record,
   *         if a name is not a constant string or if two names are the same.
   * @throws ClassCastException if a value class is not compatible with the record component type
   *
   * @return a new record instance with the updated values
   */
  default R with(R record, String name0, Object value0, String name1, Object value1, String name2, Object value2) {
    return invoke(record, 3, name0, value0, name1, value1, name2, value2, null, null, null, null, null, null, null, null, null, null, null, null);
  }

  /**
   * Returns a new record instance with the record components named {@code name0} and {@code name1}
   * being updated to the value {@code value0} and {@code value1} respectively.
   *
   * @param record a record instance
   * @param name0 a name of a record component
   * @param value0 the value of the record component {@code name0}
   * @param name1 a name of a record component
   * @param value1 the value of the record component {@code name1}
   *
   * @throws NullPointerException if the record is null or one of the names is null
   * @throws IllegalArgumentException if a name is not the name of one of the record component of the record,
   *         if a name is not a constant string or if two names are the same.
   * @throws ClassCastException if a value class is not compatible with the record component type
   *
   * @return a new record instance with the updated values
   */
  default R with(R record, String name0, Object value0, String name1, Object value1) {
    return invoke(record, 2, name0, value0, name1, value1, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
  }

  /**
   * Returns a new record instance with the record component named {@code name0} being updated to the value
   * {@code value0}.
   *
   * @param record a record instance
   * @param name0 a name of a record component
   * @param value0 the value of the record component {@code name0}
   *
   * @throws NullPointerException if the record is null or the name is null
   * @throws IllegalArgumentException if the {@code name0} is not the name of one of the record component of the record
   *         or if the name is not a constant string.
   * @throws ClassCastException if the value class is not compatible with the record component type
   *
   * @return a new record instance with the updated values
   */
  default R with(R record, String name0, Object value0) {
    return invoke(record, 1, name0, value0, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
  }

  /**
   * Create a {@code Wither} from a lookup and a record class.
   *
   * @param lookup a lookup that should see the record constructor and accessors
   * @param recordType the class of the record instances that will be updated
   * @param <R> the type of the record
   * @return a new {@code Wither}
   *
   * @throws NullPointerException if {@code lookup} or {@code recordType} is null
   * @throws IllegalAccessError if the record constructor is not accessible from the {@code lookup}
   */
  static <R extends Record> Wither<R> of(Lookup lookup, Class<? extends R> recordType) {
    requireNonNull(lookup);
    requireNonNull(recordType);
    var target = WitherImpl.createMH(lookup, recordType);
    return (record, nameCount, name0, value0, name1, value1, name2, value2, name3, value3, name4, value4, name5, value5, name6, value6, name7, value7, name8, value8) -> {
      Object newRecord;
      try {
        newRecord = target.invokeExact((Object) record, nameCount, name0, value0, name1, value1, name2, value2, name3, value3, name4, value4, name5, value5, name6, value6, name7, value7, name8, value8);
      } catch(RuntimeException | Error e) {
        throw e;
      } catch(Throwable t) {
        throw new AssertionError(t);
      }
      return recordType.cast(newRecord);
    };
  }
}
