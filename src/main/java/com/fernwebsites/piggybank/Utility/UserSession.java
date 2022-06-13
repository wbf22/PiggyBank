/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.fernwebsites.piggybank.Utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author brand
 */
public class UserSession {
    private String userName;
    private String password;
    private SecureRandom random;
    private Object[][] data;
    private Key key;

    public UserSession() {
        random = new SecureRandom();
    }
    
    
    /**
     * 
     * @param userName
     * @param password
     * @return 0 if logged in, 1 if password wrong, 2 if user doesn't exist, 3 error
     */
    public int login(String userName, String password) {
        
        try {
            File user = new File(userName);
            File salt = new File(userName + "Salt");
            if (user.exists() && salt.exists()){
                this.userName = userName;
                this.password = password;

                FileInputStream fileInputStream = new FileInputStream(salt);
                byte[] saltBytes = new byte[64];
                fileInputStream.read(saltBytes);
                fileInputStream.close();

                MessageDigest digest = MessageDigest.getInstance("SHA-512");
                digest.update(saltBytes);
                byte[] encodedHash = digest.digest(
                  password.getBytes(StandardCharsets.UTF_8));


                fileInputStream = new FileInputStream(user);
                byte[] passwordBytes = new byte[64];
                fileInputStream.read(passwordBytes);
                fileInputStream.close();

                int correctPassword = 0;
                for (int i = 0; i < passwordBytes.length; i++) {
                    if (encodedHash[i] != passwordBytes[i]) {
                        correctPassword = 1;
                    }
                }

                return correctPassword;
            } 
        } catch (FileNotFoundException ex) {
            Logger.getLogger(UserSession.class.getName()).log(Level.SEVERE, null, ex);
            return 3;
        } catch (IOException ex) {
            Logger.getLogger(UserSession.class.getName()).log(Level.SEVERE, null, ex);
            return 3;
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(UserSession.class.getName()).log(Level.SEVERE, null, ex);
            return 3;
        }
        
        return 2;
    }
    
    public int createUser(String userName, String password){
        
        try {
            File user = new File(userName);
            File salt = new File(userName + "Salt");
            this.userName = userName;
            this.password = password;

            FileOutputStream fileOutputStream;
            fileOutputStream = new FileOutputStream(salt);
            
            byte[] saltBytes = new byte[64];
            random.nextBytes(saltBytes);
            fileOutputStream.write(saltBytes);
            fileOutputStream.close();

            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            digest.update(saltBytes);
            byte[] encodedhash = digest.digest(
              password.getBytes(StandardCharsets.UTF_8));


            fileOutputStream = new FileOutputStream(user);
            fileOutputStream.write(encodedhash);
            fileOutputStream.close();
            
            return 0;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(UserSession.class.getName()).log(Level.SEVERE, "failed to create user", ex);
            return 3;
        } catch (IOException ex) {
            Logger.getLogger(UserSession.class.getName()).log(Level.SEVERE, null, ex);
            return 3;
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(UserSession.class.getName()).log(Level.SEVERE, null, ex);
            return 3;
        }
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public Object[][] getData() {
        return data;
    }
    
    public void setData(Object[][] data) {
        this.data = data;
    }

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }
    
    
    
}
