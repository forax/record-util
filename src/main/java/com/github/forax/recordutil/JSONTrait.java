package com.github.forax.recordutil;

import java.io.IOException;
import java.io.Reader;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collection;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * An interface that provides a method {@link #toJSON()} for any {@link Record record} that
 * implements this interface.
 *
 * Adding this interface add two methods {@link #toJSON()} and {@link #toHumanReadableJSON(String, String)}
 * <pre>
 *   record Person(String name, int age) implements JSONTrait { }
 *   ...
 *   var person = new Person("Bob", 42);
 *   System.out.println(person.toJSON());
 * </pre>
 *
 * Moreover, JSONTrait defines a method {@link #parse(Reader, Class)} to decode a JSON file
 * to a record instance. This feature is implemented using Jackson but the dependency is not enabled by default
 * so to use it you have to add a `requires com.fasterxml.jackson.core;` into your module-info
 * and also add the dependency to `com.fasterxml.jackson.core:jackson-core` in your POM file
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
 */
public interface JSONTrait {
  /**
   * Returns the current record instance formatted using the JSON format
   * @return the current record instance formatted using the JSON format
   */
  default String toJSON() {
    var builder = new StringBuilder();
    toJSONRecord(builder, this, "", "", "");
    return builder.toString();
  }

  /**
   * Returns a human readable text using the JSON format of the current record,
   * using \n as line separator and "  " as line indent.
   *
   * This is semantically equivalent to call
   * {@code toHumanReadableJSON("  ", "\n")}
   *
   * @return a human readable text using the JSON format of the current record
   * @see #toHumanReadableJSON(String, String)
   */
  default String toHumanReadableJSON() {
    return toHumanReadableJSON("  ", "\n");
  }

  /**
   * Returns a human readable text using the JSON format of the current record
   * @param lineIndent number of spaces to increment when entering a JSON Object or a JSON Array
   * @param lineSeparator the line separator (e.g. "\n" or "\r\n")
   * @return a human readable text using the JSON format of the current record
   */
  default String toHumanReadableJSON(String lineIndent, String lineSeparator) {
    var builder = new StringBuilder();
    toJSONRecord(builder, this, "", lineIndent, lineSeparator);
    return builder.toString();
  }

  private static Object invokeValue(Object object, MethodHandle getter) {
    try {
      return getter.invokeExact(object);
    } catch(RuntimeException | Error e) {
      throw e;
    } catch (Throwable t) {
      throw new UndeclaredThrowableException(t);
    }
  }

  private static void toJSON(StringBuilder builder, Object o, String linePrefix, String lineIndent, String lineSeparator) {
    if (o instanceof Record record) {
      toJSONRecord(builder, record, linePrefix, lineIndent, lineSeparator);
      return;
    }
    if (o instanceof Collection<?> collection) {
      toJSONArray(builder, collection, linePrefix, lineIndent, lineSeparator);
      return;
    }
    toJSONPrimitive(builder, o);
  }

  private static void toJSONRecord(StringBuilder builder, Object record, String linePrefix, String lineIndent, String lineSeparator) {
    var shape = TraitImpl.mapShape(record.getClass());
    builder.append('{');
    var separator = "";
    var innerLinePrefix = linePrefix + lineIndent;
    for(var i = 0; i < shape.size(); i++) {
      var key = shape.getKey(i);
      var value = shape.getValue(i);
      builder.append(separator)
          .append(lineSeparator).append(innerLinePrefix)
          .append('"').append(key).append("\": ");
      toJSON(builder, invokeValue(record, value), innerLinePrefix, lineIndent, lineSeparator);
      separator = lineSeparator.isEmpty()? ", ": ",";
    }
    builder.append(lineSeparator).append(linePrefix).append('}');
  }

  private static void toJSONArray(StringBuilder builder, Collection<?> collection, String linePrefix, String lineIndent, String lineSeparator) {
    builder.append('[');
    var separator = "";
    var innerLinePrefix = linePrefix + lineIndent;
    for(var value: collection) {
      builder.append(separator).append(lineSeparator).append(innerLinePrefix);
      toJSON(builder, value, innerLinePrefix, lineIndent, lineSeparator);
      separator = lineSeparator.isEmpty()? ", ": ",";
    }
    builder.append(lineSeparator).append(linePrefix).append(']');
  }

  private static void toJSONPrimitive(StringBuilder builder, Object o) {
    if (o == null || o instanceof Number || o instanceof Boolean) {
      builder.append(o);
      return;
    }
    builder.append('"')
        .append(o.toString().replace("\"", "\\\""))
        .append('"');
  }

  /**
   * User defined callback to convert a JSON value typed as a {@code String} to a Java value
   * of a specific {@code Class}.
   *
   * By example, to parse dates using {@link java.time.LocalDate}, one can write
   * <pre>
   *   Converter converter = (valueAsString, type, downstreamConverter) -> {
   *       if (type == LocalDate.class) {
   *         return LocalDate.parse(valueAsString);
   *       }
   *       return downstreamConverter.convert(valueAsString, type);
   *     };
   * </pre>
   * 
   * @see #parse(Reader, Class, Converter)
   */
  @FunctionalInterface
  interface Converter {
    /**
     * Default implementation of a converter that knows how to convert Java primitive types.
     */
    interface DownStream {
      /**
       * Convert a JSON value encoded as a String to an object of peculiar Java class.
       *
       * @param valueAsString  JSON value
       * @param type a Java class
       * @return a value of class {@code Class}
       * @throws IOException if the conversion is not possible
       */
      Object convert(String valueAsString, Class<?> type) throws IOException;
    }

    /**
     * Convert a JSON value encoded as a String to an object of peculiar Java class.
     *
     * @param valueAsString a JSON value
     * @param type a Java class
     * @param downstreamConverter an already defined converted that implement the default conversions
     * @return a value of class {@code Class}
     * @throws IOException if the conversion is not possible
     *
     * @see DownStream
     */
    Object convert(String valueAsString, Class<?> type, DownStream downstreamConverter) throws IOException;
  }

  private static Converter defaultConverter() {
    return (valueAsString, type, downstreamConverter) -> downstreamConverter.convert(valueAsString, type);
  }

  /**
   * Parse a JSON Object using a record class to guide the decoding.
   *
   * @param reader the reader containing the JSON
   * @param recordType the type of the record to decode
   * @param <R> the type of the record
   * @return a newly allocated record
   * @throws IOException if either an i/o error or a parsing error occur
   *
   * @see #parse(Reader, Class, Converter)
   */
  static <R extends Record> R parse(Reader reader, Class<? extends R> recordType) throws IOException {
    return parse(reader, recordType, defaultConverter());
  }

  /**
   * Parse a JSON Object using a record class to guide the decoding.
   *
   * @param reader the reader containing the JSON
   * @param recordType the type of the record to decode
   * @param converter a user defined converter to handle user specific conversions
   * @param <R> the type of the record
   * @return a newly allocated record
   * @throws IOException if either an i/o error or a parsing error occur
   *
   * @see #parse(Reader, Class)
   */
  static <R extends Record> R parse(Reader reader, Class<? extends R> recordType, Converter converter) throws IOException {
    requireNonNull(reader, "reader is null");
    requireNonNull(recordType, "recordType is null");
    requireNonNull(converter, "converter is null");
    return recordType.cast(JSONParsing.parse(reader, recordType, converter));
  }

  /**
   * Returns a Stream from a JSON Array of Objects using a record class to guide the decoding.
   *
   * @param reader the reader containing the JSON
   * @param recordType the type of the record to decode
   * @param <R> the type of the record
   * @return a Stream of records
   * @throws java.io.UncheckedIOException if either an i/o error or a parsing error occur
   *
   * @see #stream(Reader, Class, Converter)
   */
  static <R extends Record> Stream<R> stream(Reader reader, Class<? extends R> recordType) {
    return JSONParsing.stream(reader, recordType, defaultConverter());
  }

  /**
   * Returns a Stream from a JSON Array of Objects using a record class to guide the decoding.
   *
   * @param reader the reader containing the JSON
   * @param recordType the type of the record to decode
   * @param converter a user defined converter to handle user specific conversions
   * @param <R> the type of the record
   * @return a Stream of records
   * @throws java.io.UncheckedIOException if either an i/o error or a parsing error occur
   *
   * @see #stream(Reader, Class)
   */
  static <R extends Record> Stream<R> stream(Reader reader, Class<? extends R> recordType, Converter converter) {
    requireNonNull(reader, "reader is null");
    requireNonNull(recordType, "recordType is null");
    requireNonNull(converter, "converter is null");
    return JSONParsing.stream(reader, recordType, converter);
  }
}
