/******************************************************************************
 * Copyright (c) 2002-2007 Peter 'Merlin' Balsiger and Fredy 'Mythos' Dobler
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

//------------------------------------------------------------------- header

/**
 * Script code for editing values.
 *
 * @file          edit.js
 *
 * @author        balsiger@ixitxachitls.net (Peter Balsiger)
 *
 */

//..........................................................................

/** The object to store everything in. */
var edit = new Object();

/** All the values currently edited. */
edit.all = [];

//----------------------------- makeEditable -------------------------------

/**
 * Make the 'this' element editable.
 */
edit.makeEditable = function()
{
  var target = edit.findTarget(this);

  if(!target.__edit)
    target.__edit = [];

  target.__edit.push(this);

  $(target).dblclick(edit.edit);
  $(target).bind('contextmenu', edit.edit);
}

//..........................................................................
//---------------------------------- edit ----------------------------------

/**
 * Edit this field.
 *
 * @param inEvent     the event that led to the edit
 * @param inElement   the dma editable element to edit
 * @param inNoRelated true when not editing related values
 *
 */
  edit.edit = function(inEvent, inElement, inNoRelated)
{
  var element = inElement || this;

  // clear a possible selection
  util.clearSelection();

  // remove the handlers
  $(element).unbind('dblclick', edit.edit);
  $(element).unbind('contextmenu', edit.edit);

  if(element.__edit)
    for(var i = 0; i < element.__edit.length; i++)
      edit.editValue(element.__edit[i], element, inNoRelated);

  gui.addAction('save', 'Save', edit.save);
  gui.addAction('cancel', 'Cancel', util.link);

  return false;
};

//..........................................................................
//------------------------------- editValue --------------------------------

/**
 * Edit the dma value.
 *
 * @param       inEditable     the field to edit
 * @param       inTarget       the target container
 * @param       inNoRelated    true when not editing related values
 *
 */
edit.editValue = function(inEditable, inTarget, inNoRelated)
{
  var editable = edit.Base.create(inEditable);

  $(inEditable).after(editable.getElement());
  $(inEditable).hide();
  inTarget.__edit = editable;

  if(!inNoRelated && editable.related)
  {
    var related = editable.related.split(/,\s*/);
    for(var i = 0; i < related.length; i++)
      edit.edit(undefined, $('dmaeditable[key=' + related[i] + ']')[0], true);
  }

  editable.focus();
};

//..........................................................................
//------------------------------- findTarget -------------------------------

/**
 * Find the target for editing.
 *
 * @param   inElement the dma editable field
 *
 * @return  the element to edit
 *
 */
edit.findTarget = function(inElement)
{
  var parent = $(inElement).parent('.value-content');
  if(parent.length)
    return parent[0];

  return inElement;
};

//..........................................................................
//-------------------------------- editAll ---------------------------------

/**
 * Edit all values on the page.
 */
edit.editAll = function()
{
  var targets = [];
  $('dmaeditable').each(function()
  {
    var target = edit.findTarget(this);
    if(!targets.contains(target))
    {
      edit.edit(null, target);
      targets.push(target);
    }
  });
};

//..........................................................................
//---------------------------------- save ----------------------------------

/**
 * Save all the current edits.
 *
 */
edit.save = function()
{
  var values = { };

  for(var i = 0, editable; editable = edit.all[i]; i++)
    editable.save(values);

  // send the data to the server
  window.console.log("saving", values);
  util.ajax('/actions/save', values,
            function(inResult) { eval(inResult); });

  // remove the move away code
  window.onbeforeunload = undefined;
};

//..........................................................................
//-------------------------------- unparsed --------------------------------

/** Show that some part of an input field could not be parsed.
 *
 * @param  inEntry the entry type of the error
 * @param  inID    the id of the entry with the error
 * @param  inKey   the name of the key that could not be parsed
 * @param  inText  the text that could not be parsed
 *
 */
