package services;
import java.util.List;

public interface IServices <T>{

    public void addProduit(T t);
    public void removeProduit(T t);
    public void editProduit(T t);
    public List<T> showProduit();
    public T getoneProduit(int id);

}
