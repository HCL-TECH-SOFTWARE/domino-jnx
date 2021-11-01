/*
 * ==========================================================================
 * Copyright (C) 2019-2021 HCL America, Inc. ( http://www.hcl.com/ )
 *                            All rights reserved.
 * ==========================================================================
 * Licensed under the  Apache License, Version 2.0  (the "License").  You may
 * not use this file except in compliance with the License.  You may obtain a
 * copy of the License at <http://www.apache.org/licenses/LICENSE-2.0>.
 *
 * Unless  required  by applicable  law or  agreed  to  in writing,  software
 * distributed under the License is distributed on an  "AS IS" BASIS, WITHOUT
 * WARRANTIES OR  CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the  specific language  governing permissions  and limitations
 * under the License.
 * ==========================================================================
 */
package it.com.hcl.domino.test.calendaring;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import org.apache.commons.text.StringEscapeUtils;
import org.junit.jupiter.api.Assertions;
import org.opentest4j.AssertionFailedError;

import com.hcl.domino.DominoClient;
import com.hcl.domino.calendar.CalendarWrite;
import com.hcl.domino.calendar.Calendaring;
import com.hcl.domino.data.Database;
import com.hcl.domino.exception.ImplicitScheduleFailedException;
import com.ibm.commons.util.PathUtil;
import com.ibm.commons.util.StringUtil;
import com.ibm.commons.util.io.StreamUtil;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public abstract class AbstractCalendaringTest extends AbstractNotesRuntimeTest {

  /**
   * Interface representing an icalendar-source.
   * Basically it consists of a set of {@link ICalProperty}.
   */
  protected interface ICal {
    boolean contains(ICalProperty prop);

    boolean hasProperty(String name);

    Stream<ICalProperty> stream();
  }

  protected static class ICalImpl implements ICal {
    private final Set<ICalProperty> properties = new LinkedHashSet<>();

    public ICalImpl(final ICalProperty... props) {
      for (final ICalProperty p : props) {
        this.properties.add(p);
      }
    }

    @Override
    public boolean contains(final ICalProperty prop) {
      return this.properties.contains(prop);
    }

    @Override
    public boolean hasProperty(final String name) {
      return this.properties.stream().anyMatch(p -> p.getName().equals(name));
    }

    @Override
    public Stream<ICalProperty> stream() {
      return this.properties.stream();
    }
  }

  /**
   * Interface representing a sinlge property in an icalendar source.
   */
  protected interface ICalProperty {
    String getName();

    String getValue();
  }

  /**
   * Simple implementation of an icalendar property with
   * {@link #hashCode()} and {@link #equals(Object)} implementations
   * based on bother name and value.
   */
  protected static class ICalPropertyImpl implements ICalProperty {
    private final String name;
    private final String value;

    public ICalPropertyImpl(final String name, final String value) {
      this.name = name;
      this.value = value;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if ((obj == null) || (this.getClass() != obj.getClass())) {
        return false;
      }
      final ICalPropertyImpl other = (ICalPropertyImpl) obj;
      if (this.name == null) {
        if (other.name != null) {
          return false;
        }
      } else if (!this.name.equals(other.name)) {
        return false;
      }
      if (this.value == null) {
        if (other.value != null) {
          return false;
        }
      } else if (!this.value.equals(other.value)) {
        return false;
      }
      return true;
    }

    @Override
    public String getName() {
      return this.name;
    }

    @Override
    public String getValue() {
      return this.value;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + (this.name == null ? 0 : this.name.hashCode());
      result = prime * result + (this.value == null ? 0 : this.value.hashCode());
      return result;
    }

    @Override
    public String toString() {
      if (this.value != null) {
        return this.name + ":" + this.value;
      } else {
        return this.name + ":";
      }
    }

  }

  @FunctionalInterface
  protected interface ICalSourceConsumer {
    /**
     * Consumer being called for loaded ical source
     * 
     * @param source cal source
     * @throws Exception
     */
    void accept(String source) throws Exception;
  }

  /**
   * Representation of an imported cal-entry.
   */
  protected static class ImportedCalEntry {
    private final String uid;
    private final String src;

    public ImportedCalEntry(final String uid, final String src) {
      this.uid = uid;
      this.src = src;
    }

    /**
     * Returns the original ical-source of the entry.
     * 
     * @return the source
     */
    public String getSource() {
      return this.src;
    }

    /**
     * Returns the new UID of the imported entry.
     * 
     * @return the uid
     */
    public String getUid() {
      return this.uid;
    }
  }

  @FunctionalInterface
  protected interface ImportICalConsumer {
    /**
     * Consumer being called for imported cal-entries.
     * 
     * @param database the database being imported to
     * @param entries  imported cal-entries
     * @throws Exception
     */
    void accept(Database database, ImportedCalEntry... entries) throws Exception;
  }

  public static final String PROP_MAIL_SERVER = "MailServer";

  public static final String PROP_MAIL_FILE = "MailFile";

  public static final String PROP_MAIL_TEMPLATE = "MailTemplate";

  public static final String DEFAULT_MAIL_TEMPLATE = "mail11.ntf";

  private static Set<String> ignoredICalFieldValues = new HashSet<>(Arrays.asList("DTSTAMP", "LAST-MODIFIED", "PRODID"));

  /**
   * T.B.D
   * Maybe improve this comparison
   * 
   * @param expected
   * @param actual
   * @param message
   */
  protected static void assertEqualsICal(final String expected, final String actual, final String message) {
    final ICal actualICal = AbstractCalendaringTest.parseICal(actual);
    final ICal expectedICal = AbstractCalendaringTest.parseICal(expected);

    final Optional<ICalProperty> description = actualICal.stream().filter(prop -> "DESCRIPTION".equals(prop.getName())).findAny();

    expectedICal.stream().forEach(prop -> {
      if (!actualICal.contains(prop)) {
        // ignore fields like last-modified and timestamp, as well as slight differences
        // in description
        // since the calendaring-api seems to reformat the descriptions in terms of
        // line-termination
        if (!AbstractCalendaringTest.ignoredICalFieldValues.contains(prop.getName())) {
          Assertions.assertEquals("DESCRIPTION", prop.getName());
          if (description.isPresent()) {
            Assertions.assertTrue(description.get().getValue().startsWith(prop.getValue()),
                StringUtil.format("Line \"{0}\" does not start with \"{1}\"",
                    StringEscapeUtils.escapeJava(description.get().getValue()),
                    StringEscapeUtils.escapeJava(prop.getValue())));
          }
        }
      }
    });
  }

  /**
   * T.B.D
   * Maybe improve this comparison
   * 
   * @param expected
   * @param actual
   * @param message
   */
  protected static void assertNotEqualsICal(final String expected, final String actual, final String message) {
    try {
      AbstractCalendaringTest.assertEqualsICal(expected, actual, message);

      throw new AssertionFailedError(message, expected, actual);
    } catch (final AssertionFailedError e) {
      // expected
    }
  }

  /**
   * Parses the given icalendar-source with a non-validating
   * simple parser, which only performs unfolding of the lines
   * 
   * @param iCalSource the source to be parsed
   * @return the parsed ICal
   */
  protected static ICal parseICal(final String iCalSource) {
    final List<ICalProperty> properties = new ArrayList<>();

    for (final String line : AbstractCalendaringTest.unfold(iCalSource.replace("\r\n", "\n")).split("\n")) {
      final String[] lineParts = line.split(":");

      properties.add(new ICalPropertyImpl(lineParts[0].trim(), lineParts.length > 1 ? lineParts[1].trim() : ""));
    }

    return new ICalImpl(properties.toArray(new ICalProperty[properties.size()]));
  }

  /**
   * Basic unfolding of multi-line fields in ical-sources.
   * 
   * @param in the ical-source
   * @return the source with unfolded fields
   */
  private static String unfold(final String in) {
    final StringBuilder out = new StringBuilder();

    for (final String line : in.split("\n")) {
      if (line.startsWith(" ")) {
        out.append(line.substring(1));
      } else {
        if (out.length() > 0) {
          out.append("\n");
        }
        out.append(line.trim());
      }
    }

    return out.toString();
  }

  public AbstractCalendaringTest() {
    super();
  }

  /**
   * Returns the calendaring instance to be used for testing
   * 
   * @return the instance
   */
  protected Calendaring getCalendaring() {
    return this.getClient().getCalendaring();
  }

  protected String getMailServer() {
    final String mailServer = System.getenv(AbstractCalendaringTest.PROP_MAIL_SERVER);
    if (!StringUtil.isEmpty(mailServer)) {
      return mailServer;
    }

    if (this.log.isLoggable(Level.INFO)) {
      this.log.info(MessageFormat.format(
          "No specific mail-server defined via \"{0}\" env-variable: Falling back to notes-ini variable \"{1}\"",
          AbstractCalendaringTest.PROP_MAIL_SERVER, AbstractCalendaringTest.PROP_MAIL_SERVER));
    }

    return this.getClient().getDominoRuntime().getPropertyString(AbstractCalendaringTest.PROP_MAIL_SERVER);
  }

  protected String getMailTemplate() {
    String mailTemplate = System.getenv(AbstractCalendaringTest.PROP_MAIL_TEMPLATE);
    if (!StringUtil.isEmpty(mailTemplate)) {
      return mailTemplate;
    }

    final DominoClient client = this.getClient();
    try (Database database = client.openDatabase("names.nsf")) {
      // TODO might make sense to just concat the major version for every release
      switch (database.getBuildVersionInfo().getMajorVersion()) {
        case 12:
          mailTemplate = "mail12.ntf";
          break;
        default:
          if (this.log.isLoggable(Level.INFO)) {
            this.log
                .info(MessageFormat.format("No specific mail-template defined via \"{0}\" env-variable: Falling back to \"{1}\"",
                    AbstractCalendaringTest.PROP_MAIL_TEMPLATE, AbstractCalendaringTest.DEFAULT_MAIL_TEMPLATE));
          }
          mailTemplate = AbstractCalendaringTest.DEFAULT_MAIL_TEMPLATE;
      }
    }
    return mailTemplate;
  }

  // assertNotEquals(iCal1, iCal2, "Entry not updated");

  /**
   * Imports all ical-files from the specified path to a mail-database.
   * 
   * @param db         target database for the import
   * @param resDirPath the path to the ical-files
   * @param resFilter  optional filter to ignore resources
   * @param c          optional consumer being called after all icals have been
   *                   imported
   * @param ceConsumer optional consumer being called for every imported entry
   * @throws Exception thrown if an error occured
   */
  protected void withICalImport(final Database db, final String resDirPath, final Predicate<String> resFilter,
      final ImportICalConsumer c, final ImportICalConsumer ceConsumer) throws Exception {
    final AtomicBoolean aborted = new AtomicBoolean(false);
    final List<ImportedCalEntry> importedEntries = new ArrayList<>();

    this.withICalSource(resDirPath, resFilter, source -> {
      final EnumSet<CalendarWrite> writeFlags = EnumSet.of(
          CalendarWrite.IGNORE_VERIFY_DB,
          CalendarWrite.DISABLE_IMPLICIT_SCHEDULING);

      String uid;
      try {
        uid = this.getClient().getCalendaring().createCalendarEntry(db, source, writeFlags);
      } catch (final ImplicitScheduleFailedException e) {
        // Occurs in the Docker builder, which isn't surprising
        this.log.info(MessageFormat.format("Aborting calendaring-tests: {0}", e.getMessage()));

        aborted.set(true);
        return;
      }

      Assertions.assertNotNull(uid, "UID should not be null");
      Assertions.assertNotEquals("", uid, "Empty uid returned");

      final ImportedCalEntry newEntry = new ImportedCalEntry(uid, source);
      importedEntries.add(newEntry);

      if (ceConsumer != null) {
        ceConsumer.accept(db, newEntry);
      }
    });

    if (!aborted.get() && c != null) {
      c.accept(db, importedEntries.toArray(new ImportedCalEntry[importedEntries.size()]));
    }
  }

  /**
   * Imports all ical-files from the specified path to a temporary mail-database.
   * 
   * @param resDirPath the path to the ical-files
   * @param c          optional consumer being called after all icals have been
   *                   imported
   * @throws Exception thrown if an error occured
   */
  protected void withICalImport(final String resDirPath, final ImportICalConsumer c) throws Exception {
    this.withICalImport(resDirPath, c, null);
  }

  /**
   * Imports all ical-files from the specified path to a temporary mail-database.
   * 
   * @param resDirPath the path to the ical-files
   * @param c          optional consumer being called after all icals have been
   *                   imported
   * @param ceConsumer optional consumer being called for every imported entry
   * @throws Exception thrown if an error occured
   */
  private void withICalImport(final String resDirPath, final ImportICalConsumer c, final ImportICalConsumer ceConsumer)
      throws Exception {
    final String mailServerName = this.getMailServer();
    final String mailTemplate = this.getMailTemplate();

    this.withTempDbFromTemplate(mailServerName, mailTemplate, tempDb -> {
      this.withICalImport(tempDb, resDirPath, null, c, ceConsumer);
    });
  }

  /**
   * Imports all ical-files from the specified path to a mail-database.
   * 
   * @param resDirPath the path to the ical-files
   * @param resFilter  optional filter to ignore resources
   * @param c          optional consumer being called after all icals have been
   *                   imported
   * @param ceConsumer optional consumer being called for every imported entry
   * @throws Exception thrown if an error occured
   */
  protected void withICalSource(final String resDirPath, final Predicate<String> resFilter, final ICalSourceConsumer c)
      throws Exception {
    AbstractNotesRuntimeTest.getResourceFiles(resDirPath).stream()
        .filter(Objects::nonNull)
        .map(name -> PathUtil.concat("/", name, '/'))
        .filter(resFilter != null ? resFilter : Objects::nonNull)
        .map(name -> StringUtil.endsWithIgnoreCase(name, ".ical") ? (InputStream) this.getClass().getResourceAsStream(name)
            : StringUtil.endsWithIgnoreCase(name, ".ical.gz")
                ? AbstractNotesRuntimeTest.call(() -> new GZIPInputStream(this.getClass().getResourceAsStream(name)))
                : null)
        .filter(Objects::nonNull)
        .forEach(is -> {
          try {
            final String originalSource = StreamUtil.readString(is).replace("\r\n", "\n");

            Assertions.assertNotEquals("", originalSource, "Invalid ical provided: empty");

            c.accept(originalSource);
          } catch (final Exception e) {
            throw new RuntimeException(e);
          } finally {
            StreamUtil.close(is);
          }
        });
  }
}