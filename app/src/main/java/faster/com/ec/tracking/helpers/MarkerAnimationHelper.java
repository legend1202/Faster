package faster.com.ec.tracking.helpers;


import android.os.Handler;
import android.os.SystemClock;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import faster.com.ec.tracking.interfaces.LatLngInterpolator;

public class MarkerAnimationHelper {
    public static Boolean isMarkerRotating = false;

    public static void animateMarkerToGB(final Marker marker, final LatLng finalPosition, final LatLngInterpolator latLngInterpolator) {
        final LatLng startPosition = marker.getPosition();
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final float durationInMs = 2000;

        handler.post(new Runnable() {
            long elapsed;
            float t;
            float v;

            @Override
            public void run() {
                // Calculate progress using interpolator
                elapsed = SystemClock.uptimeMillis() - start;
                t = elapsed / durationInMs;
                v = interpolator.getInterpolation(t);

                marker.setPosition(latLngInterpolator.interpolate(v, startPosition, finalPosition));
               // marker.setRotation(90);
                // Repeat till progress is complete.
                if (t < 1) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });

    }
    public static void rotateMarker(final Marker marker, final float toRotation) {
        if (!isMarkerRotating) {
            final Handler handler = new Handler();
            //final long start = SystemClock.uptimeMillis();
            //final float startRotation = marker.getRotation();
            //final long duration = 1000;

            final Interpolator interpolator = new LinearInterpolator();


            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        marker.setRotation(toRotation);
                    } catch (Exception e) {
                        //
                    }

                   // isMarkerRotating = true;
                   // long elapsed = SystemClock.uptimeMillis() - start;
                    //float t = interpolator.getInterpolation((float) elapsed / duration);

                  //  float rot = t * toRotation + (1 - t) * startRotation;
                  //  marker.setRotation(-rot > 180 ? rot / 2 : rot);
                  //  marker.setRotation(toRotation);
                   // if (t < 1.0) {
                        // Post again 16ms later.
                      //  handler.postDelayed(this, 16);
                   // } else {
                     //   isMarkerRotating = false;
                   // }
                }
            });
        }
    }
}
