package com.github.forax.recordutil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;

import static java.lang.invoke.MethodType.methodType;

class TraitImpl {

  /**
   * Combine a hash table ({@code table}) that stores a pair String/MethodHandle with
   * a list {@code vec} that stores the same pair.
   *
   * <ol>
   *  <li>to insert a pair uses {@link #put(int, String, MethodHandle)}
   *  <li>to know the number of pairs uses {@link #size()}
   *  <li>to get the value from a key uses {@link #getValue(String)}
   *  <li>to get the key from an index uses {@link #getKey(int)}
   *  <li>to get the value from an index uses {@link #getValue(int)}
   * </ol>
   */
  record MapShape(Object[] table, Object[] vec) {
    MapShape(int capacity) {
      this(new Object[capacity == 0? 2: Integer.highestOneBit(capacity) << 2], new Object[capacity << 1]);
    }

    void put(int index, String key, MethodHandle getter) {
      var slot = -probe(key) - 1;
      table[slot] = key;
      table[slot + 1] = getter;
      vec[index << 1] = key;
      vec[(index << 1) + 1] = getter;
    }

    MethodHandle getValue(String key) {
      var slot = probe(key);
      if (slot < 0) {
        return null;
      }
      return (MethodHandle) table[slot + 1];
    }

    boolean containsKey(String key) {
      return probe(key) >= 0;
    }

    int size() {
      return vec.length >> 1;
    }
    boolean isEmpty() {
      return vec.length == 0;
    }

    String getKey(int index) {
      return (String) vec[index << 1];
    }
    MethodHandle getValue(int index) {
      return (MethodHandle) vec[(index << 1) + 1];
    }

    private int probe(String key) {
      var slot = (key.hashCode() & ((table.length >> 1) - 1)) << 1;
      var k = table[slot];
      if (k == null) {
        return -slot - 1;
      }
      if (key.equals(k)) {
        return slot;
      }
      return probe2(key, slot);
    }

    private int probe2(String key, int slot) {
      for(;;) {
        slot = (slot + 2) & (table.length - 1);
        var k = table[slot];
        if (k == null) {
          return -slot - 1;
        }
        if (key.equals(k)) {
          return slot;
        }
      }
    }
  }

  private static final ClassValue<MapShape> SHAPE_MAP = new ClassValue<>() {
    @Override
    protected MapShape computeValue(Class<?> type) {
      var components = type.getRecordComponents();
      if (components == null) {
        throw new IllegalStateException(type.getName() + " is not a record");
      }
      var lookup = teleport(type, MethodHandles.lookup());

      var shape = new MapShape(components.length);
      for(var i = 0; i < components.length; i++) {
        var component = components[i];
        var getter = asMH(lookup, component).asType(methodType(Object.class, Object.class));
        shape.put(i, component.getName(), getter);
      }
      return shape;
    }
  };

  private static Lookup teleport(Class<?> type, Lookup localLookup) {
    // add read access to the type module
    localLookup.lookupClass().getModule().addReads(type.getModule());

    // then teleport
    try {
      return MethodHandles.privateLookupIn(type, localLookup);
    } catch (IllegalAccessException e) {
      throw (IllegalAccessError) new IllegalAccessError("""
          the module %s does not open the package %s to the module com.github.forax.recordutil
          you can add this incantation to the module-info
            module %s {
                ...
                opens %s to com.github.forax.recordutil;
            }
          """.formatted(type.getModule(), type.getPackageName(), type.getModule(), type.getPackageName())
        ).initCause(e);
    }
  }

  private static MethodHandle asMH(Lookup lookup, RecordComponent component) {
    try {
      return lookup.unreflect(component.getAccessor());
    } catch (IllegalAccessException e) {
      throw new AssertionError(e);
    }
  }

  /**
   * Returns a hash/list describing the associating between a record component
   * name and its corresponding getter as a method handle.
   *
   * @param type the class of the record
   * @return the hash/list describing the record
   */
  static MapShape mapShape(Class<?> type) {
    return SHAPE_MAP.get(type);
  }


  /**
   * Combine a hash table ({@code table}) that stores an index ({@code slot}) for
   * a String and a list that stores at the index ({@code slot})
   * the corresponding MethodHandle.
   * It also stores the constructor as a method handle.
   *
   * <ol>
   *  <li>to insert a pair uses {@link #put(int, String, MethodHandle)}
   *  <li>to know the number of key/value uses {@link #size()}
   *  <li>to get the slot (index) from a key uses {@link #getSlot(String)}
   *  <li>to get the value from a slot (index) uses {@link #getValue(int)}
   * </ol>
   */
  record WithShape(Object[] table, MethodHandle[] vec, MethodHandle constructor) {
    WithShape(int capacity, MethodHandle constructor) {
      this(new Object[capacity == 0? 2: Integer.highestOneBit(capacity) << 2], new MethodHandle[capacity], constructor);
    }

    void put(int index, String key, MethodHandle getter) {
      var slot = -probe(key) - 1;
      table[slot] = key;
      table[slot + 1] = index;
      vec[index] = getter;
    }

    int getSlot(String key) {
      var slot = probe(key);
      if (slot < 0) {
        return -1;
      }
      return (int) table[slot + 1];
    }

    int size() {
      return vec.length;
    }

    MethodHandle getValue(int index) {
      return vec[index];
    }

    private int probe(String key) {
      var slot = (key.hashCode() & ((table.length >> 1) - 1)) << 1;
      var k = table[slot];
      if (k == null) {
        return -slot - 1;
      }
      if (key.equals(k)) {
        return slot;
      }
      return probe2(key, slot);
    }

    private int probe2(String key, int slot) {
      for(;;) {
        slot = (slot + 2) & (table.length - 1);
        var k = table[slot];
        if (k == null) {
          return -slot - 1;
        }
        if (key.equals(k)) {
          return slot;
        }
      }
    }
  }

  private static final ClassValue<WithShape> WITH_SHAPE_MAP = new ClassValue<>() {
    @Override
    protected WithShape computeValue(Class<?> type) {
      var components = type.getRecordComponents();
      if (components == null) {
        throw new IllegalStateException(type.getName() + " is not a record");
      }
      var lookup = teleport(type, MethodHandles.lookup());
      var constructor = asConstructor(lookup, type, components)
          .asType(MethodType.genericMethodType(components.length))
          .asSpreader(Object[].class, components.length);

      var shape = new WithShape(components.length, constructor);
      for(var i = 0; i < components.length; i++) {
        var component = components[i];
        var getter = asMH(lookup, component).asType(methodType(Object.class, Object.class));
        shape.put(i, component.getName(), getter);
      }
      return shape;
    }
  };

  private static MethodHandle asConstructor(Lookup lookup, Class<?> type, RecordComponent[] components) {
    try {
      return lookup.findConstructor(type, methodType(void.class, Arrays.stream(components).map(RecordComponent::getType).toArray(Class[]::new)));
    } catch (IllegalAccessException | NoSuchMethodException e) {
      throw new AssertionError(e);
    }
  }

  /**
   * Returns a hash/list describing the associating between a record component
   * name and its corresponding getter as a method handle and
   * the constructor.
   *
   * @param type the class of the record
   * @return the hash/list describing the record
   */
  static WithShape withShape(Class<?> type) {
    return WITH_SHAPE_MAP.get(type);
  }
}
