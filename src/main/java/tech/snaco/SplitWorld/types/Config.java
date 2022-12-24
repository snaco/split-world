package tech.snaco.SplitWorld.types;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class Config {
  @JsonProperty("default_game_mode")
  public String defaultGameMode = "survival";
  @JsonProperty("dimension_configs")
  public List<DimensionConfig> dimensionConfigs;

  public Config() {
    this.dimensionConfigs = new ArrayList<DimensionConfig>();
  }
}
