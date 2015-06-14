package com.cs9033.songbird.models;

public class DrawerListOptions {
   public final static  String LISTEN="Listen";
   public final static  String CREATE="Create";
   public final static  String UPLOAD="Upload";
   public final static  String AUTHENTICATION="Signin/Signup";
   public final static  String LOGOUT="Logout";
   public final static  String PROFILE="Profile";
   public final static String EXIT="Exit Application";
   public static String[] getOptions(){
	   return new String[]{LISTEN,CREATE,UPLOAD,AUTHENTICATION,PROFILE,LOGOUT};
   }
   public static String[] getOptionsNoAuth(){
	   return new String[]{LISTEN,CREATE,UPLOAD,PROFILE,AUTHENTICATION};
   }
   public static String[] getOptionsAuth(){
	   return new String[]{LISTEN,CREATE,UPLOAD,PROFILE,LOGOUT};
   }
   
	
}
