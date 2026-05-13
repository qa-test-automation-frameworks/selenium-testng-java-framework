package io.github.prayag.saucedemo.framework.util;

import io.github.prayag.saucedemo.framework.config.ConfigFactory;
import io.github.prayag.saucedemo.framework.config.FrameworkConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Redacts common sensitive values from diagnostic payloads before they are attached to reports. */
public final class DiagnosticRedactor {

  private static final List<Pattern> DEFAULT_PATTERNS =
      List.of(
          Pattern.compile("(?i)(password[\"'=:\\s>]+)([^\"'\\s<&,;]+)"),
          Pattern.compile("(?i)(bearer\\s+)([a-z0-9._\\-]+)"),
          Pattern.compile("(?i)(authorization[\"'=:\\s>]+)([^\"'\\s<&,;]+)"),
          Pattern.compile("(?i)(cookie[\"'=:\\s>]+)([^\"'\\s<&,;]+)"),
          Pattern.compile("(?i)(session(?:-|_)?id[\"'=:\\s>]+)([^\"'\\s<&,;]+)"),
          Pattern.compile("(?i)(set-cookie:\\s*)(.+)"),
          Pattern.compile("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}", Pattern.CASE_INSENSITIVE),
          Pattern.compile("(?<!\\d)(?:\\+?\\d[\\d\\-() ]{7,}\\d)(?!\\d)"));

  private DiagnosticRedactor() {}

  public static String redact(String rawValue) {
    return redact(rawValue, buildPatterns(ConfigFactory.getConfig()));
  }

  static String redact(String rawValue, FrameworkConfig config) {
    return redact(rawValue, buildPatterns(config));
  }

  private static String redact(String rawValue, List<Pattern> patterns) {
    if (rawValue == null || rawValue.isBlank()) {
      return rawValue;
    }

    String redacted = rawValue;
    for (Pattern pattern : patterns) {
      redacted = replaceMatches(redacted, pattern);
    }
    return redacted;
  }

  static List<Pattern> buildPatterns(FrameworkConfig config) {
    List<Pattern> patterns = new ArrayList<>(DEFAULT_PATTERNS);
    if (config.appPassword() != null && !config.appPassword().isBlank()) {
      patterns.add(Pattern.compile(Pattern.quote(config.appPassword())));
    }
    if (config.appUsername() != null && !config.appUsername().isBlank()) {
      patterns.add(Pattern.compile(Pattern.quote(config.appUsername()), Pattern.CASE_INSENSITIVE));
    }
    return List.copyOf(patterns);
  }

  private static String replaceMatches(String input, Pattern pattern) {
    Matcher matcher = pattern.matcher(input);
    StringBuilder buffer = new StringBuilder();
    while (matcher.find()) {
      String replacement = buildReplacement(matcher);
      matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
    }
    matcher.appendTail(buffer);
    return buffer.toString();
  }

  private static String buildReplacement(Matcher matcher) {
    if (matcher.groupCount() >= 2) {
      return matcher.group(1) + "<redacted>";
    }
    return "<redacted>";
  }
}
