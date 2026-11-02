CREATE DATABASE IF NOT EXISTS como_en_casa CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE como_en_casa;
CREATE TABLE IF NOT EXISTS usuarios (
 id INT AUTO_INCREMENT PRIMARY KEY,
 nombre VARCHAR(100) NOT NULL,
 usuario VARCHAR(60) NOT NULL UNIQUE,
 password VARCHAR(255) NOT NULL,
 rol ENUM('admin','cliente') NOT NULL DEFAULT 'cliente',
 creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS productos (
 id INT AUTO_INCREMENT PRIMARY KEY,
 nombre VARCHAR(120) NOT NULL,
 descripcion TEXT,
 categoria VARCHAR(60) DEFAULT 'Plato de fondo',
 precio DECIMAL(10,2) NOT NULL,
 imagen VARCHAR(255) DEFAULT '',
 activo TINYINT(1) NOT NULL DEFAULT 1,
 creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS metodos_pago (
 id INT AUTO_INCREMENT PRIMARY KEY,
 nombre VARCHAR(80) NOT NULL,
 activo TINYINT(1) NOT NULL DEFAULT 1
);
CREATE TABLE IF NOT EXISTS metodos_envio (
 id INT AUTO_INCREMENT PRIMARY KEY,
 nombre VARCHAR(80) NOT NULL,
 costo DECIMAL(10,2) NOT NULL DEFAULT 0,
 activo TINYINT(1) NOT NULL DEFAULT 1
);
CREATE TABLE IF NOT EXISTS pedidos (
 id INT AUTO_INCREMENT PRIMARY KEY,
 usuario_id INT NOT NULL,
 total DECIMAL(10,2) NOT NULL,
 direccion VARCHAR(255) NOT NULL,
 metodo_pago_id INT NOT NULL,
 metodo_envio_id INT NOT NULL,
 estado ENUM('Pendiente','En preparación','En camino','Entregado','Cancelado') NOT NULL DEFAULT 'Pendiente',
 creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
 FOREIGN KEY (metodo_pago_id) REFERENCES metodos_pago(id),
 FOREIGN KEY (metodo_envio_id) REFERENCES metodos_envio(id)
);
CREATE TABLE IF NOT EXISTS pedido_detalles (
 id INT AUTO_INCREMENT PRIMARY KEY,
 pedido_id INT NOT NULL,
 producto_id INT NULL,
 nombre_producto VARCHAR(120) NOT NULL,
 precio DECIMAL(10,2) NOT NULL,
 cantidad INT NOT NULL,
 FOREIGN KEY (pedido_id) REFERENCES pedidos(id) ON DELETE CASCADE,
 FOREIGN KEY (producto_id) REFERENCES productos(id) ON DELETE SET NULL
);
INSERT INTO metodos_pago(nombre) SELECT 'Efectivo al recibir' WHERE NOT EXISTS (SELECT 1 FROM metodos_pago);
INSERT INTO metodos_pago(nombre) SELECT 'Transferencia bancaria' WHERE NOT EXISTS (SELECT 1 FROM metodos_pago WHERE nombre='Transferencia bancaria');
INSERT INTO metodos_pago(nombre) SELECT 'Yape / Plin' WHERE NOT EXISTS (SELECT 1 FROM metodos_pago WHERE nombre='Yape / Plin');
INSERT INTO metodos_envio(nombre,costo) SELECT 'Delivery',5.00 WHERE NOT EXISTS (SELECT 1 FROM metodos_envio);
INSERT INTO metodos_envio(nombre,costo) SELECT 'Recojo en local',0.00 WHERE NOT EXISTS (SELECT 1 FROM metodos_envio WHERE nombre='Recojo en local');
INSERT INTO productos(nombre,descripcion,categoria,precio,imagen) SELECT 'Lomo Saltado Casero','Carne salteada con cebolla, tomate y papas.','Plato de fondo',28.00,'imagenes/plato1.jpg' WHERE NOT EXISTS (SELECT 1 FROM productos);
INSERT INTO productos(nombre,descripcion,categoria,precio,imagen) SELECT 'Ají de Gallina','Pollo deshilachado en crema de ají amarillo.','Plato de fondo',22.00,'imagenes/plato2.jpg' WHERE NOT EXISTS (SELECT 1 FROM productos WHERE nombre='Ají de Gallina');
INSERT INTO productos(nombre,descripcion,categoria,precio,imagen) SELECT 'Sopa Criolla Especial','Caldo casero con carne, fideos y huevo.','Entrada',18.00,'imagenes/plato3.jpg' WHERE NOT EXISTS (SELECT 1 FROM productos WHERE nombre='Sopa Criolla Especial');
INSERT INTO productos(nombre,descripcion,categoria,precio,imagen) SELECT 'Pie de Manzana Casero','Postre horneado con canela.','Postre',12.00,'imagenes/plato4.jpg' WHERE NOT EXISTS (SELECT 1 FROM productos WHERE nombre='Pie de Manzana Casero');
