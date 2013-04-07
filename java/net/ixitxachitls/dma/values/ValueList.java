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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

import com.google.common.base.Joiner;

import net.ixitxachitls.input.ParseReader;
import net.ixitxachitls.util.EmptyIterator;
import net.ixitxachitls.util.configuration.Config;

//..........................................................................

//------------------------------------------------------------------- header

/**
 * This class stores a list of other values and is capable to read the values
 * from a reader (and write it to a writer of course).
 *
 * @file          ValueList.java
 *
 * @author        balsiger@ixitxachitls.net (Peter 'Merlin' Balsiger)
 *
 * @param         <T> the type of elements in the list
 *
 */

//..........................................................................

//__________________________________________________________________________

@Immutable
@ParametersAreNonnullByDefault
public class ValueList<T extends Value<T>>
  extends Value<ValueList<T>> implements Iterable<T>
{
  //--------------------------------------------------------- constructor(s)

  //------------------------------- ValueList -------------------------------

  /**
   * Construct the list undefined. If the given value is defined, it is used
   * as the lists only value.
   *
   * @param       inType an example value setting what kind of values
   *                     are to be stored in the list
   *
   */
  public ValueList(T inType)
  {
    this(inType, s_delimiter);
  }

  //........................................................................
  //------------------------------- ValueList -------------------------------

  /**
   * Construct the list using a type for the stored elements and a delimiter.
   *
   * @param       inType an example value setting what kind of values
   *                     are to be stored in the list (this could be done
   *                     with generics afterwards ?)
   * @param       inDelimiter the delimiter used between the elements
   *
   */
  public ValueList(T inType, String inDelimiter)
  {
    m_type      = inType;
    m_delimiter = inDelimiter;
    m_editType  = "#(list#|" + inDelimiter + "#|" + m_type.getEditType() + "#)";
    m_joiner    = Joiner.on(inDelimiter);

    append(inType);
    withTemplate("list", m_delimiter);
  }

  //........................................................................
  //------------------------------- ValueList -------------------------------

  /**
   * Construct the list with the given values.
   *
   * @param       inValues the beginning values of the list
   *
   */
  public ValueList(java.util.List<? extends T> inValues)
  {
    this(inValues, s_delimiter);
  }

  //........................................................................
  //------------------------------- ValueList -------------------------------

  /**
   * Construct the list with the given values.
   *
   * @param       inValues the beginning values of the list
   * @param       inDelimiter the delimiter used between the elements
   *
   */
  public ValueList(java.util.List<? extends T> inValues, String inDelimiter)
  {
    if(inValues.size() <= 0)
      throw new IllegalArgumentException("at least one value must be given");

    m_type      = inValues.get(0);
    m_delimiter = inDelimiter;
    m_editType  = "#(list#|" + inDelimiter + "#|" + m_type.getEditType() + "#)";
    m_joiner    = Joiner.on(inDelimiter);

    // add all the elements (they must be copied!)
    for(T value : inValues)
      append(value);

    withTemplate("list", m_delimiter);
  }

  //........................................................................
  //------------------------------- ValueList -------------------------------

  /**
   * Construct the list with the given values.
   *
   * @param       inValues the beginning values of the list
   *
   */
  @SuppressWarnings("unchecked")
  public ValueList(T ... inValues)
  {
    this(s_delimiter, inValues);
  }

  //........................................................................
  //------------------------------- ValueList -------------------------------

  /**
   * Construct the list with the given values.
   *
   * @param       inValues the beginning values of the list
   * @param       inDelimiter the delimiter used between the elements
   *
   * @undefined   IllegalArgumentException if not types are given
   *
   */
  @SuppressWarnings("unchecked") // heap pollution?
  public ValueList(String inDelimiter, T ... inValues)
  {
    if(inValues.length <= 0)
      throw new IllegalArgumentException("at least one value must be given");

    m_type      = inValues[0];
    m_delimiter = inDelimiter;
    m_editType  = "#(list#|" + inDelimiter + "#|" + m_type.getEditType() + "#)";
    m_joiner    = Joiner.on(inDelimiter);

    // add all the elements (they must be copied!)
    for(int i = 0; i < inValues.length; i++)
      append(inValues[i]);

    withTemplate("list", m_delimiter);
  }

  //........................................................................

  //------------------------------- create --------------------------------

  /**
   * Create a new list with the same type information as this one, but one
   * that is still undefined.
   *
   * @return      a similar list, but without any contents
   *
   */
  @Override
  @SuppressWarnings("unchecked") // casting result
  public ValueList<T> create()
  {
    // the value is added to the list if it is defined !
    ValueList<T> result;
    if(!m_type.isDefined())
      result = super.create(new ValueList<T>(m_type, m_delimiter));
    else
      result = super.create(new ValueList<T>(m_delimiter, m_type.create()));

    return result;
  }

  //........................................................................

  //........................................................................

  //-------------------------------------------------------------- variables

  /** The place holder type value denoting what kind of values are to be
   * stored. */
  protected T m_type;

  /** The values themselves. */
  protected @Nullable ArrayList<T> m_values = null;

  /** The delimiter to use between the elements. */
  protected String m_delimiter = s_delimiter;

  /** The joiner to join the elements. */
  protected Joiner m_joiner;

  /** The standard delimiter to use. */
  protected static final String s_delimiter =
    Config.get("resource:values/list.delimiter", ",\n");

  /** The joiner for editing values. */
  protected static final Joiner s_editJoiner =
    Joiner.on(Config.get("resource:entries/edit.multiple.delimiter", "#|"));

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

    for(Iterator<T> i = iterator(); i.hasNext(); )
      result.add(i.next().getEditValue());

    return "#(" + s_editJoiner.join(result) + "#)";
  }

  //........................................................................

  //---------------------------------- get ---------------------------------

  /**
   * Get a single value from all the values.
   *
   * @param       inIndex the number of the value to get
   *
   * @return      the requested value
   *
   */
  public @Nullable T get(int inIndex)
  {
    if(m_values == null || inIndex < 0 || inIndex > m_values.size())
      return null;

    return m_values.get(inIndex);
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
  @Override
  public Iterator<T> iterator()
  {
    if(m_values == null)
      return new EmptyIterator<T>();

    return m_values.iterator();
  }

  //........................................................................
  //--------------------------------- size ---------------------------------

  /**
   * Get the size of the list.
   *
   * @return      the number of list elements
   *
   */
  public int size()
  {
    if(m_values == null)
      return 0;

    return m_values.size();
  }

  //........................................................................
  //------------------------------ newElement ------------------------------

  /**
   * Get a new entry for the list with the same type as current entries.
   *
   * @return      the newly created element, not yet in the list
   *
   * @undefined   never
   *
   */
  @SuppressWarnings("unchecked") // casting
  public T newElement()
  {
    return m_type.create();
  }

  //........................................................................

  //------------------------------ doToString ------------------------------

  /**
   * Convert the value to a string, depending on the given kind.
   *
   * @return      a String representation, depending on the kind given
   *
   */
  @Override
  protected String doToString()
  {
    // we know this is defined
    return m_joiner.join(m_values);
  }

  //........................................................................

  //------------------------------ isDefined -------------------------------

  /**
   * Check if the value is defined or not.
   *
   * @return      true if the value is defined, false if not
   *
   */
  @Override
  public boolean isDefined()
  {
    return m_values != null;
  }

  //........................................................................
  //------------------------------ getChoices ------------------------------

  /**
   * Get the all the possible values this value can be edited with.
   *
   * @return      the values to edit
   *
   */
  @Override
  public @Nullable String getChoices()
  {
    if(super.getChoices() == null)
      return m_type.getChoices();

    return super.getChoices();
  }

  //........................................................................
  //----------------------------- getDelimiter -----------------------------

  /**
   * Get the delimiter used between items.
   *
   * @return      the delimiter between items
   *
   */
  public String getDelimiter()
  {
    return m_delimiter;
  }

  //........................................................................
  //----------------------------- isArithmetic -----------------------------

  /**
   * Checks whether the value is arithmetic and thus can be computed with.
   *
   * @return      true if the value is arithemtic
   *
   */
  @Override
  public boolean isArithmetic()
  {
    return false;
  }

  //........................................................................

  //........................................................................

  //----------------------------------------------------------- manipulators

  //---------------------------------- as ----------------------------------

  /**
   * Create a new list similar to this one but with different values.
   *
   * @param     inValues the values to create a new list from
   *
   * @return    the newly created value list
   *
   */
  public ValueList<T> as(List<T> inValues)
  {
    ValueList<T> result = create();

    if(inValues.isEmpty())
      return result;

    result.define();
    result.m_values.addAll(inValues);

    return result;
  }

  //........................................................................
  //---------------------------------- as ----------------------------------

  /**
   * Create a new list similar to this one but with different values.
   *
   * @param     inValues the values to create a new list from
   *
   * @return    the newly created value list
   *
   */
  @SuppressWarnings("unchecked") // heap pollution?
  public ValueList<T> asAppended(T ... inValues)
  {
    if(inValues.length == 0)
      return this;

    ValueList<T> result = create();

    result.define();
    if(m_values != null)
      result.m_values.addAll(m_values);
    result.m_values.addAll(Arrays.asList(inValues));

    return result;
  }

  //........................................................................

  //------------------------------- append --------------------------------

  /**
   * Add a value to the list (only defined values are added!).
   *
   * @param       inValue the value to add (it is ignored if it is not
   *                      defined or null)
   *
   * @return      true if added, false else
   *
   */
  @Deprecated
  protected boolean append(T inValue)
  {
    if(!inValue.isDefined())
      return false;

    define();
    m_values.add(inValue);

    return true;
  }

  //........................................................................
  //-------------------------------- insert --------------------------------

  /**
   * Add a value to the list (only defined values are added!).
   *
   * @param       inIndex where to add the value in the list
   * @param       inValue the value to add (it is ignored if it is not
   *                      defined or null)
   *
   * @return      true if added, false else
   *
   */
  @Deprecated
  protected boolean insert(int inIndex, T inValue)
  {
    if(!inValue.isDefined())
      return false;

    define();
    m_values.add(inIndex, inValue);

    return true;
  }

  //........................................................................
  //-------------------------------- remove --------------------------------

  /**
   * Remove the given value from the list, if it is there.
   *
   * @param       inValue the value to remove
   *
   * @return      true if remove, false if not
   *
   */
  @Deprecated
  protected boolean remove(T inValue)
  {
    return m_values.remove(inValue);
  }

  //........................................................................
  //-------------------------------- define --------------------------------

  /**
   * Mark the list as being defined.
   *
   * @return      true if newly defined, false if it already was
   *
   */
  @Deprecated
  @SuppressWarnings("unchecked")
  protected boolean define()
  {
    if(m_values != null)
      return false;

    m_values = (ArrayList<T>)new ArrayList<Value<?>>();

    return true;
  }

  //........................................................................
  //------------------------------- complete -------------------------------

  /**
   * Complete this value from data from another value (usually a value from
   * the corresponding base entry).
   *
   * @param       inValue    the other value to use for completing this one
   * @param       inCampaign the campaign with all the data
   *
   * @undefined   never
   *
   */
//   @Deprecated // ??
//   public void complete(Value inValue)
//   {
//     if(inValue != null)
//       super.complete(inValue);
//     else
//       for(java.util.Iterator<Value> i = m_values.iterator(); i.hasNext(); )
//         i.next().complete(null);
//   }

  //........................................................................
  //-------------------------------- modify --------------------------------

  /**
   * Modify the value.
   *
   * @param       inModify the modifier to apply to this value
   *
   * @return      true if modified, false if not
   *
   */
//   @Deprecated // ??
//   public boolean modify(ValueGroup.Modifier inModify)
//   {
//     if(m_values == null)
//       return false;

//     boolean result = false;

//     for(Value element : m_values)
//     {
//       if(element instanceof Modifiable)
//         result |= ((Modifiable)element).modify(inModify);
//     }

//     return result;
//   }


  //........................................................................
  //--------------------------------- add ----------------------------------

  /**
   * Add the current and given value and return it. The current value is not
   * changed.
   *
   * @param       inValue the value to add to this one
   *
   * @return      the added values
   *
   */
  public ValueList<T> add(ValueList<T> inValue)
  {
    List<T> values = new ArrayList<T>();
    Set<String> added = new HashSet<String>();

    // only add the same value once
    for(T value : m_values)
      if(!added.contains(value.toString()))
      {
        added.add(value.toString());
        values.add(value);
      }

    for(T value : inValue.m_values)
      if(!added.contains(value.toString()))
      {
        added.add(value.toString());
        values.add(value);
      }

    return as(values);
  }

  //........................................................................

  //........................................................................

  //------------------------------------------------- other member functions

  //--------------------------------- add ----------------------------------

  /**
   * Add the given value to the end of the list.
   *
   * @param       inValue the value to add to this one
   *
   * @return      the result of the addition
   *
   */
  // public ValueList<T> add(ValueList<T> inValue)
  // {
  //   ValueList<T> result = this.create();

  //   for(T value : m_values)
  //     result.add(value);

  //   for(T value : inValue)
  //     result.add(value);

  //   return result;
  // }

  //........................................................................
  //------------------------------ subtract --------------------------------

  /**
   * Subtract the current value from the given one. This will remove all the
   * values in the given list from the current one.
   *
   * @param       inValue the value to subtract from
   *
   * @return      the result of the subtraction
   *
   */
  @Override
  @SuppressWarnings("unchecked") // comparing values of the raw type
  public ValueList<T> subtract(ValueList<T> inValue)
  {
    ValueList<T> result = create();

    for(T value : m_values)
      result.append(value);

    for(T value : inValue)
      result.remove(value);

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
  @Override
  @SuppressWarnings("unchecked")
  public boolean doRead(ParseReader inReader)
  {
    ParseReader.Position pos = inReader.getPosition();

    do
    {
      // create a new value
      T value = m_type.read(inReader);

      // read it
      if(value == null)
      {
        if(isDefined())
        {
          // go back before the last delimiter
          inReader.seek(pos);

          return true;
        }

        // nothing was read
        return false;
      }

      append(value);

      // read ok so far...
      pos = inReader.getPosition();

    } while(m_delimiter == null || inReader.expect(m_delimiter));

    return true;
  }

  //........................................................................

  //---------------------------- createElement -----------------------------

  /**
   * Create a new element value with the same type information as the values
   * already in the list, but one that is still undefined.
   *
   * @return      a similar value, but without any contents
   *
   */
  @SuppressWarnings("unchecked") // casting cloned value
  public T createElement()
  {
    return m_type.create();
  }

  //........................................................................

  //........................................................................

  //------------------------------------------------------------------- test

  /** The test.
   *
   * @hidden
   *
   */
  public static class Test extends net.ixitxachitls.util.test.TestCase
  {
    //----- init -----------------------------------------------------------

    /** Testing init. */
    @org.junit.Test
    public void init()
    {
      ValueList<Name> list = new ValueList<Name>(new Name());

      // undefined value
      assertEquals("not undefined at start", false, list.isDefined());
      assertEquals("undefined value not correct", "$undefined$",
                   list.toString());
      assertEquals("size", 0, list.size());
      assertNull("choices", list.getChoices());

      assertTrue("define", list.define());
      assertTrue("defined", list.isDefined());
      assertEquals("define", "", list.toString());
      assertFalse("define 2", list.define());

      list = new ValueList<Name>
        (new Name("Hello"), new Name("how are"), new Name("you"));

      // defined value
      assertEquals("not defined at start", true, list.isDefined());
      assertEquals("defined value not correct", "Hello,\nhow are,\nyou",
                   list.toString());
//       assertEquals("defined value not correct",
//                    "\"Hello\",\n\"how are\",\n\"you\"", list.toStore());
      assertEquals("size", 3, list.size());

      assertEquals("1", "Hello",   list.get(0).toString());
      assertEquals("2", "how are", list.get(1).toString());
      assertEquals("3", "you",     list.get(2).toString());

      ArrayList<Name> input = new ArrayList<Name>();

      input.add(new Name("Hello"));
      input.add(new Name("how are"));
      input.add(new Name("you"));

      list = new ValueList<Name>(input);

      // defined value
      assertEquals("not defined at start", true, list.isDefined());
      assertEquals("defined value not correct", "Hello,\nhow are,\nyou",
                   list.toString());
      assertEquals("size", 3, list.size());

      assertEquals("new", "$undefined$", list.createElement().toString());

      Value.Test.createTest(list);
    }

    //......................................................................
    //----- read -----------------------------------------------------------

    /** Testing reading. */
    @org.junit.Test
    public void read()
    {
      String []tests =
        {
          "simple", "1, 2, 3, 4", "1,\n2,\n3,\n4", null,

          "whites",
          "first, second,     third,  \n\n\nfourth",
          "first,\nsecond,\nthird,\nfourth", null,

          "other", "first, second. third", "first,\nsecond", ". third",
          "other 2", "first, second =", "first,\nsecond", "=",
          "empty", "", null, null,
          "delimiter", "1, 2, 3, \"guru\"", "1,\n2,\n3", ", \"guru\"",
          "none", "= first, second", null, "= first, second",
        };

      Value.Test.readTest(tests, new ValueList<Name>(new Name()));
    }

    //......................................................................
    //----- delimiter ------------------------------------------------------

    /** Testing delimiter. */
    @org.junit.Test
    public void delimiter()
    {
      String text = "one:two   : three\n\n\n:four = ";

      ParseReader reader =
        new ParseReader(new java.io.StringReader(text),
                        "test");

      // normal read
      ValueList<Name> list = new ValueList<Name>(new Name(), ":");

      list = list.read(reader);
      assertTrue("list should have been read", list != null);
      assertEquals("converted list does not match",
                   "one:two:three:four", list.toString());

      assertEquals("expecting delimiter", true, reader.expect('='));
    }

    //......................................................................
    //----- set ------------------------------------------------------------

    /** Testing setting. */
    @org.junit.Test
    public void set()
    {
      ValueList<Name> list = new ValueList<Name>(new Name());

      assertFalse("not undefined at start", list.isDefined());
      assertEquals("undefined value not correct", "$undefined$",
                   list.toString());

      assertTrue(list.append(new Name("test 1")));
      assertTrue("not defined", list.isDefined());
      assertEquals("undefined value not correct", "test 1",
                   list.toString());

      assertTrue(list.append(new Name("test 2")));
      assertTrue("not defined", list.isDefined());
      assertEquals("undefined value not correct", "test 1,\ntest 2",
                   list.toString());

      assertTrue(list.insert(1, new Name("test 3")));
      assertTrue("not defined", list.isDefined());
      assertEquals("undefined value not correct", "test 1,\ntest 3,\ntest 2",
                   list.toString());

      assertFalse(list.insert(1, new Name()));
      assertTrue("not defined", list.isDefined());
      assertEquals("undefined value not correct", "test 1,\ntest 3,\ntest 2",
                   list.toString());

      // values with other types are added but with the stored type
      assertTrue(list.insert(1, new Name("guru")));
      assertTrue("not defined", list.isDefined());
      assertEquals("undefined value not correct",
                   "test 1,\nguru,\ntest 3,\ntest 2",
                   list.toString());
    }

    //......................................................................
    //----- remove ---------------------------------------------------------

    /** Test removing of elements. */
    @org.junit.Test
    public void remove()
    {
      ValueList<Name> list = new ValueList<Name>(new Name(), " - ");

      Name first = new Name("first");
      Name second = new Name("second");
      Name third = new Name("third");
      Name fourth = new Name("fourth");

      list.append(first);
      list.append(second);
      list.append(third);
      list.append(fourth);

      assertEquals("added", "first - second - third - fourth",
                    list.toString());

      assertTrue("remove", list.remove(third));

      assertEquals("removed", "first - second - fourth", list.toString());

      assertFalse("remove", list.remove(third));

      assertTrue("remove", list.remove(first));

      assertEquals("removed", "second - fourth", list.toString());

      assertTrue("remove", list.remove(second));

      assertEquals("removed", "fourth", list.toString());

      assertTrue("remove", list.remove(fourth));

      assertEquals("removed", "", list.toString());
      assertTrue("defined", list.isDefined());
    }

    //......................................................................
    //----- compute --------------------------------------------------------

    // /** The compute Test. */
    // @org.junit.Test
    // public void compute()
    // {
    //   ValueList<Name> list = new ValueList<Name>(" ",
    //                                              new Name("hello"),
    //                                              new Name("there,"),
    //                                              new Name("how"),
    //                                              new Name("are"),
    //                                              new Name("you?"));

    //   list = list.subtract(new ValueList<Name>(new Name("guru"),
    //                                            new Name("there,"),
    //                                            new Name("are"),
    //                                            new Name("you")));

    //   assertEquals("subtract", "hello how you?", list.toString());

    //   list = list.add(new ValueList<Name>(new Name("guru"),
    //                                       new Name("guru")));

    //   assertEquals("subtract", "hello how you? guru guru", list.toString());
    // }

    //......................................................................

    //----- no delimiter ---------------------------------------------------

    /** Test a list without delimiter. */
    // TODO: reenable
//     @org.junit.Test
//     public void noDelimiter()
//     {
//     String text = "item Dagger = value 10 gp.\n#---- test\n item Longsword."
//      + "item with contents Backpack = contents item Axe..\n#-----\n ;\nguru";

//       ParseReader reader =
//         new ParseReader(new java.io.StringReader(text), "test");

//       ValueList<EntryValue> list =
//         new ValueList<EntryValue>(new EntryValue(), null);

//       assertTrue("read", list.read(reader));
//       assertEquals("store",
//                    "item Dagger [Dagger] =\n"
//                    + "\n"
//                    + "  value             10 gp.\n"
//                    + "#---- test\n"
//                    + "\n"
//                    + "item Longsword [Longsword] =\n"
//                    + "\n"
//                    + ".\n"
//                    + "\n"
//                    + "item with contents Backpack [Backpack] =\n"
//                    + "\n"
//                    + "  contents          item Axe [Axe] =\n"
//                    + "                    .\n"
//                    + "                    .\n"
//                    + "#-----\n"
//                    + "\n",
//                    list.toStore());
//       assertEquals("next", " ;\nguru", reader.read("_"));

//       m_logger.addExpectedPattern("WARNING: base.not-found:.*"
//                                   + "(base name 'Dagger').*");
//       m_logger.addExpectedPattern("WARNING: base.not-found:.*"
//                                   + "(base name 'Longsword').*");
//       m_logger.addExpectedPattern("WARNING: base.not-found:.*"
//                                   + "(base name 'Backpack').*");
//       m_logger.addExpectedPattern("WARNING: base.not-found:.*"
//                                   + "(base name 'Axe').*");
//    }

    //......................................................................
  }

  //........................................................................
}

