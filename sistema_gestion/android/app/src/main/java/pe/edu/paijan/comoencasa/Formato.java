package pe.edu.paijan.comoencasa;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class Formato {
    private Formato() {
    }

    public static String dinero(double valor) {
        return String.format(Locale.US, "S/ %.2f", valor);
    }

    public static String numero(double valor) {
        if (Math.abs(valor - Math.rint(valor)) < 0.0001) {
            return String.format(Locale.US, "%.0f", valor);
        }
        return String.format(Locale.US, "%.2f", valor);
    }

    public static String estado(String estado) {
        return capital(estado == null ? "" : estado.replace('_', ' '));
    }

    public static int colorEstado(String estado) {
        if (estado == null) return Ui.SUAVE;
        switch (estado) {
            case "registrada":
            case "en_preparacion":
                return Ui.NARANJA;
            case "lista":
                return Ui.AMARILLO;
            case "entregada":
            case "pagada":
            case "disponible":
            case "activo":
                return Ui.VERDE;
            case "cerrada":
            case "inactivo":
                return Ui.SUAVE;
            case "anulada":
            case "agotado":
                return Ui.ROJO_CLARO;
            default:
                return Ui.SUAVE;
        }
    }

    public static String fechaHora(String iso) {
        if (iso == null) return "";
        if (iso.length() >= 16) {
            return iso.substring(8, 10) + "/" + iso.substring(5, 7) + "/" + iso.substring(0, 4)
                    + " " + iso.substring(11, 16);
        }
        return fecha(iso);
    }

    public static String fecha(String iso) {
        if (iso == null) return "";
        if (iso.length() >= 10) {
            return iso.substring(8, 10) + "/" + iso.substring(5, 7) + "/" + iso.substring(0, 4);
        }
        return iso;
    }

    public static String hoy() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }

    public static String primerDiaMes() {
        return new SimpleDateFormat("yyyy-MM-01", Locale.US).format(new Date());
    }

    public static String capital(String texto) {
        if (texto == null || texto.isEmpty()) return "";
        texto = texto.trim();
        if (texto.isEmpty()) return "";
        return texto.substring(0, 1).toUpperCase(Locale.US) + texto.substring(1);
    }

    public static String tipoServicio(String tipo) {
        if (tipo == null) return "";
        switch (tipo) {
            case "mesa": return "Mesa";
            case "llevar": return "Para llevar";
            case "delivery": return "Delivery";
            default: return capital(tipo);
        }
    }

    public static String medio(String medio) {
        if (medio == null) return "";
        switch (medio) {
            case "efectivo": return "Efectivo";
            case "yape": return "Yape";
            case "tarjeta": return "Tarjeta";
            default: return capital(medio);
        }
    }

    public static String txt(JSONObject objeto, String clave) {
        return txt(objeto, clave, "");
    }

    public static String txt(JSONObject objeto, String clave, String porDefecto) {
        if (objeto == null) return porDefecto;
        String valor = objeto.optString(clave, porDefecto);
        return valor == null || "null".equals(valor) ? porDefecto : valor;
    }

    public static double num(JSONObject objeto, String clave) {
        return objeto == null ? 0 : objeto.optDouble(clave, 0);
    }

    public static int ent(JSONObject objeto, String clave) {
        return objeto == null ? 0 : objeto.optInt(clave, 0);
    }

    public static boolean bool(JSONObject objeto, String clave) {
        return objeto != null && objeto.optBoolean(clave, false);
    }

    public static double parseMonto(String texto) {
        if (texto == null) return 0;
        texto = texto.trim().replace(',', '.');
        if (texto.isEmpty()) return 0;
        try {
            return Double.parseDouble(texto);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
