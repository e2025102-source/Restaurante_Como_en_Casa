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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class NuevaVentaActivity extends BaseActivity {
    private static final String[] TIPOS = {"mesa", "llevar", "delivery"};
    private static final String[] TIPOS_TEXTO = {"Mesa", "Para llevar", "Delivery"};
    private static final String[] CATEGORIAS = {"entradas", "platos", "bebidas", "postres"};
    private static final String[] CATEGORIAS_TEXTO = {"Todas", "Entradas", "Platos", "Bebidas", "Postres"};
    private static final String[] MEDIOS = {"efectivo", "yape", "tarjeta"};
    private static final String[] MEDIOS_TEXTO = {"Efectivo", "Yape", "Tarjeta"};

    private JSONArray productos = new JSONArray();
    private JSONArray clientes = new JSONArray();
    private final Map<Integer, JSONObject> mapaProductos = new LinkedHashMap<>();
    private final Map<Integer, Integer> carrito = new LinkedHashMap<>();

    private boolean productosListos;
    private boolean clientesListos;
    private boolean construido;
    private boolean enviando;
    private boolean errorCarga;

    private LinearLayout listaProductos;
    private LinearLayout listaCarrito;
    private LinearLayout contenedorCliente;
    private TextView textoTotal;
    private EditText busqueda;
    private Spinner filtroCategoria;
    private Spinner tipoServicio;
    private Spinner medioPago;
    private Spinner spinnerCliente;
    private EditText campoMesa;
    private EditText campoDescuento;
    private EditText campoObservacion;
    private EditText campoPago;
    private Button botonRegistrar;
    private final List<String> nombresClientes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitulo("Nueva venta");

        if (!puedeVender()) {
            agregar(mensaje("Tu rol no puede registrar ventas."));
            return;
        }

        cargarDatos();
    }

    private void cargarDatos() {
        productosListos = false;
        clientesListos = false;
        construido = false;
        errorCarga = false;
        limpiar();
        mostrarCargando("Cargando productos y clientes...");

        Api.get(this, "productos", null, new Api.Callback() {
            @Override
            public void onSuccess(Object data) {
                if (data instanceof JSONArray) {
                    productos = (JSONArray) data;
                    mapaProductos.clear();
                    for (int i = 0; i < productos.length(); i++) {
                        JSONObject producto = productos.optJSONObject(i);
                        if (producto != null) {
                            mapaProductos.put(producto.optInt("id", 0), producto);
                        }
                    }
                    productosListos = true;
                    intentarConstruir();
                } else {
                    fallo("No se pudieron cargar los productos.");
                }
            }

            @Override
            public void onError(String mensaje) {
                fallo(mensaje);
            }
        });

        Api.get(this, "clientes", null, new Api.Callback() {
            @Override
            public void onSuccess(Object data) {
                if (data instanceof JSONArray) {
                    clientes = (JSONArray) data;
                    clientesListos = true;
                    intentarConstruir();
                } else {
                    fallo("No se pudieron cargar los clientes.");
                }
            }

            @Override
            public void onError(String mensaje) {
                fallo(mensaje);
            }
        });
    }

    private void fallo(String mensaje) {
        if (construido) return;
        errorCarga = true;
        mostrarError(mensaje, this::cargarDatos);
    }

    private void intentarConstruir() {
        if (!productosListos || !clientesListos || construido || errorCarga) return;
        construido = true;
        limpiar();
        construirFormulario();
    }

    private void construirFormulario() {
        LinearLayout cardProductos = Ui.tarjeta(this);
        cardProductos.addView(Ui.etiqueta(this, "Productos"));

        busqueda = Ui.campo(this, "Buscar producto...");
        busqueda.setSingleLine(true);
        busqueda.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                renderProductos();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        cardProductos.addView(busqueda);

        filtroCategoria = spinner(CATEGORIAS_TEXTO);
        filtroCategoria.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> padre, View vista, int posicion, long id) {
                renderProductos();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> padre) {
            }
        });
        LinearLayout.LayoutParams lpFiltro = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpFiltro.topMargin = Ui.dp(this, 8);
        filtroCategoria.setLayoutParams(lpFiltro);
        cardProductos.addView(filtroCategoria);

        listaProductos = new LinearLayout(this);
        listaProductos.setOrientation(LinearLayout.VERTICAL);
        listaProductos.setPadding(0, Ui.dp(this, 8), 0, 0);
        cardProductos.addView(listaProductos);
        agregar(cardProductos);

        LinearLayout cardCarrito = Ui.tarjeta(this);
        cardCarrito.addView(Ui.etiqueta(this, "Carrito"));
        listaCarrito = new LinearLayout(this);
        listaCarrito.setOrientation(LinearLayout.VERTICAL);
        cardCarrito.addView(listaCarrito);

        textoTotal = Ui.texto(this, "Total: S/ 0.00", 18f, Ui.ROJO, true);
        textoTotal.setPadding(0, Ui.dp(this, 10), 0, 0);
        cardCarrito.addView(textoTotal);
        agregar(cardCarrito);

        LinearLayout cardDatos = Ui.tarjeta(this);
        cardDatos.addView(Ui.etiqueta(this, "Datos de la venta"));

        cardDatos.addView(Ui.espacio(this, 8));
        cardDatos.addView(Ui.etiqueta(this, "Tipo de servicio"));
        tipoServicio = spinner(TIPOS_TEXTO);
        cardDatos.addView(tipoServicio);

        cardDatos.addView(Ui.espacio(this, 8));
        cardDatos.addView(Ui.etiqueta(this, "Mesa (si aplica)"));
        campoMesa = Ui.campo(this, "Numero o nombre de mesa");
        campoMesa.setSingleLine(true);
        cardDatos.addView(campoMesa);

        cardDatos.addView(Ui.espacio(this, 8));
        cardDatos.addView(Ui.etiqueta(this, "Cliente"));
        contenedorCliente = new LinearLayout(this);
        contenedorCliente.setOrientation(LinearLayout.VERTICAL);
        cardDatos.addView(contenedorCliente);
        construirSpinnerCliente();

        cardDatos.addView(Ui.espacio(this, 8));
        cardDatos.addView(Ui.etiqueta(this, "Descuento (S/)"));
        campoDescuento = Ui.campoNumero(this, "0");
        campoDescuento.setText("0");
        cardDatos.addView(campoDescuento);

        cardDatos.addView(Ui.espacio(this, 8));
        cardDatos.addView(Ui.etiqueta(this, "Observacion"));
        campoObservacion = Ui.campo(this, "Sin cebolla, para llevar, etc.");
        cardDatos.addView(campoObservacion);
        agregar(cardDatos);

        LinearLayout cardPago = Ui.tarjeta(this);
        cardPago.addView(Ui.etiqueta(this, "Pago inicial (opcional)"));
        TextView pista = Ui.texto(this, "Deja el monto en 0 para cobrar despues.",
                12f, Ui.SUAVE, false);
        pista.setPadding(0, Ui.dp(this, 4), 0, Ui.dp(this, 8));
        cardPago.addView(pista);

        campoPago = Ui.campoNumero(this, "0.00");
        cardPago.addView(campoPago);

        cardPago.addView(Ui.espacio(this, 8));
        cardPago.addView(Ui.etiqueta(this, "Medio de pago"));
        medioPago = spinner(MEDIOS_TEXTO);
        cardPago.addView(medioPago);
        agregar(cardPago);

        botonRegistrar = Ui.botonPrimario(this, "Registrar venta");
        botonRegistrar.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        botonRegistrar.setOnClickListener(v -> registrarVenta());
        agregar(botonRegistrar);

        renderProductos();
        renderCarrito();
    }

    private void construirSpinnerCliente() {
        contenedorCliente.removeAllViews();
        nombresClientes.clear();
        nombresClientes.add("Consumidor final");
        for (int i = 0; i < clientes.length(); i++) {
            JSONObject cliente = clientes.optJSONObject(i);
            if (cliente == null) continue;
            String nombre = Formato.txt(cliente, "nombre", "Cliente");
            String documento = Formato.txt(cliente, "documento");
            nombresClientes.add(documento.isEmpty() ? nombre : nombre + " - " + documento);
        }
        spinnerCliente = spinner(nombresClientes.toArray(new String[0]));
        contenedorCliente.addView(spinnerCliente);
    }

    private void renderProductos() {
        if (listaProductos == null) return;
        listaProductos.removeAllViews();

        String consulta = busqueda == null ? "" : busqueda.getText().toString().trim().toLowerCase(Locale.US);
        int categoriaPos = filtroCategoria == null ? 0 : filtroCategoria.getSelectedItemPosition();
        String categoria = categoriaPos <= 0 ? "" : CATEGORIAS[categoriaPos - 1];

        int mostrados = 0;
        for (int i = 0; i < productos.length(); i++) {
            JSONObject producto = productos.optJSONObject(i);
            if (producto == null) continue;
            String nombre = Formato.txt(producto, "nombre");
            String categoriaProducto = Formato.txt(producto, "categoria");
            if (!categoria.isEmpty() && !categoria.equals(categoriaProducto)) continue;
            if (!consulta.isEmpty() && !nombre.toLowerCase(Locale.US).contains(consulta)) continue;
            listaProductos.addView(filaProducto(producto));
            mostrados++;
        }

        if (mostrados == 0) {
            listaProductos.addView(mensaje("No se encontraron productos."));
        }
    }

    private LinearLayout filaProducto(JSONObject producto) {
        LinearLayout fila = Ui.fila(this);
        fila.setPadding(0, Ui.dp(this, 6), 0, Ui.dp(this, 6));

        LinearLayout izquierda = new LinearLayout(this);
        izquierda.setOrientation(LinearLayout.VERTICAL);
        izquierda.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        String nombre = Formato.txt(producto, "nombre", "Producto");
        int cantidad = carrito.getOrDefault(producto.optInt("id", 0), 0);
        TextView nombreVista = Ui.texto(this, nombre + (cantidad > 0 ? "  (x" + cantidad + ")" : ""),
                14f, Ui.TEXTO, true);
        izquierda.addView(nombreVista);
        TextView detalle = Ui.texto(this, Formato.capital(Formato.txt(producto, "categoria"))
                + " \u00b7 " + Formato.dinero(Formato.num(producto, "precio")), 12f, Ui.SUAVE, false);
        izquierda.addView(detalle);
        fila.addView(izquierda);

        String estado = Formato.txt(producto, "estado", "disponible");
        Button agregar = Ui.botonSecundario(this, "Agregar");
        if (!"disponible".equals(estado)) {
            agregar.setText("No disponible");
            agregar.setEnabled(false);
            agregar.setAlpha(0.5f);
        } else {
            agregar.setTextColor(Ui.ROJO);
            agregar.setOnClickListener(v -> {
                int id = producto.optInt("id", 0);
                carrito.put(id, carrito.getOrDefault(id, 0) + 1);
                renderProductos();
                renderCarrito();
            });
        }
        fila.addView(agregar);
        return fila;
    }

    private void renderCarrito() {
        if (listaCarrito == null) return;
        listaCarrito.removeAllViews();

        if (carrito.isEmpty()) {
            listaCarrito.addView(mensaje("Aun no agregaste productos."));
        } else {
            for (Map.Entry<Integer, Integer> entrada : carrito.entrySet()) {
                JSONObject producto = mapaProductos.get(entrada.getKey());
                if (producto == null) continue;
                int cantidad = entrada.getValue();
                listaCarrito.addView(filaCarrito(producto, cantidad));
            }
        }

        if (textoTotal != null) {
            textoTotal.setText("Total: " + Formato.dinero(subtotal()));
        }
    }

    private LinearLayout filaCarrito(JSONObject producto, final int cantidad) {
        final int id = producto.optInt("id", 0);
        LinearLayout fila = Ui.fila(this);
        fila.setPadding(0, Ui.dp(this, 5), 0, Ui.dp(this, 5));

        LinearLayout izquierda = new LinearLayout(this);
        izquierda.setOrientation(LinearLayout.VERTICAL);
        izquierda.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        izquierda.addView(Ui.texto(this, Formato.txt(producto, "nombre"), 14f, Ui.TEXTO, true));
        izquierda.addView(Ui.texto(this, "x" + cantidad + " \u00b7 "
                        + Formato.dinero(Formato.num(producto, "precio") * cantidad),
                12f, Ui.SUAVE, false));
        fila.addView(izquierda);

        Button menos = Ui.botonSecundario(this, "-");
        LinearLayout.LayoutParams lpMenos = new LinearLayout.LayoutParams(
                Ui.dp(this, 40), Ui.dp(this, 38));
        lpMenos.rightMargin = Ui.dp(this, 4);
        menos.setLayoutParams(lpMenos);
        menos.setPadding(0, 0, 0, 0);
        menos.setOnClickListener(v -> {
            int actual = carrito.getOrDefault(id, 0);
            if (actual <= 1) {
                carrito.remove(id);
            } else {
                carrito.put(id, actual - 1);
            }
            renderProductos();
            renderCarrito();
        });
        fila.addView(menos);

        Button mas = Ui.botonPrimario(this, "+");
        mas.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(this, 40), Ui.dp(this, 38)));
        mas.setPadding(0, 0, 0, 0);
        mas.setOnClickListener(v -> {
            carrito.put(id, carrito.getOrDefault(id, 0) + 1);
            renderProductos();
            renderCarrito();
        });
        fila.addView(mas);
        return fila;
    }

    private double subtotal() {
        double total = 0;
        for (Map.Entry<Integer, Integer> entrada : carrito.entrySet()) {
            JSONObject producto = mapaProductos.get(entrada.getKey());
            if (producto != null) {
                total += Formato.num(producto, "precio") * entrada.getValue();
            }
        }
        return Math.round(total * 100.0) / 100.0;
    }

    private void registrarVenta() {
        if (enviando) return;
        if (carrito.isEmpty()) {
            Ui.toast(this, "Agrega al menos un producto.");
            return;
        }

        final double subtotal = subtotal();
        double descuento = Formato.parseMonto(campoDescuento.getText().toString());
        if (descuento < 0) {
            Ui.toast(this, "El descuento no es valido.");
            return;
        }
        if (descuento > subtotal) {
            Ui.toast(this, "El descuento no puede superar el subtotal.");
            return;
        }
        double total = Math.round((subtotal - descuento) * 100.0) / 100.0;

        double pago = Formato.parseMonto(campoPago.getText().toString());
        if (pago < 0) {
            Ui.toast(this, "El monto del pago no es valido.");
            return;
        }
        if (pago > total + 0.001) {
            Ui.toast(this, "El pago inicial no puede superar el total.");
            return;
        }

        JSONObject cuerpo = new JSONObject();
        try {
            cuerpo.put("idempotencia", UUID.randomUUID().toString());
            cuerpo.put("tipo_servicio", TIPOS[tipoServicio.getSelectedItemPosition()]);
            String mesa = campoMesa.getText().toString().trim();
            if (!mesa.isEmpty()) {
                cuerpo.put("mesa", mesa);
            }
            int clientePos = spinnerCliente == null ? 0 : spinnerCliente.getSelectedItemPosition();
            if (clientePos > 0) {
                JSONObject cliente = clientes.optJSONObject(clientePos - 1);
                if (cliente != null) {
                    cuerpo.put("cliente_id", cliente.optInt("id", 0));
                }
            }
            cuerpo.put("descuento", descuento);
            String observacion = campoObservacion.getText().toString().trim();
            if (!observacion.isEmpty()) {
                cuerpo.put("observacion", observacion);
            }

            JSONArray items = new JSONArray();
            for (Map.Entry<Integer, Integer> entrada : carrito.entrySet()) {
                JSONObject item = new JSONObject();
                item.put("producto_id", entrada.getKey());
                item.put("cantidad", entrada.getValue());
                items.put(item);
            }
            cuerpo.put("items", items);

            if (pago > 0) {
                JSONArray pagos = new JSONArray();
                JSONObject pagoJson = new JSONObject();
                pagoJson.put("monto", pago);
                pagoJson.put("medio", MEDIOS[medioPago.getSelectedItemPosition()]);
                pagos.put(pagoJson);
                cuerpo.put("pagos", pagos);
            }
        } catch (Exception e) {
            Ui.toast(this, "No se pudo preparar la venta: " + e.getMessage());
            return;
        }

        enviando = true;
        botonRegistrar.setEnabled(false);
        botonRegistrar.setText("Registrando...");

        Api.post(this, "venta_crear", cuerpo, new Api.Callback() {
            @Override
            public void onSuccess(Object data) {
                enviando = false;
                botonRegistrar.setEnabled(true);
                botonRegistrar.setText("Registrar venta");
                JSONObject venta = data instanceof JSONObject ? (JSONObject) data : null;
                String numero = venta == null ? "Venta" : Formato.txt(venta, "numero", "Venta");
                double totalFinal = venta == null ? 0 : Formato.num(venta, "total");
                carrito.clear();
                renderCarrito();
                new AlertDialog.Builder(NuevaVentaActivity.this)
                        .setTitle("Venta registrada")
                        .setMessage(numero + " por " + Formato.dinero(totalFinal)
                                + ".\nEl pedido queda en estado registrada.")
                        .setPositiveButton("Aceptar", (dialogo, opcion) -> finish())
                        .setNeutralButton("Ver ventas", (dialogo, opcion) -> {
                            ir(VentasActivity.class);
                            finish();
                        })
                        .show();
            }

            @Override
            public void onError(String mensaje) {
                enviando = false;
                botonRegistrar.setEnabled(true);
                botonRegistrar.setText("Registrar venta");
                alerta("No se pudo registrar la venta", mensaje);
            }
        });
    }

    private TextView mensaje(String texto) {
        TextView vista = Ui.texto(this, texto, 13f, Ui.SUAVE, false);
        vista.setPadding(0, Ui.dp(this, 8), 0, Ui.dp(this, 8));
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
