package program;

import engine.general.GameEngine;

public class Main {

    public static void main(String[] args) {
        Program program = new Program();


        GameEngine gameEngine = null;

        try {
            gameEngine = new GameEngine("Slime",1920,1080,false,program);
        } catch (Exception e) {
            e.printStackTrace();
        }
        new Thread(gameEngine).start();

    }
}
