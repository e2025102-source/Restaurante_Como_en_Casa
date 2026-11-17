package pe.edu.paijan.comoencasa;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class InventarioActivity extends BaseActivity {
    private static final String[] TIPOS = {"entrada", "salida"};
    private static final String[] TIPOS_TEXTO = {"Entrada", "Salida"};

    private LinearLayout lista;
    private EditText busqueda;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitulo("Inventario");

        LinearLayout barra = Ui.fila(this);
        busqueda = Ui.campo(this, "Buscar insumo...");
        busqueda.setSingleLine(true);
        busqueda.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        busqueda.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cargar();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        barra.addView(busqueda);

        if (puedeInventariar()) {
            Button nuevo = Ui.botonPrimario(this, "+ Insumo");
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.leftMargin = Ui.dp(this, 8);
            nuevo.setLayoutParams(lp);
            nuevo.setOnClickListener(v -> dialogoInsumo(null));
            barra.addView(nuevo);
        }
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
        lista.addView(mensaje("Cargando inventario..."));

        Map<String, String> parametros = new HashMap<>();
        String q = busqueda == null ? "" : busqueda.getText().toString().trim();
        if (!q.isEmpty()) parametros.put("q", q);

        Api.get(this, "insumos", parametros, new Api.Callback() {
            @Override
            public void onSuccess(Object data) {
                lista.removeAllViews();
                if (!(data instanceof JSONArray)) {
                    lista.addView(mensaje("Respuesta inesperada del servidor."));
                    return;
                }
                JSONArray insumos = (JSONArray) data;
                if (insumos.length() == 0) {
                    lista.addView(mensaje("No hay insumos registrados."));
                    return;
                }
                for (int i = 0; i < insumos.length(); i++) {
                    JSONObject insumo = insumos.optJSONObject(i);
                    if (insumo != null) {
                        lista.addView(tarjetaInsumo(insumo));
                    }
                }
            }

            @Override
            public void onError(String mensaje) {
                lista.removeAllViews();
                lista.addView(mensaje(mensaje));
            }
        });
    }

    private LinearLayout tarjetaInsumo(JSONObject insumo) {
        LinearLayout tarjeta = Ui.tarjeta(this);
        LinearLayout fila = Ui.fila(this);

        LinearLayout izquierda = new LinearLayout(this);
        izquierda.setOrientation(LinearLayout.VERTICAL);
        izquierda.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        izquierda.addView(Ui.texto(this, Formato.txt(insumo, "nombre", "Insumo"),
                15f, Ui.TEXTO, true));
        String stock = "Stock: " + Formato.numero(Formato.num(insumo, "stock_actual"))
                + " " + Formato.txt(insumo, "unidad", "unidad")
                + " \u00b7 Min: " + Formato.numero(Formato.num(insumo, "stock_minimo"));
        izquierda.addView(Ui.texto(this, stock, 12f, Ui.SUAVE, false));
        izquierda.addView(Ui.texto(this, "Costo: " + Formato.dinero(Formato.num(insumo, "costo"))
                + " por " + Formato.txt(insumo, "unidad", "unidad"), 12f, Ui.SUAVE, false));
        fila.addView(izquierda);

        boolean bajo = Formato.bool(insumo, "bajo_stock");
        TextView chip = Ui.texto(this, bajo ? "STOCK BAJO" : "OK", 11f, Ui.BLANCO, true);
        chip.setBackground(Ui.fondo(bajo ? Ui.ROJO_CLARO : Ui.VERDE, Ui.dp(this, 20)));
        chip.setPadding(Ui.dp(this, 10), Ui.dp(this, 4), Ui.dp(this, 10), Ui.dp(this, 4));
        chip.setGravity(Gravity.CENTER);
        fila.addView(chip);
        tarjeta.addView(fila);

        tarjeta.setOnClickListener(v -> {
            if (puedeInventariar()) {
                dialogoInsumo(insumo);
            } else {
                alerta(Formato.txt(insumo, "nombre", "Insumo"),
                        "Stock actual: " + Formato.numero(Formato.num(insumo, "stock_actual"))
                                + " " + Formato.txt(insumo, "unidad", "unidad")
                                + "\nStock minimo: " + Formato.numero(Formato.num(insumo, "stock_minimo")));
            }
        });
        return tarjeta;
    }

    private void dialogoInsumo(final JSONObject insumo) {
        final boolean edicion = insumo != null;

        LinearLayout cuerpo = new LinearLayout(this);
        cuerpo.setOrientation(LinearLayout.VERTICAL);
        cuerpo.setPadding(Ui.dp(this, 18), Ui.dp(this, 14), Ui.dp(this, 18), Ui.dp(this, 8));

        cuerpo.addView(Ui.etiqueta(this, "Nombre"));
        final EditText nombre = Ui.campo(this, "Nombre del insumo");
        nombre.setSingleLine(true);
        if (edicion) nombre.setText(Formato.txt(insumo, "nombre"));
        cuerpo.addView(nombre);

        cuerpo.addView(Ui.espacio(this, 8));
        cuerpo.addView(Ui.etiqueta(this, "Unidad de medida"));
        final EditText unidad = Ui.campo(this, "kg, litro, unidad...");
        unidad.setSingleLine(true);
        if (edicion) unidad.setText(Formato.txt(insumo, "unidad"));
        cuerpo.addView(unidad);

        cuerpo.addView(Ui.espacio(this, 8));
        cuerpo.addView(Ui.etiqueta(this, "Costo unitario (S/)"));
        final EditText costo = Ui.campoNumero(this, "0.00");
        if (edicion) {
            costo.setText(String.format(Locale.US, "%.2f", Formato.num(insumo, "costo")));
        }
        cuerpo.addView(costo);

        cuerpo.addView(Ui.espacio(this, 8));
        cuerpo.addView(Ui.etiqueta(this, "Stock minimo"));
        final EditText stockMinimo = Ui.campoNumero(this, "0");
        if (edicion) {
            stockMinimo.setText(Formato.numero(Formato.num(insumo, "stock_minimo")));
        }
        cuerpo.addView(stockMinimo);

        final EditText stockInicial;
        if (!edicion) {
            cuerpo.addView(Ui.espacio(this, 8));
            cuerpo.addView(Ui.etiqueta(this, "Stock inicial"));
            stockInicial = Ui.campoNumero(this, "0");
            cuerpo.addView(stockInicial);
        } else {
            stockInicial = null;
            TextView actual = Ui.texto(this,
                    "El stock actual se modifica con movimientos de entrada o salida.",
                    12f, Ui.SUAVE, false);
            actual.setPadding(0, Ui.dp(this, 10), 0, 0);
            cuerpo.addView(actual);
        }

        AlertDialog.Builder constructor = new AlertDialog.Builder(this)
                .setTitle(edicion ? "Editar insumo" : "Nuevo insumo")
                .setView(cuerpo)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Guardar", null);

        final AlertDialog dialogo = constructor.create();

        dialogo.setOnShowListener(ignorado -> {
            dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String valorNombre = nombre.getText().toString().trim();
                String valorUnidad = unidad.getText().toString().trim();
                double valorCosto = Formato.parseMonto(costo.getText().toString());
                double valorMinimo = Formato.parseMonto(stockMinimo.getText().toString());
                double valorInicial = stockInicial == null ? 0
                        : Formato.parseMonto(stockInicial.getText().toString());

                if (valorNombre.isEmpty()) {
                    Ui.toast(this, "Escribe el nombre del insumo.");
                    return;
                }
                if (valorUnidad.isEmpty()) {
                    Ui.toast(this, "Indica la unidad de medida.");
                    return;
                }
                if (valorCosto < 0 || valorMinimo < 0 || valorInicial < 0) {
                    Ui.toast(this, "Los valores no pueden ser negativos.");
                    return;
                }

                JSONObject datos = new JSONObject();
                try {
                    if (edicion) datos.put("id", insumo.optInt("id", 0));
                    datos.put("nombre", valorNombre);
                    datos.put("unidad", valorUnidad);
                    datos.put("costo", valorCosto);
                    datos.put("stock_minimo", valorMinimo);
                    if (!edicion) datos.put("stock_actual", valorInicial);
                } catch (Exception e) {
                    Ui.toast(this, "Datos no validos.");
                    return;
                }

                Api.post(this, "insumo_guardar", datos, new Api.Callback() {
                    @Override
                    public void onSuccess(Object data) {
                        dialogo.dismiss();
                        Ui.toast(InventarioActivity.this, "Insumo guardado.");
                        cargar();
                    }

                    @Override
                    public void onError(String mensaje) {
                        alerta("No se pudo guardar", mensaje);
                    }
                });
            });
        });

        if (edicion) {
            Button movimiento = Ui.botonNaranja(this, "Registrar movimiento");
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.topMargin = Ui.dp(this, 10);
            movimiento.setLayoutParams(lp);
            movimiento.setOnClickListener(v -> {
                dialogo.dismiss();
                dialogoMovimiento(insumo);
            });
            cuerpo.addView(movimiento);

            Button eliminar = Ui.botonSecundario(this, "Eliminar insumo");
            LinearLayout.LayoutParams lpEliminar = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lpEliminar.topMargin = Ui.dp(this, 8);
            eliminar.setLayoutParams(lpEliminar);
            eliminar.setTextColor(Ui.ROJO_CLARO);
            eliminar.setOnClickListener(v -> confirmarEliminarInsumo(insumo, dialogo));
            cuerpo.addView(eliminar);
        }

        dialogo.show();
    }

    private void dialogoMovimiento(final JSONObject insumo) {
        final int id = insumo.optInt("id", 0);
        LinearLayout cuerpo = new LinearLayout(this);
        cuerpo.setOrientation(LinearLayout.VERTICAL);
        cuerpo.setPadding(Ui.dp(this, 18), Ui.dp(this, 14), Ui.dp(this, 18), Ui.dp(this, 8));

        TextView info = Ui.texto(this, "Stock actual: "
                        + Formato.numero(Formato.num(insumo, "stock_actual")) + " "
                        + Formato.txt(insumo, "unidad", "unidad"),
                13f, Ui.SUAVE, false);
        cuerpo.addView(info);

        cuerpo.addView(Ui.espacio(this, 8));
        cuerpo.addView(Ui.etiqueta(this, "Tipo de movimiento"));
        final Spinner tipo = spinner(TIPOS_TEXTO);
        cuerpo.addView(tipo);

        cuerpo.addView(Ui.espacio(this, 8));
        cuerpo.addView(Ui.etiqueta(this, "Cantidad"));
        final EditText cantidad = Ui.campoNumero(this, "0");
        cuerpo.addView(cantidad);

        cuerpo.addView(Ui.espacio(this, 8));
        cuerpo.addView(Ui.etiqueta(this, "Costo unitario (S/)"));
        final EditText costo = Ui.campoNumero(this, "0.00");
        costo.setText(String.format(Locale.US, "%.2f", Formato.num(insumo, "costo")));
        cuerpo.addView(costo);

        cuerpo.addView(Ui.espacio(this, 8));
        cuerpo.addView(Ui.etiqueta(this, "Motivo"));
        final EditText motivo = Ui.campo(this, "Compra, merma, consumo...");
        cuerpo.addView(motivo);

        new AlertDialog.Builder(this)
                .setTitle("Movimiento de inventario")
                .setView(cuerpo)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Registrar", (dialogo, opcion) -> {
                    double valorCantidad = Formato.parseMonto(cantidad.getText().toString());
                    double valorCosto = Formato.parseMonto(costo.getText().toString());
                    if (valorCantidad <= 0) {
                        Ui.toast(this, "La cantidad debe ser mayor a cero.");
                        return;
                    }
                    if (valorCosto < 0) {
                        Ui.toast(this, "El costo no es valido.");
                        return;
                    }
                    JSONObject datos = new JSONObject();
                    try {
                        datos.put("insumo_id", id);
                        datos.put("tipo", TIPOS[tipo.getSelectedItemPosition()]);
                        datos.put("cantidad", valorCantidad);
                        datos.put("costo_unitario", valorCosto);
                        datos.put("motivo", motivo.getText().toString().trim());
                    } catch (Exception ignorado) {
                    }
                    Api.post(this, "movimiento_crear", datos, new Api.Callback() {
                        @Override
                        public void onSuccess(Object data) {
                            String mensaje = "Movimiento registrado.";
                            if (data instanceof JSONObject) {
                                JSONObject resultado = (JSONObject) data;
                                JSONObject actualizado = resultado.optJSONObject("insumo");
                                if (actualizado != null) {
                                    mensaje += " Nuevo stock: "
                                            + Formato.numero(Formato.num(actualizado, "stock_actual"))
                                            + " " + Formato.txt(actualizado, "unidad", "");
                                }
                            }
                            Ui.toast(InventarioActivity.this, mensaje);
                            cargar();
                        }

                        @Override
                        public void onError(String mensaje) {
                            alerta("No se pudo registrar", mensaje);
                        }
                    });
                })
                .show();
    }

    private void confirmarEliminarInsumo(final JSONObject insumo, final AlertDialog padre) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar insumo")
                .setMessage("\u00bfEliminar " + Formato.txt(insumo, "nombre", "este insumo") + "?")
                .setNegativeButton("No", null)
                .setPositiveButton("Eliminar", (dialogo, opcion) -> {
                    JSONObject datos = new JSONObject();
                    try {
                        datos.put("id", insumo.optInt("id", 0));
                    } catch (Exception ignorado) {
                    }
                    Api.post(this, "insumo_eliminar", datos, new Api.Callback() {
                        @Override
                        public void onSuccess(Object data) {
                            padre.dismiss();
                            Ui.toast(InventarioActivity.this, "Insumo eliminado.");
                            cargar();
                        }

                        @Override
                        public void onError(String mensaje) {
                            alerta("No se pudo eliminar", mensaje);
                        }
                    });
                })
                .show();
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
