package br.com.jogomemoria;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class MainServer {
    public static void main(String[] args) {
        try {
            System.setProperty("java.rmi.server.hostname", "localhost");
            LocateRegistry.createRegistry(1099);
            IMemoriaJogo service = new ServidorJogo();
            Naming.rebind("rmi://localhost/MemoriaJogo", service);
            System.out.println("✅ Servidor RMI iniciado na porta 1099");
        } catch (Exception e) {
            System.err.println("❌ Erro ao iniciar servidor:");
            e.printStackTrace();
        }
    }
}