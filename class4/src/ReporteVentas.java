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