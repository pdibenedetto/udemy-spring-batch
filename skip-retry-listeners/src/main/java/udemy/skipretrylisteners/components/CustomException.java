package udemy.skipretrylisteners.components;

public class CustomException extends RuntimeException{
    public CustomException(){}
    public CustomException(String message) {
        super(message);
    }
}
