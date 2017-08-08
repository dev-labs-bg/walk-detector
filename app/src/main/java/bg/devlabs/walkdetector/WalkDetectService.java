package bg.devlabs.walkdetector;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import bg.devlabs.walkdetector.util.SharedPreferencesHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Simona Stoyanova on 8/8/17.
 * simona@devlabs.bg
 * <p>
 * This sample demonstrates how to use the Sensors API of the Google Fit platform to find
 * available data sources and to register/unregister listeners to those sources
 * <p>
 * This service is always running and checking if there has been enough steps for walking activity
 * to be detected. If so, a notification is shown.
 * <p>
 * The service reads the tracking state from shared preferences.
 * The state can e changed from the UI (Main Activity -> three dot Menu)
 */

public class WalkDetectService extends Service {
    public static final String TAG = "WalkDetectService";
    //how long will the checked period be for walking activity
    private static final int CHECKED_PERIOD_SECOND = 180000; //180 seconds = 3 minutes
    //how much will the app wait for response until a timeout exception is thrown
    private static final int AWAIT_PERIOD_SECOND = 60; // 60 seconds = 1 min
    //how ofter will we query the client for walking activity
    private static final int OBSERVABLE_PERIOD_SECOND = 10;//CHECKED_PERIOD_SECOND + AWAIT_PERIOD_SECOND;
    //Walking slow (2 mph)	67 steps per minute which is almost one step per second
    private static final int SLOW_WALKING_STEPS_PER_SECOND = 1;
    private static final int COUNT_STEPS_WALKING = 1;//CHECKED_PERIOD_SECOND * SLOW_WALKING_STEPS_PER_SECOND;
    static private GoogleApiClient mClient = null;
    static private final java.text.DateFormat dateTimeInstance = DateFormat.getDateTimeInstance();
    static private Disposable disposable;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     * name Used to name the worker thread, important only for debugging.
     */
    public WalkDetectService() {
        super();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (SharedPreferencesHelper.shouldDetectWalking(getApplicationContext())) {
            buildFitnessClient();
        } else {
            stopCheckingForWalking();
        }
        //this service will run until we stop it
        return START_STICKY;
    }


    /**
     * Build a {@link GoogleApiClient} that will authenticate the user and allow the application
     * to connect to Fitness APIs. The scopes included should match the scopes your app needs
     * (see documentation for details). Authentication will occasionally fail intentionally,
     * and in those cases, there will be a known resolution, which the OnConnectionFailedListener()
     * can address. Examples of this include the user never having signed in before, or having
     * multiple accounts on the device and needing to specify which account to use, etc.
     */
    private void buildFitnessClient() {
        if (mClient != null) {
            return;
        }
        ConnectionCallbacks connectionCallbacks = getConnectionCallbacks();
        mClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.HISTORY_API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addConnectionCallbacks(connectionCallbacks)
                .build();
        mClient.connect();
    }

    private ConnectionCallbacks getConnectionCallbacks() {
        return new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle) {
                startTimerObservable();
            }

            @Override
            public void onConnectionSuspended(int i) {
                // If your connection to the sensor gets lost at some point,
                // you'll be able to determine the reason and react to it here.
                if (i == ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                    Log.d(TAG, "Connection lost.  Cause: Network Lost.");
                } else if (i == ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                    Log.d(TAG,
                            "Connection lost.  Reason: Service Disconnected");
                }
            }
        };
    }

    private void startTimerObservable() {
        disposable = Observable.interval(OBSERVABLE_PERIOD_SECOND, TimeUnit.SECONDS)
                .startWith(0L)
                .map(ignored -> getReadRequest())
                .map(this::callHistoryApi)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleDataReadResult,
                        (Throwable e) -> {
                            Log.d(TAG, "Throwable " + e.getLocalizedMessage());
                        }
                );
    }

    private void handleDataReadResult(DataReadResult dataReadResult) {
        Log.d(TAG, "handleDataReadResult: ");
        //Used for aggregated data
        if (dataReadResult.getBuckets().size() > 0) {
            Log.d("History", "Number of buckets: " +
                    dataReadResult.getBuckets().size());
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                Log.d(TAG, "dataSets.size() = " + dataSets.size());

                for (DataSet dataSet : dataSets) {
                    showDataSet(dataSet);
                }
            }
        } else {
            Log.d("History", "Number of buckets:0 ");
        }
    }

    private DataReadResult callHistoryApi(DataReadRequest dataReadRequest) {
        Log.d(TAG, "calling HistoryApi");
        // Invoke the History API to fetch the data with the query and await the result of
        // the read request.
        return Fitness.HistoryApi.readData(mClient, dataReadRequest)
                .await(AWAIT_PERIOD_SECOND, TimeUnit.SECONDS);

    }

    private DataReadRequest getReadRequest() {
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.SECOND, -CHECKED_PERIOD_SECOND);
        long startTime = cal.getTimeInMillis();

        Log.d("History", "Range Start: " + dateTimeInstance.format(startTime));
        Log.d("History", "Range End: " + dateTimeInstance.format(endTime));

        //Check how many steps were walked and recorded in the last 7 days
        return new DataReadRequest.Builder()
                // The data request can specify multiple data types to return, effectively
                // combining multiple data queries into one call.
                // In this example, it's very unlikely that the request is for several hundred
                // data points each consisting of a few steps and a timestamp.  The more likely
                // scenario is wanting to see how many steps were walked per day, for 7 days.
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(3, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
    }

    private void showDataSet(DataSet dataSet) {
        for (DataPoint dp : dataSet.getDataPoints()) {
            if (!dp.getDataType().getName().equals("com.google.step_count.delta")) {
                break;
            }
            for (Field field : dp.getDataType().getFields()) {
                Log.d("History", "\tField: " + field.getName() + " Value: " + dp.getValue(field));
                int count = dp.getValue(field).asInt();
                if (field.getName().equals("steps") && count > COUNT_STEPS_WALKING) {
                    sendNotification("Steps = " + count
                            + "\nFrom \t" + dateTimeInstance.format(dp.getStartTime(TimeUnit.MILLISECONDS))
                            + " to " + dateTimeInstance.format(dp.getEndTime(TimeUnit.MILLISECONDS))
                    );
                    return;
                }
            }
        }
    }


    private void stopCheckingForWalking() {
        Log.d(TAG, "stopCheckingForWalking: disposable.dispose();");
        if (disposable != null) {
            disposable.dispose();
        }
        if (mClient != null && mClient.isConnected()) {
            mClient.disconnect();
            mClient = null;
        }
    }

    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the MainActivity.
     */
    private void sendNotification(String message) {
        Log.d(TAG, "sendNotification " + message);
        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = getPackageManager().getLaunchIntentForPackage("com.charitymilescm.android");
        if (notificationIntent == null) {
            notificationIntent = new Intent(this, MainActivity.class);
        }

        notificationIntent.putExtra("notificationDetails", message);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // Define the notification settings.
        builder.setSmallIcon(R.drawable.ic_directions_walk_black_24dp)
                .setColor(Color.RED)
                .setContentTitle(getString(R.string.walking_detected))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentText(message)
                .setContentIntent(notificationPendingIntent);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
