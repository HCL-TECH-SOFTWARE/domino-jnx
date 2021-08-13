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
package com.hcl.domino.design;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import com.hcl.domino.admin.idvault.UserId;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Database.Action;
import com.hcl.domino.data.DocumentClass;

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
   * @param agentName the name of the agent to create
   * @return the newly-created in-memory {@link DesignAgent}
   */
  DesignAgent createAgent(String agentName);

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
   * Creates a new, unsaved view design element.
   *
   * @param viewName the name of the view to create
   * @return the newly-created in-memory {@link View}
   */
  View createView(String viewName);
  
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
   */
  Optional<CollectionDesignElement> getCollection(String name);

  /**
   * Retrieves all folders and views in the database.
   *
   * @return a {@link Stream} of {@link Folder}s and/or {@link View}s
   */
  Stream<CollectionDesignElement> getCollections();

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
   * @param <T>  the type of design element to retrieve
   * @param type a {@link Class} object representing {@code <T>}
   * @param name the element name to restrict to
   * @return an {@link Optional} describing the requested design element,
   *         or an empty {@code Optional} if no such element exists
   */
  <T extends DesignElement> Optional<T> getDesignElementByName(Class<T> type, String name);

  /**
   * Retrieves all design elements in the database of the provided type.
   *
   * @param <T>  the type of design element to retrieve
   * @param type a {@link Class} object representing {@code <T>}
   * @return a {@link Stream} of matching {@link DesignElement}s
   */
  <T extends DesignElement> Stream<T> getDesignElements(Class<T> type);

  /**
   * Retrieves all design elements in the database of the provided type matching
   * the provided
   * title.
   *
   * @param <T>  the type of design element to retrieve
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
   *
   * @param name the name of the resource to restrict to
   * @return an {@link Optional} describing the {@link FileResource}, or an empty
   *         one if no such element exists
   * @since 1.0.24
   */
  Optional<FileResource> getFileResource(String name);

  /**
   * Retrieves all file resource design elements in the database.
   *
   * @return a {@link Stream} of {@link FileResource}s
   * @since 1.0.24
   */
  Stream<FileResource> getFileResources();

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
   * @param name            the name of the design element to select
   * @return the form or subform element, or {@code null} if no element exists
   * @deprecated use {@link #getForm(String)} or {@link #getSubform(String)}
   */
  @Deprecated
  default GenericFormOrSubform<?> getFormOrSubform(final FormOrSubform formSubformType, final String name) {
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
   * Queries all design elements in the database by the provided formula and
   * restricted
   * to the provided type.
   *
   * @param <T>     the type of design element to query
   * @param type    a {@link Class} object representing {@code <T>}
   * @param formula the formula query to execute
   * @return a {@link Stream} of matching {@link DesignElement}s
   */
  <T extends DesignElement> Stream<T> queryDesignElements(final Class<T> type, final String formula);

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
   * @param id       id to sign or null for active Notes ID
   * @param callback optional sign callback to control with document to sign
   */
  void signAll(Set<DocumentClass> docClass, UserId id, SignCallback callback);

  /**
   * Returns the parent database of the design
   * 
   * @return database
   */
  Database getDatabase();
}
