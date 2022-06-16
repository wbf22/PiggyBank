/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.fernwebsites.piggybank.Utility;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.SecretKeyEntry;
import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author brand
 */
public class KeyStoreManager {
    
    public static final int GCM_IV_LENGTH = 12;
   
    public static final int GCM_TAG_LENGTH = 16;
    
    private static String PATH = System.getenv("APPDATA") + "\\PiggyBank\\";
    
    
    private static Key getKey(UserSession userSession) {
        try{
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(new FileInputStream(PATH + userSession.getUserName() + ".p12"), userSession.getPassword().toCharArray()); 
            return keyStore.getKey("master", userSession.getPassword().toCharArray());
        } catch(Exception ex) {
            return null;
        } 
    }
    
    private static Key makeKey(UserSession userSession) {
        try{
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(null, null); // Initialize a blank keystore
            
            SecureRandom securerandom= new SecureRandom();
            KeyGenerator keygenerator= KeyGenerator.getInstance("AES");
            keygenerator.init(256, securerandom);
            SecretKey key = keygenerator.generateKey();
            
            byte[] salt = new byte[20];
            new SecureRandom().nextBytes(salt);
            keyStore.setEntry("master", new SecretKeyEntry(key),
                new PasswordProtection(userSession.getPassword().toCharArray(),
                    "PBEWithHmacSHA512AndAES_256",
                    new PBEParameterSpec(salt, 100_000)));
            keyStore.store(new FileOutputStream(PATH + userSession.getUserName() + ".p12"), userSession.getPassword().toCharArray());

            return key;
        } catch(Exception ex) {
            return null;
        } 
    }
    
    private static byte[] genIv(int size) {
        byte[] initializationVector = new byte[size];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(initializationVector);
        return initializationVector;
    }
    
    private static boolean encryptAndStore(Key key, UserSession userSession) {
        
        try {
            Cipher cipher= Cipher.getInstance("AES/GCM/PKCS5PADDING");
            byte[] iv = genIv(GCM_IV_LENGTH);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmParameterSpec);
            byte[] encryptedBytes = cipher.doFinal(getAsBytes(userSession.getData()));
            
            //write the iv
            File data = new File(PATH + userSession.getUserName() + "Data");
            FileOutputStream fileOutputStream;
            fileOutputStream = new FileOutputStream(data);
            fileOutputStream.write(iv);
            
            //write data length
            ByteBuffer byteBuffer = ByteBuffer.allocate(4);
            byteBuffer.putInt(encryptedBytes.length);
            
            //write data
            fileOutputStream.write(byteBuffer.array());
            fileOutputStream.write(encryptedBytes);
            fileOutputStream.close();
//            
//            System.out.println(DatatypeConverter.printHexBinary(iv));
//            System.out.println(DatatypeConverter.printHexBinary(byteBuffer.array()));
//            System.out.println(DatatypeConverter.printHexBinary(encryptedBytes));
            
            return true;
        } catch (Exception ex) {
            Logger.getLogger(KeyStoreManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        
        
        return false;
    }
    
    private static Object[][] decryptAndReturn(Key key, UserSession userSession) {
        
        
        try {
            File data = new File(PATH + userSession.getUserName() + "Data");
            FileInputStream fileInputStream = new FileInputStream(data);
            byte[] iv = new byte[GCM_IV_LENGTH];
            fileInputStream.read(iv);
            
            byte[] lengthAsBytes = new byte[4];
            fileInputStream.read(lengthAsBytes);
            int length = ByteBuffer.wrap(lengthAsBytes).getInt();
            
            
            Cipher cipher = Cipher.getInstance("AES/GCM/PKCS5PADDING");
            SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), "AES");
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);
            
            byte[] cipherData = new byte[length];
            fileInputStream.read(cipherData);
            fileInputStream.close();
            
            byte[] decrypted = cipher.doFinal(cipherData);
            return getObjectArray(decrypted);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        } 
        
        
        return null;
    }
    
    public static byte[] getAsBytes(Object[][] data) throws IOException {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(bos);
      oos.writeObject(data);
      oos.flush();
      return bos.toByteArray();
    }
    
    public static Object[][] getObjectArray(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bis);
        Object[][] deserialized = (Object[][]) ois.readObject();
        return deserialized;
    }
    
    
    public static Object[][] getUserData(UserSession userSession){
        
        Key symKey = getKey(userSession);
        if (symKey == null) {
            symKey = makeKey(userSession);
        }
        
        if (symKey != null) {
            return decryptAndReturn(symKey, userSession);
        }
        
        
        return null;
    }
    
    public static boolean storeUserData(UserSession userSession){
        
        Key symKey = getKey(userSession);
        if (symKey == null) {
            symKey = makeKey(userSession);
        }
        
        if (symKey != null) {
            return encryptAndStore(symKey, userSession);
        }
        
        
        return false;
    }
    
    
    
    
}
