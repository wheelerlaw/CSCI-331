import java.io.*;
import java.util.*;

/**
 * Main execution class.
 */
public class ESS {

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

    private static int interactionCount = 0;

    /**
     * Main execution entrypoint of the application.
     * @param args popSize [percentHawks] [resourceAmt] [costHawk-Hawk]
     * @throws IOException Thrown if something happens.
     */
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
        int numDoves = populationSize - numHawks;

        List<Individual> population = new ArrayList<>();

        // Create the population.
        for(int i = 0; i < numHawks; i++){
            population.add(new Individual(Individual.Strategy.HAWK));
        }
        for(int i = 0; i < numDoves; i++){
            population.add(new Individual(Individual.Strategy.DOVE));
        }

        // Create a separate list dedicated for maintaining a list of alive population members.
        List<Individual> alivePopulation = new ArrayList<>(population);

        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

        // Main execution loop
        loop:
        for(;;) {
            System.out.println(PROMPT_TEXT);
            System.out.print("> ");
            String line = input.readLine();
            if (line == null) break;

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
                case "4":
                    for(int i = 0; i < 1000; i++) {
                        if (population.size() < 2) break;
                        System.out.println(runInteraction(alivePopulation, resourceAmount, hawkHawkCost));
                    }
                    break;
                case "5":
                    for(int i = 0; i < 10000; i++) {
                        if (population.size() < 2) break;
                        System.out.println(runInteraction(alivePopulation, resourceAmount, hawkHawkCost));
                    }
                    break;
                case "6":
                    System.out.print("> ");
                    line = input.readLine();
                    int N = Integer.parseInt(line);

                    for(int i = 0; i < N; i++) {
                        if (population.size() < 2) break;
                        System.out.println(runInteraction(alivePopulation, resourceAmount, hawkHawkCost));
                    }
                    break;
                case "7":
                    do {
                        System.out.println(runInteraction(alivePopulation, resourceAmount, hawkHawkCost));
                    } while(!input.readLine().equals("Stop"));
                    break;
                case "8":
                    break loop;
            }
        }

    }

    /**
     * Print out the starting statistics of the simulation.
     * @param populationSize The population size.
     * @param percentHawks The percentage of the population that are hawks. The number of hawks, percentage and number of
     *                     doves will be derived from this.
     * @param resourceAmount The resources that are at stake for each interaction.
     * @param hawkHawkCost The cost of two hawks fighting each other.
     * @return A formatted string of the statistics.
     */
    private static String getStartingStats(int populationSize, int percentHawks, int resourceAmount, int hawkHawkCost){
        int numHawks = (int) (populationSize * (percentHawks / 100f));
        int percentDoves = 100 - percentHawks;
        int numDoves = populationSize - numHawks;
        return String.format(STARTING_STATS, populationSize, percentHawks, numHawks, percentDoves, numDoves, resourceAmount, hawkHawkCost);
    }

    /**
     * Gets details about the population.
     * @param population The list of all of the population members.
     * @return A formatted string of the population details.
     */
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

    /**
     * Get a shorter, sorted summary of the population.
     * @param population The list of all of the population members.
     * @return The formatted string of the sorted summary of the population.
     */
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

    /**
     * Run a single interaction. Selects two individuals from the population to participate in the interaction.
     * @param population The list of alive members of the population.
     * @param resourceAmount The amount of resource that is being contended for.
     * @param hawkHawkCost The cost dealt to the hawks when two hawks fight each other.
     * @return A formatted string of the results of the interaction.
     */
    private static String runInteraction(List<Individual> population, int resourceAmount, int hawkHawkCost){
        Random random = new Random();
        int individual1Index = random.nextInt(population.size());
        int individual2Index = random.nextInt(population.size() - 1);

        if (individual2Index == individual1Index){
            individual2Index += 1;
        }

        Individual individual1 = population.get(individual1Index);
        Individual individual2 = population.get(individual2Index);

        int individual1ResourceChange = 0;
        int individual2ResourceChange = 0;

        if (individual1.strategy == Individual.Strategy.DOVE && individual2.strategy == Individual.Strategy.DOVE) {
            individual1ResourceChange += resourceAmount / 2;
            individual2ResourceChange += resourceAmount / 2;
        } else if (individual1.strategy == Individual.Strategy.HAWK && individual2.strategy == Individual.Strategy.DOVE) {
            individual1ResourceChange += resourceAmount;
        } else if (individual2.strategy == Individual.Strategy.HAWK && individual1.strategy == Individual.Strategy.DOVE) {
            individual2ResourceChange += resourceAmount;
        } else if (individual1.strategy == Individual.Strategy.HAWK && individual2.strategy == Individual.Strategy.HAWK) {
            individual1ResourceChange += resourceAmount;

            individual1ResourceChange -= hawkHawkCost;
            individual2ResourceChange -= hawkHawkCost;
        }

        individual1.resources += individual1ResourceChange;
        individual2.resources += individual2ResourceChange;

        String output1 =
            "Encounter: %d%n" +
                "Individual %d: %s%n" +
                "Individual %d: %s%n" +
                "%3$s/%5$s: %3$s: %+d\t%5$s: %+d";

        String output2 = "%s one has died!";
        String output3 = "%s two has died!";

        String output4 = "Individual %2$d=%d\t\t\tIndividual %4$d=%d";

        StringJoiner sj = new StringJoiner("\n", "", "");
        sj.add(output1);
        if (individual1.resources < 0){
            sj.add(String.format(output2, individual1.strategy.name));
            population.remove(individual1Index);
        }
        if (individual2.resources < 0) {
            sj.add(String.format(output3, individual2.strategy.name));
            population.remove(individual1Index);
        }
        sj.add(output4);

        interactionCount++;

        return String.format(sj.toString(),
            interactionCount, individual1Index, individual1.strategy.name, individual2Index, individual2.strategy.name,
            individual1ResourceChange, individual2ResourceChange, individual1.resources, individual2.resources
        );
    }

}

class Individual {
    int resources;
    Strategy strategy;

    Individual(Strategy strategy){
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

