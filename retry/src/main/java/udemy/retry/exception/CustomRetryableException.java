package udemy.retry.exception;

public class CustomRetryableException extends RuntimeException{
    public CustomRetryableException(){}
    public CustomRetryableException(String message){
        super(message);
    }
}
