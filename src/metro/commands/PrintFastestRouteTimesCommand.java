package metro.commands;

import metro.db.Database;
import metro.data.MetroStation;
import metro.io.InputReader;
import metro.search.Search;

import java.util.Map;

public class PrintFastestRouteTimesCommand implements Command {
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
            throw new IllegalArgumentException("Metro station " + metroStationName + " (" + metroLineName + ") doesn't exist!");
        }

        MetroStation metroStation = Database.getMetroStation(metroLineName, metroStationName);

        if (Database.getDisabledMetroStations().contains(metroStation)) {
            throw new UnsupportedOperationException(metroStation.getFullName() + " is under maintenance and is not in use.");
        }

        Map<MetroStation, Integer> distMap = Search.findAllFastestRoutes(metroStation);

        for (MetroStation metroStation1 : distMap.keySet()) {
            System.out.println(metroStation1.getName() + " (" + metroStation1.getMetroLineName() + "): " + distMap.get(metroStation1) + " minutes");
        }
    }
}
