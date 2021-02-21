package com.github.forax.recordutil;

import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandles;

import static org.junit.jupiter.api.Assertions.*;

public class WitherTest {

  @Test
  public void with1() {
    record Person(String name, int age) {}
    var wither = Wither.of(MethodHandles.lookup(), Person.class);

    var bob = new Person("Bob", 42);
    assertAll(
        () -> assertEquals(new Person("Ana", 42), wither.with(bob, "name", "Ana")),
        () -> assertEquals(new Person("Bob", 23), wither.with(bob, "age", 23))
        );
  }

  @Test
  public void with2() {
    record Person(String name, int age) {}
    var wither = Wither.of(MethodHandles.lookup(), Person.class);

    var bob = new Person("Bob", 42);
    assertAll(
        () -> assertEquals(new Person("Ana", 23), wither.with(bob, "name", "Ana", "age", 23)),
        () -> assertEquals(new Person("Ana", 23), wither.with(bob, "age", 23, "name", "Ana"))
    );
  }

  @Test
  public void with3() {
    record Point3D(double x, double y, double z) {}
    var wither = Wither.of(MethodHandles.lookup(), Point3D.class);

    var point = new Point3D(1.0, 2.0, 4.0);
    assertAll(
        () -> assertEquals(new Point3D(3, 7, 13), wither.with(point, "x", 3.0, "y", 7.0, "z", 13.0)),
        () -> assertEquals(new Point3D(3, 7, 13), wither.with(point, "y", 7.0, "z", 13.0, "x", 3.0))
    );
  }

  @Test
  public void with4() {
    record Address(int number, String street, String city, String state, String country) {}
    var wither = Wither.of(MethodHandles.lookup(), Address.class);

    var address = new Address(13, "baker street", "san jose", "CA", "United States");
    var expected = new Address(71, "river street", "banshee", "TX", "United States");
    assertAll(
        () -> assertEquals(expected, wither.with(address, "number", 71, "street", "river street", "city", "banshee", "state", "TX")),
        () -> assertEquals(expected, wither.with(address, "state", "TX", "street", "river street", "number", 71, "city", "banshee"))
    );
  }

  @Test
  public void with5() {
    record Address(int number, String street, String city, String state, String country) {}
    var wither = Wither.of(MethodHandles.lookup(), Address.class);

    var address = new Address(13, "baker street", "san jose", "CA", "United States");
    var expected = new Address(71, "river street", "banshee", "n/a", "Poland");
    assertAll(
        () -> assertEquals(expected, wither.with(address, "number", 71, "street", "river street", "city", "banshee", "state", "n/a", "country", "Poland")),
        () -> assertEquals(expected, wither.with(address, "state", "n/a", "country", "Poland", "street", "river street", "number", 71, "city", "banshee"))
    );
  }

  @Test
  public void with6() {
    record Foo(int a, long b, float c, double d, boolean e, byte f, char g, short h, Object i) {}
    var wither = Wither.of(MethodHandles.lookup(), Foo.class);

    var foo = new Foo(1, 2, 3, 5, true, (byte) 5, '6', (short) 7, null);
    var expected = new Foo(1, 3, 4, 5, false, (byte) 6, '7', (short) 8, null);
    assertAll(
        () -> assertEquals(expected, wither.with(foo, "b", 3L, "c", 4f, "e", false, "f", (byte) 6, "g", '7', "h", (short) 8)),
        () -> assertEquals(expected, wither.with(foo, "h", (short) 8, "e", false, "f", (byte) 6, "g", '7', "b", 3L, "c", 4f))
    );
  }

  @Test
  public void with7() {
    record Foo(int a, long b, float c, double d, boolean e, byte f, char g, short h, Object i) {}
    var wither = Wither.of(MethodHandles.lookup(), Foo.class);

    var foo = new Foo(1, 2, 3, 4, true, (byte) 5, '6', (short) 7, null);
    var expected = new Foo(1, 3, 4, 5, false, (byte) 6, '7', (short) 8, null);
    assertAll(
        () -> assertEquals(expected, wither.with(foo, "b", 3L, "c", 4f, "d", 5.0, "e", false, "f", (byte) 6, "g", '7', "h", (short) 8)),
        () -> assertEquals(expected, wither.with(foo, "e", false, "d", 5.0,"f", (byte) 6, "g", '7', "b", 3L, "c", 4f,  "h", (short) 8))
    );
  }

