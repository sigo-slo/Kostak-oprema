package si.kostakdd;

public class Constants {
   
    // json {"url":"http://192.168.205.33:8080/Hello/app_v3.0.1_Other_20150116.apk","versionCode":2,"updateMessage":"版本更新信息"}

    public static final String APK_DOWNLOAD_URL = "url";
    public static final String APK_UPDATE_CONTENT = "updateMessage";
    public static final String APK_VERSION_NAME = "versionName";


    public static final int TYPE_NOTIFICATION = 2;

    public static final int TYPE_DIALOG = 1;

    public static final String TAG = "UpdateChecker";
    public static final String HOST_URL = "https://kostak-test.000webhostapp.com";//"http://192.168.1.19/kostak";//
    public static final String UPDATE_URL = HOST_URL + "/update/update.json";
    public static final String LOGIN_URL = HOST_URL + "/action.php";
    public static final String SERVER_IMG_FOLDER = HOST_URL+"/img/";
    public static final String SERVER_URL = HOST_URL;
    public static final String IMAGE_FORMAT = ".webp";

    public static final int BITMAP_HIGH_COMPRESSION = 10;
    public static final int BITMAP_MEDIUM_COMPRESSION = 50;
    public static final int BITMAP_NO_COMPRESSION = 100;

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public static final int REQUEST_CODE_TAKEPICTURE = 11;
    public static final int REQUEST_CODE_SCAN_QR = 22;
    
    
    
}
