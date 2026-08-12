import java.nio.file.*;
import java.util.List;

public class AnalizarC {
    public static void main(String[] args) throws Exception {
        Path ruta = Path.of("nombres.txt");
        List<String> lineas = Files.readAllLines(ruta);

        System.out.println("Cantidad: " + lineas.size());
        System.out.println("Primera: " + lineas.get(0));
        System.out.println("Última: " + lineas.get(lineas.size() - 1));

        for (String linea : lineas) {
            System.out.println(linea);
        }
    }
}