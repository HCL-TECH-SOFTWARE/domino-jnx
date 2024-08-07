/*
 * ==========================================================================
 * Copyright (C) 2019-2022 HCL America, Inc. ( http://www.hcl.com/ )
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
package com.hcl.domino.design;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import com.hcl.domino.admin.idvault.UserId;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Database.Action;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.design.agent.DesignLotusScriptAgent;
import com.hcl.domino.misc.NotesConstants;

/**
 * Provides access to a database's design elements, allowing querying,
 * selection,
 * and creation.
 */
public interface DbDesign {

  @Deprecated
  public enum FormOrSubform {
    FORM, SUBFORM, ANY
  }

  /**
   * Callback interface to get notified about progress when signing
   *
   * @author Karsten Lehmann
   */
  public interface SignCallback {
    /**
     * Method is called after signing a note
     *
     * @param designElement design element
     * @return return value to stop signing
     */
    Action noteSigned(DesignElement designElement);

    /**
     * Method to skip signing for specific notes
     *
     * @param designElement design element
     * @param currentSigner current design element signer
     * @return true to sign
     */
    boolean shouldSign(DesignElement designElement, String currentSigner);
  }

  /**
   * Creates a new, unsaved agent design element.
   *
   * @param <T> agent subtype
   * @param agentType type of agent, e.g. {@link DesignLotusScriptAgent}
   * @param agentName the name of the agent to create
   * @return the newly-created in-memory {@link DesignAgent}
   */
  <T extends DesignAgent> T createAgent(Class<T> agentType, String agentName);

  /**
   * Creates a new, unsaved script library design element.
   *
   * @param <T> library subtype
   * @param libraryType type of library, e.g. {@link LotusScriptLibrary}
   * @param libName the name of the library to create
   * @return the newly-created in-memory {@link ScriptLibrary}
   */
  <T extends ScriptLibrary> T createScriptLibrary(Class<T> libraryType, String libName);

  /**
   * Create a new unsaved database script library if it does not exist yet or the
   * one already available in the database
   *
   * @return database script library
   */
  DatabaseScriptLibrary createDatabaseScriptLibrary();

  /**
   * Creates a new, unsaved File Resource design element.
   *
   * @param filePath the name of the file resource element
   * @return the newly-created in-memory {@link FileResource}
   * @since 1.1.2
   */
  FileResource createFileResource(String filePath);

  /**
   * Creates a new, unsaved folder design element.
   *
   * @param folderName the name of the folder to create
   * @return the newly-created in-memory {@link Folder}
   */
  Folder createFolder(String folderName);

  /**
   * Creates a new, unsaved form design element.
   *
   * @param formName the name of the form to create
   * @return the newly-created in-memory {@link Form}
   */
  Form createForm(String formName);

  /**
   * Creates a new, unsaved subform design element.
   *
   * @param subformName the name of the subform to create
   * @return the newly-created in-memory {@link Subform}
   */
  Subform createSubform(String subformName);

  /**
   * Creates a new, unsaved page design element
   *
   * @param pageName the name of the page to create
   * @return the newly-created in-memory {@link Page}
   * @since 1.7.8
   */
  Page createPage(String pageName);

  /**
   * Creates a new, unsaved view design element.
   *
   * @param viewName the name of the view to create
   * @return the newly-created in-memory {@link View}
   */
  View createView(String viewName);

  /**
   * Creates a new, unsaved shared column design element
   *
   * @param columnName the name of the shared column to create
   * @return the newly-created in-memory {@link SharedColumn}
   * @since 1.5.7
   */
  SharedColumn createSharedColumn(String columnName);

  /**
   * Creates a new, unsaved shared field design element
   *
   * @param fieldName the name of the shared field to create
   * @return the newly-created in-memory {@link SharedField}
   * @since 1.27.0
   */
  SharedField createSharedField(String fieldName);

  /**
   * Creates a new, unsaved frameset design element.
   *
   * @param framesetName the name of the frameset to create
   * @return the newly-created in-memory {@link Frameset}
   */
  Frameset createFrameset(String framesetName);

  /**
   * Queries the design collection for a single design note.
   *
   * @param <T> the type of design element to find
   * @param type a {@link Class} object representing {@code <T>}
   * @param name the name or alias of the design note
   * @param partialMatch whether partial matches are allowed
   * @return an {@link OptionalInt} describing the note ID of the specified
   *         design note, or an empty one if the note was not found
   * @impl.Spec It is expected that implementations will use {@code NIFFindDesignNoteExt}
   *            if available
   * @since 1.40.0
   */
  <T extends DesignElement> OptionalInt findDesignNote(Class<T> type, String name,
      boolean partialMatch);

