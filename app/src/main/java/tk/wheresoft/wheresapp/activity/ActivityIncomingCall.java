package tk.wheresoft.wheresapp.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fima.glowpadview.GlowPadView;
import com.google.gson.Gson;
import tk.wheresoft.wheresapp.R;

import tk.wheresoft.wheresapp.bussiness.calls.ASCalls;
import tk.wheresoft.wheresapp.bussiness.calls.factory.ASCallsFactory;
import tk.wheresoft.wheresapp.integration.calls.factory.DAOCallsFactory;
import tk.wheresoft.wheresapp.model.Call;
import tk.wheresoft.wheresapp.model.CallState;
import tk.wheresoft.wheresapp.model.Contact;

import java.io.IOException;


public class ActivityIncomingCall extends Activity implements GlowPadView.OnTriggerListener {

    public static final String KEY_CONTACT = "CONTACT";
    private ImageView imagenContacto;
    private TextView nombreContacto;
    private Contact contact;
    private Call call;
    private BroadcastReceiver updateCallReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras().containsKey("update")) {
                Log.d("IncomingCall", "updateCallReceiver");
                Bundle extras = intent.getExtras();
                if (extras != null && !extras.isEmpty()) {  // has effect of unparcelling Bundle
                    if (extras.containsKey("type")) {
                        String type = extras.getString("type");
                        switch (type) {
                            case "call": {
                                String message = extras.getString("message");
                                Call callReceive = gson.fromJson(message, Call.class);
                                if (call.getServerId().equals(callReceive.getServerId())) {
                                    if (callReceive.getState().equals(CallState.END) && call.getState().equals(CallState.WAIT)) {
                                        call.setUpdate(callReceive.getUpdate());
                                        call.setEnd(callReceive.getEnd());
                                        call.setState(callReceive.getState());
                                        DAOCallsFactory.getInstance().getInstanceDAOCalls(ActivityIncomingCall.this).update(call);
                                        Toast.makeText(ActivityIncomingCall.this, "Se ha finalizado la llamada", Toast.LENGTH_LONG).show();
                                        finish();
                                    }
                                }
                            }
                        }
                        setResultCode(Activity.RESULT_OK);
                    }
                }
            }
        }
    };
    private ASCalls asCalls;
    private Gson gson = new Gson();
    private Vibrator vibrator;
    private Ringtone r;
    private GlowPadView mGlowPadView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_incoming_call);
        mGlowPadView = (GlowPadView) findViewById(R.id.glow_pad_view);

        mGlowPadView.setOnTriggerListener(this);

        // uncomment this to make sure the glowpad doesn't vibrate on touch
        // mGlowPadView.setVibrateEnabled(false);

        // uncomment this to hide targets
        mGlowPadView.setShowTargetsOnIdle(true);
        asCalls = ASCallsFactory.getInstance().getInstanceASCalls(this);
        call = asCalls.getActiveCall();
        if (call == null) {
            finish();
        }
        IntentFilter filter = new IntentFilter("com.google.android.c2dm.intent.RECEIVE");
        registerReceiver(updateCallReceiver, filter);
        imagenContacto = (ImageView) findViewById(R.id.imageContact);
        nombreContacto = (TextView) findViewById(R.id.textName);
        if (getIntent().getExtras().containsKey(KEY_CONTACT)) {
            contact = (Contact) getIntent().getExtras().getSerializable(KEY_CONTACT);
            if (contact.getImageURI() != null)
                imagenContacto.setImageURI(Uri.parse(contact.getImageURI()));
            nombreContacto.setText(contact.getName());
        }
        Uri ringTone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        r = RingtoneManager.getRingtone(getApplicationContext(), ringTone);
        r.play();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        //Set the pattern, like vibrate for 300 milliseconds and then stop for 200 ms, then
        //vibrate for 300 milliseconds and then stop for 500 ms and repeat the same style. You can change the pattern and
        // test the result for better clarity.
        long pattern[] = {0, 300, 200, 300, 500};
        //start vibration with repeated count, use -1 if you don't want to repeat the vibration
        vibrator.vibrate(pattern, 0);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_incoming_call, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void rechazarLlamada() {
        new AsyncTask<Void, Void, Bundle>() {
            @Override
            protected Bundle doInBackground(Void... params) {
                Bundle bundle = new Bundle();
                try {
                    asCalls.reject(call);
                } catch (IOException e) {
                    bundle.putBoolean("ERROR", true);
                }
                return new Bundle();
            }

            @Override
            protected void onPostExecute(Bundle bundle) {
                if (bundle.containsKey("ERROR")) {
                    Toast.makeText(ActivityIncomingCall.this, "Ha ocurrido un error", Toast.LENGTH_LONG).show();
                }
                finish();
            }
        }.execute(null, null, null);
    }

    private void aceptarLlamada() {
        new AsyncTask<Void, Void, Bundle>() {
            @Override
            protected Bundle doInBackground(Void... params) {
                Bundle bundle = new Bundle();
                try {
                    if (!asCalls.accept(call)) {
                        bundle.putBoolean("ERROR", true);
                    }
                } catch (IOException e) {
                    bundle.putBoolean("ERROR", true);
                }
                return bundle;
            }

            @Override
            protected void onPostExecute(Bundle bundle) {
                if (bundle.containsKey("ERROR")) {
                    Toast.makeText(ActivityIncomingCall.this, "Ha ocurrido un error", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Intent intent = new Intent(ActivityIncomingCall.this, MapActivity.class);
                    intent.putExtra("TOUSER", contact);
                    intent.putExtra("INCOMING", true);
                    startActivity(intent);
                    finish();
                }
            }
        }.execute(null, null, null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(updateCallReceiver);
    }


    @Override
    public void onGrabbed(View view, int i) {

    }

    @Override
    public void onReleased(View view, int i) {
        mGlowPadView.ping();
    }

    @Override
    public void onTrigger(View view, int i) {
        final int resId = mGlowPadView.getResourceIdForTarget(i);
        switch (resId) {
            case R.drawable.ic_item_camera:
                mGlowPadView.setEnabled(false);
                vibrator.cancel();
                rechazarLlamada();
                r.stop();
                break;
            case R.drawable.ic_item_google:
                mGlowPadView.setEnabled(false);
                vibrator.cancel();
                aceptarLlamada();
                r.stop();
                break;
            default:
                // Code should never reach here.
        }
    }

    @Override
    public void onGrabbedStateChange(View view, int i) {

    }

    @Override
    public void onFinishFinalAnimation() {

    }
}
