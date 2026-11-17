package pe.edu.paijan.comoencasa;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class VentasActivity extends BaseActivity {
    private static final String[] FILTROS = {
            "Todas", "registrada", "en_preparacion", "lista",
            "entregada", "pagada", "cerrada", "anulada"
    };
    private static final String[] FILTROS_TEXTO = {
            "Todas", "Registradas", "En preparacion", "Listas",
            "Entregadas", "Pagadas", "Cerradas", "Anuladas"
    };
    private static final String[] MEDIOS = {"efectivo", "yape", "tarjeta"};
    private static final String[] MEDIOS_TEXTO = {"Efectivo", "Yape", "Tarjeta"};

    private Spinner filtroEstado;
    private LinearLayout lista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitulo("Ventas");

        LinearLayout barra = Ui.fila(this);
        filtroEstado = spinner(FILTROS_TEXTO);
        LinearLayout.LayoutParams lpSpinner = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lpSpinner.rightMargin = Ui.dp(this, 8);
        filtroEstado.setLayoutParams(lpSpinner);
        filtroEstado.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> padre, View vista, int posicion, long id) {
                cargar();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> padre) {
            }
        });
        barra.addView(filtroEstado);

        Button nueva = Ui.botonPrimario(this, "+ Venta");
        nueva.setOnClickListener(v -> ir(NuevaVentaActivity.class));
        if (!puedeVender()) {
            nueva.setVisibility(View.GONE);
        }
        barra.addView(nueva);
        agregar(barra);

        lista = new LinearLayout(this);
        lista.setOrientation(LinearLayout.VERTICAL);
        lista.setPadding(0, Ui.dp(this, 12), 0, 0);
        agregar(lista);

        cargar();
    }

    private void cargar() {
        if (lista == null) return;
        lista.removeAllViews();
        TextView carga = Ui.texto(this, "Cargando ventas...", 14f, Ui.SUAVE, false);
        carga.setPadding(0, Ui.dp(this, 16), 0, Ui.dp(this, 16));
        lista.addView(carga);

        Map<String, String> parametros = new HashMap<>();
        int posicion = filtroEstado == null ? 0 : filtroEstado.getSelectedItemPosition();
        if (posicion > 0) {
            parametros.put("estado", FILTROS[posicion]);
        }

        Api.get(this, "ventas", parametros, new Api.Callback() {
            @Override
            public void onSuccess(Object data) {
                lista.removeAllViews();
                if (!(data instanceof JSONArray)) {
                    lista.addView(mensaje("Respuesta inesperada del servidor."));
                    return;
                }
                JSONArray ventas = (JSONArray) data;
                if (ventas.length() == 0) {
                    lista.addView(mensaje("No hay ventas para el filtro seleccionado."));
                    return;
                }
                for (int i = 0; i < ventas.length(); i++) {
                    JSONObject venta = ventas.optJSONObject(i);
                    if (venta != null) {
                        lista.addView(tarjetaVenta(venta));
                    }
                }
            }

            @Override
            public void onError(String mensaje) {
                lista.removeAllViews();
                lista.addView(mensaje(mensaje));
                Button reintentar = Ui.botonSecundario(VentasActivity.this, "Reintentar");
                reintentar.setOnClickListener(v -> cargar());
                lista.addView(reintentar);
            }
        });
    }

    private LinearLayout tarjetaVenta(JSONObject venta) {
        LinearLayout tarjeta = Ui.tarjeta(this);
        final int id = venta.optInt("id", 0);

        LinearLayout fila = Ui.fila(this);
        LinearLayout izquierda = new LinearLayout(this);
        izquierda.setOrientation(LinearLayout.VERTICAL);
        izquierda.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView numero = Ui.texto(this, Formato.txt(venta, "numero", "Venta"), 16f, Ui.TEXTO, true);
        izquierda.addView(numero);
        TextView cliente = Ui.texto(this, Formato.txt(venta, "cliente", "Consumidor final"),
                13f, Ui.SUAVE, false);
        izquierda.addView(cliente);
        TextView fecha = Ui.texto(this, Formato.fechaHora(Formato.txt(venta, "fecha")),
                11f, Ui.SUAVE, false);
        izquierda.addView(fecha);
        fila.addView(izquierda);

        LinearLayout derecha = new LinearLayout(this);
        derecha.setOrientation(LinearLayout.VERTICAL);
        derecha.setGravity(Gravity.END);
        TextView total = Ui.texto(this, Formato.dinero(Formato.num(venta, "total")),
                16f, Ui.TEXTO, true);
        total.setGravity(Gravity.END);
        derecha.addView(total);
        TextView chip = chipEstado(Formato.txt(venta, "estado"));
        LinearLayout.LayoutParams lpChip = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpChip.topMargin = Ui.dp(this, 4);
        chip.setLayoutParams(lpChip);
        derecha.addView(chip);
        fila.addView(derecha);
        tarjeta.addView(fila);

        double pagado = Formato.num(venta, "pagado");
        double saldo = Math.max(0, Formato.num(venta, "total") - pagado);
        String detalle = Formato.ent(venta, "items") + " items \u00b7 "
                + Formato.tipoServicio(Formato.txt(venta, "tipo_servicio"))
                + (saldo > 0.001 ? " \u00b7 Saldo " + Formato.dinero(saldo) : " \u00b7 Pagada");
        TextView resumen = Ui.texto(this, detalle, 12f, Ui.SUAVE, false);
        resumen.setPadding(0, Ui.dp(this, 8), 0, 0);
        tarjeta.addView(resumen);

        tarjeta.setOnClickListener(v -> abrirVenta(id));
        return tarjeta;
    }

    private void abrirVenta(int id) {
        if (id <= 0) return;
        Map<String, String> parametros = new HashMap<>();
        parametros.put("id", String.valueOf(id));
        Api.get(this, "venta", parametros, new Api.Callback() {
            @Override
            public void onSuccess(Object data) {
                if (data instanceof JSONObject) {
                    dialogoVenta((JSONObject) data);
                } else {
                    alerta("Venta", "No se pudo leer la venta.");
                }
            }

            @Override
            public void onError(String mensaje) {
                alerta("No se pudo abrir la venta", mensaje);
            }
        });
    }

    private void dialogoVenta(JSONObject venta) {
        final int id = venta.optInt("id", 0);
        final String estado = Formato.txt(venta, "estado");
        final double total = Formato.num(venta, "total");
        final double pagado = Formato.num(venta, "pagado");
        final double saldo = Math.max(0, total - pagado);

        ScrollView scroll = new ScrollView(this);
        LinearLayout cuerpo = new LinearLayout(this);
        cuerpo.setOrientation(LinearLayout.VERTICAL);
        cuerpo.setPadding(Ui.dp(this, 18), Ui.dp(this, 16), Ui.dp(this, 18), Ui.dp(this, 12));
        scroll.addView(cuerpo);

        LinearLayout encabezado = Ui.fila(this);
        TextView numero = Ui.texto(this, Formato.txt(venta, "numero", "Venta"), 19f, Ui.TEXTO, true);
        LinearLayout.LayoutParams lpNumero = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        numero.setLayoutParams(lpNumero);
        encabezado.addView(numero);
        encabezado.addView(chipEstado(estado));
        cuerpo.addView(encabezado);

        cuerpo.addView(dato("Cliente", Formato.txt(venta, "cliente", "Consumidor final")));
        cuerpo.addView(dato("Fecha", Formato.fechaHora(Formato.txt(venta, "fecha"))));
        String servicio = Formato.tipoServicio(Formato.txt(venta, "tipo_servicio"));
        String mesa = Formato.txt(venta, "mesa");
        cuerpo.addView(dato("Servicio", servicio + (mesa.isEmpty() ? "" : " \u00b7 Mesa " + mesa)));
        cuerpo.addView(dato("Atendido por", Formato.txt(venta, "usuario", "-")));
        String observacion = Formato.txt(venta, "observacion");
        if (!observacion.isEmpty()) {
            cuerpo.addView(dato("Observacion", observacion));
        }

        cuerpo.addView(separador());
        cuerpo.addView(Ui.etiqueta(this, "Productos"));
        JSONArray detalles = venta.optJSONArray("detalles");
        if (detalles != null) {
            for (int i = 0; i < detalles.length(); i++) {
                JSONObject detalle = detalles.optJSONObject(i);
                if (detalle == null) continue;
                String linea = Formato.num(detalle, "cantidad") == Math.rint(Formato.num(detalle, "cantidad"))
                        ? String.valueOf((int) Formato.num(detalle, "cantidad"))
                        : Formato.numero(Formato.num(detalle, "cantidad"));
                cuerpo.addView(dato(
                        linea + " x " + Formato.txt(detalle, "producto"),
                        Formato.dinero(Formato.num(detalle, "subtotal"))));
            }
        }

        cuerpo.addView(separador());
        cuerpo.addView(dato("Subtotal", Formato.dinero(Formato.num(venta, "subtotal"))));
        double descuento = Formato.num(venta, "descuento");
        if (descuento > 0) {
            cuerpo.addView(dato("Descuento", "- " + Formato.dinero(descuento)));
        }
        cuerpo.addView(dato("Total", Formato.dinero(total)));
        cuerpo.addView(dato("Pagado", Formato.dinero(pagado)));
        cuerpo.addView(dato("Saldo", Formato.dinero(saldo), saldo > 0.001 ? Ui.ROJO_CLARO : Ui.VERDE));

        JSONArray pagos = venta.optJSONArray("pagos");
        if (pagos != null && pagos.length() > 0) {
            cuerpo.addView(separador());
            cuerpo.addView(Ui.etiqueta(this, "Pagos"));
            for (int i = 0; i < pagos.length(); i++) {
                JSONObject pago = pagos.optJSONObject(i);
                if (pago == null) continue;
                cuerpo.addView(dato(Formato.medio(Formato.txt(pago, "medio")),
                        Formato.dinero(Formato.num(pago, "monto"))));
            }
        }

        final AlertDialog dialogo = new AlertDialog.Builder(this)
                .setView(scroll)
                .setNegativeButton("Cerrar", null)
                .create();

        boolean bloqueada = "anulada".equals(estado) || "cerrada".equals(estado);

        if (puedeVender() && saldo > 0.001 && !bloqueada) {
            Button pago = Ui.botonNaranja(this, "Registrar pago");
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.topMargin = Ui.dp(this, 10);
            pago.setLayoutParams(lp);
            pago.setOnClickListener(v -> dialogoPago(venta, dialogo));
            cuerpo.addView(pago);
        }

        String siguiente = siguienteEstado(estado);
        if (siguiente != null && !bloqueada) {
            boolean permitido = puedeVender()
                    || (esCocina() && ("registrada".equals(estado) || "en_preparacion".equals(estado)));
            if (permitido) {
                Button cambiar = Ui.botonPrimario(this, textoAccion(siguiente));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.topMargin = Ui.dp(this, 8);
                cambiar.setLayoutParams(lp);
                cambiar.setOnClickListener(v -> cambiarEstado(id, siguiente, dialogo));
                cuerpo.addView(cambiar);
            }
        }

        if (esAdmin() && !bloqueada) {
            Button anular = Ui.botonSecundario(this, "Anular venta");
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.topMargin = Ui.dp(this, 8);
            anular.setLayoutParams(lp);
            anular.setTextColor(Ui.ROJO_CLARO);
            anular.setOnClickListener(v -> confirmarAnulacion(id, dialogo));
            cuerpo.addView(anular);
        }

        dialogo.show();
    }

    private String siguienteEstado(String estado) {
        if ("registrada".equals(estado)) return "en_preparacion";
        if ("en_preparacion".equals(estado)) return "lista";
        if ("lista".equals(estado)) return "entregada";
        if ("entregada".equals(estado) || "pagada".equals(estado)) return "cerrada";
        return null;
    }

    private String textoAccion(String estado) {
        if ("en_preparacion".equals(estado)) return "Iniciar preparacion";
        if ("lista".equals(estado)) return "Marcar como lista";
        if ("entregada".equals(estado)) return "Marcar entregada";
        if ("cerrada".equals(estado)) return "Cerrar venta";
        return "Cambiar estado";
    }

    private void cambiarEstado(final int id, final String estado, final AlertDialog dialogo) {
        JSONObject cuerpo = new JSONObject();
        try {
            cuerpo.put("id", id);
            cuerpo.put("estado", estado);
        } catch (Exception ignorado) {
        }
        Api.post(this, "venta_estado", cuerpo, new Api.Callback() {
            @Override
            public void onSuccess(Object data) {
                dialogo.dismiss();
                String nuevo = data instanceof JSONObject
                        ? Formato.txt((JSONObject) data, "estado", estado) : estado;
                Ui.toast(VentasActivity.this, "Venta actualizada: " + Formato.estado(nuevo));
                cargar();
            }

            @Override
            public void onError(String mensaje) {
                alerta("No se pudo cambiar el estado", mensaje);
            }
        });
    }

    private void dialogoPago(final JSONObject venta, final AlertDialog padre) {
        final int id = venta.optInt("id", 0);
        final double saldo = Math.max(0, Formato.num(venta, "total") - Formato.num(venta, "pagado"));

        LinearLayout cuerpo = new LinearLayout(this);
        cuerpo.setOrientation(LinearLayout.VERTICAL);
        cuerpo.setPadding(Ui.dp(this, 18), Ui.dp(this, 14), Ui.dp(this, 18), Ui.dp(this, 8));

        cuerpo.addView(Ui.etiqueta(this, "Monto a cobrar"));
        final EditText monto = Ui.campoNumero(this, "0.00");
        monto.setText(String.format(java.util.Locale.US, "%.2f", saldo));
        cuerpo.addView(monto);

        cuerpo.addView(Ui.espacio(this, 10));
        cuerpo.addView(Ui.etiqueta(this, "Medio de pago"));
        final Spinner medio = spinner(MEDIOS_TEXTO);
        cuerpo.addView(medio);

        cuerpo.addView(Ui.espacio(this, 10));
        cuerpo.addView(Ui.etiqueta(this, "Referencia (opcional)"));
        final EditText referencia = Ui.campo(this, "Nro. operacion");
        referencia.setSingleLine(true);
        cuerpo.addView(referencia);

        TextView ayuda = Ui.texto(this, "Saldo pendiente: " + Formato.dinero(saldo),
                12f, Ui.SUAVE, false);
        ayuda.setPadding(0, Ui.dp(this, 8), 0, 0);
        cuerpo.addView(ayuda);

        new AlertDialog.Builder(this)
                .setTitle("Registrar pago")
                .setView(cuerpo)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Cobrar", (dialogo, opcion) -> {
                    double valor = Formato.parseMonto(monto.getText().toString());
                    if (valor <= 0) {
                        Ui.toast(this, "Ingresa un monto mayor a cero.");
                        return;
                    }
                    JSONObject datos = new JSONObject();
                    try {
                        datos.put("venta_id", id);
                        datos.put("monto", valor);
                        datos.put("medio", MEDIOS[medio.getSelectedItemPosition()]);
                        datos.put("referencia", referencia.getText().toString().trim());
                    } catch (Exception ignorado) {
                    }
                    padre.dismiss();
                    Api.post(this, "venta_pago", datos, new Api.Callback() {
                        @Override
                        public void onSuccess(Object data) {
                            double nuevoSaldo = 0;
                            if (data instanceof JSONObject) {
                                nuevoSaldo = Math.max(0,
                                        Formato.num((JSONObject) data, "total")
                                                - Formato.num((JSONObject) data, "pagado"));
                            }
                            Ui.toast(VentasActivity.this,
                                    nuevoSaldo > 0.001
                                            ? "Pago registrado. Saldo: " + Formato.dinero(nuevoSaldo)
                                            : "Venta pagada en su totalidad.");
                            cargar();
                        }

                        @Override
                        public void onError(String mensaje) {
                            alerta("No se pudo registrar el pago", mensaje);
                        }
                    });
                })
                .show();
    }

    private void confirmarAnulacion(final int id, final AlertDialog padre) {
        new AlertDialog.Builder(this)
                .setTitle("Anular venta")
                .setMessage("La venta quedara marcada como anulada. \u00bfContinuar?")
                .setNegativeButton("No", null)
                .setPositiveButton("Anular", (dialogo, opcion) -> {
                    JSONObject datos = new JSONObject();
                    try {
                        datos.put("id", id);
                    } catch (Exception ignorado) {
                    }
                    Api.post(this, "venta_anular", datos, new Api.Callback() {
                        @Override
                        public void onSuccess(Object data) {
                            padre.dismiss();
                            Ui.toast(VentasActivity.this, "Venta anulada.");
                            cargar();
                        }

                        @Override
                        public void onError(String mensaje) {
                            alerta("No se pudo anular", mensaje);
                        }
                    });
                })
                .show();
    }

    private TextView chipEstado(String estado) {
        TextView chip = Ui.texto(this, Formato.estado(estado), 11f, Ui.BLANCO, true);
        chip.setBackground(Ui.fondo(Formato.colorEstado(estado), Ui.dp(this, 20)));
        chip.setPadding(Ui.dp(this, 10), Ui.dp(this, 4), Ui.dp(this, 10), Ui.dp(this, 4));
        return chip;
    }

    private LinearLayout dato(String etiqueta, String valor) {
        return dato(etiqueta, valor, Ui.TEXTO);
    }

    private LinearLayout dato(String etiqueta, String valor, int colorValor) {
        LinearLayout fila = Ui.fila(this);
        TextView izquierda = Ui.texto(this, etiqueta, 13f, Ui.SUAVE, false);
        izquierda.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        fila.addView(izquierda);
        TextView derecha = Ui.texto(this, valor, 13f, colorValor, true);
        derecha.setGravity(Gravity.END);
        fila.addView(derecha);
        fila.setPadding(0, Ui.dp(this, 3), 0, Ui.dp(this, 3));
        return fila;
    }

    private View separador() {
        View vista = new View(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 1);
        lp.topMargin = Ui.dp(this, 10);
        lp.bottomMargin = Ui.dp(this, 10);
        vista.setLayoutParams(lp);
        vista.setBackgroundColor(Ui.BORDE);
        return vista;
    }

    private TextView mensaje(String texto) {
        TextView vista = Ui.texto(this, texto, 14f, Ui.SUAVE, false);
        vista.setPadding(0, Ui.dp(this, 10), 0, Ui.dp(this, 10));
        return vista;
    }

    private Spinner spinner(String[] valores) {
        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adaptador = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, valores);
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adaptador);
        spinner.setMinimumHeight(Ui.dp(this, 42));
        return spinner;
    }
}