edit.unparsed = function(inEntry, inID, inKey, inText)
{
  gui.delayed(function() {
      var container =
      $('dmaeditable[key=' + inKey+ '][entry=' + inEntry + '][id=' +
        inID + ']').parent();
      container.addClass('unparsed');
      container.append('<div class="unparsed-rest">' + inText + '</div>');
    }, 100);
}

//..........................................................................
//--------------------------------- refresh --------------------------------

/**
 * Redo all edit setup for a page (after a page refresh).
 *
 */
edit.refresh = function()
{
  edit.all = [];
  $('dmaeditable').each(edit.makeEditable);
  gui.removeAction('save');
  gui.removeAction('cancel');

  if(location.search == '?create')
  {
    edit.editAll();
    $(':input')[0].focus();
    window.scroll(0, 0);
  }
};

//..........................................................................

//--------------------------------------------------------------------- Base

/**
 * The base object for ediable values
 *
 * @param inEditable   the editable for this edit, if any
 * @param inProperties an object with all the properties
 *
 */
edit.Base = function(inEditable, inProperties)
{
  this.properties = inProperties;
  this.editable = inEditable;
  this.id = this.properties.id,
  this.entry = this.properties.entry;
  this.key = this.properties.key;
  this.type = this.properties.type;
  this.label = this.properties.label;
  this.note = this.properties.note;
  this.related = this.properties.related;
  this._value =
    (this.properties.value == '$undefined$' ? '' : this.properties.value);
  this._defined = this.properties.value != '$undefined$';

  this._element   = this._createElement();
  this._unlabeled = this._element;

  if(!this.properties.nosave)
    edit.all.push(this);
};

/**
 * Create an editable object for the given editable tag.
 *
 * @param  inElement the editable element to create from or an object with all
 *                   properties
 *
 * @return the edit object of the right type
 */
edit.Base.create = function(inElement)
{
  var properties;
  var element;
  if(inElement.type)
  {
    properties = inElement;
    element = null;
  }
  else
  {
    properties = edit.Base._parse(inElement);
    element = inElement;
    properties.buttons = true;
  }

  switch(properties.type)
  {
    case 'name':
      return new edit.Name(element, properties);

    case 'string':
      return new edit.String(element, properties);

    case 'selection':
      return new edit.Selection(element, properties);

    case 'formatted':
      return new edit.FormattedString(element, properties);

    case 'list':
      return new edit.List(element, properties);

    case 'multiple':
      return new edit.Multiple(element, properties);

    case 'autostring':
      return new edit.AutocompleteString(element, properties);

    case 'autoname':
      return new edit.AutocompleteName(element, properties);

    case 'autokey':
      return new edit.AutocompleteKey(element, properties);

    case 'date':
      return new edit.Date(element, properties);

    case 'isbn':
      properties.validate = 'isbn';
      return new edit.Name(element, properties);

    case 'isbn13':
      properties.validate = 'isbn13';
      return new edit.Name(element, properties);

    case 'pages':
      properties.validate = 'pages';
      return new edit.Name(element, properties);

    case 'number':
      properties.validate = 'number';
      return new edit.Name(element, properties);

    case 'price':
      properties.validate = 'price';
      return new edit.Name(element, properties);
  }

  window.alert('Could not find edit object for ' + properties.type);
};

/**
 * Get the element that renders the editable.
 *
 * @return the element rendered
 */
edit.Base.prototype.getElement = function()
{
  return this._element;
};

edit.Base.prototype.isDefined = function()
{
  return this._defined;
};

edit.Base.prototype.getValue = function()
{
  return this._getValue();
};

/**
 * Parse the properties of the dma editable.
 *
 * @param     inEditable the field with the properties
 *
 * @return    an object with all the properties extracted
 *
 */
