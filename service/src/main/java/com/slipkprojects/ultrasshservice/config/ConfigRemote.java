package com.slipkprojects.ultrasshservice.config;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class ConfigRemote extends AsyncTask<String, String, String> {

    private Context context;
    private OnUpdateListener listener;
    private ProgressDialog progressDialog;
    private boolean isOnCreate;
    private String urlRemote;
    private Settings mConfig;

    public ConfigRemote(Context context, OnUpdateListener listener, String url_remote) {
        this.context = context;
        this.listener = listener;
        this.urlRemote = url_remote;
        mConfig = new Settings(context);
    }

    public void start(boolean isOnCreate) {
        this.isOnCreate = isOnCreate;
        execute();
    }

    public interface OnUpdateListener {
        void onUpdateListener(String result);
    }


    @Override
    protected String doInBackground(String... strings) {
        try {
            Log.d("doInBackground", this.urlRemote);
            StringBuilder sb = new StringBuilder();
            URL url = new URL(this.urlRemote);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response;

            while ((response = br.readLine()) != null) {
                sb.append(response);
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error on getting data: " + e.getMessage();
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (!isOnCreate) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Please wait while loading");
            progressDialog.setTitle("Verification actualization");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (!isOnCreate && progressDialog != null) {
            progressDialog.dismiss();
        }
        if (listener != null) {
            listener.onUpdateListener(s);
        }
    }
}
