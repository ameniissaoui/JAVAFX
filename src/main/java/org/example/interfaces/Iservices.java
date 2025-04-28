package org.example.interfaces;
import java.util.List;

public interface Iservices<T>
{
    public void add(T t);
    public void update(T t);
    public void remove(T t);
    public T find(int id);
    public List<T> afficher();
    public T getone();

}
