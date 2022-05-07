package metro.commands;

import metro.Main;
import metro.data.MetroLine;
import metro.db.Database;

public class PrintMetroInfoCommand implements Command {
    @Override
    public void execute(String[] args) {
        System.out.println("JSON file: " + Database.getJsonFilePath());
        System.out.println(Database.getNumLines() + " metro lines and " + Database.getNumStations() +
                           " metro stations are loaded.");
        System.out.println("Available metro lines: ");
        for (MetroLine metroLine : Database.getMetroLines()) {
            System.out.println("\t" + metroLine.getName() + " (" + metroLine.size() + " stations)");
        }
        System.out.println();
        System.out.println("To get info about metro line, use " + Main.COMMAND_PREFIX + "lineinfo <line name>");
    }
}
