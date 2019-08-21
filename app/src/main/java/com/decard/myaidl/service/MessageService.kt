package com.decard.myaidl.service

import android.app.ApplicationErrorReport
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.IBinder
import android.os.Parcel
import android.os.RemoteCallbackList
import android.util.Log
import com.decard.myaidl.MessageReceiver
import com.decard.myaidl.MessageSender
import com.decard.myaidl.data.MessageModel
import java.util.concurrent.atomic.AtomicBoolean

class MessageService : Service() {


    private val serviceStop = AtomicBoolean(false)
    //RemoteCallbackList 专门用来管理多进程回调接口
    private val listenerList = RemoteCallbackList<MessageReceiver>()


    override fun onCreate() {
        super.onCreate()
        Log.d("------service", "onCreate")
        Thread(FakeTcpTask()).start()
    }


    private val messageSender = object : MessageSender.Stub() {


        override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            //包验证方式
            var packageName: String? = null
            val packages = packageManager.getPackagesForUid(Binder.getCallingUid())

            if (packages != null && packages.isNotEmpty()) {
                packages.forEach {
                    Log.d("--------packageName", it)
                }
                packageName = packages[0]
            }
            if (packageName == null || !packageName.startsWith("com.decard.myaidl")) {
                Log.d("--------onTransact", "拒绝调用")
                return false
            }
            return super.onTransact(code, data, reply, flags)
        }

        override fun registerReceiveListener(messageReceiver: MessageReceiver?) {
            listenerList.register(messageReceiver)
        }

        override fun unRegisterReceiveListener(messageReceiver: MessageReceiver?) {
            listenerList.unregister(messageReceiver)
        }

        override fun sendMessage(model: MessageModel?) {
            Log.d("-----------server", model.toString())
        }
    }


    override fun onBind(intent: Intent?): IBinder? {
        //自定义permission方式检查 权限
        if (checkCallingOrSelfPermission("com.decard.myaidl.permission.REMOTE_SERVICE_PERMISSION") == PackageManager.PERMISSION_DENIED) {
            return null
        }
        return messageSender
    }

    //模拟长连接，通知客户端有消息到达
    private inner class FakeTcpTask : Runnable {

        override fun run() {
            val startTime = System.currentTimeMillis()
            while (!serviceStop.get()) {
                if (System.currentTimeMillis() - startTime > 15000) {
                    ApplicationErrorReport.CrashInfo()
                }
                Thread.sleep(5000)
                val messageModel = MessageModel("server", "client", "宝塔镇河妖")

                /**
                 * RemoteCallbackList的遍历方式
                 * beginBroadcast和finishBroadCast一定要配对使用
                 */
                val listenerCount = listenerList.beginBroadcast()
                Log.d("-----------server", "listenerCount: $listenerCount")
                for (item in 0 until listenerCount) {
                    val messageReceiver = listenerList.getBroadcastItem(item)
                    if (messageReceiver != null) {
                        messageReceiver.onMessageReceived(messageModel)
                    }
                }
                listenerList.finishBroadcast()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceStop.set(true)
    }
}