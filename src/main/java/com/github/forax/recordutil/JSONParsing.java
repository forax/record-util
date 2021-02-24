package com.github.forax.recordutil;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.UndeclaredThrowableException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

class JSONParsing {
  public static Object parse(Reader reader, Class<?> recordType, JSONTrait.Converter converter) throws IOException {
    try (var parser = createFactory().createParser(reader)) {
      if (parser.nextToken() != JsonToken.START_OBJECT) {
        throw new IOException("invalid start token for a JSON Object" + parser.getText());
      }
      return parseRecord(parser, recordType, converter);
    }
  }

  private static JsonFactory createFactory() {
    try {
      return new JsonFactory();
    } catch(NoClassDefFoundError error) {
      error.addSuppressed(new ClassNotFoundException("""
          
          This feature requires the JSON parser named 'jackson'. To enable it, you have to
          add `requires requires com.fasterxml.jackson.core;` to your module-info
          and also add the dependency to `com.fasterxml.jackson.core:jackson-core` in your POM file
          """));
      throw error;
    }
  }

  public static <R extends Record> Stream<R> stream(Reader reader, Class<? extends R> recordType, JSONTrait.Converter converter) {
    JsonParser _parser = null;
    try {
      _parser = createFactory().createParser(reader);
      if (_parser.nextToken() != JsonToken.START_ARRAY) {
        throw new UncheckedIOException(new IOException("invalid start token for a JSON Array" + _parser.getText()));
      }
    } catch(IOException e) {
      try {
        if (_parser != null) {
          _parser.close();
        }
      } catch(IOException e2) {
        e.addSuppressed(e2);
      }
      throw new UncheckedIOException(e);
    }

    var parser = _parser;
    var stream = StreamSupport.stream(new Spliterator<R>() {
      @Override
      public boolean tryAdvance(Consumer<? super R> action) {
        try {
          var token = parser.nextToken();
          if (token == JsonToken.END_ARRAY) {
            return false;
          }
          var record = recordType.cast(parseRecord(parser, recordType, converter));
          action.accept(record);
          return true;
        } catch(IOException e) {
          throw new UncheckedIOException(e);
        }
      }
      @Override
      public Spliterator<R> trySplit() {
        return null;
      }
      @Override
      public long estimateSize() {
        return Long.MAX_VALUE;
      }
      @Override
      public int characteristics() {
        return ORDERED;
      }
    }, false);
    return stream.onClose(() -> {
      try {
        parser.close();
      } catch(IOException e) {
        // silently ignore it
      }
    });
  }

  private static Object parseRecord(JsonParser parser, Class<?> recordType, JSONTrait.Converter converter) throws IOException {
    var shape = TraitImpl.jsonShape(recordType);
    var array = new Object[shape.size()];
    for(;;) {
      var token = parser.nextToken();
      switch (token) {
        case END_OBJECT:
          return invokeArray(shape.constructor(), array);
        case FIELD_NAME: {
          var name = parser.getCurrentName();
          var slot = shape.getSlot(name);
          if (slot == -1) {
            throw new IOException("invalid key name " + name + " for record " + recordType.getName());
          }
          var type = shape.getType(slot);
          array[slot] = parseValue(parser, parser.nextValue(), type, converter);
          continue;
        }
        default:
          throw new IOException("invalid token " + parser.getText());
      }
    }
  }

  private static Object invokeArray(MethodHandle constructor, Object[] array) {
    try {
      return constructor.invokeExact(array);
    } catch (RuntimeException | Error e) {
      throw e;
    } catch (Throwable throwable) {
      throw new UndeclaredThrowableException(throwable);
    }
  }

  private static Object parseArray(JsonParser parser, Class<?> type, JSONTrait.Converter converter) throws IOException {
    var list = new ArrayList<>();
    for(;;) {
      var token = parser.nextToken();
      if (token == JsonToken.END_ARRAY) {
        return defaultListConversion(list, type);
      } else {
        list.add(parseValue(parser, token, type, converter));
      }
    }
  }

  private static Object parseValue(JsonParser parser, JsonToken token, Class<?> type, JSONTrait.Converter converter) throws IOException {
    return switch (token) {
      case VALUE_TRUE -> true;
      case VALUE_FALSE -> false;
      case VALUE_NULL -> null;
      case VALUE_NUMBER_INT, VALUE_NUMBER_FLOAT -> convertValue(parser.getValueAsString(), type, converter);
      case VALUE_STRING -> convertValue(parser.getText(), type, converter);
      case START_OBJECT -> parseRecord(parser, type, converter);
      case START_ARRAY -> parseArray(parser, type, converter);
      default -> throw new IOException("invalid value " + parser.getValueAsString());
    };
  }

  private static Object convertValue(String valueAsString, Class<?> type, JSONTrait.Converter converter) throws IOException {
    return converter.convert(valueAsString, type, JSONParsing::defaultValueConversions);
  }

  private static Object defaultValueConversions(String valueAsString, Class<?> type) throws IOException {
    try {
      return switch (type.getName()) {
        case "java.lang.String" -> valueAsString;
        case "char", "java.lang.Character" -> {
          if (valueAsString.length() != 1) {
            throw new IOException("can not convert " + valueAsString + " to a char or a java.lang.Character");
          }
          yield valueAsString.charAt(0);
        }
        case "byte", "java.lang.Byte" -> Byte.parseByte(valueAsString);
        case "short", "java.lang.Short" -> Short.parseShort(valueAsString);
        case "int", "java.lang.Integer" -> Integer.parseInt(valueAsString);
        case "double", "java.lang.Double" -> Double.parseDouble(valueAsString);
        case "long", "java.lang.Long" -> Long.parseLong(valueAsString);
        case "float", "java.lang.Float" -> Float.parseFloat(valueAsString);
        case "java.math.BigInteger" -> new BigInteger(valueAsString);
        case "java.math.BigDecimal" -> new BigDecimal(valueAsString);
        default -> throw new IOException("unknown conversion from " + valueAsString + " to " + type.getName());
      };
    } catch(NumberFormatException e) {
      throw new IOException("invalid conversion from " + valueAsString + " to " + type.getName(), e);
    }
  }

  private static Object defaultListConversion(ArrayList<Object> list, Class<?> type) throws IOException {
    return switch(type.getName()) {
      case "java.util.Collection", "java.util.List", "java.util.ArrayList" -> list;
      case "java.util.Set", "java.util.HashSet", "java.util.LinkedHashSet" -> new LinkedHashSet<>(list);
      default -> throw new IOException("unknown conversion from array to " + type.getName());
    };
  }
}
