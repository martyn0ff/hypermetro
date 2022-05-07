package metro.commands;

import metro.db.Database;

public class PrintMetroCommand implements Command {
    @Override
    public void execute(String[] args) {
        for (String metroLineName : Database.getMetroMap()
                                            .keySet()) {
            System.out.println("-".repeat(metroLineName.length()));
            System.out.println(metroLineName);
            System.out.println("-".repeat(metroLineName.length()));
            for (String metroStationName : Database.getMetroMap()
                                                   .get(metroLineName)
                                                   .keySet()) {

                System.out.println(Database.getMetroMap()
                                           .get(metroLineName)
                                           .get(metroStationName));
            }
            System.out.println();
        }
    }
}
