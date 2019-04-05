package com.company;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;

public class Main {

    public static void main(String[] args) throws IOException {
/*
        Path keyPath = Paths.get(args[4]);
        byte[] cipher = Files.readAllBytes(keyPath);

        Path cipherPath = Paths.get(args[6]);
        byte[] cipher2 = Files.readAllBytes(cipherPath);

        System.out.println(test(cipher,cipher2));
*/


        if (args[0].equals( "-e")){
            //read key
            Path keyPath1 = Paths.get(args[2]);
            byte[] key = Files.readAllBytes(keyPath1);
            //read message
            Path messagePath = Paths.get(args[4]);
            byte[] message = Files.readAllBytes(messagePath);

            //make cipher text & write in to output file
            byte[]cipherText;
            cipherText=encrypt(message,key);
            FileOutputStream fos = new FileOutputStream(args[6]);
            fos.write(cipherText);
            fos.close();

        }


       else if (args[0].equals("-b")){
            //read message
            Path messagePath = Paths.get(args[2]);
            byte[] message = Files.readAllBytes(messagePath);

            //read cipher
            Path cipherPath = Paths.get(args[4]);
            byte[] cipher = Files.readAllBytes(cipherPath);

            //find 3 keys & write it to output file
            byte[]keys=findThreeKeys(message,cipher);
            FileOutputStream fos = new FileOutputStream(args[6]);
            fos.write(keys);
            fos.close();
        }

        else if(args[0].equals("-d")){
           //read key
            Path keyPath = Paths.get(args[2]);
            byte[] key = Files.readAllBytes(keyPath);
        //read cipher text
           Path cipherPath = Paths.get(args[4]);
           byte[] cipher = Files.readAllBytes(cipherPath);

           //do reverse AES
            ArrayList keysInBlocks=separateToBlocks(key);
            ArrayList cipherInBlocks=separateToBlocks(cipher);
            ArrayList c1=invertAES(cipherInBlocks,(Byte[][]) keysInBlocks.get(2));
            ArrayList c2=invertAES(c1,(Byte[][]) keysInBlocks.get(1));
            ArrayList c3=invertAES(c2,(Byte[][]) keysInBlocks.get(0));

            byte[] plainText=reverseFromBlocksToArray(c3);

            //write plainText to output file
            FileOutputStream fos = new FileOutputStream(args[6]);
            fos.write(plainText);
            fos.close();

        }


    }

    private static ArrayList invertAES(ArrayList cipherInBlocks, Byte[][] key) {
        ArrayList afterXor=Xor(cipherInBlocks,key);
        ArrayList afterInvertShiftRows=inverShiftRows(afterXor);
        return afterInvertShiftRows;
    }

    private static ArrayList inverShiftRows(ArrayList afterXor) {
        ArrayList <Byte[][]>ans= new ArrayList<>();

        for (int i=0;i<afterXor.size();i++){
            ans.add(invertShiftRowForOneBlock((Byte[][])afterXor.get(i)));
        }
        return ans;
    }

    private static Byte[][] invertShiftRowForOneBlock(Byte[][] message) {
        for (int i=0;i<4;i++){
            message[i]=invertShiftOneRow(message[i],i);
        }
        return message;
    }

    private static Byte[] invertShiftOneRow(Byte[] block, int rowNum) {
        //row num 0 have no change
        if (rowNum == 0)
            return block;

        else {
            Byte[] newRow = new Byte[4];
            for (int i = 0; i < 4; i++) {
                if (i+rowNum<4)
                    newRow[i+rowNum]=block[i];

                else
                    newRow[(i+rowNum)%4]=block[i];

            }
            return newRow;
        }
    }

//check if two strings are equals.
    private static boolean test(byte[]message,byte[]cipher) {
           boolean equal=true;
        for (int i=0;i<cipher.length;i++){
            if (cipher[i]!=message[i]) {
                equal = false;
                break;
            }
        }
        return equal;

    }

    private static byte[] encrypt(byte[] message, byte[] key) {
        //splite message to blocks
        ArrayList messageToBlocks;
       messageToBlocks=separateToBlocks(message);
        //splite key to blocks
       ArrayList keyToBlocks;
       keyToBlocks=separateToBlocks(key);

       ArrayList c1=AES(messageToBlocks,(Byte[][]) keyToBlocks.get(0));
       ArrayList c2=AES(c1,(Byte[][]) keyToBlocks.get(1));
       ArrayList c3=AES(c2,(Byte[][]) keyToBlocks.get(2));

       byte[] cipherText= reverseFromBlocksToArray(c3);
       return cipherText;
    }

