package br.com.jogomemoria;

import br.com.jogomemoria.IMemoriaJogo;
import java.rmi.Naming;

public class MemoriaClient {
    public static void main(String[] args) {
        try {
            IMemoriaJogo jogo = (IMemoriaJogo) Naming.lookup("rmi://localhost/MemoriaJogo");
            int meuId = jogo.registrarJogador();
            System.out.println("Conectado como jogador " + meuId);
            
            // Testar conexão
            int vez = jogo.obterVez();
            String estado = jogo.obterMensagemEstado();
            System.out.println("Vez: " + vez + " - Estado: " + estado);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}