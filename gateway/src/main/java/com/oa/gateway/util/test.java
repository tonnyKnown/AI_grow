package com.oa.gateway.util;

public class test {

    public static void main(String[] args) {
        canReach(new int[]{4,2,3,0,2,1,2},4);
    }
    private static boolean[] dp;
    public static boolean canReach(int[] arr, int start) {
        if(start <0 || start >= arr.length) return false;
        int length = arr.length;
        dp = new boolean[length];
        boolean b = check(arr, start - arr[start], start) || check(arr, start + arr[start], start);
        return b;

    }

    private static boolean  check(int[] arr, int index,int pre) {
        if(index < 0 || index >=arr.length || dp[index]){
            dp[pre] = true;
            return false;
        }
        dp[pre] = true;
        if(arr[index] == 0) return true;
        return check(arr,index -arr[index],index) || check(arr,index+arr[index],index);
    }
}
