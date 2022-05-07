package metro;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import metro.db.Database;
import metro.data.MetroStation;
import metro.io.InputReader;
import metro.io.MetroStationSerializer;
import metro.io.Parser;

import java.io.*;
import java.util.List;
import java.util.Map;

public class Main {
    public static final String COMMAND_PREFIX = "/";
    public static boolean ENFORCE_CONNECTIVITY = false;
    public static int TRANSFER_TIME = 5;
    public static int TIMEOUT_MILLIS = 10000;
    public static String METRO_FILE = "../data/london.json";
    public static BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

    public static Gson gson = new GsonBuilder().setPrettyPrinting()
                                               .disableHtmlEscaping()
                                               .registerTypeAdapter(MetroStation.class, new MetroStationSerializer())
                                               // .serializeNulls()
                                               .create();


    private Main() {

    }

    public static void main(String[] args) {
        try {
            Map<String, List<String>> runtimeArgs = Parser.parseRuntimeArgs(args);
            setRuntimeArgs(runtimeArgs);

            Database.loadJsonFile(new File(METRO_FILE), ENFORCE_CONNECTIVITY);
            System.out.println();
            System.out.println("Use " + COMMAND_PREFIX + "help for more info.");

            while (true) {
                try {
                    InputReader.readCommand(bufferedReader);
                } catch (IllegalArgumentException | UnsupportedOperationException e) {
                    System.out.println(e.getMessage());
                }
            }

        } catch (NullPointerException | IllegalStateException | IllegalArgumentException | UnsupportedOperationException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }

    }

    public static void setRuntimeArgs(Map<String, List<String>> args) {
        for (String arg : args.keySet()) {
            switch (arg) {
                case "-metro":
                    METRO_FILE = args.get(arg).get(0);
                    break;
                case "-ttime":
                    TRANSFER_TIME = Integer.parseInt(args.get(arg).get(0));
                    break;
                case "-timeout":
                    TIMEOUT_MILLIS = Integer.parseInt(args.get(arg).get(0));
                    break;
                case "-enforce":
                    ENFORCE_CONNECTIVITY = true;
                    break;
            }
        }
    }

}
