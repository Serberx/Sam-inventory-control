package com.samic.samic.exceptions;

public class ProducerException extends RuntimeException  {
    public ProducerException(String message){
        super(message);
    }

    public ProducerException(String message, Throwable cause){
        super(message, cause);
    }
}
