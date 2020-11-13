package dist;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MatchingRMI extends Remote{
    Response matchAC(Request req) throws RemoteException;
    Response promoteAC(Request req) throws RemoteException;
}
