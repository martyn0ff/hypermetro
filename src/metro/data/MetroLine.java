package metro.data;

import metro.db.Database;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MetroLine {
    private final String name;
    private final List<MetroStation> metroStations;
    private final List<MetroStation> terminalStations;

    public MetroLine() {
        this.name = "Unknown Metro Line";
        this.metroStations = new LinkedList<>();
        this.terminalStations = new ArrayList<>();
    }

    public MetroLine(Map.Entry<String, List<MetroStation>> metroLineEntry) {
        this.name = metroLineEntry.getKey();
        this.metroStations = metroLineEntry.getValue();
        this.terminalStations = new ArrayList<>();
        findTerminalStations();
    }

    public void addNewMetroStation(MetroStation metroStation) {
        if (!Database.isMetroStationExists(metroStation.getMetroLineName(), metroStation.getName())) {
            metroStations.add(metroStation);
        }
    }

    public void findTerminalStations() {
        terminalStations.clear();
        for (MetroStation metroStation : metroStations) {
            if (!metroStation.hasNext() || !metroStation.hasPrev()) {
                terminalStations.add(metroStation);
            }
        }
    }

    public MetroStation get(int index) {
        return metroStations.get(index);
    }

    /* We count those stations that don't have next stations to be a head station */
    public List<MetroStation> getHeadStations() {
        List<MetroStation> headStations = new ArrayList<>();
        for (MetroStation metroStation : terminalStations) {
            if (!metroStation.hasNext()) {
                headStations.add(metroStation);
            }
        }
        return headStations;
    }

    public List<MetroStation> getMetroStations() {
        return new LinkedList<>(metroStations);
    }

    public String getName() {
        return name;
    }

    /* We count those stations that don't have prev stations to be a tail station */
    public List<MetroStation> getTailStations() {
        List<MetroStation> tailStations = new ArrayList<>();
        for (MetroStation metroStation : terminalStations) {
            if (!metroStation.hasPrev()) {
                tailStations.add(metroStation);
            }
        }
        return tailStations;
    }

    public List<MetroStation> getTerminalStations() {
        return new ArrayList<>(terminalStations);
    }

    public boolean isMetroStationExists(MetroStation metroStation) {
        return metroStations.contains(metroStation);
    }

    public int size() {
        return metroStations.size();
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("-".repeat(getName().length()));
        sb.append("\n");
        sb.append(getName());
        sb.append("\n");
        sb.append("-".repeat(getName().length()));
        sb.append("\n");
        for (MetroStation metroStation : getMetroStations()) {
            sb.append(metroStation);
            sb.append("\n");
        }
        return sb.toString();
    }
}
