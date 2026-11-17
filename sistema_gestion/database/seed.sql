-- =====================================================================
--  DATOS DE DEMOSTRACION - RESTAURANTE "COMO EN CASA"
--  Ejecutar despues de schema.sql
-- =====================================================================

USE como_en_casa;

-- ---------------------------------------------------------------------
--  USUARIOS (claves: admin123 / cajero123 / cocina123 / inventario123)
-- ---------------------------------------------------------------------
INSERT INTO usuarios (id, nombres, usuario, clave_hash, rol) VALUES
(1, 'Administrador General', 'admin',      '$2y$10$0NKhqbt51hgYclqtZe/UJO.6bCqkJFBQiovGkgnJEyjPS7D8IwZL2', 'administrador'),
(2, 'Maria Lopez',           'cajero',     '$2y$10$EM2IxuewNO7Ezm5L3FsluOpwTn.MURNUKAnletoamwbYuno6rKjs.', 'cajero'),
(3, 'Juan Perez',            'cocina',     '$2y$10$GErI1WCIgl5m3L.MIt3Yk.MpMk3v6n.JnN6yeom97IqEY2wUNQU9G', 'cocina'),
(4, 'Rosa Torres',           'inventario', '$2y$10$9hG5sP8scmEwzdGi904LPuXtr5hpX8yzSw2PPFP6ToRShH2lA8A0.', 'inventario');

-- ---------------------------------------------------------------------
--  CLIENTES
-- ---------------------------------------------------------------------
INSERT INTO clientes (id, nombre, documento, telefono, direccion) VALUES
(1, 'Consumidor final', NULL, NULL, NULL),
(2, 'Ana Torres', '45678912', '987654321', 'Av. Lima 245'),
(3, 'Carlos Rojas', '12345678', '912345678', 'Jr. Puno 118'),
(4, 'Maria Sanchez', '87654321', '998877665', 'Calle Real 450'),
(5, 'Jose Ramirez', '11223344', '955443322', 'Av. Pajan 900'),
(6, 'Lucia Fernandez', '55667788', '966112233', 'Jr. Arequipa 322');

-- ---------------------------------------------------------------------
--  PRODUCTOS / PLATOS
-- ---------------------------------------------------------------------
INSERT INTO productos (id, nombre, descripcion, categoria, precio) VALUES
(1,  'Causa limena',              'Papa amarilla con aji y pollo',            'entradas', 12.00),
(2,  'Papa a la huancaina',       'Papa sancochada con crema de aji',         'entradas', 10.00),
(3,  'Anticuchos',                'Brochetas de corazon a la parrilla',       'entradas', 15.00),
(4,  'Ensalada cesar',            'Lechuga, pollo, croutones y parmesano',    'entradas', 14.00),
(5,  'Lomo saltado',              'Lomo de res salteado con papas fritas',    'platos',   22.00),
(6,  'Aji de gallina',            'Pollo deshilachado en crema de aji',       'platos',   18.00),
(7,  'Arroz con pollo',           'Arroz verde con presa de pollo',           'platos',   16.00),
(8,  'Tallarin saltado',          'Tallarin de res al estilo chifa',          'platos',   20.00),
(9,  'Pollo a la brasa 1/4',      'Cuarto de pollo con papas y ensalada',     'platos',   19.00),
(10, 'Seco de res',               'Guiso de res con arroz y frijoles',        'platos',   21.00),
(11, 'Chaufa de pollo',           'Arroz chaufa con pollo y huevo',           'platos',   17.00),
(12, 'Pescado a la plancha',      'Filete de pescado con guarnicion',         'platos',   25.00),
(13, 'Chicha morada (jarra)',     'Jarra de 1 litro',                         'bebidas',  10.00),
(14, 'Limonada frozen',           'Vaso de 500 ml',                           'bebidas',   9.00),
(15, 'Inca Kola',                 'Botella personal',                         'bebidas',   5.00),
(16, 'Agua mineral',              'Botella de 625 ml',                        'bebidas',   3.00),
(17, 'Cafe pasado',               'Taza de cafe',                             'bebidas',   4.00),
(18, 'Suspiro a la limena',       'Postre tradicional',                       'postres',   8.00),
(19, 'Arroz con leche',           'Porcion individual',                       'postres',   7.00),
(20, 'Mazamorra morada',          'Porcion individual',                       'postres',   7.00),
(21, 'Tortilla de raya',          'Postre casero',                            'postres',   8.00);

