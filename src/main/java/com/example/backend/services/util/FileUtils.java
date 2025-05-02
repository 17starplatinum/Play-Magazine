package com.example.backend.services.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileUtils {
    /*private FileUtils(){
        throw new IllegalStateException("А нафиг ты здесь?");
    }*/

    public static String calculateFileHash(byte[] fileContent) throws NoSuchAlgorithmException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(fileContent);
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new NoSuchAlgorithmException("Something went wrong while calculating hash", e);
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
