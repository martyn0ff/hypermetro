package metro.search;

import metro.Main;
import metro.data.MetroLine;
import metro.data.MetroStation;
import metro.db.Database;

import java.util.*;

public class Search {

    private Search() {

    }

    /* Calculates time of fastest route to each metro station in metro */
    public static Map<MetroStation, Integer> findAllFastestRoutes(MetroStation from) {

        /* Distance map represents distances from source station to all other stations */
        Map<MetroStation, Integer> distancesMap = new LinkedHashMap<>();

        /* First we populate distance map with +inf and 0 for source station */

        for (MetroLine metroLine : Database.getMetroLines()) {
            for (MetroStation metroStation : metroLine.getMetroStations()) {
                if (Database.getDisabledMetroStations().contains(metroStation)) {
                    continue;
                }
                if (metroStation == from) {
                    distancesMap.put(metroStation, 0);
                } else {
                    distancesMap.put(metroStation, Integer.MAX_VALUE);
                }
            }
        }

        /* Each map entry represent distance from source station to chosen metro station */

        PriorityQueue<Map.Entry<MetroStation, Integer>> pq = new PriorityQueue<>(Map.Entry.comparingByValue());
        List<MetroStation> visited = new ArrayList<>();

        /* Distance from source to source station is zero */

        pq.add(Map.entry(from, 0));

        while (!pq.isEmpty()) {
            Map.Entry<MetroStation, Integer> currentMetroStationEntry = pq.poll();
            MetroStation currentMetroStation = currentMetroStationEntry.getKey();
            int currentTotalTimeToFromStation = currentMetroStationEntry.getValue();

            visited.add(currentMetroStation);

            if (currentMetroStation.hasTransfers()) {
                for (MetroStation transferMetroStation : currentMetroStation.getTransfers()) {

                    if (Database.getDisabledMetroStations().contains(transferMetroStation)) {
                        continue;
                    }

                    if (visited.contains(transferMetroStation)) {
                        continue;
                    }

                    int newTotalTimeToFromStation = currentTotalTimeToFromStation + Main.TRANSFER_TIME;

                    if (newTotalTimeToFromStation < distancesMap.get(transferMetroStation)) {
                        distancesMap.put(transferMetroStation, newTotalTimeToFromStation);
                        pq.add(Map.entry(transferMetroStation, newTotalTimeToFromStation));
                    }
                }
            }

            List<MetroStation> adjacentMetroStations = new ArrayList<>(currentMetroStation.getPrev());
            adjacentMetroStations.addAll(currentMetroStation.getNext());

            for (MetroStation adjacentMetroStation : adjacentMetroStations) {

                if (Database.getDisabledMetroStations().contains(adjacentMetroStation)) {
                    continue;
                }

                if (visited.contains(adjacentMetroStation)) {
                    continue;
                }

                /* We need to know whether adjacent station is before or after the current one */
                boolean isAdjacentStationNext = currentMetroStation.getNext()
                                                                   .contains(adjacentMetroStation);

                /* If adjacent station is next, then we have to use current station's time */
                /* If adjacent station is previous, then we have to use adjacent station's time */

                int timeToAdjacentStation =
                        isAdjacentStationNext ? currentMetroStation.getTime() : adjacentMetroStation.getTime();
                int newTotalTimeToFromStation = currentTotalTimeToFromStation + timeToAdjacentStation;

                if (newTotalTimeToFromStation < distancesMap.get(adjacentMetroStation)) {
                    distancesMap.put(adjacentMetroStation, newTotalTimeToFromStation);
                    pq.add(Map.entry(adjacentMetroStation,
                                     currentMetroStationEntry.getValue() + timeToAdjacentStation
                                    ));
                }
            }
        }

        return distancesMap;
    }

    /* Without maxRoutes limitation, this code produces out of memory error due to graph being undirected and containing gazillion of cycles */
    /* It counts transfer from one station to another as a part of a route. */
    /* This algorithm is a BFS algorithm without visiting. Having so many cycles and intersections makes visiting
     * nodes ineffective */

