package com.allen.disable_screenshots

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.WindowManager
import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** DisableScreenshotsPlugin */
class DisableScreenshotsPlugin: FlutterPlugin, MethodCallHandler, EventChannel.StreamHandler, ActivityAware {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel
  private lateinit var applicationContext: Context;
  private lateinit var activity: Activity;
  private var eventSink: EventChannel.EventSink? = null
  private lateinit var screenShotListenManager: ScreenShotListenManager;
  var disableScreenshots: Boolean = false

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "disable_screenshots")
    channel.setMethodCallHandler(this)
    val eventCannel = EventChannel(flutterPluginBinding.binaryMessenger,"disable_screenshots/observer")
    eventCannel.setStreamHandler(this)
  }

  private fun setDisableScreenshotsStatus(disable: Boolean) {
    if (disable) { // 禁用截屏
      activity.window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
      println("禁用截屏")
    } else { // 允许截屏
      activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
      println("允许截屏")
    }
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    if (call.method == "disableScreenshots") {
      var disable = call.argument<Boolean>("disable") == true
      setDisableScreenshotsStatus(disable)
      result.success("")
    } else {
      result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  override fun onListen(arguments: Any?, events: EventChannel.EventSink?){
    println("开始监听")
    eventSink = events
    // ScreenShotListenManager为一个实现了监听截屏功能的Manager
    screenShotListenManager = ScreenShotListenManager.newInstance(applicationContext)
    screenShotListenManager.setListener { imagePath ->
      println("监听到截屏，截屏图片地址是：$imagePath")
      // 发送事件给Flutter端，告知监听到了截屏行为。
      eventSink?.success("监听到截屏行为")
    }
    screenShotListenManager.startListen()
}
  override fun onCancel(arguments: Any?){
    screenShotListenManager.stopListen()
    eventSink = null
}
  override fun onAttachedToActivity(binding: ActivityPluginBinding){
    activity = binding.activity;
    setDisableScreenshotsStatus(this.disableScreenshots)
}
  override fun onDetachedFromActivityForConfigChanges(){
    this.onDetachedFromActivity();
}
  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding){
    this.onAttachedToActivity(binding);
    activity = binding.activity
    setDisableScreenshotsStatus(this.disableScreenshots)
}
  override fun onDetachedFromActivity(){

}
}
