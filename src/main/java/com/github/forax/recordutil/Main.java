package com.github.forax.recordutil;

public class Main {
  public static void main(String[] args) {
    record Point(int x, int y) implements MapTrait {}
    var point = new Point(1, 2);
    System.out.println(point.entrySet());

    var list = point.values();
    System.out.println(list);
  }
}
