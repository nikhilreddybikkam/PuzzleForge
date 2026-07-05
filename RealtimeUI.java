package humvsai;

import javax.swing.*;
import java.awt.*;

public class RealtimeUI {

    JFrame frame;
    JButton[][] userBtns;
    JButton[][] aiBtns;
    JLabel info;
    Timer gameTimer;
    int timeLeft = 120;

    // ====================== MAIN ======================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new RealtimeUI().setupDialog();
        });
    }

    // ====================== SETUP ======================
    void setupDialog(){

        JTextField name = new JTextField();
        JTextField id = new JTextField();
        JTextField rows = new JTextField("3");
        JTextField cols = new JTextField("3");

        Object[] msg = {
                "Name:", name,
                "ID:", id,
                "Rows:", rows,
                "Cols:", cols
        };

        int ok = JOptionPane.showConfirmDialog(
                null,msg,"Game Setup",
                JOptionPane.OK_CANCEL_OPTION);

        if(ok != JOptionPane.OK_OPTION)
            System.exit(0);

        try{
            DijakPaths.userName = name.getText();
            DijakPaths.userId = id.getText();
            DijakPaths.rows = Integer.parseInt(rows.getText());
            DijakPaths.cols = Integer.parseInt(cols.getText());

            if(DijakPaths.rows==1 && DijakPaths.cols==1){
                JOptionPane.showMessageDialog(null,"Invalid board size");
                setupDialog();
                return;
            }

            if(DijakPaths.rows * DijakPaths.cols > 20){
                JOptionPane.showMessageDialog(null,"Board too large");
                setupDialog();
                return;
            }

            DijakPaths.GOAL =
                    (1<<(DijakPaths.rows*DijakPaths.cols))-1;

            chooseLevel();

        }catch(Exception e){
            JOptionPane.showMessageDialog(null,"Invalid Input");
            setupDialog();
        }
    }

    // ====================== LEVEL ======================
    void chooseLevel(){

        String[] levels = {"Easy","Medium","Hard"};

        int ch = JOptionPane.showOptionDialog(
                null,"Select Level",
                "Level",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,levels,levels[0]);

        if(ch==-1) System.exit(0);

        DijakPaths.levelName = levels[ch];

        mainMenu();
    }

    // ====================== MENU ======================
    void mainMenu(){

        String[] menu = {"Start Game","Change Level","Exit"};

        int ch = JOptionPane.showOptionDialog(
                null,"Main Menu",
                "Menu",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,menu,menu[0]);

        if(ch==0){
            launchGame();
        }
        else if(ch==1){
            chooseLevel();
        }
        else{
            System.exit(0);
        }
    }

    // ====================== GAME UI ======================
    void launchGame(){

        DijakPaths.startNewGame();

        frame = new JFrame("Human vs AI Solver");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel boards = new JPanel(new GridLayout(1,2,20,20));

        userBtns = new JButton[DijakPaths.rows][DijakPaths.cols];
        aiBtns   = new JButton[DijakPaths.rows][DijakPaths.cols];

        boards.add(buildBoard(userBtns,true));
        boards.add(buildBoard(aiBtns,false));

        info = new JLabel("",SwingConstants.CENTER);

        // ---------- Control Buttons ----------
        JPanel controls = new JPanel();

        JButton best = new JButton("Reveal Best Move");
        JButton undo = new JButton("Undo"); 
        JButton restart = new JButton("Restart");
        JButton menu = new JButton("Menu");

        best.addActionListener(e->{
            int mv = DijakPaths.getOptimalMoveDijkstra(
                    DijakPaths.userBoard);

            JOptionPane.showMessageDialog(frame,
                    "Best Move: "
                            +(mv/DijakPaths.cols)
                            +","
                            +(mv%DijakPaths.cols));
        });
        undo.addActionListener(e -> {           
            DijakPaths.undoLastMove();
            refresh();
        });

        restart.addActionListener(e->{
            DijakPaths.startNewGame();
            startTimer(); 
            refresh();
        });

        menu.addActionListener(e->{
            frame.dispose();
            stopTimer(); 
            mainMenu();
        });

        controls.add(best);
        controls.add(undo); 
        controls.add(restart);
        controls.add(menu);

        frame.add(info,BorderLayout.NORTH);
        frame.add(boards,BorderLayout.CENTER);
        frame.add(controls,BorderLayout.SOUTH);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        refresh();
        startTimer(); 
    }

    // ====================== BOARD ======================
    JPanel buildBoard(JButton[][] btns, boolean user){

        JPanel panel = new JPanel(
                new GridLayout(DijakPaths.rows,DijakPaths.cols));

        for(int r=0;r<DijakPaths.rows;r++){
            for(int c=0;c<DijakPaths.cols;c++){

                JButton b = new JButton();
                b.setPreferredSize(new Dimension(60,60));
                b.setOpaque(true);
                b.setBorderPainted(true);
                b.setFocusPainted(false);
                b.setFont(new Font("Arial", Font.BOLD, 16));

                int rr=r, cc=c;

                if(user){
                    b.addActionListener(e->{
                    	
                    	DijakPaths.pushUndoState();
                    	int pos = rr * DijakPaths.cols + cc;
                        if((DijakPaths.userUsedMask & (1 << pos)) == 0){
                            DijakPaths.userScore += 100;
                            DijakPaths.userUsedMask |= (1 << pos);
                        }else{
                            DijakPaths.userScore += 50;
                        } 
                        DijakPaths.flip(
                                DijakPaths.userBoard,rr,cc);

                        DijakPaths.userFlipCount++;
                        DijakPaths.aiMove();

                        refresh();
                        checkWin();
                    });
                }else {
                	b.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)); 
                }

                btns[r][c] = b;
                panel.add(b);
            }
        }
        return panel;
    }
    void startTimer(){
        stopTimer();
        timeLeft = 120;

        gameTimer = new Timer(1000, e -> {
            timeLeft--;
            refresh();

            if(timeLeft <= 0){
                stopTimer();
                showGameOverDialog("TIME OVER! AI WON \nScore: " + DijakPaths.userScore);
            }
        });
        gameTimer.start();
    }
    void showGameOverDialog(String message){

        String[] options = {"Restart", "Change Level", "Exit"};

        int ch = JOptionPane.showOptionDialog(
                frame,
                message + "\nWhat do you want to do?",
                "Game Over",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        if(ch == 0){
            DijakPaths.startNewGame();
            timeLeft=120;
            startTimer();
            refresh();
        }
        else if(ch == 1){
            stopTimer();
            frame.dispose();
            chooseLevel();
        }
        else{
            System.exit(0);
        }
    }
    void stopTimer(){
        if(gameTimer != null){
            gameTimer.stop();
            gameTimer = null;
        }
    }

    // ====================== REFRESH ======================
    void refresh(){

        for(int r=0;r<DijakPaths.rows;r++){
            for(int c=0;c<DijakPaths.cols;c++){

                userBtns[r][c].setText(
                        String.valueOf(
                                DijakPaths.userBoard[r][c]));

                aiBtns[r][c].setText(
                        String.valueOf(
                                DijakPaths.aiBoard[r][c]));
            }
        }

        info.setText(
        	    "Score: " + DijakPaths.userScore +
        	    "   Time Left: " + timeLeft + "s" +
        	    "   User Moves: " + DijakPaths.userFlipCount +
        	    "   AI Moves: " + DijakPaths.aiFlipCount +
        	    "   Level: " + DijakPaths.levelName
        	);
        }

    // ====================== WIN CHECK ======================
    void checkWin(){

        boolean userWin = DijakPaths.isSolved(DijakPaths.userBoard);
        boolean aiWin   = DijakPaths.isSolved(DijakPaths.aiBoard);

        if(!userWin && !aiWin) return;

        String msg = userWin ? "USER WON! " : "AI WON! ";

        String[] options = {"Restart", "Change Level", "Exit"};

        int ch = JOptionPane.showOptionDialog(
                frame,
                msg + "\nWhat do you want to do?",
                "Game Over",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        if(ch == 0){ 
            DijakPaths.startNewGame();
            refresh();
        }
        else if(ch == 1){
            frame.dispose();
            chooseLevel();   
        }
        else { 
            System.exit(0);
        }
    }
}
