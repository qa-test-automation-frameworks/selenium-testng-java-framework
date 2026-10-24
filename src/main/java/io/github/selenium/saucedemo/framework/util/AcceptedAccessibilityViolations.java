package io.github.selenium.saucedemo.framework.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Register of axe violations accepted for a limited time. Every entry needs a reason, an issue and
 * an expiry at most {@value #MAX_ACCEPTANCE_DAYS} days after it was accepted.
 */
public final class AcceptedAccessibilityViolations {

  public static final long MAX_ACCEPTANCE_DAYS = 90;
  private static final ObjectMapper MAPPER = new ObjectMapper();

  public record Entry(
      String ruleId,
      String page,
      String reason,
      LocalDate acceptedOn,
      LocalDate expiresOn,
      String issueUrl) {}

  /** Outcome of applying the register to the blocking rules found on one page. */
  public record Evaluation(Set<String> unaccepted, Set<String> accepted, List<String> problems) {}

  private AcceptedAccessibilityViolations() {}

  public static List<Entry> loadFromClasspath(String resource) {
    try (InputStream stream = AcceptedAccessibilityViolations.class.getResourceAsStream(resource)) {
      if (stream == null) {
        throw new IllegalStateException("Accepted accessibility violations not found: " + resource);
      }
      return parse(MAPPER.readTree(stream));
    } catch (IOException e) {
      throw new IllegalStateException("Cannot read accepted accessibility violations", e);
    }
  }

  public static List<Entry> parse(String json) {
    try {
      return parse(MAPPER.readTree(json));
    } catch (IOException e) {
      throw new IllegalArgumentException("Accepted accessibility violations are not valid JSON", e);
    }
  }

  private static List<Entry> parse(JsonNode root) {
    if (root == null || !root.isArray()) {
      throw new IllegalArgumentException("Accepted accessibility violations must be a JSON array");
    }
    List<Entry> entries = new ArrayList<>();
    for (JsonNode node : root) {
      LocalDate acceptedOn = date(node, "acceptedOn");
      LocalDate expiresOn = date(node, "expiresOn");
      String ruleId = text(node, "ruleId");
      if (expiresOn.isBefore(acceptedOn)
          || ChronoUnit.DAYS.between(acceptedOn, expiresOn) > MAX_ACCEPTANCE_DAYS) {
        throw new IllegalArgumentException(
            "Acceptance for " + ruleId + " must expire within " + MAX_ACCEPTANCE_DAYS + " days");
      }
      String issueUrl = text(node, "issueUrl");
      if (!URI.create(issueUrl).getScheme().equals("https")) {
        throw new IllegalArgumentException("Acceptance for " + ruleId + " needs an https issueUrl");
      }
      entries.add(
          new Entry(
              ruleId, text(node, "page"), text(node, "reason"), acceptedOn, expiresOn, issueUrl));
    }
    return List.copyOf(entries);
  }

  private static String text(JsonNode node, String field) {
    JsonNode value = node.get(field);
    if (value == null || !value.isTextual() || value.asText().isBlank()) {
      throw new IllegalArgumentException("Accepted violation field '" + field + "' is required");
    }
    return value.asText();
  }

  private static LocalDate date(JsonNode node, String field) {
    try {
      return LocalDate.parse(text(node, field));
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException(
          "Accepted violation field '" + field + "' is not a date", e);
    }
  }

  /** Splits the blocking rule ids found on {@code page} into accepted and unaccepted. */
  public static Evaluation evaluate(
      List<Entry> entries, Collection<String> blockingRuleIds, String page, LocalDate today) {
    Set<String> unaccepted = new LinkedHashSet<>(blockingRuleIds);
    Set<String> accepted = new LinkedHashSet<>();
    List<String> problems = new ArrayList<>();
    for (Entry entry : entries) {
      if (!entry.page().equals(page)) {
        continue;
      }
      if (entry.expiresOn().isBefore(today)) {
        problems.add(
            "Accepted accessibility violation "
                + entry.ruleId()
                + " expired on "
                + entry.expiresOn());
      } else if (!blockingRuleIds.contains(entry.ruleId())) {
        problems.add("Stale acceptance for " + entry.ruleId() + "; remove it");
      } else {
        unaccepted.remove(entry.ruleId());
        accepted.add(entry.ruleId());
      }
    }
    return new Evaluation(unaccepted, accepted, List.copyOf(problems));
  }
}
