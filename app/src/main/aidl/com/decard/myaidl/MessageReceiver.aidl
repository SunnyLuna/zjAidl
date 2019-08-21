// MessageReceiver.aidl
package com.decard.myaidl;
import com.decard.myaidl.data.MessageModel;
// Declare any non-default types here with import statements

interface MessageReceiver {
   void onMessageReceived(in MessageModel messageModel);
}
