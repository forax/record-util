package com.github.forax.recordutil;

import com.github.forax.recordutil.TraitImpl.MapShape;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class MapShapeTest {
  private static final MethodHandle STRING_LENGTH;
  private static final MethodHandle STRING_EQUALS;
  static {
    var lookup = MethodHandles.lookup();
    try {
      STRING_LENGTH = lookup.findVirtual(String.class, "length", MethodType.methodType(int.class));
      STRING_EQUALS = lookup.findVirtual(String.class, "equals", MethodType.methodType(boolean.class, Object.class));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new AssertionError(e);
    }
  }

  @Test
  public void getEmptySize() {
    var shape = new MapShape(0);
    assertNull(shape.getValue("foo"));
    assertNull(shape.getValue("bar"));
    assertNull(shape.getValue("baz"));
  }

  @Test
  public void getEmpty1() {
    var shape = new MapShape(1);
    assertNull(shape.getValue("foo"));
    assertNull(shape.getValue("bar"));
    assertNull(shape.getValue("baz"));
  }

  @Test
  public void getEmpty6() {
    var shape = new MapShape(6);
    assertNull(shape.getValue("foo"));
    assertNull(shape.getValue("bar"));
    assertNull(shape.getValue("baz"));
  }

  @Test
  public void getPut1() {
    var shape = new MapShape(1);
    shape.put(0, "hello", STRING_LENGTH);
    assertEquals(1, shape.size());
    assertEquals(STRING_LENGTH, shape.getValue("hello"));
    assertEquals("hello", shape.getKey(0));
    assertEquals(STRING_LENGTH, shape.getValue(0));
  }

  @Test
  public void getPut2() {
    var shape = new MapShape(2);
    shape.put(0, "foo", STRING_LENGTH);
    shape.put(1, "bar", STRING_EQUALS);
    assertEquals(2, shape.size());
    assertEquals(STRING_LENGTH, shape.getValue("foo"));
    assertEquals(STRING_EQUALS, shape.getValue("bar"));
    assertNull(shape.getValue("baz"));
    assertNull(shape.getValue("joy"));
    assertNull(shape.getValue("love"));
    assertEquals("foo", shape.getKey(0));
    assertEquals(STRING_LENGTH, shape.getValue(0));
    assertEquals("bar", shape.getKey(1));
    assertEquals(STRING_EQUALS, shape.getValue(1));
  }

  @Test
  public void getKeyPutALot() {
    var capacity = 100_000;
    var shape = new MapShape(capacity);
    IntStream.range(0, capacity).forEach(i -> shape.put(i, "" + i, i %2 == 0? STRING_LENGTH: STRING_EQUALS));

    // hit
    IntStream.range(0, capacity).forEach(i -> assertEquals(i %2 == 0? STRING_LENGTH: STRING_EQUALS, shape.getValue("" + i)));

    // miss
    IntStream.range(0, capacity).forEach(i -> assertNull(shape.getValue("foo" + i)));
  }

  @Test
  public void getIndexPutALot() {
    var capacity = 100_000;
    var shape = new MapShape(capacity);
    IntStream.range(0, capacity).forEach(i -> shape.put(i, "" + i, i %2 == 0? STRING_LENGTH: STRING_EQUALS));

    // linear scan
    IntStream.range(0, capacity).forEach(i -> assertEquals("" + i, shape.getKey(i)));
    IntStream.range(0, capacity).forEach(i -> assertEquals(i %2 == 0? STRING_LENGTH: STRING_EQUALS, shape.getValue(i)));
  }

  @Test
  public void size() {
    var shape = new MapShape(77);
    assertEquals(77, shape.size());
  }
}