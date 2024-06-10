package com.example.tfgisma;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
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

public class EquipoActivity extends AppCompatActivity {

    private EditText nombreEditText, entrenadorEditText, competicionEditText, tipoTerrenoEditText, golesFavorEditText, golesContraEditText, ciudadEditText;
    private Button btnJugadores, btnCompeticion, btnResultados, btnEquipo;
    private Button logoutButton;
    private String idUsuario;
    private ProgressBar progressBar;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipo);

        // Inicializa las vistas
        nombreEditText = findViewById(R.id.nombreEditText);
        entrenadorEditText = findViewById(R.id.entrenadorEditText);
        competicionEditText = findViewById(R.id.competicionEditText);
        tipoTerrenoEditText = findViewById(R.id.tipoTerrenoEditText);
        golesFavorEditText = findViewById(R.id.golesFavorEditText);
        golesContraEditText = findViewById(R.id.golesContraEditText);
        ciudadEditText = findViewById(R.id.ciudadEditText);

        btnJugadores = findViewById(R.id.btnJugadores);
        btnCompeticion = findViewById(R.id.btnCompeticion);
        btnResultados = findViewById(R.id.btnResultados);
        btnEquipo = findViewById(R.id.btnEquipo);

        logoutButton = findViewById(R.id.logoutButton);
        progressBar = findViewById(R.id.progressBar);

        // Obtiene el id_usuario del Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("id_usuario")) {
            idUsuario = intent.getStringExtra("id_usuario");
            Log.d("EquipoActivity", "Received id_usuario: " + idUsuario);
            new FetchEquipoTask().execute(idUsuario);
        } else {
            Log.e("EquipoActivity", "No id_usuario received in Intent");
            showErrorDialog("No se recibió el id de usuario.");
        }

        logoutButton.setOnClickListener(v -> {
            Intent intent1 = new Intent(EquipoActivity.this, LoginActivity.class);
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent1);
            finish();
        });

        btnJugadores.setOnClickListener(v -> navigateToActivity(JugadoresActivity.class));
        btnCompeticion.setOnClickListener(v -> navigateToActivity(CompeticionActivity.class));
        btnResultados.setOnClickListener(v -> navigateToActivity(ResultadosActivity.class));
        btnEquipo.setOnClickListener(v -> navigateToActivity(EquipoActivity.class));
    }

    private void showErrorDialog(String s) {
        Toast.makeText(EquipoActivity.this, s, Toast.LENGTH_SHORT).show();
    }

    private void navigateToActivity(Class<?> activityClass) {
        Intent intent = new Intent(EquipoActivity.this, activityClass);
        intent.putExtra("id_usuario", idUsuario);
        startActivity(intent);
    }

    private class FetchEquipoTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            String idUsuario = params[0];
            String result = "";

            try {
                URL url = new URL("http://172.20.10.2/MANAGERFOOT/Equipo.php");
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
            progressBar.setVisibility(View.GONE);
            if (result == null) {
                showErrorDialog("Error en la conexión con el servidor.");
                return;
            }
            Log.d("EquipoActivity", "Response from server: " + result);
            try {
                JSONObject jsonObject = new JSONObject(result);
                String status = jsonObject.getString("status");

                if (status.equals("success")) {
                    JSONObject equipo = jsonObject.getJSONObject("equipo");

                    nombreEditText.setText(equipo.getString("nombre"));
                    entrenadorEditText.setText(equipo.getString("entrenador"));
                    competicionEditText.setText(equipo.getString("competicion"));
                    tipoTerrenoEditText.setText(equipo.getString("tipo_terreno"));
                    golesFavorEditText.setText(equipo.getString("goles_favor"));
                    golesContraEditText.setText(equipo.getString("goles_contra"));
                    ciudadEditText.setText(equipo.getString("ciudad"));

                } else {
                    String message = jsonObject.getString("message");
                    showErrorDialog(message);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                showErrorDialog("Error al procesar la respuesta del servidor.");
            }
        }

        private void showErrorDialog(String message) {
            Toast.makeText(EquipoActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    }
}