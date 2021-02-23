package com.github.forax.recordutil;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collection;
import java.util.Map;

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
    if (o instanceof Map<?,?> map) {
      toJSONObject(builder, map, linePrefix, lineIndent, lineSeparator);
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

  private static void toJSONObject(StringBuilder builder, Map<?,?> map, String linePrefix, String lineIndent, String lineSeparator) {
    builder.append('{');
    var separator = "";
    var innerLinePrefix = linePrefix + lineIndent;
    for(var entry: map.entrySet()) {
      var key = entry.getKey();
      var value = entry.getValue();
      builder.append(separator)
          .append(lineSeparator).append(innerLinePrefix)
          .append('"').append(key).append("\": ");
      toJSON(builder, value, innerLinePrefix, lineIndent, lineSeparator);
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
}
