package com.samic.samic.exceptions;

public class StorageObjectHistoryException extends RuntimeException{
      public StorageObjectHistoryException(String message){
            super(message);
      }

      public StorageObjectHistoryException(String message, Throwable cause){
            super(message, cause);
      }
}
