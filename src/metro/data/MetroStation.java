package metro.data;

import metro.db.Database;

import java.util.*;

public class MetroStation {
    private final String metroLineName;
    private final String name;
    private final List<MetroStation> transfers;
    private final List<MetroStation> prev;
    private final List<MetroStation> next;
    private MetroLine metroLine;
    private Integer time;

    public MetroStation(String metroLineName, String name) {
        this.metroLineName = metroLineName;
        this.metroLine = new MetroLine();
        this.name = name;
        this.transfers = new ArrayList<>();
        this.time = null;
        this.prev = new ArrayList<>();
        this.next = new ArrayList<>();
    }
    public MetroStation(
            String metroLineName,
            String name,
            List<MetroStation> transfers,
            Integer time,
            List<MetroStation> prev,
            List<MetroStation> next
                       ) {
        this.metroLineName = metroLineName;
        this.name = name;
        this.transfers = transfers;
        this.time = time;
        this.prev = prev;
        this.next = next;
    }

    public void addNext(MetroStation metroStation) {
        if (this.getNext()
                .contains(metroStation)) {
            throw new UnsupportedOperationException(
                    "This station already contains " + metroStation + " as its next station.");
        }
        this.next.add(metroStation);
    }

    public void addPrev(MetroStation metroStation) {
        if (this.getPrev()
                .contains(metroStation)) {
            throw new UnsupportedOperationException(
                    "This station already contains " + metroStation + " as its previous station.");
        }
        this.prev.add(metroStation);
    }

    public void addTransfer(MetroStation metroStation) {
        this.transfers.add(metroStation);
    }

    public List<MetroStation> getAdjacentMetroStations() {
        List<MetroStation> adjacentMetroStations = new ArrayList<>();
        adjacentMetroStations.addAll(prev);
        adjacentMetroStations.addAll(next);
        adjacentMetroStations.addAll(transfers);
        return adjacentMetroStations;
    }

    public String getFullName() {
        return this.getName() + " (" + this.getMetroLineName() + ")";
    }

    public MetroLine getMetroLine() {
        return metroLine;
    }

    public String getMetroLineName() {
        return metroLineName;
    }

    public String getName() {
        return name;
    }

    public List<MetroStation> getNext() {
        return next;
    }

    public List<MetroStation> getPrev() {
        return prev;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
            this.time = time;
    }

    public List<MetroStation> getTransfers() {
        return transfers;
    }

    public boolean hasNext() {
        return next.size() > 0;
    }

    public boolean hasPrev() {
        return prev.size() > 0;
    }

    public boolean hasTransfers() {
        return transfers.size() > 0;
    }

    public boolean isDisabled() {
        return Database.getDisabledMetroStations()
                       .contains(this);
    }

    public boolean isHead() {
        return !this.hasPrev();
    }

    public boolean isTail() {
        return !this.hasNext();
    }

    public void printMetroStationInfoForCar() {
        List<MetroStation> thisWithAdjacentMetroStations = new LinkedList<>();
        thisWithAdjacentMetroStations.addAll(prev);

        if (prev.size() == 0) {
            thisWithAdjacentMetroStations.add(new Depot(metroLineName));
        }

        thisWithAdjacentMetroStations.add(this);
        thisWithAdjacentMetroStations.addAll(next);

        if (next.size() == 0) {
            thisWithAdjacentMetroStations.add(new Depot(metroLineName));
        }

        /* We don't want to print metro stations in usual format that contains transfers */
        List<String> thisWithAdjacentMetroStationsReduced = new LinkedList<>();

        for (MetroStation metroStation : thisWithAdjacentMetroStations) {
            if (metroStation == this) {
                thisWithAdjacentMetroStationsReduced.add(metroStation.getName());
            } else {
                thisWithAdjacentMetroStationsReduced.add(metroStation.getName());
            }
        }

        System.out.println(thisWithAdjacentMetroStationsReduced.toString()
                                                               .replace("[", "")
                                                               .replace(", ", " - ")
                                                               .replace("]", ""));
    }

    @Override
    public String toString() {
        return name;
    }
}
