package net.erel.maven.plugins.utils;

public enum ANSI_COLOR {

  // code for color console
  RESET("\u001B[0m"),
  BLACK("\u001B[30m"),
  RED("\u001B[31m"),
  GREEN("\u001B[32m"),
  YELLOW("\u001B[33m"),
  BLUE("\u001B[34m"),
  PURPLE("\u001B[35m"),
  CYAN("\u001B[36m"),
  WHITE("\u001B[37m"), ;

  String colorCode;

  private ANSI_COLOR(String colorCode) {
    this.colorCode = colorCode;
  }

  public String getColorCode() {
    return this.colorCode;
  }

  public String toString() {
    return this.getColorCode();
  }

}