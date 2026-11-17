package pe.edu.paijan.comoencasa;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Api {
    public interface Callback {
        void onSuccess(Object data);

        void onError(String mensaje);
    }

    private static final ExecutorService POOL = Executors.newFixedThreadPool(3);
    private static final Handler PRINCIPAL = new Handler(Looper.getMainLooper());

    private Api() {
    }

    public static void get(Context contexto, String accion, Map<String, String> parametros, Callback callback) {
        ejecutar(contexto.getApplicationContext(), accion, "GET", parametros, null, callback);
    }

    public static void post(Context contexto, String accion, JSONObject cuerpo, Callback callback) {
        ejecutar(contexto.getApplicationContext(), accion, "POST", null, cuerpo, callback);
    }

    private static void ejecutar(Context contexto, String accion, String metodo,
                                 Map<String, String> parametros, JSONObject cuerpo, Callback callback) {
        POOL.execute(() -> {
            try {
                Object datos = solicitar(contexto, accion, metodo, parametros, cuerpo);
                PRINCIPAL.post(() -> callback.onSuccess(datos));
            } catch (Exception error) {
                String mensaje = error.getMessage();
                if (mensaje == null || mensaje.trim().isEmpty()) {
                    mensaje = "No se pudo conectar con el servidor.";
                }
                String finalMensaje = mensaje;
                PRINCIPAL.post(() -> callback.onError(finalMensaje));
            }
        });
    }

    private static Object solicitar(Context contexto, String accion, String metodo,
                                    Map<String, String> parametros, JSONObject cuerpo) throws Exception {
        String base = Prefs.baseUrl(contexto);
        StringBuilder url = new StringBuilder(base);
        if (base.contains("?")) {
            url.append('&');
        } else {
            url.append('?');
        }
        url.append("action=").append(URLEncoder.encode(accion, "UTF-8"));
        if (parametros != null) {
            for (Map.Entry<String, String> entrada : parametros.entrySet()) {
                String valor = entrada.getValue();
                if (valor != null && !valor.isEmpty()) {
                    url.append('&').append(URLEncoder.encode(entrada.getKey(), "UTF-8"))
                            .append('=').append(URLEncoder.encode(valor, "UTF-8"));
                }
            }
        }

        HttpURLConnection conexion = (HttpURLConnection) new URL(url.toString()).openConnection();
        conexion.setConnectTimeout(15000);
        conexion.setReadTimeout(25000);
        conexion.setRequestMethod(metodo);
        conexion.setRequestProperty("Accept", "application/json");

        String token = Prefs.token(contexto);
        if (token != null && !token.isEmpty()) {
            conexion.setRequestProperty("Authorization", "Bearer " + token);
        }

        if ("POST".equals(metodo)) {
            conexion.setDoOutput(true);
            conexion.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            byte[] salida = (cuerpo == null ? new JSONObject() : cuerpo).toString().getBytes(StandardCharsets.UTF_8);
            conexion.setFixedLengthStreamingMode(salida.length);
            try (OutputStream os = conexion.getOutputStream()) {
                os.write(salida);
            }
        }

        int codigo = conexion.getResponseCode();
        InputStream flujo = codigo >= 200 && codigo < 400 ? conexion.getInputStream() : conexion.getErrorStream();
        String texto = leer(flujo);

        JSONObject raiz;
        try {
            raiz = new JSONObject(texto);
        } catch (Exception e) {
            throw new IOException("Respuesta no valida del servidor (HTTP " + codigo + ").");
        }

        if (!raiz.optBoolean("ok", false)) {
            String mensaje = raiz.optString("error", "Error del servidor.");
            if (codigo == 401) {
                Prefs.limpiarSesion(contexto);
            }
            throw new IOException(mensaje);
        }

        conexion.disconnect();
        return raiz.opt("data");
    }

    private static String leer(InputStream flujo) throws IOException {
        if (flujo == null) {
            return "";
        }
        StringBuilder texto = new StringBuilder();
        try (BufferedReader lector = new BufferedReader(new InputStreamReader(flujo, StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                texto.append(linea);
            }
        }
        return texto.toString();
    }
}