  /**
   * Queries the design collection for a single design note.
   *
   * @param noteClass the class of note to query (see <code>NOTE_CLASS_*</code>
   *        in {@link NotesConstants})
   * @param pattern the note flag pattern to query (see
   *        <code>DFLAGPAT_*</code> in {@link NotesConstants})
   * @param name the name or alias of the design note
   * @param partialMatch whether partial matches are allowed
   * @return an {@link OptionalInt} describing the note ID of the specified
   *         design note, or an empty one if the note was not found
   * @impl.Spec It is expected that implementations will use {@code NIFFindDesignNoteExt}
   *            if available
   * @since 1.40.0
   */
  OptionalInt findDesignNote(DocumentClass noteClass, String pattern, String name,
      boolean partialMatch);

  /**
   * Retrieves the named agent.
   *
   * @param name the element name to restrict to
   * @return an {@link Optional} describing the {@link DesignAgent},
   *         or an empty {@code Optional} if no such element exists
   */
  Optional<DesignAgent> getAgent(String name);

  /**
   * Retrieves all agents in the database.
   *
   * @return a {@link Stream} of {@link DesignAgent}s
   */
  Stream<DesignAgent> getAgents();

  /**
   * Retrieves the named folder or view.
   *
   * @param name the element name to restrict to
   * @return an {@link Optional} describing the {@link Folder} or {@link View},
   *         or an empty {@code Optional} if no such element exists
   * @param <T> collection type
   */
  <T extends CollectionDesignElement<?>> Optional<CollectionDesignElement<T>> getCollection(
      String name);

  /**
   * Retrieves all folders and views in the database.
   *
   * @return a {@link Stream} of {@link Folder}s and/or {@link View}s
   */
  Stream<CollectionDesignElement<?>> getCollections();

  /**
   * Retrieves the database properties. This is, for implementation reasons, also
   * known
   * as the icon note.
   *
   * @return the DB properties element for this database
   */
  DbProperties getDatabaseProperties();

  /**
   * Retrieves a single design element in the database of the provided type
   * matching the provided
   * title.
   *
   * @param <T> the type of design element to retrieve
   * @param type a {@link Class} object representing {@code <T>}
   * @param name the element name to restrict to
   * @return an {@link Optional} describing the requested design element,
   *         or an empty {@code Optional} if no such element exists
   */
  <T extends DesignElement> Optional<T> getDesignElementByName(Class<T> type, String name);

  /**
   * Retrieves all design elements in the database of the provided type.
   *
   * @param <T> the type of design element to retrieve
   * @param type a {@link Class} object representing {@code <T>}
   * @return a {@link Stream} of matching {@link DesignElement}s
   */
  <T extends DesignElement> Stream<T> getDesignElements(Class<T> type);

  /**
   * Retrieves summary information about all design elements in the database of
   * the provided type.
   * <p>
   * Unlike {@link #getDesignElements(Class)}, this method does not actually
   * load the underlying design-element notes, and can be used to more-efficiently
   * retrieve information about large numbers of design elements at a time.
   * </p>
   *
   * @param <T> the type of design element to retrieve
   * @param type a {@link Class} object representing {@code <T>}
   * @return a {@link Stream} of {@link DesignEntry} objects matching the
   *         element type
   * @since 1.21.0
   */
  <T extends DesignElement> Stream<DesignEntry<T>> getDesignEntries(Class<T> type);

  /**
   * Retrieves summary information about all design elements in the database
   * matching a {@code $FLAGS} pattern.
   * <p>
   * Unlike {@link #getDesignElements(Class)}, this method does not actually
   * load the underlying design-element notes, and can be used to more-efficiently
   * retrieve information about large numbers of design elements at a time.
   * </p>
   * <p>
   * If your patterns matche several design element types, it is safest to use
   * {@link DesignElement} directly as the return component type.
   * </p>
   *
   * @param <T> the type of design element to retrieve
   * @param noteClasses the {@link DocumentClass}es of the elements to limit to
   * @param patterns the flags patterns to match to the elements
   * @return a {@link Stream} of {@link DesignEntry} objects matching the
   *         flags pattern
   * @since 1.39.0
   */
  <T extends DesignElement> Stream<DesignEntry<T>> getDesignEntries(
      Collection<DocumentClass> noteClasses, Collection<String> patterns);

