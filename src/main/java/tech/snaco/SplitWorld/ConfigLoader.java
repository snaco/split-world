package tech.snaco.SplitWorld;

import java.io.File;

import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConfigLoader {
  private static Logger LOGGER = SplitWorld.LOGGER;
  private static File file = new File("config/splitworld.json");
  private static ObjectMapper objectMapper = new ObjectMapper(new JsonFactory());

  public static Config load() {
    try {
      var config = ConfigLoader.objectMapper.readValue(ConfigLoader.file, Config.class);
      LOGGER.info("Loaded config.");
      return config;
    }
    catch (Exception ex) {
      var config = ConfigLoader.createConfig();
      LOGGER.info("Created new config.");
      return config;
    }
  }

  private static Config createConfig() {
    var config = new Config();
    try {
      ConfigLoader.objectMapper.writeValue(ConfigLoader.file, config);
    } catch (Exception ex) {
      LOGGER.error("Error writing config file!", ex);
    }
    return config;
  }
}
