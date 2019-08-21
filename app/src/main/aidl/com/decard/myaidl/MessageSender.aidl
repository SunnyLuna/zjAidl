// MessageSender.aidl
package com.decard.myaidl;
import com.decard.myaidl.data.MessageModel;
import com.decard.myaidl.MessageReceiver;
interface MessageSender{

   void sendMessage(in MessageModel model);
   void registerReceiveListener(MessageReceiver messageReceiver);
   void unRegisterReceiveListener(MessageReceiver messageReceiver);
}
