package ipc.demo.merlin.ipc_demo_client;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.SyncStateContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import ipc.demo.merlin.ipc_demo.SercviceAidlInterface;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private SercviceAidlInterface remoteService;
    private Button bind_btn;
    private Button read_btn;
    private TextView messenger_txt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent =new Intent("com.demo.IPCService");
        intent.setPackage("ipc.demo.merlin.ipc_demo");
        bindService(intent,conn, Context.BIND_AUTO_CREATE);

        Intent intent1=new Intent("com.demo.MessgenerService");
        intent1.setPackage("ipc.demo.merlin.ipc_demo");
        bindService(intent1,messengerConn,Context.BIND_AUTO_CREATE);

        messenger_txt=(TextView)findViewById(R.id.messenger_txt);
        bind_btn=((Button)findViewById(R.id.bind_btn));
        read_btn=(Button)findViewById(R.id.read_btn);
        bind_btn.setOnClickListener(this);
        read_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bind_btn:
        try{
           bind_btn.setText(remoteService.getString());
        }catch (Exception ex){
            bind_btn.setText("fail");
        }
                break;
            case R.id.read_btn:
                Uri uri= Uri.parse("content://ipc.demo.merlin.ipc_demo_provider/user/小明");
                Cursor cursor=getContentResolver().query(uri,null,null,null,null);
                if(cursor!=null){
                    while(cursor.moveToNext()){
                        read_btn.setText(cursor.getString(cursor.getColumnIndex("name"))+cursor.getString(cursor.getColumnIndex("sex")));
                    }
                    cursor.close();
                }
                break;
        };
    }

    private ServiceConnection conn=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            remoteService=SercviceAidlInterface.Stub.asInterface(iBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };


    private Messenger serverMessenger;

    private Handler messengerhandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            messenger_txt.setText(msg.getData().getString("reply"));

        }
    };

    private Messenger mGetReplyMessenger = new Messenger(messengerhandler);
    private ServiceConnection messengerConn=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                serverMessenger=new Messenger(iBinder);
                Message msg=Message.obtain(null, 0);
                msg.replyTo=mGetReplyMessenger;
                try{
                    serverMessenger.send(msg);
                }catch (RemoteException ex){
                    ex.printStackTrace();
                }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

}
