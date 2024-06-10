package com.example.tfgisma;

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

public class LoginActivity extends AppCompatActivity {

    private EditText usuarioEditText, contrasenaEditText;
    private Button loginButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usuarioEditText = findViewById(R.id.usuarioEditText);
        contrasenaEditText = findViewById(R.id.contrasenaEditText);
        loginButton = findViewById(R.id.loginButton);
        progressBar = findViewById(R.id.progressBar);

        loginButton.setOnClickListener(v -> {
            String usuario = usuarioEditText.getText().toString();
            String contrasena = contrasenaEditText.getText().toString();

            if (usuario.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            } else {
                new LoginTask().execute(usuario, contrasena);
            }
        });
    }

    private class LoginTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            String usuario = params[0];
            String contrasena = params[1];
            String result = "";

            try {
                URL url = new URL("http://172.20.10.2/MANAGERFOOT/Login.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                // Enviar parámetros de login
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write("usuario=" + URLEncoder.encode(usuario, "UTF-8") + "&contrasenia=" + URLEncoder.encode(contrasena, "UTF-8"));
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
                Log.d("LoginActivity", "Respuesta del servidor: " + result);
            } catch (Exception e) {
                Log.e("LoginActivity", "Error en doInBackground", e);
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
            try {
                JSONObject jsonObject = new JSONObject(result);
                String status = jsonObject.getString("status");

                if (status.equals("success")) {
                    String id_usuario = jsonObject.getString("id_usuario");
                    Intent intent = new Intent(LoginActivity.this, EquipoActivity.class);
                    intent.putExtra("id_usuario", id_usuario);
                    startActivity(intent);
                    finish();
                } else {
                    String message = jsonObject.getString("message");
                    showErrorDialog(message);
                }
            } catch (JSONException e) {
                Log.e("LoginActivity", "Error procesando JSON", e);
                showErrorDialog("Error al procesar la respuesta del servidor.");
            } catch (Exception e) {
                Log.e("LoginActivity", "Error desconocido", e);
                showErrorDialog("Error desconocido.");
            }
        }

        private void showErrorDialog(String message) {
            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    }
}