package pe.edu.paijan.comoencasa;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ReportesActivity extends BaseActivity {
    private EditText campoDesde;
    private EditText campoHasta;
    private LinearLayout resultados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitulo("Reportes");

        if (!puedeVender()) {
            agregar(mensaje("Tu rol no tiene acceso a los reportes."));
            return;
        }

        LinearLayout filtros = Ui.tarjeta(this);
        filtros.addView(Ui.etiqueta(this, "Periodo"));

        LinearLayout filaFechas = Ui.fila(this);
        campoDesde = Ui.campo(this, "yyyy-mm-dd");
        campoDesde.setSingleLine(true);
        campoDesde.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        campoDesde.setText(Formato.primerDiaMes());
        filaFechas.addView(campoDesde);

        campoHasta = Ui.campo(this, "yyyy-mm-dd");
        campoHasta.setSingleLine(true);
        LinearLayout.LayoutParams lpHasta = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lpHasta.leftMargin = Ui.dp(this, 8);
        campoHasta.setLayoutParams(lpHasta);
        campoHasta.setText(Formato.hoy());
        filaFechas.addView(campoHasta);
        LinearLayout.LayoutParams lpFila = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpFila.topMargin = Ui.dp(this, 6);
        filaFechas.setLayoutParams(lpFila);
        filtros.addView(filaFechas);

        LinearLayout accesos = Ui.fila(this);
        Button hoy = Ui.botonSecundario(this, "Hoy");
        LinearLayout.LayoutParams lpHoy = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lpHoy.rightMargin = Ui.dp(this, 4);
        hoy.setLayoutParams(lpHoy);
        hoy.setOnClickListener(v -> {
            campoDesde.setText(Formato.hoy());
            campoHasta.setText(Formato.hoy());
            consultar();
        });
        accesos.addView(hoy);

        Button mes = Ui.botonSecundario(this, "Este mes");
        LinearLayout.LayoutParams lpMes = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lpMes.leftMargin = Ui.dp(this, 4);
        lpMes.rightMargin = Ui.dp(this, 4);
        mes.setLayoutParams(lpMes);
        mes.setOnClickListener(v -> {
            campoDesde.setText(Formato.primerDiaMes());
            campoHasta.setText(Formato.hoy());
            consultar();
        });
        accesos.addView(mes);

        Button boton = Ui.botonPrimario(this, "Consultar");
        LinearLayout.LayoutParams lpBoton = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lpBoton.leftMargin = Ui.dp(this, 4);
        boton.setLayoutParams(lpBoton);
        boton.setOnClickListener(v -> consultar());
        accesos.addView(boton);

        LinearLayout.LayoutParams lpAccesos = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpAccesos.topMargin = Ui.dp(this, 8);
        accesos.setLayoutParams(lpAccesos);
        filtros.addView(accesos);
        agregar(filtros);

        resultados = new LinearLayout(this);
        resultados.setOrientation(LinearLayout.VERTICAL);
        agregar(resultados);

        consultar();
    }

    private void consultar() {
        if (resultados == null) return;
        final String desde = campoDesde.getText().toString().trim();
        final String hasta = campoHasta.getText().toString().trim();
        if (desde.isEmpty() || hasta.isEmpty()) {
            Ui.toast(this, "Completa las fechas del periodo.");
            return;
        }

        resultados.removeAllViews();
        resultados.addView(mensaje("Calculando reporte..."));

        Map<String, String> parametros = new HashMap<>();
        parametros.put("desde", desde);
        parametros.put("hasta", hasta);

        Api.get(this, "reportes", parametros, new Api.Callback() {
            @Override
            public void onSuccess(Object data) {
                resultados.removeAllViews();
                if (!(data instanceof JSONObject)) {
                    resultados.addView(mensaje("Respuesta inesperada del servidor."));
                    return;
                }
                pintar((JSONObject) data);
            }

            @Override
            public void onError(String mensaje) {
                resultados.removeAllViews();
                resultados.addView(mensaje(mensaje));
            }
        });
    }

    private void pintar(JSONObject reporte) {
        JSONObject resumen = reporte.optJSONObject("resumen");
        if (resumen == null) {
            resultados.addView(mensaje("El reporte no contiene resumen."));
            return;
        }

        LinearLayout tarjeta = Ui.tarjeta(this);
        tarjeta.addView(Ui.etiqueta(this, "Resumen del periodo"));
        TextView periodo = Ui.texto(this,
                Formato.fecha(desdeSeguro(reporte)) + " al " + Formato.fecha(hastaSeguro(reporte)),
                14f, Ui.TEXTO, true);
        periodo.setPadding(0, Ui.dp(this, 4), 0, Ui.dp(this, 8));
        tarjeta.addView(periodo);

        tarjeta.addView(dato("Ventas registradas", String.valueOf(Formato.ent(resumen, "ventas_cantidad"))));
        tarjeta.addView(dato("Total vendido", Formato.dinero(Formato.num(resumen, "ventas_total"))));
        tarjeta.addView(dato("Platos vendidos", String.valueOf(Formato.ent(resumen, "platos_vendidos"))));
        tarjeta.addView(dato("Descuentos", Formato.dinero(Formato.num(resumen, "descuentos"))));
        tarjeta.addView(separador());
        tarjeta.addView(dato("Ingresos cobrados", Formato.dinero(Formato.num(resumen, "ingresos"))));
        tarjeta.addView(dato("Efectivo", Formato.dinero(Formato.num(resumen, "efectivo"))));
        tarjeta.addView(dato("Yape", Formato.dinero(Formato.num(resumen, "yape"))));
        tarjeta.addView(dato("Tarjeta", Formato.dinero(Formato.num(resumen, "tarjeta"))));
        tarjeta.addView(separador());
        tarjeta.addView(dato("Gastos", Formato.dinero(Formato.num(resumen, "gastos"))));
        tarjeta.addView(dato("Pagos al personal", Formato.dinero(Formato.num(resumen, "pagos_personal"))));
        tarjeta.addView(dato("Compras", Formato.dinero(Formato.num(resumen, "compras"))));
        tarjeta.addView(separador());
        double ganancia = Formato.num(resumen, "ganancia");
        tarjeta.addView(dato("Ganancia", Formato.dinero(ganancia),
                ganancia >= 0 ? Ui.VERDE : Ui.ROJO_CLARO));
        double neto = Formato.num(resumen, "resultado_neto");
        tarjeta.addView(dato("Resultado neto", Formato.dinero(neto),
                neto >= 0 ? Ui.VERDE : Ui.ROJO_CLARO));
        resultados.addView(tarjeta);

        LinearLayout top = Ui.tarjeta(this);
        top.addView(Ui.etiqueta(this, "Productos mas vendidos"));
        JSONArray productos = reporte.optJSONArray("top_productos");
        if (productos == null || productos.length() == 0) {
            top.addView(mensaje("Sin ventas en el periodo."));
        } else {
            for (int i = 0; i < productos.length() && i < 10; i++) {
                JSONObject producto = productos.optJSONObject(i);
                if (producto == null) continue;
                top.addView(dato(Formato.txt(producto, "nombre"),
                        Formato.numero(Formato.num(producto, "cantidad")) + " und \u00b7 "
                                + Formato.dinero(Formato.num(producto, "total"))));
            }
        }
        resultados.addView(top);

        LinearLayout medios = Ui.tarjeta(this);
        medios.addView(Ui.etiqueta(this, "Ingresos por medio de pago"));
        JSONArray porMedio = reporte.optJSONArray("ingresos_por_medio");
        if (porMedio == null || porMedio.length() == 0) {
            medios.addView(mensaje("Sin cobros registrados."));
        } else {
            for (int i = 0; i < porMedio.length(); i++) {
                JSONObject medio = porMedio.optJSONObject(i);
                if (medio == null) continue;
                medios.addView(dato(Formato.medio(Formato.txt(medio, "medio")),
                        Formato.ent(medio, "cantidad") + " pagos \u00b7 "
                                + Formato.dinero(Formato.num(medio, "total"))));
            }
        }
        resultados.addView(medios);

        LinearLayout dias = Ui.tarjeta(this);
        dias.addView(Ui.etiqueta(this, "Ventas por dia"));
        JSONArray porDia = reporte.optJSONArray("ventas_por_dia");
        if (porDia == null || porDia.length() == 0) {
            dias.addView(mensaje("Sin ventas en el periodo."));
        } else {
            for (int i = 0; i < porDia.length(); i++) {
                JSONObject dia = porDia.optJSONObject(i);
                if (dia == null) continue;
                dias.addView(dato(Formato.fecha(Formato.txt(dia, "fecha")),
                        Formato.ent(dia, "cantidad") + " ventas \u00b7 "
                                + Formato.dinero(Formato.num(dia, "total"))));
            }
        }
        resultados.addView(dias);
    }

    private String desdeSeguro(JSONObject reporte) {
        String valor = Formato.txt(reporte, "desde");
        return valor.isEmpty() ? campoDesde.getText().toString().trim() : valor;
    }

    private String hastaSeguro(JSONObject reporte) {
        String valor = Formato.txt(reporte, "hasta");
        return valor.isEmpty() ? campoHasta.getText().toString().trim() : valor;
    }

    private LinearLayout dato(String etiqueta, String valor) {
        return dato(etiqueta, valor, Ui.TEXTO);
    }

    private LinearLayout dato(String etiqueta, String valor, int colorValor) {
        LinearLayout fila = Ui.fila(this);
        fila.setPadding(0, Ui.dp(this, 3), 0, Ui.dp(this, 3));
        TextView izquierda = Ui.texto(this, etiqueta, 13f, Ui.SUAVE, false);
        izquierda.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        fila.addView(izquierda);
        TextView derecha = Ui.texto(this, valor, 13f, colorValor, true);
        derecha.setGravity(Gravity.END);
        fila.addView(derecha);
        return fila;
    }

    private View separador() {
        View vista = new View(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 1);
        lp.topMargin = Ui.dp(this, 8);
        lp.bottomMargin = Ui.dp(this, 8);
        vista.setLayoutParams(lp);
        vista.setBackgroundColor(Ui.BORDE);
        return vista;
    }

    private TextView mensaje(String texto) {
        TextView vista = Ui.texto(this, texto, 14f, Ui.SUAVE, false);
        vista.setPadding(0, Ui.dp(this, 10), 0, Ui.dp(this, 10));
        return vista;
    }
}
