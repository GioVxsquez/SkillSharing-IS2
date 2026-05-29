import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class RemoveComments {
    public static void main(String[] args) throws IOException {
        Path srcPath = Paths.get("src/main/java");
        try (Stream<Path> paths = Files.walk(srcPath)) {
            paths.filter(Files::isRegularFile)
                 .filter(p -> p.toString().endsWith(".java"))
                 .forEach(RemoveComments::processFile);
        }
    }

    private static void processFile(Path file) {
        try {
            String content = new String(Files.readAllBytes(file));
            // Eliminar comentarios block /* ... */
            content = content.replaceAll("(?s)/\\*.*?\\*/", "");
            // Eliminar comentarios de linea // ...
            content = content.replaceAll("//.*", "");
            // Eliminar lineas en blanco multiples (opcional pero ayuda)
            content = content.replaceAll("(?m)^[ \t]*\r?\n", "");
            Files.write(file, content.getBytes());
            System.out.println("Procesado: " + file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
