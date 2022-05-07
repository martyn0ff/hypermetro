package metro.commands;

import metro.Main;
import metro.data.MetroLine;
import metro.data.MetroStation;
import metro.db.Database;
import metro.io.InputReader;

import java.util.ArrayList;
import java.util.List;

public class FindMetroStationByNameCommand implements Command {
    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Please specify one argument: <station name>");
        }

        args = InputReader.parseArguments(args, 1);

        if (args == null) {
            throw new IllegalArgumentException(
                    "Please specify one argument: <station name>. Metro stations containing spaces should be encapsulated in double quotes.");

        }

        String metroStationName = args[0];
        List<MetroStation> result = new ArrayList<>();

        for (MetroLine metroLine : Database.getMetroLines()) {
            for (MetroStation metroStation : metroLine.getMetroStations()) {
                if (metroStation.getName().contains(metroStationName)) {
                    result.add(metroStation);
                }
            }
        }

        System.out.println("Found " + result.size() + " results.");

        for (MetroStation metroStation : result) {
            System.out.println("\t" + metroStation.getName() + " (" + metroStation.getMetroLineName() + ")");
        }

        if (result.size() > 0) {
            System.out.println();
            System.out.println("If you want get info about station, use " + Main.COMMAND_PREFIX + "stationinfo <line name> <station name> command.");
        }
    }
}
