package br.com.jogomemoria;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays; // Adicionar import


public class ServidorJogo extends UnicastRemoteObject implements IMemoriaJogo {
    private int tempL1 = -1, tempC1 = -1, tempL2 = -1, tempC2 = -1;
    private final int[][] matrizDonos = new int[3][4];
    private final int[][] matrizGabarito = new int[3][4]; // Matriz resultado (não muda após inicialização)
    private final int[][] matrizJogadas = new int[3][4];  // Matriz visível (atualizada durante o jogo)
    private final int[] placar = new int[]{0, 0}; // [Pontos J1, Pontos J2]
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
        
        // Resetar matriz de donos e placar
        for (int[] linha : matrizDonos) Arrays.fill(linha, 0);
        placar[0] = 0;
        placar[1] = 0;
        
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
        if (jogadorId != jogadorAtual) return false;

        // Se acertou o par
        if (matrizGabarito[linha1][col1] == matrizGabarito[linha2][col2]) {
            matrizJogadas[linha1][col1] = matrizGabarito[linha1][col1];
            matrizJogadas[linha2][col2] = matrizGabarito[linha2][col2];
            matrizDonos[linha1][col1] = jogadorId;
            matrizDonos[linha2][col2] = jogadorId;
            placar[jogadorId - 1]++;
            return true;
        } else {
            // Se errou: salvamos as coordenadas temporárias para o Cliente "enxergar" no próximo poll
            tempL1 = linha1; tempC1 = col1;
            tempL2 = linha2; tempC2 = col2;

            // Pequena pausa no servidor para garantir que o cliente consiga ler o estado "errado"
            // antes de mudarmos o jogador atual
            try { Thread.sleep(700); } catch (InterruptedException e) {}

            jogadorAtual = (jogadorAtual == 1) ? 2 : 1;

            // Limpa as temporárias após a troca
            tempL1 = -1; tempC1 = -1; tempL2 = -1; tempC2 = -1;
            return false;
        }
    }

    // NOVOS MÉTODOS DA INTERFACE
    @Override
    public synchronized int[][] obterDonos() throws RemoteException {
        int[][] copia = new int[3][4];
        for (int i = 0; i < 3; i++) {
            System.arraycopy(matrizDonos[i], 0, copia[i], 0, 4);
        }
        return copia;
    }

    @Override
    public synchronized int[] obterPlacar() throws RemoteException {
        return Arrays.copyOf(placar, 2);
    }

    

    @Override
    public synchronized int registrarJogador() throws RemoteException {
        
        if (jogadoresConectados < 2) {
            jogadoresConectados++;
            System.out.println("Jogador " + jogadoresConectados + " conectado.");
            return jogadoresConectados;
        }
        return -1; // Retorna -1 se já houver 2
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
    public synchronized int obterValorGabarito(int linha, int coluna) throws RemoteException {
        return matrizGabarito[linha][coluna];
    }
    
    @Override
    public synchronized String obterMensagemEstado() throws RemoteException {
        if (jogadoresConectados < 2) {
            return "⏳ Aguardando outro jogador... (" + jogadoresConectados + "/2)";
        }
        return "🎯 Vez do jogador " + jogadorAtual;
    }
}