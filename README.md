# record-util
Some utility classes around java records

### On the menu

- MapTrait
  
  Transform any record to a `java.util.Map` just by implementing the interface `MapTrait`
  ```java
    record Person(String name, int age) implements MapTrait {}
    ...
    Map<String, Object> map = new Person("Bob", 42);
  ```

- WithTrait

  Implementing the interface `WithTrait` adds several methods `with` that allow to duplicate
  a record instance and update several record components in the process
  ```java
  record Person(String name, int age) implements WithTrait<Person> {}
  ...
  var bob = new Person("Bob", 42);
  var ana = bob.with("name", "Ana");
  ```

- Wither
  
  A very fast but more cumbersome way to duplicate/update a record instance
  ```java
  record Person(String name, int age) {}
  ...
  private static final Wither<Person> wither = Wither.of(MethodHandles.lookup(), Person.class);
  ...
  var bob = new Person("Bob", 42);
  var ana = wither.with(bob, "name", "Ana");
  ```

- JSONTrait

  Implementing the interface `JSONTrait` adds a method `toJSON` that
  enable to output a record instance using the JSON format
  ```java
  record Person(String name, int age) implements JSONTrait { }
  ...
  var person = new Person("Bob", 42);
  System.out.println(person.toJSON());
  ```

### How to build
```
  mvn package
```
