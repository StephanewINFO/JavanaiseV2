/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package burstTest;

import irc.IFoo;
import irc.Irc;
import irc.Sentence;
import java.util.Random;

/**
 *
 * @author stephanie
 */
public class BurstMode {

    IFoo instance;

    private BurstMode() {
        this.instance = (IFoo) jvn.JvnInvocationHandler.newInstance(new Sentence());
    }

    public static void main(String[] args) {

        BurstMode bm = new BurstMode();

        Random random = new Random();

        while (true) {

            int nombreR = random.nextInt(2);

            switch (nombreR) {
                case 0:
                    bm.instance.read();
                    break;

                case 1:
                    bm.instance.write("test burst..");
                    break;

            }
        }
    }

}
