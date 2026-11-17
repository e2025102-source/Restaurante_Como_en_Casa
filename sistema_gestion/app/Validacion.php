<?php

declare(strict_types=1);

/**
 * Utilidades de validacion y conversion de datos de entrada.
 */
final class Validacion
{
    public static function texto($valor, string $campo, int $max = 255): string
    {
        $texto = trim((string) $valor);
        if ($texto === '') {
            throw new InvalidArgumentException("El campo {$campo} es obligatorio.");
        }
        if (mb_strlen($texto) > $max) {
            throw new InvalidArgumentException("El campo {$campo} no debe superar {$max} caracteres.");
        }

        return $texto;
    }

    public static function textoOpcional($valor, int $max = 255): ?string
    {
        $texto = trim((string) $valor);
        if ($texto === '') {
            return null;
        }
        if (mb_strlen($texto) > $max) {
            $texto = mb_substr($texto, 0, $max);
        }

        return $texto;
    }

    public static function decimal($valor, string $campo, float $min = 0): float
    {
        if (!is_numeric($valor)) {
            throw new InvalidArgumentException("El campo {$campo} debe ser numerico.");
        }
        $numero = (float) $valor;
        if ($numero < $min) {
            throw new InvalidArgumentException("El campo {$campo} no puede ser menor que {$min}.");
        }

        return round($numero, 2);
    }

    public static function entero($valor, string $campo, int $min = 0): int
    {
        if (!is_numeric($valor)) {
            throw new InvalidArgumentException("El campo {$campo} debe ser numerico.");
        }
        $numero = (int) $valor;
        if ($numero < $min) {
            throw new InvalidArgumentException("El campo {$campo} no puede ser menor que {$min}.");
        }

        return $numero;
    }

    public static function fecha($valor, string $campo): string
    {
        $texto = trim((string) $valor);
        $fecha = DateTimeImmutable::createFromFormat('Y-m-d', $texto);
        if (!$fecha || $fecha->format('Y-m-d') !== $texto) {
            throw new InvalidArgumentException("El campo {$campo} debe tener el formato AAAA-MM-DD.");
        }

        return $texto;
    }

    public static function fechaOpcional($valor): ?string
    {
        $texto = trim((string) $valor);

        return $texto === '' ? null : self::fecha($texto, 'fecha');
    }

    public static function enLista($valor, array $permitidos, string $campo): string
    {
        $texto = trim((string) $valor);
        if (!in_array($texto, $permitidos, true)) {
            throw new InvalidArgumentException(
                "El campo {$campo} debe ser uno de: " . implode(', ', $permitidos) . '.'
            );
        }

        return $texto;
    }
}
