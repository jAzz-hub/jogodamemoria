package br.com.jogomemoria;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IMemoriaJogo extends Remote {
    int registrarJogador() throws RemoteException;
    boolean jogar(int jogadorId, int linha1, int col1, int linha2, int col2) 
    throws RemoteException;
    int[][] obterTabuleiro() throws RemoteException;
    
    int[][] obterDonos() throws RemoteException;
    int[] obterPlacar() throws RemoteException;

    int obterValorGabarito(int linha, int coluna) throws RemoteException;
    int obterVez() throws RemoteException;
    String obterMensagemEstado() throws RemoteException;
}