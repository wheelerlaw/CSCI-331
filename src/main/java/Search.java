import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Search {

    private static final String CITIES_FILENAME = "city.dat";
    private static final String ROUTES_FILENAME = "edge.dat";

    private static final boolean develop = true;

    public static void main(String[] args) throws IOException {
        if (args.length != 2){
            System.err.println("Error: incorrect number of arguments");
        }

        if (!develop){
            System.setOut(new PrintStream(args[1]));
        }

        // Build the map of cities (hehe). String -> City, where String is the city name
        Map<String, City> cities = Files.lines(Paths.get(CITIES_FILENAME))
            .filter(line -> line.trim().split("\\s+").length == 4)
            .map(City::new)
            .collect(Collectors.toMap(c -> c.name, Function.identity()));

        // Build up the neighbors
        Files.lines(Paths.get(ROUTES_FILENAME)).forEach(line -> {
            String[] tokens = line.trim().split("\\s+");
            if (tokens.length != 2) return;
            cities.get(tokens[0]).neighbors.put(tokens[1], cities.get(tokens[1]));
            cities.get(tokens[1]).neighbors.put(tokens[0], cities.get(tokens[0]));
        });

        System.out.println(cities);
    }
}

class City {
    public final String name;
    public final String state;
    public final float latitude;
    public final float longitude;
    public final Map<String, City> neighbors;

    City(String line){
        String[] tokens = line.split("\\s+");
        this.name = tokens[0];
        this.state = tokens[1];
        this.latitude = Float.parseFloat(tokens[2]);
        this.longitude = Float.parseFloat(tokens[3]);
        this.neighbors = new TreeMap<>();
    }

    @Override
    public String toString() {
        return name
            + state
            + latitude + "," + longitude
            + this.neighbors.keySet().parallelStream()
                .map(name -> name.substring(0, 3))
                .collect(Collectors.toList());
    }
}

class Results {

}