    private static byte[] reverseFromBlocksToArray(ArrayList c3) {
        int a=0;
        Byte [] temp=new Byte[16*c3.size()];
        byte[] ans=new byte[16*c3.size()];
        for(int i=0;i<c3.size();i++){
            for (int j=0;j<4;j++){
                for (int x=0;x<4;x++){
                    temp[a]=((Byte[][])c3.get(i))[x][j];
                    a++;
                }

            }
        }
        for(int i=0;i<temp.length;i++)
            ans[i] = temp[i].byteValue();

        return ans;
    }

    private static ArrayList AES(ArrayList messageToBlocks, Byte[][]key) {

        //do shiftRow
        ArrayList<Byte[][]> afterShiftRows= shiftRows(messageToBlocks);

        //do addRoundkey
        ArrayList<Byte[][]> afterXor= Xor(afterShiftRows,key);
        return afterXor;

    }

    private static ArrayList<Byte[][]> Xor(ArrayList<Byte[][]> afterShiftRows, Byte[][] key) {
       for (int x=0;x<afterShiftRows.size();x++) {
           for (int i = 0; i < 4; i++) {
               for (int j = 0; j < 4; j++) {
                   afterShiftRows.get(x)[i][j]=(byte)((afterShiftRows.get(x)[i][j])^key[i][j]);
               }
           }
       }
       return afterShiftRows;
    }

    private static ArrayList<Byte[][]> Xor(ArrayList<Byte[][]> arr1, ArrayList<Byte[][]> arr2){
        for (int x=0;x<arr1.size();x++) {
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    arr1.get(x)[i][j]=(byte)((arr1.get(x)[i][j])^arr2.get(x)[i][j]);
                }
            }
        }
        return arr1;
    }

    //do shiftRows for all blocks
    private static ArrayList shiftRows(ArrayList blocks) {
       ArrayList <Byte[][]>ans= new ArrayList<>();

        for (int i=0;i<blocks.size();i++){
            ans.add(shiftRowForOneBlock((Byte[][])blocks.get(i)));
        }
        return ans;
    }

    //do shiftRow for one block
    public static Byte[][] shiftRowForOneBlock(Byte[][] block) {

        for (int i=0;i<4;i++){
            block[i]=shiftOneRow(block[i],i);
        }
        return block;
    }


    //do shiftRow for one block in index rowNum
    private static Byte[] shiftOneRow(Byte[] block, int rowNum) {
       //row num 0 have no change
        if (rowNum == 0)
            return block;

        else {
            Byte[] newRow = new Byte[4];
            for (int i = 0; i < 4; i++) {
                if (i - rowNum >= 0)
                    newRow[i - rowNum] = block[i];

                else if (i - rowNum < 0)
                    newRow[4 - Math.abs(i - rowNum)] = block[i];
            }
            return newRow;
        }
    }

    //seperate message to blocks in size of 128 byte.
    private static ArrayList separateToBlocks(byte[] message) {
        //check how many blocks
        int numOfBlocks = message.length / 16;
        ArrayList<Byte[][]> messageToBlocks = new ArrayList();
        //add block (4*4 metrix)
        for (int j = 0; j < numOfBlocks; j++)
            messageToBlocks.add(new Byte[4][4]);

        int counter = 0;
        //loop on each byte and insert to block
        int i;
        for (i=0;i < message.length; i++) {
            if (i % 16 == 0)
                counter++;

            messageToBlocks.get(counter - 1)[i % 4][(i%16) / 4] = message[i];
        }
        return messageToBlocks;
    }

    public static byte[] findThreeKeys(byte[]message,byte[]cipher){
       //make 3 keys
        byte[] k1=getRandKey(16);
        byte[] k2=getRandKey(16);
        ArrayList mes1;
        Byte[] cipherBytes=new Byte[cipher.length];
        for (int i=0;i<cipher.length;i++)
            cipherBytes[i]=cipher[i];

        mes1=AES(separateToBlocks(message),(Byte[][]) separateToBlocks(k1).get(0));
        ArrayList mes2;
        mes2=AES(mes1,(Byte[][]) separateToBlocks(k2).get(0));

        ArrayList cipherArr=separateToBlocks(cipher);
        ArrayList k3Arr=(Xor(shiftRows(mes2),cipherArr));
        byte[] k3=reverseFromBlocksToArray(k3Arr);

        byte[] allKeys=new byte[48];
        for (int i=0;i<16;i++) {
            allKeys[i] = k1[i];

        }

        for (int i=0;i<16;i++) {
            allKeys[16+i] = k2[i];

        }

        for (int i=0;i<16;i++) {
            allKeys[32+i] = k3[i];

        }


        return allKeys;
    }


    //return random key
    private static byte[] getRandKey(int size) {
        byte[] key=new byte [size];
        Random rand= new Random();
        rand.nextBytes(key);
        return key;
    }


    public void print(byte[][] arr){
        for (int i=0; i<arr.length;i++){
            for (int j=0;j<arr[0].length;j++)
                System.out.println(arr[i][j]);
        }
    }


}
