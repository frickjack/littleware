package littleware.asset.client;

import java.io.IOException;

public class RemoteException extends IOException {
    private static final long serialVersionUID = 1L;

    public RemoteException(String message) {
        super(message);
    }

    public RemoteException(String message, Throwable causedBy) {
        super(message, causedBy);
    }

}