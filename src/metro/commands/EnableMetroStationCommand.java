package metro.commands;

import metro.db.Database;
import metro.io.InputReader;

public class EnableMetroStationCommand implements Command {
    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Please specify two arguments: <line name> <station name>");
        }

        args = InputReader.parseArguments(args, 2);

        if (args == null) {
            throw new IllegalArgumentException(
                    "Please specify two arguments: <line name> <station name>. Stations containing spaces should be encapsulated in double quotes.");

        }

        String metroLineName = args[0];
        String metroStationName = args[1];

        if (!Database.isMetroLineExists(metroLineName)) {
            throw new IllegalArgumentException("Metro line " + metroLineName + " doesn't exist!");
        }

        if (!Database.isMetroStationExists(metroLineName, metroStationName)) {
            throw new IllegalArgumentException(
                    "Station " + metroStationName + " is not in " + metroLineName + " or doesn't exist!");
        }
            Database.enableMetroStation(metroLineName, metroStationName);
            System.out.println(metroStationName + " (" + metroLineName + ") is no longer in maintenance and is available for travel again.");
    }
}
