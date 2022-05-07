package metro.commands;

import metro.db.Database;
import metro.data.MetroStation;
import metro.io.InputReader;
import metro.search.Search;

import java.util.List;
import java.util.Map;

public class FindFastestRouteCommand implements Command {
    @Override
    public void execute(String[] args) {
        if (args.length < 4) {
            throw new IllegalArgumentException(
                    "Please specify four arguments: <from line name> <from station name> <to line name> <to station name>");
        }

        args = InputReader.parseArguments(args, 4);

        if (args == null) {
            throw new IllegalArgumentException(
                    "Please specify four arguments: <from line name> <from station name> <to line name> <to station name>. Stations containing spaces should be encapsulated in double quotes.");

        }

        String metroLine1Name = args[0];
        String metroStation1Name = args[1];
        String metroLine2Name = args[2];
        String metroStation2Name = args[3];

        if (!Database.isMetroLineExists(metroLine1Name)) {
            throw new IllegalArgumentException("Metro line " + metroLine1Name + " doesn't exist!");
        }
        if (!Database.isMetroLineExists(metroLine2Name)) {
            throw new IllegalArgumentException("Metro line " + metroLine2Name + " doesn't exist!");
        }
        if (!Database.isMetroStationExists(metroLine1Name, metroStation1Name)) {
            throw new IllegalArgumentException(
                    "Metro station " + metroStation1Name + " (" + metroLine1Name + ") doesn't exist!");
        }
        if (!Database.isMetroStationExists(metroLine2Name, metroStation2Name)) {
            throw new IllegalArgumentException(
                    "Metro station " + metroStation2Name + " (" + metroLine2Name + ") doesn't exist!");
        }

        MetroStation metroStationFrom = Database.getMetroStation(metroLine1Name, metroStation1Name);
        MetroStation metroStationTo = Database.getMetroStation(metroLine2Name, metroStation2Name);

        if (Database.getDisabledMetroStations()
                    .contains(metroStationFrom)) {
            throw new UnsupportedOperationException(metroStationFrom + " is under maintenance and is not in use.");
        }

        if (Database.getDisabledMetroStations()
                    .contains(metroStationTo)) {
            throw new UnsupportedOperationException(metroStationTo + " is under maintenance and is not in use.");
        }

        Map<MetroStation, Integer> distanceMap = Search.findAllFastestRoutes(metroStationFrom);
        List<MetroStation> route = Search.findFastestRoute(metroStationFrom, metroStationTo);

        if (route == null) {
            throw new UnsupportedOperationException(
                    "Impossible to build a route, that may happen when some station on the route is disabled and there are no other ways to travel around it.");
        }

        for (int i = 0; i < route.size(); i++) {
            boolean isTransferStation = false;
            for (MetroStation transferMetroStation : route.get(i)
                                                          .getTransfers()) {
                if (i != route.size() - 1 && transferMetroStation == route.get(i + 1)) {
                    isTransferStation = true;
                    System.out.println(route.get(i)
                                            .getName());
                    System.out.println("-- Transfer to " + transferMetroStation.getMetroLineName() + " --");
                    System.out.println(route.get(++i)
                                            .getName());
                }
            }
            if (!isTransferStation) {
                System.out.println(route.get(i)
                                        .getName());
            }
        }
        System.out.println("------------");
        System.out.println("Total: " + distanceMap.get(metroStationTo) + " minutes on the way");
    }
}
