package udemy.skip.components;

public class CustomRetryableException extends RuntimeException{
    public CustomRetryableException(){}
    public CustomRetryableException(String message){
        super(message);
    }
}
