# Castro

A minimal API for Google Cast with utility Activity and Fragment classes. This library handles Menu setup, Connect/Reconnect, and Disconnect.

### AndroidManifest
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="..." >

    <application ...>

        <!-- Provide the id given to your app in the developer console -->
        <meta-data
            android:name="GoogleCastId"
            android:value="@string/app_id" />

            ...

    </application>

</manifest>
```

### Code
```java
// Can use an activity...
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

// Or a fragment
public class SampleFragment extends CastFragment {

    @Override
    public void onCastConnect(GoogleApiClient apiClient, boolean isReconnect) {

    }

    @Override
    public void onCastDisconnect(GoogleApiClient apiClient) {

    }
}
```

### Install
```gradle
compile 'com.manotaurgames.Castro:Castro-android:0.1'
```

### Acknowledgements
The original code for this library was unceremoniously lifted from Google's [Sample Code](https://github.com/googlecast/CastHelloText-android). I just wanted to get rid of all the awkward status handlers and callback hell.