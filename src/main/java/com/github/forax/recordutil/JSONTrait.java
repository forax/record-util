package com.github.forax.recordutil;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.UndeclaredThrowableException;

/**
 * An interface that provides a method {@link #toJSON()} for any {@link Record record} that
 * implements this interface.
 *
 * Adding this interface add a method {@link #toJSON()}
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
 * the module-info of this module should contains the following delcaration
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
    var shape = TraitImpl.mapShape(getClass());
    var builder = new StringBuilder().append('{');
    var separator = "";
    for(var i = 0; i < shape.size(); i++) {
      var key = shape.getKey(i);
      var value = shape.getValue(i);
      builder.append(separator)
          .append('"').append(key).append("\": ")
          .append(escape(invokeValue(value)));
      separator = ", ";
    }
    return builder.append('}').toString();
  }

  private Object invokeValue(MethodHandle getter) {
    try {
      return getter.invokeExact((Object) this);
    } catch(RuntimeException | Error e) {
      throw e;
    } catch (Throwable t) {
      throw new UndeclaredThrowableException(t);
    }
  }

  private static String escape(Object o) {
    return o instanceof String text? "\"" + text.replace("\"", "\\\"") + "\"": String.valueOf(o);
  }
}
