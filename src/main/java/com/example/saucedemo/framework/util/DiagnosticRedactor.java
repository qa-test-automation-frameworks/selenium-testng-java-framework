package com.example.saucedemo.framework.util;

import com.example.saucedemo.framework.config.ConfigFactory;
import com.example.saucedemo.framework.config.FrameworkConfig;
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
  private static volatile List<Pattern> cachedPatterns;

  private DiagnosticRedactor() {}

  public static String redact(String rawValue) {
    if (rawValue == null || rawValue.isBlank()) {
      return rawValue;
    }

    String redacted = rawValue;
    for (Pattern pattern : redactionPatterns()) {
      redacted = replaceMatches(redacted, pattern);
    }
    return redacted;
  }

  private static List<Pattern> redactionPatterns() {
    List<Pattern> localPatterns = cachedPatterns;
    if (localPatterns != null) {
      return localPatterns;
    }

    synchronized (DiagnosticRedactor.class) {
      if (cachedPatterns == null) {
        List<Pattern> patterns = new ArrayList<>(DEFAULT_PATTERNS);
        FrameworkConfig config = ConfigFactory.getConfig();
        if (config.appPassword() != null && !config.appPassword().isBlank()) {
          patterns.add(Pattern.compile(Pattern.quote(config.appPassword())));
        }
        if (config.appUsername() != null && !config.appUsername().isBlank()) {
          patterns.add(
              Pattern.compile(Pattern.quote(config.appUsername()), Pattern.CASE_INSENSITIVE));
        }
        cachedPatterns = List.copyOf(patterns);
      }
      return cachedPatterns;
    }
  }

  private static String replaceMatches(String input, Pattern pattern) {
    Matcher matcher = pattern.matcher(input);
    StringBuffer buffer = new StringBuffer();
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
