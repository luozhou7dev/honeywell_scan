package icwind.honeywell_scan;

import com.honeywell.aidc.AidcManager;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;
import com.honeywell.aidc.ScannerNotClaimedException;
import com.honeywell.aidc.ScannerUnavailableException;
import com.honeywell.aidc.TriggerStateChangeEvent;

import io.flutter.app.FlutterActivity;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/**
 * Honeywell扫描插件
 */
public class HoneywellScanPlugin extends FlutterActivity implements MethodCallHandler, EventChannel.StreamHandler,
        BarcodeReader.BarcodeListener, BarcodeReader.TriggerListener {

    AidcManager aidcManager;
    BarcodeReader barcodeReader;

    private EventChannel.EventSink mEventSink;

    private HoneywellScanPlugin() {
        AidcManager.create(this, new ManagerCreatedCallback());
    }

    public static void registerWith(Registrar registrar) {
        final MethodChannel methodChannel = new MethodChannel(registrar.messenger(), "honeywell_scan/method_channel");
        final EventChannel eventChannel = new EventChannel(registrar.messenger(), "honeywell_scan/event_channel");

        final HoneywellScanPlugin honeywellScanInstance = new HoneywellScanPlugin();
        methodChannel.setMethodCallHandler(honeywellScanInstance);
        eventChannel.setStreamHandler(honeywellScanInstance);
    }

    @Override
    public void onMethodCall(MethodCall methodCall, Result result) {
        switch (methodCall.method) {
            case "getPlatformVersion":
                result.success("Android " + android.os.Build.VERSION.RELEASE);
                break;
            case "startScanning":
                doScan(true);
                break;
            case "stopScanning":
                doScan(false);
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    @Override
    public void onListen(Object o, EventChannel.EventSink eventSink) {
        this.mEventSink = eventSink;
    }

    @Override
    public void onCancel(Object o) {

    }

    /**
     * AidcManager创建时的回调方法
     */
    class ManagerCreatedCallback implements AidcManager.CreatedCallback {
        ManagerCreatedCallback() {
        }

        @Override
        public void onCreated(AidcManager aidcManager) {
            HoneywellScanPlugin.this.aidcManager = aidcManager;
            barcodeReader = HoneywellScanPlugin.this.aidcManager.createBarcodeReader();

            try {
                barcodeReader.setProperty(BarcodeReader.PROPERTY_CODE_128_ENABLED, true);
                barcodeReader.setProperty(BarcodeReader.PROPERTY_QR_CODE_ENABLED, false);
                barcodeReader.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE,
                        BarcodeReader.TRIGGER_CONTROL_MODE_CLIENT_CONTROL);

                // 打开扫描功能
                barcodeReader.claim();

                // 注册Trigger监听器和Barcode监听器
                barcodeReader.addTriggerListener(HoneywellScanPlugin.this);
                barcodeReader.addBarcodeListener(HoneywellScanPlugin.this);
            } catch (Exception e) {
                e.printStackTrace();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    /**
     * 获取扫描数据
     *
     * @param barcodeReadEvent BarcodeReadEvent
     */
    @Override
    public void onBarcodeEvent(BarcodeReadEvent barcodeReadEvent) {
        // 获取扫描数据
        String barcodeData = barcodeReadEvent.getBarcodeData();
        mEventSink.success(barcodeData);
    }

    /**
     * 扫描出错回调事件
     *
     * @param barcodeFailureEvent BarcodeReadEvent
     */
    @Override
    public void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent) {

    }

    /**
     * 扫描按钮触发监听器
     *
     * @param triggerStateChangeEvent TriggerStateChangeEvent
     */
    @Override
    public void onTriggerEvent(TriggerStateChangeEvent triggerStateChangeEvent) {
        try {
            // 开关补光
            barcodeReader.light(triggerStateChangeEvent.getState());
            // 开关瞄准线
            barcodeReader.aim(triggerStateChangeEvent.getState());
            // 开关解码功能
            doScan(triggerStateChangeEvent.getState());
        } catch (Exception e) {
            System.out.println("开关扫描功能失败");
        }
    }

    /**
     * 开启|关闭扫描
     *
     * @param do_scan boolean
     */
    void doScan(boolean do_scan) {
        try {
            barcodeReader.decode(do_scan);
        } catch (ScannerNotClaimedException e) {
            e.printStackTrace();
        } catch (ScannerUnavailableException e2) {
            e2.printStackTrace();
        } catch (Exception e3) {
            e3.printStackTrace();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        barcodeReader.removeTriggerListener(this);
        barcodeReader.removeBarcodeListener(this);
        barcodeReader.close();
        aidcManager.close();
    }
}
