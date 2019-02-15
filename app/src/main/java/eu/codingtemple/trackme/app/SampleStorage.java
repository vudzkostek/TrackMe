package eu.codingtemple.trackme.app;

import eu.codingtemple.trackme.sink.ConsentStorage;
import eu.codingtemple.trackme.sink.Hashable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class SampleStorage implements ConsentStorage {

    private Map<Hashable, Boolean> consents = new HashMap<>();

    @Override
    public void setConsent(@NotNull Hashable sinkId, boolean consent) {
        consents.put(sinkId, consent);
    }

    @Override
    public boolean getConsent(@NotNull Hashable sinkId) {
        return consents.containsKey(sinkId) ? consents.get(sinkId) : false;
    }
}
