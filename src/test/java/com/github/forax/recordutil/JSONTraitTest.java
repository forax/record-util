package com.github.forax.recordutil;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JSONTraitTest {

  @Test
  public void toJSON() {
    record Person(String name, int age) implements JSONTrait {}
    var person = new Person("Bob", 42);

    assertEquals("""
        {"name": "Bob", "age": 42}\
        """, person.toJSON());
  }

  @Test
  public void toHumanReadableJSON() {
    record Person(String name, int age) implements JSONTrait {}
    var person = new Person("Bob", 42);

    assertEquals("""
        {
          "name": "Bob",
          "age": 42
        }\
        """, person.toHumanReadableJSON());
  }

  @Test
  public void parseJSON() throws IOException {
    record Person(String name, int age) {}
    var person = JSONTrait.parse(new StringReader("""
        {
          "name": "Bob",
          "age": 42
        }\
        """), Person.class);

    var expected = new Person("Bob", 42);
    assertEquals(expected, person);
  }

  @Test
  public void toJSONWithPrimitiveTypes() {
    record Foo(boolean b, char c, int i, long l, float f, double d, String s) implements JSONTrait {}
    var foo = new Foo(true, 'f', 2, 3L, 4f, 8.0, null);

    assertEquals("""
        {"b": true, "c": "f", "i": 2, "l": 3, "f": 4.0, "d": 8.0, "s": null}\
        """, foo.toJSON());
  }

  @Test
  public void toHumanReadableJSONWithPrimitiveTypes() {
    record Foo(boolean b, char c, int i, long l, float f, double d, String s) implements JSONTrait {}
    var foo = new Foo(true, 'f', 2, 3L, 4f, 8.0, null);

    assertEquals("""
        {
          "b": true,
          "c": "f",
          "i": 2,
          "l": 3,
          "f": 4.0,
          "d": 8.0,
          "s": null
        }\
        """, foo.toHumanReadableJSON());
  }

  @Test
  public void parseJSONWithPrimitiveTypes() throws IOException {
    record Foo(boolean b, char c, int i, long l, float f, double d, String s) {}
    var person = JSONTrait.parse(new StringReader("""
        {
          "b": true,
          "c": "f",
          "i": 2,
          "l": 3,
          "f": 4.0,
          "d": 8.0,
          "s": null
        }\
        """), Foo.class);

    var expected = new Foo(true, 'f', 2, 3L, 4f, 8.0, null);
    assertEquals(expected, person);
  }

  @Test
  public void toJSONWithUnknownType() {
    record Timestamp(LocalDate date) implements JSONTrait {}
    var timestamp = new Timestamp(LocalDate.of(2000, 1, 1));

    assertEquals("""
        {"date": "2000-01-01"}\
        """, timestamp.toJSON());
  }

  @Test
  public void toHumanReadableJSONWithUnknownType() {
    record Timestamp(LocalDate date) implements JSONTrait {}
    var timestamp = new Timestamp(LocalDate.of(2000, 1, 1));

    assertEquals("""
        {
          "date": "2000-01-01"
        }\
        """, timestamp.toHumanReadableJSON());
  }

  @Test
  public void parseJSONWithUnknownType() throws IOException {
    record Timestamp(LocalDate date) {}
    var timestamp = JSONTrait.parse(new StringReader("""
        {
          "date": "2000-01-01"
        }\
        """), Timestamp.class, (valueAsString, type, downstreamConverter) -> {
      if (type == LocalDate.class) {
        return LocalDate.parse(valueAsString);
      }
      return downstreamConverter.convert(valueAsString, type);
    });

    var expected = new Timestamp(LocalDate.of(2000, 1, 1));
    assertEquals(expected, timestamp);
  }

  @Test
  public void toEnclosedJSON() {
    record Address(int number, String street) {}
    record Person(String name, int age, Address address) implements JSONTrait {}
    var person = new Person("Bob", 42, new Address(13, "civic street"));

    assertEquals("""
        {"name": "Bob", "age": 42, "address": {"number": 13, "street": "civic street"}}\
        """, person.toJSON());
  }

  @Test
  public void toHumanReadableEnclosedJSON() {
    record Address(int number, String street) {}
    record Person(String name, int age, Address address) implements JSONTrait {}
    var person = new Person("Bob", 42, new Address(13, "civic street"));

    assertEquals("""
        {
          "name": "Bob",
          "age": 42,
          "address": {
            "number": 13,
            "street": "civic street"
          }
        }\
        """, person.toHumanReadableJSON());
  }

  @Test
  public void parseEnclosedJSON() throws IOException {
    record Address(int number, String street) {}
    record Person(String name, int age, Address address) {}
    var person = JSONTrait.parse(new StringReader("""
        {
          "name": "Bob",
          "age": 42,
          "address": {
            "number": 13,
            "street": "civic street"
          }
        }\
        """), Person.class);

    var expected = new Person("Bob", 42, new Address(13, "civic street"));
    assertEquals(expected, person);
  }

  @Test
  public void streamArrayOfJSONObject() {
    record Person(String name, int age) {}
    var reader = new StringReader("""
        [
          { "name": "Bob", "age": 42 },
          { "name": "Ana", "age": 27 }
        ]\
        """);

    List<Person> list;
    try(var stream = JSONTrait.stream(reader, Person.class)) {
      list = stream.toList();
    }

    var expected = List.of(new Person("Bob", 42), new Person("Ana", 27));
    assertEquals(expected, list);
  }
}