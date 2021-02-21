package com.github.forax.recordutil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MutableCallSite;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static java.lang.invoke.MethodHandles.dropArguments;
import static java.lang.invoke.MethodHandles.filterArguments;
import static java.lang.invoke.MethodHandles.guardWithTest;
import static java.lang.invoke.MethodHandles.insertArguments;
import static java.lang.invoke.MethodHandles.permuteArguments;
import static java.lang.invoke.MethodType.methodType;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.range;

class WitherImpl {
  public static MethodHandle createMH(Lookup lookup, Class<?> recordType) {
    var components = recordType.getRecordComponents();
    if (components == null) {
      throw new LinkageError("the record class " + recordType.getName() + " is not a record ");
    }
    Map<String, Integer> nameToIndexMap = range(0, components.length).boxed().collect(toMap(i -> components[i].getName(), i -> i));

    MethodHandle constructor;
    try {
      constructor = lookup.findConstructor(recordType, methodType(void.class, Arrays.stream(components).map(RecordComponent::getType).toArray(Class[]::new)));
    } catch (NoSuchMethodException e) {
      throw (NoSuchMethodError) new NoSuchMethodError().initCause(e);
    } catch (IllegalAccessException e) {
      throw (IllegalAccessError) new IllegalAccessError().initCause(e);
    }

    return new InliningCache(lookup, recordType, components, nameToIndexMap, constructor).dynamicInvoker().asType(
        methodType(Object.class, Object.class, int.class, String.class, Object.class, String.class, Object.class, String.class, Object.class, String.class, Object.class, String.class, Object.class, String.class, Object.class, String.class, Object.class, String.class, Object.class, String.class, Object.class));
  }

  private static class InliningCache extends MutableCallSite {
    private static final MethodHandle NAME_CHECK, NAME_COUNT_CHECK, FALLBACK;
    static {
      var lookup = MethodHandles.lookup();
      try {
        NAME_CHECK = lookup.findStatic(InliningCache.class, "nameCheck",
            methodType(boolean.class, String.class, String.class));
        NAME_COUNT_CHECK = lookup.findStatic(InliningCache.class, "nameCountCheck",
            methodType(boolean.class, int.class, int.class));
        FALLBACK = lookup.findVirtual(InliningCache.class, "fallback",
            methodType(Object.class, Object.class, int.class, String.class, Object.class, String.class, Object.class, String.class, Object.class, String.class, Object.class, String.class, Object.class, String.class, Object.class, String.class, Object.class, String.class, Object.class, String.class, Object.class));
      } catch (NoSuchMethodException | IllegalAccessException e) {
        throw new AssertionError(e);
      }
    }

    private final Lookup lookup;
    private final Class<?> recordType;
    private final RecordComponent[] components;
    private final Map<String, Integer> nameToIndexMap;
    private final MethodHandle constructor;

    private InliningCache(Lookup lookup, Class<?> recordType, RecordComponent[] components, Map<String, Integer> nameToIndexMap, MethodHandle constructor) {
      super(methodType(Object.class, Object.class, int.class, String.class, Object.class, String.class, Object.class, String.class, Object.class, String.class, Object.class, String.class, Object.class, String.class, Object.class, String.class, Object.class, String.class, Object.class, String.class, Object.class));
      this.lookup = lookup;
      this.recordType = recordType;
      this.components = components;
      this.nameToIndexMap = nameToIndexMap;
      this.constructor = constructor;
      setTarget(FALLBACK.bindTo(this).asType(type()));
    }

    private static boolean nameCheck(String name, String expected) {
      //noinspection StringEquality
      return name == expected;
    }

    private static boolean nameCountCheck(int nameCount, int expected) {
      return nameCount == expected;
    }

