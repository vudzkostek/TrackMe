package eu.codingtemple.trackme;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrackMe {

    private final Map<Hashable, Sink> sinks = new HashMap<>();
    private final boolean overrideConsent;
    private final boolean overrideValue;
    private final boolean silentCrashing;
    @Nullable
    private final SinkStateListener sinkListener;

    private TrackMe(Builder builder) {
        for (Sink sink : builder.sinks) {
            sinks.put(sink.getId(), sink);
        }
        this.overrideConsent = builder.overrideConsent;
        this.overrideValue = builder.overrideValue;
        this.silentCrashing = builder.silentCrashing;
        this.sinkListener = builder.sinkListener;
    }

    public class Builder {
        private List<Sink> sinks = new ArrayList<>();
        private boolean overrideConsent;
        private boolean overrideValue;
        private boolean silentCrashing = true;
        private SinkStateListener sinkListener;

        public Builder withSink(Sink sink) {
            sinks.add(sink);
            return this;
        }

        public Builder withConsentOverride(boolean overrideConsent, boolean overrideValue) {
            this.overrideConsent = overrideConsent;
            this.overrideValue = overrideValue;
            return this;
        }

        public Builder withSilentCrashing(boolean silentCrashing) {
            this.silentCrashing = silentCrashing;
            return this;
        }

        public Builder withSinkListener(@NonNull SinkStateListener sinkListener) {
            this.sinkListener = sinkListener;
            return this;
        }

        public TrackMe build() {
            return new TrackMe(this);
        }
    }
}
