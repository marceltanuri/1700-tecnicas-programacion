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