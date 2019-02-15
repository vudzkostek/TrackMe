package eu.codingtemple.trackme.app;

import android.content.Context;
import android.util.Log;
import eu.codingtemple.trackme.event.Event;
import eu.codingtemple.trackme.sink.Hashable;
import eu.codingtemple.trackme.sink.Sink;
import eu.codingtemple.trackme.sink.StringSinkId;
import io.reactivex.Single;
import kotlin.Pair;
import org.jetbrains.annotations.NotNull;

public class SampleSink implements Sink {

    private StringSinkId sinkId = new StringSinkId("uniqueSinkId");
    private boolean isConsentGiven;
    private boolean isInitialized;

    @NotNull
    @Override
    public Hashable getId() {
        return sinkId;
    }

    @Override
    public boolean getConsent() {
        return isConsentGiven;
    }

    @Override
    public void setConsent(boolean consent) {
        isConsentGiven = consent;
    }

    @NotNull
    @Override
    public Single<Pair<Hashable, Boolean>> initialize(@NotNull Context context) {
        return Single.fromCallable(() -> {
            boolean success = true;
            // Initialize your tracking library
            Log.i("TEST", "Init sink " + getId());
            isInitialized = success;
            return new Pair<>(sinkId, success);
        });
    }

    @Override
    public boolean isInitialized() {
        return isInitialized;
    }

    @NotNull
    @Override
    public Single<Pair<Hashable, Boolean>> log(@NotNull Event event) {
        return Single.fromCallable(() -> {
            boolean success = true;
            // Log event
            Log.i("TEST", "Log event " + event.getEventId() + " with sink " + getId());
            return new Pair<>(sinkId, success);
        });
    }

    @NotNull
    @Override
    public Single<Pair<Hashable, Boolean>> start() {
        return Single.fromCallable(() -> {
            boolean success = true;
            // Start tracking
            Log.i("TEST", "Start sink " + getId());
            return new Pair<>(sinkId, success);
        });
    }

    @NotNull
    @Override
    public Single<Pair<Hashable, Boolean>> finish() {
        return Single.fromCallable(() -> {
            boolean success = true;
            // Finish tracking
            Log.i("TEST", "Finish sink " + getId());
            return new Pair<>(sinkId, success);
        });
    }
}
