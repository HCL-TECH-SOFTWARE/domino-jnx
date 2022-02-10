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
package com.hcl.domino.commons.util;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.hcl.domino.DominoException;
import com.hcl.domino.commons.OSLoadStringProvider;
import com.hcl.domino.commons.errors.ErrorText;
import com.hcl.domino.commons.errors.errorcodes.IAgntsErr;
import com.hcl.domino.commons.errors.errorcodes.IBsafeErr;
import com.hcl.domino.commons.errors.errorcodes.IClErr;
import com.hcl.domino.commons.errors.errorcodes.IDbdrvErr;
import com.hcl.domino.commons.errors.errorcodes.IDirErr;
import com.hcl.domino.commons.errors.errorcodes.IEventErr;
import com.hcl.domino.commons.errors.errorcodes.IFtErr;
import com.hcl.domino.commons.errors.errorcodes.IHtmlErr;
import com.hcl.domino.commons.errors.errorcodes.IMailMiscErr;
import com.hcl.domino.commons.errors.errorcodes.IMiscErr;
import com.hcl.domino.commons.errors.errorcodes.INetErr;
import com.hcl.domino.commons.errors.errorcodes.INifErr;
import com.hcl.domino.commons.errors.errorcodes.INsfErr;
import com.hcl.domino.commons.errors.errorcodes.IOdsErr;
import com.hcl.domino.commons.errors.errorcodes.IOsErr;
import com.hcl.domino.commons.errors.errorcodes.IRegErr;
import com.hcl.domino.commons.errors.errorcodes.IRouteErr;
import com.hcl.domino.commons.errors.errorcodes.ISecErr;
import com.hcl.domino.commons.errors.errorcodes.ISrvErr;
import com.hcl.domino.commons.errors.errorcodes.IXmlErr;
import com.hcl.domino.exception.BadPasswordException;
import com.hcl.domino.exception.CancelException;
import com.hcl.domino.exception.CompactInProgressException;
import com.hcl.domino.exception.CompactionRequiredException;
import com.hcl.domino.exception.DocumentDeletedException;
import com.hcl.domino.exception.EntryNotFoundInIndexException;
import com.hcl.domino.exception.FileDoesNotExistException;
import com.hcl.domino.exception.FixupInProgressException;
import com.hcl.domino.exception.FixupNeededException;
import com.hcl.domino.exception.ImplicitScheduleFailedException;
import com.hcl.domino.exception.InvalidDocumentException;
import com.hcl.domino.exception.ItemNotFoundException;
import com.hcl.domino.exception.ItemNotPresentException;
import com.hcl.domino.exception.MimePartNotFoundException;
import com.hcl.domino.exception.NoCrossCertificateException;
import com.hcl.domino.exception.NotAuthorizedException;
import com.hcl.domino.exception.QuitPendingException;
import com.hcl.domino.exception.ServerNotFoundException;
import com.hcl.domino.exception.ServerRestrictedException;
import com.hcl.domino.exception.ServerUnavailableException;
import com.hcl.domino.exception.SpecialObjectCannotBeLocatedException;
import com.hcl.domino.misc.NotesConstants;

/**
 * Utility class to work with errors coming out of C API method calls
 *
 * @author Karsten Lehmann
 */
public class NotesErrorUtils {
  private static volatile Map<Short, String> staticTexts;

  /**
   * Checks a Notes C API result code for errors and throws a
   * {@link DominoException} with
   * a proper error message if the specified result code is not 0.
   * 
   * @param result code
   * @throws DominoException if the result is not 0
   */
  public static void checkResult(final short result) {
    NotesErrorUtils.checkResult(result, false);
  }

