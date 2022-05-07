package metro.db;

import com.google.gson.*;
import metro.Main;
import metro.data.MetroLine;
import metro.data.MetroStation;
import metro.io.Parser;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Database {
    private static final Map<String, List<JsonElement>> jsonMap = new LinkedHashMap<>();
    private static final Map<String, Map<String, MetroStation>> metroMap = new LinkedHashMap<>();
    private static final Map<MetroStation, Map<MetroStation, Integer>> adjMatrixTime = new LinkedHashMap<>();
    private static final Map<MetroStation, Map<MetroStation, Integer>> adjMatrixHops = new LinkedHashMap<>();
    private static final List<MetroLine> metroLines = new LinkedList<>();
    private static final List<List<MetroStation>> cycles = new LinkedList<>();
    private static final List<MetroStation> disabledMetroStations = new ArrayList<>();
    private static File jsonFile = null;
    private static int lines = -1;
    private static int stations = -1;

    private Database() {

    }

    public static void addNewMetroStation(
            String metroLineName, String metroStationName
                                         ) throws UnsupportedOperationException {
        if (metroMap.containsKey(metroLineName) && metroMap.get(metroLineName)
                                                           .get(metroStationName) != null) {
            throw new UnsupportedOperationException(
                    metroStationName + " (" + metroStationName + ") already exists in database.");
        }
        if (!isMetroLineExists(metroLineName)) {
            throw new UnsupportedOperationException("Metro line " + metroLineName + " doesn't exist!");
        }
        metroMap.get(metroLineName)
                .put(metroStationName, new MetroStation(metroLineName, metroStationName));
    }

    public static void append(
            String toMetroLineName, String appendToStationName, String newMetroStationName, Integer time
                             ) {
        if (Database.isMetroStationExists(toMetroLineName, newMetroStationName)) {
            throw new UnsupportedOperationException(
                    newMetroStationName + " already exists in " + toMetroLineName + ".");
        }
        MetroStation metroStation = Database.getMetroStation(toMetroLineName, appendToStationName);
        MetroStation newMetroStation = new MetroStation(toMetroLineName, newMetroStationName);

        jsonMap.get(toMetroLineName)
               .add(Main.gson.toJsonTree(newMetroStation));

        for (JsonElement metroStationJsonElement : Database.getJsonMap()
                                                           .get(toMetroLineName)) {
            if (((JsonObject) metroStationJsonElement).get("name")
                                                      .getAsString()
                                                      .equals(appendToStationName)) {
                JsonArray next = ((JsonObject) metroStationJsonElement).getAsJsonArray("next");
                next.add(newMetroStation.getName());
                ((JsonObject) metroStationJsonElement).add("time", new JsonPrimitive(time));
            }
            if (((JsonObject) metroStationJsonElement).get("name")
                                                      .getAsString()
                                                      .equals(newMetroStationName)) {
                JsonArray prev = ((JsonObject) metroStationJsonElement).getAsJsonArray("prev");
                prev.add(metroStation.getName());
            }
        }

        Database.reload();

    }

    /* With enforceConnection flag is set to true, all incorrect connections will be fixed.
     * Input JSON file is prone for improperly set connections and transfers: enforceConnection flag
     * enforces two-way connectivity in case stations connected in one-way manner.
     * Look at Leytonstone in london.json, then look at https://www.bbc.co.uk/london/travel/downloads/tube_map.html
     * (top right red line) - Leytonstone misses connectivity to Snaresbrook, while it clearly should have it.  */

    public static void disableMetroStation(String metroStationLine, String metroStationName) {
        if (!Database.isMetroStationExists(metroStationLine, metroStationName)) {
            throw new UnsupportedOperationException(
                    "Metro station " + metroStationName + " doesn't exist or is not in " + metroStationLine);
        }

        MetroStation metroStationToDisable = Database.getMetroStation(metroStationLine, metroStationName);
        if (Database.getDisabledMetroStations()
                    .contains(metroStationToDisable)) {
            throw new UnsupportedOperationException(
                    metroStationToDisable.getFullName() + " is already disabled, no need to re-disable again.");
        }

        disabledMetroStations.add(metroStationToDisable);
        JsonElement metroStationToDisableJsonElement = get(metroStationLine, metroStationName);
        ((JsonObject) metroStationToDisableJsonElement).add("maintenance", new JsonPrimitive(true));
        disabledMetroStations.add(metroStationToDisable);
        Database.reload();

    }

    public static void enableMetroStation(String metroStationLine, String metroStationName) {
        if (!Database.isMetroStationExists(metroStationLine, metroStationName)) {
            throw new UnsupportedOperationException(
                    "Metro station " + metroStationName + " doesn't exist or is not in " + metroStationLine);
        }
        MetroStation metroStationToEnable = Database.getMetroStation(metroStationLine, metroStationName);

        if (!Database.getDisabledMetroStations()
                     .contains(metroStationToEnable)) {
            throw new UnsupportedOperationException(
                    metroStationToEnable.getFullName() + " is already enabled, no need to re-enable again.");
        }

        disabledMetroStations.remove(metroStationToEnable);

        JsonElement metroStationToEnableJsonElement = get(metroStationLine, metroStationName);
        ((JsonObject) metroStationToEnableJsonElement).remove("maintenance");
        Database.reload();

    }

    public static void findDisabledMetroStations() {
        if (jsonMap.isEmpty()) {
            throw new UnsupportedOperationException("Metro database is empty!");
        }
        for (String metroLineName : jsonMap.keySet()) {
            for (JsonElement metroStationJsonElement : jsonMap.get(metroLineName)) {
                if (((JsonObject) metroStationJsonElement).has("maintenance")) {
                    String metroStationName = ((JsonObject) metroStationJsonElement).get("name")
                                                                                    .getAsString();
                    MetroStation disabledMetroStation = Database.getMetroStation(metroLineName, metroStationName);
                    disabledMetroStations.add(disabledMetroStation);
                }
            }
        }
    }

    /* Returns null if metro station is not in database */
    public static JsonElement get(String metroLineName, String metroStationName) {
        for (JsonElement metroStationJsonElement : Database.getJsonMap()
                                                           .get(metroLineName)) {
            if (((JsonObject) metroStationJsonElement).get("name")
                                                      .getAsString()
                                                      .equals(metroStationName)) {
                return metroStationJsonElement;
            }
        }
        return null;
    }

    public static Map<MetroStation, Map<MetroStation, Integer>> getAdjMatrixHops() {
        return new HashMap<>(adjMatrixHops);
    }

    public static Map<MetroStation, Map<MetroStation, Integer>> getAdjMatrixTime() {
        return new HashMap<>(adjMatrixTime);
    }

    public static List<List<MetroStation>> getCycles() {
        return new LinkedList<>(cycles);
    }

    public static List<MetroStation> getDisabledMetroStations() {
        return new ArrayList<>(disabledMetroStations);
    }

    public static String getJsonFilePath() {
        try {
            return jsonFile.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Map<String, List<JsonElement>> getJsonMap() {
        return new LinkedHashMap<>(jsonMap);
    }

    /* Metro stations will appear in the order as they were in JSON file */
    public static MetroLine getMetroLine(String metroLineName) {
        if (!isMetroLineExists(metroLineName)) {
            return null;
        }
        for (MetroLine metroLine : metroLines) {
            if (metroLine.getName()
                         .equals(metroLineName)) {
                return metroLine;
            }
        }
        return null;

    }

    public static List<MetroLine> getMetroLines() {
        return new LinkedList<>(metroLines);
    }

    public static Map<String, Map<String, MetroStation>> getMetroMap() {
        return new LinkedHashMap<>(metroMap);
    }

    /* If metro line name and/or metro station name is not found returns null*/
    public static MetroStation getMetroStation(String metroLineName, String metroStationName) {
        if (isMetroStationExists(metroLineName, metroStationName)) {
            return metroMap.get(metroLineName)
                           .get(metroStationName);
        }
        return null;
    }

    public static int getNumLines() {
        return metroLines.size();
    }

    public static int getNumStations() {
        int count = 0;
        for (MetroLine metroLine : metroLines) {
            count += metroLine.size();
        }
        stations = count;
        return stations;
    }

    public static boolean isMetroLineEmpty(String metroLineName) {
        return isMetroLineExists(metroLineName) && metroMap.get(metroLineName) == null;
    }

    public static boolean isMetroLineExists(String metroLineName) {
        return metroMap.containsKey(metroLineName);
    }

    public static boolean isMetroStationEmpty(String metroLineName, String metroStationName) {
        return isMetroStationExists(metroLineName, metroStationName) && metroMap.get(metroLineName)
                                                                                .get(metroStationName) == null;
    }

    public static boolean isMetroStationExists(String metroLineName, String metroStationName) {
        return !isMetroLineEmpty(metroLineName) && metroMap.get(metroLineName)
                                                           .containsKey(metroStationName);
    }

    /* enforceConnectivity flag ensures that all one-sided connections are two-sided. JSON file remains unaffected. */
    public static void loadJsonFile(File file, boolean enforceConnectivity) {
        try {
            if (!Database.jsonMap.isEmpty()) {
                Database.jsonMap.clear();
            }
            if (!Database.metroMap.isEmpty()) {
                Database.metroMap.clear();
            }
            if (!Database.metroLines.isEmpty()) {
                Database.metroLines.clear();
            }
            jsonMap.putAll(Parser.read(file));
            jsonFile = file;
            convertJsonMapToMetroMap(enforceConnectivity);
            convertMetroMapToMetroLines();
            findDisabledMetroStations();
            buildAdjMatrixHops();
            buildAdjMatrixTime();
            System.out.println("Loaded metro JSON file " + Database.getJsonFilePath() + " successfully.");
            System.out.println(
                    "File contains " + Database.getNumLines() + " metro lines and " + Database.getNumStations() + " metro stations.");
            System.out.println("Enforce connectivity: " + (Main.ENFORCE_CONNECTIVITY ? "yes" : "no"));
            System.out.println("Transfer time: " + Main.TRANSFER_TIME + "m");
            System.out.println("Timeout: " + Main.TIMEOUT_MILLIS + "ms");
        } catch (IOException e) {
            System.out.println("Error! Such file doesn't exist.");
        }
    }

    public static void prepend(
            String toMetroLineName, String prependToMetroStationName, String newMetroStationName, Integer time
                              ) {
        if (Database.isMetroStationExists(toMetroLineName, newMetroStationName)) {
            throw new UnsupportedOperationException(
                    newMetroStationName + " already exists in " + toMetroLineName + ".");
        }
        MetroStation metroStation = Database.getMetroStation(toMetroLineName, prependToMetroStationName);
        MetroStation newMetroStation = new MetroStation(toMetroLineName, newMetroStationName);

        jsonMap.get(toMetroLineName)
               .add(Main.gson.toJsonTree(newMetroStation));

        for (JsonElement metroStationJsonElement : Database.getJsonMap()
                                                           .get(toMetroLineName)) {
            if (((JsonObject) metroStationJsonElement).get("name")
                                                      .getAsString()
                                                      .equals(prependToMetroStationName)) {
                JsonArray prev = ((JsonObject) metroStationJsonElement).getAsJsonArray("prev");
                prev.add(newMetroStation.getName());
            }
            if (((JsonObject) metroStationJsonElement).get("name")
                                                      .getAsString()
                                                      .equals(newMetroStationName)) {
                JsonArray next = ((JsonObject) metroStationJsonElement).getAsJsonArray("next");
                next.add(metroStation.getName());
                ((JsonObject) metroStationJsonElement).add("time", new JsonPrimitive(time));
            }
        }

        Database.reload();

    }

    public static void reload() {
        if (!Database.metroMap.isEmpty()) {
            Database.metroMap.clear();
        }
        if (!Database.metroLines.isEmpty()) {
            Database.metroLines.clear();
        }
        if (!Database.disabledMetroStations.isEmpty()) {
            Database.disabledMetroStations.clear();
        }
        convertJsonMapToMetroMap(Main.ENFORCE_CONNECTIVITY);
        convertMetroMapToMetroLines();
        findDisabledMetroStations();
        buildAdjMatrixHops();
        buildAdjMatrixTime();
        for (MetroLine metroLine : getMetroLines()) {
            metroLine.findTerminalStations();
        }
    }

    /* Adjacency matrix that represents weight as movement between stations */
    private static void buildAdjMatrixHops() {
        if (!adjMatrixHops.isEmpty()) {
            adjMatrixHops.clear();
        }
        for (MetroLine metroLine : metroLines) {
            for (MetroStation metroStation : metroLine.getMetroStations()) {
                adjMatrixHops.put(metroStation, new LinkedHashMap<>());
            }
        }
        for (MetroStation metroStation : adjMatrixHops.keySet()) {
            for (MetroStation transferStation : metroStation.getTransfers()) {
                adjMatrixHops.get(metroStation)
                             .put(transferStation, 0);
                adjMatrixHops.get(transferStation)
                             .put(metroStation, 0);
            }
            List<MetroStation> nonTransferStations = new ArrayList<>(metroStation.getPrev());
            nonTransferStations.addAll(metroStation.getNext());
            for (MetroStation nonTransferStation : nonTransferStations) {
                adjMatrixHops.get(metroStation)
                             .put(nonTransferStation, 1);
                adjMatrixHops.get(nonTransferStation)
                             .put(metroStation, 1);
            }
        }
    }

    /* Adjacency matrix that represents weight as time between stations */
    private static void buildAdjMatrixTime() {
        if (!adjMatrixTime.isEmpty()) {
            adjMatrixTime.clear();
        }
        for (MetroLine metroLine : metroLines) {
            for (MetroStation metroStation : metroLine.getMetroStations()) {
                adjMatrixTime.put(metroStation, new LinkedHashMap<>());
            }
        }
        for (MetroStation metroStation : adjMatrixTime.keySet()) {
            for (MetroStation nextStation : metroStation.getNext()) {
                adjMatrixTime.get(metroStation)
                             .put(nextStation, metroStation.getTime());
                adjMatrixTime.get(nextStation)
                             .put(metroStation, metroStation.getTime());
            }
        }
    }

    /* Convert map of JsonElements into map of MetroStations */
    private static void convertJsonMapToMetroMap(boolean enforceConnectivity) {
        if (jsonMap.isEmpty()) {
            throw new UnsupportedOperationException("JSON map is empty, did you parse json data?");
        }

        /* Preserve order as it was read from JSON file */
        for (String metroLineName : jsonMap.keySet()) {
            Map<String, MetroStation> metroLineAsMap = new LinkedHashMap<>();

            for (JsonElement metroStationJsonElement : jsonMap.get(metroLineName)) {
                String metroStationName = metroStationJsonElement.getAsJsonObject()
                                                                 .get("name")
                                                                 .getAsString();
                metroLineAsMap.put(metroStationName, null);
            }
            metroMap.put(metroLineName, metroLineAsMap);
        }

        /* Populate map with actual data */
        for (String metroLineName : jsonMap.keySet()) {
            Map<String, MetroStation> metroLineAsMap = new LinkedHashMap<>();

            for (JsonElement metroStationJsonElement : jsonMap.get(metroLineName)) {
                String metroStationName = metroStationJsonElement.getAsJsonObject()
                                                                 .get("name")
                                                                 .getAsString();



                /* Populate metro map with MetroStation objects knowing only metro line and metro station names*/
                if (Database.isMetroStationEmpty(metroLineName, metroStationName)) {
                    addNewMetroStation(metroLineName, metroStationName);
                }

                /* After metro map is populated with MetroStation objects, we want to configure them (add prev, next, time values) */
                MetroStation currentMetroStation = Database.getMetroStation(metroLineName, metroStationName);

                /* We are 100% positive that current metro station has its own object in database, safe to assert */
                assert currentMetroStation != null;

                JsonArray prevMetroStations = metroStationJsonElement.getAsJsonObject()
                                                                     .get("prev")
                                                                     .getAsJsonArray();

                JsonArray nextMetroStations = metroStationJsonElement.getAsJsonObject()
                                                                     .get("next")
                                                                     .getAsJsonArray();

                JsonArray transferMetroStations = metroStationJsonElement.getAsJsonObject()
                                                                         .get("transfer")
                                                                         .getAsJsonArray();

                // boolean isTimeKeyNull = metroStationJsonElement.getAsJsonObject()
                //                                                .get("time")
                //                                                .isJsonNull();

                boolean isTimeKeyPresent = metroStationJsonElement.getAsJsonObject().has("time");

                Integer time = isTimeKeyPresent ? metroStationJsonElement.getAsJsonObject()
                                                                             .get("time")
                                                                             .getAsInt() : null;

                JsonArray adjacentMetroStations = new JsonArray();
                adjacentMetroStations.addAll(prevMetroStations);
                adjacentMetroStations.addAll(nextMetroStations);

                for (JsonElement adjacentMetroStationJsonElement : adjacentMetroStations) {
                    String currentMetroStationName = adjacentMetroStationJsonElement.getAsString();
                    if (Database.isMetroStationEmpty(metroLineName, currentMetroStationName)) {
                        Database.addNewMetroStation(metroLineName, currentMetroStationName);
                    }
                }

                /* We create MetroStation objects metro line by  metro line. Transfers lead to other metro lines, so we may need to construct
                 new MetroStation objects for them ahead of a time */

                for (JsonElement transferMetroStationJsonElement : transferMetroStations) {
                    String currentMetroLineName = transferMetroStationJsonElement.getAsJsonObject()
                                                                                 .get("line")
                                                                                 .getAsString();
                    String currentMetroStationName = transferMetroStationJsonElement.getAsJsonObject()
                                                                                    .get("station")
                                                                                    .getAsString();
                    if (Database.isMetroStationEmpty(currentMetroLineName, currentMetroStationName)) {
                        Database.addNewMetroStation(currentMetroLineName, currentMetroStationName);
                    }
                }

                /* Actual configuration: adding prev/next stations and setting time */
                for (JsonElement prevMetroStationJsonElement : prevMetroStations) {
                    String prevMetroStationName = prevMetroStationJsonElement.getAsString();
                    MetroStation prevMetroStation = getMetroStation(metroLineName, prevMetroStationName);
                    if (!currentMetroStation.getPrev()
                                            .contains(prevMetroStation)) {
                        currentMetroStation.addPrev(prevMetroStation);
                    }
                    if (enforceConnectivity) {
                        if (!prevMetroStation.getNext()
                                             .contains(currentMetroStation)) {
                            prevMetroStation.addNext(currentMetroStation);
                        }
                    }
                }

                for (JsonElement nextMetroStationJsonElement : nextMetroStations) {
                    String nextMetroStationName = nextMetroStationJsonElement.getAsString();
                    MetroStation nextMetroStation = getMetroStation(metroLineName, nextMetroStationName);
                    if (!currentMetroStation.getNext()
                                            .contains(nextMetroStation)) {
                        currentMetroStation.addNext(nextMetroStation);
                    }
                    if (enforceConnectivity) {
                        if (!nextMetroStation.getPrev()
                                             .contains(currentMetroStation)) {
                            nextMetroStation.addPrev(currentMetroStation);
                        }
                    }
                }

                for (JsonElement transferMetroStationJsonElement : transferMetroStations) {
                    String transferMetroLineName = transferMetroStationJsonElement.getAsJsonObject()
                                                                                  .get("line")
                                                                                  .getAsString();
                    String transferMetroStationName = transferMetroStationJsonElement.getAsJsonObject()
                                                                                     .get("station")
                                                                                     .getAsString();
                    MetroStation transferMetroStation = getMetroStation(transferMetroLineName,
                                                                        transferMetroStationName
                                                                       );
                    if (!currentMetroStation.getTransfers()
                                            .contains(transferMetroStation)) {
                        currentMetroStation.addTransfer(transferMetroStation);
                    }
                    if (enforceConnectivity) {
                        transferMetroStation.addTransfer(currentMetroStation);
                    }
                }

                currentMetroStation.setTime(time);

                metroLineAsMap.put(metroStationName, currentMetroStation);
            }
            metroMap.put(metroLineName, metroLineAsMap);
        }
    }

    private static void convertMetroMapToMetroLines() {
        if (!metroLines.isEmpty()) {
            metroLines.clear();
        }
        for (String metroLineName : metroMap.keySet()) {
            List<MetroStation> metroStations = new LinkedList<>();
            for (String metroStationName : metroMap.get(metroLineName)
                                                   .keySet()) {
                metroStations.add(metroMap.get(metroLineName)
                                          .get(metroStationName));
            }
            metroLines.add(new MetroLine(Map.entry(metroLineName, metroStations)));
        }
    }
}
