import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.TreeMap;

public class CalculateAverage {
    private static final Path FILE_PATH = Paths.get("./measurements.txt");
    private static final String DELIMITER = ";";
    private static final double count = 0.0;

    private long parseTemperature(String temperatureStr) {
        boolean isNegative = false;
        if (temperatureStr.charAt(0) == '-') {
            isNegative = true;
        }

        int valor_total = 0;

        for(char c : temperatureStr.toCharArray()) {
            if(Character.isDigit(c) && c != '.') {
                valor_total = (valor_total * 10) + (c - '0');
            }
        }

        long temperature = valor_total;
        if (isNegative) {
            temperature = -temperature;
        }

        return temperature;
    }

    private Map<String, Long[]> readArchive(Path path) throws FileNotFoundException {
        SortedMap<String, Long[]> resultMap = new TreeMap<>();
        File file = path.toFile();
        BufferedReader reader = new BufferedReader(new FileReader(file));

        try (reader) {
            String line;
            while ((line = reader.readLine()) != null) {
                int positionToCrack = line.indexOf(';');
                String city = line.substring(0, positionToCrack);
                long temperature = parseTemperature(line.substring(positionToCrack +1));
                Long[] stats = resultMap.getOrDefault(city, new Long[]{Long.MAX_VALUE, Long.MIN_VALUE, 0L, 0L});
                stats[0] = Math.min(stats[0], temperature);
                stats[1] = Math.max(stats[1], temperature);
                stats[2] += temperature;
                stats[3] += 1;
                resultMap.put(city, stats);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return resultMap;
    }


    public static void main(String[] args) throws FileNotFoundException {
        CalculateAverage calculateAverage = new CalculateAverage();
        long startTime = System.nanoTime();
        Map<String, Long[]> archive = calculateAverage.readArchive(FILE_PATH);

        long endTime = System.nanoTime();

        for(Map.Entry<String, Long[]> entry : archive.entrySet()) {
            String city = entry.getKey();
            Long[] stats = entry.getValue();
            double min = stats[0] / 10.0;
            double max = stats[1] / 10.0;
            double avg = ((double)stats[2] / stats[3]) / 10.0;
            System.out.printf("%s: %.1f/%.1f/%.1f%n", city, min, max, avg);
        }

        long durationInNanos = endTime - startTime;
        double durationInMillis = durationInNanos / 1_000_000.0; // 1 milhão de nanossegundos em 1 milissegundo
        System.out.println("Tempo de processamento: " + durationInMillis + " ms");

    }
}
