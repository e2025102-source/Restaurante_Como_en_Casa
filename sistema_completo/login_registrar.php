<?php
require_once 'funciones.php';
if($_SERVER['REQUEST_METHOD']!=='POST'){header('Location: index.php');exit;}
$usuario=trim($_POST['usuario']??''); $pass=$_POST['pass']??'';
if(isset($_POST['btnregistrar'])){
 $nombre=trim($_POST['nombre']??''); $confirmar=$_POST['confirmar']??'';
 if($nombre===''||strlen($usuario)<3||strlen($pass)<8||$pass!==$confirmar){aviso('Revisa tus datos: completa todo, usa al menos 8 caracteres y confirma la contraseña.','error');header('Location: registrar.php');exit;}
 $q=$conn->prepare('SELECT id FROM usuarios WHERE usuario=?');$q->bind_param('s',$usuario);$q->execute();$q->store_result();$existe=$q->num_rows>0;$q->close();
 if($existe){aviso('Ese nombre de usuario ya existe. Elige otro.','error');header('Location: registrar.php');exit;}
 $hash=password_hash($pass,PASSWORD_DEFAULT);$rol='cliente';$q=$conn->prepare('INSERT INTO usuarios(nombre,usuario,password,rol) VALUES(?,?,?,?)');$q->bind_param('ssss',$nombre,$usuario,$hash,$rol);
 if($q->execute()){aviso('¡Tu cuenta fue creada! Ya puedes iniciar sesión.');header('Location: index.php');exit;} aviso('No se pudo registrar la cuenta.','error');header('Location: registrar.php');exit;
}
if(isset($_POST['btningresar'])){
 $q=$conn->prepare('SELECT id,nombre,usuario,password,rol FROM usuarios WHERE usuario=? LIMIT 1');$q->bind_param('s',$usuario);$q->execute();$fila=$q->get_result()->fetch_assoc();$q->close();
 if($fila&&password_verify($pass,$fila['password'])){session_regenerate_id(true);$_SESSION['usuario_id']=(int)$fila['id'];$_SESSION['nombre']=$fila['nombre'];$_SESSION['usuario']=$fila['usuario'];$_SESSION['rol']=$fila['rol'];header('Location: '.($fila['rol']==='admin'?'admin/index.php':'tienda.php'));exit;}
 aviso('Usuario o contraseña incorrectos.','error');header('Location: index.php');exit;
}
header('Location: index.php');
