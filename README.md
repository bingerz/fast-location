# FastLocation

An Android Platform library to quickly request the location

## Adding to your project
You should add this to your dependencies:

```groovy
compile 'cn.bingerz.android:fastlocation:1.1.0'
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
    fastLocation.getLocation(new LocationResultListener() {
                        @Override
                        public void onResult(Location location) {
                            //Handle your location code
                        }
                    }); // LocationParams default LocationParams.MEDIUM_ACCURACY
```

Specify parameters to get the location
```java
    //Start to get location
    LocationParams params = LocationParams.HIGH_ACCURACY //other default params:MEDIUM_ACCURACY、LOW_ACCURACY
    fastLocation.getLocation(new LocationResultListener() {
                        @Override
                        public void onResult(Location location) {
                            //Handle your location code
                        }
                    }, params);
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