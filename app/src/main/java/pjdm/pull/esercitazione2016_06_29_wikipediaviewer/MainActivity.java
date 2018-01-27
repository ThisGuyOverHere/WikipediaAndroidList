package pjdm.pull.esercitazione2016_06_29_wikipediaviewer;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    String searchTerm;
    String url;
    String apiUrl;
    ArrayList<String> items = new ArrayList<String>();
    ListView lv;
    ArrayAdapter<String> adapter;
    ArrayList<JSONObject> JSONItems = new ArrayList<JSONObject>();


    private final Handler handler =	new	Handler() {
        @Override
        public void handleMessage(android.os.Message msg){
            super.handleMessage(msg);
            new GetData().execute();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(savedInstanceState!= null){
            for (int i = 0; i < savedInstanceState.size(); i++) {
                items.add(savedInstanceState.getString(String.valueOf(i)));
            }
        }

        url = getString(R.string.url);
        apiUrl = url + "w/api.php";

        lv = (ListView) findViewById(R.id.searchItemList);
        adapter = new ArrayAdapter<String>(this, R.layout.riga,R.id.element, items);

        //Codice del bottone random

        Button randomButton = (Button) findViewById(R.id.buttonRandom);

        randomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url+"wiki/Special:Random"));
                startActivity(i);
            }
        });

        //Codice del bottone cerca

        final EditText searchText = (EditText) findViewById(R.id.searchText);

        Button searchButton = (Button) findViewById(R.id.buttonSearch);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchTerm = searchText.getText().toString();
                handler.sendMessage(handler.obtainMessage());
                lv.invalidate();
            }
        });

        //Codice per andare su una pagina dell'elenco

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                TextView tv = (TextView) view.findViewById(R.id.element);
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url + "wiki/" + tv.getText().toString()));
                startActivity(i);

            }
        });
        lv.setAdapter(adapter);
    }

    private class GetData extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... voids){
            String s = "";

            try {

                //URL url = new URL(searchURLparts[0]+searchTerm+searchURLparts[1]);
                URL url = new URL (Uri.parse(apiUrl)
                        .buildUpon()
                        .appendQueryParameter("action","query")
                        .appendQueryParameter("prop","links")
                        .appendQueryParameter("list","search")
                        .appendQueryParameter("srsearch",searchTerm)
                        .appendQueryParameter("format","json").build().toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                s = br.readLine();

                Log.d("PJDM", s);


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return s;
        }

        @Override
        protected void onPostExecute(String s){
            super.onPostExecute(s);
            if(s!=null){
                try {
                    items.clear();
                    JSONObject jo = new JSONObject(s);
                    JSONArray articles = jo.getJSONObject("query").getJSONArray("search");
                    for(int i = 0; i < articles.length(); i++){
                        items.add(articles.getJSONObject(i).getString("title"));
                        JSONItems.add(articles.getJSONObject(i));
                        Log.d("PJDM", items.get(i));
                    }
                    lv.setAdapter(adapter);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.clear();
        for (int i = 0; i < items.size() ; i++) {
            savedInstanceState.putString(String.valueOf(i), items.get(i));
        }
    }

}
