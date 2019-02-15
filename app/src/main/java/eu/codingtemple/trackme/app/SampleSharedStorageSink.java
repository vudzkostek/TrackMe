package eu.codingtemple.trackme.app;

import android.content.Context;
import android.support.annotation.NonNull;
import eu.codingtemple.trackme.event.Event;
import eu.codingtemple.trackme.sink.*;
import io.reactivex.Single;
import kotlin.Pair;
import org.jetbrains.annotations.NotNull;

public class SampleSharedStorageSink extends SharedStorageSink {

    private StringSinkId sinkId = new StringSinkId("uniqueSinkId");
    private boolean isConsentGiven;
    private boolean isInitialized;
    private ConsentStorage consentStorage;

    public SampleSharedStorageSink(@NonNull ConsentStorage storage){
        consentStorage = storage;
    }

    @NotNull
    @Override
    public Hashable getId() {
        return sinkId;
    }

    @NotNull
    @Override
    public Single<Pair<Hashable, Boolean>> initialize(@NotNull Context context) {
        return Single.fromCallable(() -> {
            boolean success = true;
            // Initialize your tracking library
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
            return new Pair<>(sinkId, success);
        });
    }

    @NotNull
    @Override
    public Single<Pair<Hashable, Boolean>> start() {
        return Single.fromCallable(() -> {
            boolean success = true;
            // Start tracking
            return new Pair<>(sinkId, success);
        });
    }

    @NotNull
    @Override
    public Single<Pair<Hashable, Boolean>> finish() {
        return Single.fromCallable(() -> {
            boolean success = true;
            // Finish tracking
            return new Pair<>(sinkId, success);
        });
    }

    @NotNull
    @Override
    public ConsentStorage getStorage$lib_debug() {
        return consentStorage;
    }
}
