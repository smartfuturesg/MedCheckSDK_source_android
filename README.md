# MedCheck SDK for Android

A library that gives you power to access MedCheck Devices in your Android app.

## Getting Started
### Installation

Below are the two ways to integrate MedCheck Android SDK into your app.

- **Option 1:** Add medchecklib as New Module To Your Project

  If you want to modify lib files at the time of development, you have to integrate medchecklib as Android Library Module to your project. 
  
  For that you have to clone the medchecklib present in current repositor and you can import it as Android Library in your project by following below steps:
 
* Open your project in Android Studio.
* Copy and paste the medchecklib folder to your Project folder.
* On the root of your project directory create/modify the settings.gradle file. It should contain something like the following:
```groovy
include 'YourApp', 'medchecklib'
```
* gradle clean & build/close the project and reopen/re-import it.
* Edit your app's build.gradle to add this in the "depencies" section:
```groovy
dependencies {
//...
    compile project(':medchecklib')
}
```

**Option 2:** Compiling for yourself into AAR file and Adding only AAR file.

  If you want to manually compile the SDK and also want to go through Example, begin by cloning the repository locally or retrieving the source code. Open the project in Android Studio and run the application:

Output file can be found in `medchecklib/build/outputs/` with extension .aar
  
You can add that AAR file to your project by following below steps.
  * Click File > New > New Module.
  * Click Import .JAR/.AAR Package then click Next.
  * Enter the location of the compiled AAR or JAR file then click Finish.
  * On the root of your project directory create/modify the settings.gradle file. It should contain something like the         following:
  ```groovy
  include 'YourApp', 'medchecklib'
  ```
  * gradle clean & build/close the project and reopen/re-import it.
  * Edit your app's build.gradle to add this in the "depencies" section:
    ```groovy
    dependencies {
      //...
      compile project(':medchecklib')
    }
    ```

## Usage

Library contains two App Components:

* **MedCheckActivity** which can be extended by your activity as below:
  ```groovy
  public class MainActivity extends MedCheckActivity {
  
  @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        }
  //.....
  
  }
  ```
  * **MedCheckFragment** which can be extended by your Fragment as below:
  ```groovy
  public class FirstFragment extends MedCheckFragment {
  
  @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        }
  //.....
  
  }
  ```
  As per your requirement you can use above components.

After extending you can implement all the parent class methods, which are mentioned below:

 ```groovy
    protected void onPermissionGrantedAndBluetoothOn() {
    }

    protected void startScan() {
    }

    protected void onDeviceClearCommand(int state) {
    }

    private void onDeviceTimeSyncCommand(int state) {
    }

    protected void onDeviceScanResult(ScanResult scanResult) {
    }

    protected void onDeviceDataReadingStateChange(int state, String message) {
    }

    protected void onDeviceDataReceive(BluetoothDevice device, ArrayList<IDeviceData> deviceData, String jsonString, String     deviceType) {
    }

    protected void onDeviceConnectionStateChange(BleDevice bleDevice, int status) {
    }
 ```
## Important Methods

App component classes have three main methods which are essential for registering callbacks and checking favorable conditions before starting scan or any connection request, mentioned below:

**requestLocationPermission()** This method is used to request location permission and handle bluetooth and GPS state and can be used as below:

```groovy

@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        requestLocationPermission();
    }
```

**registerCallback();** This method is used to register callbacks in extended class coming from **MedCheck** Singleton Class and can be used as below:

```groovy
    @Override
    protected void onResume() {
        super.onResume();
        registerCallback();
    }
```

**checkAllConditions();** This method is used to check all the required conditions like GPS, Bluetooth and Location Permission before doing any scan or connection process which provides callback in **startScan()** if all conditions are satisfied and can be used as below:

```groovy
@Override
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.btnStartScan:
                checkAllConditions();
                break;
            default:
                break;
        }
    }
```

## Powerful MedCheck.java Class

To perform the operations with Medcheck Devices we have a Singletone Class Medcheck.java which provides below methods:

```groovy
public void startScan(Context context) {
        MedCheckBluetoothLeService.startBluetoothLeScan(context.getApplicationContext());
    }

    public void stopScan(Context context) {
        MedCheckBluetoothLeService.stopBluetoothLeScan(context.getApplicationContext());
    }

    public void disconnectDevice(Context context) {
        MedCheckBluetoothLeService.disconnectBluetoothLeDevice(context.getApplicationContext());
    }

    public void connect(Context context, String mac) {
        MedCheckBluetoothLeService.connectDeviceUsingMacAddress(context.getApplicationContext(), mac);
    }

    public void removeOnConnectionStateChangeListener() {
        mCallback = null;
    }

    public void clearDevice(Context context, String mac) {
        MedCheckBluetoothLeService.clearDeviceUsingMacAddress(context.getApplicationContext(), mac);
    }

    public void timeSyncDevice(Context context, String mac) {
        MedCheckBluetoothLeService.timeSyncUsingMacAddress(context.getApplicationContext(), mac);
    }

    public void writeCommand(Context context, String mac) {
        MedCheckBluetoothLeService.writeCharacteristicsUsingMac(context.getApplicationContext(), mac);
    }
```
All the above methods can be used as below mentioned:

```groovy
  MedCheck.getInstance().methodName();
```
For example :

```groovy
  MedCheck.getInstance().startScan(mContext);
```


## Author

smartfuturesg, sumit@oursmartfuture.com

## License

MedCheckSDK is available under the MIT license. See the LICENSE file for more info.
