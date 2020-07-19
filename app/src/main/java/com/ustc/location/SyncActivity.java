package com.ustc.location;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.ustc.location.location.Anchor;
import com.ustc.location.location.Position;
import com.ustc.location.network.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class SyncActivity extends Activity {
    private static String registerURL = "http://192.168.79.1:80/register.php";
    private static String requestURL = "http://192.168.79.1:80/request.php";
    private static String adduserURL = "http://192.168.79.1:80/adduser.php";
    private static String loginURL = "http://192.168.79.1:80/login.php";
    private static final int SEND_ANCHOR_POSITION= 1;
    private static final int GET_ANCHOR_LIST= 2;
    private static final int REGISTER= 3;
    private static final int LOGIN= 4;
    private int sync_kind;
    private Anchor anchor;
    private boolean flag;
    private Vector<Anchor> syncResult;

    private String userName,md5Pwd,school;
    private int sex;
    private String res;
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (sync_kind){
                case SEND_ANCHOR_POSITION:{
                    switch (msg.what){
                        case 0:{
                            Intent i = new Intent();
                            Bundle bundle = new Bundle();
                            bundle.putString("result", "同步成功");
                            setResult(0, i.putExtras(bundle));
                            SyncActivity.this.finish();
                            break;
                        }
                        case 1:{
                            Intent i = new Intent();
                            Bundle bundle = new Bundle();
                            bundle.putString("result", "同步失败");
                            setResult(1, i.putExtras(bundle));
                            SyncActivity.this.finish();
                            break;
                        }
                    }
                }
                case GET_ANCHOR_LIST:{
                    switch (msg.what){
                        case 0:{
                            Intent i = new Intent();
                            Bundle bundle = new Bundle();
                            bundle.putString("result", "同步成功");
                            bundle.putSerializable("syncResult",syncResult);
                            setResult(0, i.putExtras(bundle));
                            SyncActivity.this.finish();
                            break;
                        }
                        case 1:{
                            Intent i = new Intent();
                            Bundle bundle = new Bundle();
                            bundle.putString("result", "同步失败");
                            bundle.putSerializable("syncResult",syncResult);
                            setResult(1, i.putExtras(bundle));
                            SyncActivity.this.finish();
                            break;
                        }
                    }
                }
                case REGISTER:{
                    switch (msg.what){
                        case 0:{
                            Intent i = new Intent();
                            Bundle bundle = new Bundle();
                            bundle.putString("result", "同步成功");
                            setResult(0, i.putExtras(bundle));
                            SyncActivity.this.finish();
                            break;
                        }
                        case 1:{
                            Intent i = new Intent();
                            Bundle bundle = new Bundle();
                            bundle.putString("result", "同步失败");
                            setResult(1, i.putExtras(bundle));
                            SyncActivity.this.finish();
                            break;
                        }
                    }
                }
                case LOGIN:{
                    switch (msg.what){
                        case 0:{
                            Intent i = new Intent();
                            Bundle bundle = new Bundle();
                            bundle.putString("result", "同步成功");
                            setResult(0, i.putExtras(bundle));
                            SyncActivity.this.finish();
                            break;
                        }
                        case 1:{
                            Intent i = new Intent();
                            Bundle bundle = new Bundle();
                            bundle.putString("result", "同步失败");
                            setResult(1, i.putExtras(bundle));
                            SyncActivity.this.finish();
                            break;
                        }
                    }
                }
            }



        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);
        processSyncKind();
        processThread();
    }
    private void processSyncKind(){
        sync_kind= getIntent().getIntExtra("sync_kind",-1);
        switch (sync_kind){
            case SEND_ANCHOR_POSITION:{
                anchor = (Anchor) getIntent().getSerializableExtra("anchor");
                break;
            }
            case REGISTER:{
                userName=(String)getIntent().getStringExtra("userName");
                md5Pwd=(String)getIntent().getStringExtra("md5Pwd");
                school=(String)getIntent().getStringExtra("school");
                sex=(int)getIntent().getIntExtra("sex",0);
            }
            case LOGIN:{
                userName=(String)getIntent().getStringExtra("userName");
                md5Pwd=(String)getIntent().getStringExtra("md5Pwd");
            }
        }

    }
    private void processThread() {
        switch (sync_kind){
            case SEND_ANCHOR_POSITION:{
                new Thread(){
                    public void run() {
                        try {
                            List<NameValuePair> params = new ArrayList<NameValuePair>();
                            params.add(new BasicNameValuePair("mac", anchor.getMac()));
                            params.add(new BasicNameValuePair("x", String.valueOf(anchor.getPos().getPosX())));
                            params.add(new BasicNameValuePair("y", String.valueOf(anchor.getPos().getPosY())));
                            flag= JSONParser.makeHttpRequest(registerURL, "POST",	params);
                        } catch (Exception e) {
                            e.printStackTrace();
                            handler.sendEmptyMessage(1);
                        }
                        if (flag==true){
                            handler.sendEmptyMessage(0);
                        }else{
                            handler.sendEmptyMessage(1);
                        }

                    }
                }.start();
                break;
            }
            case GET_ANCHOR_LIST :{
                new Thread(){
                    public void run() {
                        try {
                            JSONArray jsonRes= JSONParser.getJSONArrayFromUrl(requestURL);
                            //System.out.println(jsonRes.length());
                            System.out.println(jsonRes);
                            syncResult=new Vector<Anchor>();
                            for(int i=0;i<jsonRes.length();i++){
                                JSONObject o = jsonRes.getJSONObject(i);
                                Anchor a=new Anchor(o.getString("mac"),new Position(o.getDouble("x"),o.getDouble("y")));
                                syncResult.add(a);
                            }
                            System.out.println(syncResult);
                        } catch (Exception e) {
                            e.printStackTrace();
                            syncResult=new Vector<Anchor>();
                            handler.sendEmptyMessage(1);
                        }
                        handler.sendEmptyMessage(0);
                    }
                }.start();
                break;
            }
            case REGISTER:{
                new Thread(){
                    public void run() {
                        try {
                            List<NameValuePair> params = new ArrayList<NameValuePair>();
                            params.add(new BasicNameValuePair("userName", userName));
                            params.add(new BasicNameValuePair("md5Psw", md5Pwd));
                            params.add(new BasicNameValuePair("sex", String.valueOf(sex)));
                            params.add(new BasicNameValuePair("school", school));
                            flag= JSONParser.makeHttpRequest(adduserURL, "POST",	params);
                        } catch (Exception e) {
                            e.printStackTrace();
                            handler.sendEmptyMessage(1);
                        }
                        if (flag==true){
                            handler.sendEmptyMessage(0);
                        }else{
                            handler.sendEmptyMessage(1);
                        }

                    }
                }.start();
                break;
            }
            case LOGIN:{
                new Thread(){
                    public void run() {
                        try {
                            List<NameValuePair> params = new ArrayList<NameValuePair>();
                            params.add(new BasicNameValuePair("userName", userName));
                            params.add(new BasicNameValuePair("md5Psw", md5Pwd));
                            res= JSONParser.makeLoginHttpRequest(loginURL, "POST",	params);
                        } catch (Exception e) {
                            e.printStackTrace();
                            handler.sendEmptyMessage(1);
                        }
                        if (res==""||!res.trim().equals(md5Pwd.trim())){
                            handler.sendEmptyMessage(1);
                        }else{
                            handler.sendEmptyMessage(0);
                        }

                    }
                }.start();
                break;
            }
        }
    }
}
