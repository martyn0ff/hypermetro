package metro.io;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import metro.Main;

import java.io.*;
import java.util.*;

public class Parser {

    private static final Map<String, Integer> runtimeArgsToMaxArgsMap = Map.ofEntries(Map.entry("-metro", 1),
                                                                                      Map.entry("-ttime", 1),
                                                                                      Map.entry("-timeout", 1),
                                                                                      Map.entry("-enforce", 0)
                                                                                     );


    private static final Map<String, RuntimeArgumentChecker> runtimeArgsCheckerMap = Map.ofEntries(
            Map.entry("-metro", (arg) -> arg.matches("[\\w.]+")),
            Map.entry("-ttime", (arg) -> arg.matches("\\d+")),
            Map.entry("-timeout", (arg) -> arg.matches("\\d+"))
                                                                                                  );


    /* Read JSON file into map. It preserves the order of appearance of JsonElements in the file. */
    public static Map<String, List<JsonElement>> read(File file) throws FileNotFoundException {
        JsonReader reader = new JsonReader(new BufferedReader(new FileReader(file)));
        Map<String, List<JsonElement>> jsonMap = new LinkedHashMap<>();

        try {
            JsonObject parsedJsonObject = JsonParser.parseReader(reader)
                                                    .getAsJsonObject();

            for (Map.Entry<String, JsonElement> entry : parsedJsonObject.entrySet()) {
                String metroLineName = entry.getKey();
                JsonArray metroStations = entry.getValue()
                                               .getAsJsonArray();
                List<JsonElement> metroStationsList = new LinkedList<>();
                for (JsonElement metroStationJsonElement : metroStations) {

                    // if (!metroStationJsonElement.getAsJsonObject().has("time")) {
                    //     ((JsonObject) metroStationJsonElement).add("time", JsonNull.INSTANCE);
                    // }

                    metroStationsList.add(metroStationJsonElement);

                }
                jsonMap.put(metroLineName, metroStationsList);
            }
            return jsonMap;
        } catch (IllegalStateException e) {
            e.printStackTrace();
            throw new IllegalStateException("Incorrect file");
        }

    }

    public static Map<String, List<String>> parseRuntimeArgs(String[] args) {
        Map<String, List<String>> runtimeArgsMap = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            if (runtimeArgsToMaxArgsMap.containsKey(args[i])) {
                List<String> vals = new ArrayList<>();
                int requiredVals = runtimeArgsToMaxArgsMap.get(args[i]);
                if (i + requiredVals < args.length) {
                    for (int j = 0; j < requiredVals; j++) {
                        i++;
                        vals.add(args[i]);
                    }
                    runtimeArgsMap.put(args[i - requiredVals], vals);
                } else {
                    throw new IllegalArgumentException("Invalid or incomplete arguments were passed.");
                }
            } else {
                throw new IllegalArgumentException("Unknown argument: " + args[i]);
            }
        }

        for (String arg : runtimeArgsMap.keySet()) {

            // No args = no checking for values
            if (runtimeArgsMap.get(arg).size() == 0) {
                continue;
            }

            if (runtimeArgsMap.get(arg).size() == 1) {
                String val = runtimeArgsMap.get(arg).get(0);
                if (!runtimeArgsCheckerMap.get(arg).check(val)) {
                    throw new UnsupportedOperationException("Invalid value for argument " + arg + ".");
                }
            }
        }

        return runtimeArgsMap;
    }

}
