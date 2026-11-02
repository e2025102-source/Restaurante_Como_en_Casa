<?php
// Cambia estos datos si tu XAMPP/MySQL usa otro usuario o contraseña.
$dbhost = 'localhost';
$dbuser = 'root';
$dbpass = '';
$dbname = 'como_en_casa';
$conn = new mysqli($dbhost, $dbuser, $dbpass, $dbname);
if ($conn->connect_error) { http_response_code(500); die('Error de conexión con MySQL. Revisa conexion.php y que MySQL esté encendido.'); }
$conn->set_charset('utf8mb4');
