package tech.snaco.SplitWorld;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.slf4j.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import tech.snaco.SplitWorld.types.Config;

public class ConfigLoader {
  private Logger LOGGER = SplitWorld.LOGGER;
  private Path file = Path.of("config/splitworld.yaml");
  private File yaml = new File("config/splitworld.yaml");
  private Gson gson = new Gson();
  private YAMLMapper mapper;


  public ConfigLoader() {
    this.mapper = YAMLMapper.builder()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
            .build();
  }

  public Config load() {
    Config config;
    try {
      config = mapper.readValue(Files.readString(file), Config.class);
//      config = gson.fromJson(Files.readString(file), Config.class);
      LOGGER.info("Loaded config.");
    } catch (JsonSyntaxException | IOException e) {
      config = this.createConfig();
    }
    return config;
  }
  
  private Config createConfig() {
    try {
      try {
        Files.createDirectory(Path.of("config"));
      } catch (Exception ex1) {}
      mapper.writeValue(yaml, new Config());
//      Files.writeString(file, gson.toJson(new Config()), StandardOpenOption.CREATE);
      LOGGER.info("Created new config.");
    } catch (Exception ex) {
      LOGGER.error("Error writing config file!", ex);
    }
    return new Config();
  }
}
