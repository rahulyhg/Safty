package com.rvsoft.safty.helper;

public class Constant {
    public static final String SMS_SENDER = "KNAPPS";

    public static final String API = "http://rvsoft.esy.es/Android/helpo/Authentication.php";

    /**
     * GET METHOD<br>
     * {0} LAT<br>
     * {1} LONG
     */
    public static String POLICE = "https://maps.googleapis.com/maps/api/place/search/json?location={0},{1}&rankby=distance&types=police&sensor=false&key=AIzaSyCOi8IoIkmCvCU73d0KMmZdrlzPszk_qqE";

    public interface REQUESTS{
        // PERMISSION RELATED REQUEST STARTS FROM 100 SERIES
        int LOCATION = 100;
        int CAMERA = 101;
        int STORAGE = 102;
        int ENABLE_GPS = 103;
        int ALL = 104;
        int SMS = 105;

        // ACTIVITY RESULT REQUEST STARTS FROM 200 SERIES
        int ADD_REQUEST = 200;
        int EDIT_REQUEST = 201;
        int OFFLINE_REQUEST = 205;
        int FILE_PICKER = 202;
        int IMAGE_PICKER = 203;
        int IMAGE_SHOW = 204;
        int ADD_OFFLINE_REQUEST = 205;

        // FOREGROUND REQUEST
        int FOREGROUND_SERVICE = 301;
    }


    public interface FIREBASE_CHILD{
        String USER = "user";
        String LOCATION = "location";
        String HELPO_REQUEST = "request";
        String HELP_DETAIL = "helpo";
    }
}