  /**
   * Checks a Notes C API result code for errors and throws a
   * {@link DominoException} with
   * a proper error message if the specified result code is not 0.
   * 
   * @param result          code
   * @param staticTextsOnly true to not use the C API method to resolve the error
   *                        code (e.g. for errors during C API initialization)
   * @throws DominoException if the result is not 0
   */
  public static void checkResult(final short result, final boolean staticTextsOnly) {
    if (result == 0) {
      return;
    }

    final Optional<DominoException> ex = NotesErrorUtils.toNotesError(result, staticTextsOnly);
    if (!ex.isPresent()) {
      return;
    } else {
      // Remove the stack trace elements for this and toNotesError
      final StackTraceElement[] stack = ex.get().getStackTrace();
      ex.get().setStackTrace(Arrays.copyOfRange(stack, 2, stack.length));
      throw ex.get();
    }
  }

  /**
   * Converts a C API error code to an error message
   * 
   * @param err error code
   * @return error message
   */
  public static String errToString(final short err) {
    return NotesErrorUtils.errToString(err, false);
  }

  /**
   * Converts a C API error code to an error message
   * 
   * @param err             error code
   * @param staticTextsOnly true to not use the C API method to resolve the error
   *                        code (e.g. for errors during C API initialization)
   * @return error message
   */
  public static String errToString(final short err, final boolean staticTextsOnly) {
    if (err == 0) {
      return ""; //$NON-NLS-1$
    }

    final String message = staticTextsOnly ? "" : OSLoadStringProvider.get().loadString(0, err); //$NON-NLS-1$
    if (StringUtil.isEmpty(message)) {
      final Map<Short, String> texts = NotesErrorUtils.getStaticTexts();
      final String errText = texts.get(err);
      return StringUtil.toString(errText);
    }
    return message;
  }

  private static Map<Short, String> getStaticTexts() {
    if (NotesErrorUtils.staticTexts == null) {
      NotesErrorUtils.staticTexts = AccessController.doPrivileged((PrivilegedAction<Map<Short, String>>) () -> {
        final Map<Short, String> texts = new HashMap<>();

        final Class<?>[] classes = new Class[] {
            IAgntsErr.class,
            IBsafeErr.class,
            IClErr.class,
            IDbdrvErr.class,
            IDirErr.class,
            IEventErr.class,
            IFtErr.class,
            IHtmlErr.class,
            IMailMiscErr.class,
            IMiscErr.class,
            INetErr.class,
            INifErr.class,
            INsfErr.class,
            IOdsErr.class,
            IOsErr.class,
            IRegErr.class,
            IRouteErr.class,
            ISecErr.class,
            ISrvErr.class,
            IXmlErr.class
        };

        for (final Class<?> currClass : classes) {
          final Field[] fields = currClass.getFields();
          for (final Field currField : fields) {
            final ErrorText currTxt = currField.getAnnotation(ErrorText.class);
            if (currTxt != null) {
              try {
                final short currCode = currField.getShort(currClass);
                texts.put(currCode, currTxt.text());
              } catch (IllegalArgumentException | IllegalAccessException e) {
                continue;
              }
            }
          }
        }

        return texts;
      });
    }
    return NotesErrorUtils.staticTexts;
  }

  /**
   * Converts an error code into a {@link DominoException}.
   * 
   * @param result error code
   * @return an {@link Optional} describing a newly-created instance of
   *         {@link DominoException} or a subclass,
   *         an empty one if {@code result} is 0
   */
  public static Optional<DominoException> toNotesError(final short result) {
    return NotesErrorUtils.toNotesError(result, false);
  }

  /**
   * Converts an error code into a {@link DominoException}.
   * 
   * @param result          error code
   * @param staticTextsOnly true to not use the C API method to resolve the error
   *                        code (e.g. for errors during C API initialization)
   * @return an {@link Optional} describing a newly-created instance of
   *         {@link DominoException} or a subclass,
   *         an empty one if {@code result} is 0
   */
  public static Optional<DominoException> toNotesError(final short result, final boolean staticTextsOnly) {
    final short status = (short) (result & NotesConstants.ERR_MASK);
    if (status == 0) {
      return Optional.empty();
    }

    final boolean isRemoteError = (result & NotesConstants.STS_REMOTE) == NotesConstants.STS_REMOTE;

    String message;
    try {
      message = NotesErrorUtils.errToString(status, staticTextsOnly);
    } catch (final Throwable e) {
      return Optional.of(new DominoException(result, "ERR " + status)); //$NON-NLS-1$
    }

    final int s = Short.toUnsignedInt(status);
    final String msg = MessageFormat.format(
        "{0} (error code: 0x{1}, raw error with all flags: 0x{2})",
        message,
        Integer.toHexString(s) + (isRemoteError ? ", remote server error" : ""), //$NON-NLS-2$
        Integer.toHexString(result));

    return NotesErrorUtils.toNotesError(result, msg);
  }

