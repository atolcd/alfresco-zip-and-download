/*
 * Copyright (C) 2011 Atol Conseils et Développements.
 * http://www.atolcd.com/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * DocumentList "Zip" action
 *
 * @namespace Alfresco
 * @class Alfresco.DocListToolbar
 */
(function() {
/**
 * Zip multiple documents.
 *
 * @method onActionZip
 */
  Alfresco.DocListToolbar.prototype.onActionZip = function DLTB_onActionZip() {
    if (!this.modules.docList) {
      return;
    }

    var files = this.modules.docList.getSelectedFiles();

    var i, length=files.length, nodesRef = [];
    for(i=0 ; i<length ; i++) {
      try {
        nodesRef.push(files[i].nodeRef.split('/').reverse()[0]); // Get only nodeId
      }
      catch(e) {}
    }


    // Download Simple Dialog
    var downloadDlg = new YAHOO.widget.SimpleDialog('download-zip-dialog', {
      width: "400px",
      modal:true,
      fixedcenter: true,
      draggable: false,
      constraintoviewport: true
    });

    downloadDlg.setHeader(Alfresco.util.message("zip-content-action.header"));

    // Create dialog div element
    var html = '<div class="filename-container">';
    html += '<label for="zip-filename">' + Alfresco.util.message("zip-content-action.filename.label") + '</label>'; // Create label element
    html += '<input id="zip-filename" type="text" name="zip-filename" /> .zip'; // Create input for filename
    html += '</div>';

    // Create checkbox
    html += '<p class="checkbox-noaccent-container">';
    html += '<input id="no-accent" type="checkbox" name="no-accent">';
    html += '<label for="no-accent">' + Alfresco.util.message("zip-content-action.no-accent.label") + '</label>';
    html += '</p>';


    // Add DOM elements into dialog body
    downloadDlg.setBody(html);

    // Add buttons : cancel and download
    buttons = [
      {
        text: Alfresco.util.message("zip-content-action.cancel"),
        handler: function(){ this.destroy(); }
      },
      {
        text: Alfresco.util.message("zip-content-action.download"),
        handler: function() {
          var filename = YAHOO.util.Dom.get("zip-filename").value;
          var noAccent = YAHOO.util.Dom.get("no-accent").checked;

          if (filename != "") {
            var url = Alfresco.constants.PROXY_URI + "slingshot/zipContents?nodes=" + nodesRef.join() + "&filename=" + encodeURIComponent(filename) + "&noaccent=" + noAccent;
            window.open(url);
            this.destroy();
          }
          else {
            Alfresco.util.PopupManager.displayMessage({
              text: Alfresco.util.message("zip-content-action.filename.mandatory-field"),
              spanClass: "message"
            });
          }
        },
        isDefault:true
      }
    ];
    downloadDlg.cfg.queueProperty("buttons", buttons);

    // Dialog render
    downloadDlg.render(document.body);
  };
})();