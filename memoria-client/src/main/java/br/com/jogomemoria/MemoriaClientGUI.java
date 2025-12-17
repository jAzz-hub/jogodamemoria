package br.com.jogomemoria;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.io.File;
import javax.imageio.ImageIO;

public class MemoriaClientGUI extends JFrame {

    private IMemoriaJogo jogo;
    private int meuId;
    private final int LINHAS = 3;
    private final int COLUNAS = 4;

    // Componentes Visuais
    private CartaButton[][] botoes;
    private JLabel lblStatus, lblVez;
    private JLabel lblPlacarJ1, lblPlacarJ2;
    private JPanel panelTabuleiro, panelEspera;
    private CardLayout cardLayout; // Para trocar entre tela de espera e jogo

    // Estado e Imagens
    private Point primeiraCarta = null;
    private boolean bloqueado = false;
    private boolean jogoFinalizado = false;
    private Image imgBallP1, imgBallP2;

    public MemoriaClientGUI() {
        super("Jogo da Memória RMI");
        carregarImagens();
        configurarJanela();
        conectarServidor();
        iniciarGameLoop();
    }

    private void carregarImagens() {
        try {
            // Tenta carregar as imagens dos caminhos absolutos
            imgBallP1 = ImageIO.read(new File("C:\\Dados\\FIGURAS\\black_ball.png"));
            imgBallP2 = ImageIO.read(new File("C:\\Dados\\FIGURAS\\red_ball.png"));
        } catch (Exception e) {
            System.err.println("Erro ao carregar imagens: " + e.getMessage());
            // Fallback: cria bolinhas coloridas em memória se não achar o arquivo
            imgBallP1 = criarImagemFallback(Color.BLACK);
            imgBallP2 = criarImagemFallback(Color.RED);
        }
    }
    
    private Image criarImagemFallback(Color c) {
        // Cria uma bolinha simples via código caso a imagem não exista
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(20, 20, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(c);
        g.fillOval(0, 0, 20, 20);
        g.dispose();
        return img;
    }

    private void configurarJanela() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 600);
        
        // Layout principal com CardLayout para alternar entre "Espera" e "Jogo"
        cardLayout = new CardLayout();
        setLayout(cardLayout);

        // --- TELA DE ESPERA ---
        panelEspera = new JPanel(new GridBagLayout());
        JLabel lblLoading = new JLabel("Aguardando Jogador 2 conectar...");
        lblLoading.setFont(new Font("Arial", Font.BOLD, 20));
        lblLoading.setIcon(UIManager.getIcon("OptionPane.informationIcon"));
        panelEspera.add(lblLoading);
        add(panelEspera, "ESPERA");

        // --- TELA DO JOGO ---
        JPanel panelJogo = new JPanel(new BorderLayout());

        // Topo: Placar e Vez
        JPanel panelInfo = new JPanel(new GridLayout(2, 1));
        JPanel panelPlacar = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        
        lblPlacarJ1 = new JLabel("Jogador 1: 0");
        lblPlacarJ1.setFont(new Font("Arial", Font.BOLD, 14));
        lblPlacarJ1.setForeground(Color.BLACK); // Representa a black ball
        
        lblPlacarJ2 = new JLabel("Jogador 2: 0");
        lblPlacarJ2.setFont(new Font("Arial", Font.BOLD, 14));
        lblPlacarJ2.setForeground(Color.RED);   // Representa a red ball
        
        panelPlacar.add(lblPlacarJ1);
        panelPlacar.add(new JSeparator(SwingConstants.VERTICAL));
        panelPlacar.add(lblPlacarJ2);
        
        lblVez = new JLabel("Conectando...", SwingConstants.CENTER);
        lblVez.setFont(new Font("Arial", Font.BOLD, 18));
        lblVez.setBorder(BorderFactory.createEmptyBorder(5,0,5,0));

        panelInfo.add(panelPlacar);
        panelInfo.add(lblVez);
        panelJogo.add(panelInfo, BorderLayout.NORTH);

        // Centro: Tabuleiro
        panelTabuleiro = new JPanel(new GridLayout(LINHAS, COLUNAS, 10, 10));
        panelTabuleiro.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        botoes = new CartaButton[LINHAS][COLUNAS];

        for (int i = 0; i < LINHAS; i++) {
            for (int j = 0; j < COLUNAS; j++) {
                CartaButton btn = new CartaButton(i, j);
                btn.addActionListener(e -> processarClique(btn.linha, btn.coluna));
                botoes[i][j] = btn;
                panelTabuleiro.add(btn);
            }
        }
        panelJogo.add(panelTabuleiro, BorderLayout.CENTER);

        // Base: Status
        lblStatus = new JLabel("...", SwingConstants.CENTER);
        lblStatus.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        panelJogo.add(lblStatus, BorderLayout.SOUTH);

        add(panelJogo, "JOGO");
        
