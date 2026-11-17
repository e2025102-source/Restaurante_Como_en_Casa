package pe.edu.paijan.comoencasa;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends BaseActivity {
    private LinearLayout bloqueResumen;
    private TextView subtituloFecha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ocultarAtras();
        setTitulo("Panel general");

        LinearLayout saludo = Ui.tarjeta(this);
        TextView hola = Ui.texto(this,
                "Hola, " + Prefs.nombres(this), 19f, Ui.TEXTO, true);
        saludo.addView(hola);
        subtituloFecha = Ui.texto(this,
                rolLargo() + " \u00b7 " + fechaHoy(), 13f, Ui.SUAVE, false);
        subtituloFecha.setPadding(0, Ui.dp(this, 4), 0, 0);
        saludo.addView(subtituloFecha);
        agregar(saludo);

        bloqueResumen = new LinearLayout(this);
        bloqueResumen.setOrientation(LinearLayout.VERTICAL);
        agregar(bloqueResumen);

        agregar(tituloSeccion("Modulos"));
        agregar(grillaModulos());

        Button actualizar = Ui.botonSecundario(this, "Actualizar panel");
        actualizar.setOnClickListener(v -> cargarPanel());
        agregar(actualizar);

        cargarPanel();
    }

    private String rolLargo() {
        String rol = Prefs.rol(this);
        if ("administrador".equals(rol)) return "Administrador";
        if ("cajero".equals(rol)) return "Cajero";
        if ("cocina".equals(rol)) return "Cocina";
        if ("inventario".equals(rol)) return "Inventario";
        return rol;
    }

    private String fechaHoy() {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.US).format(new Date());
    }

    private LinearLayout grillaModulos() {
        LinearLayout contenedor = Ui.tarjeta(this);
        contenedor.setPadding(Ui.dp(this, 12), Ui.dp(this, 12), Ui.dp(this, 12), Ui.dp(this, 12));

        contenedor.addView(filaBotones(
                new String[]{"Ventas", "Nueva venta"},
                puedeVender(),
                VentasActivity.class,
                NuevaVentaActivity.class));

        contenedor.addView(Ui.espacio(this, 8));
        contenedor.addView(filaBotones(
                new String[]{"Productos", "Inventario"},
                true,
                ProductosActivity.class,
                InventarioActivity.class));

        if (puedeVender()) {
            contenedor.addView(Ui.espacio(this, 8));
            contenedor.addView(filaBotones(
                    new String[]{"Clientes", "Reportes"},
                    true,
                    ClientesActivity.class,
                    ReportesActivity.class));
        }

        return contenedor;
    }

    private LinearLayout filaBotones(String[] etiquetas, boolean segundaActiva,
                                     Class<?> primera, Class<?> segunda) {
        LinearLayout fila = Ui.fila(this);

        Button boton1 = Ui.botonPrimario(this, etiquetas[0]);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp1.rightMargin = Ui.dp(this, 6);
        boton1.setLayoutParams(lp1);
        boton1.setOnClickListener(v -> ir(primera));
        fila.addView(boton1);

        Button boton2 = Ui.botonNaranja(this, etiquetas[1]);
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp2.leftMargin = Ui.dp(this, 6);
        boton2.setLayoutParams(lp2);
        if (segundaActiva) {
            boton2.setOnClickListener(v -> ir(segunda));
        } else {
            boton2.setEnabled(false);
            boton2.setAlpha(0.45f);
        }
        fila.addView(boton2);

        return fila;
    }

    private void cargarPanel() {
        bloqueResumen.removeAllViews();
        LinearLayout carga = Ui.tarjeta(this);
        TextView texto = Ui.texto(this, "Cargando resumen del dia...", 14f, Ui.SUAVE, false);
        carga.addView(texto);
        bloqueResumen.addView(carga);

        Api.get(this, "dashboard", null, new Api.Callback() {
            @Override
            public void onSuccess(Object data) {
                bloqueResumen.removeAllViews();
                if (!(data instanceof JSONObject)) {
                    bloqueResumen.addView(mensajeError("Respuesta inesperada del servidor."));
                    return;
                }
                pintarResumen((JSONObject) data);
            }

            @Override
            public void onError(String mensaje) {
                bloqueResumen.removeAllViews();
                bloqueResumen.addView(mensajeError(mensaje));
            }
        });
    }

    private void pintarResumen(JSONObject datos) {
        JSONObject ventas = datos.optJSONObject("ventas_hoy");
        int cantidad = ventas == null ? 0 : ventas.optInt("cantidad", 0);
        double totalVentas = ventas == null ? 0 : ventas.optDouble("total", 0);
        double ingresos = datos.optDouble("ingresos_hoy", 0);
        double ganancia = datos.optDouble("ganancia_hoy", 0);
        JSONArray stockBajo = datos.optJSONArray("stock_bajo");
        int bajos = stockBajo == null ? 0 : stockBajo.length();

        LinearLayout fila1 = Ui.fila(this);
        fila1.addView(tarjetaResumen("Ventas de hoy", cantidad + " pedidos", Ui.ROJO));
        fila1.addView(tarjetaResumen("Ingresos", dinero(ingresos), Ui.NARANJA));
        bloqueResumen.addView(fila1);
        bloqueResumen.addView(Ui.espacio(this, 10));

        LinearLayout fila2 = Ui.fila(this);
        fila2.addView(tarjetaResumen("Ganancia", dinero(ganancia),
                ganancia >= 0 ? Ui.VERDE : Ui.ROJO_CLARO));
        fila2.addView(tarjetaResumen("Stock bajo", bajos + " insumos",
                bajos > 0 ? Ui.ROJO_CLARO : Ui.VERDE));
        bloqueResumen.addView(fila2);

        LinearLayout total = Ui.tarjeta(this);
        total.addView(Ui.etiqueta(this, "Ventas del dia"));
        TextView valor = Ui.texto(this, dinero(totalVentas), 24f, Ui.TEXTO, true);
        total.addView(valor);
        bloqueResumen.addView(total);

        JSONArray recientes = datos.optJSONArray("ventas_recientes");
        if (recientes != null && recientes.length() > 0) {
            LinearLayout tarjeta = Ui.tarjeta(this);
            tarjeta.addView(Ui.etiqueta(this, "Ultimas ventas"));
            for (int i = 0; i < recientes.length() && i < 6; i++) {
                JSONObject venta = recientes.optJSONObject(i);
                if (venta == null) continue;
                LinearLayout fila = Ui.fila(this);
                LinearLayout izquierda = new LinearLayout(this);
                izquierda.setOrientation(LinearLayout.VERTICAL);
                izquierda.setLayoutParams(new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                TextView numero = Ui.texto(this, venta.optString("numero", "Venta"),
                        14f, Ui.TEXTO, true);
                TextView cliente = Ui.texto(this, venta.optString("cliente", "Cliente"),
                        12f, Ui.SUAVE, false);
                izquierda.addView(numero);
                izquierda.addView(cliente);
                fila.addView(izquierda);

                LinearLayout derecha = new LinearLayout(this);
                derecha.setOrientation(LinearLayout.VERTICAL);
                derecha.setGravity(Gravity.END);
                TextView monto = Ui.texto(this, dinero(venta.optDouble("total", 0)),
                        14f, Ui.TEXTO, true);
                monto.setGravity(Gravity.END);
                TextView estado = Ui.texto(this, estadoLegible(venta.optString("estado", "")),
                        11f, Ui.NARANJA, true);
                estado.setGravity(Gravity.END);
                derecha.addView(monto);
                derecha.addView(estado);
                fila.addView(derecha);

                tarjeta.addView(fila);
                TextView separador = new TextView(this);
                separador.setHeight(Ui.dp(this, 8));
                tarjeta.addView(separador);
            }
            bloqueResumen.addView(tarjeta);
        }
    }

    private LinearLayout tarjetaResumen(String etiqueta, String valor, int color) {
        LinearLayout tarjeta = Ui.tarjeta(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp.rightMargin = Ui.dp(this, 6);
        tarjeta.setLayoutParams(lp);
        tarjeta.setPadding(Ui.dp(this, 14), Ui.dp(this, 12), Ui.dp(this, 14), Ui.dp(this, 12));

        TextView etiquetaVista = Ui.etiqueta(this, etiqueta);
        tarjeta.addView(etiquetaVista);
        TextView valorVista = Ui.texto(this, valor, 19f, color, true);
        valorVista.setTypeface(Typeface.DEFAULT_BOLD);
        valorVista.setPadding(0, Ui.dp(this, 6), 0, 0);
        tarjeta.addView(valorVista);
        return tarjeta;
    }

    private TextView mensajeError(String mensaje) {
        TextView vista = Ui.texto(this, mensaje, 14f, Ui.ROJO_CLARO, true);
        vista.setPadding(0, Ui.dp(this, 10), 0, Ui.dp(this, 10));
        return vista;
    }

    static String estadoLegible(String estado) {
        return Formato.estado(estado);
    }

    static String dinero(double valor) {
        return Formato.dinero(valor);
    }
}