  /**
   * Retrieves all design elements in the database of the provided type matching
   * the provided
   * title.
   *
   * @param <T> the type of design element to retrieve
   * @param type a {@link Class} object representing {@code <T>}
   * @param name the element name to restrict to
   * @return a {@link Stream} of matching {@link DesignElement}s
   */
  <T extends DesignElement> Stream<T> getDesignElementsByName(Class<T> type, String name);

  /**
   * Retrieves the named file resource.
   * <p>
   * Note: this method uses the value of the "$TITLE" field of the design element,
   * not the "$FileNames" of the file resource. These may diverge, such as when a
   * file resource has an alias assigned to it.
   * </p>
   * <p>
   * Moreover, this method finds only elements listed in the "File Resources"
   * list in Designer, not elements like "loose" resources in the WebContent
   * directory.
   * </p>
   *
   * @param name the name of the resource to restrict to
   * @return an {@link Optional} describing the {@link FileResource}, or an empty
   *         one if no such element exists
   * @since 1.0.24
   */
  default Optional<FileResource> getFileResource(String name) {
    return getFileResource(name, false);
  }

  /**
   * Retrieves the named file resource, optionally including the pool of "XPages-side"
   * resources, such as files placed in the "WebContent" directory.
   * <p>
   * Note: this method uses the value of the "$TITLE" field of the design element,
   * not the "$FileNames" of the file resource. These may diverge, such as when a
   * file resource has an alias assigned to it.
   * </p>
   *
   * @param name the name of the resource to restrict to
   * @param includeXsp whether to include XPages-side file resources
   * @return an {@link Optional} describing the {@link FileResource}, or an empty
   *         one if no such element exists
   * @since 1.0.38
   */
  Optional<FileResource> getFileResource(String name, boolean includeXsp);

  /**
   * Retrieves all file resource design elements in the database.
   * <p>
   * This method finds only elements listed in the "File Resources"
   * list in Designer, not elements like "loose" resources in the WebContent
   * directory.
   * </p>
   *
   * @return a {@link Stream} of {@link FileResource}s
   * @since 1.0.24
   */
  default Stream<FileResource> getFileResources() {
    return getFileResources(false);
  }

  /**
   * Retrieves all file resource design elements in the database, optionally
   * including the pool of "XPages-side" resources, such as files placed in the
   * "WebContent" directory.
   *
   * @param includeXsp whether to include XPages-side file resources
   * @return a {@link Stream} of {@link FileResource}s
   * @since 1.0.38
   */
  Stream<FileResource> getFileResources(boolean includeXsp);

  /**
   * Retrieves the named folder.
   *
   * @param name the element name to restrict to
   * @return an {@link Optional} describing the {@link Folder}, or an empty one if
   *         no such view
   *         exists
   * @since 1.0.27
   */
  Optional<Folder> getFolder(String name);

  /**
   * Retrieves all folders in the database
   *
   * @return a {@link Stream} of {@link Folder}s
   * @since 1.0.27
   */
  Stream<Folder> getFolders();

  /**
   * Retrieves the named form.
   *
   * @param name the element name to restrict to
   * @return an {@link Optional} describing the {@link Form},
   *         or an empty {@code Optional} if no such element exists
   */
  Optional<Form> getForm(String name);

  /**
   * @param formSubformType the type of element to select
   * @param name the name of the design element to select
   * @return the form or subform element, or {@code null} if no element exists
   * @deprecated use {@link #getForm(String)} or {@link #getSubform(String)}
   */
  @Deprecated
  default GenericFormOrSubform<?> getFormOrSubform(final FormOrSubform formSubformType,
      final String name) {
    switch (formSubformType) {
      case FORM:
        return this.getForm(name).orElse(null);
      case SUBFORM:
        return this.getSubform(name).orElse(null);
      case ANY:
      default:
        final Form form = this.getForm(name).orElse(null);
        if (form != null) {
          return form;
        }
        final Subform subform = this.getSubform(name).orElse(null);
        if (subform != null) {
          return subform;
        }
        return null;
    }
  }

  /**
   * Retrieves all forms in the database.
   *
   * @return a {@link Stream} of {@link Form}s
   */
  Stream<Form> getForms();