    public static List<List<MetroStation>> findAllRoutes(MetroStation from, MetroStation to, int maxRoutes, int timeoutMillis) {
        double startFrom = System.currentTimeMillis();
        Queue<List<MetroStation>> routesQueue = new LinkedList<>();
        List<List<MetroStation>> allRoutes = new LinkedList<>();
        List<MetroStation> route = new LinkedList<>();
        route.add(from);
        routesQueue.add(route);
        while (!routesQueue.isEmpty()) {
            route = routesQueue.poll();
            if (System.currentTimeMillis() - startFrom >= timeoutMillis) {
                System.out.println("Search was stopped due to timeout. Showing routes that we were able to build.");
                break;
            }
            if (allRoutes.size() >= maxRoutes) {
                break;
            }
            MetroStation current = route.get(route.size() - 1);
            if (current == to) {
                if (!allRoutes.contains(route)) {
                    allRoutes.add(new LinkedList<>(route));
                }
                continue;
            }
            for (MetroStation adjacentMetroStation : current.getAdjacentMetroStations()) {
                if (adjacentMetroStation.isDisabled()) {
                    continue;
                }
                if (!route.contains(adjacentMetroStation)) {
                    List<MetroStation> newRoute = new LinkedList<>(route);
                    newRoute.add(adjacentMetroStation);
                    routesQueue.add(newRoute);
                }
            }
        }

        return allRoutes;
    }

    /* Based on BFS */
    public static List<List<MetroStation>> findCycles(
            MetroStation from,
            MetroStation current,
            boolean isCycleFound,
            List<MetroStation> visited,
            List<MetroStation> currentPath,
            List<List<MetroStation>> cycles
                                                     ) {

        /* In order for it to work, we have to start from a terminal station to be assured that we're not starting
         * from within the loop */

        List<MetroStation> adjacentMetroStations = new ArrayList<>(current.getPrev());
        adjacentMetroStations.addAll(current.getNext());

        visited.add(current);
        currentPath.add(current);

        List<MetroStation> visitedCopy = new LinkedList<>(visited);

        /* Since it's an undirected graph, and it's a BFS, we want to avoid a cycle that is one hop from src to dest route */
        if (isCycleFound && current.getPrev()
                                   .contains(from) && currentPath.size() > 2) {
            currentPath.add(from);
            cycles.add(new LinkedList<>(currentPath));
            // return null;
        }

        /* If current station is already part of a cycle */
        if (current.getNext()
                   .size() > 1 && !isCycleFound) {
            // System.out.println("Current station has branch!");
            for (List<MetroStation> cycle : cycles) {
                if (cycle.contains(current)) {
                    return null;
                }
            }
            findCycles(current, current, true, visitedCopy, new LinkedList<>(), cycles);
        }

        for (MetroStation metroStation : adjacentMetroStations) {
            if (metroStation.isDisabled()) {
                continue;
            }
            if (!visited.contains(metroStation)) {
                findCycles(from, metroStation, isCycleFound, visited, currentPath, cycles);
                currentPath.remove(metroStation);
            }
        }

        return cycles;

    }

    /* This implementation uses "time" key from JSON file as a replacement for adjacency matrix */
    public static List<MetroStation> findFastestRoute(MetroStation from, MetroStation to) {
        Map<MetroStation, Integer> distancesMap = new LinkedHashMap<>();

        /* Routes map keeps track of metro stations that the shortest edge stems from for each station - child-parent relationship*/

        Map<MetroStation, MetroStation> routesMap = new LinkedHashMap<>();
        List<MetroStation> ret = new LinkedList<>();

        /* Distance map represents distances from source station to all other stations */
        /* First we populate distance map with +inf and 0 for source station */

        for (MetroLine metroLine : Database.getMetroLines()) {
            for (MetroStation metroStation : metroLine.getMetroStations()) {
                if (metroStation.isDisabled()) {
                    continue;
                }
                if (metroStation == from) {
                    distancesMap.put(metroStation, 0);
                } else {
                    distancesMap.put(metroStation, Integer.MAX_VALUE);
                }
            }
        }

        /* Each map entry represent distance from source station to chosen metro station */

        PriorityQueue<Map.Entry<MetroStation, Integer>> pq = new PriorityQueue<>(Map.Entry.comparingByValue());
        List<MetroStation> visited = new ArrayList<>();

        /* Distance from source to source station is zero */

        pq.add(Map.entry(from, 0));

        while (!pq.isEmpty()) {
            Map.Entry<MetroStation, Integer> currentMetroStationEntry = pq.poll();
            MetroStation currentMetroStation = currentMetroStationEntry.getKey();
            int currentTotalTimeToFromStation = currentMetroStationEntry.getValue();

            visited.add(currentMetroStation);

            if (currentMetroStation.hasTransfers()) {
                for (MetroStation transferMetroStation : currentMetroStation.getTransfers()) {

                    if (transferMetroStation.isDisabled()) {
                        continue;
                    }

                    if (visited.contains(transferMetroStation)) {
                        continue;
                    }

                    int newTotalTimeToFromStation = currentTotalTimeToFromStation + Main.TRANSFER_TIME;

                    if (newTotalTimeToFromStation < distancesMap.get(transferMetroStation)) {
                        distancesMap.put(transferMetroStation, newTotalTimeToFromStation);
                        pq.add(Map.entry(transferMetroStation, newTotalTimeToFromStation));
                        routesMap.put(transferMetroStation, currentMetroStation);

                    }
                }
            }

            List<MetroStation> adjacentMetroStations = new ArrayList<>(currentMetroStation.getPrev());
            adjacentMetroStations.addAll(currentMetroStation.getNext());

            for (MetroStation adjacentMetroStation : adjacentMetroStations) {

                if (adjacentMetroStation.isDisabled()) {
                    continue;
                }

                if (visited.contains(adjacentMetroStation)) {
                    continue;
                }

                /* We need to know whether adjacent station is before or after the current one */
                boolean isAdjacentStationNext = currentMetroStation.getNext()
                                                                   .contains(adjacentMetroStation);

                /* If adjacent station is next, then we have to use current station's time */
                /* If adjacent station is previous, then we have to use adjacent station's time */

                int timeToAdjacentStation =
                        isAdjacentStationNext ? currentMetroStation.getTime() : adjacentMetroStation.getTime();
                int newTotalTimeToFromStation = currentTotalTimeToFromStation + timeToAdjacentStation;

                if (newTotalTimeToFromStation < distancesMap.get(adjacentMetroStation)) {
                    distancesMap.put(adjacentMetroStation, newTotalTimeToFromStation);
                    pq.add(Map.entry(adjacentMetroStation,
                                     currentMetroStationEntry.getValue() + timeToAdjacentStation
                                    ));
                    routesMap.put(adjacentMetroStation, currentMetroStation);

                }
            }
        }

        MetroStation currentStation = to;
        ret.add(currentStation);
        while (currentStation != from) {
            ret.add(routesMap.get(currentStation));
            currentStation = routesMap.get(currentStation);

            /* If parent is null, it means that it's impossible to build a route because some station is in maintenance */
            if (currentStation == null) {
                return null;
            }
        }

        Collections.reverse(ret);

        return ret;

    }

