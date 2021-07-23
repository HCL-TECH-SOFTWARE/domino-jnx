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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

	public static final String PROP_MAIL_SERVER = "MailServer";
	public static final String PROP_MAIL_FILE = "MailFile";
	public static final String PROP_MAIL_TEMPLATE = "MailTemplate";
	public static final String DEFAULT_MAIL_TEMPLATE = "mail11.ntf";
	private static Set<String> ignoredICalFieldValues = new HashSet<String>(Arrays.asList("DTSTAMP","LAST-MODIFIED", "PRODID"));

	/**
	 * Basic unfolding of multi-line fields in ical-sources.
	 * 
	 * @param in		the ical-source
	 * @return			the source with unfolded fields
	 */
	private static String unfold(String in) {
		StringBuilder out=new StringBuilder();
		
		for (String line:in.split("\n")) {
			if (line.startsWith(" ")) {
				out.append(line.substring(1));
			}
			else {
				if (out.length()>0) {
					out.append("\n");
				}
				out.append(line.trim());
			}
		}
		
		return out.toString();
	}

	protected String getMailTemplate() {
		String mailTemplate = System.getenv(PROP_MAIL_TEMPLATE);
		if(!StringUtil.isEmpty(mailTemplate)) {
			return mailTemplate;
		}
		
		DominoClient client = getClient();
		try(Database database = client.openDatabase("names.nsf")) {
			// TODO might make sense to just concat the major version for every release
			switch(database.getBuildVersionInfo().getMajorVersion()) {
			case 12:
				mailTemplate = "mail12.ntf";
				break;
			default:
				if(log.isLoggable(Level.INFO)) {
					log.info(MessageFormat.format("No specific mail-template defined via \"{0}\" env-variable: Falling back to \"{1}\"", PROP_MAIL_TEMPLATE, DEFAULT_MAIL_TEMPLATE));
				}
				mailTemplate = DEFAULT_MAIL_TEMPLATE;
			}
		}
		return mailTemplate;
	}

	private String getMailFileName() {
		String mailFile = System.getenv(PROP_MAIL_FILE);
		if(!StringUtil.isEmpty(mailFile)) {
			return mailFile;
		}
		
		if(log.isLoggable(Level.INFO)) {
			log.info(MessageFormat.format("No specific mail-file defined via \"{0}\" env-variable: Falling back to notes-ini variable \"{1}\"", PROP_MAIL_FILE, PROP_MAIL_FILE));
		}
		
		return getClient().getDominoRuntime().getPropertyString(PROP_MAIL_FILE);
	}

	protected String getMailServer() {
		String mailServer = System.getenv(PROP_MAIL_SERVER);
		if(!StringUtil.isEmpty(mailServer)) {
			return mailServer;
		}
		
		if(log.isLoggable(Level.INFO)) {
			log.info(MessageFormat.format("No specific mail-server defined via \"{0}\" env-variable: Falling back to notes-ini variable \"{1}\"", PROP_MAIL_SERVER, PROP_MAIL_SERVER));
		}
		
		return getClient().getDominoRuntime().getPropertyString(PROP_MAIL_SERVER);
	}
	
	/**
	 * Returns the calendaring instance to be used for testing
	 * 
	 * @return		the instance
	 */
	protected Calendaring getCalendaring() {
		return getClient().getCalendaring();
	}

	/**
	 * T.B.D 
	 * Maybe improve this comparison
	 * 
	 * @param expected
	 * @param actual
	 * @param message
	 */
	protected static void assertEqualsICal(String expected, String actual, String message) {
		ICal actualICal=parseICal(actual);
		ICal expectedICal=parseICal(expected);
		
		Optional<ICalProperty> description=actualICal.stream().filter((prop)->{
			return "DESCRIPTION".equals(prop.getName());
		}).findAny();
		
		expectedICal.stream().forEach((prop)-> {
			if (!actualICal.contains(prop)) {
				// ignore fields like last-modified and timestamp, as well as slight differences in description
				// since the calendaring-api seems to reformat the descriptions in terms of line-termination
				if (!ignoredICalFieldValues.contains(prop.getName())) {
					assertEquals("DESCRIPTION", prop.getName());
					if(description.isPresent()) {
						assertTrue(description.get().getValue().startsWith(prop.getValue()),
							StringUtil.format("Line \"{0}\" does not start with \"{1}\"",
								StringEscapeUtils.escapeJava(description.get().getValue()),
								StringEscapeUtils.escapeJava(prop.getValue())
							)
						);
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
	protected static void assertNotEqualsICal(String expected, String actual, String message) {
		try {
			assertEqualsICal(expected, actual, message);
			
			throw new AssertionFailedError(message, expected, actual);
		}
		catch (AssertionFailedError e) {
			// expected
		}
	}

	/**
	 * Representation of an imported cal-entry.
	 */
	protected static class ImportedCalEntry {
		private String uid;
		private String src;
		
		public ImportedCalEntry(String uid, String src) {
			this.uid=uid;
			this.src=src;
		}
		
		/**
		 * Returns the new UID of the imported entry.
		 * 
		 * @return	the uid
		 */
		public String getUid() {
			return uid;
		}
		
		/**
		 * Returns the original ical-source of the entry.
		 * 
		 * @return		the source
		 */
		public String getSource() {
			return src;
		}
	}

	@FunctionalInterface
	protected static interface ImportICalConsumer {
		/**
		 * Consumer being called for imported cal-entries.
		 * 
		 * @param database		the database being imported to
		 * @param entries		imported cal-entries
		 * @throws Exception
		 */
		void accept(Database database, ImportedCalEntry... entries) throws Exception;
	}

	@FunctionalInterface
	protected static interface ICalSourceConsumer {
		/**
		 * Consumer being called for loaded ical source
		 * 
		 * @param source		cal source
		 * @throws Exception
		 */
		void accept(String source) throws Exception;
	}

	public AbstractCalendaringTest() {
		super();
	}

	/**
	 * Imports all ical-files from the specified path to a temporary mail-database.
	 * 
	 * @param resDirPath	the path to the ical-files
	 * @param c				optional consumer being called after all icals have been imported
	 * @throws Exception	thrown if an error occured
	 */
	protected void withICalImport(String resDirPath, ImportICalConsumer c) throws Exception {
		withICalImport(resDirPath, c, null);
	}

	/**
	 * Imports all ical-files from the specified path to a temporary mail-database.
	 * 
	 * @param resDirPath	the path to the ical-files
	 * @param c				optional consumer being called after all icals have been imported
	 * @param ceConsumer	optional consumer being called for every imported entry
	 * @throws Exception	thrown if an error occured
	 */
	private void withICalImport(String resDirPath, ImportICalConsumer c, ImportICalConsumer ceConsumer) throws Exception {
		String mailServerName = getMailServer();
		String mailTemplate = getMailTemplate();
		
		withTempDbFromTemplate(mailServerName, mailTemplate, tempDb -> {
			withICalImport(tempDb, resDirPath, null, c, ceConsumer);
		});
	}

	/**
	 * Imports all ical-files from the specified path to a mail-database.
	 * 
	 * @param db			target database for the import
	 * @param resDirPath	the path to the ical-files
	 * @param resFilter		optional filter to ignore resources
	 * @param c				optional consumer being called after all icals have been imported
	 * @param ceConsumer	optional consumer being called for every imported entry
	 * @throws Exception	thrown if an error occured
	 */
	protected void withICalImport(Database db, String resDirPath, Predicate<String> resFilter, ImportICalConsumer c, ImportICalConsumer ceConsumer) throws Exception {
		AtomicBoolean aborted=new AtomicBoolean(false);
		List<ImportedCalEntry> importedEntries=new ArrayList<ImportedCalEntry>();
	
		withICalSource(resDirPath, resFilter, (source) -> {
			EnumSet<CalendarWrite> writeFlags = EnumSet.of(
				CalendarWrite.IGNORE_VERIFY_DB,
				CalendarWrite.DISABLE_IMPLICIT_SCHEDULING
			);
			
			String uid;
			try {
				uid = getClient().getCalendaring().createCalendarEntry(db, source, writeFlags);
			} catch(ImplicitScheduleFailedException e) {
				// Occurs in the Docker builder, which isn't surprising
				log.info(MessageFormat.format("Aborting calendaring-tests: {0}", e.getMessage()));
				
				aborted.set(true);
				return;
			}
			
			assertNotNull(uid, "UID should not be null");
			assertNotEquals("", uid, "Empty uid returned");
	
			ImportedCalEntry newEntry = new ImportedCalEntry(uid, source);
			importedEntries.add(newEntry);
			
			if (ceConsumer!=null) {
				ceConsumer.accept(db, newEntry);
			}
		});
	
		if (!aborted.get() && c!=null) {
			c.accept(db, importedEntries.toArray(new ImportedCalEntry[importedEntries.size()]));
		}
	}

	/**
	 * Imports all ical-files from the specified path to a mail-database.
	 * 
	 * @param resDirPath	the path to the ical-files
	 * @param resFilter		optional filter to ignore resources
	 * @param c				optional consumer being called after all icals have been imported
	 * @param ceConsumer	optional consumer being called for every imported entry
	 * @throws Exception	thrown if an error occured
	 */
	protected void withICalSource(String resDirPath, Predicate<String> resFilter, ICalSourceConsumer c) throws Exception {
		getResourceFiles(resDirPath).stream()
		.filter(Objects::nonNull)
		.map(name -> PathUtil.concat("/", name, '/'))
		.filter(resFilter!=null ? resFilter : Objects::nonNull)
		.map(name ->
			StringUtil.endsWithIgnoreCase(name, ".ical") ?
				(InputStream)getClass().getResourceAsStream(name) :
			StringUtil.endsWithIgnoreCase(name, ".ical.gz") ?
				call(() -> new GZIPInputStream(getClass().getResourceAsStream(name))) :
				null
		)
		.filter(Objects::nonNull)
		.forEach(is -> {
			try {
				String originalSource=StreamUtil.readString(is).replace("\r\n", "\n");
				
				assertNotEquals("", originalSource, "Invalid ical provided: empty");
	
				c.accept(originalSource);
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				StreamUtil.close(is);
			}
		});
	}

	protected void withMailDatabase(DatabaseConsumer c) throws Exception {
		String mailServerName = getMailServer();
		String mailFile = getMailFileName();
		
		assertFalse(mailFile==null || mailFile.length()==0, "Cannot find mail-file in environment");
		
		Database mailDb = getClient().openDatabase(mailServerName, mailFile);
		 
		assertNotNull(mailDb, "Cannot find mail-database: server=" 
				+ (mailServerName==null ? "" : mailServerName) + "dbFile=" + mailFile);
		
		c.accept(mailDb);
	}
	
	// assertNotEquals(iCal1, iCal2, "Entry not updated");
	

	
	/**
	 * Parses the given icalendar-source with a non-validating 
	 * simple parser, which only performs unfolding of the lines
	 * 
	 * @param iCalSource		the source to be parsed
	 * @return					the parsed ICal
	 */
	protected static ICal parseICal(String iCalSource) {
		List<ICalProperty> properties=new ArrayList<ICalProperty>();
		
		for (String line:unfold(iCalSource.replace("\r\n", "\n")).split("\n")) {
			String[] lineParts=line.split(":");
			
			properties.add(new ICalPropertyImpl(lineParts[0].trim(), lineParts.length>1 ? lineParts[1].trim() : ""));
		}
		
		return new ICalImpl(properties.toArray(new ICalProperty[properties.size()]));
	}
	
	/**
	 * Interface representing a sinlge property in an icalendar source.
	 */
	protected static interface ICalProperty {
		public String getName();
		public String getValue();
	}
	
	/**
	 * Interface representing an icalendar-source.
	 * Basically it consists of a set of {@link ICalProperty}.
	 */
	protected static interface ICal {
		public boolean hasProperty(String name);
		public boolean contains(ICalProperty prop);
		public Stream<ICalProperty> stream();
	}

	/**
	 * Simple implementation of an icalendar property with 
	 * {@link #hashCode()} and {@link #equals(Object)} implementations
	 * based on bother name and value.
	 */
	protected static class ICalPropertyImpl implements ICalProperty {
		private String name;
		private String value;
		
		public ICalPropertyImpl(String name, String value) {
			this.name=name;
			this.value=value;
		}
		
		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getValue() {
			return value;
		}
		
		@Override
		public String toString() {
			if (value!=null) {
				return name + ":" + value;
			}
			else {
				return name + ":";
			}
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ICalPropertyImpl other = (ICalPropertyImpl) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}

	}
	
	protected static class ICalImpl implements ICal {
		private Set<ICalProperty> properties=new LinkedHashSet<ICalProperty>();

		public ICalImpl(ICalProperty... props) {
			for (ICalProperty p:props) {
				properties.add(p);
			}
		}
		
		public boolean hasProperty(String name) {
			return properties.stream().anyMatch((p)-> {
				return p.getName().equals(name);
			});
		}
		
		@Override
		public boolean contains(ICalProperty prop) {
			return properties.contains(prop);
		}

		@Override
		public Stream<ICalProperty> stream() {
			return properties.stream();
		}
	}
}