    private Object fallback(Object record, int nameCount,
                            String name0, Object value0, String name1, Object value1, String name2, Object value2,
                            String name3, Object value3, String name4, Object value4, String name5, Object value5,
                            String name6, Object value6, String name7, Object value7, String name8, Object value8) throws Throwable {

      Objects.requireNonNull(record, "record is null");
      var names = gatherKeys(nameCount, name0, name1, name2, name3, name4, name5, name6, name7, name8);

      // check null names, non interned names or duplicate names
      var duplicates = new HashSet<String>();
      for(var i = 0; i < names.length; i++) {
        var name = names[i];
        Objects.requireNonNull(name, "name " + i + " is null");
        //noinspection StringEquality
        if (name != name.intern()) {
          throw new IllegalArgumentException("name " + name + " should be a constant name");
        }
        if (!duplicates.add(name)) {
          throw new IllegalArgumentException("duplicate name " + i);
        }
      }

      int[] reorder = new int[components.length];
      Class<?>[] newTypes = new Class<?>[1 + names.length];
      newTypes[0] = recordType;
      for(var i = 0; i < names.length; i++) {
        var name = names[i];
        var componentIndex = nameToIndexMap.get(name);
        if (componentIndex == null) {
          throw new IllegalArgumentException("unknown record component " + name + " for record " + recordType.getName());
        }

        reorder[componentIndex] = i + 1;
        newTypes[i + 1] = components[componentIndex].getType();
      }

      // use getters
      var filters = range(0, components.length)
          .mapToObj(i -> (reorder[i] != 0)? null: asGetter(lookup, components[i]))
          .toArray(MethodHandle[]::new);
      var mh = filterArguments(constructor, 0, filters);

      // re-organise, duplicate the record if there is a getter
      mh = permuteArguments(mh, methodType(recordType, newTypes), reorder);

      // drop the names
      for(var i = names.length; --i >= 0;) {
        mh = dropArguments(mh, 1 + i, String.class);
      }

      // drop null name/value pair up to 9
      if (names.length != 9) {
        mh = dropArguments(mh, mh.type().parameterCount(), range(names.length, 9).boxed().flatMap(__ -> Stream.of(String.class, Object.class)).toArray(Class[]::new));
      }

      var result = mh.invoke(record, name0, value0, name1, value1, name2, value2, name3, value3, name4, value4, name5, value5, name6, value6, name7, value7, name8, value8);

      // drop nameCount
      mh = dropArguments(mh, 1, int.class);

      // mask all values as Object
      mh = mh.asType(type());

      // install constant name guards
      var newTypes2 = mh.type().parameterArray();
      var other = new InliningCache(lookup, recordType, components, nameToIndexMap, constructor).dynamicInvoker();
      for(var i = 0; i < names.length; i++) {
        var name = names[i];
        var check = insertArguments(NAME_CHECK, 1, name);
        var test = dropArguments(check, 0, Arrays.stream(newTypes2, 0, 2 + 2 * i).toArray(Class[]::new));
        mh = guardWithTest(test, mh, other);
      }

      // install nameCount guard
      var check = insertArguments(NAME_COUNT_CHECK, 1, nameCount);
      var test = dropArguments(check, 0, Object.class);
      mh = guardWithTest(test, mh, other);

      // install the target
      setTarget(mh);

      return result;
    }

    private static MethodHandle asGetter(Lookup lookup, RecordComponent component) {
      try {
        return  lookup.unreflect(component.getAccessor());
      } catch (IllegalAccessException e) {
        throw (LinkageError) new LinkageError().initCause(e);
      }
    }

    @SuppressWarnings({"fallthrough", "DefaultNotLastCaseInSwitch"})
    private static String[] gatherKeys(int nameCount, String name0, String name1, String name2, String name3, String name4, String name5, String name6, String name7, String name8) {
      var names = new String[nameCount];
      switch (nameCount) {
        default:
          throw new IllegalArgumentException("invalid nameCount " + nameCount);
        case 9:
          names[8] = name8;  // fallthrough
        case 8:
          names[7] = name7;  // fallthrough
        case 7:
          names[6] = name6;  // fallthrough
        case 6:
          names[5] = name5;  // fallthrough
        case 5:
          names[4] = name4;  // fallthrough
        case 4:
          names[3] = name3;  // fallthrough
        case 3:
          names[2] = name2;  // fallthrough
        case 2:
          names[1] = name1;  // fallthrough
        case 1:
          names[0] = name0;  // fallthrough
      }
      return names;
    }
  }
}
