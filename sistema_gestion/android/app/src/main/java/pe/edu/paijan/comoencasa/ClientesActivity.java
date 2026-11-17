package pe.edu.paijan.comoencasa;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

public class ClientesActivity extends BaseActivity {
    private LinearLayout lista;
    private EditText busqueda;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitulo("Clientes");

        if (!puedeVender()) {
            agregar(mensaje("Tu rol no tiene acceso a este modulo."));
            return;
        }

        LinearLayout barra = Ui.fila(this);
        busqueda = Ui.campo(this, "Buscar cliente...");
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

        Button nuevo = Ui.botonPrimario(this, "+ Nuevo");
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = Ui.dp(this, 8);
        nuevo.setLayoutParams(lp);
        nuevo.setOnClickListener(v -> dialogoCliente(null));
        barra.addView(nuevo);
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
        lista.addView(mensaje("Cargando clientes..."));

        Map<String, String> parametros = new HashMap<>();
        String q = busqueda == null ? "" : busqueda.getText().toString().trim();
        if (!q.isEmpty()) parametros.put("q", q);

        Api.get(this, "clientes", parametros, new Api.Callback() {
            @Override
            public void onSuccess(Object data) {
                lista.removeAllViews();
                if (!(data instanceof JSONArray)) {
                    lista.addView(mensaje("Respuesta inesperada del servidor."));
                    return;
                }
                JSONArray clientes = (JSONArray) data;
                if (clientes.length() == 0) {
                    lista.addView(mensaje("No hay clientes registrados."));
                    return;
                }
                for (int i = 0; i < clientes.length(); i++) {
                    JSONObject cliente = clientes.optJSONObject(i);
                    if (cliente != null) {
                        lista.addView(tarjetaCliente(cliente));
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

    private LinearLayout tarjetaCliente(JSONObject cliente) {
        LinearLayout tarjeta = Ui.tarjeta(this);
        tarjeta.addView(Ui.texto(this, Formato.txt(cliente, "nombre", "Cliente"),
                15f, Ui.TEXTO, true));

        String documento = Formato.txt(cliente, "documento");
        String telefono = Formato.txt(cliente, "telefono");
        String contacto = "";
        if (!documento.isEmpty()) contacto += "Doc: " + documento;
        if (!telefono.isEmpty()) contacto += (contacto.isEmpty() ? "" : " \u00b7 ") + "Tel: " + telefono;
        if (!contacto.isEmpty()) {
            tarjeta.addView(Ui.texto(this, contacto, 12f, Ui.SUAVE, false));
        }

        String direccion = Formato.txt(cliente, "direccion");
        if (!direccion.isEmpty()) {
            tarjeta.addView(Ui.texto(this, direccion, 12f, Ui.SUAVE, false));
        }

        TextView resumen = Ui.texto(this,
                Formato.ent(cliente, "total_ventas") + " ventas \u00b7 Consumo "
                        + Formato.dinero(Formato.num(cliente, "consumo_total")),
                12f, Ui.ROJO, true);
        resumen.setPadding(0, Ui.dp(this, 6), 0, 0);
        tarjeta.addView(resumen);

        tarjeta.setOnClickListener(v -> dialogoCliente(cliente));
        return tarjeta;
    }

    private void dialogoCliente(final JSONObject cliente) {
        final boolean edicion = cliente != null;

        LinearLayout cuerpo = new LinearLayout(this);
        cuerpo.setOrientation(LinearLayout.VERTICAL);
        cuerpo.setPadding(Ui.dp(this, 18), Ui.dp(this, 14), Ui.dp(this, 18), Ui.dp(this, 8));

        cuerpo.addView(Ui.etiqueta(this, "Nombre"));
        final EditText nombre = Ui.campo(this, "Nombre completo");
        nombre.setSingleLine(true);
        if (edicion) nombre.setText(Formato.txt(cliente, "nombre"));
        cuerpo.addView(nombre);

        cuerpo.addView(Ui.espacio(this, 8));
        cuerpo.addView(Ui.etiqueta(this, "Documento (opcional)"));
        final EditText documento = Ui.campo(this, "DNI / RUC");
        documento.setSingleLine(true);
        if (edicion) documento.setText(Formato.txt(cliente, "documento"));
        cuerpo.addView(documento);

        cuerpo.addView(Ui.espacio(this, 8));
        cuerpo.addView(Ui.etiqueta(this, "Telefono (opcional)"));
        final EditText telefono = Ui.campo(this, "Numero de contacto");
        telefono.setSingleLine(true);
        if (edicion) telefono.setText(Formato.txt(cliente, "telefono"));
        cuerpo.addView(telefono);

        cuerpo.addView(Ui.espacio(this, 8));
        cuerpo.addView(Ui.etiqueta(this, "Direccion (opcional)"));
        final EditText direccion = Ui.campo(this, "Direccion del cliente");
        if (edicion) direccion.setText(Formato.txt(cliente, "direccion"));
        cuerpo.addView(direccion);

        AlertDialog.Builder constructor = new AlertDialog.Builder(this)
                .setTitle(edicion ? "Editar cliente" : "Nuevo cliente")
                .setView(cuerpo)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Guardar", null);
        if (edicion) {
            constructor.setNeutralButton("Eliminar", null);
        }
        final AlertDialog dialogo = constructor.create();

        dialogo.setOnShowListener(ignorado -> {
            dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String valorNombre = nombre.getText().toString().trim();
                if (valorNombre.isEmpty()) {
                    Ui.toast(this, "Escribe el nombre del cliente.");
                    return;
                }
                JSONObject datos = new JSONObject();
                try {
                    if (edicion) datos.put("id", cliente.optInt("id", 0));
                    datos.put("nombre", valorNombre);
                    datos.put("documento", documento.getText().toString().trim());
                    datos.put("telefono", telefono.getText().toString().trim());
                    datos.put("direccion", direccion.getText().toString().trim());
                } catch (Exception e) {
                    Ui.toast(this, "Datos no validos.");
                    return;
                }
                Api.post(this, "cliente_guardar", datos, new Api.Callback() {
                    @Override
                    public void onSuccess(Object data) {
                        dialogo.dismiss();
                        Ui.toast(ClientesActivity.this, "Cliente guardado.");
                        cargar();
                    }

                    @Override
                    public void onError(String mensaje) {
                        alerta("No se pudo guardar", mensaje);
                    }
                });
            });

            if (edicion) {
                dialogo.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Ui.ROJO_CLARO);
                dialogo.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Eliminar cliente")
                            .setMessage("\u00bfEliminar a " + Formato.txt(cliente, "nombre", "este cliente") + "?")
                            .setNegativeButton("No", null)
                            .setPositiveButton("Eliminar", (confirmacion, opcion) -> {
                                JSONObject datos = new JSONObject();
                                try {
                                    datos.put("id", cliente.optInt("id", 0));
                                } catch (Exception ignorado2) {
                                }
                                Api.post(this, "cliente_eliminar", datos, new Api.Callback() {
                                    @Override
                                    public void onSuccess(Object data) {
                                        dialogo.dismiss();
                                        Ui.toast(ClientesActivity.this, "Cliente eliminado.");
                                        cargar();
                                    }

                                    @Override
                                    public void onError(String mensaje) {
                                        alerta("No se pudo eliminar", mensaje);
                                    }
                                });
                            })
                            .show();
                });
            }
        });

        dialogo.show();
    }

    private TextView mensaje(String texto) {
        TextView vista = Ui.texto(this, texto, 14f, Ui.SUAVE, false);
        vista.setPadding(0, Ui.dp(this, 10), 0, Ui.dp(this, 10));
        return vista;
    }
}
