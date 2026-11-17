package pe.edu.paijan.comoencasa;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONObject;

public class LoginActivity extends Activity {
    private EditText campoUrl;
    private EditText campoUsuario;
    private EditText campoClave;
    private TextView textoError;
    private Button botonIngresar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Ui.ROJO_OSCURO);
        getWindow().setNavigationBarColor(Ui.CREMA_2);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Ui.CREMA);
        scroll.setFillViewport(true);

        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(raiz, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout cabecera = new LinearLayout(this);
        cabecera.setOrientation(LinearLayout.VERTICAL);
        cabecera.setBackgroundColor(Ui.ROJO);
        cabecera.setPadding(Ui.dp(this, 24), Ui.dp(this, 46), Ui.dp(this, 24), Ui.dp(this, 34));
        TextView marca = Ui.texto(this, "Como en Casa", 30f, Ui.BLANCO, true);
        marca.setTypeface(Typeface.DEFAULT_BOLD);
        TextView lema = Ui.texto(this, "Sistema de gestion del restaurante", 14f, 0xFFFFE0B2, false);
        lema.setPadding(0, Ui.dp(this, 6), 0, 0);
        cabecera.addView(marca);
        cabecera.addView(lema);
        raiz.addView(cabecera);

        LinearLayout cuerpo = new LinearLayout(this);
        cuerpo.setOrientation(LinearLayout.VERTICAL);
        cuerpo.setPadding(Ui.dp(this, 20), Ui.dp(this, 20), Ui.dp(this, 20), Ui.dp(this, 28));
        raiz.addView(cuerpo);

        LinearLayout tarjeta = Ui.tarjeta(this);
        tarjeta.setPadding(Ui.dp(this, 20), Ui.dp(this, 20), Ui.dp(this, 20), Ui.dp(this, 20));
        cuerpo.addView(tarjeta);

        TextView titulo = Ui.texto(this, "Iniciar sesion", 21f, Ui.TEXTO, true);
        tarjeta.addView(titulo);
        TextView pista = Ui.texto(this, "Ingresa con tu usuario del restaurante.", 13f, Ui.SUAVE, false);
        pista.setPadding(0, Ui.dp(this, 4), 0, Ui.dp(this, 16));
        tarjeta.addView(pista);

        tarjeta.addView(Ui.etiqueta(this, "Servidor de la API"));
        campoUrl = Ui.campo(this, "http://10.0.2.2/como_en_casa/api.php");
        campoUrl.setText(Prefs.baseUrl(this));
        campoUrl.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        campoUrl.setSingleLine(true);
        tarjeta.addView(campoUrl);

        tarjeta.addView(Ui.espacio(this, 12));
        tarjeta.addView(Ui.etiqueta(this, "Usuario"));
        campoUsuario = Ui.campo(this, "admin");
        campoUsuario.setSingleLine(true);
        tarjeta.addView(campoUsuario);

        tarjeta.addView(Ui.espacio(this, 12));
        tarjeta.addView(Ui.etiqueta(this, "Clave"));
        campoClave = Ui.campo(this, "Tu clave");
        campoClave.setSingleLine(true);
        campoClave.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        tarjeta.addView(campoClave);

        textoError = Ui.texto(this, "", 13f, Ui.ROJO_CLARO, true);
        textoError.setVisibility(TextView.GONE);
        textoError.setPadding(0, Ui.dp(this, 10), 0, 0);
        tarjeta.addView(textoError);

        tarjeta.addView(Ui.espacio(this, 16));
        botonIngresar = Ui.botonPrimario(this, "Ingresar");
        botonIngresar.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        botonIngresar.setOnClickListener(v -> ingresar());
        tarjeta.addView(botonIngresar);

        LinearLayout demo = Ui.tarjeta(this);
        TextView demoTexto = Ui.texto(this,
                "Cuentas demo:\nadmin / admin123\ncajero / cajero123\ncocina / cocina123\ninventario / inventario123",
                13f, Ui.SUAVE, false);
        demo.addView(demoTexto);
        cuerpo.addView(demo);

        TextView pie = Ui.texto(this, "Como en Casa \u00b7 Paita, Peru", 12f, Ui.SUAVE, false);
        pie.setGravity(Gravity.CENTER);
        pie.setPadding(0, Ui.dp(this, 10), 0, 0);
        cuerpo.addView(pie);

        scroll.setOnApplyWindowInsetsListener((vista, insets) -> {
            int arriba = insets.getSystemWindowInsetTop();
            int abajo = insets.getSystemWindowInsetBottom();
            cabecera.setPadding(Ui.dp(this, 24), Ui.dp(this, 46) + arriba,
                    Ui.dp(this, 24), Ui.dp(this, 34));
            cuerpo.setPadding(Ui.dp(this, 20), Ui.dp(this, 20),
                    Ui.dp(this, 20), Ui.dp(this, 28) + abajo);
            return insets;
        });

        setContentView(scroll);
    }

    private void ingresar() {
        final String url = campoUrl.getText().toString().trim();
        final String usuario = campoUsuario.getText().toString().trim();
        final String clave = campoClave.getText().toString();

        textoError.setVisibility(TextView.GONE);

        if (url.isEmpty()) {
            mostrarError("Escribe la direccion del servidor.");
            return;
        }
        if (usuario.isEmpty() || clave.isEmpty()) {
            mostrarError("Escribe tu usuario y tu clave.");
            return;
        }

        Prefs.guardarBaseUrl(this, url);
        botonIngresar.setEnabled(false);
        botonIngresar.setText("Ingresando...");

        JSONObject cuerpo = new JSONObject();
        try {
            cuerpo.put("usuario", usuario);
            cuerpo.put("clave", clave);
        } catch (Exception ignorado) {
            // No deberia ocurrir con claves simples.
        }

        Api.post(this, "login", cuerpo, new Api.Callback() {
            @Override
            public void onSuccess(Object data) {
                botonIngresar.setEnabled(true);
                botonIngresar.setText("Ingresar");
                if (!(data instanceof JSONObject)) {
                    mostrarError("Respuesta inesperada del servidor.");
                    return;
                }
                JSONObject respuesta = (JSONObject) data;
                String token = respuesta.optString("token", "");
                JSONObject usuarioJson = respuesta.optJSONObject("usuario");
                if (token.isEmpty() || usuarioJson == null) {
                    mostrarError("El servidor no devolvio una sesion valida.");
                    return;
                }
                Prefs.guardarSesion(
                        LoginActivity.this,
                        token,
                        usuarioJson.optString("nombres", usuario),
                        usuarioJson.optString("usuario", usuario),
                        usuarioJson.optString("rol", ""));
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onError(String mensaje) {
                botonIngresar.setEnabled(true);
                botonIngresar.setText("Ingresar");
                mostrarError(mensaje);
            }
        });
    }

    private void mostrarError(String mensaje) {
        textoError.setText(mensaje);
        textoError.setVisibility(TextView.VISIBLE);
    }
}
