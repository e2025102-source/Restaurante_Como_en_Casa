<?php
declare(strict_types=1);
require_once __DIR__ . '/config/config.php';
?>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="theme-color" content="#7a0c0c">
    <title><?= APP_NOMBRE ?> | Sistema de Gestion</title>
    <link rel="icon" href="assets/favicon.svg" type="image/svg+xml">
    <link rel="stylesheet" href="assets/styles.css">
</head>
<body>

<!-- ============================ LOGIN ============================ -->
<div id="vista-login" class="login-shell">
    <section class="login-hero">
        <div class="login-hero-bg"></div>
        <div class="login-hero-content">
            <div class="marca marca-clara">
                <span class="marca-icono">
                    <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M11 9H9V2H7v7H5V2H3v7c0 2.12 1.66 3.84 3.75 3.97V22h2.5v-9.03C11.34 12.84 13 11.12 13 9V2h-2v7zm5-3v8h2.5v8H21V2c-2.76 0-5 2.24-5 4z"/></svg>
                </span>
                <div>
                    <strong>Como en Casa</strong>
                    <span>Sazon y tradicion</span>
                </div>
            </div>
            <h1>El control total de tu restaurante, en un solo lugar.</h1>
            <p>Ventas, pedidos, inventario, gastos, personal y reportes integrados en una misma plataforma.</p>
            <ul class="login-lista">
                <li>Registra pedidos y cobra en efectivo, Yape o tarjeta.</li>
                <li>Controla el stock de insumos y recibe alertas.</li>
                <li>Consulta la ganancia del dia y del periodo.</li>
            </ul>
            <a class="login-menu-link" href="menu.php">Ver la carta publica &rarr;</a>
        </div>
    </section>

    <section class="login-panel">
        <form id="form-login" class="login-card" autocomplete="on">
            <div class="login-card-head">
                <span class="login-badge">Acceso al sistema</span>
                <h2>Bienvenido de vuelta</h2>
                <p>Ingrese sus credenciales para continuar.</p>
            </div>
            <div id="login-error" class="alerta-error oculto"></div>
            <label class="campo">
                <span>Usuario</span>
                <input type="text" id="login-usuario" name="usuario" placeholder="Ej. admin" autocomplete="username" required>
            </label>
            <label class="campo">
                <span>Contrasena</span>
                <div class="campo-clave">
                    <input type="password" id="login-clave" name="clave" placeholder="Ingrese su contrasena" autocomplete="current-password" required>
                    <button type="button" id="btn-ver-clave" class="btn-ojo" title="Mostrar u ocultar contrasena" aria-label="Mostrar contrasena"></button>
                </div>
            </label>
            <button type="submit" class="btn btn-primario btn-bloque" id="btn-ingresar">Ingresar</button>

            <div class="demo-box">
                <span class="demo-titulo">Cuentas de demostracion</span>
                <div class="demo-grid">
                    <button type="button" class="demo-chip" data-usuario="admin" data-clave="admin123"><strong>Administrador</strong><span>admin / admin123</span></button>
                    <button type="button" class="demo-chip" data-usuario="cajero" data-clave="cajero123"><strong>Cajero</strong><span>cajero / cajero123</span></button>
                    <button type="button" class="demo-chip" data-usuario="cocina" data-clave="cocina123"><strong>Cocina</strong><span>cocina / cocina123</span></button>
                    <button type="button" class="demo-chip" data-usuario="inventario" data-clave="inventario123"><strong>Inventario</strong><span>inventario / inventario123</span></button>
                </div>
            </div>
        </form>
    </section>
</div>

<!-- ============================ APLICACION ============================ -->
<div id="vista-app" class="app-shell oculto">
    <aside id="sidebar" class="sidebar">
        <div class="sidebar-marca">
            <span class="marca-icono">
                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M11 9H9V2H7v7H5V2H3v7c0 2.12 1.66 3.84 3.75 3.97V22h2.5v-9.03C11.34 12.84 13 11.12 13 9V2h-2v7zm5-3v8h2.5v8H21V2c-2.76 0-5 2.24-5 4z"/></svg>
            </span>
            <div>
                <strong>Como en Casa</strong>
                <span>Sistema de gestion</span>
            </div>
        </div>
        <nav id="nav-principal" class="nav-principal" aria-label="Menu principal"></nav>
        <div class="sidebar-pie">
            <div class="sidebar-usuario">
                <span id="sidebar-avatar" class="avatar">A</span>
                <div>
                    <strong id="sidebar-nombre">Usuario</strong>
                    <span id="sidebar-rol">Rol</span>
                </div>
            </div>
            <button type="button" id="btn-salir" class="btn-salir">Cerrar sesion</button>
        </div>
    </aside>

    <div id="sidebar-overlay" class="sidebar-overlay"></div>

    <div class="app-main">
        <header class="topbar">
            <div class="topbar-izquierda">
                <button type="button" id="btn-menu" class="btn-icono btn-menu" aria-label="Abrir menu"></button>
                <div>
                    <h2 id="titulo-pagina">Panel principal</h2>
                    <span id="subtitulo-pagina" class="topbar-fecha"></span>
                </div>
            </div>
            <div class="topbar-derecha">
                <span id="topbar-fecha" class="fecha-chip"></span>
                <span id="topbar-usuario" class="usuario-chip"></span>
            </div>
        </header>
        <main id="contenido" class="contenido" aria-live="polite"></main>
    </div>
</div>

<!-- ============================ MODAL ============================ -->
<div id="modal-overlay" class="modal-overlay oculto" role="dialog" aria-modal="true" aria-labelledby="modal-titulo">
    <div id="modal-caja" class="modal-caja">
        <div class="modal-cabecera">
            <h3 id="modal-titulo">Titulo</h3>
            <button type="button" id="modal-cerrar" class="btn-icono" aria-label="Cerrar"></button>
        </div>
        <form id="modal-form">
            <div id="modal-cuerpo" class="modal-cuerpo"></div>
            <div class="modal-pie">
                <button type="button" id="modal-cancelar" class="btn btn-secundario">Cancelar</button>
                <button type="submit" id="modal-guardar" class="btn btn-primario">Guardar</button>
            </div>
        </form>
    </div>
</div>

<div id="toast-container" class="toast-container" aria-live="assertive"></div>

<script src="assets/app.js"></script>
</body>
</html>
