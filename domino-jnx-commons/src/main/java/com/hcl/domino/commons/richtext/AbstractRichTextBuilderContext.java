package com.hcl.domino.commons.richtext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.design.RichTextBuilder;
import com.hcl.domino.design.RichTextBuilder.RichTextBuilderContext;
import com.hcl.domino.design.RichTextBuilder.RichTextBuilderOperation;

public abstract class AbstractRichTextBuilderContext<O> implements RichTextBuilderContext<O> {
  private static final Map<String, Pattern> PATTERN_CACHE = new ConcurrentHashMap<>();

  private final RichTextBuilder rtBuilder;
  private final Database targetDb;
  private final Document templateDoc;
  private final String itemName;
  protected final List<RichTextBuilderOperation> operations;
  private final Class<O> resultType;

  public AbstractRichTextBuilderContext(RichTextBuilder rtBuilder, Database targetDb,
      Document template, String itemName, Class<O> resultType) {
    this.rtBuilder = rtBuilder;
    this.targetDb = targetDb;
    this.templateDoc = template;
    this.itemName = itemName;
    this.resultType = resultType;
    this.operations = new ArrayList<>();
  }

  @Override
  public RichTextBuilder getRichTextBuilder() {
    return rtBuilder;
  }

  @Override
  public Class<O> getResultType() {
    return resultType;
  }

  @Override
  public String getItemName() {
    return itemName;
  }

  @Override
  public RichTextBuilderContext<O> replace(String placeholder, String label) {
    Map<Pattern, Function<Matcher, Object>> replacementsWithPattern = new HashMap<>();
    Pattern pattern = PATTERN_CACHE.get(placeholder);
    if (pattern == null) {
      pattern = Pattern.compile(Pattern.quote(placeholder), Pattern.CASE_INSENSITIVE);
      PATTERN_CACHE.put(placeholder, pattern);
    }
    replacementsWithPattern.put(pattern, (matcher) -> {
      return label;
    });
    return replaceExt(replacementsWithPattern);
  }

  @Override
  public RichTextBuilderContext<O> replace(Map<String, Object> replacements) {
    Map<Pattern, Function<Matcher, Object>> replacementsWithPattern = new HashMap<>();
    for (Entry<String, Object> currEntry : replacements.entrySet()) {
      Pattern currPattern = PATTERN_CACHE.get(currEntry.getKey());
      if (currPattern == null) {
        currPattern = Pattern.compile(Pattern.quote(currEntry.getKey()), Pattern.CASE_INSENSITIVE);
        PATTERN_CACHE.put(currEntry.getKey(), currPattern);
      }
      replacementsWithPattern.put(currPattern, (matcher) -> {
        return currEntry.getValue();
      });
    }

    return replaceExt(replacementsWithPattern);
  }

  protected Document getTemplate() {
    return templateDoc;
  }

  protected Database getTargetDb() {
    return targetDb;
  }
}