edit.Base._parse = function(inEditable)
{
  var properties = {
    id: inEditable.getAttribute('id'),
    entry: inEditable.getAttribute('entry'),
    value: inEditable.getAttribute('value'),
    key: inEditable.getAttribute('key'),
    script: inEditable.getAttribute('script'),
    values: inEditable.getAttribute('vaues'),
    note: inEditable.getAttribute('note'),
    related: inEditable.getAttribute('related')
  };

  var type = edit.Base._parseType(inEditable.getAttribute('type'));
  properties.type = type.type;
  properties.subtype = type.subtype;
  properties.label = type.label;
  properties.options = type.options

  // extract values if any
  var values = inEditable.getAttribute('values');
  if(values)
    properties.values = values.split('||');

  return properties;
};

/**
 * Parse a type string.
 *
 * @param inType the type to parse
 * @param an object with the parsed values (type, subtype, label, options)
 *
 */
edit.Base._parseType = function(inType)
{
  // extract subtype, if any 'type#subtype'
  var types = inType.match(/^((?:\n|.)+?)\#((?:\n|.)+)$/);

  var properties = { type: inType };
  if(types)
  {
    properties.type    = types[1];
    properties.subtype = types[2];
  }

  // extract type options, if any 'type(option)type'
  types = properties.type.match(/(.*?)\(((?:\n|.)*)\)(.*)/);

  if(types)
  {
    properties.type    = types[1] + types[3];
    properties.options = types[2];
  }

  // extract label if any 'type[label]'
  types = properties.type.match(/(.*)\[(.*)\]/);

  if(types)
  {
    properties.type  = types[1];
    properties.label = types[2];
  }

  return properties;
};

/**
 * Save the editable and it's value to the given object
 *
 * @param inValues where to store the value(s)
 *
 */
edit.Base.prototype.save = function(inValues)
{
  var value;
  if(this.isDefined())
    value = this.getValue();
  else
    value = '$undefined$';

  inValues[this.entry + '::' + this.id + '::' + this.key] = value;
};

/**
 * Make the value undefined.
 */
edit.Base.prototype._undefine = function()
{
  this._defined = false;
};

/**
 * Make the value defined.
 */
edit.Base.prototype._define = function()
{
  this._defined = true;
};

/**
 * Cancel editing.
 */
edit.Base.prototype._cancel = function()
{
  $(this.editable).show();
  $(this._element).remove();

  // remove from the list of pending changes
  edit.all.remove(this);
  if(edit.all.length == 0)
    gui.removeAction('save');

  edit.findTarget(this.editable).__edit = null;
  $(this.editable).each(edit.makeEditable);
};


//..........................................................................
//-------------------------------------------------------------------- Field

/**
 * The base object for editable values with a single field.
 *
 * @param inEditable   the editable for this edit, if any
 * @param inProperties an object with all the properties
 *
 */
edit.Field = function(inEditable, inProperties)
{
  edit.Base.call(this, inEditable, inProperties);

  this.hasFocus = false;
  this.hasMouse = false;

  if(!this._field)
    this._field = this._unlabeled;

  this._field.addClass('edit ' + this.type).
    attr('value', this._value).
    attr('name', this.key).
    attr('id', 'field-' + this.key).
    data('editable', this);

  // add a container around the field for all the additional values
  this._element =
    $(this._element).wrap('<div class="edit-container"></div>').parent();

  // add a note, if necessary
  if(this.note)
    this._element.prepend('<div class="edit-note edit-dynamic">' + this.note
                          + '</div>');

  // add the button to undefine a value
  if(this.properties.buttons)
  {
    $('<div class="sprite edit-undefine edit-dynamic" title="Undefine">' +
      '</div>').
      prependTo(this._element).
      click(this._undefine.bind(this));

    $('<div class="sprite edit-cancel edit-dynamic" title="Cancel"></div>').
      prependTo(this._element).
      click(this._cancel.bind(this));

    this._element.mouseover(this._updateDecoration.bind(this, undefined, true));
    this._element.mouseout(this._updateDecoration.bind(this, undefined, false));
    this._field.focus(this._updateDecoration.bind(this, true, undefined));
    this._field.blur(this._updateDecoration.bind(this, false, undefined));
    this._field.keypress(this._define.bind(this));
    this._field.change(this._define.bind(this));
  }

  // add the label, if any
  if(this.label)
  {
    this._element.addClass('label-' + this.label);
    this._field.addClass('label-' + this.label);
    this._element.append('<div class="edit-label">' + this.label + '</div>');
  }

  // add field validation
  form.setupValidation(this._element);
};
extend(edit.Field, edit.Base);

/**
 * Update the sate of decoration elements for editing.
 *
 * @param inFocus true if the field has focus, false if not
 * @param inMouse true if the filed has mouse over, false if not
 */
edit.Field.prototype._updateDecoration = function(inFocus, inMouse) {
  if(inFocus != undefined)
    this._hasFocus = inFocus;

  if(inMouse != undefined)
    this._hasMouse = inMouse;

  if(this._hasMouse || this._hasFocus)
    this._element.find('.edit-dynamic').show();
  else
    this._element.find('.edit-dynamic').hide();
};

/**
  * Get the value entered for this editable.
  *
  * @return A string with the value for this editable.
  *
  */
edit.Field.prototype._getValue = function()
{
  return $(this._field).attr('value');
  this._field = "guru";
};

/**
 * Make the value undefined.
 */
edit.Field.prototype._undefine = function()
{
  edit.Field._super._undefine.call(this);

  $(this._field).attr('value', '');
  $(this._field).addClass('edit-undefined');
};

/**
 * Make the value defined.
 */
edit.Field.prototype._define = function()
{
  edit.Field._super._define.call(this);

  $(this._field).removeClass('edit-undefined');
};

/**
  * Set the width of the field.
  *
  * @param inWidth the new width to set
  *
  */
edit.Field.prototype.setWidth = function(inWidth)
{
  this._field.style.width = inWidth + '%';
};

/**
 * Set the focus to this field.
 *
 */
edit.Field.prototype.focus = function()
{
  this._field.focus();
};

//..........................................................................
//--------------------------------------------------------------------- Name

/**
 * An object representing an editable name field.
 *
 * @param inEditable   the editable for this edit, if any
 * @param inProperties an object with all the properties
 *
 */
edit.Name = function(inEditable, inProperties)
{
  inProperties.value = inProperties.value.removeNewlines();
  edit.Field.call(this, inEditable, inProperties);
};
extend(edit.Name, edit.Field);

/**
  * Create the element associated with this editable.
  *
  * @return the html element created
  */
edit.Name.prototype._createElement = function()
{
  return $('<input class="edit-field" validate="' +
           (this.properties.validate || 'name') + '"/>');
};

//..........................................................................
//------------------------------------------------------------------- String

/**
 * An object representing an editable string field.
 *
 * @param inEditable   the editable for this edit, if any
 * @param inProperties an object with all the properties
 *
 */
edit.String = function(inEditable, inProperties)
{
  inProperties.value =
    inProperties.value.replace(/^\s*\"([\s\S\n]*)\"\s*$/, "$1");
  edit.Field.call(this, inEditable, inProperties);
};
extend(edit.String, edit.Field);

/**
  * Create the element associated with this editable.
  *
  * @return the html element created
  */
edit.String.prototype._createElement = function()
{
  return $('<input class="edit-field" validate="' +
           (this.properties.validate || 'string') + '" size="30"/>');
};

/**
 * Get the value of the field.
 *
 * @return the fields value, ready for storing.
 */
edit.String.prototype._getValue = function()
{
  var value = edit.String._super._getValue.call(this);
  value = value.replace(/(["'])/g, '\\$1'); // '"
  return "\"" + value + "\"";
};

//..........................................................................
//---------------------------------------------------------- FormattedString

/**
 * An object representing an editable formatted string field.
 *
 * @param inEditable   the editable for this edit, if any
 * @param inProperties an object with all the properties
 *
 */
edit.FormattedString = function(inEditable, inProperties)
{
  inProperties.value = inProperties.value.replace(/[\r\n][\t ]+/g, '\n');
  edit.String.call(this, inEditable, inProperties);
};
extend(edit.FormattedString, edit.String);

/**
  * Create the element associated with this editable.
  *
  * @return the html element created
  */
edit.FormattedString.prototype._createElement = function()
{
  return $('<textarea class="edit-field" validate="string" rows="15" ' +
           'cols="80"/>');
};

//..........................................................................
//---------------------------------------------------------------- Selection

/**
 * An object representing an editable selection field.
 *
 * @param inEditable   the editable for this edit, if any
 * @param inProperties an object with all the properties
 *
 */
edit.Selection = function(inEditable, inProperties)
{
  // this is used in the constructor
  this._selections = inProperties.values;

  edit.Field.call(this, inEditable, inProperties);

  // selections are always defined when starting to edit, as per default always
  // something will be selected
  this._defined = true;
};
extend(edit.Selection, edit.Field);

/**
  * Create the element associated with this editable.
  *
  * @return the html element created
  */
edit.Selection.prototype._createElement = function()
{
  var element = $('<select class="edit-field" />');

  if(this._selections)
    for(var i = 0; i < this._selections.length; i++)
    {
      var parts = this._selections[i].split("::");

      var text  = parts[0];
      var value = parts[0];

      if(parts.length > 1)
        value = parts[1];

      var option = $('<option value="' + value + '">' + text + '</option>');

      element.append(option);

      if(value == this._value)
        element.attr('selectedIndex', i);
    }

  return element;
};

//..........................................................................
//--------------------------------------------------------------------- List

/**
 * An object representing an editable list field.
 *
 * @param inEditable   the editable for this edit, if any
 * @param inProperties an object with all the properties
 *
 */
edit.List = function(inEditable, inProperties)
{
  this.delimiter = inProperties.options.trim();
  this.subtype = inProperties.subtype;
  this._initValues = inProperties.value.split(this.delimiter);
  this._entries = [];

  // fix strings that were split in the middle because the delimiter appears in
  // the middle of a string
  for(var i = 0; this._initValues.length > 1 && i < this._initValues.length - 1;
      i++)
        if(this._initValues[i].replace(/[^\"]+/g, "").length % 2)
        {
          this._initValues[i] += this.delimiter + this._initValues[i + 1];
          this._initValues.splice(i + 1, 1);
          i--; // do this again
        }

  // trim values
  for(var i = 0; i < this._initValues.length; i++)
    this._initValues[i] = this._initValues[i].trim();

  // ignore lists with only a single, empty element
  if(this._initValues.length == 1 && this._initValues[0].length == 0)
    this._initValues = [];

  // have to setup init values first
  edit.Field.call(this, inEditable, inProperties);
};
extend(edit.List, edit.Field);

/**
  * Create the element associated with this editable.
  *
  * @return the html element created
  */
edit.List.prototype._createElement = function()
{
  var element = $('<div class="edit-list"></div>');

  if(this._initValues.length > 0)
    for(var i = 0; i < this._initValues.length; i++)
      element.append(this._createLine(this._initValues[i]));
  else
    element.append(this._createLine(""));

  return element;
};

/**
  * Create a single line in the list (with images).
  *
  * @param  inValue    the value to set to
  * @param  inPrevious the editable to add after, if any
  *
  * @return the sub editable created
  *
  */
edit.List.prototype._createLine = function(inValue, inPrevious)
{
  var line = $('<div class="edit-list-element" />');

  var type = edit.Base._parseType(this.subtype);
  var entry = edit.Base.create({
    id: this.id,
    entry: this.entry,
    type: type.type,
    value: inValue,
    key: this.key,
    script: null,
    values: this.properties.values,
    note: null,
    label: type.label,
    related: null,
    subtype: type.subtype,
    options: type.options,
    nobuttons: true,
    nosave: true
    });

  entry._line = line;

  if(inPrevious)
  {
    var i;
    for(i = 0; i < this._entries.length; i++)
      if(this._entries[i] == inPrevious)
      {
        this._entries.splice(i + 1, 0, entry);

        break;
      }

    // not found, add it to the end
    if(i >= this._entries.length)
      this._entries.push(entry);
  }
  else
    this._entries.push(entry);

  line.append(entry._element);

  // add the buttons
  $('<div class="sprite edit-list-add" title="Add"></div>').
  appendTo(line).
  click(this._add.bind(this, entry))
  ;
  $('<div class="sprite edit-list-remove" title="Remove"></div>').
  appendTo(line).
  click(this._remove.bind(this, entry))
  ;

  return line;
};

/**
  * Remove the entry listed
  *
  * @param the entry to remove
  */
edit.List.prototype._remove = function(inEntry)
{
  inEntry._line.remove();
  this._entries.remove(inEntry);
};

/**
  * Add an empty line after the given entry.
  *
  * @param the entry to add after
  */
edit.List.prototype._add = function(inEntry)
{
  this._createLine("", inEntry).insertAfter(inEntry._line).
    find('input').focus();
};

/**
 * Get the value of the field.
 *
 * @return the fields value, ready for storing.
 */
edit.List.prototype._getValue = function()
{
  var result = [];

  for(var i = 0; i < this._entries.length; i++)
  {
    var value = this._entries[i].getValue();
    if(value)
      result.push(value);
  }

  return result.join(this.delimiter) || '$undefined$';
};

//..........................................................................
//----------------------------------------------------------------- Multiple

/**
 * An object representing an editable multiple field.
 *
 * @param inEditable   the editable for this edit, if any
 * @param inProperties an object with all the properties
 *
 */
edit.Multiple = function(inEditable, inProperties)
{
  this.items = [];
  this.subvalues = inProperties.value.split(/::/);
  this.subtypes = inProperties.subtype.split(/@/);
  this.delimiters =
    inProperties.options ? inProperties.options.split(/::/) : [];

  // have to setup init values first
  edit.Field.call(this, inEditable, inProperties);
};
extend(edit.Multiple, edit.Field);

/**
  * Create the element associated with this editable.
  *
  * @return the html element created
  */
edit.Multiple.prototype._createElement = function()
{
  var element = $('<span class="edit-list-multiple"></span>');

  for(var i = 0; i < this.subtypes.length; i++)
  {
    var type = edit.Base._parseType(this.subtypes[i]);
    var item = edit.Base.create({
      id: this.id,
      entry: this.entry,
      type: type.type,
      value: this._value.length > i ? this._value[i] : "",
      key: this.key,
      script: null,
      value: i < this.subvalues.length ? this.subvalues[i] : "",
      values: this.properties.values,
      note: null,
      label: type.label,
      related: null,
      subtype: type.subtype,
      options: type.options,
      nosave: true
      });

    this.items.push(item);
    element.append(item._element);
  }

  return element;
};

/**
 * Get the value of the field.
 *
 * @return the fields value, ready for storing.
 */
edit.Multiple.prototype._getValue = function()
{
  var result = "";

  for(var i = 0; i < this.items.length; i++)
  {
    var value = this.items[i].getValue();

    if(value.length > 0 && value != "$undefined$")
      result += this.delimiters[i] + value;
  }

  return result + this.delimiters[i];
};

//..........................................................................
//------------------------------------------------------- AutocompleteString

/**
 * An object representing an editable autocomplete string field.
 *
 * @param inEditable   the editable for this edit, if any
 * @param inProperties an object with all the properties
 *
 */
edit.AutocompleteString = function(inEditable, inProperties)
{
  var options = inProperties.options.split(/\|/);
  this.source = options[0];
  this.addition = options[1];
  edit.String.call(this, inEditable, inProperties);

  this._field.focus(edit.AutocompleteString._update.bind(this));
};
extend(edit.AutocompleteString, edit.String);

/**
 * Update the value on focus to make sure we have the proper autocomplete
 * installed (as some depend on the values of other fields).
 */
edit.AutocompleteString._update = function()
{
  var source = '/autocomplete/' + this.source;

  if(this.addition)
  {
    // Try to get an addition in the siblings first.
    var addition =
      this._element.siblings().find('input.label-' + this.addition);

    // How about a field with the given name
    if(addition.length == 0)
      addition = $('#field-' + this.addition);

    // Last but not least, take the editable itself
    if(addition.length == 0)
      addition = $('dmaeditable[key=' + this.addition + ']');

    if(addition.length > 0)
      source += '/' + addition.attr('value');
  }

  this._field.autocomplete({
    source: source,
    autoFocus: true,
    minLength: 0,
    delay: 100
  });
};

//..........................................................................
//--------------------------------------------------------- AutocompleteName

/**
 * An object representing an editable autocomplete name field.
 *
 * @param inEditable   the editable for this edit, if any
 * @param inProperties an object with all the properties
 *
 */
edit.AutocompleteName = function(inEditable, inProperties)
{
  var options = inProperties.options.split(/\|/);
  this.source = options[0];
  this.addition = options[1];
  edit.Name.call(this, inEditable, inProperties);

  this._field.focus(edit.AutocompleteString._update.bind(this));
};
extend(edit.AutocompleteName, edit.Name);

//..........................................................................
//---------------------------------------------------------- AutocompleteKey

/**
 * An object representing an editable autocomplete name field.
 *
 * @param inEditable   the editable for this edit, if any
 * @param inProperties an object with all the properties
 *
 */
edit.AutocompleteKey = function(inEditable, inProperties)
{
  inProperties.validate = 'any';
  edit.AutocompleteName.call(this, inEditable, inProperties);

  this._field.focus(edit.AutocompleteString._update.bind(this));
};
extend(edit.AutocompleteKey, edit.AutocompleteName);

/**
  * Get the value entered for this editable.
  *
  * @return A string with the value for this editable.
  *
  */
edit.AutocompleteKey.prototype._getValue = function()
{
  var value = edit.AutocompleteKey._super._getValue.call(this);
  var match = value.match(/\((.*)\)/);
  if(!match || match.length < 2)
    return value;

  return match[1];
};

//..........................................................................
//--------------------------------------------------------------------- Date

/**
 * An object representing an editable date field.
 *
 * @param inEditable   the editable for this edit, if any
 * @param inProperties an object with all the properties
 *
 */
edit.Date = function(inEditable, inProperties)
{
  edit.Field.call(this, inEditable, inProperties);
};
extend(edit.Date, edit.Field);

/**
  * Create the element associated with this editable.
  *
  * @return the html element created
  */
edit.Date.prototype._createElement = function()
{
  var values = this.properties.value.split(/ /);

  var monthProperties =
  {
    id: this.id,
    entry: this.entry,
    type: 'selection',
    value: values.length > 1 ? values[0] : '',
    key: this.key,
    script: null,
    values: ['', 'January', 'February', 'March', 'April', 'May', 'June',
             'July', 'August', 'September', 'October', 'November',
             'December'],
    note: null,
    label: null,
    related: null,
    subtype: null,
    options: null,
    nobuttons: true,
    nosave: true
  };

  var years = [];
  for (var i = 1970; i < new Date().getFullYear() + 2; i++)
    years.push('' + i);

  var yearProperties =
  {
    id: this.id,
    entry: this.entry,
    type: 'selection',
    value: values.length > 1 ? values[1] : values[0],
    key: this.key,
    script: null,
    values: years,
    note: null,
    label: null,
    related: null,
    subtype: null,
    options: null,
    nobuttons: true,
    nosave: true
  };

  this.month = new edit.Selection(this.editable, monthProperties);
  this.year = new edit.Selection(this.editable, yearProperties);

  return $('<span />').append(this.month._element).append(this.year._element);
};

/**
 * Get the value of the field.
 *
 * @return the fields value, ready for storing.
 */
edit.Date.prototype._getValue = function()
{
  return this.month._getValue() + ' ' + this.year._getValue();
};

//..........................................................................

// make the form fields editable
$(document).ready(edit.refresh);