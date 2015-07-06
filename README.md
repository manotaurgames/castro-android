# SimpleCast

A minimal API for Google Cast with utility Activity and Fragment classes.

### Usage
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="..." >

    <application ...>

        <meta-data
            android:name="SimpleCastId"
            android:value="@string/app_id" />

            ...

    </application>

</manifest>
```

```java
public class SampleActivity extends CastActivity {

    @Override
    public void onCastConnect(GoogleApiClient apiClient, boolean isReconnect) {
        // Do some logging or maybe show something on the UI
    }

    @Override
    public void onCastDisconnect(GoogleApiClient apiClient) {
        // Clean everything up, or show a popup
    }
}
```

### Install
```gradle
compile 'com.manotaurgames.simplecast:simplecast-android:0.1'
```

### Acknowledgements
The original code for this library was unceremoniously lifted from Google's [https://github.com/googlecast/CastHelloText-android](Sample Code). I just wanted to get rid of all the awkward status handlers and callback hell.