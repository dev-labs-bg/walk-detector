package bg.devlabs.walkdetector;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
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

import bg.devlabs.walkdetector.util.NotificationHelper;
import bg.devlabs.walkdetector.util.SettingsManager;
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
    // tag used for logging purposes
    private static final String TAG = WalkDetectService.class.getSimpleName();

    // Used to access the Fitness.HistoryApi
    static private GoogleApiClient mClient = null;

    // Simple DateTimeFormat for logging and information showing purposes
    static private final java.text.DateFormat dateTimeInstance = DateFormat.getDateTimeInstance();

    // Disposable from the interval Observable, which is disposed when the user no longer wants
    // his status to be checked
    static private Disposable disposable;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     * name Used to name the worker thread, important only for debugging.
     */
    public WalkDetectService() {
        super();
    }

    /**
     * Called by the system every time a client explicitly starts the service by calling startService(Intent)
     * Checks the state and:
     * - if true starts the detection by building the client and connecting to it
     * - if false stops the detection by disposing the disposable and disconnecting from the client
     *
     * @return START_STICKY in order for the service not to die when the app dies
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "onStartCommand: flags = " + flags);

        if(intent.hasExtra("Alarm")){
            if(intent.getStringExtra("Alarm").equals("start")){
                Log.d(TAG, "onStartCommand: staring service");
                SharedPreferencesHelper.saveShouldDetectStatus(getApplicationContext(),true);
                buildFitnessClient();
                return START_STICKY;
            } else if(intent.getStringExtra("Alarm").equals("stop")){
                Log.d(TAG, "onStartCommand: stopping service");
                SharedPreferencesHelper.saveShouldDetectStatus(getApplicationContext(),false);
                stopCheckingForWalking();
                return START_STICKY;
            }
        }

        if (SharedPreferencesHelper.shouldDetectWalking(getApplicationContext())) {
            Log.d(TAG, "onStartCommand: staring service");
            buildFitnessClient();
        } else {
            Log.d(TAG, "onStartCommand: stopping service");
            stopCheckingForWalking();
        }
        //this service will run until we stop it
        return START_STICKY;
    }


    /**
     * Build a {@link GoogleApiClient} that will authenticate the user and allow the application
     * to connect to Fitness APIs. The scopes included should match the scopes your app needs
     * (see documentation for details).
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

    /**
     * @return Connection callbacks for when the client is connected or the connection is suspended
     */
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

    /**
     * Defines an interval based observable and subscribes to it.
     * <p>
     * The subscription returns a disposable which later can be disposed
     * when we no longer want to detect walking activity
     * <p>
     * Uses .startWith(0L) in order to trigger onNext immediately
     * <p>
     * The first result from interval is ignored as the computations don`t depend on the current index,
     * but on the current moment
     * <p>
     * On every "tick" of the interval the History API is queried
     * Then the result is computed in handleDataReadResult
     */
    private void startTimerObservable() {
        disposable = Observable.interval(SettingsManager.getInstance(this).observablePeriodSeconds, TimeUnit.SECONDS)
                .startWith(0L)
                .map(ignored -> getReadRequest())
                .map(this::callHistoryApi)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleDataReadResult,
                        (Throwable e) -> Log.d(TAG, "Throwable " + e.getLocalizedMessage())
                );
    }

    /**
     * Checks if the query result is containing any significant steps made in the last
     * {@link @SettingsManager#checkedPeriodSeconds}
     *
     * @param dataReadResult returned from the Fitness.HistoryApi
     */
    private void handleDataReadResult(DataReadResult dataReadResult) {
        //Used for aggregated data
        if (dataReadResult.getBuckets().size() > 0) {
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    checkDataForWalkActivity(dataSet);
                }
            }
        }
    }

    /**
     * Invoke the History API to fetch the data with the query and await the result of the read request.
     *
     * @param dataReadRequest user for the readData request
     * @return DataReadResult returned fom the Fitness.HistoryApi
     */
    private DataReadResult callHistoryApi(DataReadRequest dataReadRequest) {
        return Fitness.HistoryApi.readData(mClient, dataReadRequest)
                .await(SettingsManager.AWAIT_PERIOD_SECONDS, TimeUnit.SECONDS);

    }

    /**
     * Builds Read request depending on the current time
     * Queries step count delta between the current moment and checkedPeriodSeconds ago
     *
     * @return the DataReadRequest which can be used to Query the Fitness.HistoryApi
     */
    private DataReadRequest getReadRequest() {
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.SECOND, -SettingsManager.getInstance(this).checkedPeriodSeconds);
        long startTime = cal.getTimeInMillis();

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

    /**
     * Checks for walking activity in the dataSet and if found shows a notification
     *
     * @param dataSet to be examined for walking activity
     */
    private void checkDataForWalkActivity(DataSet dataSet) {
        for (DataPoint dp : dataSet.getDataPoints()) {
            if (!dp.getDataType().getName().equals("com.google.step_count.delta")) {
                break;
            }
            for (Field field : dp.getDataType().getFields()) {
                Log.d("History", "\tField: " + field.getName() + " Value: " + dp.getValue(field));
                int count = dp.getValue(field).asInt();
                if (field.getName().equals("steps") && count > SettingsManager.getInstance(this).neededStepsCountForWalking) {
                    String message = "Steps = " + count
                            + "\nFrom \t" + dateTimeInstance.format(dp.getStartTime(TimeUnit.MILLISECONDS))
                            + " to " + dateTimeInstance.format(dp.getEndTime(TimeUnit.MILLISECONDS));
                    NotificationHelper.sendNotification(message, getApplicationContext());
                    return;
                }
            }
        }
    }

    /**
     * Disposes the disposable, so that the interval is stopped
     * Disconnects the mClient
     */
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


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
