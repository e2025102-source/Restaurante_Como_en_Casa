-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 06-11-2026 a las 07:03:26
-- Versión del servidor: 10.4.32-MariaDB
-- Versión de PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `como_en_casa`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `clientes`
--

CREATE TABLE `clientes` (
  `id` int(10) UNSIGNED NOT NULL,
  `nombre` varchar(120) NOT NULL,
  `documento` varchar(20) DEFAULT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `direccion` varchar(150) DEFAULT NULL,
  `creado_en` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `clientes`
--

INSERT INTO `clientes` (`id`, `nombre`, `documento`, `telefono`, `direccion`, `creado_en`) VALUES
(1, 'Consumidor final', NULL, NULL, NULL, '2026-11-05 22:28:33'),
(2, 'Ana Torres', '45678912', '987654321', 'Av. Lima 245', '2026-11-05 22:28:33'),
(3, 'Carlos Rojas', '12345678', '912345678', 'Jr. Puno 118', '2026-11-05 22:28:33'),
(4, 'Maria Sanchez', '87654321', '998877665', 'Calle Real 450', '2026-11-05 22:28:33'),
(5, 'Jose Ramirez', '11223344', '955443322', 'Av. Pajan 900', '2026-11-05 22:28:33'),
(6, 'Lucia Fernandez', '55667788', '966112233', 'Jr. Arequipa 322', '2026-11-05 22:28:33');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `compras`
--

CREATE TABLE `compras` (
  `id` int(10) UNSIGNED NOT NULL,
  `fecha` date NOT NULL,
  `proveedor` varchar(120) NOT NULL,
  `documento` varchar(50) DEFAULT NULL,
  `total` decimal(10,2) NOT NULL DEFAULT 0.00,
  `usuario_id` int(10) UNSIGNED DEFAULT NULL,
  `creado_en` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `compras`
--

INSERT INTO `compras` (`id`, `fecha`, `proveedor`, `documento`, `total`, `usuario_id`, `creado_en`) VALUES
(1, '2026-10-30', 'Mercado Mayorista', 'F001-245', 217.00, 4, '2026-11-05 22:28:33'),
(2, '2026-11-03', 'Distribuidora San Martin', 'F002-118', 246.50, 4, '2026-11-05 22:28:33');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `detalle_compra`
--

CREATE TABLE `detalle_compra` (
  `id` int(10) UNSIGNED NOT NULL,
  `compra_id` int(10) UNSIGNED NOT NULL,
  `insumo_id` int(10) UNSIGNED NOT NULL,
  `cantidad` decimal(12,3) NOT NULL,
  `costo_unitario` decimal(10,2) NOT NULL DEFAULT 0.00,
  `subtotal` decimal(10,2) NOT NULL DEFAULT 0.00
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `detalle_compra`
--

INSERT INTO `detalle_compra` (`id`, `compra_id`, `insumo_id`, `cantidad`, `costo_unitario`, `subtotal`) VALUES
(1, 1, 1, 10.000, 14.50, 145.00),
(2, 1, 4, 20.000, 2.80, 56.00),
(3, 1, 5, 5.000, 3.20, 16.00),
(4, 2, 3, 25.000, 4.50, 112.50),
(5, 2, 7, 10.000, 9.80, 98.00),
(6, 2, 10, 60.000, 0.60, 36.00);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `detalle_venta`
--

CREATE TABLE `detalle_venta` (
  `id` int(10) UNSIGNED NOT NULL,
  `venta_id` int(10) UNSIGNED NOT NULL,
  `producto_id` int(10) UNSIGNED NOT NULL,
  `cantidad` int(10) UNSIGNED NOT NULL,
  `precio` decimal(10,2) NOT NULL DEFAULT 0.00,
  `subtotal` decimal(10,2) NOT NULL DEFAULT 0.00
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `detalle_venta`
--

INSERT INTO `detalle_venta` (`id`, `venta_id`, `producto_id`, `cantidad`, `precio`, `subtotal`) VALUES
(1, 1, 5, 1, 22.00, 22.00),
(2, 1, 13, 2, 10.00, 20.00),
(3, 2, 6, 2, 18.00, 36.00),
(4, 2, 14, 2, 9.00, 18.00),
(5, 2, 18, 1, 8.00, 8.00),
(6, 3, 9, 2, 19.00, 38.00),
(7, 3, 15, 2, 5.00, 10.00),
(8, 3, 20, 2, 7.00, 14.00),
(9, 4, 5, 2, 22.00, 44.00),
(10, 4, 3, 2, 15.00, 30.00),
(11, 4, 13, 1, 10.00, 10.00),
(12, 4, 18, 2, 8.00, 16.00),
(13, 5, 7, 1, 16.00, 16.00),
(14, 5, 10, 1, 21.00, 21.00),
(15, 5, 16, 1, 3.00, 3.00),
(16, 5, 19, 1, 7.00, 7.00),
(17, 6, 8, 1, 20.00, 20.00),
(18, 6, 11, 1, 17.00, 17.00),
(19, 6, 12, 1, 25.00, 25.00),
(20, 6, 14, 1, 9.00, 9.00),
(21, 6, 21, 1, 8.00, 8.00),
(22, 7, 5, 1, 22.00, 22.00),
(23, 7, 13, 2, 10.00, 20.00),
(24, 7, 18, 1, 8.00, 8.00),
(25, 8, 9, 1, 19.00, 19.00),
(26, 8, 15, 1, 5.00, 5.00),
(27, 8, 17, 1, 4.00, 4.00),
(28, 9, 2, 2, 10.00, 20.00),
(29, 9, 6, 1, 18.00, 18.00),
(30, 9, 19, 1, 7.00, 7.00),
(31, 10, 4, 1, 14.00, 14.00),
(32, 10, 10, 1, 21.00, 21.00),
(33, 10, 16, 2, 3.00, 6.00),
(34, 10, 20, 1, 7.00, 7.00);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `gastos`
--

CREATE TABLE `gastos` (
  `id` int(10) UNSIGNED NOT NULL,
  `fecha` date NOT NULL,
  `concepto` varchar(150) NOT NULL,
  `categoria` varchar(60) NOT NULL DEFAULT 'operativo',
  `monto` decimal(10,2) NOT NULL,
  `comprobante` varchar(60) DEFAULT NULL,
  `usuario_id` int(10) UNSIGNED DEFAULT NULL,
  `creado_en` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `gastos`
--

INSERT INTO `gastos` (`id`, `fecha`, `concepto`, `categoria`, `monto`, `comprobante`, `usuario_id`, `creado_en`) VALUES
(1, '2026-10-30', 'Compra de verduras', 'insumos', 85.00, 'B001-120', 1, '2026-11-05 22:28:33'),
(2, '2026-10-31', 'Recibo de luz', 'servicios', 120.00, 'L-0325', 1, '2026-11-05 22:28:33'),
(3, '2026-11-01', 'Balon de gas', 'servicios', 95.00, 'G-8842', 1, '2026-11-05 22:28:33'),
(4, '2026-11-02', 'Alquiler del local', 'alquiler', 800.00, 'A-0012', 1, '2026-11-05 22:28:33'),
(5, '2026-11-03', 'Internet y telefonia', 'servicios', 70.00, 'I-4410', 1, '2026-11-05 22:28:33'),
(6, '2026-11-04', 'Articulos de limpieza', 'operativo', 45.00, 'L-2210', 1, '2026-11-05 22:28:33'),
(7, '2026-11-05', 'Compra de carne y pollo', 'insumos', 150.00, 'B001-131', 1, '2026-11-05 22:28:33');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `insumos`
--

CREATE TABLE `insumos` (
  `id` int(10) UNSIGNED NOT NULL,
  `nombre` varchar(120) NOT NULL,
  `unidad` varchar(20) NOT NULL DEFAULT 'unidad',
  `costo` decimal(10,2) NOT NULL DEFAULT 0.00,
  `stock_actual` decimal(12,3) NOT NULL DEFAULT 0.000,
  `stock_minimo` decimal(12,3) NOT NULL DEFAULT 0.000,
  `creado_en` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `insumos`
--

INSERT INTO `insumos` (`id`, `nombre`, `unidad`, `costo`, `stock_actual`, `stock_minimo`, `creado_en`) VALUES
(1, 'Pollo', 'kg', 14.50, 22.000, 10.000, '2026-11-05 22:28:33'),
(2, 'Carne de res', 'kg', 32.00, 8.000, 5.000, '2026-11-05 22:28:33'),
(3, 'Arroz', 'kg', 4.50, 55.000, 20.000, '2026-11-05 22:28:33'),
(4, 'Papa', 'kg', 2.80, 45.000, 15.000, '2026-11-05 22:28:33'),
(5, 'Cebolla', 'kg', 3.20, 13.000, 5.000, '2026-11-05 22:28:33'),
(6, 'Tomate', 'kg', 3.50, 6.000, 8.000, '2026-11-05 22:28:33'),
(7, 'Aceite', 'lt', 9.80, 22.000, 8.000, '2026-11-05 22:28:33'),
(8, 'Aji amarillo', 'kg', 8.00, 4.000, 5.000, '2026-11-05 22:28:33'),
(9, 'Harina', 'kg', 4.00, 10.000, 5.000, '2026-11-05 22:28:33'),
(10, 'Huevos', 'unidad', 0.60, 140.000, 30.000, '2026-11-05 22:28:33'),
(11, 'Leche', 'lt', 4.80, 15.000, 10.000, '2026-11-05 22:28:33'),
(12, 'Azucar', 'kg', 4.20, 12.000, 5.000, '2026-11-05 22:28:33'),
(13, 'Limon', 'kg', 5.00, 7.000, 8.000, '2026-11-05 22:28:33'),
(14, 'Gaseosa personal', 'unidad', 3.00, 24.000, 12.000, '2026-11-05 22:28:33');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `menus_dia`
--

CREATE TABLE `menus_dia` (
  `id` int(10) UNSIGNED NOT NULL,
  `fecha` date NOT NULL,
  `precio` decimal(10,2) NOT NULL DEFAULT 0.00,
  `cantidad_preparada` int(10) UNSIGNED NOT NULL DEFAULT 0,
  `cantidad_vendida` int(10) UNSIGNED NOT NULL DEFAULT 0,
  `estado` enum('borrador','publicado','cerrado') NOT NULL DEFAULT 'publicado',
  `observacion` varchar(255) DEFAULT NULL,
  `usuario_id` int(10) UNSIGNED DEFAULT NULL,
  `creado_en` timestamp NOT NULL DEFAULT current_timestamp(),
  `actualizado_en` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `menu_dia_items`
--

CREATE TABLE `menu_dia_items` (
  `id` int(10) UNSIGNED NOT NULL,
  `menu_dia_id` int(10) UNSIGNED NOT NULL,
  `tipo` enum('entrada','segundo','refresco') NOT NULL,
  `nombre` varchar(120) NOT NULL,
  `producto_id` int(10) UNSIGNED DEFAULT NULL,
  `precio` decimal(10,2) NOT NULL DEFAULT 0.00,
  `cantidad_preparada` int(10) UNSIGNED NOT NULL DEFAULT 0,
  `cantidad_vendida` int(10) UNSIGNED NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `menu_dia_ventas`
--

CREATE TABLE `menu_dia_ventas` (
  `id` int(10) UNSIGNED NOT NULL,
  `menu_dia_id` int(10) UNSIGNED NOT NULL,
  `menu_item_id` int(10) UNSIGNED DEFAULT NULL,
  `venta_id` int(10) UNSIGNED NOT NULL,
  `tipo_item` enum('menu','entrada','segundo','refresco') NOT NULL,
  `cantidad` int(10) UNSIGNED NOT NULL,
  `precio` decimal(10,2) NOT NULL DEFAULT 0.00,
  `subtotal` decimal(10,2) NOT NULL DEFAULT 0.00,
  `creado_en` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `movimientos_inventario`
--

CREATE TABLE `movimientos_inventario` (
  `id` int(10) UNSIGNED NOT NULL,
  `insumo_id` int(10) UNSIGNED NOT NULL,
  `tipo` enum('entrada','salida') NOT NULL,
  `cantidad` decimal(12,3) NOT NULL,
  `costo_unitario` decimal(10,2) NOT NULL DEFAULT 0.00,
  `motivo` varchar(150) DEFAULT NULL,
  `usuario_id` int(10) UNSIGNED DEFAULT NULL,
  `fecha` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `movimientos_inventario`
--

INSERT INTO `movimientos_inventario` (`id`, `insumo_id`, `tipo`, `cantidad`, `costo_unitario`, `motivo`, `usuario_id`, `fecha`) VALUES
(1, 1, 'entrada', 10.000, 14.50, 'Compra F001-245', 4, '2026-10-30 22:28:33'),
(2, 4, 'entrada', 20.000, 2.80, 'Compra F001-245', 4, '2026-10-30 22:28:33'),
(3, 5, 'entrada', 5.000, 3.20, 'Compra F001-245', 4, '2026-10-30 22:28:33'),
(4, 3, 'entrada', 25.000, 4.50, 'Compra F002-118', 4, '2026-11-03 22:28:33'),
(5, 7, 'entrada', 10.000, 9.80, 'Compra F002-118', 4, '2026-11-03 22:28:33'),
(6, 10, 'entrada', 60.000, 0.60, 'Compra F002-118', 4, '2026-11-03 22:28:33');

--
-- Disparadores `movimientos_inventario`
--
DELIMITER $$
CREATE TRIGGER `trg_movimiento_stock` AFTER INSERT ON `movimientos_inventario` FOR EACH ROW UPDATE insumos
   SET stock_actual = stock_actual + IF(NEW.tipo = 'entrada', NEW.cantidad, -NEW.cantidad)
 WHERE id = NEW.insumo_id
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `pagos_cliente`
--

CREATE TABLE `pagos_cliente` (
  `id` int(10) UNSIGNED NOT NULL,
  `venta_id` int(10) UNSIGNED NOT NULL,
  `fecha` datetime NOT NULL DEFAULT current_timestamp(),
  `monto` decimal(10,2) NOT NULL,
  `medio` enum('efectivo','yape','tarjeta') NOT NULL DEFAULT 'efectivo',
  `referencia` varchar(60) DEFAULT NULL,
  `usuario_id` int(10) UNSIGNED DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `pagos_cliente`
--

INSERT INTO `pagos_cliente` (`id`, `venta_id`, `fecha`, `monto`, `medio`, `referencia`, `usuario_id`) VALUES
(1, 1, '2026-10-30 17:28:33', 42.00, 'efectivo', NULL, 2),
(2, 2, '2026-10-31 17:28:33', 40.00, 'yape', 'YP-1024', 2),
(3, 2, '2026-10-31 17:28:33', 22.00, 'efectivo', NULL, 2),
(4, 3, '2026-11-01 17:28:33', 62.00, 'yape', 'YP-1031', 2),
(5, 4, '2026-11-02 17:28:33', 100.00, 'efectivo', NULL, 2),
(6, 5, '2026-11-03 17:28:33', 47.00, 'yape', 'YP-1047', 2),
(7, 6, '2026-11-04 17:28:33', 50.00, 'efectivo', NULL, 2),
(8, 6, '2026-11-04 17:28:33', 29.00, 'yape', 'YP-1052', 2),
(9, 7, '2026-11-05 17:28:33', 50.00, 'yape', 'YP-1060', 2),
(10, 10, '2026-11-05 17:28:33', 48.00, 'efectivo', NULL, 2);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `pagos_personal`
--

CREATE TABLE `pagos_personal` (
  `id` int(10) UNSIGNED NOT NULL,
  `personal_id` int(10) UNSIGNED NOT NULL,
  `fecha` date NOT NULL,
  `monto` decimal(10,2) NOT NULL,
  `concepto` varchar(120) NOT NULL DEFAULT 'Sueldo',
  `usuario_id` int(10) UNSIGNED DEFAULT NULL,
  `creado_en` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `pagos_personal`
--

INSERT INTO `pagos_personal` (`id`, `personal_id`, `fecha`, `monto`, `concepto`, `usuario_id`, `creado_en`) VALUES
(1, 2, '2026-10-31', 250.00, 'Adelanto de sueldo', 1, '2026-11-05 22:28:33'),
(2, 1, '2026-11-02', 1200.00, 'Sueldo mensual', 1, '2026-11-05 22:28:33'),
(3, 3, '2026-11-04', 80.00, 'Adelanto de sueldo', 1, '2026-11-05 22:28:33');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `personal`
--

CREATE TABLE `personal` (
  `id` int(10) UNSIGNED NOT NULL,
  `nombre` varchar(120) NOT NULL,
  `documento` varchar(20) DEFAULT NULL,
  `cargo` varchar(60) NOT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `sueldo` decimal(10,2) NOT NULL DEFAULT 0.00,
  `fecha_ingreso` date DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT 1,
  `creado_en` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `personal`
--

INSERT INTO `personal` (`id`, `nombre`, `documento`, `cargo`, `telefono`, `sueldo`, `fecha_ingreso`, `activo`, `creado_en`) VALUES
(1, 'Juan Perez', '11223344', 'Cocinero', '987111222', 1200.00, '2024-03-01', 1, '2026-11-05 22:28:33'),
(2, 'Maria Lopez', '22334455', 'Cajera', '987222333', 1100.00, '2024-05-15', 1, '2026-11-05 22:28:33'),
(3, 'Pedro Ramirez', '33445566', 'Ayudante de cocina', '987333444', 950.00, '2025-01-10', 1, '2026-11-05 22:28:33'),
(4, 'Rosa Torres', '44556677', 'Mesera', '987444555', 1000.00, '2024-08-20', 1, '2026-11-05 22:28:33'),
(5, 'Luis Mendoza', '55667788', 'Administrador', '987555666', 1800.00, '2023-01-05', 1, '2026-11-05 22:28:33');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `productos`
--

CREATE TABLE `productos` (
  `id` int(10) UNSIGNED NOT NULL,
  `nombre` varchar(120) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `categoria` enum('entradas','platos','bebidas','postres') NOT NULL DEFAULT 'platos',
  `precio` decimal(10,2) NOT NULL DEFAULT 0.00,
  `estado` enum('disponible','agotado','inactivo') NOT NULL DEFAULT 'disponible',
  `creado_en` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `productos`
--

INSERT INTO `productos` (`id`, `nombre`, `descripcion`, `categoria`, `precio`, `estado`, `creado_en`) VALUES
(1, 'Causa limena', 'Papa amarilla con aji y pollo', 'entradas', 12.00, 'disponible', '2026-11-05 22:28:33'),
(2, 'Papa a la huancaina', 'Papa sancochada con crema de aji', 'entradas', 10.00, 'disponible', '2026-11-05 22:28:33'),
(3, 'Anticuchos', 'Brochetas de corazon a la parrilla', 'entradas', 15.00, 'disponible', '2026-11-05 22:28:33'),
(4, 'Ensalada cesar', 'Lechuga, pollo, croutones y parmesano', 'entradas', 14.00, 'disponible', '2026-11-05 22:28:33'),
(5, 'Lomo saltado', 'Lomo de res salteado con papas fritas', 'platos', 22.00, 'disponible', '2026-11-05 22:28:33'),
(6, 'Aji de gallina', 'Pollo deshilachado en crema de aji', 'platos', 18.00, 'disponible', '2026-11-05 22:28:33'),
(7, 'Arroz con pollo', 'Arroz verde con presa de pollo', 'platos', 16.00, 'disponible', '2026-11-05 22:28:33'),
(8, 'Tallarin saltado', 'Tallarin de res al estilo chifa', 'platos', 20.00, 'disponible', '2026-11-05 22:28:33'),
(9, 'Pollo a la brasa 1/4', 'Cuarto de pollo con papas y ensalada', 'platos', 19.00, 'disponible', '2026-11-05 22:28:33'),
(10, 'Seco de res', 'Guiso de res con arroz y frijoles', 'platos', 21.00, 'disponible', '2026-11-05 22:28:33'),
(11, 'Chaufa de pollo', 'Arroz chaufa con pollo y huevo', 'platos', 17.00, 'disponible', '2026-11-05 22:28:33'),
(12, 'Pescado a la plancha', 'Filete de pescado con guarnicion', 'platos', 25.00, 'disponible', '2026-11-05 22:28:33'),
(13, 'Chicha morada (jarra)', 'Jarra de 1 litro', 'bebidas', 10.00, 'disponible', '2026-11-05 22:28:33'),
(14, 'Limonada frozen', 'Vaso de 500 ml', 'bebidas', 9.00, 'disponible', '2026-11-05 22:28:33'),
(15, 'Inca Kola', 'Botella personal', 'bebidas', 5.00, 'disponible', '2026-11-05 22:28:33'),
(16, 'Agua mineral', 'Botella de 625 ml', 'bebidas', 3.00, 'disponible', '2026-11-05 22:28:33'),
(17, 'Cafe pasado', 'Taza de cafe', 'bebidas', 4.00, 'disponible', '2026-11-05 22:28:33'),
(18, 'Suspiro a la limena', 'Postre tradicional', 'postres', 8.00, 'disponible', '2026-11-05 22:28:33'),
(19, 'Arroz con leche', 'Porcion individual', 'postres', 7.00, 'disponible', '2026-11-05 22:28:33'),
(20, 'Mazamorra morada', 'Porcion individual', 'postres', 7.00, 'disponible', '2026-11-05 22:28:33'),
(21, 'Tortilla de raya', 'Postre casero', 'postres', 8.00, 'disponible', '2026-11-05 22:28:33');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `tokens`
--

CREATE TABLE `tokens` (
  `id` int(10) UNSIGNED NOT NULL,
  `usuario_id` int(10) UNSIGNED NOT NULL,
  `token` char(64) NOT NULL,
  `expira` datetime NOT NULL,
  `creado_en` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `tokens`
--

INSERT INTO `tokens` (`id`, `usuario_id`, `token`, `expira`, `creado_en`) VALUES
(1, 1, 'ec8fa407ec9d39489bc50ea0b17e038f532f53629f2d09fce43f66adc38af7a2', '2026-11-06 08:09:26', '2026-11-06 01:09:26'),
(2, 1, 'dd3bd4aca9feb651a448c5087143a29749d5b8a85336fc61831b664b9e204333', '2026-11-06 10:43:05', '2026-11-06 03:43:05');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuarios`
--

CREATE TABLE `usuarios` (
  `id` int(10) UNSIGNED NOT NULL,
  `nombres` varchar(120) NOT NULL,
  `usuario` varchar(50) NOT NULL,
  `clave_hash` varchar(255) NOT NULL,
  `rol` enum('administrador','cajero','cocina','inventario') NOT NULL DEFAULT 'cajero',
  `activo` tinyint(1) NOT NULL DEFAULT 1,
  `creado_en` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `usuarios`
--

INSERT INTO `usuarios` (`id`, `nombres`, `usuario`, `clave_hash`, `rol`, `activo`, `creado_en`) VALUES
(1, 'Administrador General', 'admin', '$2y$10$0NKhqbt51hgYclqtZe/UJO.6bCqkJFBQiovGkgnJEyjPS7D8IwZL2', 'administrador', 1, '2026-11-05 22:28:33'),
(2, 'Maria Lopez', 'cajero', '$2y$10$EM2IxuewNO7Ezm5L3FsluOpwTn.MURNUKAnletoamwbYuno6rKjs.', 'cajero', 1, '2026-11-05 22:28:33'),
(3, 'Juan Perez', 'cocina', '$2y$10$GErI1WCIgl5m3L.MIt3Yk.MpMk3v6n.JnN6yeom97IqEY2wUNQU9G', 'cocina', 1, '2026-11-05 22:28:33'),
(4, 'Rosa Torres', 'inventario', '$2y$10$9hG5sP8scmEwzdGi904LPuXtr5hpX8yzSw2PPFP6ToRShH2lA8A0.', 'inventario', 1, '2026-11-05 22:28:33');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `ventas`
--

CREATE TABLE `ventas` (
  `id` int(10) UNSIGNED NOT NULL,
  `numero` varchar(20) DEFAULT NULL,
  `idempotencia` varchar(64) DEFAULT NULL,
  `cliente_id` int(10) UNSIGNED DEFAULT NULL,
  `usuario_id` int(10) UNSIGNED DEFAULT NULL,
  `fecha` datetime NOT NULL DEFAULT current_timestamp(),
  `subtotal` decimal(10,2) NOT NULL DEFAULT 0.00,
  `descuento` decimal(10,2) NOT NULL DEFAULT 0.00,
  `total` decimal(10,2) NOT NULL DEFAULT 0.00,
  `estado` enum('registrada','en_preparacion','lista','entregada','pagada','cerrada','anulada') NOT NULL DEFAULT 'registrada',
  `tipo_servicio` enum('mesa','llevar','delivery') NOT NULL DEFAULT 'mesa',
  `mesa` varchar(10) DEFAULT NULL,
  `observacion` varchar(255) DEFAULT NULL,
  `creado_en` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `ventas`
--

INSERT INTO `ventas` (`id`, `numero`, `idempotencia`, `cliente_id`, `usuario_id`, `fecha`, `subtotal`, `descuento`, `total`, `estado`, `tipo_servicio`, `mesa`, `observacion`, `creado_en`) VALUES
(1, 'V-000001', NULL, 1, 2, '2026-10-30 17:28:33', 42.00, 0.00, 42.00, 'cerrada', 'mesa', '1', NULL, '2026-11-05 22:28:33'),
(2, 'V-000002', NULL, 2, 2, '2026-10-31 17:28:33', 62.00, 0.00, 62.00, 'cerrada', 'llevar', NULL, 'Sin cebolla', '2026-11-05 22:28:33'),
(3, 'V-000003', NULL, 3, 2, '2026-11-01 17:28:33', 62.00, 0.00, 62.00, 'cerrada', 'mesa', '2', NULL, '2026-11-05 22:28:33'),
(4, 'V-000004', NULL, 4, 2, '2026-11-02 17:28:33', 100.00, 0.00, 100.00, 'cerrada', 'mesa', '3', 'Cumpleanos', '2026-11-05 22:28:33'),
(5, 'V-000005', NULL, 5, 2, '2026-11-03 17:28:33', 47.00, 0.00, 47.00, 'cerrada', 'delivery', NULL, 'Av. Pajan 900', '2026-11-05 22:28:33'),
(6, 'V-000006', NULL, 1, 2, '2026-11-04 17:28:33', 79.00, 0.00, 79.00, 'cerrada', 'mesa', '1', NULL, '2026-11-05 22:28:33'),
(7, 'V-000007', NULL, 2, 2, '2026-11-05 17:28:33', 50.00, 0.00, 50.00, 'pagada', 'mesa', '4', NULL, '2026-11-05 22:28:33'),
(8, 'V-000008', NULL, 3, 2, '2026-11-05 17:28:33', 28.00, 0.00, 28.00, 'en_preparacion', 'llevar', NULL, NULL, '2026-11-05 22:28:33'),
(9, 'V-000009', NULL, 4, 2, '2026-11-05 17:28:33', 45.00, 0.00, 45.00, 'registrada', 'mesa', '5', NULL, '2026-11-05 22:28:33'),
(10, 'V-000010', NULL, 5, 2, '2026-11-05 17:28:33', 48.00, 0.00, 48.00, 'lista', 'mesa', '2', NULL, '2026-11-05 22:28:33');

-- --------------------------------------------------------

--
-- Estructura Stand-in para la vista `v_ganancias_diarias`
-- (Véase abajo para la vista actual)
--
CREATE TABLE `v_ganancias_diarias` (
`fecha` date
,`ingresos` decimal(32,2)
,`egresos` decimal(32,2)
,`ganancia` decimal(32,2)
);

-- --------------------------------------------------------

--
-- Estructura Stand-in para la vista `v_ingresos_por_medio`
-- (Véase abajo para la vista actual)
--
CREATE TABLE `v_ingresos_por_medio` (
`fecha` date
,`medio` enum('efectivo','yape','tarjeta')
,`cantidad` bigint(21)
,`total` decimal(32,2)
);

-- --------------------------------------------------------

--
-- Estructura Stand-in para la vista `v_menu_dia_ventas`
-- (Véase abajo para la vista actual)
--
CREATE TABLE `v_menu_dia_ventas` (
`id` int(10) unsigned
,`venta_id` int(10) unsigned
,`numero` varchar(20)
,`fecha` datetime
,`estado_venta` enum('registrada','en_preparacion','lista','entregada','pagada','cerrada','anulada')
,`menu_dia_id` int(10) unsigned
,`menu_fecha` date
,`tipo_item` enum('menu','entrada','segundo','refresco')
,`cantidad` int(10) unsigned
,`precio` decimal(10,2)
,`subtotal` decimal(10,2)
);

-- --------------------------------------------------------

--
-- Estructura Stand-in para la vista `v_movimientos_economicos`
-- (Véase abajo para la vista actual)
--
CREATE TABLE `v_movimientos_economicos` (
`tipo` varchar(7)
,`fecha` date
,`concepto` varchar(150)
,`monto` decimal(10,2)
);

-- --------------------------------------------------------

--
-- Estructura Stand-in para la vista `v_stock_bajo`
-- (Véase abajo para la vista actual)
--
CREATE TABLE `v_stock_bajo` (
`id` int(10) unsigned
,`nombre` varchar(120)
,`unidad` varchar(20)
,`stock_actual` decimal(12,3)
,`stock_minimo` decimal(12,3)
);

-- --------------------------------------------------------

--
-- Estructura Stand-in para la vista `v_ventas_detalle`
-- (Véase abajo para la vista actual)
--
CREATE TABLE `v_ventas_detalle` (
`venta_id` int(10) unsigned
,`numero` varchar(20)
,`fecha` datetime
,`estado` enum('registrada','en_preparacion','lista','entregada','pagada','cerrada','anulada')
,`producto_id` int(10) unsigned
,`producto` varchar(120)
,`categoria` enum('entradas','platos','bebidas','postres')
,`cantidad` int(10) unsigned
,`precio` decimal(10,2)
,`subtotal` decimal(10,2)
);

-- --------------------------------------------------------

--
-- Estructura para la vista `v_ganancias_diarias`
--
DROP TABLE IF EXISTS `v_ganancias_diarias`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `v_ganancias_diarias`  AS SELECT `v_movimientos_economicos`.`fecha` AS `fecha`, sum(case when `v_movimientos_economicos`.`tipo` = 'ingreso' then `v_movimientos_economicos`.`monto` else 0 end) AS `ingresos`, sum(case when `v_movimientos_economicos`.`tipo` = 'egreso' then `v_movimientos_economicos`.`monto` else 0 end) AS `egresos`, sum(case when `v_movimientos_economicos`.`tipo` = 'ingreso' then `v_movimientos_economicos`.`monto` else -`v_movimientos_economicos`.`monto` end) AS `ganancia` FROM `v_movimientos_economicos` GROUP BY `v_movimientos_economicos`.`fecha` ;

-- --------------------------------------------------------

--
-- Estructura para la vista `v_ingresos_por_medio`
--
DROP TABLE IF EXISTS `v_ingresos_por_medio`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `v_ingresos_por_medio`  AS SELECT cast(`pagos_cliente`.`fecha` as date) AS `fecha`, `pagos_cliente`.`medio` AS `medio`, count(0) AS `cantidad`, sum(`pagos_cliente`.`monto`) AS `total` FROM `pagos_cliente` GROUP BY cast(`pagos_cliente`.`fecha` as date), `pagos_cliente`.`medio` ;

-- --------------------------------------------------------

--
-- Estructura para la vista `v_menu_dia_ventas`
--
DROP TABLE IF EXISTS `v_menu_dia_ventas`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `v_menu_dia_ventas`  AS SELECT `mdv`.`id` AS `id`, `mdv`.`venta_id` AS `venta_id`, `v`.`numero` AS `numero`, `v`.`fecha` AS `fecha`, `v`.`estado` AS `estado_venta`, `mdv`.`menu_dia_id` AS `menu_dia_id`, `md`.`fecha` AS `menu_fecha`, `mdv`.`tipo_item` AS `tipo_item`, `mdv`.`cantidad` AS `cantidad`, `mdv`.`precio` AS `precio`, `mdv`.`subtotal` AS `subtotal` FROM ((`menu_dia_ventas` `mdv` join `ventas` `v` on(`v`.`id` = `mdv`.`venta_id`)) join `menus_dia` `md` on(`md`.`id` = `mdv`.`menu_dia_id`)) ;

-- --------------------------------------------------------

--
-- Estructura para la vista `v_movimientos_economicos`
--
DROP TABLE IF EXISTS `v_movimientos_economicos`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `v_movimientos_economicos`  AS SELECT 'ingreso' AS `tipo`, cast(`p`.`fecha` as date) AS `fecha`, concat('Pago de venta ',`v`.`numero`) AS `concepto`, `p`.`monto` AS `monto` FROM (`pagos_cliente` `p` join `ventas` `v` on(`v`.`id` = `p`.`venta_id`)) WHERE `v`.`estado` <> 'anulada'union all select 'egreso' AS `egreso`,`g`.`fecha` AS `fecha`,`g`.`concepto` AS `concepto`,`g`.`monto` AS `monto` from `gastos` `g` union all select 'egreso' AS `egreso`,`pp`.`fecha` AS `fecha`,concat('Pago al personal: ',`pe`.`nombre`) AS `CONCAT('Pago al personal: ', pe.nombre)`,`pp`.`monto` AS `monto` from (`pagos_personal` `pp` join `personal` `pe` on(`pe`.`id` = `pp`.`personal_id`)) union all select 'egreso' AS `egreso`,`c`.`fecha` AS `fecha`,concat('Compra a ',`c`.`proveedor`) AS `CONCAT('Compra a ', c.proveedor)`,`c`.`total` AS `total` from `compras` `c`  ;

-- --------------------------------------------------------

--
-- Estructura para la vista `v_stock_bajo`
--
DROP TABLE IF EXISTS `v_stock_bajo`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `v_stock_bajo`  AS SELECT `insumos`.`id` AS `id`, `insumos`.`nombre` AS `nombre`, `insumos`.`unidad` AS `unidad`, `insumos`.`stock_actual` AS `stock_actual`, `insumos`.`stock_minimo` AS `stock_minimo` FROM `insumos` WHERE `insumos`.`stock_actual` <= `insumos`.`stock_minimo` ;

-- --------------------------------------------------------

--
-- Estructura para la vista `v_ventas_detalle`
--
DROP TABLE IF EXISTS `v_ventas_detalle`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `v_ventas_detalle`  AS SELECT `v`.`id` AS `venta_id`, `v`.`numero` AS `numero`, `v`.`fecha` AS `fecha`, `v`.`estado` AS `estado`, `p`.`id` AS `producto_id`, `p`.`nombre` AS `producto`, `p`.`categoria` AS `categoria`, `dv`.`cantidad` AS `cantidad`, `dv`.`precio` AS `precio`, `dv`.`subtotal` AS `subtotal` FROM ((`ventas` `v` join `detalle_venta` `dv` on(`dv`.`venta_id` = `v`.`id`)) join `productos` `p` on(`p`.`id` = `dv`.`producto_id`)) ;

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `clientes`
--
ALTER TABLE `clientes`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_clientes_nombre` (`nombre`);

--
-- Indices de la tabla `compras`
--
ALTER TABLE `compras`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_compras_usuario` (`usuario_id`),
  ADD KEY `idx_compras_fecha` (`fecha`);

--
-- Indices de la tabla `detalle_compra`
--
ALTER TABLE `detalle_compra`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_dcompra_insumo` (`insumo_id`),
  ADD KEY `idx_dcompra_compra` (`compra_id`);

--
-- Indices de la tabla `detalle_venta`
--
ALTER TABLE `detalle_venta`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_dventa_venta` (`venta_id`),
  ADD KEY `idx_dventa_producto` (`producto_id`);

--
-- Indices de la tabla `gastos`
--
ALTER TABLE `gastos`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_gastos_usuario` (`usuario_id`),
  ADD KEY `idx_gastos_fecha` (`fecha`);

--
-- Indices de la tabla `insumos`
--
ALTER TABLE `insumos`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_insumos_nombre` (`nombre`);

--
-- Indices de la tabla `menus_dia`
--
ALTER TABLE `menus_dia`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_menus_dia_fecha` (`fecha`),
  ADD KEY `idx_menus_dia_estado` (`estado`),
  ADD KEY `fk_menus_dia_usuario` (`usuario_id`);

--
-- Indices de la tabla `menu_dia_items`
--
ALTER TABLE `menu_dia_items`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_menu_dia_tipo` (`menu_dia_id`,`tipo`),
  ADD KEY `idx_menu_items_producto` (`producto_id`);

--
-- Indices de la tabla `menu_dia_ventas`
--
ALTER TABLE `menu_dia_ventas`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_mdv_menu` (`menu_dia_id`),
  ADD KEY `idx_mdv_venta` (`venta_id`),
  ADD KEY `idx_mdv_item` (`menu_item_id`);

--
-- Indices de la tabla `movimientos_inventario`
--
ALTER TABLE `movimientos_inventario`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_mov_usuario` (`usuario_id`),
  ADD KEY `idx_mov_fecha` (`fecha`),
  ADD KEY `idx_mov_insumo` (`insumo_id`);

--
-- Indices de la tabla `pagos_cliente`
--
ALTER TABLE `pagos_cliente`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_pago_usuario` (`usuario_id`),
  ADD KEY `idx_pago_fecha` (`fecha`),
  ADD KEY `idx_pago_venta` (`venta_id`);

--
-- Indices de la tabla `pagos_personal`
--
ALTER TABLE `pagos_personal`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_ppersonal_personal` (`personal_id`),
  ADD KEY `fk_ppersonal_usuario` (`usuario_id`),
  ADD KEY `idx_ppersonal_fecha` (`fecha`);

--
-- Indices de la tabla `personal`
--
ALTER TABLE `personal`
  ADD PRIMARY KEY (`id`);

--
-- Indices de la tabla `productos`
--
ALTER TABLE `productos`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_productos_categoria` (`categoria`),
  ADD KEY `idx_productos_nombre` (`nombre`);

--
-- Indices de la tabla `tokens`
--
ALTER TABLE `tokens`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `token` (`token`),
  ADD KEY `fk_tokens_usuario` (`usuario_id`);

--
-- Indices de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `usuario` (`usuario`);

--
-- Indices de la tabla `ventas`
--
ALTER TABLE `ventas`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `numero` (`numero`),
  ADD UNIQUE KEY `idempotencia` (`idempotencia`),
  ADD KEY `fk_ventas_cliente` (`cliente_id`),
  ADD KEY `fk_ventas_usuario` (`usuario_id`),
  ADD KEY `idx_ventas_fecha` (`fecha`),
  ADD KEY `idx_ventas_estado` (`estado`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `clientes`
--
ALTER TABLE `clientes`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT de la tabla `compras`
--
ALTER TABLE `compras`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de la tabla `detalle_compra`
--
ALTER TABLE `detalle_compra`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT de la tabla `detalle_venta`
--
ALTER TABLE `detalle_venta`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=35;

--
-- AUTO_INCREMENT de la tabla `gastos`
--
ALTER TABLE `gastos`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT de la tabla `insumos`
--
ALTER TABLE `insumos`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;

--
-- AUTO_INCREMENT de la tabla `menus_dia`
--
ALTER TABLE `menus_dia`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `menu_dia_items`
--
ALTER TABLE `menu_dia_items`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `menu_dia_ventas`
--
ALTER TABLE `menu_dia_ventas`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `movimientos_inventario`
--
ALTER TABLE `movimientos_inventario`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT de la tabla `pagos_cliente`
--
ALTER TABLE `pagos_cliente`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT de la tabla `pagos_personal`
--
ALTER TABLE `pagos_personal`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `personal`
--
ALTER TABLE `personal`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT de la tabla `productos`
--
ALTER TABLE `productos`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=22;

--
-- AUTO_INCREMENT de la tabla `tokens`
--
ALTER TABLE `tokens`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT de la tabla `ventas`
--
ALTER TABLE `ventas`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `compras`
--
ALTER TABLE `compras`
  ADD CONSTRAINT `fk_compras_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL;

--
-- Filtros para la tabla `detalle_compra`
--
ALTER TABLE `detalle_compra`
  ADD CONSTRAINT `fk_dcompra_compra` FOREIGN KEY (`compra_id`) REFERENCES `compras` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_dcompra_insumo` FOREIGN KEY (`insumo_id`) REFERENCES `insumos` (`id`);

--
-- Filtros para la tabla `detalle_venta`
--
ALTER TABLE `detalle_venta`
  ADD CONSTRAINT `fk_dventa_producto` FOREIGN KEY (`producto_id`) REFERENCES `productos` (`id`),
  ADD CONSTRAINT `fk_dventa_venta` FOREIGN KEY (`venta_id`) REFERENCES `ventas` (`id`) ON DELETE CASCADE;

--
-- Filtros para la tabla `gastos`
--
ALTER TABLE `gastos`
  ADD CONSTRAINT `fk_gastos_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL;

--
-- Filtros para la tabla `menus_dia`
--
ALTER TABLE `menus_dia`
  ADD CONSTRAINT `fk_menus_dia_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL;

--
-- Filtros para la tabla `menu_dia_items`
--
ALTER TABLE `menu_dia_items`
  ADD CONSTRAINT `fk_menu_items_menu` FOREIGN KEY (`menu_dia_id`) REFERENCES `menus_dia` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_menu_items_producto` FOREIGN KEY (`producto_id`) REFERENCES `productos` (`id`) ON DELETE SET NULL;

--
-- Filtros para la tabla `menu_dia_ventas`
--
ALTER TABLE `menu_dia_ventas`
  ADD CONSTRAINT `fk_mdv_item` FOREIGN KEY (`menu_item_id`) REFERENCES `menu_dia_items` (`id`) ON DELETE SET NULL,
  ADD CONSTRAINT `fk_mdv_menu` FOREIGN KEY (`menu_dia_id`) REFERENCES `menus_dia` (`id`),
  ADD CONSTRAINT `fk_mdv_venta` FOREIGN KEY (`venta_id`) REFERENCES `ventas` (`id`) ON DELETE CASCADE;

--
-- Filtros para la tabla `movimientos_inventario`
--
ALTER TABLE `movimientos_inventario`
  ADD CONSTRAINT `fk_mov_insumo` FOREIGN KEY (`insumo_id`) REFERENCES `insumos` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_mov_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL;

--
-- Filtros para la tabla `pagos_cliente`
--
ALTER TABLE `pagos_cliente`
  ADD CONSTRAINT `fk_pago_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL,
  ADD CONSTRAINT `fk_pago_venta` FOREIGN KEY (`venta_id`) REFERENCES `ventas` (`id`) ON DELETE CASCADE;

--
-- Filtros para la tabla `pagos_personal`
--
ALTER TABLE `pagos_personal`
  ADD CONSTRAINT `fk_ppersonal_personal` FOREIGN KEY (`personal_id`) REFERENCES `personal` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_ppersonal_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL;

--
-- Filtros para la tabla `tokens`
--
ALTER TABLE `tokens`
  ADD CONSTRAINT `fk_tokens_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE;

--
-- Filtros para la tabla `ventas`
--
ALTER TABLE `ventas`
  ADD CONSTRAINT `fk_ventas_cliente` FOREIGN KEY (`cliente_id`) REFERENCES `clientes` (`id`) ON DELETE SET NULL,
  ADD CONSTRAINT `fk_ventas_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