  /**
   * Retrieves the named page.
   *
   * @param name the element name to restrict to
   * @return an {@link Optional} describing the {@link Page},
   *         or an empty {@code Optional} if no such element exists
   */
  Optional<Page> getPage(String name);

  /**
   * Retrieves all pages in the database.
   *
   * @return a {@link Stream} of {@link Page}s
   */
  Stream<Page> getPages();

  /**
   * Retrieves the named image resource.
   *
   * @param name the name of the resource to restrict to
   * @return an {@link Optional} describing the {@link ImageResource}, or an empty
   *         one if no such element exists
   * @since 1.0.24
   */
  Optional<ImageResource> getImageResource(String name);

  /**
   * Retrieves all image resources design elements in the database.
   *
   * @return a {@link Stream} of {@link ImageResource} elements
   * @since 1.0.24
   */
  Stream<ImageResource> getImageResources();

  /**
   * Retrieves a named navigator
   *
   * @param name the name of the navigator to restrict to
   * @return an {@link Optional} describing the {@link Navigator}, or an empty
   *         one if no such element exists
   * @since 1.1.1
   */
  Optional<Navigator> getNavigator(String name);

  /**
   * Retrieves all navigator design elements in the database.
   *
   * @return a {@link Stream} of {@link Navigator} elements
   * @since 1.1.1
   */
  Stream<Navigator> getNavigators();

  /**
   * Retrieves all script library design elements in the database. These libraries
   * will be of
   * a specific subtype based on their language.
   *
   * @return a {@link Stream} of {@link ScriptLibrary} subclass elements
   * @see JavaLibrary
   * @see JavaScriptLibrary
   * @see ServerJavaScriptLibrary
   * @see LotusScriptLibrary
   * @since 1.0.24
   */
  Stream<ScriptLibrary> getScriptLibraries();

  /**
   * Retrieves the named script library. This library will be of a specific
   * subtype based on
   * its language.
   *
   * @param name the name of the library to restrict to
   * @return an {@link Optional} describing the {@link ScriptLibrary}, or an empty
   *         one if no such element exists
   * @see JavaLibrary
   * @see JavaScriptLibrary
   * @see ServerJavaScriptLibrary
   * @see LotusScriptLibrary
   * @since 1.0.24
   */
  Optional<ScriptLibrary> getScriptLibrary(String name);

  /**
   * Retrives the database script library.
   *
   * @return an {@link Optional} describing the {@link DatabaseScriptLibrary}, or an empty one if no
   *         such element exists
   * @since 1.0.48
   */
  Optional<DatabaseScriptLibrary> getDatabaseScriptLibrary();

  /**
   * Retrieves the named subform.
   *
   * @param name the element name to restrict to
   * @return an {@link Optional} describing the {@link Subform},
   *         or an empty {@code Optional} if no such element exists
   */
  Optional<Subform> getSubform(String name);

  /**
   * Retrieves all subforms in the database.
   *
   * @return a {@link Stream} of {@link Subform}s
   */
  Stream<Subform> getSubforms();

  /**
   * Retrieves the named view.
   *
   * @param name the element name to restrict to
   * @return an {@link Optional} describing the {@link View}, or an empty one if
   *         no such view
   *         exists
   * @since 1.0.27
   */
  Optional<View> getView(String name);

  /**
   * Retrieves all views in the database
   *
   * @return a {@link Stream} of {@link View}s
   * @since 1.0.27
   */
  Stream<View> getViews();

  /**
   * Retrieves the named Outline.
   *
   * @param name the element name to restrict to
   * @return an {@link Optional} describing the {@link Outline}, or an empty one if
   *         no such Outline
   *         exists
   * @since 1.0.27
   */
  Optional<Outline> getOutline(String name);

  /**
   * Retrieves all Outlines in the database
   *
   * @return a {@link Stream} of {@link Outline}s
   * @since 1.0.27
   */
  Stream<Outline> getOutlines();

  /**
   * Retrieves the "About Application" document for the database, if it exists.
   *
   * @return an {@link Optional} describing the {@link AboutDocument} for the database,
   *         or an empty one if none has been created
   * @since 1.0.34
   */
  Optional<AboutDocument> getAboutDocument();

  /**
   * Retrieves the "Using" document for the database, if it exists.
   *
   * @return an {@link Optional} describing the {@link UsingDocument} for the database,
   *         or an empty one if none has been created
   * @since 1.0.34
   */
  Optional<UsingDocument> getUsingDocument();

