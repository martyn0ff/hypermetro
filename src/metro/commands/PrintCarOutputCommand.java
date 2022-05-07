package metro.commands;

import metro.db.Database;
import metro.data.MetroStation;
import metro.io.InputReader;

public class PrintCarOutputCommand implements Command {
    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Please specify two arguments: <line name> <metro station name>");
        }

        args = InputReader.parseArguments(args, 2);

        if (args == null) {
            throw new IllegalArgumentException(
                    "Please specify two arguments: <line name> <metro station name>. Metro lines and stations containing spaces should be encapsulated in double quotes.");

        }

        String metroLineName = args[0];
        String metroStationName = args[1];

        if (!Database.isMetroLineExists(metroLineName)) {
            throw new IllegalArgumentException("Metro line " + metroLineName + " was not found.");
        }
        if (!Database.isMetroStationExists(metroLineName, metroStationName)) {
            throw new IllegalArgumentException("Metro station " + metroStationName + " was not found.");
        }

        MetroStation metroStation = Database.getMetroStation(metroLineName, metroStationName);
        metroStation.printMetroStationInfoForCar();
    }
}