  @Test
  public void with8() {
    record Foo(int a, long b, float c, double d, boolean e, byte f, char g, short h, Object i) {}
    var wither = Wither.of(MethodHandles.lookup(), Foo.class);

    var foo = new Foo(1, 2, 3, 4, true, (byte) 5, '6', (short) 7, null);
    var expected = new Foo(2, 3, 4, 5, false, (byte) 6, '7', (short) 8, null);
    assertAll(
        () -> assertEquals(expected, wither.with(foo, "a", 2, "b", 3L, "c", 4f, "d", 5.0, "e", false, "f", (byte) 6, "g", '7', "h", (short) 8)),
        () -> assertEquals(expected, wither.with(foo, "d", 5.0,"f", (byte) 6, "g", '7', "a", 2, "e",  false, "b", 3L, "c", 4f,  "h", (short) 8))
    );
  }

  @Test
  public void with9() {
    record Foo(int a, long b, float c, double d, boolean e, byte f, char g, short h, Object i) {}
    var wither = Wither.of(MethodHandles.lookup(), Foo.class);

    var foo = new Foo(1, 2, 3, 4, true, (byte) 5, '6', (short) 7, new Object());
    var expected = new Foo(2, 3, 4, 5, false, (byte) 6, '7', (short) 8, null);
    assertAll(
        () -> assertEquals(expected, wither.with(foo, "a", 2, "b", 3L, "c", 4f, "d", 5.0, "e", false, "f", (byte) 6, "g", '7', "h", (short) 8, "i", null)),
        () -> assertEquals(expected, wither.with(foo, "f", (byte) 6, "g", '7', "a", 2, "d", 5.0, "e", false, "i", null,  "b", 3L, "c", 4f,  "h", (short) 8))
    );
  }

  @Test
  public void withDifferentKeyCounts() {
    record Person(String name, int age) {}
    var wither = Wither.of(MethodHandles.lookup(), Person.class);

    var bob = new Person("Bob", 42);
    assertAll(
        () -> assertEquals(new Person("Ana", 42), wither.with(bob, "name", "Ana")),
        () -> assertEquals(new Person("Ana", 23), wither.with(bob, "age", 23, "name", "Ana"))
    );
  }

  @Test
  public void withNullRecord() {
    record Person(String name, int age) {}
    var wither = Wither.of(MethodHandles.lookup(), Person.class);

    assertThrows(NullPointerException.class, () -> wither.with(null, "name", "Ana"));
  }

  @Test
  public void withANullKey() {
    record Person(String name, int age) {}
    var wither = Wither.of(MethodHandles.lookup(), Person.class);

    var bob = new Person("Bob", 42);
    assertThrows(NullPointerException.class, () -> wither.with(bob, "name", "foo", null, "bar"));
  }

  @Test
  public void withInvalidKey() {
    record Person(String name, int age) {}
    var wither = Wither.of(MethodHandles.lookup(), Person.class);

    var bob = new Person("Bob", 42);
    assertThrows(IllegalArgumentException.class, () -> wither.with(bob, "foo", "Bar"));
  }

  @Test
  public void withNonInternedKey() {
    record Person(String name, int age) {}
    var wither = Wither.of(MethodHandles.lookup(), Person.class);

    var bob = new Person("Bob", 42);
    assertThrows(IllegalArgumentException.class, () -> wither.with(bob, new String("name"), "Ana"));
  }

  @Test
  public void withDuplicateKeys() {
    record Person(String name, int age) {}
    var wither = Wither.of(MethodHandles.lookup(), Person.class);

    var bob = new Person("Bob", 42);
    assertThrows(IllegalArgumentException.class, () -> wither.with(bob, "name", "Ana", "name", "Elis"));
  }
}