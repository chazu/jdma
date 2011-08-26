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

//------------------------------------------------------------------ imports

package net.ixitxachitls.dma.values;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.google.common.base.Joiner;

import net.ixitxachitls.input.ParseReader;
import net.ixitxachitls.output.commands.Command;
import net.ixitxachitls.util.ArrayIterator;
import net.ixitxachitls.util.Strings;
import net.ixitxachitls.util.configuration.Config;

//..........................................................................

//------------------------------------------------------------------- header

/**
 * This class stores a multiple string and is capable of reading such strings
 * from a reader (and write it to a writer of course).
 *
 * @file          Multiple.java
 *
 * @author        balsiger@ixitxachitls.net (Peter 'Merlin' Balsiger)
 *
 */

//..........................................................................

//__________________________________________________________________________

@Immutable
public class Multiple extends Value<Multiple>
  implements Iterable<Multiple.Element>
{
  //----------------------------------------------------------------- nested

  //----- Element ----------------------------------------------------------

  /**
   * An element of the multiple value.
   *
   */
  @Immutable
  public static class Element
  {
    //------------------------------ Element -------------------------------

    /**
     * Create the multiple element.
     *
     * @param       inValue    the value of the element
     * @param       inOptional true if the element if optional, false if not
     *
     */
    public Element(@Nonnull Value inValue, boolean inOptional)
    {
      this(inValue, inOptional, null, null);
    }

    //......................................................................
    //------------------------------ Element -------------------------------

    /**
     * Create the multiple element.
     *
     * @param       inValue    the value of the element
     * @param       inOptional true if the element if optional, false if not
     * @param       inFront    the string to appear in front of the element
     * @param       inTail     the string required after the element
     *
     */
    public Element(@Nonnull Value inValue, boolean inOptional,
                   @Nullable String inFront, @Nullable String inTail)
    {
      m_value    = inValue;
      m_optional = inOptional;
      m_front    = inFront;
      m_tail     = inTail;
    }

    //......................................................................

    //------------------------------- create -------------------------------

    /**
     * Create a new element similar to the current one, but witout a value.
     *
     * @return      the newly created element
     *
     */
    public Element create()
    {
      return new Element(m_value.create(), m_optional, m_front, m_tail);
    }

    //......................................................................


    /** The value itself. */
    private @Nonnull Value m_value;

    /** The flag if the value is optional. */
    private boolean m_optional;

    /** The string required before the value (if any). */
    private @Nullable String m_front;

    /** The string required after the value (if any). */
    private @Nullable String m_tail;

    //-------------------------------- get ---------------------------------

    /**
     * Get the value stored in this element.
     *
     * @return      the current value
     *
     */
    public @Nonnull Value get()
    {
      return m_value;
    }

    //......................................................................
    //----------------------------- isOptional -----------------------------

    /**
     * Return whether this element is optional or not.
     *
     * @return      true if optional, false if not
     *
     */
    public boolean isOptional()
    {
      return m_optional;
    }

    //......................................................................
    //------------------------------ getFront ------------------------------

    /**
      * Get the string required in front of the element.
      *
      * @return      the string in front or null if none required
      *
      */
    public @Nullable String getFront()
    {
      return m_front;
    }

    //......................................................................
    //------------------------------ getTail -------------------------------

    /**
     * Get the string required after of the element.
     *
     * @return      the string after or null if none required
     *
     */
    public @Nullable String getTail()
    {
      return m_tail;
    }

    //......................................................................
    //------------------------------ toString ------------------------------

    /**
     * Convert the value to a string, depending on the given kind.
     *
     * @return      a String representation, depending on the kind given
     *
     */
    public @Nonnull String toString()
    {
      if(!m_value.isDefined() && isOptional())
        return "";

      StringBuilder result = new StringBuilder();
      if(m_front != null)
        result.append(m_front);

      result.append(m_value.toString());

      if(m_tail != null)
        result.append(m_tail);

      return result.toString();
    }

    //........................................................................
  }

  //........................................................................

  //........................................................................

  //--------------------------------------------------------- constructor(s)

  //------------------------------- Multiple -------------------------------

  /**
   * Construct the multiple object using the given values.
   *
   * @param       inElements  the elements of the value
   *
   */
  public Multiple(@Nonnull Element ... inElements)
  {
    m_elements = new Element[inElements.length];

    String delimiters = "";
    String subTypes   = null;

    for(int i = 0; i < inElements.length; i++)
    {
      if(inElements[i] == null)
        throw new IllegalArgumentException("element  " + i
                                           + " must not be null");

      m_elements[i] = inElements[i];

      delimiters += Strings.handleNull(m_elements[i].m_front) + "::"
          + Strings.handleNull(m_elements[i].m_tail);

      if(subTypes == null)
        subTypes = m_elements[i].m_value.getEditType();
      else
        subTypes += "@" + m_elements[i].m_value.getEditType();
    }

    m_editType = "multiple(" + delimiters + ")#" + subTypes;
  }

  //........................................................................

  //-------------------------------- create ---------------------------------

  /**
   * Create a new text with the same type information as this one, but one
   * that is still undefined.
   *
   * @return      a similar text, but without any contents
   *
   */
  public @Nonnull Multiple create()
  {
    Element []elements = new Element[m_elements.length];

    for(int i = 0; i < elements.length; i++)
      elements[i] = m_elements[i].create();

    return super.create(new Multiple(elements));
  }

  //........................................................................

  //........................................................................

  //-------------------------------------------------------------- variables

  /** The elements of the multiple. */
  protected @Nonnull Element []m_elements;

  /** The standard delimiter between multiple entries. */
  protected static final char s_delimiter =
    Config.get("resource:values/multiple.delimiter", ' ');

  /** The joiner for editing values. */
  protected static final @Nonnull Joiner s_editJoiner =
    Joiner.on(Config.get("resource:entries/edit.multiple.delimiter", "::"));


  //........................................................................

  //-------------------------------------------------------------- accessors

  //----------------------------- getEditValue -----------------------------

  /**
   * Convert the given value into a String for editing.
   *
   * @return      the object converted to a String
   *
   */
  @Override
  public String getEditValue()
  {
    List<String> result = new ArrayList<String>();

    for(Iterator<Multiple.Element> i = iterator(); i.hasNext(); )
      result.add(i.next().get().getEditValue());

    return s_editJoiner.join(result);
  }

  //........................................................................

  //--------------------------------- get ----------------------------------

  /**
   * Get a single value from all the values.
   *
   * @param       inIndex the number of the value to get
   *
   * @return      the requested value
   *
   */
  public @Nonnull Element getElement(int inIndex)
  {
    if(inIndex < 0 || inIndex > m_elements.length)
      throw new IllegalArgumentException("invalid index '" + inIndex
                                         + " to get element");

    return m_elements[inIndex];
  }

  //........................................................................
  //--------------------------------- get ----------------------------------

  /**
   * Get the value of the indexed element.
   *
   * @param       inIndex the index of the value to get
   *
   * @return      the value of the element
   *
   */
  public @Nonnull Value get(int inIndex)
  {
    return getElement(inIndex).get();
  }

  //........................................................................

  //------------------------------- iterator -------------------------------

  /**
   * Get an iterator for all the values (if values are accessed, their are
   * copied!).
   *
   * @return      the requested values
   *
   */
  public @Nullable Iterator<Element> iterator()
  {
    return new ArrayIterator<Element>(m_elements);
  }

  //........................................................................
  //--------------------------------- size ---------------------------------

  /**
   * Get the number of values stored in this value.
   *
   * @return      the number of values
   *
   */
  public int size()
  {
    return m_elements.length;
  }

  //........................................................................

  //------------------------------ isDefined -------------------------------

  /**
   * Check if the value is defined or not.
   *
   * @return      true if the value is defined, false if not
   *
   */
  public boolean isDefined()
  {
    for(int i = 0; i < m_elements.length; i++)
      if(!m_elements[i].isOptional() && !m_elements[i].m_value.isDefined())
        return false;

    return true;
  }

  //........................................................................

  //------------------------------ doToString ------------------------------

  /**
   * Convert the value to a string, depending on the given kind.
   *
   * @return      a String representation, depending on the kind given
   *
   */
  protected @Nonnull String doToString()
  {
    StringBuilder result = new StringBuilder();

    boolean delim = false;

    for(int i = 0; i < m_elements.length; i++)
    {
      String element = m_elements[i].toString();

      if(element.length() > 0)
      {
        if(delim)
        {
          if(m_elements[i].m_front == null)
            result.append(" ");

          delim = false;
        }

        result.append(element);

        if(m_elements[i].m_tail == null)
          delim = true;
      }
    }

    return result.toString();
  }

  //........................................................................
  //------------------------------- doFormat -------------------------------

  /**
   * Format the value for printing.
   *
   * @return      the command that can be printed
   *
   */
  protected @Nonnull Command doFormat()
  {
    ArrayList<Object> commands = new ArrayList<Object>();

    for(int i = 0; i < m_elements.length; i++)
    {
      if(!m_elements[i].isOptional() || m_elements[i].get().isDefined())
        if(m_elements[i].m_front != null)
          commands.add(m_elements[i].m_front);
        else
          if(i > 0)
            commands.add(" ");

      commands.add(get(i).format(m_elements[i].isOptional()));

      if(!m_elements[i].isOptional() || m_elements[i].get().isDefined())
        if(m_elements[i].m_tail != null)
          commands.add(m_elements[i].m_tail);
    }

    return new Command(commands);
  }

  //........................................................................
  //------------------------------ getChoices ------------------------------

  /**
   * Get the all the possible values this value can be edited with. Returns
   * null if no preselection is available.
   *
   * @return      the possible value to select from or null for no selection
   *
   */
  public @Nonnull String getChoices()
  {
    ArrayList<String> list = new ArrayList<String>();

    for(Iterator<Element> i = iterator(); i.hasNext(); )
    {
      String values = i.next().get().getChoices();

      if(values != null)
        list.add(values);
    }

    if(list.size() > 0)
      return Strings.toString(list, "::", "");

    return super.getChoices();
  }

  //........................................................................

  //........................................................................

  //----------------------------------------------------------- manipulators

  //---------------------------------- as ----------------------------------

  /**
   * Set the values of the multiple.
   *
   * @param       inValues the values to set the multiple to
   *
   * @return      true if set, false if values not valid
   *
   */
  public @Nonnull Multiple as(@Nonnull Value ... inValues)
  {
    Multiple result = create();

    // compare length
    if(inValues.length != m_elements.length)
      return result;

    // check if all necessary values are present
    for(int i = 0; i < m_elements.length; i++)
      if(inValues[i] == null && !m_elements[i].isOptional())
        return result;

    for(int i = 0; i < m_elements.length; i++)
      if(inValues[i] != null)
        result.m_elements[i].m_value = inValues[i];

    return result;
  }

  //........................................................................

  //------------------------------- doRead ---------------------------------

  /**
   * Read the value from the reader and replace the current one.
   *
   * @param       inReader the reader to read from
   *
   * @return      true if read, false if not
   *
   */
  protected boolean doRead(@Nonnull ParseReader inReader)
  {
    for(int i = 0; i < m_elements.length; i++)
    {
      boolean mandatory = !m_elements[i].isOptional();

      // read the front delimiter, if any
      if(m_elements[i].m_front != null)
        if(!inReader.expect(m_elements[i].m_front))
          if(mandatory)
            return false;
          else
            continue;
        else
          // we now need a value here
          mandatory = true;

      Value value = m_elements[i].m_value.read(inReader);

      if(value == null && mandatory)
        // value is required here
        return false;

      if(value == null)
        m_elements[i].m_value = m_elements[i].m_value.create();
      else
        m_elements[i].m_value = value;

      if(value != null && m_elements[i].m_tail != null)
        if(!inReader.expect(m_elements[i].m_tail))
          // we read a value thus the delimiter has to be there as well
          return false;
    }

    return true;
  }

  //........................................................................

  //--------------------------------- add ----------------------------------

  /**
   * Add the given value to the beginning of the current one.
   *
   * @param       inValue the value to add to this one
   *
   * @return      the additiona of the value
   *
   */
  @SuppressWarnings("unchecked")
  public @Nonnull Multiple add(@Nonnull Multiple inValue)
  {
    Multiple result = create();

    if(m_elements.length != inValue.m_elements.length)
      throw
        new IllegalArgumentException("must have the same number of elements");

    for(int i = 0; i < result.m_elements.length; i++)
      result.m_elements[i].m_value =
        m_elements[i].m_value.add(inValue.m_elements[i].m_value);

    return result;
  }

  //........................................................................

  //........................................................................

  //------------------------------------------------- other member functions
  //........................................................................

  //------------------------------------------------------------------- test

  /** Test test. */
  public static class Test extends Value.Test
  {
    //----- init -----------------------------------------------------------

    /** Testing init. */
    @org.junit.Test
    public void init()
    {
      Multiple multiple = new Multiple(new Element []
        { new Element(new Name(), false),
          new Element(new Name(), true),
          new Element(new Name(), false) });

      // undefined value
      assertEquals("not undefined at start", false, multiple.isDefined());
      assertEquals("undefined value not correct", "$undefined$",
                   multiple.toString());

      assertFalse("undefined", multiple.get(0).isDefined());
      assertFalse("undefined", multiple.get(1).isDefined());
      assertFalse("undefined", multiple.get(2).isDefined());
      assertEquals("format", "", multiple.format(true).toString());

      multiple =
        new Multiple(new Element(new Name(), true),
                     new Element(new Name("guru"), true, "front", "tail"),
                     new Element(new Name("hia"), false));

      // defined value
      assertTrue("should be defined", multiple.isDefined());
      assertEquals("undefined value not correct",
                   "frontgurutailhia", multiple.toString());

      assertEquals("front", "front", multiple.getElement(1).getFront());
      assertEquals("tail", "tail", multiple.getElement(1).getTail());
      assertEquals("size", 3, multiple.size());

      assertFalse("undefined", multiple.get(0).isDefined());
      assertTrue("undefined", multiple.get(1).isDefined());
      assertTrue("undefined", multiple.get(2).isDefined());

      assertEquals("format", "frontgurutail hia",
                   multiple.format(true).toString());
      assertNull("edit values", multiple.getChoices());

      // assign some edit values to elements
      multiple.get(0).withChoices("1");
      multiple.get(1).withChoices("2");
      multiple.get(2).withChoices("3");

      assertEquals("edit values", "1::2::3", multiple.getChoices());

      Value.Test.createTest(multiple);
    }

    //......................................................................
    //----- read -----------------------------------------------------------

    /** Testing reading. */
    @org.junit.Test
    public void read()
    {
      String []tests =
        {
          "complete",
          "\"hia\" how about some \"test\"",
          "\"hia\" how about some \"test\"", null,

          "whites",
          "\"hia\" \nhow      about some \n\"test\"    ",
          "\"hia\" how about some \"test\"", "    ",

          "optional", "\"some other\" \"text\"", "\"some other\" \"text\"",
          null,

          "missing", "some missing", null, "some missing",
          "missing end", "\"and finally some \" harder error", null,
          "\"and finally some \" harder error",

          "empty", "", null, null,
        };

      Value.Test.readTest(tests, new Multiple(new Element []
        { new Element(new Text(), false),
          new Element(new Name(), true),
          new Element(new Text(), false) }));
    }

    //......................................................................
    //----- delimiters -----------------------------------------------------

    /** Testing delimiters. */
    @org.junit.Test
    public void delimiters()
    {
      String []tests =
        {
          "complete",
          "42   from    123 to   99   end",
          "42 from 123 to 99 end", null,

          "whites",
          "42    \n  from \n   123 to   99  \n\n\n    end",
          "42 from 123 to 99 end", null,

          "optional 1", "42 to 99 end", "42 to 99 end", null,
          "optional 2", "42 from 120 ", "42 from 120", " ",

          "additional 1", "42 other 123 to 99 end", "42",
          " other 123 to 99 end",

          "additional 2", "42 from 123 other 99 end", "42 from 123",
          " other 99 end",

          "missing 1", "42 123", "42",  " 123",
          "missing 2", "42 from to", null, "42 from to",
          "missing 3", "42 from 123 99", "42 from 123", " 99",
          "missing 4", "42 to 99 guru", null, "42 to 99 guru",

          "missing", "some missing", null, "some missing",
          "empty", "", null, null,
        };

      Value.Test.readTest(tests, new Multiple
                          (new Element(new Number(0, 200), false),
                           new Element(new Number(0, 200), true,
                                       " from ", null),
                           new Element(new Number(0, 200), true,
                                       " to ", " end")));
  }

    //......................................................................
    //----- set ------------------------------------------------------------

    /** Testing setting. */
    @org.junit.Test
    public void as()
    {
      Multiple multiple = new Multiple(new Element(new Text(), false),
                                       new Element(new Name(), true),
                                       new Element(new Text(), false));

       assertEquals("not undefined at start", false, multiple.isDefined());
       assertEquals("undefined value not correct", "$undefined$",
                    multiple.toString());

       multiple = multiple.as(new Text("1"), new Name("2"), new Text("3"));
       assertEquals("set", "\"1\" 2 \"3\"", multiple.toString());

       multiple = multiple.as(new Text("1"), null, new Text("3"));
       assertEquals("optional", "\"1\" \"3\"", multiple.toString());

       multiple = multiple.as(null, null, new Text("3"));
       assertFalse("missing", multiple.isDefined());

       multiple = multiple.as(new Text("1"), null);
       assertFalse("not enough", multiple.isDefined());

       multiple = multiple.as(new Text("1"), null, null, null);
       assertFalse("too many", multiple.isDefined());
     }

    //......................................................................
    //----- add ------------------------------------------------------------

    /** The add Test. */
    @org.junit.Test
    public void add()
    {
      Multiple value1 =
        new Multiple(new Element(new Number(10, 0, 200), false),
                     new Element(new Number(20, 0, 200), false),
                     new Element(new Name("hello"), false));

      Multiple value2 =
        new Multiple(new Element(new Number(32, 0, 200), false),
                     new Element(new Number(3, 0, 200), false),
                     new Element(new Name("there"), false));

      assertEquals("add", "42 23 hello there", value1.add(value2).toString());
      assertEquals("add", "42 23 there hello", value2.add(value1).toString());
    }

    //......................................................................
  }

  //........................................................................
}