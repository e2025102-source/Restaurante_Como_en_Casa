<?php
declare(strict_types=1);

require_once __DIR__ . '/config/config.php';
require_once __DIR__ . '/app/Database.php';
require_once __DIR__ . '/app/modelos/Reporte.php';

$menu = Reporte::menuPublico();
$categorias = $menu['categorias'];
$orden = ['entradas', 'platos', 'bebidas', 'postres'];
$etiquetas = [
    'entradas' => 'Entradas',
    'platos'   => 'Platos de fondo',
    'bebidas'  => 'Bebidas',
    'postres'  => 'Postres',
];
$presentes = array_values(array_filter($orden, static fn($c) => !empty($categorias[$c])));

function h(string $texto): string
{
    return htmlspecialchars($texto, ENT_QUOTES, 'UTF-8');
}
?>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="theme-color" content="#7a0c0c">
    <meta name="description" content="Carta del restaurante Como en Casa: entradas, platos de fondo, bebidas y postres.">
    <title><?= h(APP_NOMBRE) ?> | Nuestra carta</title>
    <link rel="icon" href="assets/favicon.svg" type="image/svg+xml">
    <link rel="stylesheet" href="assets/styles.css">
</head>
<body class="menu-publico">

<header class="menu-header">
    <div class="menu-header-inner">
        <a class="marca" href="menu.php">
            <span class="marca-icono">
                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M11 9H9V2H7v7H5V2H3v7c0 2.12 1.66 3.84 3.75 3.97V22h2.5v-9.03C11.34 12.84 13 11.12 13 9V2h-2v7zm5-3v8h2.5v8H21V2c-2.76 0-5 2.24-5 4z"/></svg>
            </span>
            <div>
                <strong>Como en Casa</strong>
                <span>Sazon y tradicion</span>
            </div>
        </a>
        <nav class="menu-nav" aria-label="Categorias de la carta">
            <?php foreach ($presentes as $clave): ?>
                <a href="#<?= h($clave) ?>"><?= h($etiquetas[$clave] ?? ucfirst($clave)) ?></a>
            <?php endforeach; ?>
        </nav>
        <a class="btn btn-primario" href="index.php">Ingresar al sistema</a>
    </div>
</header>

<section class="menu-hero">
    <div class="menu-hero-bg"></div>
    <div class="menu-hero-content">
        <h1>Sabores que se sienten como en casa</h1>
        <p>Comida casera preparada al momento, con ingredientes frescos y el sazon de siempre.</p>
        <a class="btn btn-naranja" href="#carta">Ver la carta</a>
    </div>
</section>

<main id="carta">
    <?php if (!$presentes): ?>
        <div class="menu-seccion">
            <div class="estado-vacio">
                <strong>Carta en preparacion</strong>
                <span>Pronto publicaremos nuestros platos disponibles.</span>
            </div>
        </div>
    <?php endif; ?>

    <?php foreach ($presentes as $clave): ?>
        <section class="menu-seccion" id="<?= h($clave) ?>">
            <div class="menu-seccion-titulo">
                <h2><?= h($etiquetas[$clave] ?? ucfirst($clave)) ?></h2>
                <span class="linea"></span>
            </div>
            <div class="menu-grid">
                <?php foreach ($categorias[$clave] as $producto): ?>
                    <article class="plato-card">
                        <span class="categoria"><?= h($etiquetas[$clave] ?? ucfirst($clave)) ?></span>
                        <h3><?= h($producto['nombre']) ?></h3>
                        <p><?= h($producto['descripcion'] ?: 'Preparado al momento con ingredientes frescos.') ?></p>
                        <span class="precio">S/ <?= number_format((float) $producto['precio'], 2) ?></span>
                    </article>
                <?php endforeach; ?>
            </div>
        </section>
    <?php endforeach; ?>
</main>

<footer class="menu-pie">
    <strong>Como en Casa</strong> · Sazon y tradicion<br>
    Atencion de lunes a domingo · Pedidos en salon, para llevar y delivery.
</footer>

</body>
</html>
