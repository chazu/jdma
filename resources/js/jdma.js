/******************************************************************************
 * Copyright (c) 2002-2011 Peter 'Merlin' Balsiger and Fredy 'Mythos' Dobler
 * All rights reserved
 *
 * This file is part of Dungeon Master Assistant.
 *
 * Dungeon Master Assistant is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Dungeon Master Assistant is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Dungeon Master Assistant; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *****************************************************************************/


/**
 * This is the main DMA javascript file. It contains most javascript methods
 * used in all the HTML pages.
 *
 * @file          DMA.js
 *
 * @author        balsiger@ixitxachitls.net (Peter Balsiger)
 *
 */

//--------------------------------- registration ----------------------------------

/**
 * Create the registration dialog and ask the user to register.
 *
 */
function registration()
{
  var registrationDialog = $('<div id="registration"/>')
    .html('Please provide username to register:'
          + '<p>'
          + '<label>Username'
          + '<input type="text" name="username" validate="non-empty" '
          + 'validateButton="#registration-button" size="30" maxlength="30">'
          + '</label>'
          + '<p>'
          + '<div id="registration-error"/>')
    .dialog({
      title: 'DMA Registration',
      modal: true,
      resizable: false,
      width: 300,
      dialogClass: 'registration-dialog dialog',
      buttons: [
          {
            id:  'registration-button',
            text: 'Registration',
            click: function() {
              doRegistration($('input[name="username"]', this).val());
            }
          },
          {
            text: 'Cancel',
            click: function() {
              $(this).dialog('close');
            }
          }
        ]
    });

  // submit on return if valid
  registrationDialog.keyup(function(event) {
      if(event.keyCode == 13 && !$('#registration-button').is(':disabled'))
        $('#registration-button').click();
    });


  form.setupValidation(registrationDialog);
}

//..........................................................................
//------------------------------ createEntry -------------------------------

/**
 * Create a new entry.
 */
function createEntry()
{
  var entryDialog = $('<div id="create-entry"/>')
    .html('<label>ID<br>'
          + '<input type="text" name="id" validate="name" '
          + 'validateButton="#create-button" size="30" maxlength="30">'
          + '</label>'
          + '<p>')
    .dialog({
      title: 'Entry Creation',
      modal: true,
      resizable: false,
      width: 300,
      closeOnEscape: true,
      dialogClass: 'create-dialog dialog',
      buttons: [
          {
            id:  'create-button',
            text: 'Create',
            click: function() {
              $(this).dialog('close');
              util.link(event,
                        $('input[name="id"]', this)[0].value + '?create');
            }
          },
          {
            text: 'Cancel',
            click: function() {
              $(this).dialog('close');
            }
          }
      ]
    });

  // submit on return if valid
  entryDialog.keyup(function(event) {
      if(event.keyCode == 13 && !$('#create-button').is(':disabled'))
        $('#create-button').click();
    });

  form.setupValidation(entryDialog);
}

//..........................................................................
//------------------------------ removeEntry -------------------------------

/**
 * Remove the entry with the given id from the data store.
 *
 * @param       inID the id of the entry to remove
 *
 */
function removeEntry(inID)
{
  gui.alert('Not yet implemented');
}

//..........................................................................

//-------------------------------- doRegistration ---------------------------------

/**
  *
  * Try to register the a new user. If the registration is successful, the page is
  * reloaded, otherwise an error message will be shown.
  *
  * @param       inUsername the username
  *
  */
function doRegistration(inUsername)
{
  var result =
    util.ajax("/actions/registration", { username: inUsername });

  if(result != "")
    $("#registration-error").html(result);
  else
    // reload the page to show registration
    util.reload();
}

//..........................................................................

//----- admin --------------------------------------------------------------

var admin = {};

admin.addEvent = function(inText, inSeconds)
{
  window.console.log("add event", inText, inSeconds, $('admin-events'));
  $('#admin-events').append('<div class="event"><div class="date">' +
                            util.niceDate(inSeconds)
                            + '</div><div class="text">' +
                            inText + '</div></div>');
};

admin.addLog = function(inType, inText, inSeconds)
{
  window.console.log("add log", inType, inText, inSeconds, $('admin-logs'));
  $('#admin-logs').append('<div class="log ' + inType + '">' +
                          '<div class="date">' + util.niceDate(inSeconds) +
                          '</div><div class="text">' + inText + '</div>' +
                          '</div></div>');
};

admin.show = function(inType)
{
  window.console.log(inType, $('#admin-logs div.COMPLETE'),
                     $('#admin-logs div.' + inType));
  $('#admin-logs div.COMPLETE').hide();
  $('#admin-logs div.' + inType).show();
};

admin.resetIndexes = function(inType)
{
  if(!inType)
    return;

  util.ajax('/admin', { 'reset': inType }, null, true);
};

admin.clearCache = function()
{
  util.ajax('/admin', { 'cache': 'clear' }, null, true);
}

//..........................................................................


