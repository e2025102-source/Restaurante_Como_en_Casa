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
import java.util.Map;

public class ProductosActivity extends BaseActivity {
    private static final String[] CATEGORIAS = {"entradas", "platos", "bebidas", "postres"};
    private static final String[] CATEGORIAS_TEXTO = {"Todas", "Entradas", "Platos", "Bebidas", "Postres"};
    private static final String[] ESTADOS = {"disponible", "agotado", "inactivo"};
    private static final String[] ESTADOS_TEXTO = {"Todos", "Disponible", "Agotado", "Inactivo"};

    private LinearLayout lista;
    private EditText busqueda;
    private Spinner filtroCategoria;
    private Spinner filtroEstado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitulo("Productos");

        LinearLayout barra = Ui.fila(this);
        busqueda = Ui.campo(this, "Buscar producto...");
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
            Button nuevo = Ui.botonPrimario(this, "+ Nuevo");
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.leftMargin = Ui.dp(this, 8);
            nuevo.setLayoutParams(lp);
            nuevo.setOnClickListener(v -> dialogoProducto(null));
            barra.addView(nuevo);
        }
        agregar(barra);

        LinearLayout filtros = Ui.fila(this);
        filtroCategoria = spinner(CATEGORIAS_TEXTO);
        filtroCategoria.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        filtroCategoria.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> padre, View vista, int posicion, long id) {
                cargar();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> padre) {
            }
        });
        filtros.addView(filtroCategoria);

        filtroEstado = spinner(ESTADOS_TEXTO);
        LinearLayout.LayoutParams lpEstado = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lpEstado.leftMargin = Ui.dp(this, 8);
        filtroEstado.setLayoutParams(lpEstado);
        filtroEstado.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> padre, View vista, int posicion, long id) {
                cargar();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> padre) {
            }
        });
        filtros.addView(filtroEstado);

        LinearLayout.LayoutParams lpFiltros = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpFiltros.topMargin = Ui.dp(this, 10);
        filtros.setLayoutParams(lpFiltros);
        agregar(filtros);

        lista = new LinearLayout(this);
        lista.setOrientation(LinearLayout.VERTICAL);
        lista.setPadding(0, Ui.dp(this, 12), 0, 0);
        agregar(lista);

        cargar();
    }

    private void cargar() {
        if (lista == null) return;
        lista.removeAllViews();
        lista.addView(mensaje("Cargando productos..."));

        Map<String, String> parametros = new HashMap<>();
        String q = busqueda == null ? "" : busqueda.getText().toString().trim();
        if (!q.isEmpty()) parametros.put("q", q);
        int catPos = filtroCategoria == null ? 0 : filtroCategoria.getSelectedItemPosition();
        if (catPos > 0) parametros.put("categoria", CATEGORIAS[catPos - 1]);
        int estPos = filtroEstado == null ? 0 : filtroEstado.getSelectedItemPosition();
        if (estPos > 0) parametros.put("estado", ESTADOS[estPos - 1]);

        Api.get(this, "productos", parametros, new Api.Callback() {
            @Override
            public void onSuccess(Object data) {
                lista.removeAllViews();
                if (!(data instanceof JSONArray)) {
                    lista.addView(mensaje("Respuesta inesperada del servidor."));
                    return;
                }
                JSONArray productos = (JSONArray) data;
                if (productos.length() == 0) {
                    lista.addView(mensaje("No hay productos con esos filtros."));
                    return;
                }
                for (int i = 0; i < productos.length(); i++) {
                    JSONObject producto = productos.optJSONObject(i);
                    if (producto != null) {
                        lista.addView(tarjetaProducto(producto));
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

    private LinearLayout tarjetaProducto(JSONObject producto) {
        LinearLayout tarjeta = Ui.tarjeta(this);
        LinearLayout fila = Ui.fila(this);

        LinearLayout izquierda = new LinearLayout(this);
        izquierda.setOrientation(LinearLayout.VERTICAL);
        izquierda.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        izquierda.addView(Ui.texto(this, Formato.txt(producto, "nombre", "Producto"),
                15f, Ui.TEXTO, true));
        izquierda.addView(Ui.texto(this, Formato.capital(Formato.txt(producto, "categoria"))
                + " \u00b7 " + Formato.dinero(Formato.num(producto, "precio")),
                12f, Ui.SUAVE, false));
        fila.addView(izquierda);

        TextView chip = Ui.texto(this, Formato.capital(Formato.txt(producto, "estado")),
                11f, Ui.BLANCO, true);
        chip.setBackground(Ui.fondo(Formato.colorEstado(Formato.txt(producto, "estado")),
                Ui.dp(this, 20)));
        chip.setPadding(Ui.dp(this, 10), Ui.dp(this, 4), Ui.dp(this, 10), Ui.dp(this, 4));
        chip.setGravity(Gravity.CENTER);
        fila.addView(chip);
        tarjeta.addView(fila);

        String descripcion = Formato.txt(producto, "descripcion");
        if (!descripcion.isEmpty()) {
            TextView detalle = Ui.texto(this, descripcion, 12f, Ui.SUAVE, false);
            detalle.setPadding(0, Ui.dp(this, 6), 0, 0);
            tarjeta.addView(detalle);
        }

        tarjeta.setOnClickListener(v -> {
            if (puedeInventariar()) {
                dialogoProducto(producto);
            } else {
                alerta(Formato.txt(producto, "nombre", "Producto"),
                        "Categoria: " + Formato.capital(Formato.txt(producto, "categoria"))
                                + "\nPrecio: " + Formato.dinero(Formato.num(producto, "precio"))
                                + "\nEstado: " + Formato.capital(Formato.txt(producto, "estado")));
            }
        });
        return tarjeta;
    }

    private void dialogoProducto(final JSONObject producto) {
        final boolean edicion = producto != null;

        LinearLayout cuerpo = new LinearLayout(this);
        cuerpo.setOrientation(LinearLayout.VERTICAL);
        cuerpo.setPadding(Ui.dp(this, 18), Ui.dp(this, 14), Ui.dp(this, 18), Ui.dp(this, 8));

        cuerpo.addView(Ui.etiqueta(this, "Nombre"));
        final EditText nombre = Ui.campo(this, "Nombre del producto");
        nombre.setSingleLine(true);
        if (edicion) nombre.setText(Formato.txt(producto, "nombre"));
        cuerpo.addView(nombre);

        cuerpo.addView(Ui.espacio(this, 8));
        cuerpo.addView(Ui.etiqueta(this, "Descripcion"));
        final EditText descripcion = Ui.campo(this, "Descripcion breve");
        if (edicion) descripcion.setText(Formato.txt(producto, "descripcion"));
        cuerpo.addView(descripcion);

        cuerpo.addView(Ui.espacio(this, 8));
        cuerpo.addView(Ui.etiqueta(this, "Categoria"));
        final Spinner categoria = spinner(nombresCategorias());
        if (edicion) {
            int indice = indiceDe(CATEGORIAS, Formato.txt(producto, "categoria"));
            if (indice >= 0) categoria.setSelection(indice);
        }
        cuerpo.addView(categoria);

        cuerpo.addView(Ui.espacio(this, 8));
        cuerpo.addView(Ui.etiqueta(this, "Precio (S/)"));
        final EditText precio = Ui.campoNumero(this, "0.00");
        if (edicion) {
            precio.setText(String.format(java.util.Locale.US, "%.2f", Formato.num(producto, "precio")));
        }
        cuerpo.addView(precio);

        cuerpo.addView(Ui.espacio(this, 8));
        cuerpo.addView(Ui.etiqueta(this, "Estado"));
        final Spinner estado = spinner(new String[]{"Disponible", "Agotado", "Inactivo"});
        if (edicion) {
            int indice = indiceDe(ESTADOS, Formato.txt(producto, "estado"));
            if (indice >= 0) estado.setSelection(indice);
        }
        cuerpo.addView(estado);

        AlertDialog.Builder constructor = new AlertDialog.Builder(this)
                .setTitle(edicion ? "Editar producto" : "Nuevo producto")
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
                double valorPrecio = Formato.parseMonto(precio.getText().toString());
                if (valorNombre.isEmpty()) {
                    Ui.toast(this, "Escribe el nombre del producto.");
                    return;
                }
                if (valorPrecio <= 0) {
                    Ui.toast(this, "El precio debe ser mayor a cero.");
                    return;
                }
                JSONObject datos = new JSONObject();
                try {
                    if (edicion) datos.put("id", producto.optInt("id", 0));
                    datos.put("nombre", valorNombre);
                    datos.put("descripcion", descripcion.getText().toString().trim());
                    datos.put("categoria", CATEGORIAS[categoria.getSelectedItemPosition()]);
                    datos.put("precio", valorPrecio);
                    datos.put("estado", ESTADOS[estado.getSelectedItemPosition()]);
                } catch (Exception e) {
                    Ui.toast(this, "Datos no validos.");
                    return;
                }
                Api.post(this, "producto_guardar", datos, new Api.Callback() {
                    @Override
                    public void onSuccess(Object data) {
                        dialogo.dismiss();
                        Ui.toast(ProductosActivity.this, "Producto guardado.");
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
                            .setTitle("Eliminar producto")
                            .setMessage("Esta seguro de eliminar este producto?")
                            .setNegativeButton("No", null)
                            .setPositiveButton("Eliminar", (confirmacion, opcion) -> {
                                JSONObject datos = new JSONObject();
                                try {
                                    datos.put("id", producto.optInt("id", 0));
                                } catch (Exception ignorado2) {
                                }
                                Api.post(this, "producto_eliminar", datos, new Api.Callback() {
                                    @Override
                                    public void onSuccess(Object data) {
                                        dialogo.dismiss();
                                        Ui.toast(ProductosActivity.this, "Producto eliminado.");
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

    private String[] nombresCategorias() {
        return new String[]{"Entradas", "Platos", "Bebidas", "Postres"};
    }

    private int indiceDe(String[] valores, String buscado) {
        for (int i = 0; i < valores.length; i++) {
            if (valores[i].equals(buscado)) return i;
        }
        return -1;
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
