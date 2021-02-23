package com.github.forax.recordutil;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

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
  public void toJSONWithPrimitiveType() {
    record Foo(boolean b, char c, int i, long l, float f, double d, String s) implements JSONTrait {}
    var foo = new Foo(true, 'f', 2, 3L, 4f, 8.0, null);

    assertEquals("""
        {"b": true, "c": "f", "i": 2, "l": 3, "f": 4.0, "d": 8.0, "s": null}\
        """, foo.toJSON());
  }

  @Test
  public void toHumanReadableJSONWithPrimitiveType() {
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
  public void toJSONWithUnknownType() {
    record Timestamp(LocalDate date) implements JSONTrait {}
    var timestamp = new Timestamp(LocalDate.of(2000, 1, 1));

    assertEquals("""
        {"date": "2000-01-01"}\
        """, timestamp.toJSON());
  }

  @Test
  public void toHumanRedableJSONWithUnknownType() {
    record Timestamp(LocalDate date) implements JSONTrait {}
    var timestamp = new Timestamp(LocalDate.of(2000, 1, 1));

    assertEquals("""
        {
          "date": "2000-01-01"
        }\
        """, timestamp.toHumanReadableJSON());
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
}