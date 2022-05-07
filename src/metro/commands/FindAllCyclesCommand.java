package metro.commands;

import metro.data.MetroLine;
import metro.data.MetroStation;
import metro.db.Database;
import metro.search.Search;

import java.util.LinkedList;
import java.util.List;

public class FindAllCyclesCommand implements Command {
    @Override
    public void execute(String[] args) {
        for (MetroLine metroLine : Database.getMetroLines()) {
            String metroLineName = metroLine.getName();
            MetroStation terminalStation = Database.getMetroLine(metroLineName)
                                                   .getTerminalStations()
                                                   .get(0);
            List<List<MetroStation>> cycles = Search.findCycles(terminalStation,
                                                                terminalStation,
                                                                false,
                                                                new LinkedList<>(),
                                                                new LinkedList<>(),
                                                                new LinkedList<>()
                                                               );
            for (List<MetroStation> cycle : cycles) {
                System.out.println("Cycle in " + terminalStation.getMetroLineName() + ":");
                int totalTime = 0;
                for (int i = 0; i < cycle.size(); i++) {
                    System.out.println("\t" + cycle.get(i)
                                                   .getName());

                    /* Since cycle includes the station we've started from, we need to ignore its time once we reach it */
                    if (i != cycle.size() - 1) {
                        totalTime += cycle.get(i)
                                          .getTime();
                    }
                }
                System.out.println("----------");
                System.out.println("Stations in cycle: " + cycle.size());
                System.out.println("Time to travel the cycle: " + totalTime + " minutes");
                System.out.println();
            }
        }
    }
}
