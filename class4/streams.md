# Streams en Java — E/S con archivos y Stream API con colecciones

> **Curso:** Programación Orientada a Objetos · API de Java (nivel iniciante)
> **Duración:** 2 h 30 min
> **Requisitos previos:** clases, objetos, herencia, interfaces, `List`/`Map`.

## Cómo usar este material

Cada tema sigue **tres momentos**:

1. 🔍 **Código para analizar** — lee, entiende y predice qué hace. No lo ejecutes todavía: primero razona.
2. 🔧 **Código para mejorar** — código que *funciona pero está mal escrito*. Tu trabajo es refactorizarlo.
3. 🏆 **Desafío completo** — al final, un ejercicio que combina todo lo aprendido.

Las soluciones están ocultas dentro de bloques desplegables (`▶ Ver solución`). **Intenta resolverlo tú primero.**

---

## Índice

- [0. La palabra "stream" significa dos cosas](#0-la-palabra-stream-significa-dos-cosas)
- [Parte 1 — Streams de E/S con archivos](#parte-1--streams-de-es-con-archivos)
  - [1.1 Teoría mínima](#11-teoría-mínima)
  - [1.1b ¿Dónde tiene que estar el archivo?](#11b-dónde-tiene-que-estar-el-archivo-rutas-relativas)
  - [1.2 🔍 Código para analizar](#12--código-para-analizar)
  - [1.3 🔧 Código para mejorar](#13--código-para-mejorar)
- [Parte 2 — Stream API con colecciones](#parte-2--stream-api-con-colecciones)
  - [2.1 Teoría mínima](#21-teoría-mínima)
  - [2.2 🔍 Código para analizar](#22--código-para-analizar)
  - [2.3 🔧 Código para mejorar](#23--código-para-mejorar)
- [Parte 3 — 🏆 Desafío completo](#parte-3---desafío-completo)
- [Apéndice — Chuleta rápida](#apéndice--chuleta-rápida)

---

## 0. La palabra "stream" significa dos cosas

En Java, **"stream" tiene dos significados totalmente distintos**. No los confundas:

| | Flujos de E/S | Stream API |
|---|---|---|
| **Paquete** | `java.io` / `java.nio.file` | `java.util.stream` |
| **Sirve para** | leer/escribir datos externos (archivos, red) | procesar colecciones de datos |
| **Clases típicas** | `BufferedReader`, `FileWriter`, `Files` | `Stream`, `Collectors` |
| **Desde** | Java 1.0 | Java 8 (2014) |

Un **flujo de E/S** es una secuencia de datos que entra o sale de tu programa:

```
  ARCHIVO  ──►  [InputStream / Reader]   ──►  PROGRAMA      (leer)
  PROGRAMA ──►  [OutputStream / Writer]  ──►  ARCHIVO       (escribir)
```

La **Stream API** es una "cinta transportadora" que transforma los elementos de una colección:

```
  coleccion.stream() ──► filter ──► map ──► sorted ──► collect ──► resultado
```

Y al final veremos que **se combinan**: `Files.lines()` lee un archivo y te da directamente un `Stream` para procesarlo.

---

## Parte 1 — Streams de E/S con archivos

### 1.1 Teoría mínima

**Dos familias**, según el tipo de dato:

- **Byte streams** (`InputStream` / `OutputStream`): datos binarios (imágenes, audio).
- **Character streams** (`Reader` / `Writer`): texto. **Es lo que usaremos casi siempre.**

**Clases clave para texto:**

- `FileReader` / `FileWriter`: leen/escriben caracteres de un archivo.
- `BufferedReader` / `BufferedWriter`: **envuelven** a las anteriores para ir más rápido y dar métodos cómodos (`readLine()`, `newLine()`).
- `Files` (`java.nio.file`): la forma **moderna** y corta para tareas comunes.

**La regla de oro:** todo recurso de E/S debe **cerrarse**. Usa siempre **`try-with-resources`**:

```java
try (BufferedReader br = new BufferedReader(new FileReader("datos.txt"))) {
    // usar br...
}   // se cierra automáticamente, incluso si hay error
```

> Casi todos estos métodos lanzan `IOException`: hay que capturarla (`try/catch`) o declararla (`throws`).

---

### 1.1b ¿Dónde tiene que estar el archivo? (rutas relativas)

Una de las dudas más frecuentes: *"¿el archivo debe estar en la misma carpeta que el `.class`?"* **No necesariamente**, y aquí está la trampa que confunde a casi todos.

Cuando pasas una ruta **relativa** como `"lista.txt"`, Java **no** la busca junto al `.class`. La busca en el **directorio de trabajo actual** (*working directory*): la carpeta desde la que **ejecutaste** el comando `java`, no donde está el `.class`.

Ejemplo. Con esta estructura:

```
proyecto/
├── bin/
│   └── MiClase.class
└── lista.txt
```

- ✅ Funciona: `lista.txt` se encuentra aunque el `.class` esté en `bin/`.
  ```
  cd proyecto
  java -cp bin MiClase
  ```
  El *working directory* es `proyecto/`, y ahí está `lista.txt`.

- ❌ Falla con `FileNotFoundException`, aunque el archivo exista en `proyecto/`:
  ```
  cd proyecto/bin
  java MiClase
  ```
  Ahora el *working directory* es `proyecto/bin/`, y ahí no está `lista.txt`.

Para saber **exactamente dónde busca Java** las rutas relativas, imprime el directorio de trabajo:

```java
System.out.println("Directorio actual: " + System.getProperty("user.dir"));
```

Es la mejor forma de depurar el clásico "no encuentra mi archivo".

**Consejos prácticos:**

- En un **IDE** (IntelliJ, Eclipse, VS Code), el *working directory* por defecto suele ser la **raíz del proyecto**, no la carpeta de los `.class`. Por eso un `"lista.txt"` en la raíz del proyecto normalmente funciona al ejecutar desde el IDE.
- Para evitar toda ambigüedad, usa una **ruta absoluta**: `"/home/usuario/proyecto/lista.txt"` (o `C:\\datos\\lista.txt` en Windows). Nota: en Java, la barra invertida se escribe doble (`\\`).
- Regla simple para los ejercicios: **pon el archivo en la raíz del proyecto y ejecuta siempre desde ahí.**

> **Resumen:** el archivo debe estar donde apunta el *working directory*, **no** junto al `.class`. Ante la duda, imprime `user.dir` y deja de adivinar.

---

### 1.2 🔍 Código para analizar

#### Análisis A — ¿Qué imprime y por qué?

```java
import java.io.*;

public class AnalizarA {
    public static void main(String[] args) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader("frase.txt"))) {
            String linea;
            int contador = 0;
            while ((linea = br.readLine()) != null) {
                contador++;
                System.out.println(contador + ": " + linea);
            }
            System.out.println("Total de líneas: " + contador);
        }
    }
}
```

Supón que `frase.txt` contiene:

```
Hola mundo
Java es genial

Hasta luego
```

**Preguntas de análisis:**

1. ¿Qué imprime exactamente el programa?
2. ¿Qué valor tiene `contador` al final? ¿Cuenta la línea en blanco?
3. ¿Por qué la condición del `while` es `!= null` y no `!= -1`?
4. ¿En qué momento exacto se cierra el `BufferedReader`?

<details>
<summary>▶ Ver análisis</summary>

1. Imprime:
   ```
   1: Hola mundo
   2: Java es genial
   3:
   4: Hasta luego
   Total de líneas: 4
   ```
2. `contador` vale **4**. Sí cuenta la línea en blanco: `readLine()` devuelve una cadena vacía `""` (no `null`) para una línea vacía. Solo devuelve `null` al llegar al **fin del archivo**.
3. Porque `readLine()` devuelve un `String` (o `null` al final). El `-1` lo usa `read()`, que devuelve un `int` (un byte/carácter). Son métodos distintos.
4. Se cierra al salir del bloque `try` (llegando a su `}` de cierre, o si se lanza una excepción). Eso es lo que hace `try-with-resources`.

</details>

---

#### Análisis B — Composición de objetos

```java
BufferedReader br = new BufferedReader(new FileReader("datos.txt"));
```

**Preguntas:**

1. ¿Cuántos objetos se crean en esa línea?
2. ¿Cuál "envuelve" a cuál? ¿Qué concepto de POO es este?
3. Si cierro `br` con `br.close()`, ¿se cierra también el `FileReader` interno?

<details>
<summary>▶ Ver análisis</summary>

1. **Dos** objetos: un `FileReader` y un `BufferedReader`.
2. El `BufferedReader` **envuelve** (contiene) al `FileReader`. Es **composición** (POO): un objeto usa a otro para hacer su trabajo. También se le llama patrón *Decorator*.
3. **Sí.** Al cerrar el `BufferedReader`, este cierra en cascada el `FileReader` que contiene. Por eso basta con poner el `BufferedReader` en el `try-with-resources`.

</details>

---

#### Análisis C — La clase `Files`

```java
import java.nio.file.*;
import java.util.List;

public class AnalizarC {
    public static void main(String[] args) throws Exception {
        Path ruta = Path.of("nombres.txt");
        List<String> lineas = Files.readAllLines(ruta);

        System.out.println("Cantidad: " + lineas.size());
        System.out.println("Primera: " + lineas.get(0));
        System.out.println("Última: " + lineas.get(lineas.size() - 1));
    }
}
```

**Preguntas:**

1. ¿Qué ventaja tiene `Files.readAllLines` frente a un bucle con `BufferedReader`?
2. ¿Qué **desventaja** tendría si `nombres.txt` fuera un archivo de 5 GB?
3. Aquí no hay `try-with-resources`. ¿Por qué no pasa nada?

<details>
<summary>▶ Ver análisis</summary>

1. Es mucho **más corto**: en una línea te da toda la lista, sin bucle ni variable temporal.
2. Carga **todo el archivo en memoria** de golpe. Con 5 GB probablemente te quedas sin memoria (`OutOfMemoryError`). Para archivos enormes conviene leer línea por línea (`BufferedReader`) o usar `Files.lines()` como stream.
3. Porque `readAllLines` **abre y cierra el archivo internamente**. No te devuelve un recurso abierto, así que no hay nada que cerrar tú.

</details>

---

### 1.3 🔧 Código para mejorar

Estos programas **funcionan**, pero tienen malas prácticas. Refactorízalos.

#### Mejora 1 — Fuga de recursos

```java
import java.io.*;

public class Mejora1 {
    public static void main(String[] args) throws IOException {
        FileReader fr = new FileReader("datos.txt");
        BufferedReader br = new BufferedReader(fr);

        String linea;
        while ((linea = br.readLine()) != null) {
            System.out.println(linea);
        }
        // ⚠️ nunca se cierra nada
    }
}
```

**Problema:** el archivo nunca se cierra → fuga de recursos.
**Tarea:** reescríbelo usando `try-with-resources`.

<details>
<summary>▶ Ver solución</summary>

```java
import java.io.*;

public class Mejora1 {
    public static void main(String[] args) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader("datos.txt"))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                System.out.println(linea);
            }
        }
    }
}
```

Ya no necesitamos la variable `fr` suelta, y el cierre es automático y seguro.

</details>

---

#### Mejora 2 — Ignorar errores en silencio

```java
import java.io.*;

public class Mejora2 {
    public static void main(String[] args) {
        try {
            BufferedReader br = new BufferedReader(new FileReader("config.txt"));
            System.out.println(br.readLine());
            br.close();
        } catch (Exception e) {
            // no hago nada
        }
    }
}
```

**Problemas:**
- Captura `Exception` genérica en vez de `IOException`.
- Se traga el error sin decir nada → si el archivo no existe, el programa parece funcionar pero no hace nada.
- `close()` manual (mejor `try-with-resources`).

**Tarea:** mejóralo para que sea claro y seguro.

<details>
<summary>▶ Ver solución</summary>

```java
import java.io.*;

public class Mejora2 {
    public static void main(String[] args) {
        try (BufferedReader br = new BufferedReader(new FileReader("config.txt"))) {
            System.out.println(br.readLine());
        } catch (IOException e) {
            System.err.println("No se pudo leer config.txt: " + e.getMessage());
        }
    }
}
```

Captura la excepción **específica** (`IOException`), **informa** del error, y cierra el recurso automáticamente.

</details>

---

#### Mejora 3 — Demasiado código para algo simple

```java
import java.io.*;
import java.util.*;

public class Mejora3 {
    public static void main(String[] args) throws IOException {
        List<String> lineas = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader("lista.txt"));
        String l;
        while ((l = br.readLine()) != null) {
            lineas.add(l);
        }
        br.close();
        System.out.println(lineas);
    }
}
```

**Problema:** todo esto solo para meter las líneas en una lista.
**Tarea:** consíguelo en **una sola línea** usando `Files`.

<details>
<summary>▶ Ver solución</summary>

```java
import java.nio.file.*;
import java.util.List;

public class Mejora3 {
    public static void main(String[] args) throws Exception {
        List<String> lineas = Files.readAllLines(Path.of("lista.txt"));
        System.out.println(lineas);
    }
}
```

`Files.readAllLines` hace el bucle, la lista y el cierre por ti.

</details>

---

## Parte 2 — Stream API con colecciones

### 2.1 Teoría mínima

La **Stream API** procesa colecciones en estilo **declarativo**: dices *qué* quieres, no *cómo* recorrer el bucle.

Un stream tiene **tres partes** (un *pipeline*):

```java
lista.stream()                 // 1. FUENTE
     .filter(n -> n > 10)      // 2. operaciones INTERMEDIAS (devuelven otro Stream)
     .map(n -> n * 2)
     .toList();                // 3. operación TERMINAL (produce el resultado)
```

**Intermedias más usadas:** `filter` (selecciona), `map` (transforma), `sorted` (ordena), `distinct` (quita repetidos), `limit`, `skip`.

**Terminales más usadas:** `toList()` / `collect(...)`, `count()`, `forEach(...)`, `reduce(...)`, `anyMatch(...)`, `findFirst()`.

Reglas importantes:

- Sin operación **terminal**, no se ejecuta nada.
- Un stream es de **un solo uso**.
- Los **lambdas** (`n -> n * 2`) son funciones cortas; `String::toUpperCase` es una *referencia a método* equivalente a `s -> s.toUpperCase()`.

---

### 2.2 🔍 Código para analizar

#### Análisis D — Predecir la salida

```java
import java.util.*;

public class AnalizarD {
    public static void main(String[] args) {
        List<Integer> nums = List.of(5, 3, 8, 3, 1, 8, 10);

        List<Integer> resultado = nums.stream()
            .distinct()
            .filter(n -> n > 3)
            .sorted()
            .map(n -> n * 10)
            .toList();

        System.out.println(resultado);
    }
}
```

**Preguntas:**

1. Sigue el pipeline paso a paso. ¿Qué contiene el stream después de cada operación?
2. ¿Cuál es la lista final que se imprime?
3. Si quito `.toList()`, ¿qué pasa al ejecutar?

<details>
<summary>▶ Ver análisis</summary>

Paso a paso, partiendo de `[5, 3, 8, 3, 1, 8, 10]`:

- `distinct()` → `[5, 3, 8, 1, 10]`
- `filter(n > 3)` → `[5, 8, 10]`
- `sorted()` → `[5, 8, 10]`
- `map(n * 10)` → `[50, 80, 100]`

2. Imprime **`[50, 80, 100]`**.
3. Sin `.toList()` (ni otra terminal), el pipeline **no se ejecuta**: `resultado` ni siquiera compilaría como `List`, porque tendrías un `Stream<Integer>` sin recoger. Un stream necesita una operación terminal para producir algo.

</details>

---

#### Análisis E — `map` vs `filter`

```java
import java.util.*;

public class AnalizarE {
    public static void main(String[] args) {
        List<String> palabras = List.of("sol", "luna", "mar", "estrella", "rio");

        long r1 = palabras.stream().filter(p -> p.length() > 3).count();

        List<Integer> r2 = palabras.stream().map(String::length).toList();

        System.out.println("r1 = " + r1);
        System.out.println("r2 = " + r2);
    }
}
```

**Preguntas:**

1. ¿Qué hace `filter` y qué hace `map`? ¿En qué se diferencian?
2. ¿Cuánto vale `r1`?
3. ¿Qué contiene `r2`?

<details>
<summary>▶ Ver análisis</summary>

1. `filter` **selecciona** elementos según una condición (mismos elementos, menos cantidad). `map` **transforma** cada elemento en otro (misma cantidad, distinto contenido/tipo).
2. `r1 = 3` → las palabras con más de 3 letras: "luna", "estrella", "rio"... espera: "rio" tiene 3 letras, **no** cuenta. Son "luna" (4) y "estrella" (8) → en realidad **`r1 = 2`**. (Ojo con `>` vs `>=`.)
3. `r2 = [3, 4, 3, 8, 3]` → la longitud de cada palabra.

> Lección: cuidado con los límites (`>` vs `>=`). Analizar bien la condición evita errores.

</details>

---

#### Análisis F — Agrupar con `Collectors`

```java
import java.util.*;
import java.util.stream.*;

public class AnalizarF {
    public static void main(String[] args) {
        List<String> nombres = List.of("Ana", "Luis", "Eva", "Adán", "Lucía");

        Map<Character, List<String>> porInicial = nombres.stream()
            .collect(Collectors.groupingBy(n -> n.charAt(0)));

        System.out.println(porInicial);
    }
}
```

**Preguntas:**

1. ¿Qué tipo de estructura produce `groupingBy`?
2. ¿Cuál es el resultado?
3. ¿Cómo cambiarías el código para obtener *cuántos* nombres hay por inicial, en vez de la lista?

<details>
<summary>▶ Ver análisis</summary>

1. Un `Map` donde la **clave** es el criterio de agrupación (aquí, la inicial) y el **valor** es la lista de elementos de ese grupo.
2. `{A=[Ana, Adán], L=[Luis, Lucía], E=[Eva]}` (el orden puede variar).
3. Añadiendo un segundo argumento `Collectors.counting()`:
   ```java
   Map<Character, Long> conteo = nombres.stream()
       .collect(Collectors.groupingBy(n -> n.charAt(0), Collectors.counting()));
   // {A=2, L=2, E=1}
   ```

</details>

---

### 2.3 🔧 Código para mejorar

Estos programas usan bucles imperativos que quedan mucho mejor con la Stream API.

#### Mejora 4 — Filtrar y transformar con un bucle

```java
import java.util.*;

public class Mejora4 {
    public static void main(String[] args) {
        List<Integer> nums = List.of(4, 7, 2, 9, 12, 5, 8);
        List<Integer> resultado = new ArrayList<>();

        for (Integer n : nums) {
            if (n % 2 == 0) {
                resultado.add(n * n);
            }
        }
        Collections.sort(resultado);
        System.out.println(resultado);
    }
}
```

**Tarea:** reescribe la lógica (pares → al cuadrado → ordenados) con un stream.

<details>
<summary>▶ Ver solución</summary>

```java
import java.util.*;

public class Mejora4 {
    public static void main(String[] args) {
        List<Integer> nums = List.of(4, 7, 2, 9, 12, 5, 8);

        List<Integer> resultado = nums.stream()
            .filter(n -> n % 2 == 0)
            .map(n -> n * n)
            .sorted()
            .toList();

        System.out.println(resultado);   // [4, 16, 64, 144]
    }
}
```

Más corto y más legible: se lee casi como una frase.

</details>

---

#### Mejora 5 — Sumar con acumulador

```java
import java.util.*;

public class Mejora5 {
    public static void main(String[] args) {
        List<Integer> precios = List.of(120, 89, 45, 200, 15);
        int total = 0;
        for (int p : precios) {
            if (p > 50) {
                total += p;
            }
        }
        System.out.println("Total (> 50): " + total);
    }
}
```

**Tarea:** consigue la suma de los precios mayores que 50 con un stream.

<details>
<summary>▶ Ver solución</summary>

```java
import java.util.*;

public class Mejora5 {
    public static void main(String[] args) {
        List<Integer> precios = List.of(120, 89, 45, 200, 15);

        int total = precios.stream()
            .filter(p -> p > 50)
            .mapToInt(Integer::intValue)   // pasamos a IntStream
            .sum();

        System.out.println("Total (> 50): " + total);   // 409
    }
}
```

`mapToInt` convierte a un `IntStream`, que tiene el método `sum()` directo.

</details>

---

#### Mejora 6 — Construir un texto a mano

```java
import java.util.*;

public class Mejora6 {
    public static void main(String[] args) {
        List<String> etiquetas = List.of("java", "streams", "io", "api");
        String resultado = "";
        for (int i = 0; i < etiquetas.size(); i++) {
            resultado += "#" + etiquetas.get(i).toUpperCase();
            if (i < etiquetas.size() - 1) {
                resultado += " ";
            }
        }
        System.out.println(resultado);
    }
}
```

**Tarea:** genera `#JAVA #STREAMS #IO #API` usando `map` + `Collectors.joining`.

<details>
<summary>▶ Ver solución</summary>

```java
import java.util.*;
import static java.util.stream.Collectors.joining;

public class Mejora6 {
    public static void main(String[] args) {
        List<String> etiquetas = List.of("java", "streams", "io", "api");

        String resultado = etiquetas.stream()
            .map(e -> "#" + e.toUpperCase())
            .collect(joining(" "));

        System.out.println(resultado);   // #JAVA #STREAMS #IO #API
    }
}
```

`joining(" ")` se encarga de poner el separador solo *entre* elementos: adiós al `if (i < size - 1)`.

</details>

---

## Parte 3 — 🏆 Desafío completo

> Este desafío **combina las dos partes**: leer un archivo (E/S) y procesarlo con la Stream API.

### Contexto

Tienes un archivo **`ventas.csv`** con las ventas de una tienda. Cada línea tiene el formato:

```
vendedor;producto;cantidad;precio_unitario
```

Contenido de ejemplo:

```
Ana;Teclado;2;25.00
Luis;Monitor;1;150.00
Ana;Ratón;3;10.50
Eva;Monitor;2;150.00
Luis;Teclado;1;25.00
Ana;Monitor;1;150.00
Eva;Ratón;5;10.50
```

### Lo que debe hacer tu programa

1. **Leer** el archivo `ventas.csv` usando `Files.lines()` dentro de `try-with-resources`.
2. Ignorar líneas en blanco.
3. Para cada línea, calcular el **importe** de la venta = `cantidad × precio_unitario`.
4. Calcular el **total facturado por cada vendedor** (agrupando por vendedor).
5. Encontrar el **vendedor con mayor facturación**.
6. **Escribir** un archivo `reporte.txt` con:
   - Una línea por vendedor: `Vendedor: total€`
   - Al final, una línea: `MEJOR VENDEDOR: nombre (total€)`

### Salida esperada (según el ejemplo)

Cálculos:
- Ana: 2×25 + 3×10.50 + 1×150 = 50 + 31.5 + 150 = **231.5**
- Luis: 1×150 + 1×25 = **175.0**
- Eva: 2×150 + 5×10.50 = 300 + 52.5 = **352.5**

`reporte.txt`:
```
Ana: 231.5€
Luis: 175.0€
Eva: 352.5€
MEJOR VENDEDOR: Eva (352.5€)
```

### Pistas

- Divide cada línea con `linea.split(";")`.
- Usa `Collectors.groupingBy(..., Collectors.summingDouble(...))` para el total por vendedor.
- Para el mejor vendedor, recorre el `Map` con un stream y usa `max(Map.Entry.comparingByValue())`.
- Para escribir: construye una `List<String>` y usa `Files.write(...)`.

<details>
<summary>▶ Ver una solución completa</summary>

```java
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class ReporteVentas {
    public static void main(String[] args) throws IOException {

        // 1-4. Leer archivo y agrupar total por vendedor
        Map<String, Double> totalPorVendedor;
        try (Stream<String> lineas = Files.lines(Path.of("ventas.csv"))) {
            totalPorVendedor = lineas
                .filter(l -> !l.isBlank())
                .map(l -> l.split(";"))
                .collect(Collectors.groupingBy(
                    campos -> campos[0],                                   // vendedor
                    Collectors.summingDouble(campos ->
                        Integer.parseInt(campos[2]) * Double.parseDouble(campos[3]))
                ));
        }

        // 5. Mejor vendedor
        Optional<Map.Entry<String, Double>> mejor = totalPorVendedor.entrySet().stream()
            .max(Map.Entry.comparingByValue());

        // 6. Construir las líneas del reporte
        List<String> reporte = new ArrayList<>();
        totalPorVendedor.forEach((vendedor, total) ->
            reporte.add(vendedor + ": " + total + "€"));

        mejor.ifPresent(e ->
            reporte.add("MEJOR VENDEDOR: " + e.getKey() + " (" + e.getValue() + "€)"));

        // Escribir el archivo
        Files.write(Path.of("reporte.txt"), reporte);

        System.out.println("Reporte generado. Contenido:");
        reporte.forEach(System.out::println);
    }
}
```

**Qué se usa de cada parte:**
- E/S con archivos: `Files.lines` (lectura), `try-with-resources`, `Files.write` (escritura).
- Stream API: `filter`, `map`, `collect` + `groupingBy` + `summingDouble`, `max`, `forEach`.

**Variantes para mejorar aún más (opcional):**
- Definir una clase `Venta` (POO) con `vendedor`, `producto`, `cantidad`, `precio` y un método `getImporte()`, y mapear cada línea a un objeto `Venta` en vez de a un `String[]`. Queda más limpio y orientado a objetos.
- Formatear los importes a 2 decimales con `String.format("%.2f", total)`.

</details>

---

## Apéndice — Chuleta rápida

### E/S con archivos

```java
// Leer línea por línea (archivos grandes)
try (BufferedReader br = new BufferedReader(new FileReader("a.txt"))) {
    String l;
    while ((l = br.readLine()) != null) { /* ... */ }
}

// Leer archivo pequeño entero
List<String> lineas = Files.readAllLines(Path.of("a.txt"));
String texto        = Files.readString(Path.of("a.txt"));

// Escribir
Files.writeString(Path.of("out.txt"), "contenido");
Files.write(Path.of("out.txt"), List.of("l1", "l2"));
try (BufferedWriter bw = new BufferedWriter(new FileWriter("out.txt", true))) { // append
    bw.write("línea"); bw.newLine();
}
```

### Stream API

```java
coleccion.stream()
    .filter(x -> condicion)        // seleccionar
    .map(x -> transformar(x))      // transformar
    .distinct().sorted()           // sin repetidos, ordenar
    .limit(5)                      // primeros 5
    // --- terminales ---
    .toList();                     // → List
long   c = ...stream().count();
boolean b = ...stream().anyMatch(x -> ...);
int    s = ...stream().mapToInt(x -> x).sum();
String u = ...stream().collect(Collectors.joining(", "));
Map<K, List<V>> g = ...stream().collect(Collectors.groupingBy(v -> clave(v)));
```

### Puente entre ambos

```java
try (Stream<String> lineas = Files.lines(Path.of("datos.txt"))) {
    lineas.filter(...).map(...).forEach(...);
}   // Files.lines abre el archivo → hay que cerrarlo (try-with-resources)
```

### Errores frecuentes a evitar

- No cerrar recursos → usa **`try-with-resources`** siempre.
- Reutilizar un `Stream` (es de un solo uso).
- Olvidar la operación **terminal** (sin ella no pasa nada).
- Confundir `map` (transforma) con `filter` (selecciona).
- `new FileWriter("x.txt")` **sobrescribe**; usa `new FileWriter("x.txt", true)` para añadir.