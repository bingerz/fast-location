# FastLocation

An Android platform library to quickly request the location.

This dependency library integrates Android native API and Google play service API.

If you use it in the app of the original Android (Support Google play service), please add com.google.android.gms:play-services-location dependency Library

## Adding to your project
You should add this to your dependencies:

```groovy
implementation 'cn.bingerz.android:fastlocation:1.2.1-SNAPSHOT'
```

## Starting

For starting the location service:
```java
    
    //Use Context to initialize the object
    FastLocation fastLocation = new FastLocation(this);
```

Get the location using the default location strategy
```java
    //Start to get location
    boolean isSingle = true; //Single request location;
    fastLocation.getLocation(new LocationResultListener() {
                        @Override
                        public void onResult(Location location) {
                            //Handle your location code
                        }
                    }, isSingle); // LocationParams default LocationParams.MEDIUM_ACCURACY
```

Specify parameters to get the location
```java
    //Start to get location
    boolean isSingle = true; //Single request location;
    LocationParams params = LocationParams.HIGH_ACCURACY //other default params:MEDIUM_ACCURACY、LOW_ACCURACY
    fastLocation.getLocation(new LocationResultListener() {
                        @Override
                        public void onResult(Location location) {
                            //Handle your location code
                        }
                    }, params, isSingle);
```

Get the last known location
```java
    //Start to get last know location
    FastLocation fastLocation = new FastLocation(this);
    fastLocation.getLastKnowLocation(new LocationResultListener() {
        @Override
        public void onResult(Location location) {
            //Handle your location code
        }
    });
```

## Location strategy
HIGH_ACCURACY  
MEDIUM_ACCURACY (default)  
LOW_ACCURACY  