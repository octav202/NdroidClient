package com.ndroid.ndroidclient.server;

import android.util.Log;

import com.ndroid.ndroidclient.Constants;
import com.ndroid.ndroidclient.models.Device;
import com.ndroid.ndroidclient.models.DeviceLocation;
import com.ndroid.ndroidclient.models.DeviceStatus;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static com.ndroid.ndroidclient.Constants.ADD_DEVICE;
import static com.ndroid.ndroidclient.Constants.GET_DEVICE_ID;
import static com.ndroid.ndroidclient.Constants.GET_DEVICE_STATUS;
import static com.ndroid.ndroidclient.Constants.SEND_DEVICE_STATUS;
import static com.ndroid.ndroidclient.Constants.SEND_LOCATION;
import static com.ndroid.ndroidclient.Constants.SERVER_URL;

public class ServerApi {

    private static final String TAG = Constants.TAG + ServerApi.class.getSimpleName();
    private static int currentId = 0;
    private static Device sDevice;
    private static DeviceStatus sDeviceStatus;

    public static int getCurrentDeviceId() {
        return currentId;
    }
    public static void setCurrentDeviceId(int id) {
        currentId = id;
    }

    public static DeviceStatus getCurrentDeviceStatus() {
        return sDeviceStatus;
    }
    public static void setCurrentDeviceStatus(DeviceStatus status) {
        sDeviceStatus = status;
    }

