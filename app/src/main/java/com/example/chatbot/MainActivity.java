package com.example.chatbot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private TextView messagesTextView;
    private EditText inputEditText;
    private Context context;
    /**
     * The conversationContext is a JSONObject returned by the Watson API.
     * It is responsible to keep the conversation flow.
     */
    private JSONObject conversationContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        messagesTextView = findViewById(R.id.messagesTextView);
        inputEditText = findViewById(R.id.inputEditText);
        context = this;
        getResponse();
    }

    /** This method will be called when the "Send" button is pressed
     *
     * @param view - autogenerated
     */
    public void send(View view){
        String input = inputEditText.getText().toString();
        // print on the outputTextView what the user types
        messagesTextView.append(Html.fromHtml("<p><b>You:</b> " + input + "</p>"));
        getResponse();
        inputEditText.setText("");
    }

    /**
     * This method is responsible for the communication with the watson API
     */
    private void getResponse() {
        String workspaceId = "a7436f08-a414-41ab-9906-fa9e6324be46";
        String urlAssistant = "https://gateway.watsonplatform.net/assistant/api/v1/workspaces/" +
                workspaceId +
                "/message?version=2019-02-28";
        String authentication = "YXBpa2V5OlNqOWdOeHdNOF9CS2Naamd1eW1jOE5DeUxseVpUSXU2YmlzdzdteUM4Ukh3";
        AndroidNetworking.post(urlAssistant)
                .addHeaders("Content-Type", "application/json")
                .addHeaders("Authorization", "Basic " + authentication)
                .addJSONObjectBody(createJsonObjectBody())
                .setPriority(Priority.HIGH)
                .setTag(R.string.app_name)
                .build()
                .getAsJSONObject(getOutputMessage());
    }

    /** This method builds the JSONObject that will be passed as parameter to the Watson API

     *
     * @return JSONObject
     */
    private JSONObject createJsonObjectBody(){
        JSONObject inputJsonObject = new JSONObject();
        JSONObject jsonBody = new JSONObject();
        try {
            inputJsonObject.put("text", inputEditText.getText().toString());
            // put the text Json in the main JSONObject
            jsonBody.put("input", inputJsonObject);
            // put the conversation context Json in the main JSONObject
            jsonBody.put("context", conversationContext);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonBody;
    }

    /**
     * This method gets response from the Watson and prints it in the outputTextView
     * @return JSONObjectListener
     */
    private JSONObjectRequestListener getOutputMessage(){
        return new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject response) {
                JSONArray outputJsonObject;
                try {
                    // Get the response text from Watson
                    outputJsonObject = response.getJSONObject("output").getJSONArray("text");
                    // Refresh the conversation context
                    conversationContext = response.getJSONObject("context");
                    /* Sometimes Watson can return more then one string
                    *  These strings are in a JSONArray that is iterated by the for bellow
                    */
                    for(int index=0; index<outputJsonObject.length(); index++){
                        // Print the messages in the outputTextView
                        messagesTextView.append(Html.fromHtml("<p><b>Bot:</b> " + outputJsonObject.get(index) + "</p>"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onError(ANError anError) {
                // Shows a message of error in the case of the connection fails
                Toast.makeText(context, "connection error", Toast.LENGTH_SHORT).show();
            }
        };
    }

}