    /* This implementation uses adjacency matrix */
    public static List<MetroStation> findLeastHopsRoute(MetroStation from, MetroStation to) {
        /* Hops map represents hops from source station to all other stations */
        Map<MetroStation, Integer> hopsMap = new LinkedHashMap<>();

        /* Routes map keeps track of metro stations that the shortest edge stems from for each station - a child-parent relationship*/
        Map<MetroStation, MetroStation> routesMap = new LinkedHashMap<>();
        List<MetroStation> ret = new LinkedList<>();

        /* First we populate hops map with +inf and 0 for source station */
        for (MetroLine metroLine : Database.getMetroLines()) {
            for (MetroStation metroStation : metroLine.getMetroStations()) {
                if (metroStation.isDisabled()) {
                    continue;
                }
                if (metroStation == from) {
                    hopsMap.put(metroStation, 0);
                } else {
                    hopsMap.put(metroStation, Integer.MAX_VALUE);
                }
            }
        }

        /* Each map entry represent hops from source station to chosen metro station */

        PriorityQueue<Map.Entry<MetroStation, Integer>> pq = new PriorityQueue<>(Map.Entry.comparingByValue());
        List<MetroStation> visited = new ArrayList<>();

        /* Distance from source to source station is zero */

        pq.add(Map.entry(from, 0));

        while (!pq.isEmpty()) {
            Map.Entry<MetroStation, Integer> currentMetroStationEntry = pq.poll();
            MetroStation currentMetroStation = currentMetroStationEntry.getKey();
            int currentTotalHops = currentMetroStationEntry.getValue();

            visited.add(currentMetroStation);

            for (MetroStation adjMetroStation : currentMetroStation.getAdjacentMetroStations()) {

                if (adjMetroStation.isDisabled()) {
                    continue;
                }

                if (visited.contains(adjMetroStation)) {
                    continue;
                }

                int newTotalHops = currentTotalHops + Database.getAdjMatrixHops()
                                                              .get(currentMetroStation)
                                                              .get(adjMetroStation);

                if (newTotalHops < hopsMap.get(adjMetroStation)) {
                    hopsMap.put(adjMetroStation, newTotalHops);
                    pq.add(Map.entry(adjMetroStation, newTotalHops));
                    routesMap.put(adjMetroStation, currentMetroStation);
                }
            }
        }

        /* Route reconstruction */

        MetroStation currentStation = to;
        ret.add(currentStation);
        while (currentStation != from) {
            ret.add(routesMap.get(currentStation));
            currentStation = routesMap.get(currentStation);
            if (currentStation == null) {
                return null;
            }
        }

        Collections.reverse(ret);

        return ret;

    }
}
