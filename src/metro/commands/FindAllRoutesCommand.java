package metro.commands;

import metro.Main;
import metro.db.Database;
import metro.data.MetroStation;
import metro.io.InputReader;
import metro.search.Search;

import java.util.*;

public class FindAllRoutesCommand implements Command {

    @Override
    public void execute(String[] args) {
        if (args.length < 5) {
            throw new IllegalArgumentException(
                    "Please specify five arguments: <line 1 name> <station 1 name> <line 2 name> <station 2 name> <max routes>.");
        }

        args = InputReader.parseArguments(args, 5);

        if (args == null) {
            throw new IllegalArgumentException(
                    "Please specify five arguments: <line 1 name> <station 1 name> <line 2 name> <station 2 name> <max routes>. Stations or metro lines containing spaces should be encapsulated in double quotes.");

        }

        String metroLine1Name = args[0];
        String metroStation1Name = args[1];
        String metroLine2Name = args[2];
        String metroStation2Name = args[3];
        String maxRoutes = args[4];

        if (!Database.isMetroLineExists(metroLine1Name)) {
            throw new IllegalArgumentException("Metro line " + metroLine1Name + " doesn't exist.");
        }
        if (!Database.isMetroLineExists(metroLine2Name)) {
            throw new IllegalArgumentException("Metro line " + metroLine2Name + " doesn't exist.");
        }
        if (!Database.isMetroStationExists(metroLine1Name, metroStation1Name)) {
            throw new IllegalArgumentException(
                    "Metro station " + metroStation1Name + " (" + metroLine1Name + ") doesn't exist.");
        }
        if (!Database.isMetroStationExists(metroLine2Name, metroStation2Name)) {
            throw new IllegalArgumentException(
                    "Metro station " + metroStation2Name + " (" + metroLine2Name + ") doesn't exist.");
        }
        if (!maxRoutes.matches("\\d+")) {
            throw new IllegalArgumentException(
                    "Incorrect max routes value.");
        }

        MetroStation metroStation1 = Database.getMetroStation(metroLine1Name, metroStation1Name);
        MetroStation metroStation2 = Database.getMetroStation(metroLine2Name, metroStation2Name);

        if (Database.getDisabledMetroStations().contains(metroStation1)) {
            throw new UnsupportedOperationException(metroStation1.getFullName() + " is under maintenance and is not in use.");
        }

        if (Database.getDisabledMetroStations().contains(metroStation2)) {
            throw new UnsupportedOperationException(metroStation2.getFullName() + " is under maintenance and is not in use.");
        }

        int maxRoutesInt = Integer.parseInt(maxRoutes);

        List<List<MetroStation>> allRoutes = Search.findAllRoutes(metroStation1, metroStation2, maxRoutesInt, Main.TIMEOUT_MILLIS);

        if (allRoutes.size() == 0) {
            throw new UnsupportedOperationException("Impossible to build a route, that may happen when some station on the route is disabled and there are no other ways to travel around it or max route value is too small.");
        }

        int routeNo = 0;
        for (List<MetroStation> route : allRoutes) {
            routeNo++;
            StringBuilder sb = new StringBuilder("Route ");
            sb.append(routeNo);
            sb.append("\n");
            sb.append("-".repeat(sb.length() - 1));
            sb.append("\n");
            for (int i = 0; i < route.size(); i++) {
                sb.append(route.get(i)
                               .getName());
                if (i < route.size() - 1 && route.get(i)
                                                 .getName()
                                                 .equals(route.get(i + 1)
                                                              .getName())) {
                    sb.append("\n");
                    sb.append("-- Transfer to ");
                    sb.append(route.get(i + 1)
                                   .getMetroLineName());
                    sb.append(" --\n");
                } else {
                    sb.append("\n");
                }
            }
            System.out.println(sb);
        }
    }
}
