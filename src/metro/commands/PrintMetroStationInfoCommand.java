package metro.commands;

import metro.db.Database;
import metro.data.MetroStation;
import metro.io.InputReader;

public class PrintMetroStationInfoCommand implements Command {
    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Please specify two arguments: <line name> <station name>");
        }

        args = InputReader.parseArguments(args, 2);

        if (args == null) {
            throw new IllegalArgumentException(
                    "Please specify two arguments: <line name> <station name>. Stations or metro lines containing spaces should be encapsulated in double quotes.");

        }

        String metroLineName = args[0];
        String metroStationName = args[1];

        if (!Database.isMetroLineExists(metroLineName)) {
            throw new IllegalArgumentException("Metro line " + metroLineName + " doesn't exist!");
        } else if (!Database.isMetroStationExists(metroLineName, metroStationName)) {
            throw new IllegalArgumentException(
                    "Station " + metroStationName + " (" + metroLineName + ") doesn't exist.");
        }

        MetroStation metroStation = Database.getMetroStation(metroLineName, metroStationName);
        assert metroStation != null;

        System.out.println("============");
        System.out.println("STATION INFO");
        System.out.println("============");
        System.out.println();
        if (metroStation.isDisabled()) {
            System.out.println("!!! " + metroStation + " is currently under maintenance !!!");
            System.out.println("!!! NOT AVAILABLE FOR TRAVEL !!!");
            System.out.println();
        }
        System.out.println("Station name: " + metroStation.getName());
        System.out.println("Station line name: " + metroStation.getMetroLineName());
        System.out.println("Previous stations: " + metroStation.getPrev()
                                                               .size());
        for (MetroStation prevMetroStation : metroStation.getPrev()) {
            System.out.println("\t" + prevMetroStation.getName());
        }
        System.out.println("Next stations: " + metroStation.getNext()
                                                           .size());
        for (MetroStation nextMetroStation : metroStation.getNext()) {
            System.out.println("\t" + nextMetroStation.getName());
        }
        if (metroStation.getTime() != null) {
            System.out.println("Time: " + metroStation.getTime());
        }
        System.out.println("Transfers: " + metroStation.getTransfers()
                                                       .size());
        for (MetroStation transferMetroStation : metroStation.getTransfers()) {
            System.out.println(
                    "\t to " + transferMetroStation.getName() + " (" + transferMetroStation.getMetroLineName() + ")");
        }


    }
}
