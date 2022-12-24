package tech.snaco.SplitWorld.types;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DimensionConfig {
  @JsonProperty("dimension_name")
  public String dimensionName;
  @JsonProperty("enabled")
  public boolean enabled;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonProperty("game_mode_override")
  public String gameModeOverride;
  @JsonProperty("border_axis")
  public String borderAxis;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonProperty("border_location")
  public Integer borderLocation;
  @JsonProperty("creative_side")
  public String creativeSide;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonProperty("border_width")
  public Integer borderWidth;
  @JsonProperty("replace_border_blocks")
  public boolean replaceBorderBlocks;

  public DimensionConfig() {}

  public DimensionConfig(String dimensionName, boolean enabled, String gameModeOverride, String borderAxis, Integer borderLocation, String creativeSide, Integer borderWidth, boolean replaceBorderBlocks) {
    this.dimensionName = dimensionName;
    this.enabled = enabled;
    this.gameModeOverride = gameModeOverride;
    this.borderAxis = borderAxis;
    this.borderLocation = borderLocation;
    this.creativeSide = creativeSide;
    this.borderWidth = borderWidth;
    this.replaceBorderBlocks = replaceBorderBlocks;
  }
}
