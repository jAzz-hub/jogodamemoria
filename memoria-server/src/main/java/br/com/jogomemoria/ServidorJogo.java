package br.com.jogomemoria;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;

public class ServidorJogo extends UnicastRemoteObject implements IMemoriaJogo {

        private final int[][] matrizGabarito = new int[3][4]; // Matriz resultado (não muda após inicialização)
    private final int[][] matrizJogadas = new int[3][4];  // Matriz visível (atualizada durante o jogo)
    private int jogadoresConectados = 0;
    private int jogadorAtual = 1; // Começa com o jogador 1

    private static final String PATH_RESULTADO = "C:\\Dados\\MATRIZES_RESULTADO.txt";
    private static final String PATH_JOGADAS = "C:\\Dados\\JOGADAS.txt";

    public ServidorJogo() throws RemoteException {
        super();
        inicializarJogo();
    }

    private void inicializarJogo() {
        // Gera 6 pares (números de 1 a 6, duas vezes cada)
        ArrayList<Integer> numeros = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            numeros.add(i);
            numeros.add(i);
        }
        Collections.shuffle(numeros);

        // Preenche a matriz gabarito
        int index = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                matrizGabarito[i][j] = numeros.get(index);
                matrizJogadas[i][j] = 0; // Tudo oculto no início
                index++;
            }
        }

        // Salva as duas matrizes em arquivos TXT
        salvarMatrizEmArquivo(PATH_RESULTADO, matrizGabarito);
        salvarMatrizEmArquivo(PATH_JOGADAS, matrizJogadas);
    }

    private void salvarMatrizEmArquivo(String caminho, int[][] matriz) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(caminho))) {
            for (int[] linha : matriz) {
                for (int j = 0; j < linha.length; j++) {
                    writer.print(linha[j]);
                    if (j < linha.length - 1) writer.print(" ");
                }
                writer.println();
            }
        } catch (Exception e) {
            System.err.println("Erro ao salvar arquivo " + caminho + ": " + e.getMessage());
        }
    }

    @Override
    public synchronized boolean jogar(int jogadorId, int linha1, int col1, int linha2, int col2) throws RemoteException {
        // Verifica se é a vez do jogador
        if (jogadorId != jogadorAtual) {
            return false; // Jogada inválida (não é a vez dele)
        }

        // Verifica se formou par
        if (matrizGabarito[linha1][col1] == matrizGabarito[linha2][col2]) {
            // Acertou: revela permanentemente as duas posições
            matrizJogadas[linha1][col1] = matrizGabarito[linha1][col1];
            matrizJogadas[linha2][col2] = matrizGabarito[linha2][col2];
            salvarMatrizEmArquivo(PATH_JOGADAS, matrizJogadas);
            // Mantém a vez (regra comum: quem acerta joga novamente)
            return true;
        } else {
            // Errou: passa a vez para o outro jogador
            jogadorAtual = (jogadorAtual == 1) ? 2 : 1;
            return false;
        }
    }

    
    
    @Override
    public synchronized int registrarJogador() throws RemoteException {
        if (jogadoresConectados >= 2) {
            return -1; // Já tem dois jogadores
        }
        jogadoresConectados++;
        System.out.println("🎮 Jogador " + jogadoresConectados + " conectado.");
        return jogadoresConectados;
    }
    
    @Override
    public synchronized int[][] obterTabuleiro() throws RemoteException {
        int[][] copia = new int[3][4];
        for (int i = 0; i < 3; i++) {
            System.arraycopy(matrizJogadas[i], 0, copia[i], 0, 4);
        }
        return copia;
    }
    
    @Override
    public synchronized int obterVez() throws RemoteException {
        return jogadorAtual;
    }
    
    @Override
    public synchronized String obterMensagemEstado() throws RemoteException {
        if (jogadoresConectados < 2) {
            return "⏳ Aguardando outro jogador... (" + jogadoresConectados + "/2)";
        }
        return "🎯 Vez do jogador " + jogadorAtual;
    }
}