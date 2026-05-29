import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class StripCommentsKeepHU {
    public static void main(String[] args) throws IOException {
        Path srcPath = Paths.get("src/main/java");
        try (Stream<Path> paths = Files.walk(srcPath)) {
            paths.filter(Files::isRegularFile)
                 .filter(p -> p.toString().endsWith(".java"))
                 .forEach(StripCommentsKeepHU::processFile);
        }
    }

    private static void processFile(Path file) {
        try {
            String content = new String(Files.readAllBytes(file));
            
            // Eliminar comentarios de bloque /* ... */
            content = content.replaceAll("(?s)/\\*.*?\\*/", "");
            
            // Eliminar comentarios de linea // que NO contengan hu o HU
            // Regex: // seguido de cualquier cosa que no sea fin de linea, pero si NO contiene "hu" ni "HU"
            Pattern pattern = Pattern.compile("//.*");
            Matcher matcher = pattern.matcher(content);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String match = matcher.group();
                if (match.toLowerCase().contains("hu")) {
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(match));
                } else {
                    matcher.appendReplacement(sb, "");
                }
            }
            matcher.appendTail(sb);
            content = sb.toString();
            
            // Eliminar lineas en blanco multiples
            content = content.replaceAll("(?m)^[ \t]*\r?\n", "");
            
            Files.write(file, content.getBytes());
            System.out.println("Procesado: " + file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
