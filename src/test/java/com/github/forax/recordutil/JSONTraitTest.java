package com.github.forax.recordutil;

import org.junit.jupiter.api.Test;

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
}