  /**
   * Creates a {@link DominoException} or subclass based on the provided result
   * code, while
   * using a customized message.
   * <p>
   * If {@code message} is {@code null}, then this method will look up the message
   * using
   * the internal Notes API, combined with some higher-level additional
   * information.
   * </p>
   * 
   * @param result  the status code of the underlying error
   * @param message the message to include in the exception
   * @return an {@link Optional} describing a newly-created instance of
   *         {@link DominoException} or a subclass,
   *         an empty one if {@code result} is 0
   */
  public static Optional<DominoException> toNotesError(final short result, final String message) {
    final short status = (short) (result & NotesConstants.ERR_MASK);
    if (status == 0) {
      return Optional.empty();
    }

    final int s = Short.toUnsignedInt(status);
    switch (s) {
      case INsfErr.ERR_NOTE_DELETED:
        return Optional.of(new DocumentDeletedException(s, message));
      case INsfErr.ERR_ITEM_NOT_FOUND:
        return Optional.of(new ItemNotFoundException(s, message));
      case IMiscErr.ERR_NOT_FOUND:
        return Optional.of(new EntryNotFoundInIndexException(s, message));
      case INifErr.ERR_NO_SUCH_ITEM:
        return Optional.of(new ItemNotPresentException(s, message));
      case ISecErr.ERR_SECURE_BADPASSWORD:
        return Optional.of(new BadPasswordException(s, message));
      case IOsErr.ERR_NOEXIST:
        return Optional.of(new FileDoesNotExistException(s, message));
      case IMailMiscErr.ERR_IMPLICIT_SCHED_FAILED:
        return Optional.of(new ImplicitScheduleFailedException(s, message));
      case IMiscErr.ERR_MQ_QUITTING:
        return Optional.of(new QuitPendingException(s, message));
      case INsfErr.ERR_NOACCESS:
        return Optional.of(new NotAuthorizedException(s, message));
      case IOsErr.ERR_CANCEL:
        return Optional.of(new CancelException(s, message));
      case ISrvErr.ERR_SERVER_UNAVAILABLE:
        return Optional.of(new ServerUnavailableException(s, message));
      case ISrvErr.ERR_SERVER_RESTRICTED:
        return Optional.of(new ServerRestrictedException(s, message));
      case IClErr.ERR_SERVER_NOT_FOUND:
        return Optional.of(new ServerNotFoundException(s, message));
      case INsfErr.ERR_FIXUP_NEEDED:
        return Optional.of(new FixupNeededException(s, message));
      case INsfErr.ERR_FIXUP_IN_PROGRESS:
        return Optional.of(new FixupInProgressException(s, message));
      case INsfErr.ERR_NSFOPENEXCLUSIVE:
        return Optional.of(new CompactInProgressException(s, message));
      case IMiscErr.ERR_MISC_MIMEPART_NOT_FOUND:
        return Optional.of(new MimePartNotFoundException(s, message));
      case INsfErr.ERR_SPECIAL_ID:
        return Optional.of(new SpecialObjectCannotBeLocatedException(s, message));
      case INsfErr.ERR_INVALID_NOTE:
        return Optional.of(new InvalidDocumentException(s, message));
      case INsfErr.ERR_LOCALSEC_NEEDCOMPACT:
        return Optional.of(new CompactionRequiredException(s, message));
      case IBsafeErr.ERR_BSAFE_NO_CROSS_CERT:
        return Optional.of(new NoCrossCertificateException(s, message));
      default:
        return Optional.of(new DominoException(s, message));
    }
  }
}
