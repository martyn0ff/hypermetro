package metro.commands;

import metro.Main;
import metro.db.Database;
import metro.io.InputReader;

import java.io.File;

public class ImportMetroCommand implements Command {
    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException(
                    "Please specify two arguments: <path to JSON file> <enforce productivity flag>");
        }

        args = InputReader.parseArguments(args, 2);


        if (args == null) {
            throw new IllegalArgumentException(
                    "Please specify two arguments: <path to JSON file> <enforce productivity flag. If path contains spaces, it should be encapsulated in double quotes.");

        }

        String fileName = args[0];
        String enforceConnectivity = args[1];
        File file = new File("./data/" + fileName);

        if (!file.exists()) {
            throw new IllegalArgumentException("The file " + fileName + " doesn't exist!");
        }

        if (!enforceConnectivity.equalsIgnoreCase("true") && !enforceConnectivity.equalsIgnoreCase("false")) {
            throw new UnsupportedOperationException("Possible values for enforce connectivity flag: true, false");
        }

        boolean enforceConnectivityBoolean = Boolean.parseBoolean(enforceConnectivity);
        Main.ENFORCE_CONNECTIVITY = Boolean.parseBoolean(enforceConnectivity);
        Database.loadJsonFile(file, enforceConnectivityBoolean);

    }
}
