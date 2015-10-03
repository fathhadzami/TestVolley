package com.freecostudio.fathhadzami.testvolley;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    ProgressDialog pDialog;

    Spinner spnDay,spnCity;
    Button btnSearch;
    TextView txtHeaderCity,txtHeaderAvg,txtHeaderAvgVar;

    ListView lvData;
    RequestQueue requestQueue;

    String URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestQueue = Volley.newRequestQueue(this);

        spnDay = (Spinner)findViewById(R.id.spnDay);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.cnt, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnDay.setAdapter(adapter);


        spnCity = (Spinner)findViewById(R.id.spnCity);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.city, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnCity.setAdapter(adapter2);

        txtHeaderCity   = (TextView)findViewById(R.id.txtHeaderCity);
        txtHeaderAvg    = (TextView)findViewById(R.id.txtHeaderAvg);
        txtHeaderAvgVar = (TextView)findViewById(R.id.txtHeaderAvgVar);

        btnSearch = (Button)findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new HitAPI().execute();
            }
        });

        lvData = (ListView)findViewById(R.id.lvData);
    }

    private class HitAPI extends AsyncTask<Void,Void,Void> {
        float avg = 0f, avgVar = 0f;
        int lengthList = 0;
        List<Map<String,String>> data = new ArrayList<Map<String,String>>();
        String jsonResponse = "";
        String urlApi = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Mohon tunggu. Sedang mengambil data...");
            pDialog.show();

            try {
                String city = URLEncoder.encode(spnCity.getSelectedItem().toString(), "UTF-8");
                String cnt = URLEncoder.encode(spnDay.getSelectedItem().toString(), "UTF-8");
                urlApi = URL + "q=" + city + "&mode=json&units=metric&cnt=" + cnt;
                Log.w("urlApi", urlApi);
            }catch (Exception e){
                urlApi = URL;
                Log.e("urlApi", urlApi);
            }
        }

        @Override
        protected Void doInBackground(Void... prmtr) {
            try {
                StringRequest jsonObjectRequest = new StringRequest(Request.Method.GET, urlApi,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                jsonResponse = response;
                                Log.w("response", response);
                                txtHeaderCity.setText(spnCity.getSelectedItem().toString());
                                Log.w("trace", "1");
                                if (jsonResponse.length() > 0) {
                                    Log.w("trace", "2");
                                    JSONArray arrList = new JSONObject(jsonResponse).getJSONArray("list");
                                    lengthList = arrList.length();
                                    Log.w("trace", "1");
                                    for (int i = 0; i < lengthList; i++) {
                                        JSONObject objList = arrList.getJSONObject(i);
                                        Map<String, String> mapData = new HashMap<String, String>();

                                        Date dt = new Date(Integer.parseInt(objList.getString("dt")) * 1000);
                                        mapData.put("dt", new SimpleDateFormat("yyyy-MM-dd").format(dt));

                                        String strDayTemp = objList.getJSONObject("temp").getString("day");
                                        avg += Float.parseFloat(strDayTemp);
                                        mapData.put("dayTemp", strDayTemp);

                                        String strMinTemp = objList.getJSONObject("temp").getString("min");
                                        String strMaxTemp = objList.getJSONObject("temp").getString("max");
                                        float variance = Float.parseFloat(strMaxTemp) - Float.parseFloat(strMinTemp);
                                        avgVar += variance;
                                        mapData.put("varTemp", String.valueOf(variance));

                                        data.add(mapData);
                                    }
                                    Log.w("trace", "3");

                                    avg = avg / lengthList;
                                    avgVar = avgVar / lengthList;
                                    txtHeaderAvg.setText(String.valueOf(avg));
                                    txtHeaderAvgVar.setText(String.valueOf(avgVar));

                                    Log.w("trace", "4");
                                    String[] from = {"dt", "dayTemp", "varTemp"};
                                    int[] view = {R.id.txtDate, R.id.txtDayTemp, R.id.txtVarTemp};
                                    SimpleAdapter ad = new SimpleAdapter(MainActivity.this, data, R.layout.detail_table, from, view);
                                    lvData.setAdapter(ad);
                                    Log.w("trace", "5");
                                }
                            }catch (Exception e){
                                Log.e("doInBackground", e.toString());
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("Volley", error.toString());
                        }
                    }
                );
                requestQueue.add(jsonObjectRequest);
            } catch (Exception err) {
                Log.e("doInBackground", err.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            pDialog.cancel();
            super.onPostExecute(aVoid);
        }
    }
}
