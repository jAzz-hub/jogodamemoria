package jogomemoria;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IMemoriaJogo extends Remote {

    /** Registra um novo jogador e retorna seu ID (1 ou 2) */
    int registrarJogador() throws RemoteException;

    /** Realiza a jogada de um jogador com duas posições */
    boolean jogar(int jogadorId, int linha1, int col1, int linha2, int col2) 
            throws RemoteException;

    /** Retorna a matriz atual de jogadas visíveis (0 = oculto, >0 = figura revelada) */
    int[][] obterTabuleiro() throws RemoteException;

    /** Retorna o ID do jogador da vez atual */
    int obterVez() throws RemoteException;

    /** Retorna mensagem de estado do jogo (ex: "Aguardando jogador", "Fim de jogo") */
    String obterMensagemEstado() throws RemoteException;
}
