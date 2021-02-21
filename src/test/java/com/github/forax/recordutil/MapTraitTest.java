package com.github.forax.recordutil;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class MapTraitTest {

  @Test
  public void size() {
    record Point(int x, int y) implements MapTrait {}
    var point = new Point(1, 2);
    assertEquals(2, point.size());
  }

  @Test
  public void isEmpty() {
    record Person(String name, int age) implements MapTrait { }
    var person = new Person("Bob", 42);
    assertFalse(person.isEmpty());
  }

  @Test
  public void get() {
    record Person(String name, int age) implements MapTrait { }
    var person = new Person("Bob", 42);
    assertAll(
        () -> assertEquals("Bob", person.get("name")),
        () -> assertEquals(42, person.get("age")),
        () -> assertNull(person.get("whatever")),
        () -> assertNull(person.get(null))
    );
  }

  @Test
  public void getOrDefault() {
    record Person(String name, int age) implements MapTrait { }
    var person = new Person("Bob", 42);
    assertAll(
        () -> assertEquals("Bob", person.getOrDefault("name", "oops")),
        () -> assertEquals(42, person.getOrDefault("age", 0)),
        () -> assertEquals("boom", person.getOrDefault("whatever", "boom")),
        () -> assertEquals("boom", person.getOrDefault(null, "boom"))
    );
  }

  @Test
  public void containsKey() {
    record Person(String name, int age) implements MapTrait { }
    var person = new Person("Bob", 42);
    assertAll(
        () -> assertTrue(person.containsKey("name")),
        () -> assertTrue(person.containsKey("age")),
        () -> assertFalse(person.containsKey("whatever")),
        () -> assertFalse(person.containsKey(null))
    );
  }

  @Test
  public void containsValue() {
    record Person(String name, int age) implements MapTrait { }
    var person = new Person("Bob", 42);
    assertAll(
        () -> assertTrue(person.containsValue("Bob")),
        () -> assertTrue(person.containsValue(42)),
        () -> assertFalse(person.containsValue("whatever")),
        () -> assertFalse(person.containsValue(null))
    );
  }

  @Test
  public void entrySet() {
    record Person(String name, int age) implements MapTrait { }
    var person = new Person("Bob", 42);
    assertEquals(Set.of(Map.entry("name", "Bob"), Map.entry("age", 42)), person.entrySet());
    assertEquals(List.of(Map.entry("name", "Bob"), Map.entry("age", 42)), person.entrySet().stream().toList());
  }

  @Test
  public void keySet() {
    record Person(String name, int age) implements MapTrait { }
    var person = new Person("Bob", 42);
    assertAll(
        () -> assertEquals(Set.of("name", "age"), person.keySet()),
        () -> assertEquals(List.of("name", "age"), person.keySet().stream().toList()),
        () -> assertTrue(person.keySet().contains("name")),
        () -> assertTrue(person.keySet().contains("age")),
        () -> assertFalse(person.keySet().contains("foo"))
    );
  }

  @Test
  public void values() {
    record Person(String name, int age) implements MapTrait { }
    var person = new Person("Bob", 42);
    assertEquals(List.of("Bob", 42), person.values());
  }

  @Test
  public void keys() {
    record Person(String name, int age) implements MapTrait { }
    var person = new Person("Bob", 42);
    assertAll(
        () -> assertEquals(List.of("name", "age"), person.keys()),
        () -> assertTrue(person.keys().contains("name")),
        () -> assertTrue(person.keys().contains("age")),
        () -> assertFalse(person.keys().contains("foo"))
    );
  }

  @Test
  public void unsupported() {
    record Person(String name, int age) implements MapTrait { }
    var person = new Person("Bob", 42);
    assertAll(
        () -> assertThrows(UnsupportedOperationException.class, () -> person.clear()),
        () -> assertThrows(UnsupportedOperationException.class, () -> person.put("foo", "bar")),
        () -> assertThrows(UnsupportedOperationException.class, () -> person.putAll(Map.of())),
        () -> assertThrows(UnsupportedOperationException.class, () -> person.remove("name")),
        () -> assertThrows(UnsupportedOperationException.class, () -> person.remove("name", "Bob")),
        () -> assertThrows(UnsupportedOperationException.class, () -> person.replace("name", "Ana")),
        () -> assertThrows(UnsupportedOperationException.class, () -> person.replace("name", "Bob", "Ana")),
        () -> assertThrows(UnsupportedOperationException.class, () -> person.replaceAll((key, value) -> value)),
        () -> assertThrows(UnsupportedOperationException.class, () -> person.compute("Bob", (key, value) -> value)),
        () -> assertThrows(UnsupportedOperationException.class, () -> person.computeIfAbsent("Bob", key -> 77)),
        () -> assertThrows(UnsupportedOperationException.class, () -> person.computeIfPresent("Bob", (key, value) -> 77))
    );
  }

  @Test
  public void equalsHashCode() {
    record Person(String name, int age) implements MapTrait {
      @Override
      public boolean equals(Object o) {
        return MapTrait.super.equalsOfMap(o);
      }

      @Override
      public int hashCode() {
        return MapTrait.super.hashCodeOfMap();
      }
    }
    var person = new Person("Bob", 42);
    assertAll(
        () -> assertEquals(Map.of("name", "Bob", "age", 42), person),
        () -> assertFalse(person.equals(null)),
        () -> assertEquals(Map.of("name", "Bob", "age", 42).hashCode(), person.hashCode())
    );
  }

  @Test
  public void testToString() {
    record Person(String name, int age) implements MapTrait {
      @Override
      public String toString() {
        return MapTrait.super.toStringOfMap();
      }
    }
    var person = new Person("Bob", 42);
    var expectedMap = new LinkedHashMap<String, Object>();
    expectedMap.put("name", "Bob");
    expectedMap.put("age", 42);
    assertEquals(expectedMap.toString(), person.toString());
  }
}