package io.github.prayag.saucedemo.framework.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

/**
 * Collects a narrow, deterministic baseline of DOM accessibility findings without adding heavy
 * third-party scanners to the default framework runtime.
 */
@Slf4j
public final class AccessibilityProbe {

  private final JavascriptExecutor javascriptExecutor;

  public AccessibilityProbe(WebDriver driver) {
    if (!(driver instanceof JavascriptExecutor executor)) {
      throw new IllegalArgumentException(
          "Accessibility probing requires a JavaScript-capable driver");
    }
    this.javascriptExecutor = executor;
  }

  /**
   * Returns baseline accessibility findings for the current page.
   *
   * <p>This intentionally focuses on stable, high-signal checks that are unlikely to fluctuate
   * across browsers: visible images without alt text.
   */
  public List<String> findBaselineViolations() {
    List<String> violations = new ArrayList<>(findVisibleImagesWithoutAltText());
    log.debug("Accessibility probe found {} baseline violations", violations.size());
    return violations;
  }

  /** Returns non-blocking structural accessibility advisories for the current page. */
  public List<String> findStructuralAdvisories() {
    List<String> advisories = new ArrayList<>(findDuplicateIdViolations());
    log.debug("Accessibility probe found {} structural advisories", advisories.size());
    return advisories;
  }

  private List<String> findDuplicateIdViolations() {
    Object rawResult =
        javascriptExecutor.executeScript(
            "const counts = Array.from(document.querySelectorAll('[id]'))"
                + ".filter(element => {"
                + "  const style = window.getComputedStyle(element);"
                + "  return style.display !== 'none' && style.visibility !== 'hidden'"
                + "    && element.getClientRects().length > 0;"
                + "})"
                + ".map(element => element.id && element.id.trim())"
                + ".filter(Boolean)"
                + ".reduce((accumulator, id) => {"
                + "  accumulator[id] = (accumulator[id] || 0) + 1;"
                + "  return accumulator;"
                + "}, {});"
                + "return counts;");

    if (!(rawResult instanceof Map<?, ?> rawMap)) {
      return List.of("Accessibility probe could not evaluate duplicate DOM ids");
    }

    List<String> violations = new ArrayList<>();
    Map<String, Long> counts = new LinkedHashMap<>();
    rawMap.forEach(
        (key, value) -> counts.put(String.valueOf(key), Long.parseLong(String.valueOf(value))));
    counts.forEach(
        (id, count) -> {
          if (count > 1) {
            violations.add(String.format("Duplicate DOM id '%s' appears %d times", id, count));
          }
        });
    return violations;
  }

  private List<String> findVisibleImagesWithoutAltText() {
    Object rawResult =
        javascriptExecutor.executeScript(
            "return Array.from(document.images)"
                + ".filter(image => {"
                + "  const style = window.getComputedStyle(image);"
                + "  const visible = style.display !== 'none' && style.visibility !== 'hidden'"
                + "    && image.getClientRects().length > 0;"
                + "  const alt = image.getAttribute('alt');"
                + "  return visible && (!alt || !alt.trim());"
                + "})"
                + ".map(image => image.getAttribute('src') || image.outerHTML);");

    if (!(rawResult instanceof List<?> rawList)) {
      return List.of("Accessibility probe could not evaluate image alt text");
    }

    return rawList.stream()
        .map(String::valueOf)
        .map(source -> String.format("Visible image is missing alt text: %s", source))
        .toList();
  }
}
