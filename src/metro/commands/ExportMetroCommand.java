package metro.commands;

import metro.Main;
import metro.db.Database;
import metro.io.InputReader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ExportMetroCommand implements Command {
    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException(
                    "Please specify one argument: <file name>");
        }

        args = InputReader.parseArguments(args, 1);

        if (args == null) {
            throw new IllegalArgumentException(
                    "Please specify one argument: <file name>. File names containing spaces should be encapsulated in double quotes.");

        }

        String fileName = args[0];
        File jsonFile = new File("./data/" + fileName);

        jsonFile.getParentFile().mkdir();

        try {
            FileWriter fileWriter = new FileWriter(jsonFile);
            fileWriter.write(Main.gson.toJson(Database.getJsonMap()).trim());
            System.out.println("Successfully written current metro to " + jsonFile.getCanonicalPath());
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
