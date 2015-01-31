# Introduction

Android library projects that provides easy to use and extensible Barcode Scanner views based on ZXing.

This library is a fork of [barcodescanner][1] by [dm77][2]. It removes support for the ZBar scanner and instead only relies on ZXing. Furthermore it adds the possibility to use an Android View as custom HUD.

# Installation

Add the following dependency to your build system:

**Group** `com.taig.android`

**Artifact** `barcode-scanner`

**Version** `1.0.0`

And also make sure to add my custom content resolver:

`http://taig.github.io/repository`

Full example for sbt:

````scala
resolvers += Resolver.url( "Taig", url( "http://taig.github.io/repository" ) )( ivyStylePatterns )

libraryDependencies += "com.taig.android" % "barcode-scanner" % "1.0.0"
````

# Usage

Add camera permission to your `AndroidManifest.xml` file:

```xml
<uses-permission android:name="android.permission.CAMERA" />
```
## Basic

Activity setup:

```java
public class SimpleScannerActivity extends Activity implements BarcodeScannerView.ResultHandler {
    private BarcodeScannerView mScannerView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        mScannerView = new BarcodeScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);                  // Set the scanner view as the content view
    }

    @Override
    public void onResume() {
        super.onResume();

        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {

        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {

        // Do something with the result here
        Log.v(TAG, rawResult.getText()); // Prints scan results
        Log.v(TAG, rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)
    }
}
```

## Advanced

To supply your custom HUD from xml:

**my_activity_view.xml**

````xml
<com.taig.android.barcode.scanner.BarcodeScannerView
	xmlns:android="http://schemas.android.com/apk/res/android"
	xmlns:scanner="http://schemas.android.com/apk/res-auto"    <!-- Important namespace import -->
	android:id="@+id/my_scanner"
	android:layout_width="match_parent"
	android:layout_height="match_parent"
	scanner:hud="@layout/scanner_hud" />                       <!-- Reference to a layout that will be placed above the scanner -->
````

**scanner_hud.xml**

````xml
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
             android:layout_width="match_parent"
	         android:layout_height="match_parent"
	         android:layout_gravity="center">

    <!--
         The view below is your crosshair, the area that the barcode decoder will analyze, which is kind of
         expensive. If you omit the crosshair id the whole screen will be analyzed which may be bad idea,
         performance wise.
    --> 

    <View id="@id/crosshair"
          android:layout_width="150dp"
	      android:layout_height="150dp"
	      android:background="@color/whatever" />

</FrameLayout>
````

From code you can use the `setHud()` methods of the `BarcodeScannerView`.

## More

For more details about flash and autofocus you should have a look at the original [documentation][1].

# TODO / Known Issues

- Distored image rendering
- Usage of deprecated Camera API
- Performance tweaks

# License

Apache License, Version 2.0

[1]: https://github.com/dm77/barcodescanner
[2]: https://github.com/dm77
