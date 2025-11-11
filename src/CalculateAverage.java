import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class CalculateAverage {
    private static final Path FILE_PATH = Paths.get("./measurements.txt");
    private static final String DELIMITER = ";";
    private static final double count = 0.0;

    private long parseTemperature(String temperatureStr) {
        boolean isNegative = temperatureStr.charAt(0) == '-';

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

    private Map<String, Long[]> readArchive(Path path) throws IOException {
        SortedMap<String, Long[]> resultMap = new TreeMap<>();

        try(FileInputStream fileInputStream = new FileInputStream(path.toFile())) {
            byte[] buffer = new byte[8192]; // 8KB
            int readedBytes = 0;

            boolean isReadingCity = true;
            long actualTemperature = 0;
            boolean isNegative = false;

            ByteArrayOutputStream cityBuffer = new ByteArrayOutputStream();

            while((readedBytes = fileInputStream.read(buffer)) != -1) {
                for (int i = 0; i < readedBytes; i++) {
                    byte b = buffer[i];

                    if(isReadingCity) {
                        if(b == 59) { // ';'
                            isReadingCity = false;
                        }else{
                            cityBuffer.write(b);

                        }
                    }else{
                        if (b == 10) { // '\n' (FIM DA LINHA)
                            long finalTemperature = isNegative ? -actualTemperature : actualTemperature;
                            String city = cityBuffer.toString();

                            Long[] stats = resultMap.getOrDefault(city, new Long[]{Long.MAX_VALUE, Long.MIN_VALUE, 0L, 0L});
                            stats[0] = Math.min(stats[0], finalTemperature);
                            stats[1] = Math.max(stats[1], finalTemperature);
                            stats[2] += finalTemperature;
                            stats[3] += 1;
                            resultMap.put(city, stats);

                            isReadingCity = true;
                            cityBuffer.reset();
                            actualTemperature = 0;
                            isNegative = false;

                        } else if (b == 45) { // '-'
                            isNegative = true;

                        } else if (b != 46) { // ('.')
                            int digit = b - '0';
                            actualTemperature = (actualTemperature * 10) + digit;
                        }
                    }

                }
            }


        }catch (IOException e) {
            throw new RuntimeException(e);
        }

        return resultMap;
    }


    public static void main(String[] args) throws IOException {
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
