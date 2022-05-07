package metro.commands;

import java.util.List;
import java.util.Map;

public interface Command {

    Map<String, List<String>> shorthands = Map.ofEntries(Map.entry("append", List.of("append", "addhead", "ah")),
                                                         Map.entry("prepend", List.of("prepend", "addtail", "at")),
                                                         Map.entry("output", List.of("output", "out", "o")),
                                                         Map.entry("exit", List.of("exit", "quit", "q", "e")),
                                                         Map.entry("metro",
                                                                   List.of("metro", "m")
                                                                  ),
                                                         Map.entry("routes", List.of("routes", "r")),
                                                         Map.entry("route", List.of("route", "ro")),
                                                         Map.entry("help", List.of("help", "commands", "h")),
                                                         Map.entry("fastestroutes", List.of("fastestroutes", "frs")),
                                                         Map.entry("fastestroute", List.of("fastestroute", "fr")),
                                                         Map.entry("stationinfo", List.of("stationinfo", "info", "si")),
                                                         Map.entry("findstation", List.of("findstation", "find", "fs")),
                                                         Map.entry("metroinfo",
                                                                   List.of("metroinfo")
                                                                  ),
                                                         Map.entry("lineinfo",
                                                                   List.of("lineinfo", "line", "l")
                                                                  ),
                                                         Map.entry("loadmetro",
                                                                   List.of("loadmetro", "load", "import")
                                                                  ),
                                                         Map.entry("cycles", List.of("cycles", "cy")),
                                                         Map.entry("savemetro",
                                                                   List.of("savemetro", "save", "export")
                                                                  ),
                                                         Map.entry("disable", List.of("disable", "maintenance")),
                                                         Map.entry("enable", List.of("enable", "open"))

                                                        );

    Map<String, Command> availableCommands = Map.ofEntries(Map.entry("prepend", new AddTailCommand()),
                                                           Map.entry("output", new PrintCarOutputCommand()),
                                                           Map.entry("append", new AddHeadCommand()),
                                                           Map.entry("exit", new ExitCommand()),
                                                           Map.entry("metro", new PrintMetroCommand()),
                                                           Map.entry("routes", new FindAllRoutesCommand()),
                                                           Map.entry("route", new FindLeastHopsRouteCommand()),
                                                           Map.entry("help", new PrintHelpCommand()),
                                                           Map.entry("fastestroutes",
                                                                     new PrintFastestRouteTimesCommand()
                                                                    ),
                                                           Map.entry("fastestroute", new FindFastestRouteCommand()),
                                                           Map.entry("stationinfo", new PrintMetroStationInfoCommand()),
                                                           Map.entry("findstation",
                                                                     new FindMetroStationByNameCommand()
                                                                    ),
                                                           Map.entry("metroinfo", new PrintMetroInfoCommand()),
                                                           Map.entry("lineinfo", new PrintMetroLineCommand()),
                                                           Map.entry("loadmetro", new ImportMetroCommand()),
                                                           Map.entry("cycles", new FindAllCyclesCommand()),
                                                           Map.entry("savemetro", new ExportMetroCommand()),
                                                           Map.entry("disable", new DisableMetroStationCommand()),
                                                           Map.entry("enable", new EnableMetroStationCommand())
                                                          );

    void execute(String[] args);
}