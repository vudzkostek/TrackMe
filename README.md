# TrackMe
TrackMe allows you to manage all your tracking services.

Nowadays we use different tools for gathering data about crashes, ui experience, incomes etc. Each and every tool has to be managed separately and in order to send events to multiple tools we need to either add couple of lines in desired place or, like in most cases, we create some uber singleton for managing all these tools and sending events. TrackMe is a library that will help you manage your tracking tools.

## Getting started

### Setting up the dependency

The first step is to include TrackMe into your project, for example, as a Gradle compile dependency:

```groovy
implementation "TODO:trackme:x.y.z"
```

(Please replace `x` and `y` with the latest version numbers: [![Maven Central](https://maven-badges.herokuapp.com/maven-central/TODO.trackme/trackme/badge.svg)](https://maven-badges.herokuapp.com/maven-central/TODO.trackme/trackme)
)

### Creating a sink

Sink is an object that represents single tool for tracking. In order to create one you need to implement ```Sink``` interface or extend ```SharedStorageSink``` abstract class.

```java
import android.content.Context;
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
}
```

```SharedStorageSink``` abstract class allows you to create common object for keeping consents state and share this object between all sinks. In order to create shared storage you should implement ```ConsentStorage``` interface.

###Storage:
```java
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
```

###Sink:
```java
public class SampleSharedStorageSink extends SharedStorageSink {

   /*
    * Same as Sink implementation but you need to omit setConsent() and getConsent() overriding and provide storage object
    */
	
	...
	
    private ConsentStorage consentStorage;

    public SampleSharedStorageSink(@NonNull ConsentStorage storage){
        consentStorage = storage;
    }

    @NotNull
    @Override
    public ConsentStorage getStorage() {
        return consentStorage;
    }
}

```


