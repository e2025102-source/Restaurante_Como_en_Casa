-- =====================================================================
--  SISTEMA DE GESTION PARA EL RESTAURANTE "COMO EN CASA"
--  Base de datos MySQL / MariaDB
--  Proyecto: Programacion Orientada a Objetos - IESTP Pajan
-- =====================================================================

CREATE DATABASE IF NOT EXISTS como_en_casa
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE como_en_casa;

SET FOREIGN_KEY_CHECKS = 0;

DROP VIEW  IF EXISTS v_ganancias_diarias;
DROP VIEW  IF EXISTS v_movimientos_economicos;
DROP VIEW  IF EXISTS v_ventas_detalle;
DROP VIEW  IF EXISTS v_ingresos_por_medio;
DROP VIEW  IF EXISTS v_stock_bajo;
DROP TABLE IF EXISTS detalle_compra;
DROP TABLE IF EXISTS compras;
DROP TABLE IF EXISTS pagos_personal;
DROP TABLE IF EXISTS personal;
DROP TABLE IF EXISTS gastos;
DROP TABLE IF EXISTS pagos_cliente;
DROP TABLE IF EXISTS detalle_venta;
DROP TABLE IF EXISTS ventas;
DROP TABLE IF EXISTS movimientos_inventario;
DROP TABLE IF EXISTS insumos;
DROP TABLE IF EXISTS productos;
DROP TABLE IF EXISTS clientes;
DROP TABLE IF EXISTS tokens;
DROP TABLE IF EXISTS usuarios;

SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------------------
--  USUARIOS Y SESIONES
-- ---------------------------------------------------------------------
CREATE TABLE usuarios (
    id            INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombres       VARCHAR(120) NOT NULL,
    usuario       VARCHAR(50)  NOT NULL UNIQUE,
    clave_hash    VARCHAR(255) NOT NULL,
    rol           ENUM('administrador','cajero','cocina','inventario') NOT NULL DEFAULT 'cajero',
    activo        TINYINT(1) NOT NULL DEFAULT 1,
    creado_en     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE tokens (
    id            INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    usuario_id    INT UNSIGNED NOT NULL,
    token         CHAR(64) NOT NULL UNIQUE,
    expira        DATETIME NOT NULL,
    creado_en     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tokens_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
--  CLIENTES
-- ---------------------------------------------------------------------
CREATE TABLE clientes (
    id            INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre        VARCHAR(120) NOT NULL,
    documento     VARCHAR(20)  NULL,
    telefono      VARCHAR(20)  NULL,
    direccion     VARCHAR(150) NULL,
    creado_en     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_clientes_nombre (nombre)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
--  PRODUCTOS / PLATOS
-- ---------------------------------------------------------------------
CREATE TABLE productos (
    id            INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre        VARCHAR(120) NOT NULL,
    descripcion   VARCHAR(255) NULL,
    categoria     ENUM('entradas','platos','bebidas','postres') NOT NULL DEFAULT 'platos',
    precio        DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    estado        ENUM('disponible','agotado','inactivo') NOT NULL DEFAULT 'disponible',
    creado_en     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_productos_categoria (categoria),
    INDEX idx_productos_nombre (nombre)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
--  INVENTARIO DE INSUMOS
-- ---------------------------------------------------------------------
CREATE TABLE insumos (
    id            INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre        VARCHAR(120) NOT NULL,
    unidad        VARCHAR(20)  NOT NULL DEFAULT 'unidad',
    costo         DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    stock_actual  DECIMAL(12,3) NOT NULL DEFAULT 0.000,
    stock_minimo  DECIMAL(12,3) NOT NULL DEFAULT 0.000,
    creado_en     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_insumos_nombre (nombre)
) ENGINE=InnoDB;

CREATE TABLE movimientos_inventario (
    id              INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    insumo_id       INT UNSIGNED NOT NULL,
    tipo            ENUM('entrada','salida') NOT NULL,
    cantidad        DECIMAL(12,3) NOT NULL,
    costo_unitario  DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    motivo          VARCHAR(150) NULL,
    usuario_id      INT UNSIGNED NULL,
    fecha           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mov_insumo FOREIGN KEY (insumo_id)
        REFERENCES insumos(id) ON DELETE CASCADE,
    CONSTRAINT fk_mov_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id) ON DELETE SET NULL,
    INDEX idx_mov_fecha (fecha),
    INDEX idx_mov_insumo (insumo_id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
--  COMPRAS DE INSUMOS
-- ---------------------------------------------------------------------
CREATE TABLE compras (
    id            INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    fecha         DATE NOT NULL,
    proveedor     VARCHAR(120) NOT NULL,
    documento     VARCHAR(50)  NULL,
    total         DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    usuario_id    INT UNSIGNED NULL,
    creado_en     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_compras_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id) ON DELETE SET NULL,
    INDEX idx_compras_fecha (fecha)
) ENGINE=InnoDB;

CREATE TABLE detalle_compra (
    id              INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    compra_id       INT UNSIGNED NOT NULL,
    insumo_id       INT UNSIGNED NOT NULL,
    cantidad        DECIMAL(12,3) NOT NULL,
    costo_unitario  DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    subtotal        DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_dcompra_compra FOREIGN KEY (compra_id)
        REFERENCES compras(id) ON DELETE CASCADE,
    CONSTRAINT fk_dcompra_insumo FOREIGN KEY (insumo_id)
        REFERENCES insumos(id) ON DELETE RESTRICT,
    INDEX idx_dcompra_compra (compra_id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
--  VENTAS
-- ---------------------------------------------------------------------
CREATE TABLE ventas (
    id              INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    numero          VARCHAR(20) NULL UNIQUE,
    idempotencia    VARCHAR(64) NULL UNIQUE,
    cliente_id      INT UNSIGNED NULL,
    usuario_id      INT UNSIGNED NULL,
    fecha           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    subtotal        DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    descuento       DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total           DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    estado          ENUM('registrada','en_preparacion','lista','entregada','pagada','cerrada','anulada')
                    NOT NULL DEFAULT 'registrada',
    tipo_servicio   ENUM('mesa','llevar','delivery') NOT NULL DEFAULT 'mesa',
    mesa            VARCHAR(10) NULL,
    observacion     VARCHAR(255) NULL,
    creado_en       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ventas_cliente FOREIGN KEY (cliente_id)
        REFERENCES clientes(id) ON DELETE SET NULL,
    CONSTRAINT fk_ventas_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id) ON DELETE SET NULL,
    INDEX idx_ventas_fecha (fecha),
    INDEX idx_ventas_estado (estado)
) ENGINE=InnoDB;

CREATE TABLE detalle_venta (
    id            INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    venta_id      INT UNSIGNED NOT NULL,
    producto_id   INT UNSIGNED NOT NULL,
    cantidad      INT UNSIGNED NOT NULL,
    precio        DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    subtotal      DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_dventa_venta FOREIGN KEY (venta_id)
        REFERENCES ventas(id) ON DELETE CASCADE,
    CONSTRAINT fk_dventa_producto FOREIGN KEY (producto_id)
        REFERENCES productos(id) ON DELETE RESTRICT,
    INDEX idx_dventa_venta (venta_id),
    INDEX idx_dventa_producto (producto_id)
) ENGINE=InnoDB;

CREATE TABLE pagos_cliente (
    id            INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    venta_id      INT UNSIGNED NOT NULL,
    fecha         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    monto         DECIMAL(10,2) NOT NULL,
    medio         ENUM('efectivo','yape','tarjeta') NOT NULL DEFAULT 'efectivo',
    referencia    VARCHAR(60) NULL,
    usuario_id    INT UNSIGNED NULL,
    CONSTRAINT fk_pago_venta FOREIGN KEY (venta_id)
        REFERENCES ventas(id) ON DELETE CASCADE,
    CONSTRAINT fk_pago_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id) ON DELETE SET NULL,
    INDEX idx_pago_fecha (fecha),
    INDEX idx_pago_venta (venta_id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
--  GASTOS
-- ---------------------------------------------------------------------
CREATE TABLE gastos (
    id            INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    fecha         DATE NOT NULL,
    concepto      VARCHAR(150) NOT NULL,
    categoria     VARCHAR(60)  NOT NULL DEFAULT 'operativo',
    monto         DECIMAL(10,2) NOT NULL,
    comprobante   VARCHAR(60)  NULL,
    usuario_id    INT UNSIGNED NULL,
    creado_en     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_gastos_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id) ON DELETE SET NULL,
    INDEX idx_gastos_fecha (fecha)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
--  PERSONAL Y PAGOS AL PERSONAL
-- ---------------------------------------------------------------------
CREATE TABLE personal (
    id            INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre        VARCHAR(120) NOT NULL,
    documento     VARCHAR(20)  NULL,
    cargo         VARCHAR(60)  NOT NULL,
    telefono      VARCHAR(20)  NULL,
    sueldo        DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    fecha_ingreso DATE NULL,
    activo        TINYINT(1) NOT NULL DEFAULT 1,
    creado_en     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE pagos_personal (
    id            INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    personal_id   INT UNSIGNED NOT NULL,
    fecha         DATE NOT NULL,
    monto         DECIMAL(10,2) NOT NULL,
    concepto      VARCHAR(120) NOT NULL DEFAULT 'Sueldo',
    usuario_id    INT UNSIGNED NULL,
    creado_en     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ppersonal_personal FOREIGN KEY (personal_id)
        REFERENCES personal(id) ON DELETE CASCADE,
    CONSTRAINT fk_ppersonal_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id) ON DELETE SET NULL,
    INDEX idx_ppersonal_fecha (fecha)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
--  TRIGGER: mantiene el stock al registrar movimientos de inventario
-- ---------------------------------------------------------------------
CREATE TRIGGER trg_movimiento_stock
AFTER INSERT ON movimientos_inventario
FOR EACH ROW
UPDATE insumos
   SET stock_actual = stock_actual + IF(NEW.tipo = 'entrada', NEW.cantidad, -NEW.cantidad)
 WHERE id = NEW.insumo_id;

-- ---------------------------------------------------------------------
--  VISTAS DE APOYO PARA REPORTES
-- ---------------------------------------------------------------------
CREATE VIEW v_stock_bajo AS
SELECT id, nombre, unidad, stock_actual, stock_minimo
  FROM insumos
 WHERE stock_actual <= stock_minimo;

CREATE VIEW v_ingresos_por_medio AS
SELECT DATE(fecha) AS fecha, medio, COUNT(*) AS cantidad, SUM(monto) AS total
  FROM pagos_cliente
 GROUP BY DATE(fecha), medio;

CREATE VIEW v_ventas_detalle AS
SELECT v.id            AS venta_id,
       v.numero        AS numero,
       v.fecha         AS fecha,
       v.estado        AS estado,
       p.id            AS producto_id,
       p.nombre        AS producto,
       p.categoria     AS categoria,
       dv.cantidad     AS cantidad,
       dv.precio       AS precio,
       dv.subtotal     AS subtotal
  FROM ventas v
  JOIN detalle_venta dv ON dv.venta_id = v.id
  JOIN productos p      ON p.id = dv.producto_id;

CREATE VIEW v_movimientos_economicos AS
SELECT 'ingreso' AS tipo, DATE(p.fecha) AS fecha,
       CONCAT('Pago de venta ', v.numero) AS concepto, p.monto AS monto
  FROM pagos_cliente p
  JOIN ventas v ON v.id = p.venta_id
 WHERE v.estado <> 'anulada'
UNION ALL
SELECT 'egreso', g.fecha, g.concepto, g.monto
  FROM gastos g
UNION ALL
SELECT 'egreso', pp.fecha, CONCAT('Pago al personal: ', pe.nombre), pp.monto
  FROM pagos_personal pp
  JOIN personal pe ON pe.id = pp.personal_id
UNION ALL
SELECT 'egreso', c.fecha, CONCAT('Compra a ', c.proveedor), c.total
  FROM compras c;

CREATE VIEW v_ganancias_diarias AS
SELECT fecha,
       SUM(CASE WHEN tipo = 'ingreso' THEN monto ELSE 0 END) AS ingresos,
       SUM(CASE WHEN tipo = 'egreso'  THEN monto ELSE 0 END) AS egresos,
       SUM(CASE WHEN tipo = 'ingreso' THEN monto ELSE -monto END) AS ganancia
  FROM v_movimientos_economicos
 GROUP BY fecha;
