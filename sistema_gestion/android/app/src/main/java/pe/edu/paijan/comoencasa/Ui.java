package pe.edu.paijan.comoencasa;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public final class Ui {
    public static final int ROJO = Color.rgb(139, 0, 0);
    public static final int ROJO_OSCURO = Color.rgb(77, 5, 5);
    public static final int NARANJA = Color.rgb(230, 126, 34);
    public static final int CREMA = Color.rgb(253, 248, 241);
    public static final int CREMA_2 = Color.rgb(247, 239, 229);
    public static final int BLANCO = Color.WHITE;
    public static final int TEXTO = Color.rgb(43, 33, 29);
    public static final int SUAVE = Color.rgb(111, 98, 91);
    public static final int BORDE = Color.rgb(234, 223, 210);
    public static final int VERDE = Color.rgb(31, 138, 76);
    public static final int ROJO_CLARO = Color.rgb(168, 15, 15);
    public static final int AMARILLO = Color.rgb(180, 122, 0);

    private Ui() {
    }

    public static int dp(Context contexto, float valor) {
        return Math.round(valor * contexto.getResources().getDisplayMetrics().density);
    }

    public static GradientDrawable fondo(int color, float radio) {
        GradientDrawable fondo = new GradientDrawable();
        fondo.setColor(color);
        fondo.setCornerRadius(dpActual(radio));
        return fondo;
    }

    private static float dpActual(float valor) {
        return valor;
    }

    public static GradientDrawable fondoBorde(int color, float radio, int colorBorde, float grosor) {
        GradientDrawable fondo = new GradientDrawable();
        fondo.setColor(color);
        fondo.setCornerRadius(radio);
        fondo.setStroke(Math.max(1, Math.round(grosor)), colorBorde);
        return fondo;
    }

    public static TextView texto(Context contexto, CharSequence contenido, float tamanio, int color, boolean negrita) {
        TextView vista = new TextView(contexto);
        vista.setText(contenido);
        vista.setTextSize(tamanio);
        vista.setTextColor(color);
        if (negrita) {
            vista.setTypeface(Typeface.DEFAULT_BOLD);
        }
        return vista;
    }

    public static TextView etiqueta(Context contexto, String contenido) {
        TextView vista = texto(contexto, contenido.toUpperCase(), 11f, SUAVE, true);
        vista.setLetterSpacing(0.08f);
        return vista;
    }

    public static EditText campo(Context contexto, String pista) {
        EditText campo = new EditText(contexto);
        campo.setHint(pista);
        campo.setTextSize(15f);
        campo.setTextColor(TEXTO);
        campo.setHintTextColor(SUAVE);
        campo.setInputType(InputType.TYPE_CLASS_TEXT);
        campo.setBackground(fondoBorde(BLANCO, dp(contexto, 9), BORDE, 1));
        int p = dp(contexto, 12);
        campo.setPadding(p, dp(contexto, 10), p, dp(contexto, 10));
        return campo;
    }

    public static EditText campoNumero(Context contexto, String pista) {
        EditText campo = campo(contexto, pista);
        campo.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        return campo;
    }

    public static Button boton(Context contexto, String contenido, int color) {
        Button boton = new Button(contexto);
        boton.setText(contenido);
        boton.setAllCaps(false);
        boton.setTextSize(14.5f);
        boton.setTextColor(Color.WHITE);
        boton.setBackground(fondo(color, dp(contexto, 10)));
        boton.setPadding(dp(contexto, 16), dp(contexto, 10), dp(contexto, 16), dp(contexto, 10));
        boton.setMinHeight(dp(contexto, 44));
        return boton;
    }

    public static Button botonPrimario(Context contexto, String contenido) {
        return boton(contexto, contenido, ROJO);
    }

    public static Button botonNaranja(Context contexto, String contenido) {
        return boton(contexto, contenido, NARANJA);
    }

    public static Button botonSecundario(Context contexto, String contenido) {
        Button boton = new Button(contexto);
        boton.setText(contenido);
        boton.setAllCaps(false);
        boton.setTextSize(14f);
        boton.setTextColor(TEXTO);
        boton.setBackground(fondoBorde(BLANCO, dp(contexto, 10), BORDE, 1));
        boton.setPadding(dp(contexto, 14), dp(contexto, 10), dp(contexto, 14), dp(contexto, 10));
        boton.setMinHeight(dp(contexto, 44));
        return boton;
    }

    public static LinearLayout tarjeta(Context contexto) {
        LinearLayout tarjeta = new LinearLayout(contexto);
        tarjeta.setOrientation(LinearLayout.VERTICAL);
        tarjeta.setBackground(fondo(BLANCO, dp(contexto, 14)));
        tarjeta.setPadding(dp(contexto, 16), dp(contexto, 14), dp(contexto, 16), dp(contexto, 14));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(contexto, 12);
        tarjeta.setLayoutParams(lp);
        return tarjeta;
    }

    public static LinearLayout fila(Context contexto) {
        LinearLayout fila = new LinearLayout(contexto);
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setGravity(Gravity.CENTER_VERTICAL);
        return fila;
    }

    public static View espacio(Context contexto, int altoDp) {
        View vista = new View(contexto);
        vista.setLayoutParams(new LinearLayout.LayoutParams(1, dp(contexto, altoDp)));
        return vista;
    }

    public static void margenInferior(View vista, Context contexto, int dp) {
        ViewGroup.LayoutParams lp = vista.getLayoutParams();
        if (lp instanceof LinearLayout.LayoutParams) {
            ((LinearLayout.LayoutParams) lp).bottomMargin = dp(contexto, dp);
            vista.setLayoutParams(lp);
        }
    }

    public static void margenDerecha(View vista, Context contexto, int dp) {
        ViewGroup.LayoutParams lp = vista.getLayoutParams();
        if (lp instanceof LinearLayout.LayoutParams) {
            ((LinearLayout.LayoutParams) lp).rightMargin = dp(contexto, dp);
            vista.setLayoutParams(lp);
        }
    }

    public static void toast(Context contexto, String mensaje) {
        Toast.makeText(contexto, mensaje, Toast.LENGTH_SHORT).show();
    }
}
