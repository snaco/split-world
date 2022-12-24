package tech.snaco.SplitWorld;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.slf4j.Logger;
import com.google.gson.JsonSyntaxException;
import tech.snaco.SplitWorld.types.Config;
import tech.snaco.SplitWorld.types.DimensionConfig;
import tech.snaco.utils.string.string;

public class ConfigLoader {
  private final Logger LOGGER = SplitWorld.LOGGER;
  private final Path file = Path.of("config/splitworld.yaml");
  private final File yaml = new File("config/splitworld.yaml");
  private final YAMLMapper mapper;


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
      LOGGER.info(string.info("Loaded config."));
    } catch (JsonSyntaxException | IOException e) {
      config = this.createConfig();
    }
    return config;
  }
  
  private Config createConfig() {
    try {
      try {
        Files.createDirectory(Path.of("config"));
      } catch (Exception ex1) {
        LOGGER.debug(string.debug("config folder exists, skipping creation."));
      }
      var config = new Config();
      config.dimensionConfigs.add(new DimensionConfig("minecraft:overworld", true, null, "X", 0, "negative", 10, true));
      mapper.writeValue(yaml, config);
      LOGGER.info(string.info("Created new config."));
    } catch (Exception ex) {
      LOGGER.error(string.error("Error writing config file!"), ex);
    }
    return new Config();
  }
}
