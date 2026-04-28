package com.github.forax.javawebcompiler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public final class ApplicationTest {
  @Test
  public void compileValidCode() {
    var version = Runtime.Version.parse("25");
    var code = """
      public class Main {
        public static void main(String[] args) {
          System.out.println("Hello");
        }
      }
      """;
    var result = Compiler.compileInMemory("Main", code, new MemoryClassLoader(), version);
    assertTrue(result.isEmpty());
  }

  @Test
  public void diagnosticContainsLineAndColumn() {
    var version = Runtime.Version.parse("25");
    var source = """
      public class Main {
        public static void main(String[] args) {
          int x = "ksdjfj";
        }
      }
    """;
    var diagnostics = Compiler.compileInMemory("Main", source, new MemoryClassLoader(), version);
    assertFalse(diagnostics.isEmpty());
    var first = diagnostics.getFirst();
    assertTrue(first.line() > 0);
    assertTrue(first.column() > 0);
    assertNotNull(first.message());
    assertFalse(first.message().isEmpty());
  }

    @Test
    public void runHelloWorld() throws Exception {
      var version = Runtime.Version.parse("25");
      var code = """
        public class Main {
          public static void main(String[] args) {
            System.out.println("Hello");
          }
        }
      """;
      var loader = new MemoryClassLoader();
      Compiler.compileInMemory("Main", code, loader, version);
      var output = Runner.runFromMemory("Main", loader);
      assertEquals("Hello\n", output);
    }

    @Test
    public void runWithNoCompiledCode() {
      var loader = new MemoryClassLoader();
      assertThrows(ClassNotFoundException.class, () -> Runner.runFromMemory("Main", loader));
    }

    @Test
    public void runMultipleLines() throws Exception {
      var version = Runtime.Version.parse("25");
      var code = """
        public class Main {
          public static void main(String[] args) {
          System.out.println("line1");
          System.out.println("line2");
          }
        }
      """;
      var loader = new MemoryClassLoader();
      Compiler.compileInMemory("Main", code, loader, version);
      var output = Runner.runFromMemory("Main", loader);
      assertEquals("line1\nline2\n", output);
    }

    @Test
    public void runEmptyOutput() throws Exception {
      var version = Runtime.Version.parse("25");
      var code = """
        public class Main {
          public static void main(String[] args) {}
        }
      """;
      var loader = new MemoryClassLoader();
      Compiler.compileInMemory("Main", code, loader, version);
      var output = Runner.runFromMemory("Main", loader);
      assertEquals("", output);
    }

  @Test
  public void compileWithDifferentClassName() {
    var version = Runtime.Version.parse("25");
    var code = """
      public class Test {
        public static void main(String[] args) {
        }
      }
      """;
    var loader = new MemoryClassLoader();
    var diagnostics = Compiler.compileInMemory("Test", code,loader, version);

    assertTrue(diagnostics.isEmpty());
  }

  @Test
  public void compileWithWrongClassNameShouldFail() {
    var version = Runtime.Version.parse("25");
    var code = """
      public class Test {
      }
      """;

    var loader = new MemoryClassLoader();
    var diagnostics = Compiler.compileInMemory("Main", code,loader, version);

    assertFalse(diagnostics.isEmpty());
  }

  @Test
  public void compileClassWithoutMainShouldStillCompile() {
    var version = Runtime.Version.parse("25");
    var code = """
        public class Test {
            int x = 10;
        }
        """;

    var loader = new MemoryClassLoader();
    var diagnostics = Compiler.compileInMemory("Test", code,loader, version);

    assertTrue(diagnostics.isEmpty());
  }
  @Test
  public void compileWrongCodeWithMultipleErrors(){
    var version = Runtime.Version.parse("25");
    var code = """
        public class Main {
            System.out.println("Hello");
            int a = "";
        }
        """;
    var loader = new MemoryClassLoader();
    var diagnostics = Compiler.compileInMemory("Main", code,loader, version);
    assertFalse(diagnostics.isEmpty());
    assertEquals(2, diagnostics.size());
  }

  @Test
  public void compileDifferentJavaVersion(){
    var version = Runtime.Version.parse("17");;
    var code = """
        public class Main {
          public static void main(String[] args) {
            var a = "One" ;
            switch (a) {
              case String s -> System.out.println(s.toLowerCase());
              default -> System.out.println("Hello");
            }
          }
        }
        """;
    var compilerResult = Compiler.compileInMemory("Main", code, new MemoryClassLoader(), version);
    assertFalse(compilerResult.isEmpty());
    assertEquals(1, compilerResult.size()); //cannot pattern matching in java 17
  }
}
