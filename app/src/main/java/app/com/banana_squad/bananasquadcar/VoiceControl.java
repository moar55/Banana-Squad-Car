package app.com.banana_squad.bananasquadcar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.List;

public class VoiceControl extends AppCompatActivity {



    private static final int SPEECH_REQUEST_CODE = 0;
    BluetoothConnection connection;
    String spokenText;
    Button order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connection=new BluetoothConnection(this);
        connection.initiateBluetoothConnection();
        setContentView(R.layout.activity_voice_control);

        order= (Button)findViewById(R.id.give_order);

        order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
// Start the activity, the intent will be populated with the speech text
                startActivityForResult(intent, SPEECH_REQUEST_CODE);
            }
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        final AlertDialog.Builder confirm  = new android.support.v7.app.AlertDialog.Builder(this);

        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);

             spokenText="";
            String allText="";
            for (String text :results)
               allText+=results+" ";


            if(allText.contains("forward") || allText.contains("Forward"))
                spokenText="Forward";


            else if(allText.contains("backward") || allText.contains("Backward") )
                spokenText="Backward";

            else  if(allText.contains("right") || allText.contains("Right") )
                spokenText="Right";

            else if(allText.contains("left") || allText.contains("Left") )
                spokenText="Left";

            else if(allText.contains("straight") || allText.contains("Straight") )
                spokenText="Straight";

            else if(allText.contains("stop") || allText.contains("Stop") )
                spokenText="Stop";

            else
            Log.e("Voice","No-match");


            Log.v("Result",spokenText);
            // Do something with spokenText

            confirm.setTitle("Use this?");

            confirm.setMessage("Do you want to use the command: "+spokenText);

            confirm.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (spokenText) {

                        case "Forward":
                            connection.getManageConnection().send('f');
                            break;

                        case "Backward":
                            connection.getManageConnection().send('b');
                            break;

                        case "Right":
                            connection.getManageConnection().send('r');
                            break;

                        case "Left":
                            connection.getManageConnection().send('l');
                            break;

                        case "Straight":
                            connection.getManageConnection().send('n');
                            break;

                        case "Stop":
                            connection.getManageConnection().send('s');
                            break;
                    }
//                    if(spokenText.equals("Forward"))
//                        connection.getManageConnection().send('f');
//
//                    else if(spokenText.equals("Backward"))
//                        connection.getManageConnection().send('b');



                    dialog.dismiss();
                }
            });

            confirm.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            confirm.show();


        }
            super.onActivityResult(requestCode, resultCode, data);

        }


    @Override
    protected void onPause() {
        super.onPause();
//        connection.getManageConnection().send('s');
    }

    @Override
    protected void onStop() {
        super.onStop();
//        connection.getManageConnection().send('s');
        connection.closeAll();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            connection.closeAll();
        }
        catch (Exception e){
            Log.e("Destroy","Error in closing stuff");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

//        connection.initiateBluetoothConnection();
    }


}