-- ---------------------------------------------------------------------
--  INSUMOS
-- ---------------------------------------------------------------------
INSERT INTO insumos (id, nombre, unidad, costo, stock_actual, stock_minimo) VALUES
(1,  'Pollo',            'kg',     14.50, 12.000, 10.000),
(2,  'Carne de res',     'kg',     32.00,  8.000,  5.000),
(3,  'Arroz',            'kg',      4.50, 30.000, 20.000),
(4,  'Papa',             'kg',      2.80, 25.000, 15.000),
(5,  'Cebolla',          'kg',      3.20,  8.000,  5.000),
(6,  'Tomate',           'kg',      3.50,  6.000,  8.000),
(7,  'Aceite',           'lt',      9.80, 12.000,  8.000),
(8,  'Aji amarillo',     'kg',      8.00,  4.000,  5.000),
(9,  'Harina',           'kg',      4.00, 10.000,  5.000),
(10, 'Huevos',           'unidad',  0.60, 80.000, 30.000),
(11, 'Leche',            'lt',      4.80, 15.000, 10.000),
(12, 'Azucar',           'kg',      4.20, 12.000,  5.000),
(13, 'Limon',            'kg',      5.00,  7.000,  8.000),
(14, 'Gaseosa personal', 'unidad',  3.00, 24.000, 12.000);

-- ---------------------------------------------------------------------
--  PERSONAL
-- ---------------------------------------------------------------------
INSERT INTO personal (id, nombre, documento, cargo, telefono, sueldo, fecha_ingreso) VALUES
(1, 'Juan Perez',    '11223344', 'Cocinero',            '987111222', 1200.00, '2024-03-01'),
(2, 'Maria Lopez',   '22334455', 'Cajera',              '987222333', 1100.00, '2024-05-15'),
(3, 'Pedro Ramirez', '33445566', 'Ayudante de cocina',  '987333444',  950.00, '2025-01-10'),
(4, 'Rosa Torres',   '44556677', 'Mesera',              '987444555', 1000.00, '2024-08-20'),
(5, 'Luis Mendoza',  '55667788', 'Administrador',       '987555666', 1800.00, '2023-01-05');

-- ---------------------------------------------------------------------
--  COMPRAS Y MOVIMIENTOS DE INVENTARIO
-- ---------------------------------------------------------------------
INSERT INTO compras (id, fecha, proveedor, documento, total, usuario_id) VALUES
(1, DATE_SUB(CURDATE(), INTERVAL 6 DAY), 'Mercado Mayorista', 'F001-245', 217.00, 4),
(2, DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'Distribuidora San Martin', 'F002-118', 246.50, 4);

INSERT INTO detalle_compra (compra_id, insumo_id, cantidad, costo_unitario, subtotal) VALUES
(1, 1, 10.000, 14.50, 145.00),
(1, 4, 20.000,  2.80,  56.00),
(1, 5,  5.000,  3.20,  16.00),
(2, 3, 25.000,  4.50, 112.50),
(2, 7, 10.000,  9.80,  98.00),
(2, 10, 60.000, 0.60,  36.00);

