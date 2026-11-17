package pe.edu.paijan.comoencasa;

import android.content.Context;
import android.content.SharedPreferences;

public final class Prefs {
    private static final String ARCHIVO = "como_en_casa_prefs";
    private static final String CLAVE_BASE = "base_url";
    private static final String CLAVE_TOKEN = "token";
    private static final String CLAVE_NOMBRES = "nombres";
    private static final String CLAVE_USUARIO = "usuario";
    private static final String CLAVE_ROL = "rol";

    public static final String BASE_POR_DEFECTO = "http://10.0.2.2/como_en_casa/api.php";

    private Prefs() {
    }

    private static SharedPreferences sp(Context contexto) {
        return contexto.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE);
    }

    public static String baseUrl(Context contexto) {
        String valor = sp(contexto).getString(CLAVE_BASE, BASE_POR_DEFECTO);
        return valor == null || valor.trim().isEmpty() ? BASE_POR_DEFECTO : valor.trim();
    }

    public static void guardarBaseUrl(Context contexto, String url) {
        sp(contexto).edit().putString(CLAVE_BASE, url.trim()).apply();
    }

    public static String token(Context contexto) {
        return sp(contexto).getString(CLAVE_TOKEN, "");
    }

    public static String nombres(Context contexto) {
        return sp(contexto).getString(CLAVE_NOMBRES, "");
    }

    public static String usuario(Context contexto) {
        return sp(contexto).getString(CLAVE_USUARIO, "");
    }

    public static String rol(Context contexto) {
        return sp(contexto).getString(CLAVE_ROL, "");
    }

    public static boolean haySesion(Context contexto) {
        return !token(contexto).isEmpty();
    }

    public static void guardarSesion(Context contexto, String token, String nombres, String usuario, String rol) {
        sp(contexto).edit()
                .putString(CLAVE_TOKEN, token)
                .putString(CLAVE_NOMBRES, nombres)
                .putString(CLAVE_USUARIO, usuario)
                .putString(CLAVE_ROL, rol)
                .apply();
    }

    public static void limpiarSesion(Context contexto) {
        sp(contexto).edit()
                .remove(CLAVE_TOKEN)
                .remove(CLAVE_NOMBRES)
                .remove(CLAVE_USUARIO)
                .remove(CLAVE_ROL)
                .apply();
    }
}
