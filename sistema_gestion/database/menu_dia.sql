-- =====================================================================
--  MODULO "MENU DEL DIA" - RESTAURANTE "COMO EN CASA"
--  Migracion segura: solo crea tablas/vista si no existen.
--  No elimina ni modifica registros existentes.
-- =====================================================================

USE como_en_casa;

CREATE TABLE IF NOT EXISTS menus_dia (
    id                    INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    fecha                 DATE NOT NULL,
    precio                DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    cantidad_preparada    INT UNSIGNED NOT NULL DEFAULT 0,
    cantidad_vendida      INT UNSIGNED NOT NULL DEFAULT 0,
    estado                ENUM('borrador','publicado','cerrado') NOT NULL DEFAULT 'publicado',
    observacion           VARCHAR(255) NULL,
    usuario_id            INT UNSIGNED NULL,
    creado_en             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_menus_dia_fecha (fecha),
    KEY idx_menus_dia_estado (estado),
    CONSTRAINT fk_menus_dia_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS menu_dia_items (
    id                    INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    menu_dia_id           INT UNSIGNED NOT NULL,
    tipo                  ENUM('entrada','segundo','refresco') NOT NULL,
    nombre                VARCHAR(120) NOT NULL,
    producto_id           INT UNSIGNED NULL,
    precio                DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    cantidad_preparada    INT UNSIGNED NOT NULL DEFAULT 0,
    cantidad_vendida      INT UNSIGNED NOT NULL DEFAULT 0,
    UNIQUE KEY uq_menu_dia_tipo (menu_dia_id, tipo),
    KEY idx_menu_items_producto (producto_id),
    CONSTRAINT fk_menu_items_menu FOREIGN KEY (menu_dia_id)
        REFERENCES menus_dia(id) ON DELETE CASCADE,
    CONSTRAINT fk_menu_items_producto FOREIGN KEY (producto_id)
        REFERENCES productos(id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS menu_dia_ventas (
    id                    INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    menu_dia_id           INT UNSIGNED NOT NULL,
    menu_item_id          INT UNSIGNED NULL,
    venta_id              INT UNSIGNED NOT NULL,
    tipo_item             ENUM('menu','entrada','segundo','refresco') NOT NULL,
    cantidad              INT UNSIGNED NOT NULL,
    precio                DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    subtotal              DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    creado_en             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_mdv_menu (menu_dia_id),
    KEY idx_mdv_venta (venta_id),
    KEY idx_mdv_item (menu_item_id),
    CONSTRAINT fk_mdv_menu FOREIGN KEY (menu_dia_id)
        REFERENCES menus_dia(id) ON DELETE RESTRICT,
    CONSTRAINT fk_mdv_item FOREIGN KEY (menu_item_id)
        REFERENCES menu_dia_items(id) ON DELETE SET NULL,
    CONSTRAINT fk_mdv_venta FOREIGN KEY (venta_id)
        REFERENCES ventas(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE OR REPLACE VIEW v_menu_dia_ventas AS
SELECT mdv.id,
       mdv.venta_id,
       v.numero,
       v.fecha,
       v.estado AS estado_venta,
       mdv.menu_dia_id,
       md.fecha AS menu_fecha,
       mdv.tipo_item,
       mdv.cantidad,
       mdv.precio,
       mdv.subtotal
  FROM menu_dia_ventas mdv
  JOIN ventas v ON v.id = mdv.venta_id
  JOIN menus_dia md ON md.id = mdv.menu_dia_id;
