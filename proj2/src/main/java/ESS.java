import java.io.*;
import java.util.*;

/**
 * Main execution class.
 */
public class ESS {

//    private static final String CITIES_FILENAME = "city.dat";
//    private static final String ROUTES_FILENAME = "edge.dat";

    private static final String PROMPT_TEXT =
        "===============MENU=============\n" +
        "1 ) Starting Stats\n" +
        "2 ) Display Individuals and Points\n" +
        "3 ) Display Sorted\n" +
        "4 ) Have 1000 interactions\n" +
        "5 ) Have 10000 interactions\n" +
        "6 ) Have N interactions\n" +
        "7 ) Step through interactions \"Stop\" to return to menu\n" +
        "8 ) Quit\n" +
        "================================";

    private static final String STARTING_STATS =
        "Population size: %d%n" +
        "Percentage of Hawks: %d%%%n%n" +
        "Number of Hawks: %d%n" +

        "Percentage of Doves: %d%%%n" +
        "Number of Doves: %d%n%n" +

        "Each resource is worth: %d%n" +
        "Cost of Hawk-Hawk interaction: %d";

    public static void main(String[] args) throws IOException {

        if (args.length < 1 || args.length > 4){
            System.err.println("Usage: ./project02 popSize [percentHawks] [resourceAmt] [costHawk-Hawk]");
            System.exit(1);
        }

        int populationSize = Integer.parseInt(args[0]);

        int percentHawks = 20;
        if (args.length >= 2) {
            percentHawks = Integer.parseInt(args[1]);
        }

        int resourceAmount = 50;
        if (args.length >= 3) {
            resourceAmount = Integer.parseInt(args[2]);
        }

        int hawkHawkCost = 100;
        if (args.length == 4) {
            hawkHawkCost = Integer.parseInt(args[3]);
        }

        int numHawks = (int) (populationSize * (percentHawks / 100f));
        int percentDoves = 100 - percentHawks;
        int numDoves = populationSize - numHawks;

        List<Individual> population = new ArrayList<>();

        for(int i = 0; i < numHawks; i++){
            population.add(new Individual(Individual.Strategy.HAWK));
        }
        for(int i = 0; i < numDoves; i++){
            population.add(new Individual(Individual.Strategy.DOVE));
        }

        population.get(1).resources = 5;

        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

        System.out.println(PROMPT_TEXT);
        System.out.print("> ");
        String line = input.readLine();

        switch (line){
            case "1":
                System.out.println(getStartingStats(populationSize, percentHawks, resourceAmount, hawkHawkCost));
                break;
            case "2":
                System.out.println(getPopulationDetails(population));
                break;
            case "3":
                System.out.println(getPopulationSummary(population));
                break;
        }

    }

    private static String getStartingStats(int populationSize, int percentHawks, int resourceAmount, int hawkHawkCost){
        int numHawks = (int) (populationSize * (percentHawks / 100f));
        int percentDoves = 100 - percentHawks;
        int numDoves = populationSize - numHawks;
        return String.format(STARTING_STATS, populationSize, percentHawks, numHawks, percentDoves, numDoves, resourceAmount, hawkHawkCost);
    }

    private static String getPopulationDetails(List<Individual> population){
        StringJoiner sj = new StringJoiner("", "", "");
        int livingCount = population.size();

        for(int i = 0; i < population.size(); i++){
            Individual individual = population.get(i);
            String strategy = individual.strategy.name;

            if (individual.resources < 0) {
                strategy = "DEAD";
                livingCount--;
            }

            sj.add("Individual[")
                .add(String.valueOf(i))
                .add("]=")
                .add(strategy)
                .add(":")
                .add(String.valueOf(individual.resources))
                .add("\n");
        }

        sj.add("Living: ").add(String.valueOf(livingCount));

        return sj.toString();
    }

    private static String getPopulationSummary(List<Individual> population){
        StringJoiner sj = new StringJoiner("", "", "");
        population.sort((a, b) -> Integer.compare(b.resources, a.resources));

        for(Individual individual: population){
            String strategy = individual.strategy.name;
            if (individual.resources < 0) {
                strategy = "DEAD";
            }
            sj.add(strategy).add(":").add(String.valueOf(individual.resources)).add("\n");
        }

        return sj.toString();
    }

