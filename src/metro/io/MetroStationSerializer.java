package metro.io;

import com.google.gson.*;
import metro.data.MetroStation;

import java.lang.reflect.Type;

public class MetroStationSerializer implements JsonSerializer<MetroStation> {
    @Override
    public JsonElement serialize(
            MetroStation src, Type typeOfSrc, JsonSerializationContext context
                                ) {
        JsonObject result = new JsonObject();
        JsonArray transfers = new JsonArray();
        JsonArray prev = new JsonArray();
        JsonArray next = new JsonArray();

        boolean isTimeNull = src.getTime() == null;
        boolean isStationDisabled = src.isDisabled();

        for (MetroStation metroStation : src.getPrev()) {
            prev.add(metroStation.getName());
        }

        for (MetroStation metroStation : src.getNext()) {
            next.add(metroStation.getName());
        }

        for (MetroStation metroStation : src.getTransfers()) {
            JsonObject transfer = new JsonObject();
            transfer.add("line", new JsonPrimitive(metroStation.getMetroLineName()));
            transfer.add("station", new JsonPrimitive(metroStation.getName()));
            transfers.add(transfer);
        }
        result.add("name", new JsonPrimitive(src.getName()));
        result.add("prev", prev);
        result.add("next", next);
        result.add("transfer", transfers);
        if (!isTimeNull) {
            result.add("time", new JsonPrimitive(src.getTime()));
        }
        if (isStationDisabled) {
            result.add("maintenance", new JsonPrimitive(true));
        }

        return result;
    }
}
