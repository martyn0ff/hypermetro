package metro.commands;

import metro.Main;
import metro.data.MetroLine;
import metro.db.Database;
import metro.io.InputReader;

public class AddHeadCommand implements Command {
    @Override
    public void execute(String[] args) {

        if (args.length < 3) {
            throw new IllegalArgumentException("Please specify three arguments: <line name> <station name> <time>");
        }

        args = InputReader.parseArguments(args, 3);

        if (args == null) {
            throw new IllegalArgumentException(
                    "Please specify three arguments: <line name> <station name> <time>. Stations containing spaces should be encapsulated in double quotes.");

        }

        String metroLineName = args[0];
        String newMetroStationName = args[1];
        String time = args[2];

        if (!Database.isMetroLineExists(metroLineName)) {
            throw new IllegalArgumentException("Metro line " + metroLineName + " doesn't exist!");
        } else if (Database.isMetroStationExists(metroLineName, newMetroStationName)) {
            throw new IllegalArgumentException(
                    "Station " + newMetroStationName + " already exists in " + metroLineName);
        } else if (!time.matches("\\d+")) {
            throw new IllegalArgumentException("Please specify correct time.");
        } else {
            String currentHeadMetroStationName;
            MetroLine metroLine = Database.getMetroLine(metroLineName);
            int headStations = metroLine.getHeadStations()
                                        .size();
            if (headStations > 1) {
                System.out.print(metroLineName + " has more than one head station: ");
                for (int i = 0; i < headStations; i++) {
                    System.out.print("(" + (i + 1) + ") " + metroLine.getHeadStations()
                                                                     .get(i)
                                                                     .getName());
                    if (i != headStations - 1) {
                        System.out.print(", ");
                    }
                }
                System.out.print(", (0) CANCEL");
                System.out.println();
                System.out.print("Choose head station of interest: ");
                String choice = InputReader.readInput(Main.bufferedReader);
                while (!choice.matches("\\d+") || Integer.parseInt(choice) < 0 ||
                       Integer.parseInt(choice) > headStations) {
                    System.out.print("Unrecognized choice. Choose head station of interest: ");
                    choice = InputReader.readInput(Main.bufferedReader);
                }
                if (Integer.parseInt(choice) == 0) {
                    return;
                }
                currentHeadMetroStationName = metroLine.getHeadStations()
                                                       .get(Integer.parseInt(choice) - 1)
                                                       .getName();

            } else {
                currentHeadMetroStationName = metroLine.getHeadStations()
                                                       .get(0)
                                                       .getName();
            }

            Database.append(metroLineName, currentHeadMetroStationName, newMetroStationName, Integer.parseInt(time));
            System.out.println(
                    "Successfully added new head station " + newMetroStationName + " to " + metroLineName + ".");

        }
    }
}
