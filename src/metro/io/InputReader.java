package metro.io;

import metro.Main;
import metro.commands.Command;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputReader {
    public static void readCommand(BufferedReader reader) {
        try {
            String input = reader.readLine();
            String[] inputArray = input.split(" ");

            if (!input.startsWith(Main.COMMAND_PREFIX)) {
                throw new IllegalArgumentException("Invalid command");
            } else {
                boolean validCommand = false;
                for (Map.Entry<String, List<String>> shorthand : Command.shorthands.entrySet()) {
                    if (shorthand.getValue().contains(inputArray[0].replace(Main.COMMAND_PREFIX, ""))) {
                        validCommand = true;
                        Command command = Command.availableCommands.get(shorthand.getKey());
                        String[] commandArgs = Arrays.copyOfRange(inputArray, 1, inputArray.length);
                        command.execute(commandArgs);
                    }
                }
                if (!validCommand) {
                    throw new IllegalArgumentException("Invalid command");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readInput(BufferedReader reader) {
        try {
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /* Parses arguments and removes all quotes. Returns null if amount of parsed arguments exceeds maximum allowed arguments */
    public static String[] parseArguments(String[] args, int maxArgs) {
        if (!checkArguments(args)) {
            throw new IllegalArgumentException("Invalid arguments - any quotes missing?");
        } else {
            String parsedArgsString = Arrays.toString(args)
                                            .replace("[", "")
                                            .replace(", ", " ")
                                            .replace("]", "");

            Pattern pattern = Pattern.compile("\\b([\\w\\d\\-/.]+)\\b|\"(.*?)\"");
            Matcher matcher = pattern.matcher(parsedArgsString);

            List<String> parsedArgs = new ArrayList<>();

            while (matcher.find()) {
                if (matcher.group(1) != null) {
                    parsedArgs.add(matcher.group(1));
                }
                if (matcher.group(2) != null) {
                    parsedArgs.add(matcher.group(2));
                }
            }

            return parsedArgs.size() == maxArgs ? parsedArgs.toArray(new String[0]) : null;
        }
    }

    /* Check for even amount of quotes */
    public static boolean checkArguments(String[] args) {
        int quotes = 0;
        for (String token : args) {
            quotes += token.chars()
                           .filter(c -> c == 0x22)
                           .count();
        }
        return quotes % 2 == 0;
    }
}
