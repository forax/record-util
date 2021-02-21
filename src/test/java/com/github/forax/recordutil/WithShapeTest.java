package com.github.forax.recordutil;

import com.github.forax.recordutil.TraitImpl.WithShape;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WithShapeTest {
  record Person(String name, int age) { }

  private static final MethodHandle NAME, AGE, CONSTRUCTOR;

  static {
    var lookup = MethodHandles.lookup();
    try {
      NAME = lookup.findVirtual(Person.class, "name", MethodType.methodType(String.class));
      AGE = lookup.findVirtual(Person.class, "age", MethodType.methodType(int.class));
      CONSTRUCTOR = lookup.findConstructor(Person.class, MethodType.methodType(void.class, String.class, int.class));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new AssertionError(e);
    }
  }

  @Test
  public void getEmptySize() {
    var shape = new WithShape(0, CONSTRUCTOR);
    assertEquals(-1, shape.getSlot("foo"));
    assertEquals(-1, shape.getSlot("bar"));
    assertEquals(-1, shape.getSlot("baz"));
  }

  @Test
  public void getEmpty1() {
    var shape = new WithShape(1, CONSTRUCTOR);
    assertEquals(-1, shape.getSlot("foo"));
    assertEquals(-1, shape.getSlot("bar"));
    assertEquals(-1, shape.getSlot("baz"));
  }

  @Test
  public void getEmpty6() {
    var shape = new WithShape(6, CONSTRUCTOR);
    assertEquals(-1, shape.getSlot("foo"));
    assertEquals(-1, shape.getSlot("bar"));
    assertEquals(-1, shape.getSlot("baz"));
  }

  @Test
  public void getPut1() {
    var shape = new WithShape(1, CONSTRUCTOR);
    shape.put(0, "age", AGE);
    assertEquals(1, shape.size());
    assertEquals(0, shape.getSlot("age"));
    assertEquals(AGE, shape.getValue(0));
  }

  @Test
  public void getPut2() {
    var shape = new WithShape(2, CONSTRUCTOR);
    shape.put(0, "name", NAME);
    shape.put(1, "age", AGE);
    assertEquals(2, shape.size());
    assertEquals(0, shape.getSlot("name"));
    assertEquals(1, shape.getSlot("age"));
    assertEquals(NAME, shape.getValue(0));
    assertEquals(AGE, shape.getValue(1));
    assertEquals(-1, shape.getSlot("baz"));
    assertEquals(-1, shape.getSlot("joy"));
    assertEquals(-1, shape.getSlot("love"));
  }

  @Test
  public void getKeyPutALot() {
    var capacity = 100_000;
    var shape = new WithShape(capacity, CONSTRUCTOR);
    IntStream.range(0, capacity).forEach(i -> shape.put(i, "" + i, i %2 == 0? NAME: AGE));

    // hit
    IntStream.range(0, capacity).forEach(i -> assertEquals(i, shape.getSlot("" + i)));

    // miss
    IntStream.range(0, capacity).forEach(i -> assertEquals(-1, shape.getSlot("foo" + i)));
  }

  @Test
  public void getIndexPutALot() {
    var capacity = 100_000;
    var shape = new WithShape(capacity, CONSTRUCTOR);
    IntStream.range(0, capacity).forEach(i -> shape.put(i, "" + i, i %2 == 0? NAME: AGE));

    // linear scan
    IntStream.range(0, capacity).forEach(i -> assertEquals(i %2 == 0? NAME: AGE, shape.getValue(i)));
  }

  @Test
  public void size() {
    var shape = new WithShape(77, CONSTRUCTOR);
    assertEquals(77, shape.size());
  }
}