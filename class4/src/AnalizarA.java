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