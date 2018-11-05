package com.example.suhee.mymymymy;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.perples.recosdk.RECOBeacon;
import com.perples.recosdk.RECOBeaconRegion;
import com.perples.recosdk.RECOErrorCode;
import com.perples.recosdk.RECORangingListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;


public class GuideeActivity  extends RecoActivity implements RECORangingListener {

    private RecoListAdapter mRangingListAdapter;
    private ListView mRegionListView;

    int minor[] = new int[3];
    int rssi[] = new int[3];
    int from;
    String to;

    //외벽등
    int temp=4;
    int count=0;
    int[] M={0,0,0};

    TextView stairText;
    TextView directText;
    ImageView arrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guidee);

        arrow = (ImageView)findViewById(R.id.imageView2);
        arrow.setImageResource(R.drawable.reallogo);

        stairText = (TextView)findViewById(R.id.textView3);
        directText = (TextView)findViewById(R.id.textView4);

        ImageButton call = findViewById(R.id.imageButton);
        ImageButton map = findViewById(R.id.imageButton2);

        mRecoManager.setRangingListener(this);
        mRecoManager.bind(this);

        MyClientTask myClientTask = new MyClientTask();
        myClientTask.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mRangingListAdapter = new RecoListAdapter(this);
        mRegionListView = (ListView)findViewById(R.id.list_testing);
        mRegionListView.setAdapter(mRangingListAdapter);

    }

    public int getBeacon() {

        temp=M[0];
        for (int i = 0; i < 3; i++) {
            minor[i] = mRangingListAdapter.getMinor(i);
            rssi[i] = mRangingListAdapter.getRssi(i);
            M[i]=minor[i];
        }

        if(temp==minor[0]) count++;
        else count=0;

        //위치인식
        if(minor[0]==7799 && rssi[0]>-100) from=5;
        else if(minor[0]==7796 && rssi[0]>-100) from=6;
        else if(minor[0]==7800 && rssi[0]>-100) from=7 ; //수정필요
        else if(minor[0]==7803 && rssi[0]>-100) from=8;
        else if(minor[0]==7797 && rssi[0]>-100) from=9;
        else if(minor[0]==7798 && rssi[0]>-100) from=10;
        else from=11;

        MyClientTask2 myClientTask2 = new MyClientTask2();
        myClientTask2.execute();

        return 0;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.stop(mRegions);
        this.unbind();
    }

    private void unbind() {
        try {
            mRecoManager.unbind();
        } catch (RemoteException e) {
            Log.i("RECORangingActivity", "Remote Exception");
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceConnect() {
        Log.i("RECORangingActivity", "onServiceConnect()");
        mRecoManager.setDiscontinuousScan(MainActivity.DISCONTINUOUS_SCAN);
        this.start(mRegions);
        //Write the code when RECOBeaconManager is bound to RECOBeaconService
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<RECOBeacon> recoBeacons, RECOBeaconRegion recoRegion) {
        Log.i("RECORangingActivity", "didRangeBeaconsInRegion() region: " + recoRegion.getUniqueIdentifier() + ", number of beacons ranged: " + recoBeacons.size());

        mRangingListAdapter.updateAllBeacons(recoBeacons);
        mRangingListAdapter.notifyDataSetChanged();
        getBeacon();
        //Write the code when the beacons in the region is received
    }

    @Override
    protected void start(ArrayList<RECOBeaconRegion> regions) {

        for(RECOBeaconRegion region : regions) {
            try {
                mRecoManager.startRangingBeaconsInRegion(region);
            } catch (RemoteException e) {
                Log.i("RECORangingActivity", "Remote Exception");
                e.printStackTrace();
            } catch (NullPointerException e) {
                Log.i("RECORangingActivity", "Null Pointer Exception");
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void stop(ArrayList<RECOBeaconRegion> regions) {
        for(RECOBeaconRegion region : regions) {
            try {
                mRecoManager.stopRangingBeaconsInRegion(region);
            } catch (RemoteException e) {
                Log.i("RECORangingActivity", "Remote Exception");
                e.printStackTrace();
            } catch (NullPointerException e) {
                Log.i("RECORangingActivity", "Null Pointer Exception");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onServiceFail(RECOErrorCode errorCode) {
        //Write the code when the RECOBeaconService is failed.
        //See the RECOErrorCode in the documents.
        return;
    }

    @Override
    public void rangingBeaconsDidFailForRegion(RECOBeaconRegion region, RECOErrorCode errorCode) {
        Log.i("RECORangingActivity", "error code = " + errorCode);
        //Write the code when the RECOBeaconService is failed to range beacons in the region.
        //See the RECOErrorCode in the documents.
        return;
    }

    private String dstAddress = "13.209.108.212"; // IP
    private int dstPort = 9999; // PORT번호

    String Start = "0";

    public class MyClientTask extends AsyncTask<Void, Void, Void> {

        String response = "";
        String myMessage = "";

        //constructor
        MyClientTask(){
            myMessage = Start;
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            Socket socket = null;
            myMessage = myMessage.toString();
            try {
                socket = new Socket(dstAddress, dstPort);
                //송신
                OutputStream out = socket.getOutputStream();
                out.write(myMessage.getBytes());

                //수신
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
                byte[] buffer = new byte[1024];
                int bytesRead;
                InputStream inputStream = socket.getInputStream();
                /*
                 * notice:
                 * inputStream.read() will block if no data return
                 */
                while ((bytesRead = inputStream.read(buffer)) != -1){
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                    response += byteArrayOutputStream.toString("UTF-8");
                    Start = "done";
                }
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response = "UnknownHostException: " + e.toString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response = "IOException: " + e.toString();
            }finally{
                if(socket != null){
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //    recieveText.setText(response);
            super.onPostExecute(result);

            if (response.charAt(0) == 'n')
                directText.setText("화재가 발생하지 않았습니다.");
            else {
                stairText.setText("화재 발생 위치\n"+"컴퓨터공학과 사무실 앞");
            }

        }
    } //MyClientTask 종료

    //불난 함수
    public void fireFunc(){
        //핑거프린팅 코드
        if(from==5){
            if(to.equals("3")) {
                arrow.setImageResource(R.drawable.from5to3);
                directText.setText("비상구로 대피하세요.");
            }
            else if(to.equals("6")) {
                arrow.setImageResource(R.drawable.from5to6);
                directText.setText("화장실 방향으로 대피하세요.");
            }
        }
        else if(from==6){
            if(to.equals("5")) {
                arrow.setImageResource(R.drawable.from6to5);
                directText.setText("계단 방향으로 대피하세요.");
            }
            else if(to.equals("7")) {
                arrow.setImageResource(R.drawable.from6to7);
                directText.setText("화장실 방향으로 대피하세요.");
            }
        }
        else if(from==7){
            if(to.equals("6")) {
                arrow.setImageResource(R.drawable.from7to6);
                directText.setText("직진하세요.");
            }
            else if(to.equals("8")) {
                arrow.setImageResource(R.drawable.from7to8);
                directText.setText("직진하세요.");
            }
        }
        else if(from==8){
            if(to.equals("7")) {
                arrow.setImageResource(R.drawable.from8to7);
                directText.setText("직진하세요.");
            }
            else if(to.equals("8")) {
                arrow.setImageResource(R.drawable.from8to9);
                directText.setText("직진하세요.");
            }
        }
        else if(from==9){
            if(to.equals("1")&&count<5) {
                arrow.setImageResource(R.drawable.from9to1);
                directText.setText("계단으로 대피하세요.");
            }
            else if(to.equals("8")&&count<5) {
                arrow.setImageResource(R.drawable.from9to8);
                directText.setText("직진하세요.");
            }
            else {
                arrow.setImageResource(R.drawable.light);
                directText.setText("외벽등이 있는 곳으로 대피하세요.");
            }

        }
        else if(from==10){
            if(count<5) {
                arrow.setImageResource(R.drawable.from10to9);
                directText.setText("계단으로 대피하세요.");
            }
            else {
                arrow.setImageResource(R.drawable.light);
                directText.setText("외벽등이 있는 곳으로 대피하세요.");
            }
        }
        else {
            arrow.setImageResource(R.drawable.reallogo);
            directText.setText("이화여자대학교 아산공학관");
        }
        /*
        if(from==9){
            if(count<3) {
                arrow.setImageResource(R.drawable.d7797);
                directText.setText("계단으로 대피하세요.");
            }
            else {
                arrow.setImageResource(R.drawable.light);
                directText.setText("외벽등이 있는 곳으로 대피하세요.");
            }
        }
        else if(from==10){
            if(count<3) {
                arrow.setImageResource(R.drawable.d7798);
                directText.setText("계단으로 대피하세요.");
            }
            else {
                arrow.setImageResource(R.drawable.light);
                directText.setText("외벽등이 있는 곳으로 대피하세요.");
            }

        }
        else{
            arrow.setImageResource(R.drawable.reallogo);
            directText.setText("이화여자대학교 아산공학관");
        }*/
    }

    public class MyClientTask2 extends AsyncTask<Void, Void, Void> {

        String response2 = "";
        String myMessage = "";

        //constructor
        MyClientTask2(){
            int sendm;

            if(count>=3) sendm=temp;
            else sendm=from;

            myMessage = String.valueOf(sendm);
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            Socket socket = null;
            myMessage = myMessage.toString();
            try {
                socket = new Socket(dstAddress, dstPort);
                //송신
                OutputStream out = socket.getOutputStream();
                out.write(myMessage.getBytes());

                //수신
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(10);
                byte[] buffer = new byte[10];
                int bytesRead;
                InputStream inputStream = socket.getInputStream();
                /*
                 * notice:
                 * inputStream.read() will block if no data return
                 */
                while ((bytesRead = inputStream.read(buffer)) != -1){
                    Log.d(this.getClass().getName(), "데이터 받음");
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                    response2 += byteArrayOutputStream.toString("UTF-8");
                }
                to=response2;
                response2 = "서버의 응답: " + response2;

            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response2 = "UnknownHostException: " + e.toString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response2 = "IOException: " + e.toString();
            }finally{
                if(socket != null){
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            fireFunc();
        }
    } //MyClientTask2 종료

    int check=0;
    public void onClick(View v) {
        MyClientTask3 myClientTask3 = new MyClientTask3();
        switch (v.getId()) {
            case R.id.imageButton:
                //전화 걸려면 ACTION_DIAL
                check=1;
                myClientTask3.execute();
                break;
            case R.id.imageButton2:
                check=2;
                myClientTask3.execute();
                break;


        }

    }

    public class MyClientTask3 extends AsyncTask<Void, Void, Void> {
        //constructor
        MyClientTask3(){

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if(check==1){
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:119"));
                startActivity(intent);
            }
            else if(check==2){
                Dialog dialog = new Dialog(GuideeActivity.this);
                dialog.setContentView(R.layout.pop);
                dialog.setTitle("아산공학관 2층");
                //전화 걸려면
                //TextView tv = (TextView) dialog.findViewById(R.id.text)
                ImageView iv = (ImageView) dialog.findViewById(R.id.imageView3);
                iv.setImageResource(R.drawable.map);
                dialog.show();
            }

        }
    } //MyClientTask3 종료

}