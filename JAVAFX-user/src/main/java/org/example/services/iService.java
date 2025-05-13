package org.example.services;

import org.example.models.Event;

import java.util.List;

public interface iService <T>{
    void ajouter(T t);
    void modifier(T t);
    void supprimer(T t);

    List<T> afficher();
    // ArrayList<T> rechercher();
}