  /**
   * Retrieves the named shared field.
   *
   * @param name the element name to restrict to
   * @return an {@link Optional} describing the {@link SharedField}, or an empty one if
   *         no such field exists
   * @since 1.0.34
   */
  Optional<SharedField> getSharedField(String name);

  /**
   * Retrieves all shared fields in the database
   *
   * @return a {@link Stream} of {@link SharedField}s
   * @since 1.0.34
   */
  Stream<SharedField> getSharedFields();

  /**
   * Retrieves the shared-actions note, if it exists in the database.
   *
   * @return an {@link Optional} describing the {@link SharedActions} for the database, or
   *         an empty one if there is no shared-actions note
   * @since 1.0.37
   */
  Optional<SharedActions> getSharedActions();

  /**
   * Retrieves the named shared column.
   *
   * @param name the element name to restrict to
   * @return an {@link Optional} describing the {@link SharedColumn}, or an empty one if
   *         no such column exists
   * @since 1.0.37
   */
  Optional<SharedColumn> getSharedColumn(String name);

  /**
   * Retrieves all shared columns in the database.
   *
   * @return a {@link Stream} of {@link SharedColumn}s
   * @since 1.0.37
   */
  Stream<SharedColumn> getSharedColumns();

  /**
   * Retrieves the named style sheet resource.
   *
   * @param name the element name to restrict to
   * @return an {@link Optional} describing the {@link StyleSheet}, or an empty one if
   *         no such style sheet exists
   * @since 1.0.38
   */
  Optional<StyleSheet> getStyleSheet(String name);

  /**
   * Retrieves all style sheet in the database.
   *
   * @return a {@link Stream} of {@link StyleSheet}s
   * @since 1.0.38
   */
  Stream<StyleSheet> getStyleSheets();

  /**
   * Retrieves the named wiring properties element.
   *
   * @param name the element name to restrict to
   * @return an {@link Optional} describing the {@link WiringProperties}, or an empty one if
   *         no such wiring properties element exists
   * @since 1.0.38
   */
  Optional<WiringProperties> getWiringPropertiesElement(String name);

  /**
   * Retrieves all wiring properties elements in the database.
   *
   * @return a {@link Stream} of {@link WiringProperties}s
   * @since 1.0.38
   */
  Stream<WiringProperties> getWiringPropertiesElements();

  /**
   * Retrieves the named theme element.
   *
   * @param name the element name to restrict to
   * @return an {@link Optional} describing the {@link Theme}, or an empty one if
   *         no such theme exists
   * @since 1.0.38
   */
  Optional<Theme> getTheme(String name);

  /**
   * Retrieves all theme elements in the database.
   *
   * @return a {@link Stream} of {@link Theme}s
   * @since 1.0.38
   */
  Stream<Theme> getThemes();

  /**
   * Retrieves the named Composite Application Component element.
   *
   * @param name the element name to restrict to
   * @return an {@link Optional} describing the {@link CompositeComponent}, or an empty one if
   *         no such element exists
   * @since 1.0.38
   */
  Optional<CompositeComponent> getCompositeComponent(String name);

  /**
   * Retrieves all Composite Application elements in the database.
   *
   * @return a {@link Stream} of {@link CompositeApplication}s
   * @since 1.0.38
   */
  Stream<CompositeApplication> getCompositeApplications();

  /**
   * Retrieves the named Composite Application element.
   *
   * @param name the element name to restrict to
   * @return an {@link Optional} describing the {@link CompositeApplication}, or an empty one if
   *         no such element exists
   * @since 1.0.38
   */
  Optional<CompositeApplication> getCompositeApplication(String name);

  /**
   * Retrieves all Composite Application Component elements in the database.
   *
   * @return a {@link Stream} of {@link CompositeComponent}s
   * @since 1.0.38
   */
  Stream<CompositeComponent> getCompositeComponents();

  /**
   * Retrieves the named XPage element.
   *
   * @param name the element name to restrict to
   * @return an {@link Optional} describing the {@link XPage}, or an empty one if
   *         no such element exists
   * @since 1.0.38
   */
  Optional<XPage> getXPage(String name);

  /**
   * Retrieves all XPage elements in the database.
   *
   * @return a {@link Stream} of {@link XPage}s
   * @since 1.0.38
   */
  Stream<XPage> getXPages();

