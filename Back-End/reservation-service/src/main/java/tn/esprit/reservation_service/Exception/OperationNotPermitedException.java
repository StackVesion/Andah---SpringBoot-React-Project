package tn.esprit.reservation_service.Exception;

public class OperationNotPermitedException extends RuntimeException {
    public OperationNotPermitedException(String msg) {
       super(msg);
    }
}
