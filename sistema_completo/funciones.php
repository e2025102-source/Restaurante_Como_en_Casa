<?php
if (session_status() === PHP_SESSION_NONE) session_start();
require_once __DIR__ . '/conexion.php';
function e($v) { return htmlspecialchars((string)$v, ENT_QUOTES, 'UTF-8'); }
function exigir_login() { if (empty($_SESSION['usuario_id'])) { header('Location: index.php'); exit; } }
function exigir_admin() { exigir_login(); if (($_SESSION['rol'] ?? '') !== 'admin') { http_response_code(403); exit('Acceso denegado. Esta sección es solo para el administrador.'); } }
function aviso($texto, $tipo='ok') { $_SESSION['aviso'] = ['texto'=>$texto, 'tipo'=>$tipo]; }
function mostrar_aviso() { if (!empty($_SESSION['aviso'])) { $a=$_SESSION['aviso']; echo '<div class="alerta '.e($a['tipo']).'">'.e($a['texto']).'</div>'; unset($_SESSION['aviso']); } }
