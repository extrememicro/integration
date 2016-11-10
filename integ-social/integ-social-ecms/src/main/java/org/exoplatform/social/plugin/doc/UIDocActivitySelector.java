/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.plugin.doc;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.webui.composer.UIComposer;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.commons.UIDocumentSelector;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : tuan pham
 *          tuanp@exoplatform.com
 * Nov 8, 2011  
 */
@ComponentConfig(
lifecycle = Lifecycle.class,
template = "classpath:groovy/social/plugin/doc/UIDocActivitySelector.gtmpl",
events = {
  @EventConfig(listeners = UIDocActivitySelector.CancelActionListener.class),
  @EventConfig(listeners = UIDocActivitySelector.SelectedFileActionListener.class)

}             

    )
public class UIDocActivitySelector extends UIContainer implements UIPopupComponent {

  protected static final String UIDOCUMENTSELECTOR = "UIDocumentSelector";
  protected static final String CANCEL             = "Cancel";
  public static final String    SELECTEDFILE       = "SelectedFile";
  private static final Log      LOG                = ExoLogger.getLogger(UIDocActivitySelector.class);

  public UIDocActivitySelector(){
    try {
      addChild(UIDocumentSelector.class, null, UIDOCUMENTSELECTOR);
    } catch (Exception e) { 
      //UIContainer add selector exception
      LOG.error("An exception happens when init UIDocActivitySelector", e);
    }
  }
  @Override
  public void activate() {

  }

  @Override
  public void deActivate() {
    UIPopupWindow popup = (UIPopupWindow)this.getParent();
    popup.setUIComponent(null);
    popup.setShow(false);
    popup.setRendered(false);
    ((WebuiRequestContext) WebuiRequestContext.getCurrentInstance()).addUIComponentToUpdateByAjax(popup.getParent());
  }

  static public class CancelActionListener extends EventListener<UIDocActivitySelector>{
    public void execute(Event<UIDocActivitySelector> event) throws Exception {
      UIDocActivitySelector uiDocActivitySelector = event.getSource() ;
      uiDocActivitySelector.deActivate();
    }
  }

  static public class SelectedFileActionListener extends EventListener<UIDocActivitySelector>{
    public void execute(Event<UIDocActivitySelector> event) throws Exception {
      UIDocActivitySelector uiDocActivitySelector = event.getSource() ;
      UIPortletApplication uiApp = uiDocActivitySelector.getAncestorOfType(UIPortletApplication.class);
      UIDocumentSelector documentSelector = uiDocActivitySelector.getChild(UIDocumentSelector.class);
      String rawPath = documentSelector.getSeletedFile() ;
      if(rawPath == null || rawPath.trim().length() <= 0) {
        uiApp.addMessage(new ApplicationMessage("UIDocActivitySelector.msg.not-a-file", null, ApplicationMessage.WARNING));
        ((PortalRequestContext) event.getRequestContext().getParentAppRequestContext()).ignoreAJAXUpdateOnPortlets(true);
        return;
      } else {
        UIComposer uiComposer = uiApp.findFirstComponentOfType(UIComposer.class);
        UIDocActivityComposer uiDocActivityComposer = uiComposer.findFirstComponentOfType(UIDocActivityComposer.class);
        uiDocActivityComposer.doSelect(documentSelector.getSeletedFileType(), rawPath) ;
        uiDocActivitySelector.deActivate() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiDocActivityComposer.getParent());
      }
    }

  }
}