  /**
   * Retrieves a design element by its UNID.
   *
   * @param <T> the expected type of the design element
   * @param unid the UNID of the design element to retrieve
   * @return an {@link Optional} describing the design element, or an empty one if no
   *         note by that UNID exists
   * @throws ClassCastException if the design element represented by {@code unid} is not of the
   *         type represented by {@code <T>}
   * @throws IllegalArgumentException if the note represented by {@code unid} is not a design
   *         element
   * @since 1.0.37
   */
  <T extends DesignElement> Optional<T> getDesignElementByUNID(String unid);

  /**
   * Retrieves the named file, image, or stylesheet resource as a stream of bytes.
   *
   * @param filePath the path to the file-type resource
   * @return an {@link Optional} describing an {@link InputStream} of the file's bytes,
   *         or an empty one if no such file exists
   * @since 1.0.38
   */
  Optional<InputStream> getResourceAsStream(String filePath);

  /**
   * Opens a new stream to the named file-type resource (file resource, image, or stylesheet).
   * This method uses the same mechanism as {@link #getResourceAsStream(String)} to locate
   * existing resources.
   * <p>
   * If the named resource doesn't exist, then this method will create a new resource with
   * this name on closing the stream. This resource will be a normal file resource that will
   * show up in {@link #getFileResources()}.
   * </p>
   * <p>
   * Note: it is not guaranteed that any changes made to the resource are written to the NSF
   * until {@link OutputStream#close()} is called.
   * </p>
   *
   * @param filePath the path of the file to write
   * @return a new {@link OutputStream}
   * @since 1.0.39
   */
  OutputStream newResourceOutputStream(String filePath);

  /**
   * Opens a new stream to the named file-type resource (file resource, image, or stylesheet).
   * This method uses the same mechanism as {@link #getResourceAsStream(String)} to locate
   * existing resources.
   * <p>
   * If the named resource doesn't exist, then this method will create a new resource with
   * this name on closing the stream. This resource will be a normal file resource that will
   * show up in {@link #getFileResources()}.
   * </p>
   * <p>
   * When {@link OutputStream#close()} is called, then the provided {@code callback} will
   * be executed with the contextual {@link DesignElement}. This object will be one of:
   * </p>
   * <ul>
   * <li>{@link NamedFileElement}</li>
   * <li>{@link ServerJavaScriptLibrary}</li>
   * <li>{@link JavaScriptLibrary}</li>
   * </ul>
   * <p>
   * Note: it is not guaranteed that any changes made to the resource are written to the NSF
   * until {@link OutputStream#close()} is called.
   * </p>
   *
   * @param filePath the path of the file to write
   * @param callback a {@link Consumer} that will be provided with the {@link DesignElement}
   *        that is written to upon close. May be {@code null}
   * @return a new {@link OutputStream}
   * @since 1.1.2
   */
  OutputStream newResourceOutputStream(String filePath, Consumer<DesignElement> callback);

  /**
   * Queries all design elements in the database by the provided formula and
   * restricted
   * to the provided type.
   *
   * @param <T> the type of design element to query
   * @param type a {@link Class} object representing {@code <T>}
   * @param formula the formula query to execute
   * @return a {@link Stream} of matching {@link DesignElement}s
   */
  <T extends DesignElement> Stream<T> queryDesignElements(final Class<T> type,
      final String formula);

  /**
   * Queries all design elements in the database by the provided formula.
   *
   * @param formula the formula query to execute
   * @return a {@link Stream} of matching {@link DesignElement}s
   */
  Stream<DesignElement> queryDesignElements(String formula);

  /**
   * Signs all design documents of the specified types
   *
   * @param docClass doc classes to sign
   * @param id id to sign or null for active Notes ID
   * @param callback optional sign callback to control with document to sign
   */
  void signAll(Set<DocumentClass> docClass, UserId id, SignCallback callback);

  /**
   * Returns the parent database of the design
   *
   * @return database
   */
  Database getDatabase();

  /**
   * Retrieves all framesets in the database.
   *
   * @return a {@link Stream} of {@link Frameset}s
   * @since 1.0.42
   */
  Stream<Frameset> getFramesets();

  /**
   * Retrieves the named frameset.
   *
   * @param name the element name to restrict to
   * @return an {@link Optional} describing the {@link Frameset}, or an empty one if
   *         no such theme exists
   * @since 1.0.42
   */
  Optional<Frameset> getFrameset(String name);

}
