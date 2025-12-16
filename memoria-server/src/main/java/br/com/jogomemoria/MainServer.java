/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.jogomemoria;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

/**
 *
 * @author joaog
 */
public class MainServer {
    public static void main(String[] args) {
        try {
            LocateRegistry.createRegistry(1099);
            IMemoriaJogo service = new ServidorJogo();
            Naming.rebind("rmi://localhost/MemoriaJogo", service);
            System.out.println("Servidor RMI iniciado na porta 1099");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}