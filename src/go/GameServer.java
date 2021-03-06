package go;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import State.*;

public class GameServer extends Thread {
    boolean isGameOver = false;
    public BufferedReader blackIs = null;
    public PrintWriter blackOs = null;
    public BufferedReader whiteIs = null;
    public PrintWriter whiteOs = null;
    public int passcounter= 0;

    GameState state = States.BLACK_MOVE.getStateBehavior();
    int size=19;

    private String name1;
    private String name2;

    public GameBoard board;

    public GameServer(GameRoom.SocketHandler player1Handler, GameRoom.SocketHandler player2Handler, String name1, String name2) {
        board = new GameBoard(size);
        blackIs = player1Handler.in;
        blackOs = player1Handler.out;
        whiteIs = player2Handler.in;
        whiteOs = player2Handler.out;
        this.name1=name1;
        this.name2=name2;

        blackOs.println("BLACK");
        blackOs.flush();
        whiteOs.println("WHITE");
        whiteOs.flush();
        run();
    }

    public boolean pass(){
        passcounter++;
        return false;
    }
    public void close() {
        try {
            blackIs.close();
            blackOs.close();
            whiteIs.close();
            whiteOs.close();
            isGameOver = true;
            state = States.WIN.getStateBehavior();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        //System.exit(0);
    }


    public String listenWhite() throws IOException{
        String msg = whiteIs.readLine();
        if(msg.equals("SURRENDER")){
            sendToBlack("YOU_WON_SURR");
            isGameOver=true;
            close();
            state = States.WIN.getStateBehavior();
        }
        return msg;

    }
    public String listenBlack() throws IOException{
        String msg = blackIs.readLine();
        if(msg.equals("SURRENDER")){
            sendToWhite("YOU_WON_SURR");
            isGameOver=true;
            close();
            state = States.WIN.getStateBehavior();
        }
        return msg;
    }

    public void sendToWhite(String content){
        whiteOs.println(content);
        whiteOs.flush();
    }
    public void sendToBlack(String content){
        blackOs.println(content);
        blackOs.flush();
    }

    public void run() {

        if (passcounter == 2){
            state = States.TERRITORY_MODE.getStateBehavior();
        }


        try {
            while(state.getClass() != WinDialog.class){
                state = state.perform(this);
            }
            if(!isGameOver)
                state = state.perform(this);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally{
            close();
        }
    }
}