    /**
     * Converts the contents of an InputStream to a String.
     */
    public static String readStream(InputStream stream, int maxReadSize) throws IOException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] rawBuffer = new char[maxReadSize];
        int readSize;
        StringBuffer buffer = new StringBuffer();
        while (((readSize = reader.read(rawBuffer)) != -1) && maxReadSize > 0) {
            if (readSize > maxReadSize) {
                readSize = maxReadSize;
            }
            buffer.append(rawBuffer, 0, readSize);
            maxReadSize -= readSize;
        }
        return buffer.toString();
    }

    //////////////////////////////////////////////////////
    // _____________________ DEVICE ___________________ //
    //////////////////////////////////////////////////////

    /**
     * Create the URL for the GET Request getDeviceIdUrl.
     *
     * @param name
     * @param pass
     * @return the URL.
     */
    public static URL getDeviceIdUrl(String name, String pass) {
        String template = SERVER_URL + GET_DEVICE_ID + "?name=%s&pass=%s";
        String stringUrl = String.format(template, name, pass);
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * Authenticate user by name and pass
     *
     * @param name
     * @param pass
     * @return the id of the user.
     */
    public static int getDeviceId(String name, String pass) {
        URL url = getDeviceIdUrl(name, pass);
        Log.d(TAG, "____ [GET DEVICE ID] ____ :" + url);

        InputStream stream = null;
        HttpURLConnection connection = null;
        int result = 0;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(3000);
            connection.setConnectTimeout(3000);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();
            int response = connection.getResponseCode();
            if (response != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "Request Failed :" + response);
                return 0;
            }
            stream = connection.getInputStream();
            if (stream != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF8"))) {
                    result = Integer.parseInt(reader.readLine());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (connection != null) {
                connection.disconnect();
            }
        }

        currentId = result;
        Log.d(TAG, "Fetched Device Id : " + result);

        return result;
    }

    /**
     * Create the URL for the GET Request getDeviceIdUrl.
     *
     * @param name
     * @param pass
     * @return the URL.
     */
    public static URL addDeviceUrl(String name, String pass) {
        String template = SERVER_URL + ADD_DEVICE + "?name=%s&pass=%s";
        String stringUrl = String.format(template, name, pass);
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }


    /**
     * Authenticate user by name and pass
     *
     * @param name
     * @param pass
     * @return the id of the user.
     */
    public static int addDevice(String name, String pass) {
        URL url = addDeviceUrl(name, pass);
        Log.d(TAG, "____ [ADD DEVICE ID] ____ :" + url);

        InputStream stream = null;
        HttpURLConnection connection = null;
        int result = 0;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(3000);
            connection.setConnectTimeout(3000);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();
            int response = connection.getResponseCode();
            if (response != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "Request Failed :" + response);
                return 0;
            }
            stream = connection.getInputStream();
            if (stream != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF8"))) {
                    result = Integer.parseInt(reader.readLine());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (connection != null) {
                connection.disconnect();
            }
        }

        currentId = result;
        Log.d(TAG, "Added Device Id : " + result);

        return result;
    }


    ////////////////////////////////////////////////////////
    // _____________________ LOCATION ___________________ //
    ////////////////////////////////////////////////////////

    /**
     * Send device's current deviceLocation.
     *
     * @return success/fail.
     */
    public static boolean sendLocation(DeviceLocation deviceLocation) {
        URL url = null;
        try {
            url = new URL(SERVER_URL + SEND_LOCATION);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "____ [SEND LOCATION] ____" + url);

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            Log.e(TAG, "Open Connection error " + e);
            return false;
        }
        connection.setDoOutput (true);
        connection.setUseCaches (false);
        connection.setChunkedStreamingMode(0);
        connection.setRequestProperty("Content-Type","application/json");

        // Create Json Object
        JSONObject json = new JSONObject();
        try {
            json.put("deviceId", deviceLocation.getDeviceId());
            json.put("lat", deviceLocation.getLat());
            json.put("lon", deviceLocation.getLon());
            json.put("timeStamp", deviceLocation.getTimeStamp());
        } catch (JSONException e) {
            Log.e(TAG, "Error creating Json object" + e);
        }

        // Send request body
        OutputStream os = null;
        try {
            os = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(json.toString());
            writer.flush();
            writer.close();
            os.close();
        } catch (IOException e) {
            Log.e(TAG, "Error writing request body" + e);
            return false;
        }

        // Connect
        try {
            connection.connect();
            int response = connection.getResponseCode();
            if (response != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "Connection Failed :" + response);
                return false;
            }
        } catch (IOException e) {
            Log.e(TAG, "Connect error " + e);
            return false;
        }

        Log.d(TAG, "DeviceLocation sent : " + deviceLocation);
        return true;
    }

    /////////////////////////////////////////////////////////////
    // _____________________ DEVICE STATUS ___________________ //
    ////////////////////////////////////////////////////////////

    /**
     * Create the URL for getDeviceStatus request
     *
     * @param id
     * @return the URL.
     */
    public static URL getDeviceStatusUrl(int id) {
        String template = SERVER_URL + GET_DEVICE_STATUS + "?id=%s";
        String stringUrl = String.format(template, id);
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * Get location for a device
     *
     * @param deviceId
     * @return list of locations of the current device.
     */
    public static DeviceStatus getDeviceStatus(int deviceId) {
        URL url = getDeviceStatusUrl(deviceId);
        Log.d(TAG, "____ [GET DEVICE STATUS] ____" + url);

        InputStream stream = null;
        HttpURLConnection connection = null;
        String result = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(3000);
            connection.setConnectTimeout(3000);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();
            int response = connection.getResponseCode();
            if (response != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "Request Failed :" + response);
                return null;
            }
            stream = connection.getInputStream();
            if (stream != null) {
                result = readStream(stream, 500);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (connection != null) {
                connection.disconnect();
            }
        }

        DeviceStatus deviceStatus = null;
        if (result != null && !result.isEmpty()) {
            deviceStatus = new DeviceStatus();
            try {
                JSONObject jsonObj = new JSONObject(result);
                deviceStatus.setDeviceId(jsonObj.getInt("deviceId"));
                deviceStatus.setLock(jsonObj.getInt("lock"));
                deviceStatus.setWipeData(jsonObj.getInt("wipeData"));
                deviceStatus.setEncryptStorage(jsonObj.getInt("encryptStorage"));
                deviceStatus.setReboot(jsonObj.getInt("reboot"));
                deviceStatus.setTriggered(jsonObj.getInt("triggered"));
                deviceStatus.setLocationFrequency(jsonObj.getInt("locationFrequency"));
                deviceStatus.setRing(jsonObj.getInt("ring"));
                Log.d(TAG,deviceStatus.toString());
            } catch (JSONException e) {
                Log.e(TAG, "Error Parsing Json");
            }
        } else {
            Log.e(TAG, "Invalid Result");
        }
        return deviceStatus;
    }

    /**
     * Send device's current location.
     *
     * @return success/fail.
     */
    public static boolean sendDeviceStatus(DeviceStatus deviceStatus) {
        URL url = null;
        try {
            url = new URL(SERVER_URL + SEND_DEVICE_STATUS);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "____ [SEND DEVICE STATUS] ____" + url);

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            Log.e(TAG, "Open Connection error " + e);
            return false;
        }
        connection.setDoOutput (true);
        connection.setUseCaches (false);
        connection.setChunkedStreamingMode(0);
        connection.setRequestProperty("Content-Type","application/json");

        // Create Json Object
        JSONObject json = new JSONObject();
        try {
            json.put("deviceId", deviceStatus.getDeviceId());
            json.put("lock", deviceStatus.getLock());
            json.put("wipeData", deviceStatus.getWipeData());
            json.put("encryptStorage", deviceStatus.getEncryptStorage());
            json.put("reboot", deviceStatus.getReboot());
            json.put("triggered", deviceStatus.getTriggered());
            json.put("locationFrequency", deviceStatus.getLocationFrequency());
            json.put("ring", deviceStatus.getRing());
        } catch (JSONException e) {
            Log.e(TAG, "Error creating Json object" + e);
        }

        // Send request body
        OutputStream os = null;
        try {
            os = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(json.toString());
            writer.flush();
            writer.close();
            os.close();
        } catch (IOException e) {
            Log.e(TAG, "Error writing request body" + e);
            return false;
        }

        // Connect
        try {
            connection.connect();
            int response = connection.getResponseCode();
            if (response != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "Connection Failed :" + response);
                return false;
            }
        } catch (IOException e) {
            Log.e(TAG, "Connect error " + e);
            return false;
        }

        Log.d(TAG, "DeviceStatus sent : " + deviceStatus);
        return true;
    }
}
