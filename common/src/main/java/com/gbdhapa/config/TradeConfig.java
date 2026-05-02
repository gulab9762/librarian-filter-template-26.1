package com.gbdhapa.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
public class TradeConfig {
   public  boolean enableEachLevelReroll = true;   // toggle whole feature
   public  boolean enableReroll = true;   // toggle whole feature
   private static Path FILE;

   public static void setConfigFile(Path path) {
       FILE = path;
   }

   public static TradeConfig INSTANCE = new TradeConfig();

   public static void load() {
       try {
           if (!Files.exists(FILE)) {
               save();
               return;
           }
           String json = Files.readString(FILE);
           INSTANCE = new Gson().fromJson(json, TradeConfig.class);
       } catch (Exception e) {
           e.printStackTrace();
       }
   }

   public static void save() {
       try {
           String json = new GsonBuilder().setPrettyPrinting().create()
                   .toJson(INSTANCE);
           Files.writeString(FILE, json);
       } catch (Exception e) {
           e.printStackTrace();
       }
   }
}
