<?php

declare(strict_types=1);

/**
 * Excepcion con codigo HTTP para respuestas controladas de la API.
 */
final class ApiException extends RuntimeException
{
    private int $httpCode;

    public function __construct(string $mensaje, int $httpCode = 400)
    {
        parent::__construct($mensaje);
        $this->httpCode = $httpCode;
    }

    public function httpCode(): int
    {
        return $this->httpCode;
    }
}
