package se.wegelius.routedisplayer;

import java.util.Scanner;

/**
 * Created by asawe_000 on 26-02-2015.
 */
public class Validation {
    public static boolean validPassword(String password){
        return password.length() > 0;
    }

    public static boolean validName(String name){
        return name.length() > 0;
    }

    public static boolean isInteger(String number){
        Scanner sc = new Scanner(number);
        return sc.hasNextInt();
    }

}
