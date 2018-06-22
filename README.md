# FastLocation

An Android Platform library to quickly request the location

## Code Example

```java
    
    //Use Context to initialize the object
    FastLocation fastLocation = new FastLocation(this);

    //Start to get location
    LocationParams params = LocationParams.HIGH_ACCURACY //other default params:MEDIUM_ACCURACY、LOW_ACCURACY
    fastLocation.getLocation(new LocationResultListener() {
                        @Override
                        public void onResult(Location location) {
                            //Handle your location code
                        }
                    });

    //Start to get location
    fastLocation.getLocation(new LocationResultListener() {
                        @Override
                        public void onResult(Location location) {
                            //Handle your location code
                        }
                    }); // LocationParams default LocationParams.MEDIUM_ACCURACY

    //Start to get last know location
    FastLocation fastLocation = new FastLocation(this);
        fastLocation.getLastKnowLocation(new LocationResultListener() {
            @Override
            public void onResult(Location location) {
                //Handle your location code
            }
        });
```