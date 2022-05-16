/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.eco405.blockbreaker.visual;

import br.edu.eco405.blockbreaker.modelo.Bloco;
import br.edu.eco405.blockbreaker.modelo.Bola;
import br.edu.eco405.blockbreaker.modelo.GameObject;
import br.edu.eco405.blockbreaker.modelo.Plataforma;
import br.edu.eco405.blockbreaker.modelo.PlayerObject;
import br.edu.eco405.blockbreaker.modelo.PoderBola;
import br.edu.eco405.blockbreaker.modelo.PoderPlataforma;
import br.edu.eco405.blockbreaker.modelo.User;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.util.ArrayList;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author Márcio
 */
public class PanelJogo extends JPanel {

    /* Debugger */
    public static boolean D = false;
    public static boolean U = true;

    /* Constantes */
    public static final int FPS = 60;
    public static final int INIT_LIFE = 3;

    public static final int BOLA_X = 290;
    public static final int BOLA_Y = 499;
    public static final int BOLA_MASK_WIDTH = 20;
    public static final int BOLA_MASK_HEIGHT = 20;
    public static final double BOLA_VELOCIDADE = 5.0;
    public static final double BOLA_DIRECAO_X = 1.0;
    public static final double BOLA_DIRECAO_Y = 1.0;
    public static final int BOLA_DANO = 1;

    public static int PLAT_X = 250;
    public static int PLAT_Y = 520;
    public static final int PLAT_MASK_WIDTH = 100;
    public static final int PLAT_MASK_HEIGHT = 20;
    public static final double PLAT_VELOCIDADE = 10.0;

    /* Componentes do jogo */
    private User user;
    public static Timer t;

    private Bola bola;
    public static ArrayList<Bola> bolas;
    private Plataforma plataforma;
    private ArrayList<Bloco> blocos;

    private ImageIcon imageBackground;
    private ImageIcon imageLife;
    private ImageIcon imageInstructions;

    private GameObject paredeCima;
    private GameObject paredeBaixo;
    private GameObject paredeDir;
    private GameObject paredeEsq;

    private File fileMapa;

    // estado do jogo
    public static enum STATE {

        MENU,
        GAME,
        PAUSE,
        GAME_OVER
    };

    public static STATE state = STATE.MENU; // jogo começa no menu principal

    /* Variaveis auxiliares */
    //private int sx = 1;                     // direção em X
    //private int sy = 1;                     // direção em Y
    private int[] setas = new int[2];       // vetor para evitar delay do teclado

    private boolean flagRun;        // flag para quando o usuario inicia o jogo
    private boolean flagWin;
    private boolean flagInstructions;
    private double velocidadePlat;             // velocidade da plataforma

    /* Construtor da JPanel */
    public PanelJogo() throws IOException {

        this.setDoubleBuffered(true);   // dobra o buffer para desenhar
        initGame();                     // inicia o jogo
    }

