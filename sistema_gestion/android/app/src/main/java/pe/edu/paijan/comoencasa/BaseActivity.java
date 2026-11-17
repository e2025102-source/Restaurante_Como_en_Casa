package pe.edu.paijan.comoencasa;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONObject;

public abstract class BaseActivity extends Activity {
    protected LinearLayout contenido;
    private TextView tituloVista;
    private TextView botonAtras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Prefs.haySesion(this)) {
            irLogin();
            return;
        }

        getWindow().setStatusBarColor(Ui.ROJO_OSCURO);
        getWindow().setNavigationBarColor(Ui.CREMA_2);

        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        raiz.setBackgroundColor(Ui.CREMA);

        LinearLayout barra = new LinearLayout(this);
        barra.setOrientation(LinearLayout.HORIZONTAL);
        barra.setGravity(Gravity.CENTER_VERTICAL);
        barra.setBackgroundColor(Ui.ROJO);
        barra.setPadding(Ui.dp(this, 8), Ui.dp(this, 8), Ui.dp(this, 12), Ui.dp(this, 8));

        botonAtras = Ui.texto(this, "\u2039", 30f, Ui.BLANCO, false);
        botonAtras.setGravity(Gravity.CENTER);
        botonAtras.setPadding(Ui.dp(this, 8), 0, Ui.dp(this, 10), Ui.dp(this, 2));
        botonAtras.setOnClickListener(v -> finish());
        barra.addView(botonAtras);

        tituloVista = Ui.texto(this, "Como en Casa", 18f, Ui.BLANCO, true);
        LinearLayout.LayoutParams lpTitulo = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        tituloVista.setLayoutParams(lpTitulo);
        tituloVista.setSingleLine(true);
        barra.addView(tituloVista);

        TextView rol = Ui.texto(this, rolEtiqueta(), 11f, 0xFFFFE0B2, true);
        rol.setPadding(Ui.dp(this, 8), 0, Ui.dp(this, 10), 0);
        barra.addView(rol);

        TextView salir = Ui.texto(this, "Salir", 13f, Ui.BLANCO, true);
        salir.setPadding(Ui.dp(this, 8), Ui.dp(this, 8), Ui.dp(this, 6), Ui.dp(this, 8));
        salir.setOnClickListener(v -> confirmarSalida());
        barra.addView(salir);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        contenido = new LinearLayout(this);
        contenido.setOrientation(LinearLayout.VERTICAL);
        contenido.setPadding(Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 28));
        scroll.addView(contenido, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        raiz.addView(barra);
        raiz.addView(scroll);
        raiz.setOnApplyWindowInsetsListener((vista, insets) -> {
            int arriba = insets.getSystemWindowInsetTop();
            int abajo = insets.getSystemWindowInsetBottom();
            barra.setPadding(Ui.dp(this, 8), Ui.dp(this, 8) + arriba, Ui.dp(this, 12), Ui.dp(this, 8));
            contenido.setPadding(Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16),
                    Ui.dp(this, 28) + abajo);
            return insets;
        });
        setContentView(raiz);
    }

    protected void setTitulo(String titulo) {
        if (tituloVista != null) {
            tituloVista.setText(titulo);
        }
    }

    protected void ocultarAtras() {
        if (botonAtras != null) {
            botonAtras.setVisibility(View.GONE);
        }
    }

    protected void agregar(View vista) {
        contenido.addView(vista);
    }

    protected void limpiar() {
        contenido.removeAllViews();
    }

    protected void mostrarCargando(String mensaje) {
        limpiar();
        TextView carga = Ui.texto(this, mensaje, 15f, Ui.SUAVE, false);
        carga.setGravity(Gravity.CENTER);
        carga.setPadding(0, Ui.dp(this, 30), 0, Ui.dp(this, 30));
        agregar(carga);
    }

    protected void mostrarError(String mensaje, Runnable reintentar) {
        limpiar();
        LinearLayout tarjeta = Ui.tarjeta(this);
        TextView titulo = Ui.texto(this, "No se pudo cargar la informacion", 16f, Ui.ROJO_CLARO, true);
        tarjeta.addView(titulo);
        TextView detalle = Ui.texto(this, mensaje, 14f, Ui.SUAVE, false);
        detalle.setPadding(0, Ui.dp(this, 6), 0, Ui.dp(this, 10));
        tarjeta.addView(detalle);
        if (reintentar != null) {
            android.widget.Button boton = Ui.botonPrimario(this, "Reintentar");
            boton.setOnClickListener(v -> reintentar.run());
            tarjeta.addView(boton);
        }
        agregar(tarjeta);
    }

    protected boolean esAdmin() {
        return "administrador".equals(Prefs.rol(this));
    }

    protected boolean esCajero() {
        return "cajero".equals(Prefs.rol(this));
    }

    protected boolean esCocina() {
        return "cocina".equals(Prefs.rol(this));
    }

    protected boolean esInventario() {
        return "inventario".equals(Prefs.rol(this));
    }

    protected boolean puedeVender() {
        return esAdmin() || esCajero();
    }

    protected boolean puedeInventariar() {
        return esAdmin() || esInventario();
    }

    protected String rolEtiqueta() {
        String rol = Prefs.rol(this);
        if ("administrador".equals(rol)) return "ADMIN";
        if ("cajero".equals(rol)) return "CAJA";
        if ("cocina".equals(rol)) return "COCINA";
        if ("inventario".equals(rol)) return "ALMACEN";
        return rol.toUpperCase();
    }

    protected void ir(Class<?> destino) {
        startActivity(new Intent(this, destino));
    }

    protected void alerta(String titulo, String mensaje) {
        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton("Entendido", null)
                .show();
    }

    protected void confirmarSalida() {
        new AlertDialog.Builder(this)
                .setTitle("Cerrar sesion")
                .setMessage("\u00bfDeseas salir de la aplicacion?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salir", (dialogo, opcion) -> cerrarSesion())
                .show();
    }

    protected void cerrarSesion() {
        Api.post(this, "logout", new JSONObject(), new Api.Callback() {
            @Override
            public void onSuccess(Object data) {
                terminarSesion();
            }

            @Override
            public void onError(String mensaje) {
                terminarSesion();
            }
        });
    }

    protected void terminarSesion() {
        Prefs.limpiarSesion(this);
        irLogin();
    }

    protected void sesionExpirada(String mensaje) {
        new AlertDialog.Builder(this)
                .setTitle("Sesion finalizada")
                .setMessage(mensaje == null || mensaje.isEmpty()
                        ? "Tu sesion expiro. Vuelve a ingresar." : mensaje)
                .setCancelable(false)
                .setPositiveButton("Ingresar", (dialogo, opcion) -> {
                    Prefs.limpiarSesion(this);
                    irLogin();
                })
                .show();
    }

    protected void irLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    protected TextView tituloSeccion(String texto) {
        TextView vista = Ui.texto(this, texto, 20f, Ui.TEXTO, true);
        vista.setTypeface(Typeface.DEFAULT_BOLD);
        vista.setPadding(0, 0, 0, Ui.dp(this, 10));
        return vista;
    }
}
