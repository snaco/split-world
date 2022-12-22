package tech.snaco.SplitWorld.types;

public class DimensionConfig extends BaseConfig {
  public String dimensionName;
  public boolean enabled;
  public String gameModeOverride;

  public DimensionConfig(String dimensionName, boolean enabled) {
    this.dimensionName = dimensionName;
    this.enabled = enabled;
  }
}
