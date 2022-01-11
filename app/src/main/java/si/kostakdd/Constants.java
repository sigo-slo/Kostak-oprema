package si.kostakdd;

public class Constants {
   
    // json {"url":"http://192.168.205.33:8080/Hello/app_v3.0.1_Other_20150116.apk","versionCode":2,"updateMessage":"版本更新信息"}

    public static final String APK_DOWNLOAD_URL = "url";
    public static final String APK_UPDATE_CONTENT = "updateMessage";
    public static final String APK_VERSION_NAME = "versionName";
    public static final String MAPVIEW_BUNDLE_KEY = "AIzaSyC1VJgMEBl8_HHXrZmIAQx52d7fRDyuXCc";

    public static final int TYPE_NOTIFICATION = 2;

    public static final int TYPE_DIALOG = 1;

    public static final String TAG = "UpdateChecker";
    public static final String HOST_URL = "http://kostak-test.000webhostapp.com";
    //public static final String HOST_URL = "http://192.168.64.105/kostak";
    //public static final String HOST_URL = "http://91.246.245.153";
    public static final String UPDATE_URL = HOST_URL + "/update/update.json";
    public static final String LOGIN_URL = HOST_URL + "/action.php";
    public static final String SERVER_IMG_FOLDER = HOST_URL+"/img/";
    public static final String SERVER_URL = HOST_URL;
    public static final String IMAGE_FORMAT = ".webp";

    public static final int BITMAP_HIGH_COMPRESSION = 10;
    public static final int BITMAP_MEDIUM_COMPRESSION = 50;
    public static final int BITMAP_NO_COMPRESSION = 100;

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public static final int MY_PERMISSIONS_REQUEST_NFC = 88;
    public static final int REQUEST_CODE_TAKEPICTURE = 11;
    public static final int REQUEST_CODE_SCAN_QR = 22;


    public static final int FRAGMENT_IMAGE = 0;
    public static final int FRAGMENT_MAP = 1;
    public static final int FRAGMENT_ADMIN = 2;
}
