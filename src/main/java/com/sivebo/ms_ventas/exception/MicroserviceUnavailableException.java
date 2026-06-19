package com.sivebo.ms_ventas.exception;

public class MicroserviceUnavailableException extends RuntimeException {

        public MicroserviceUnavailableException(String message) {
                super(message);
        }
}