        setLocationRelativeTo(null);
    }

    private void conectarServidor() {
        try {
            jogo = (IMemoriaJogo) Naming.lookup("rmi://localhost/MemoriaJogo");
            meuId = jogo.registrarJogador();
            if (meuId == -1) {
                JOptionPane.showMessageDialog(this, "Jogo cheio!");
                System.exit(0);
            }
            setTitle("Jogo da Memória - Eu sou o Jogador " + meuId);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao conectar: " + e.getMessage());
            System.exit(1);
        }
    }

    private void iniciarGameLoop() {
        Timer timer = new Timer(500, (ActionEvent e) -> atualizarInterface());
        timer.start();
    }

    private void atualizarInterface() {
        if (jogoFinalizado || bloqueado) return; // Se estiver no meio do delay de 0.7s, não reseta os botões

        try {
            String msgEstado = jogo.obterMensagemEstado();
            
            // Verifica se deve mostrar tela de espera ou jogo
            if (msgEstado.contains("Aguardando")) {
                cardLayout.show(this.getContentPane(), "ESPERA");
                return;
            } else {
                cardLayout.show(this.getContentPane(), "JOGO");
            }

            // Atualiza status e vez
            lblStatus.setText(msgEstado);
            int vezAtual = jogo.obterVez();
            
            if (vezAtual == meuId) {
                lblVez.setText("🟢 SUA VEZ!");
                lblVez.setForeground(new Color(0, 150, 0));
            } else {
                lblVez.setText("🔴 Vez do Jogador " + vezAtual);
                lblVez.setForeground(Color.RED);
            }

int[][] tabuleiro = jogo.obterTabuleiro();
        int[][] donos = jogo.obterDonos();

        for (int i = 0; i < LINHAS; i++) {
            for (int j = 0; j < COLUNAS; j++) {
                // Se a carta já foi descoberta permanentemente no servidor
                if (tabuleiro[i][j] > 0) {
                    botoes[i][j].setText(String.valueOf(tabuleiro[i][j]));
                    botoes[i][j].setDono(donos[i][j]);
                    botoes[i][j].setEnabled(false);
                    botoes[i][j].setBackground(Color.WHITE);
                } else {
                    // Só volta para "?" se não for a carta que o jogador clicou agora
                    if (primeiraCarta == null || (primeiraCarta.x != i || primeiraCarta.y != j)) {
                        botoes[i][j].setText("?");
                        botoes[i][j].setDono(0);
                        botoes[i][j].setEnabled(true);
                        botoes[i][j].setBackground(UIManager.getColor("Button.background"));
                    }
                }
            }
        }

            // Atualiza Placar e Verifica Fim de Jogo
            int[] placar = jogo.obterPlacar();
            lblPlacarJ1.setText("Jogador 1: " + placar[0]);
            lblPlacarJ2.setText("Jogador 2: " + placar[1]);

            verificarFimDeJogo(placar);

        } catch (RemoteException e) {
            lblStatus.setText("Perda de conexão...");
        }
    }

    private void verificarFimDeJogo(int[] placar) {
        int totalPares = (LINHAS * COLUNAS) / 2; // 6 pares
        if (placar[0] + placar[1] == totalPares) {
            jogoFinalizado = true;
            String resultado;
            if (placar[0] > placar[1]) resultado = "Vitória do Jogador 1!";
            else if (placar[1] > placar[0]) resultado = "Vitória do Jogador 2!";
            else resultado = "Empate!";
            
            JOptionPane.showMessageDialog(this, 
                "FIM DE JOGO!\n" + resultado + "\nPlacar Final: " + placar[0] + " x " + placar[1]);
        }
    }

private void processarClique(int linha, int coluna) {
    if (bloqueado || jogoFinalizado) return;

    try {
        if (jogo.obterVez() != meuId) return;
        if (primeiraCarta != null && primeiraCarta.x == linha && primeiraCarta.y == coluna) return;

        // Pega o valor real da carta no servidor para exibir imediatamente
        int valorCarta = jogo.obterValorGabarito(linha, coluna);

        if (primeiraCarta == null) {
            // PRIMEIRO CLIQUE
            primeiraCarta = new Point(linha, coluna);
            botoes[linha][coluna].setText(String.valueOf(valorCarta)); // MOSTRA O NÚMERO
            botoes[linha][coluna].setBackground(Color.WHITE);
            botoes[linha][coluna].setDono(meuId); // Mostra a bolinha do jogador atual
        } else {
            // SEGUNDO CLIQUE
            bloqueado = true;
            botoes[linha][coluna].setText(String.valueOf(valorCarta)); // MOSTRA O SEGUNDO NÚMERO
            botoes[linha][coluna].setBackground(Color.WHITE);
            botoes[linha][coluna].setDono(meuId);

            // Criamos um timer para esperar 0.7 segundos antes de processar
            Timer delayTimer = new Timer(700, (ActionEvent e) -> {
                try {
                    boolean acertou = jogo.jogar(meuId, primeiraCarta.x, primeiraCarta.y, linha, coluna);
                    
                    if (!acertou) {
                        // Se errou, limpa a bolinha temporária visualmente
                        botoes[primeiraCarta.x][primeiraCarta.y].setDono(0);
                        botoes[linha][coluna].setDono(0);
                    }
                    
                    primeiraCarta = null;
                    bloqueado = false;
                    atualizarInterface(); // Sincroniza com o servidor
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            });
            delayTimer.setRepeats(false);
            delayTimer.start();
        }
    } catch (RemoteException e) {
        e.printStackTrace();
    }
}

    // --- CLASSE INTERNA PARA O BOTÃO PERSONALIZADO ---
    class CartaButton extends JButton {
        int linha, coluna;
        int dono = 0; // 0=ninguém, 1=J1, 2=J2

        public CartaButton(int linha, int coluna) {
            super("?");
            this.linha = linha;
            this.coluna = coluna;
            setFont(new Font("Arial", Font.BOLD, 24));
            setFocusable(false);
        }

        public void setDono(int d) {
            this.dono = d;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); // Desenha o botão padrão (fundo + texto)

            // Desenha a bolinha no canto superior ESQUERDO (x=2, y=2)
            if (dono == 1 && imgBallP1 != null) {
                g.drawImage(imgBallP1, 5, 5, 15, 15, this);
            } else if (dono == 2 && imgBallP2 != null) {
                g.drawImage(imgBallP2, 5, 5, 15, 15, this);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MemoriaClientGUI().setVisible(true));
    }
}