package eu.codingtemple.trackme.app;

import android.app.Application;
import eu.codingtemple.trackme.SinkStateListener;
import eu.codingtemple.trackme.TrackMe;
import eu.codingtemple.trackme.event.TargetEvent;
import eu.codingtemple.trackme.sink.ConsentStorage;
import eu.codingtemple.trackme.sink.Hashable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class MainApplication extends Application {

    private TrackMe trackMe;

    @Override
    public void onCreate() {
        super.onCreate();

        ConsentStorage storage = new SampleStorage();
        SampleSink sink1 = new SampleSink();
        SampleSharedStorageSink sink2 = new SampleSharedStorageSink(storage);

        SinkStateListener listener = new SinkStateListener() {
            @Override
            public void onSinkInitialized(@NotNull Hashable sinkId) {

            }

            @Override
            public void onSinkInitError(@NotNull Throwable throwable) {

            }

            @Override
            public void onSinkStarted(@NotNull Hashable sinkId) {
                TargetEvent event = new TargetEvent.Builder("testEventId")
                        .attribute("someCustomAttributeKey", "value")
                        .sink(sink1.getId())
                        .build();

                getTrackMeInstance().log(event);
            }

            @Override
            public void onSinkFinished(@NotNull Hashable sinkId) {

            }

            @Override
            public void onAllSinksInitialized() {

            }

            @Override
            public void onError(@NotNull Throwable throwable) {

            }
        };

        trackMe = new TrackMe.Builder()
                .withBlocking(false)
                .withConsentOverride(false, false)
                .withSinkListener(listener)
                .withSink(sink1)
                .withSink(sink2)
                .build();

        trackMe.initialize(this);
        trackMe.start();

        trackMe.setConsentTrue(Arrays.asList(sink1.getId(), sink2.getId()));
    }

    public TrackMe getTrackMeInstance() {
        return trackMe;
    }
}