    private void initGame() throws FileNotFoundException, IOException {
        /* Inicializa as componentes do jogo */

        flagRun = false;
        flagInstructions = true;
        flagWin = false;

        user = new User();
        user.setScore(0);

        /* Inicia a bola */
        bolas = new ArrayList<>();
        bola = new Bola();
        bola.setX(BOLA_X);
        bola.setY(BOLA_Y);
        bola.setMask(new Rectangle(bola.getX(), bola.getY(), BOLA_MASK_WIDTH, BOLA_MASK_HEIGHT));
        bola.setVelocidade(BOLA_VELOCIDADE);
        bola.setDirecaoX(BOLA_DIRECAO_X);
        bola.setDirecaoY(BOLA_DIRECAO_Y);
        bola.setDano(BOLA_DANO);
        bola.setState(Bola.STATE.NORMAL);
        bola.getRender().add(new ImageIcon("res/bolas/bolaA.png"));
        bola.getRender().add(new ImageIcon("res/bolas/bolaB.png"));
        bola.getRender().add(new ImageIcon("res/bolas/bolaC.png"));
        bolas.add(bola);

        /* Inicia a plataforma */
        plataforma = new Plataforma();
        plataforma.setLife(INIT_LIFE);
        plataforma.setX(PLAT_X);
        plataforma.setY(PLAT_Y);
        plataforma.setMask(new Rectangle(plataforma.getX(), plataforma.getY(), PLAT_MASK_WIDTH, PLAT_MASK_HEIGHT));
        plataforma.setVelocidade(PLAT_VELOCIDADE);
        plataforma.getRender().add(new ImageIcon("res/plataformas/plataformaA.png"));
        plataforma.getRender().add(new ImageIcon("res/plataformas/plataformaB.png"));
        plataforma.getRender().add(new ImageIcon("res/plataformas/plataformaC.png"));
        plataforma.setState(Plataforma.STATE.NORMAL);

        /* Inicia o mapa */
        fileMapa = new File("res/mapas/mapa1.txt");
        if (!fileMapa.exists()) {
            System.out.println("Arquivo de mapa não encontrado! Encerrando...");
            this.setVisible(false); // poderia fechar o programa
        }

        blocos = new ArrayList<>();
        FileReader frMapa = new FileReader(fileMapa);
        BufferedReader bfMapa = new BufferedReader(frMapa);
        String[] strMapa = new String[10];
        int k = 0;
        while (bfMapa.ready()) {
            strMapa[k++] = bfMapa.readLine();
        }

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 7; j++) {
                Bloco b = new Bloco();
                // seta as posicões do bloco

                b.setX(7 + 90 * j);
                b.setY(80 + 30 * i);

                // le o arquivo de mapa e ve o tipo de bloco
                switch (strMapa[i].charAt(j)) {
                    case '#': // bloco indestruivel
                        b.setDestrutivel(false); // bloco indestrutível
                        b.setVida(-1);
                        b.setPowerfull(false);
                        b.setPoder(null);
                        b.setMessagePoder(null);
                        b.setState(Bloco.STATE.NORMAL);
                        b.getRender().add(new ImageIcon("res/blocos/blocoIndestrutivel.png"));
                        break;

                    case '1': // bloco destruivel - comum
                        b.setDestrutivel(true);
                        b.setVida(3);
                        b.setPowerfull(false);
                        b.setPoder(null);
                        b.setMessagePoder(null);
                        b.setState(Bloco.STATE.NORMAL);
                        b.getRender().add(new ImageIcon("res/blocos/bloco1A.png"));
                        b.getRender().add(new ImageIcon("res/blocos/bloco1B.png"));
                        b.getRender().add(new ImageIcon("res/blocos/bloco1C.png"));
                        break;

                    case '2': // bloco destruivel - poder plataforma: aumenta vida 
                        b.setDestrutivel(true);
                        b.setVida(3);
                        b.setPowerfull(true);
                        b.setPoder(new PoderPlataforma());
                        b.setMessagePoder("aumenta_vida");
                        b.setState(Bloco.STATE.NORMAL);
                        b.getRender().add(new ImageIcon("res/blocos/bloco2A.png"));
                        b.getRender().add(new ImageIcon("res/blocos/bloco2B.png"));
                        b.getRender().add(new ImageIcon("res/blocos/bloco2C.png"));
                        break;
                    case '3': // bloco destruivel - poder plataforma: aumenta velocidade da plat
                        b.setDestrutivel(true);
                        b.setVida(3);
                        b.setPowerfull(true);
                        b.setPoder(new PoderPlataforma());
                        b.setMessagePoder("acelera");
                        b.setState(Bloco.STATE.NORMAL);
                        b.getRender().add(new ImageIcon("res/blocos/bloco3A.png"));
                        b.getRender().add(new ImageIcon("res/blocos/bloco3B.png"));
                        b.getRender().add(new ImageIcon("res/blocos/bloco3C.png"));
                        break;
                    case '4': // bloco destruivel - poder plataforma: aumenta tamanho da plataforma
                        b.setDestrutivel(true);
                        b.setVida(3);
                        b.setPowerfull(true);
                        b.setPoder(new PoderPlataforma());
                        b.setMessagePoder("aumenta_tamanho");
                        b.setState(Bloco.STATE.NORMAL);
                        b.getRender().add(new ImageIcon("res/blocos/bloco4A.png"));
                        b.getRender().add(new ImageIcon("res/blocos/bloco4B.png"));
                        b.getRender().add(new ImageIcon("res/blocos/bloco4C.png"));
                        break;
                    case '5': // bloco destruivel - poder bola: aumenta dano da bola
                        b.setDestrutivel(true);
                        b.setVida(3);
                        b.setPowerfull(true);
                        b.setPoder(new PoderBola());
                        b.setMessagePoder("aumenta_dano");
                        b.setState(Bloco.STATE.NORMAL);
                        b.getRender().add(new ImageIcon("res/blocos/bloco5A.png"));
                        b.getRender().add(new ImageIcon("res/blocos/bloco5B.png"));
                        b.getRender().add(new ImageIcon("res/blocos/bloco5C.png"));
                        break;
                    case '6': // bloco destruivel - poder bola: aumenta tamanho da bola
                        b.setDestrutivel(true);
                        b.setVida(3);
                        b.setPowerfull(true);
                        b.setPoder(new PoderBola());
                        b.setMessagePoder("aumenta_tamanho");
                        b.setState(Bloco.STATE.NORMAL);
                        b.getRender().add(new ImageIcon("res/blocos/bloco6A.png"));
                        b.getRender().add(new ImageIcon("res/blocos/bloco6B.png"));
                        b.getRender().add(new ImageIcon("res/blocos/bloco6C.png"));
                        break;
                    case '7': // bloco destruivel - poder bola: aumenta velocidade
                        b.setDestrutivel(true);
                        b.setVida(3);
                        b.setPowerfull(true);
                        b.setPoder(new PoderBola());
                        b.setMessagePoder("acelera");
                        b.setState(Bloco.STATE.NORMAL);
                        b.getRender().add(new ImageIcon("res/blocos/bloco7A.png"));
                        b.getRender().add(new ImageIcon("res/blocos/bloco7B.png"));
                        b.getRender().add(new ImageIcon("res/blocos/bloco7C.png"));
                        break;
                    case '8': // bloco destruivel - poder bola: aumenta qnt de bolas
                        b.setDestrutivel(true);
                        b.setVida(3);
                        b.setPowerfull(true);
                        b.setPoder(new PoderBola());
                        b.setMessagePoder("aumenta_quant");
                        b.setState(Bloco.STATE.NORMAL);
                        b.getRender().add(new ImageIcon("res/blocos/bloco8A.png"));
                        b.getRender().add(new ImageIcon("res/blocos/bloco8B.png"));
                        b.getRender().add(new ImageIcon("res/blocos/bloco8C.png"));
                        break;
                    case '-':
                        continue;
                }

                b.setMask(new Rectangle(b.getX(), b.getY(), 80, 20));
                blocos.add(b);
            }
        }

        /* Outro elementos do jogo */
        imageBackground = new ImageIcon("res/background.jpg");
        imageLife = new ImageIcon("res/life.png");
        imageInstructions = new ImageIcon("res/interface/instrucoes.png");

        paredeCima = new GameObject();
        paredeCima.setMask(new Rectangle(1, 25, 632, 1));
        paredeCima.setX(paredeCima.getMaskX());
        paredeCima.setY(paredeCima.getMaskY());

        paredeBaixo = new GameObject();
        paredeBaixo.setMask(new Rectangle(1, 559, 632, 1));
        paredeBaixo.setX(paredeBaixo.getMaskX());
        paredeBaixo.setY(paredeBaixo.getMaskY());

        paredeEsq = new GameObject();
        paredeEsq.setMask(new Rectangle(1, 26, 1, 533));
        paredeEsq.setX(paredeEsq.getMaskX());
        paredeEsq.setY(paredeEsq.getMaskY());

        paredeDir = new GameObject();
        paredeDir.setMask(new Rectangle(632, 26, 1, 533));
        paredeDir.setX(paredeDir.getMaskX());
        paredeDir.setY(paredeDir.getMaskY());

        if (U) {
            SoundEffects.init();
        }

        this.addMouseListener(new MouseInput());
        this.addMouseMotionListener(new MouseMotion());

        loop();
        inputData();
    }

    void loop() {

        t = new Timer(1000 / FPS, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {

                /* Verifica se o jogo reiniciou */
                /* Verifica se o jogador morreu */
                if (plataforma.getLife() == 0) {
                    if (D) {
                        System.out.printf("PERDEU O JOGO\n");
                        System.out.printf("Bola: x = %d y = %d\nPlataforma: x = %d y = %d\n\n",
                                bola.getX(), bola.getY(), plataforma.getX(), plataforma.getY());
                    }
                    if (U) {
                        SoundEffects.GAME_OVER.play();
                    }
                    state = STATE.GAME_OVER;
                    t.stop();
                }

                /* Verifica se o jogador ganhou */
                if (blocos.isEmpty()) {
                    if (D) {
                        System.out.printf("GANHOU O JOGO\n");
                        System.out.printf("Bola: x = %d y = %d\nPlataforma: x = %d y = %d\n\n",
                                bola.getX(), bola.getY(), plataforma.getX(), plataforma.getY());
                    }
                    flagWin = true;
                    t.stop();
                }

                /* Calcula o próximo movimento */
                if (flagRun) {
                    for (int i = 0; i < bolas.size(); i++) {
                        bolas.get(i).setMaskX((int) (bolas.get(i).getMaskX() + bolas.get(i).getDirecaoX() * bolas.get(i).getVelocidade()));
                        bolas.get(i).setMaskY((int) (bolas.get(i).getMaskY() + bolas.get(i).getDirecaoY() * bolas.get(i).getVelocidade()));
                    }
                } else {
                    //bola.setMaskX(bola.getMaskX() + setas[1] * bola.getVelocidade() - setas[0] * bola.getVelocidade());
                    bolas.get(0).setMaskX(plataforma.getMaskX() + 40);
                }

                /* aceleração da plataforma */
                /* uso uma variavel auxiliar para velocidade da plataforma
                 * pois sem ela, o efeito de aceleração cancelaria o
                 * power-up de velocidade
                 */
                if (setas[1] == 1 || setas[0] == 1) {
                    velocidadePlat += .5;
                    if (velocidadePlat > (plataforma.getVelocidade() + 20)) {
                        velocidadePlat = (plataforma.getVelocidade() + 20);
                    }
                    if (setas[1] == 1 && setas[0] == 1) {
                        velocidadePlat = plataforma.getVelocidade();
                    }
                } else {
                    velocidadePlat = plataforma.getVelocidade();
                }

                plataforma.setMaskX((int) (plataforma.getMaskX() + setas[1] * velocidadePlat
                        - setas[0] * velocidadePlat));

                /* Detecta as colisões */
                // Colisão bola - parede
                for (int i = 0; i < bolas.size(); i++) {
                    if (colisao(bolas.get(i), paredeEsq)) {
                        if (D) {
                            System.out.printf("COLISAO BOLA - PAREDE ESQ\n");
                            System.out.printf("Bola: x = %d y = %d\nPlataforma: x = %d y = %d\n\n",
                                    bola.getX(), bola.getY(), plataforma.getX(), plataforma.getY());
                        }

                        if (U) {
                            SoundEffects.BALL.play();
                        }

                    }
                }
                for (int i = 0; i < bolas.size(); i++) {
                    if (colisao(bolas.get(i), paredeDir)) {
                        if (D) {
                            System.out.printf("COLISAO BOLA - PAREDE DIR\n");
                            System.out.printf("Bola: x = %d y = %d\nPlataforma: x = %d y = %d\n\n",
                                    bola.getX(), bola.getY(), plataforma.getX(), plataforma.getY());
                        }

                        if (U) {
                            SoundEffects.BALL.play();
                        }

                    }
                }
                for (int i = 0; i < bolas.size(); i++) {
                    if (colisao(bolas.get(i), paredeCima)) {
                        if (D) {
                            System.out.printf("COLISAO BOLA - PAREDE CIMA\n");
                            System.out.printf("Bola: x = %d y = %d\nPlataforma: x = %d y = %d\n\n",
                                    bola.getX(), bola.getY(), plataforma.getX(), plataforma.getY());
                        }

                        if (U) {
                            SoundEffects.BALL.play();
                        }

                    }
                }

                for (int i = 0; i < bolas.size(); i++) {
                    if (colisao(bolas.get(i), paredeBaixo)) {

                        if (D) {
                            System.out.printf("PERDEU UMA VIDA\n");
                            System.out.printf("Bola: x = %d y = %d\nPlataforma: x = %d y = %d\n\n",
                                    bola.getX(), bola.getY(), plataforma.getX(), plataforma.getY());
                        }

                        // se colidir com a parede debaixo
                        // remove a bola que caiu la
                        bolas.remove(i);

                        if (bolas.isEmpty()) {

                            // perde uma vida
                            plataforma.setLife(plataforma.getLife() - 1);
                            // volta a bola e plataforma na posição inicial
                            flagRun = false;
                            bola.setMaskWidth(20);
                            bola.setMaskHeight(20);
                            bola.setState(Bola.STATE.NORMAL);
                            bola.setMaskX(290);
                            bola.setMaskY(499);
                            bola.setDano(1);
                            bola.setVelocidade(BOLA_VELOCIDADE);
                            bola.setDirecaoX(1);
                            bola.setDirecaoY(1);
                            bolas.add(bola);
                            plataforma.setMaskX(250);
                            plataforma.setMaskY(520);
                            // volta a velocidade da bola e plataforma ao padrão

                            plataforma.setVelocidade(PLAT_VELOCIDADE);
                            plataforma.setMaskWidth(100);
                            plataforma.setState(Plataforma.STATE.NORMAL);
                            // volta ao dano padrão

                            if ((plataforma.getLife() > 0) && U) {
                                SoundEffects.LIFE_LOST.play();
                            }
                        }

                    }
                }

                // Colisão bola - plataforma
                for (int i = 0; i < bolas.size(); i++) {
                    if (colisao(bolas.get(i), plataforma)) {
                        if (D) {
                            System.out.printf("COLISAO BOLA - PLATAFORMA\n");
                            System.out.printf("Bola: x = %d y = %d\nPlataforma: x = %d y = %d\n\n",
                                    bola.getX(), bola.getY(), plataforma.getX(), plataforma.getY());
                        }

                        if (U) {
                            SoundEffects.BALL.play();
                        }

                        // transfere 5% da velocidade da plataforma para a bola e muda angulo
                        if (setas[0] == 1 || setas[1] == 1) {
                            bolas.get(i).setVelocidade((int) (bolas.get(i).getVelocidade() + velocidadePlat * 0.05));
                            bolas.get(i).setDirecaoX(bolas.get(i).getDirecaoX() * 0.8);
                        } else {
                            // volta para 1 ou -1
                            if (bolas.get(i).getDirecaoX() < 0) {
                                bolas.get(i).setDirecaoX(-1.0);
                            } else {
                                bolas.get(i).setDirecaoX(1.0);
                            }
                        }

                    }
                }

                // Colisão plataforma - parede
                if (colisao(plataforma, paredeEsq)) {
                    if (D) {
                        System.out.printf("COLISAO PLATAFORMA - PAREDE ESQ\n");
                        System.out.printf("Bola: x = %d y = %d\nPlataforma: x = %d y = %d\n\n",
                                bola.getX(), bola.getY(), plataforma.getX(), plataforma.getY());
                    }
                }
                if (colisao(plataforma, paredeDir)) {
                    if (D) {
                        System.out.printf("COLISAO PLATAFORMA - PAREDE DIR\n");
                        System.out.printf("Bola: x = %d y = %d\nPlataforma: x = %d y = %d\n\n",
                                bola.getX(), bola.getY(), plataforma.getX(), plataforma.getY());
                    }
                }

                // Colisão bola - bloco
                for (int i = 0; i < bolas.size(); i++) {
                    for (int j = 0; j < blocos.size(); j++) {
                        if (colisao(bolas.get(i), blocos.get(j))) {
                            if (D) {
                                System.out.printf("COLISAO BOLA - BLOCO %d\n", i);
                                System.out.printf("Bola: x = %d y = %d\nPlataforma: x = %d y = %d\n\n",
                                        bola.getX(), bola.getY(), plataforma.getX(), plataforma.getY());
                            }

                            if (blocos.get(j).isDestrutivel()) { // se o bloco for destrutível

                                // sonzinho da zueira
                                if (U) {
                                    SoundEffects.EXPLODE.play();
                                }

                                user.setScore(user.getScore() + 100); // aumenta o score
                                blocos.get(j).setVida(blocos.get(j).getVida() - bolas.get(i).getDano()); // tira dano de vida

                                // se o bloco tiver poder, ative-o e depois remova-o
                                if (blocos.get(j).isPowerfull()) {

                                    if (U) {
                                        SoundEffects.POWER.play();
                                    }

                                    if (blocos.get(j).getPoder() instanceof PoderBola) {
                                        blocos.get(j).getPoder().poder(bolas.get(i), blocos.get(j).getMessagePoder());
                                    } else {
                                        blocos.get(j).getPoder().poder(plataforma, blocos.get(j).getMessagePoder());
                                    }

                                    blocos.get(j).setPowerfull(false);
                                    blocos.get(j).setPoder(null);
                                    blocos.get(j).setMessagePoder(null);
                                }

                                switch (blocos.get(j).getVida()) {

                                    case 1: {
                                        blocos.get(j).setState(Bloco.STATE.MUITO_DANIFICADO);
                                        break;
                                    }
                                    case 2: {
                                        blocos.get(j).setState(Bloco.STATE.DANIFICADO);
                                        break;
                                    }
                                }

                                if (blocos.get(j).getVida() <= 0) {
                                    blocos.get(j).setState(Bloco.STATE.DESTRUIDO);
                                    blocos.remove(j);
                                }
                            } else {
                                if (U) {
                                    SoundEffects.BLOCO_IND_HIT.play();
                                }
                            }
                        }
                    }
                }

                /* Atualiza as coordenadas dos componentes */
                for (int i = 0; i < bolas.size(); i++) {
                    bolas.get(i).setX(bolas.get(i).getMaskX());
                    bolas.get(i).setY(bolas.get(i).getMaskY());
                }

                plataforma.setX(plataforma.getMaskX());
                plataforma.setY(plataforma.getMaskY());

                repaint();
            }

        });

        if (D) {
            System.out.printf("INICIO LOG\n");
            System.out.printf("Bola: x = %d y = %d\nPlataforma: x = %d y = %d\n\n",
                    bola.getX(), bola.getY(), plataforma.getX(), plataforma.getY());
        }

        t.start();
    }

    void inputData() {
        /* Controle do teclado */
        this.setFocusable(true);
        this.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent ke) {
                // code
            }

            @Override
            public void keyPressed(KeyEvent ke) {
                if (state == STATE.GAME || state == STATE.PAUSE) {
                    switch (ke.getKeyCode()) {

                        case KeyEvent.VK_SPACE:
                            if (!flagRun) {
                                if (D) {
                                    System.out.printf("INICIO JOGO\n\n");
                                    System.out.printf("Bola: x = %d y = %d\nPlataforma: x = %d y = %d\n\n",
                                            bola.getX(), bola.getY(), plataforma.getX(), plataforma.getY());
                                }
                                flagRun = true;
                                flagInstructions = false;
                            }
                            break;

                        case KeyEvent.VK_ESCAPE:
                            if (D) {
                                System.out.printf("PAUSA\n\n");
                                System.out.printf("Bola: x = %d y = %d\nPlataforma: x = %d y = %d\n\n",
                                        bola.getX(), bola.getY(), plataforma.getX(), plataforma.getY());
                            }

                            if (U) {
                                SoundEffects.MENU_CLICK.play();
                            }

                            if (state == STATE.GAME) {
                                state = STATE.PAUSE;
                                t.stop();
                                repaint();
                            } else {
                                state = STATE.GAME;
                                t.restart();
                            }
                            break;

                        case KeyEvent.VK_LEFT:
                            if (D) {
                                System.out.printf("DIRECIONAL ESQUERDO\n");
                                System.out.printf("Bola: x = %d y = %d\nPlataforma: x = %d y = %d\n\n",
                                        bola.getX(), bola.getY(), plataforma.getX(), plataforma.getY());
                            }
                            setas[0] = 1;
                            break;

                        case KeyEvent.VK_RIGHT:
                            if (D) {
                                System.out.printf("DIRECIONAL DIREITO\n");
                                System.out.printf("Bola: x = %d y = %d\nPlataforma: x = %d y = %d\n\n",
                                        bola.getX(), bola.getY(), plataforma.getX(), plataforma.getY());
                            }
                            setas[1] = 1;
                            break;
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent ke) {
                if (state == STATE.GAME || state == STATE.PAUSE) {
                    switch (ke.getKeyCode()) {
                        case KeyEvent.VK_LEFT:
                            setas[0] = 0;
                            break;
                        case KeyEvent.VK_RIGHT:
                            setas[1] = 0;
                            break;
                    }
                }
            }

        });
    }

    // args: retangulo que vai mudar, retangulo que vai colidir
    public boolean colisao(PlayerObject a, GameObject b)
    {
        Rectangle rect1 = a.getMask();
        Rectangle rect2 = b.getMask();
        if (rect1.intersects(rect2))//are they colliding?
        {//collision=true
            double leftOverlap = rect1.x + rect1.width - rect2.x;
            double rightOverlap = rect2.x + rect2.width - rect1.x;
            double topOverlap = rect1.y + rect1.height - rect2.y;
            double botOverlap = rect2.y + rect2.height - rect1.y;

            double smallestOverlap = Double.MAX_VALUE;
            double shiftX = 0;
            double shiftY = 0;

            if (leftOverlap < smallestOverlap) {
                smallestOverlap = leftOverlap;
                shiftX = -leftOverlap;
                shiftY = 0;

            }
            if (rightOverlap < smallestOverlap) {
                smallestOverlap = rightOverlap;
                shiftX = rightOverlap;
                shiftY = 0;

            }
            if (topOverlap < smallestOverlap) {
                smallestOverlap = topOverlap;
                shiftX = 0;
                shiftY = -topOverlap;

            }
            if (botOverlap < smallestOverlap) {
                smallestOverlap = botOverlap;
                shiftX = 0;
                shiftY = botOverlap;

            }

            rect1.x += shiftX;
            rect1.y += shiftY;
            if (shiftX != 0) {

                a.setDirecaoX(a.getDirecaoX() * (-1));

            } else {

                a.setDirecaoY(a.getDirecaoY() * (-1));

            }
            return true;
        }
        return false;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // antialiasing
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (D) {

            plataforma.setLife(-1);

            // Desenha a bola e sua posição x,y
            for (int i = 0; i < bolas.size(); i++) {
                g2d.setColor(Color.RED);
                g2d.drawOval(bolas.get(i).getX(), bolas.get(i).getY(), bolas.get(i).getMask().width, bolas.get(i).getMask().height);

                g2d.setColor(Color.WHITE);
                g2d.fillRect(bolas.get(i).getX() + 23, bolas.get(i).getY() - 5, 50, 13);
                g2d.setColor(Color.BLACK);
                g.drawString(bolas.get(i).getX() + "," + bolas.get(i).getX(), bolas.get(i).getX() + 25, bolas.get(i).getY() + 5);
            }

            // Desenha a plataforma e sua posição x, y
            g2d.setColor(Color.MAGENTA);
            g2d.drawRect(plataforma.getX(), plataforma.getY(), plataforma.getMask().width, plataforma.getMask().height);

            g2d.setColor(Color.WHITE);
            g2d.fillRect(plataforma.getX() + 103, plataforma.getY() - 5, 50, 13);
            g2d.setColor(Color.BLACK);
            g.drawString(plataforma.getX() + "," + plataforma.getX(), plataforma.getX() + 105, plataforma.getY() + 5);

            // Desenha os blocos
            for (int i = 0; i < blocos.size(); i++) {
                g2d.setColor(Color.BLUE);
                g2d.drawRect(blocos.get(i).getX(), blocos.get(i).getY(), blocos.get(i).getMask().width, blocos.get(i).getMask().height);
            }

            /* parede */
            g2d.setColor(Color.BLACK);
            g2d.draw(paredeCima.getMask());
            g2d.draw(paredeBaixo.getMask());
            g2d.draw(paredeDir.getMask());
            g2d.draw(paredeEsq.getMask());

            /* INFO */
            g2d.drawString("STATE" + " = " + state, 1, 15);
            g2d.drawString("SCORE" + " = " + String.format("%08d", user.getScore()), 100, 15);
            g2d.drawString("LIFE" + " = " + plataforma.getLife(), 230, 15);
            g2d.drawString("VEL BOLA" + " = " + bola.getVelocidade(), 300, 15);
            g2d.drawString("VEL PLAT" + " = " + velocidadePlat, 400, 15);

            if (flagWin) {

                g2d.setColor(Color.YELLOW);
                g2d.fillRect(150, 235, 320, 70);
                g2d.setColor(Color.RED);
                g2d.setFont(new Font("Arial", Font.PLAIN, 36));
                g2d.drawString("YOU WIN!", 180, 280);
            }

            switch (state) {
                case MENU:
                    MainMenu mainMenu = new MainMenu(g2d, this);
                    mainMenu.paint();
                    break;

                case PAUSE:
                    PauseMenu pauseMenu = new PauseMenu(g2d, this);
                    pauseMenu.paint();
                    break;

                case GAME_OVER:
                    GameOverMenu gameOverMenu = new GameOverMenu(g2d, this);
                    gameOverMenu.paint();
                    break;
            }

        }
        if (U) {

            g2d.drawImage(imageBackground.getImage(), 0, 0, this);

            //  desenha de acordo com o estado do programa
            switch (state) {
                case GAME:
                    for (int i = 0; i < bolas.size(); i++) {
                        g2d.drawImage(bolas.get(i).getRender().get(bolas.get(i).getState().ordinal()).getImage(), bolas.get(i).getX(), bolas.get(i).getY(), this);
                    }
                    g2d.drawImage(plataforma.getRender().get(plataforma.getState().ordinal()).getImage(), plataforma.getX(), plataforma.getY(), this);
                    for (int i = 0; i < blocos.size(); i++) {
                        g2d.drawImage(blocos.get(i).getRender().get(blocos.get(i).getState().ordinal()).getImage(), blocos.get(i).getX(), blocos.get(i).getY(), this);
                    }

                    /* INFO */
                    for (int i = 0; i < plataforma.getLife(); i++) {
                        g2d.drawImage(imageLife.getImage(), 5 + (150 / plataforma.getLife()) * i, 5, this);
                    }

                    // ARRUMAR SCORE DEPOIS
                    g2d.setColor(Color.DARK_GRAY);
                    g2d.setFont(new Font("Arial", Font.PLAIN, 28));
                    g2d.drawString("SCORE" + " " + String.format("%08d", user.getScore()), 333, 50);

                    // MENUS
                    if (flagInstructions) {
                        g2d.drawImage(imageInstructions.getImage(), 70, 150, this);
                    }

                    if (flagWin) {

                        SoundEffects.WIN.play();

                        g2d.setColor(Color.YELLOW);
                        g2d.fillRect(150, 235, 320, 70);
                        g2d.setColor(Color.RED);
                        g2d.setFont(new Font("Arial", Font.PLAIN, 36));
                        g2d.drawString("YOU WIN!", 180, 280);
                    }

                    break;

                case MENU:
                    SoundEffects.OPENING.playLoop();

                    MainMenu mainMenu = new MainMenu(g2d, this);
                    mainMenu.paint();
                    break;

                case PAUSE:

                    PauseMenu pauseMenu = new PauseMenu(g2d, this);
                    pauseMenu.paint();
                    break;

                case GAME_OVER:
                    GameOverMenu gameOverMenu = new GameOverMenu(g2d, this);
                    gameOverMenu.paint();
                    break;

            }
        }
    }
}