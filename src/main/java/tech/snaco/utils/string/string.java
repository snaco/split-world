package tech.snaco.utils.string;

public class string {
  public static String f(String string, Object... args) {
    return String.format(string, args);
  }
  public static String info(String string, Object... args) {
    return "Splitworld: " + tech.snaco.utils.string.string.f(string, args);
  }
  public static String error(String string, Object... args) {
    return "Splitworld Error: " + tech.snaco.utils.string.string.f(string, args);
  }
  public static String debug(String string, Object... args) {
    return "Splitworld Debug Message: " + tech.snaco.utils.string.string.f(string, args);
  }
}
