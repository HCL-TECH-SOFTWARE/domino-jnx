package com.hcl.domino.commons.design;

import java.util.Optional;

import com.hcl.domino.data.Document;
import com.hcl.domino.design.ClassicThemeBehavior;
import com.hcl.domino.design.DesignConstants;
import com.hcl.domino.design.Page;
import com.hcl.domino.misc.DominoEnumUtil;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.richtext.records.CDDocument;
import com.hcl.domino.richtext.records.CDLinkColors;
import com.hcl.domino.richtext.structures.ColorValue;

public class PageImpl extends AbstractPageElement<Page> implements Page, IDefaultAutoFrameElement {

  public PageImpl(Document doc) {
    super(doc);
  }

  @Override
  public void initializeNewDesignNote() {
    // TODO Auto-generated method stub
    
  }
  
  @Override
  public ClassicThemeBehavior getClassicThemeBehavior() {
    return getDocumentRecord()
      .flatMap(rec -> {
        short flags = rec.getFlags3Raw();
        byte themeVal = (byte)((flags & DesignConstants.TPL_FLAG_THEMESETTING) >> DesignConstants.TPL_SHIFT_THEMESETTING);
        return DominoEnumUtil.valueOf(ClassicThemeBehavior.class, themeVal);
      })
      .orElse(ClassicThemeBehavior.USE_DATABASE_SETTING);
  }
  
  @Override
  public WebRenderingSettings getWebRenderingSettings() {
    return new DefaultWebRenderingSettings();
  }
  
  @Override
  public boolean isUseInitialFocus() {
    return !getDocumentFlags3().contains(CDDocument.Flag3.NOINITIALFOCUS);
  }

  @Override
  public boolean isFocusOnF6() {
    return !getDocumentFlags3().contains(CDDocument.Flag3.NOFOCUSWHENF6);
  }
  
  // *******************************************************************************
  // * Internal implementation
  // *******************************************************************************
  
  private class DefaultWebRenderingSettings implements WebRenderingSettings {

    @Override
    public boolean isRenderRichContentOnWeb() {
      return !getWebFlags().contains(NotesConstants.WEBFLAG_NOTE_IS_HTML);
    }

    @Override
    public Optional<String> getWebMimeType() {
      if(isRenderRichContentOnWeb()) {
        return Optional.empty();
      } else {
        return Optional.of(getDocument().getAsText(NotesConstants.ITEM_NAME_FILE_MIMETYPE, ' '));
      }
    }

    @Override
    public Optional<String> getWebCharset() {
      String charset = getDocument().getAsText(NotesConstants.ITEM_NAME_FILE_MIMECHARSET, ' ');
      return charset.isEmpty() ? Optional.empty() : Optional.of(charset);
    }

    @Override
    public ColorValue getActiveLinkColor() {
      return getHtmlCodeItem()
        .stream()
        .filter(CDLinkColors.class::isInstance)
        .map(CDLinkColors.class::cast)
        .findFirst()
        .map(CDLinkColors::getActiveColor)
        .orElseGet(DesignColorsAndFonts::defaultActiveLink);
    }

    @Override
    public ColorValue getUnvisitedLinkColor() {
      return getHtmlCodeItem()
        .stream()
        .filter(CDLinkColors.class::isInstance)
        .map(CDLinkColors.class::cast)
        .findFirst()
        .map(CDLinkColors::getUnvisitedColor)
        .orElseGet(DesignColorsAndFonts::defaultUnvisitedLink);
    }

    @Override
    public ColorValue getVisitedLinkColor() {
      return getHtmlCodeItem()
        .stream()
        .filter(CDLinkColors.class::isInstance)
        .map(CDLinkColors.class::cast)
        .findFirst()
        .map(CDLinkColors::getVisitedColor)
        .orElseGet(DesignColorsAndFonts::defaultVisitedLink);
    }
  }
}
