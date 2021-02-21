package com.github.forax.recordutil;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WithTraitTest {
  @Test
  public void with1() {
    record Person(String name, int age) implements WithTrait<Person> {}
    var person = new Person("Bob", 42);
    assertEquals(new Person("Ana", 23), person.with("name", "Ana").with("age", 23));
  }

  @Test
  public void with2() {
    record Person(String name, int age) implements WithTrait<Person> {}
    var person = new Person("Bob", 42);
    assertEquals(new Person("Ana", 23), person.with("age", 23, "name", "Ana"));
  }

  @Test
  public void with3() {
    record Point3D(int x, int y, int z) implements WithTrait<Point3D> {}
    var point = new Point3D(1, 2, 3);
    assertEquals(new Point3D(4, 7, -3), point.with("z", -3, "x", 4, "y", 7));
  }

  @Test
  public void with4() {
    record Address(int number, String street, String city, String state, String country)
        implements WithTrait<Address> {}

    var address = new Address(13, "baker street", "san jose", "CA", "United States");
    var address2 = address.with(
        "country", "Spain",
        "number", 354,
        "city", "Madrid",
        "state", "n/a");
    assertEquals(new Address(354, "baker street", "Madrid", "n/a", "Spain"), address2);
  }

  @Test
  public void withAll() {
    record Address(int number, String street, String city, String state, String country)
        implements WithTrait<Address> {}

    var address = new Address(13, "baker street", "san jose", "CA", "United States");
    var address2 = address.with(
        "country", "Spain",
        "number", 354,
        "city", "Madrid",
        "state", "n/a",
        "street", "5th avenue");
    assertEquals(new Address(354, "5th avenue", "Madrid", "n/a", "Spain"), address2);
  }
}