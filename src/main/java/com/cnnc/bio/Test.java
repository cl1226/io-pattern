package com.cnnc.bio;

public class Test implements Cloneable {

    public static void main(String[] args) {
        Test test = new Test();
        try {
            Object newTest = test.clone();
            System.out.println(newTest);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

    }

}
