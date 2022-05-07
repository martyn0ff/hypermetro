package metro.commands;

import metro.db.Database;
import metro.data.MetroStation;
import metro.io.InputReader;
import metro.search.Search;

import java.util.List;

public class FindLeastHopsRouteCommand implements Command {
    @Override
    public void execute(String[] args) {
        if (args.length < 4) {
            throw new IllegalArgumentException(
                    "Please specify four arguments: <line 1 name> <station 1 name> <line 2 name> <station 2 name>");
        }

        args = InputReader.parseArguments(args, 4);

        if (args == null) {
            throw new IllegalArgumentException(
                    "Please specify four arguments: <line 1 name> <station 1 name> <line 2 name> <station 2 name>. Stations or metro lines containing spaces should be encapsulated in double quotes.");

        }

        String metroLine1Name = args[0];
        String metroStation1Name = args[1];
        String metroLine2Name = args[2];
        String metroStation2Name = args[3];

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

        MetroStation metroStation1 = Database.getMetroStation(metroLine1Name, metroStation1Name);
        MetroStation metroStation2 = Database.getMetroStation(metroLine2Name, metroStation2Name);

        List<MetroStation> leastHopsRoute = Search.findLeastHopsRoute(metroStation1, metroStation2);

        if (leastHopsRoute == null) {
            throw new UnsupportedOperationException(
                    "Impossible to build a route, that may happen when some station on the route is disabled and there are no other ways to travel around it.");
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < leastHopsRoute.size(); i++) {
            sb.append(leastHopsRoute.get(i)
                                    .getName());
            if (i < leastHopsRoute.size() - 1 && leastHopsRoute.get(i)
                                                               .getName()
                                                               .equals(leastHopsRoute.get(i + 1)
                                                                                     .getName())) {
                sb.append("\n");
                sb.append("-- Transfer to ");
                sb.append(leastHopsRoute.get(i + 1)
                                        .getMetroLineName());
                sb.append("--\n");
                sb.append(leastHopsRoute.get(i)
                                        .getName());
                i++;
            }
            sb.append("\n");

        }
        System.out.println(sb);
    }
}
