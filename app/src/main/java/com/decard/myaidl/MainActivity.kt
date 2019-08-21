package com.decard.myaidl

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.decard.myaidl.data.MessageModel
import com.decard.myaidl.service.MessageService
import kotlinx.android.synthetic.main.activity_main.*

/**
 *
 * @author ZJ
 * created at 2019/8/21 11:11
 * 对象跨进程传输的本质就是 序列化，传输，接收和反序列化 这样一个过程，这也是为什么跨进程传输的对象必须实现Parcelable接口
 */
class MainActivity : AppCompatActivity() {
    var messageSender: MessageSender? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupService()
    }

    private fun setupService() {
        val intent = Intent(this, MessageService::class.java)
        bindService(intent, serviceConnection, 0)
        startService(intent)
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {


        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {


            //使用asInterface取得AIDL对应的操作接口
            messageSender = MessageSender.Stub.asInterface(service)
            //生成消息实体对象
            val messageModel = MessageModel("client", "service", "天王盖地虎")
            //调用远程Service的sendMessage方法，并传递消息实体对象
            messageSender!!.sendMessage(messageModel)
            //把接收消息的接口注册到服务器
            messageSender!!.registerReceiveListener(messageReceiver)


            //设置Binder死亡监听
            messageSender!!.asBinder().linkToDeath(deathRecipient, 0)
        }

    }

    //监听服务端的消息通知
    private val messageReceiver = object : MessageReceiver.Stub() {
        override fun onMessageReceived(messageModel: MessageModel?) {
            Log.d("-------client", messageModel.toString())
            runOnUiThread {
                tv_receive.text = messageModel.toString()
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        //解除消息监听接口
        if (messageSender != null && messageSender!!.asBinder().isBinderAlive) {
            messageSender!!.unRegisterReceiveListener(messageReceiver)
        }
        unbindService(serviceConnection)
    }


    /**
     * Binder可能会意外死亡，（Service crash）
     * client监听到Binder死亡后可以进行重连服务器等操作
     *
     */
    val deathRecipient = object : IBinder.DeathRecipient {
        override fun binderDied() {
            Log.d("------bindDied", "bindDied")
            if (messageSender != null) {
                //移除代理
                messageSender!!.asBinder().unlinkToDeath(this, 0)
                messageSender = null
            }
            setupService()
        }
    }
}