    private static void runInteraction(List<Individual> population, int resourceAmount){
        Random random = new Random();
        int individual1Index = random.nextInt(population.size());
        int individual2Index = random.nextInt(population.size() - 1);

        if (individual2Index == individual1Index){
            individual2Index += 1;
        }

        Individual individual1 = population.get(individual1Index);
        Individual individual2 = population.get(individual2Index);

        if (individual1.strategy == Individual.Strategy.DOVE && individual2.strategy == Individual.Strategy.DOVE) {
            individual1.resources += resourceAmount / 2;
            individual2.resources += resourceAmount / 2;
        } else if (individual1.strategy == Individual.Strategy.HAWK && individual2.strategy == Individual.Strategy.DOVE) {
            individual1.resources += resourceAmount;
        } else if (individual2.strategy == Individual.Strategy.HAWK && individual1.strategy == Individual.Strategy.DOVE) {
            individual2.resources += resourceAmount;
        } else if (individual1.strategy == Individual.Strategy.HAWK && individual2.strategy == Individual.Strategy.HAWK) {

        }


    }

}

class Individual {
    int resources;
    Strategy strategy;

    public Individual(Strategy strategy){
        this.strategy = strategy;
        this.resources = 0;
    }

    enum Strategy {
        DOVE("Dove"), HAWK("Hawk");

        Strategy(String name) {
            this.name = name;
        }
        final String name;
    }
}

//
//        // Test that the specified files are available.
//        try {
//            Files.newBufferedReader(Paths.get(CITIES_FILENAME));
//            Files.newBufferedReader(Paths.get(ROUTES_FILENAME));
//            if (!args[0].trim().equals("-")) {
//                Files.newBufferedReader(Paths.get(args[0]));
//            }
//        } catch (NoSuchFileException e){
//            System.err.println("File not found: " + e.getMessage());
//            System.exit(1);
//        }
//
//        // In the event that we are told to read/write from/to stdin/stdout.
//        if (!args[0].trim().equals("-")) {
//            System.setIn(new FileInputStream(args[0]));
//        }
//
//        if (!args[1].trim().equals("-")) {
//            System.setOut(new PrintStream(args[1]));
//        }
//
//        // Build the map of stateSpace (hehe). String -> City, where String is the city name
//        Map<String, City> cities = Files.lines(Paths.get(CITIES_FILENAME))
//            .filter(line -> line.trim().split("\\s+").length == 4)
//            .map(City::new)
//            .collect(Collectors.toMap(City::name, Function.identity()));
//
//        // Build up the neighbors
//        Files.lines(Paths.get(ROUTES_FILENAME)).forEach(line -> {
//            String[] tokens = line.trim().split("\\s+");
//            if (tokens.length != 2) return;
//            cities.get(tokens[0]).neighbors.add(cities.get(tokens[1]));
//            cities.get(tokens[1]).neighbors.add(cities.get(tokens[0]));
//        });
//
//        // Read input from whatever System.in has been set to
//        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
//
//        String startName = input.readLine();
//        String endName = input.readLine();
//
//        City start = cities.get(startName);
//        City end = cities.get(endName);
//
//        if (start == null){
//            System.err.println("No such city: (" + startName + ")");
//            System.exit(1);
//        }
//
//        if (end == null){
//            System.err.println("No such city: (" + endName + ")");
//            System.exit(1);
//        }
//
//        // Instantiate and execute the algorithms. Print results.
//        SearchAlgorithm<City> bfs = new BreadthFirst<>(new HashSet<>(cities.values()));
//        System.out.println(bfs.execute(start, end).resultsString());
//
//        SearchAlgorithm<City> dfs = new DepthFirst<>(new HashSet<>(cities.values()));
//        System.out.println(dfs.execute(start, end).resultsString());
//
//        SearchAlgorithm<City> aStar = new AStar<>(new HashSet<>(cities.values()));
//        System.out.println(aStar.execute(start, end).resultsString());


