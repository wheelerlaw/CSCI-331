import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Search {

    private static final String CITIES_FILENAME = "city.dat";
    private static final String ROUTES_FILENAME = "edge.dat";

    public static void main(String[] args) throws IOException {
        if (args.length != 2){
            System.err.println("Error: incorrect number of arguments");
            System.exit(1);
        }

        // In the event that we are told to read/write from/to stdin/stdout.
        if (!args[0].trim().equals("-")) {
            System.setIn(new FileInputStream(args[0]));
        }

        if (!args[1].trim().equals("-")) {
            System.setOut(new PrintStream(args[1]));
        }

        // Build the map of stateSpace (hehe). String -> City, where String is the city name
        Map<String, City> cities = Files.lines(Paths.get(CITIES_FILENAME))
            .filter(line -> line.trim().split("\\s+").length == 4)
            .map(City::new)
            .collect(Collectors.toMap(City::name, Function.identity()));

        // Build up the neighbors
        Files.lines(Paths.get(ROUTES_FILENAME)).forEach(line -> {
            String[] tokens = line.trim().split("\\s+");
            if (tokens.length != 2) return;
            cities.get(tokens[0]).neighbors.add(cities.get(tokens[1]));
            cities.get(tokens[1]).neighbors.add(cities.get(tokens[0]));
        });

        // Read input from whatever System.in has been set to
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

        String start = input.readLine();
        String end = input.readLine();

        // Instantiate and execute the algorithms. Print results.
        SearchAlgorithm<City> aStar = new AStar<>(new HashSet<>(cities.values()));
        System.out.println(aStar.execute(cities.get(start), cities.get(end)).resultsString());
    }

}

interface State<S>{
    String name();
    float distanceTo(S o);
    Collection<S> neighbors();
    boolean equals(Object obj);
    int hashCode();
}

class City implements State<City> {
    private final String name;
    private final String state;
    private final float latitude;
    private final float longitude;
    final Set<City> neighbors;

    City(String line){
        String[] tokens = line.split("\\s+");
        this.name = tokens[0];
        this.state = tokens[1];
        this.latitude = Float.parseFloat(tokens[2]);
        this.longitude = Float.parseFloat(tokens[3]);
        this.neighbors = new HashSet<>();
    }

    @Override
    public String name() {
        return this.name;
    }

    public float distanceTo(City city){
        float lat1 = this.latitude;
        float lat2 = city.latitude;
        float lon1 = this.longitude;
        float lon2 = city.longitude;
        return (float)Math.sqrt((lat1 - lat2)*(lat1 - lat2) + (lon1 - lon2)*(lon1 - lon2)) * 100;
    }

    @Override
    public Collection<City> neighbors() {
        return this.neighbors;
    }

    @Override
    public String toString() {
        return name
            + state
            + latitude + "," + longitude
            + this.neighbors.parallelStream()
                .map(city -> city.name.substring(0, 3))
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof City)) return false;
        City city = (City)obj;
        return this.name.equals(city.name) && this.state.equals(city.state);
    }

    @Override
    public int hashCode() {
        return this.name.hashCode() * this.state.hashCode();
    }
}

class AStar<S extends State<S>> extends SearchAlgorithm<S> {
    AStar(Set<S> stateSpace) {
        super(stateSpace);
    }

    @Override
    String getName() {
        return "A*";
    }

    @Override
    public Results<S, SearchAlgorithm<S>> execute(S start, S end) {
        Map<S, Float> h = stateSpace.stream().collect(Collectors.toMap(
            Function.identity(),
            e -> e.distanceTo(end)
        ));

        AStarNode node = new AStarNode(null, start, h);
        SortedSet<AStarNode> frontier = new TreeSet<>();
        frontier.add(node);

        Set<S> explored = new HashSet<>();

        while (!frontier.isEmpty()){
            node = frontier.first();
            frontier.remove(node);

            if (node.state.equals(end)) break;
            explored.add(node.state);

            for (S neighborState: node.state.neighbors()){
                AStarNode child = new AStarNode(node, neighborState, h);
                if (!explored.contains(child.state)){
                    frontier.add(child);
                }
            }
        }

        float length = node.g;

        LinkedList<S> path = new LinkedList<>();
        while (!node.state.equals(node.parent.state)){
            path.addFirst(node.state);
            node = node.parent;
        }
        path.addFirst(node.state);

        return new Results<>(path, length, this);
    }

    private class AStarNode implements Comparable<AStarNode>{
        final AStarNode parent;
        final S state;
        final float stepCost;
        final float h;
        final float g;
        final float f;

        AStarNode(AStarNode parent, S state, Map<S, Float> h){
            if (parent == null) parent = this;
            this.parent = parent;
            this.state = state;
            this.stepCost = parent.state.distanceTo(state);
            this.h = h.get(state);
            this.g = parent.g + this.stepCost;
            this.f = this.g + this.h;
        }

        @Override
        public int compareTo(AStarNode o) {
            return Float.compare(this.f, o.f);
        }
    }

}

abstract class SearchAlgorithm<S extends State<S>> {
    abstract String getName();
    abstract Results<S, SearchAlgorithm<S>> execute(S start, S end);

    final Set<S> stateSpace;

    SearchAlgorithm(Set<S> stateSpace){
        this.stateSpace = stateSpace;
    }


}

class Results<S extends State<S>, A extends SearchAlgorithm<S>> {
    private List<S> hops;
    private float distance;
    private A algorithm;

    String resultsString(){
        StringJoiner sj = new StringJoiner("")
            .add(algorithm.getName())
            .add(" Search Results:\n");
        for(S hop: hops){
            sj.add(hop.name()).add("\n");
        }

        sj.add("That took ").add(String.valueOf(hops.size() - 1)).add(" hops to find.\n");
        sj.add("Total distance = ").add(String.valueOf(Math.round(distance))).add(" miles.");
        return sj.toString();
    }

    Results(List<S> hops, float distance, A algorithm){
        this.hops = hops;
        this.distance = distance;
        this.algorithm = algorithm;
    }
}
