package com.example.tfgisma;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class CompeticionActivity extends AppCompatActivity {

    private TableLayout equiposTableLayout;
    private Button logoutButton, btnJugadores, btnCompeticion, btnResultados, btnEquipo;
    private String idUsuario;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_competicion);

        equiposTableLayout = findViewById(R.id.equiposTableLayout);
        logoutButton = findViewById(R.id.logoutButton);
        btnJugadores = findViewById(R.id.btnJugadores);
        btnCompeticion = findViewById(R.id.btnCompeticion);
        btnResultados = findViewById(R.id.btnResultados);
        btnEquipo = findViewById(R.id.btnEquipo);
        progressBar = findViewById(R.id.progressBar);

        Intent intent = getIntent();
        idUsuario = intent.getStringExtra("id_usuario");

        new FetchCompeticionTask().execute(idUsuario);

        logoutButton.setOnClickListener(v -> {
            Intent logoutIntent = new Intent(CompeticionActivity.this, LoginActivity.class);
            logoutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(logoutIntent);
            finish();
        });

        btnJugadores.setOnClickListener(v -> navigateTo(JugadoresActivity.class));
        btnCompeticion.setOnClickListener(v -> navigateTo(CompeticionActivity.class));
        btnResultados.setOnClickListener(v -> navigateTo(ResultadosActivity.class));
        btnEquipo.setOnClickListener(v -> navigateTo(EquipoActivity.class));
    }

    private void navigateTo(Class<?> activityClass) {
        Intent intent = new Intent(CompeticionActivity.this, activityClass);
        intent.putExtra("id_usuario", idUsuario);
        startActivity(intent);
    }

    private class FetchCompeticionTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE); // Show progress bar
        }

        @Override
        protected String doInBackground(String... params) {
            String idUsuario = params[0];
            String result = "";

            try {
                URL url = new URL("http://172.20.10.2/MANAGERFOOT/Competicion.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                // Enviar id_usuario
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write("id_usuario=" + URLEncoder.encode(idUsuario, "UTF-8"));
                writer.flush();
                writer.close();
                os.close();

                // Leer respuesta
                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                is.close();
                conn.disconnect();

                result = response.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressBar.setVisibility(View.GONE); // Hide progress bar
            if (result == null) {
                showErrorDialog("Error en la conexión con el servidor.");
                return;
            }
            Log.d("CompeticionActivity", "Response from server: " + result);
            try {
                JSONObject jsonObject = new JSONObject(result);
                String status = jsonObject.getString("status");

                if (status.equals("success")) {
                    JSONArray equipos = jsonObject.getJSONArray("equipos");
                    for (int i = 0; i < equipos.length(); i++) {
                        JSONObject equipo = equipos.getJSONObject(i);
                        String nombre = equipo.getString("nombre");

                        TableRow row = new TableRow(CompeticionActivity.this);
                        row.addView(createTextView(nombre));
                        equiposTableLayout.addView(row);
                    }
                } else {
                    String message = jsonObject.getString("message");
                    showErrorDialog(message);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                showErrorDialog("Error al procesar la respuesta del servidor.");
            }
        }

        private TextView createTextView(String text) {
            TextView textView = new TextView(CompeticionActivity.this);
            textView.setText(text);
            textView.setPadding(8, 8, 8, 8);
            return textView;
        }

        private void showErrorDialog(String message) {
            Toast.makeText(CompeticionActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    }
}