INSERT INTO movimientos_inventario (insumo_id, tipo, cantidad, costo_unitario, motivo, usuario_id, fecha) VALUES
(1,  'entrada', 10.000, 14.50, 'Compra F001-245', 4, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(4,  'entrada', 20.000,  2.80, 'Compra F001-245', 4, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(5,  'entrada',  5.000,  3.20, 'Compra F001-245', 4, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(3,  'entrada', 25.000,  4.50, 'Compra F002-118', 4, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(7,  'entrada', 10.000,  9.80, 'Compra F002-118', 4, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(10, 'entrada', 60.000,  0.60, 'Compra F002-118', 4, DATE_SUB(NOW(), INTERVAL 2 DAY));

-- ---------------------------------------------------------------------
--  VENTAS DE DEMOSTRACION
-- ---------------------------------------------------------------------
INSERT INTO ventas (id, numero, cliente_id, usuario_id, fecha, subtotal, descuento, total, estado, tipo_servicio, mesa, observacion) VALUES
(1,  'V-000001', 1, 2, DATE_SUB(NOW(), INTERVAL 6 DAY),  42.00, 0.00,  42.00, 'cerrada',        'mesa',     '1', NULL),
(2,  'V-000002', 2, 2, DATE_SUB(NOW(), INTERVAL 5 DAY),  62.00, 0.00,  62.00, 'cerrada',        'llevar',   NULL, 'Sin cebolla'),
(3,  'V-000003', 3, 2, DATE_SUB(NOW(), INTERVAL 4 DAY),  62.00, 0.00,  62.00, 'cerrada',        'mesa',     '2', NULL),
(4,  'V-000004', 4, 2, DATE_SUB(NOW(), INTERVAL 3 DAY), 100.00, 0.00, 100.00, 'cerrada',        'mesa',     '3', 'Cumpleanos'),
(5,  'V-000005', 5, 2, DATE_SUB(NOW(), INTERVAL 2 DAY),  47.00, 0.00,  47.00, 'cerrada',        'delivery', NULL, 'Av. Pajan 900'),
(6,  'V-000006', 1, 2, DATE_SUB(NOW(), INTERVAL 1 DAY),  79.00, 0.00,  79.00, 'cerrada',        'mesa',     '1', NULL),
(7,  'V-000007', 2, 2, NOW(),                             50.00, 0.00,  50.00, 'pagada',         'mesa',     '4', NULL),
(8,  'V-000008', 3, 2, NOW(),                             28.00, 0.00,  28.00, 'en_preparacion', 'llevar',   NULL, NULL),
(9,  'V-000009', 4, 2, NOW(),                             45.00, 0.00,  45.00, 'registrada',     'mesa',     '5', NULL),
(10, 'V-000010', 5, 2, NOW(),                             48.00, 0.00,  48.00, 'lista',          'mesa',     '2', NULL);

INSERT INTO detalle_venta (venta_id, producto_id, cantidad, precio, subtotal) VALUES
(1, 5, 1, 22.00, 22.00), (1, 13, 2, 10.00, 20.00),
(2, 6, 2, 18.00, 36.00), (2, 14, 2,  9.00, 18.00), (2, 18, 1, 8.00, 8.00),
(3, 9, 2, 19.00, 38.00), (3, 15, 2,  5.00, 10.00), (3, 20, 2, 7.00, 14.00),
(4, 5, 2, 22.00, 44.00), (4, 3, 2, 15.00, 30.00), (4, 13, 1, 10.00, 10.00), (4, 18, 2, 8.00, 16.00),
(5, 7, 1, 16.00, 16.00), (5, 10, 1, 21.00, 21.00), (5, 16, 1, 3.00, 3.00), (5, 19, 1, 7.00, 7.00),
(6, 8, 1, 20.00, 20.00), (6, 11, 1, 17.00, 17.00), (6, 12, 1, 25.00, 25.00), (6, 14, 1, 9.00, 9.00), (6, 21, 1, 8.00, 8.00),
(7, 5, 1, 22.00, 22.00), (7, 13, 2, 10.00, 20.00), (7, 18, 1, 8.00, 8.00),
(8, 9, 1, 19.00, 19.00), (8, 15, 1,  5.00, 5.00), (8, 17, 1, 4.00, 4.00),
(9, 2, 2, 10.00, 20.00), (9, 6, 1, 18.00, 18.00), (9, 19, 1, 7.00, 7.00),
(10, 4, 1, 14.00, 14.00), (10, 10, 1, 21.00, 21.00), (10, 16, 2, 3.00, 6.00), (10, 20, 1, 7.00, 7.00);

INSERT INTO pagos_cliente (venta_id, fecha, monto, medio, referencia, usuario_id) VALUES
(1, DATE_SUB(NOW(), INTERVAL 6 DAY), 42.00, 'efectivo', NULL, 2),
(2, DATE_SUB(NOW(), INTERVAL 5 DAY), 40.00, 'yape', 'YP-1024', 2),
(2, DATE_SUB(NOW(), INTERVAL 5 DAY), 22.00, 'efectivo', NULL, 2),
(3, DATE_SUB(NOW(), INTERVAL 4 DAY), 62.00, 'yape', 'YP-1031', 2),
(4, DATE_SUB(NOW(), INTERVAL 3 DAY), 100.00, 'efectivo', NULL, 2),
(5, DATE_SUB(NOW(), INTERVAL 2 DAY), 47.00, 'yape', 'YP-1047', 2),
(6, DATE_SUB(NOW(), INTERVAL 1 DAY), 50.00, 'efectivo', NULL, 2),
(6, DATE_SUB(NOW(), INTERVAL 1 DAY), 29.00, 'yape', 'YP-1052', 2),
(7, NOW(), 50.00, 'yape', 'YP-1060', 2),
(10, NOW(), 48.00, 'efectivo', NULL, 2);

-- ---------------------------------------------------------------------
--  GASTOS
-- ---------------------------------------------------------------------
INSERT INTO gastos (fecha, concepto, categoria, monto, comprobante, usuario_id) VALUES
(DATE_SUB(CURDATE(), INTERVAL 6 DAY), 'Compra de verduras',        'insumos',    85.00, 'B001-120', 1),
(DATE_SUB(CURDATE(), INTERVAL 5 DAY), 'Recibo de luz',             'servicios', 120.00, 'L-0325',   1),
(DATE_SUB(CURDATE(), INTERVAL 4 DAY), 'Balon de gas',              'servicios',  95.00, 'G-8842',   1),
(DATE_SUB(CURDATE(), INTERVAL 3 DAY), 'Alquiler del local',        'alquiler',  800.00, 'A-0012',   1),
(DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'Internet y telefonia',      'servicios',  70.00, 'I-4410',   1),
(DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'Articulos de limpieza',     'operativo',  45.00, 'L-2210',   1),
(CURDATE(),                          'Compra de carne y pollo',    'insumos',   150.00, 'B001-131', 1);

-- ---------------------------------------------------------------------
--  PAGOS AL PERSONAL
-- ---------------------------------------------------------------------
INSERT INTO pagos_personal (personal_id, fecha, monto, concepto, usuario_id) VALUES
(2, DATE_SUB(CURDATE(), INTERVAL 5 DAY), 250.00, 'Adelanto de sueldo', 1),
(1, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 1200.00, 'Sueldo mensual',    1),
(3, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 80.00, 'Adelanto de sueldo', 1);
