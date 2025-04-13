package com.example.sante.service;

import com.example.sante.Entity.Event;

import java.util.List;

public interface iService <T>{
    void ajouter(T t);
    void modifier(T t);
    void supprimer(T t);

    List<T> afficher();
    // ArrayList<T> rechercher();
}
