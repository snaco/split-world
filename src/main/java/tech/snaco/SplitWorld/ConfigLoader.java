package tech.snaco.SplitWorld;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class ConfigLoader {
  private static Logger LOGGER = SplitWorld.LOGGER;
  private static Path file = Path.of("config/splitworld.json");
  private static Gson gson = new Gson();

  public static Config load() {
    Config config;
    try {
      config = gson.fromJson(Files.readString(file), Config.class);
      LOGGER.info("Loaded config.");
    } catch (JsonSyntaxException | IOException e) {
      config = ConfigLoader.createConfig();
    }
    return config;
  }
  
  private static Config createConfig() {
    try {
      Files.writeString(file, gson.toJson(new Config()));
      LOGGER.info("Created new config.");
    } catch (Exception ex) {
      LOGGER.error("Error writing config file!", ex);
    }
    return new Config();
  }
}
