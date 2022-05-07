package metro.commands;

import metro.data.MetroLine;
import metro.data.MetroStation;
import metro.db.Database;
import metro.io.InputReader;

public class PrintMetroLineCommand implements Command {
    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Please specify one argument: <line name>");
        }

        args = InputReader.parseArguments(args, 1);

        if (args == null) {
            throw new IllegalArgumentException(
                    "Please specify one argument: <line name>. Metro lines containing spaces should be encapsulated in double quotes.");

        }

        String metroLineName = args[0];

        if (!Database.isMetroLineExists(metroLineName)) {
            throw new IllegalArgumentException("Metro line " + metroLineName + " doesn't exist!");
        }

        MetroLine metroLine = Database.getMetroLine(metroLineName);

        System.out.println(metroLine);
        System.out.println("Total stations: " + metroLine.size());
        int totalTravelTime = 0;
        for (MetroStation metroStation : metroLine.getMetroStations()) {
            if (metroStation.getTime() != null) {
                totalTravelTime += metroStation.getTime();
            }
        }
        System.out.println("Total travel time: " + totalTravelTime + " minutes");
        System.out.println("Head(s): ");
        for (MetroStation metroStation : metroLine.getHeadStations()) {
            System.out.println("\t" + metroStation.getName());
        }
        System.out.println("Tail(s): ");
        for (MetroStation metroStation : metroLine.getTailStations()) {
            System.out.println("\t" + metroStation.getName());
        }
    }
}
