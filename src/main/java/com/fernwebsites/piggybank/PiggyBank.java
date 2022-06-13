/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */

package com.fernwebsites.piggybank;

import com.fernwebsites.piggybank.Utility.UserSession;
import javax.swing.UIManager;


/**
 *
 * @author brand
 */
public class PiggyBank {
    

    public static void main(String[] args) {
        System.out.println("Hello World!");
        
        UserSession userSession = new UserSession();
        LockScreen lockscreen = new LockScreen(userSession);
        lockscreen.setLocationRelativeTo(null);
        lockscreen.setVisible(true);
        
        UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE);
        
        
    }
            
    
}
