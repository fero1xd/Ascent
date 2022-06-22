package me.fero.ascent;

import io.github.cdimascio.dotenv.Dotenv;
import org.json.simple.parser.JSONParser;

public class Config {
    private static final Dotenv dotenv = Dotenv.configure().directory("./").filename(".env").load();
    public static JSONParser parser = new JSONParser();

    public static String get(String key) {
        return dotenv.get(key.toUpperCase());
    }
}