///**
// * Generic representation for a state in a statespace. Parameterized such that we don't lose the type information on
// * this interface's function definitions due to type erasure in Java.
// * @param <S> The type of the state.
// */
//interface State<S>{
//
//    /**
//     * Get the name of this state.
//     * @return
//     */
//    String name();
//
//    /**
//     * Calculate the direct distance between this state and some other arbitrary state.
//     * @param o
//     * @return
//     */
//    float distanceTo(S o);
//
//    /**
//     * Return a NavigableSet of neighbors of this state. This makes the strong assumption that there
//     * is only one direct connection between any two states that are directly connected (i.e no two
//     * states can have multiple direct paths between them).
//     * @return NavigableSet of neighbors.
//     */
//    NavigableSet<S> neighbors();
//
//    /**
//     * Determine if an instance of this state is equal to some arbitrary object.
//     * @param obj The object to determine the equality between.
//     * @return Whether this state is equal to the other object.
//     */
//    boolean equals(Object obj);
//
//    /**
//     * Calculate the hashcode of this state. Used for when this state is stored in any
//     * type of hashing collection.
//     * @return The hashcode as an integer.
//     */
//    int hashCode();
//}
//
///**
// * An implementation of a state in a statespace, representing a City located by coordinates.
// */
//class City implements State<City> {
//    private final String name;
//    private final String state;
//    private final float latitude;
//    private final float longitude;
//    final NavigableSet<City> neighbors;
//
//    /**
//     * Create an instance of a City from a string with 4 tokens separated by whitespace.
//     * @param line
//     */
//    City(String line){
//        String[] tokens = line.split("\\s+");
//        this.name = tokens[0];
//        this.state = tokens[1];
//        this.latitude = Float.parseFloat(tokens[2]);
//        this.longitude = Float.parseFloat(tokens[3]);
//        this.neighbors = new TreeSet<>(Comparator.comparing(City::name));
//    }
//
//    /**
//     * Return the name of this state, in this case it is the name of the city.
//     * @return
//     */
//    @Override
//    public String name() {
//        return this.name;
//    }
//
//    /**
//     * Calculate the distance of this city from the distance of another using the coordinates provided in both.
//     * @param city The other city to find the distance to.
//     * @return The distance to the other city.
//     */
//    @Override
//    public float distanceTo(City city){
//        float lat1 = this.latitude;
//        float lat2 = city.latitude;
//        float lon1 = this.longitude;
//        float lon2 = city.longitude;
//        return (float)Math.sqrt((lat1 - lat2)*(lat1 - lat2) + (lon1 - lon2)*(lon1 - lon2)) * 100;
//    }
//
//    /**
//     * Get a collection of the other neighbors to this city.
//     * @return The collection of neighbors.
//     */
//    @Override
//    public NavigableSet<City> neighbors() {
//        return this.neighbors;
//    }
//
//    /**
//     * Return a string representation of this city, which is useful for debugging (either interactively or by using
//     * print statements).
//     * @return The string representation of the city.
//     */
//    @Override
//    public String toString() {
//        return name
//            + state
//            + latitude + "," + longitude
//            + this.neighbors.parallelStream()
//            .map(city -> city.name.substring(0, 3))
//            .collect(Collectors.toList());
//    }
//
//    /**
//     * Determine if this city is equal to another city by comparing the city names and states.
//     * @param obj The other city to compare against.
//     * @return True if the cities have the same name and state. False if they don't, or are not the same type.
//     */
//    @Override
//    public boolean equals(Object obj) {
//        if (!(obj instanceof City)) return false;
//        City city = (City)obj;
//        return this.name.equals(city.name) && this.state.equals(city.state);
//    }
//
//    /**
//     * Calculate the hashcode for this city, which is useful for storing it inside of a HashSet or HashMap.
//     * @return The hashcode of the name multiplied by the hashcode of the state.
//     */
//    @Override
//    public int hashCode() {
//        return this.name.hashCode() * this.state.hashCode();
//    }
//}
//
///**
// * A generic search algorithm.
// * @param <S> The type of state that makes up the statespace in which this search algorithm will search.
// */
//abstract class SearchAlgorithm<S extends State<S>> {
//    /**
//     * Get the name of the algorithm extending this abstract search algorithm.
//     * @return The name of the search algorithm.
//     */
//    abstract String getName();
//
//    /**
//     * Executes the algorithm and returns the results.
//     * @param start The start state.
//     * @param end The end state.
//     * @return Results containing the list of hops on the path that the algorithm found, the length of the path,
//     *         and some other relevant information.
//     */
//    abstract Results<S, SearchAlgorithm<S>> execute(S start, S end);
//
//    final Set<S> stateSpace;
//
//    /**
//     * Default constructor that implementing algorithms will call.
//     * @param stateSpace
//     */
//    SearchAlgorithm(Set<S> stateSpace){
//        this.stateSpace = stateSpace;
//    }
//
//    Results<S, SearchAlgorithm<S>> generateResults(SearchNode end){
//        float length = end.g;
//
//        LinkedList<S> path = new LinkedList<>();
//        while (!end.state.equals(end.parent.state)) {
//            path.addFirst(end.state);
//            end = end.parent;
//        }
//        path.addFirst(end.state);
//
//        return new Results<>(path, length, this);
//    }
//
//    /**
//     * A basic search node. Has the parent, the state it represents, and the step and cumulative costs.
//     */
//    class SearchNode {
//        final SearchNode parent;
//        final S state;
//        final float stepCost;
//        final float g;
//
//        SearchNode(SearchNode parent, S state){
//            if (parent == null) parent = this;
//            this.parent = parent;
//            this.state = state;
//            this.stepCost = parent.state.distanceTo(state);
//            this.g = parent.g + this.stepCost;
//        }
//    }
//
//    /**
//     * A generic results report for a search algorithm. Parameterized by the state and algorithm types.
//     * @param <S> The state type.
//     * @param <A> The algorithm type.
//     */
//    class Results<S extends State<S>, A extends SearchAlgorithm<S>> {
//        private List<S> hops;
//        private float distance;
//        private A algorithm;
//
//        /**
//         * Return a string report of the algorithm's results. Prints the states in the solution path, the length of the
//         * path, and how many hops in the path.
//         * @return
//         */
//        String resultsString(){
//            StringJoiner sj = new StringJoiner("")
//                .add("\n")
//                .add(algorithm.getName())
//                .add(" Search Results:\n");
//            for(S hop: hops){
//                sj.add(hop.name()).add("\n");
//            }
//
//            sj.add("That took ").add(String.valueOf(hops.size() - 1)).add(" hops to find.\n");
//            sj.add("Total distance = ").add(String.valueOf(Math.round(distance))).add(" miles.\n");
//            return sj.toString();
//        }
//
//        /**
//         * Create an instance of the report.
//         * @param hops The list of hop (each hop is a state) in the solution path.
//         * @param distance The length of the solution path.
//         * @param algorithm The algorithm used to search.
//         */
//        Results(List<S> hops, float distance, A algorithm){
//            this.hops = hops;
//            this.distance = distance;
//            this.algorithm = algorithm;
//        }
//    }
//
//
//}
//
//
///*
// * Implementations of search algorithms live down here.
// */
//
///**
// * Implementation of the A* search algorithm.
// * @param <S> The type of state that makes up the statespace in which it will be performing its search.
// */
//class AStar<S extends State<S>> extends SearchAlgorithm<S> {
//
//    AStar(Set<S> stateSpace) {
//        super(stateSpace);
//    }
//
//    @Override
//    String getName() {
//        return "A*";
//    }
//
//    /**
//     * Main execution of the algorithm.
//     *
//     * @param start The start state.
//     * @param end   The end state.
//     * @return An instance of a report that contains the list of hops from the start state to the end state,
//     * as well as any other relevant information.
//     */
//    @Override
//    public Results<S, SearchAlgorithm<S>> execute(S start, S end) {
//        Map<S, Float> h = stateSpace.stream().collect(Collectors.toMap(
//            Function.identity(),
//            e -> e.distanceTo(end)
//        ));
//
//        AStarNode node = new AStarNode(null, start, h::get);
//        SortedSet<AStarNode> frontier = new TreeSet<>();
//        frontier.add(node);
//
//        while (!frontier.isEmpty()) {
//            node = frontier.first();
//            frontier.remove(node);
//
//            if (node.state.equals(end)) break;
//
//            for (S neighborState : node.state.neighbors()) {
//                AStarNode child = new AStarNode(node, neighborState, h::get);
//                frontier.add(child);
//            }
//        }
//
//        return this.generateResults(node);
//    }
//
//    /**
//     * Extending the default search because we need to track the f-value and h-value.
//     */
//    private class AStarNode extends SearchNode implements Comparable<AStarNode> {
//        final float h;
//        final float f;
//
//        /**
//         * Create an instance of a node on the search tree.
//         *
//         * @param parent The parent node to which this node will point.
//         * @param state  A state in the statespace that this node represents.
//         * @param h      A heuristic function that provides an optimistic guess of the distance between the current
//         *               state and the end state.
//         */
//        AStarNode(AStarNode parent, S state, Function<S, Float> h) {
//            super(parent, state);
//            this.h = h.apply(state);
//            this.f = super.g + this.h;
//        }
//
//        /**
//         * Comapre this node to any other node, on the basis of the nodes' f-values.
//         *
//         * @param o The other node to compare against.
//         * @return 0 if the nodes are the same, <0 if this node is less, and >0 if this node is greater.
//         */
//        @Override
//        public int compareTo(AStarNode o) {
//            return Float.compare(this.f, o.f);
//        }
//    }
//}
//
//
///**
// * Implementation of the Depth-First search algorithm.
// * @param <S> The type of state that makes up the statespace in which it will be performing its search.
// */
//class DepthFirst<S extends State<S>> extends SearchAlgorithm<S>{
//
//    DepthFirst(Set<S> stateSpace) {
//        super(stateSpace);
//    }
//
//    @Override
//    String getName() {
//        return "Depth-First";
//    }
//
//    /**
//     * Main execution of the algorithm.
//     *
//     * @param start The start state.
//     * @param end   The end state.
//     * @return An instance of a report that contains the list of hops from the start state to the end state,
//     * as well as any other relevant information.
//     */
//    @Override
//    Results<S, SearchAlgorithm<S>> execute(S start, S end) {
//        Set<S> visited = new HashSet<>();
//        LinkedList<SearchNode> frontier = new LinkedList<>();
//
//        SearchNode node = new SearchNode(null, start);
//        frontier.push(node);
//
//        outer:
//        while (!frontier.isEmpty()){
//            node = frontier.pop();
//
//            if (node.state.equals(end)) break;
//            visited.add(node.state);
//
//            for(S neighborState: node.state.neighbors().descendingSet()){
//                SearchNode child = new SearchNode(node, neighborState);
//
//                if (child.state.equals(end)) {
//                    node = child;
//                    break outer;
//                }
//
//                if(!visited.contains(child.state) && frontier.stream().noneMatch(n -> child.state.equals(n.state))){
//                    frontier.push(child);
//                }
//            }
//        }
//
//        return this.generateResults(node);
//    }
//}
//
//
//
///**
// * Implementation of the Breadth-First search algorithm.
// * @param <S> The type of state that makes up the statespace in which it will be performing its search.
// */
//class BreadthFirst<S extends State<S>> extends SearchAlgorithm<S> {
//
//    /**
//     * Default constructor that implementing algorithms will call.
//     *
//     * @param stateSpace
//     */
//    BreadthFirst(Set<S> stateSpace) {
//        super(stateSpace);
//    }
//
//    @Override
//    String getName() {
//        return "Breadth-First";
//    }
//
//    /**
//     * Main execution of the algorithm.
//     *
//     * @param start The start state.
//     * @param end   The end state.
//     * @return An instance of a report that contains the list of hops from the start state to the end state,
//     * as well as any other relevant information.
//     */
//    @Override
//    Results<S, SearchAlgorithm<S>> execute(S start, S end) {
//        SearchNode node = new SearchNode(null, start);
//
//        Set<S> visited = new HashSet<>();
//        LinkedList<SearchNode> frontier = new LinkedList<>();
//
//        frontier.push(node);
//
//        outer:
//        while (!frontier.isEmpty()){
//            node = frontier.pop();
//
//            if (node.state.equals(end)) break;
//            visited.add(node.state);
//
//            for(S neighborState: node.state.neighbors()){
//                SearchNode child = new SearchNode(node, neighborState);
//
//                if (child.state.equals(end)) {
//                    node = child;
//                    break outer;
//                }
//
//                if(!visited.contains(child.state) && frontier.stream().noneMatch(n -> child.state.equals(n.state))){
//                    frontier.add(child);
//                }
//            }
//        }
//
//        return this.generateResults(node);
//    }
//}
//
