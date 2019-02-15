# TrackMe
TrackMe allows you to manage all your tracking services. It is a simple, lighweight library.

Nowadays we use different tools for gathering data about crashes, ui experience, incomes etc. Each and every tool has to be managed separately and in order to send events to multiple tools we need to either add couple of lines in desired place or, like in most cases, we create some uber singleton for managing all these tools and sending events. TrackMe is a library that will help you manage your tracking tools.

## Getting started

### Setting up the dependency

The first step is to include TrackMe into your project, for example, as a Gradle compile dependency:

```groovy
implementation "TODO:trackme:x.y.z"
```

(Please replace `x` and `y` with the latest version numbers: [![Maven Central](https://maven-badges.herokuapp.com/maven-central/TODO.trackme/trackme/badge.svg)](https://maven-badges.herokuapp.com/maven-central/TODO.trackme/trackme)
)

### Initializing TrackMe

Track me is not kept as a single instance. If you want to share same instance between all of your classes you should implement your own singleton provider.

```java
import android.app.Application;
import eu.codingtemple.trackme.sink.ConsentStorage;
import eu.codingtemple.trackme.sink.Hashable;
import org.jetbrains.annotations.NotNull;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ConsentStorage storage = new SampleStorage();
        SampleSink sink1 = new SampleSink();
        SampleSharedStorageSink sink2 = new SampleSharedStorageSink(storage);

        SinkStateListener listener = new SinkStateListener() {...};

        TrackMe trackMe = new TrackMe.Builder()
                .withBlocking(false) // True makes all track me calls synchronious
                .withConsentOverride(false, false) // You can override all consents with this
                .withSinkListener(listener) // Listen for all sinks state changes
                .withSink(sink1) // you can add as many sinks as needed, just remember to use unique ids
                .withSink(sink2)
                .build();
    }
}
```

After creating TrackMe instance we need to initialize it and start tracking process

```java
    @Override
    public void onCreate() {
        ...
        trackMe.initialize(this);
        trackMe.start();
    }
```

### Consent

Each Sink keeps his own user consent for tracking. It's up to you if this is a shared consent between all sinks, or dedicated consent for each tool.

```java
        trackMe.setConsentTrue(Arrays.asList(sink1.getId(), sink2.getId()));
        // or
        trackMe.setConsentTrue(sink1.getId());
```


### Loging events

Events in TrackMe require id and declararion of all sinks that there are supposed to be sent to. Additionally user is able to add some custom attributes to every event.

```java
   TargetEvent event = new TargetEvent.Builder("testEventId") // event Id
                .attribute("someCustomAttributeKey", "value") // custom value
                .sink(sink1.getId()) // Use sink unique id
                .build();
                
   ...
   
   trackMe.log(event);
```

Above sample will send event only to ```sink1```, other sinks will not receive it.

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


