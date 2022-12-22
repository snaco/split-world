package tech.snaco.SplitWorld.types;

import java.util.ArrayList;
import java.util.List;

public class Config {
  public List<DimensionConfig> dimensionConfigs = new ArrayList<>();
  public String defaultGameMode = "survival";
  public Config() {
    this.dimensionConfigs.add(new DimensionConfig("minecraft:overworld", true));
  }
}
