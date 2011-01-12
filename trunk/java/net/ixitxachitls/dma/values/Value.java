/******************************************************************************
 * Copyright (c) 2002,2003 Peter 'Merlin' Balsiger and Fredy 'Mythos' Dobler
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

import java.io.StringReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import net.ixitxachitls.dma.values.formatters.Formatter;
import net.ixitxachitls.input.ParseReader;
import net.ixitxachitls.output.commands.Color;
import net.ixitxachitls.output.commands.Command;
import net.ixitxachitls.util.Grouping;
import net.ixitxachitls.util.PublicCloneable;
import net.ixitxachitls.util.configuration.Config;

//..........................................................................

//------------------------------------------------------------------- header

/**
 * This is the base class for all DMA values.
 *
 * @file          Value.java
 *
 * @author        balsiger@ixitxachitls.net (Peter 'Merlin' Balsiger)
 *
 * @param         <T> the real type of the value, used for typ casting
 *
 */

//..........................................................................

//__________________________________________________________________________

public abstract class Value<T extends Value> implements
  Comparable<Value>, PublicCloneable
{
  //--------------------------------------------------------- constructor(s)

  //-------------------------------- Value ---------------------------------

  /**
   * This is an empty default constructor.
   *
   */
  protected Value()
  {
    // nothing to do
  }

  //........................................................................

  //------------------------------- create ---------------------------------

  /**
   * Create a new value with the same type information as this one, but one
   * that is still undefined.
   *
   * @return      a similar value, but without any contents
   *
   */
  public abstract @Nonnull T create();

  //........................................................................
  //------------------------------- create ---------------------------------

  /**
   * Initialize the given value with the same information as the current value.
   *
   * @param       inNew the newly created object that needs to be completed
   *
   */
//   @SuppressWarnings("unchecked")
//   public @Nonnull T create(@Nonnull T inNew)
//   {
//     inNew.m_formatter  = m_formatter;
//     inNew.m_grouping   = m_grouping;
//     inNew.m_editType   = m_editType;
//     inNew.m_editValues = m_editValues;

//     return inNew;
//   }

  //........................................................................
  //-------------------------------- clone ---------------------------------

  /**
   * Make a copy of the value and return it. If necessary, this is a deep
   * copy, although type relevant values are never copied, because these
   * are immutable.
   *
   * @return      a copy of the current value
   *
   */
  @SuppressWarnings("unchecked")
  public @Nonnull T clone()
  {
    try
    {
      return (T)super.clone();
    }
    catch(CloneNotSupportedException e)
    {
      throw new UnsupportedOperationException("Strange, clone should be "
                                              + "supported: " + e);
    }
  }

  //........................................................................

  //........................................................................

  //-------------------------------------------------------------- variables

  /** The text to use for undefined values. */
  public static final @Nonnull String UNDEFINED =
    Config.get("resource:values/undefined", "$undefined$");

  /** The hint for this value. */
  protected @Nullable Remark m_remark = null;

  /** The formatter for the value. */
  protected @Nullable Formatter<T> m_formatter = null;

  /** A grouping of the values, if any. */
  protected @Nullable Grouping<T, Object> m_grouping = null;

  /** The type to use for editing. */
  protected @Nonnull String m_editType = "";

  /** The values for editing the type. */
  protected @Nullable String m_editValues = null;

  //........................................................................

  //-------------------------------------------------------------- accessors

  //------------------------------- toString -------------------------------

  /**
   * Convert the value into a String that can be stored and reread.
   *
   * @return      a String representation for human reading
   *
   * @undefined   never
   *
   */
  public @Nonnull String toString()
  {
    if(!isDefined())
      return UNDEFINED;

    return (m_remark == null ? "" : m_remark.toString()) + doToString();
  }

  //........................................................................
  //--------------------------- toStringInternal ---------------------------

  /**
   * Return a string representation of the value. The value can be assumed to
   * be defined when this is called. This method should not be called directly,
   * instead call toString().
   *
   * @return      a string representation.
   *
   */
  public abstract @Nonnull String doToString();

  //........................................................................

  //------------------------------ isDefined -------------------------------

  /**
   * Check if the value is defined or not.
   *
   * @return      true if the value is defined, false if not
   *
   */
  public abstract boolean isDefined();

  //........................................................................

  //------------------------------ compareTo -------------------------------

  /**
   * Compare this value to another one.
   *
   * @param       inOther the value to compare to
   *
   * @return      -1 for less than, 0 for equal and +1 for greater than the
   *              object given
   *
   */
  public int compareTo(@Nonnull Value inOther)
  {
    return toString().compareTo(inOther.toString());
  }

  //........................................................................
  //-------------------------------- equals --------------------------------

  /**
   * Check for equality of the given errors.
   *
   * @param       inOther the object to compare to
   *
   * @return      true if equal, false else
   *
   */
  public boolean equals(Object inOther)
  {
    if(this == inOther)
      return true;

    if(inOther == null)
      return false;

    if(inOther instanceof Value)
      return toString().equals(inOther.toString());

    return false;
  }

  //........................................................................
  //------------------------------- hashCode -------------------------------

  /**
   * Compute the hash code for this class.
   *
   * @return      the hash code
   *
   */
  public int hashCode()
  {
    return toString().hashCode();
  }

  //........................................................................

  //-------------------------------- format --------------------------------

  /**
   * Format the value for printing.
   *
   * @param       inIgnoreUndefined true if undefined values should be returned
   *                                as empty
   *
   * @return      the command that can be printed
   *
   */
  public @Nonnull Command format(boolean inIgnoreUndefined)
  {
    return format(inIgnoreUndefined, false);
  }

  //........................................................................
  //-------------------------------- format --------------------------------

  /**
   * Format the value for printing.
   *
   * @param       inIgnoreUndefined true if undefined values should be returned
   *                                as empty
   * @param       inIgnoreFormatter true if set formattter should be ignored
   *
   * @return      the command that can be printed
   *
   */
  @SuppressWarnings("unchecked")
  public @Nonnull Command format(boolean inIgnoreUndefined,
                                 boolean inIgnoreFormatter)
  {
    Command command = null;

    if(!isDefined())
      if(inIgnoreUndefined)
        command = new Command(new Object[0]);
      else
        command = new Color("error", UNDEFINED);
    else
      if(!inIgnoreFormatter && m_formatter != null)
        command = m_formatter.format((T)this);
      else
        command = doFormat();

    if(m_remark != null)
      command = m_remark.format(command);

    return command;
  }

  //........................................................................
  //------------------------------- doFormat -------------------------------

  /**
   * Really to the formatting.
   *
   * @return      the command for setting the value
   *
   */
  protected abstract @Nonnull Command doFormat();

  //........................................................................

  //-------------------------------- group ---------------------------------

  /**
   * Return the group this value belongs to. As default we assume that each
   * distinct value has its own group. If other partitions are desired, they
   * have to be implemented in derivations or by using a grouping.
   *
   * @return      a string denoting the group this value is in
   *
   */
  @SuppressWarnings("unchecked")
  public @Nonnull Object group()
  {
    if(m_grouping != null)
      return m_grouping.group((T)this);

    return doGroup();
  }

  //........................................................................
  //------------------------------- doGroup --------------------------------

  /**
   * Really do grouping for this object. This method can be derived to have
   * special grouping in derivations.
   *
   * @return      a string denoting the group this value is in
   *
   */
  public @Nonnull Object doGroup()
  {
    return this;
  }

  //........................................................................

  //----------------------------- getEditType ------------------------------

  /**
   * Get the type used for editing.
   *
   * @return      a string representing the type to use for editing
   *
   */
  public @Nonnull String getEditType()
  {
    return m_editType;
  }

  //........................................................................
  //---------------------------- getEditValues -----------------------------

  /**
   * Get the all the possible values this value can be edited with. Returns
   * null if no preselection is available.
   *
   * TODO: this could probably be done more generic by returning the edit type
   * and values at the same time.
   *
   * @return      the possible value to select from or null for no selection
   *
   */
  public @Nullable String getEditValues()
  {
    return m_editValues;
  }

  //........................................................................

  //........................................................................

  //----------------------------------------------------------- manipulators

  //---------------------------- setFromString -----------------------------

  /**
   * Set the contents of the value from the given String. The part of the
   * String that could not be read is returned.
   *
   * @param       inText   the text to parse to get the value
   *
   * @return      the part not parsed or the empty string if everything could
   *              be parsed (if the String is the same as the one read, the
   *              value was not changed)
   *
   */
  public @Nonnull String setFromString(@Nonnull String inText)
  {
    if(inText.isEmpty())
      return inText;

    if(inText.startsWith(UNDEFINED))
    {
      reset();

      return inText.substring(UNDEFINED.length());
    }

    // init the reader
    StringReader string = new StringReader(inText);
    ParseReader reader  = new ParseReader(string, "set");

    // could we read the value at all?
    if(!read(reader))
      return inText;

    // determine the part that could not be read
    return reader.read(inText.length());
  }

  //........................................................................

  //--------------------------------- add ----------------------------------

  /**
   * Add the given value to the beginning of the current one.
   *
   * @param       inValue the value to add to this one
   *
   */
//   public @Nonnull T add(@Nonnull T inValue)
//   {
//   throw new UnsupportedOperationException("cannot add to this " + getClass());
//   }

  //........................................................................
  //------------------------------ subtract --------------------------------

  /**
   * Subtract the current value from the given one.
   *
   * @param       inValue the value to subtract from
   *
   */
//   public @Nonnull T subtract(@Nonnull T inValue)
//   {
//     throw new UnsupportedOperationException("cannot subtract fromt this "
//                                             + getClass());
//   }

  //........................................................................
  //------------------------------- multiply -------------------------------

  /**
   * Set this value to the multiple of the given one.
   *
   * @param       inValue      the value to multiply
   * @param       inMultiplier the multiplication factor
   *
   */
//   public T multiply(T inValue, Value inMultiplier)
//   {
//     throw new UnsupportedOperationException("cannot multiply to this "
//                                             + getClass());
//   }

  //........................................................................
  //----------------------------- setToDivide ------------------------------

  /**
   * Set this value to the divided of the given one.
   *
   * @param       inValue   the value to multiply
   * @param       inDivisor the divisor factor
   *
   * @undefined   IllegalArgumentException if given value is null
   * @undefined   UnsupportedOperationException if operation not allowed
   *
   */
//   public void setToDivide(Value inValue, Value inDivisor)
//   {
//     throw new UnsupportedOperationException("cannot divide to this "
//                                             + getClass());
//   }

  //........................................................................
  //------------------------------- multiply -------------------------------

  /**
   * Multiply the value with the given value.
   *
   * @param       inValue the multiplication factor
   *
   * @return      true if multiplied, false if not
   *
   * @undefined   never
   *
   */
//   public boolean multiply(long inValue)
//   {
//   throw new UnsupportedOperationException("multiplication not supported in "
//                                             + getClass());
//   }

  //........................................................................
  //------------------------------- multiply -------------------------------

  /**
   * Multiply the value with the given value.
   *
   * @param       inValue the multiplication factor
   *
   * @return      true if multiplied, false if not
   *
   * @undefined   never
   *
   */
//   public boolean multiply(Rational inValue)
//   {
//   throw new UnsupportedOperationException("multiplication not supported in "
//                                             + getClass());
//   }

  //........................................................................

  //-------------------------------- reset ---------------------------------

  /**
   * Reset the value to undefined.
   *
   */
  @OverridingMethodsMustInvokeSuper
  public void reset()
  {
    m_remark = null;
  }

  //........................................................................
  //--------------------------------- read ---------------------------------

  /**
   * Try to read the value from the given stream.
   *
   * This is just a convenience method without the need to give an initializer
   * (which will not be read with this method).
   *
   * @param       inReader   the reader to read from
   *
   * @return      true if a valid value was read, false else
   *
   */
  // TODO: do we still need this, or the one with remarks?
//   public boolean read(ParseReader inReader)
//   {
//     return read(inReader, true);
//   }

  //........................................................................
  //--------------------------------- read ---------------------------------

  /**
   * Try to read the value from the given stream.
   *
   * @param       inReader       the reader to read from
   *
   * @return      true if a valid value was read, false else
   *
   */
  public boolean read(@Nonnull ParseReader inReader)
  {
    // reset the current value
    reset();

    // check if undefined is encountered
    if(inReader.expect(UNDEFINED))
      return true;

    // store the current position
    ParseReader.Position pos = inReader.getPosition();

    m_remark = Remark.read(inReader);

    // could we read the value?
    if(!doRead(inReader))
    {
      // restore position
      inReader.seek(pos);

      // make sure the value is undefined again
      reset();

      return false;
    }

    return true;
  }

  //........................................................................
  //-------------------------------- doRead --------------------------------

  /**
   * This is the derivable method that does the real reading.
   *
   * This method does not have to care about undefined values or about
   * resetting the current value or even handling the position for next reads
   * in case of error.
   *
   * @param       inReader the reader to read from
   *
   * @return      true if a valid value was read, false else
   *
   */
  protected abstract boolean doRead(@Nonnull ParseReader inReader);

  //........................................................................

  //---------------------------- withFormatter -----------------------------

  /**
   * Set a formatter for this value.
   *
   * @param       inFormatter to formatter to use when printing the value
   *
   * @return      the value itself (to allow new T().setFormatter())
   *
   */
  @SuppressWarnings("unchecked")
  public @Nonnull T withFormatter(@Nonnull Formatter inFormatter)
  {
    m_formatter = inFormatter;

    return (T)this;
  }

  //........................................................................
  //----------------------------- withGrouping -----------------------------

  /**
   * Set a grouping for this value.
   *
   * @param       inGrouping to grouping to use when printing the value
   *
   * @return      the value itself (to allow new T().setFormatter())
   *
   */
  @SuppressWarnings("unchecked")
  public @Nonnull T withGrouping(@Nonnull Grouping<T, Object> inGrouping)
  {
    m_grouping = inGrouping;

    return (T)this;
  }

  //........................................................................
  //----------------------------- withEditType -----------------------------

  /**
   * Set a edit type for this value.
   *
   * @param       inType the type to set to
   *
   * @return      the value itself (to allow new T().setFormatter())
   *
   */
  @SuppressWarnings("unchecked")
  public @Nonnull T withEditType(@Nonnull String inType)
  {
    if(inType.length() == 0)
      throw new IllegalArgumentException("must have a type here");

    m_editType = inType;

    return (T)this;
  }

  //........................................................................
  //---------------------------- withEditValues ----------------------------

  /**
   * Set the edit values for this value.
   *
   * @param       inValues the edit values to use
   *
   * @return      the value itself (to allow new T().setFormatter())
   *
   */
  @SuppressWarnings("unchecked")
  public @Nonnull T withEditValues(@Nonnull String inValues)
  {
    if(inValues.length() == 0)
      throw new IllegalArgumentException("must have values here");

    m_editValues = inValues;

    return (T)this;
  }

  //........................................................................

  //........................................................................

  //------------------------------------------------- other member functions
  //........................................................................

  //------------------------------------------------------------------- test

  /** The Test. */
  public static class Test extends net.ixitxachitls.util.test.TestCase
  {
    //----- readTest -------------------------------------------------------

    /**
     * A simple test routine for testing reads.
     *
     * @param inTests the note, test and return value triplets
     * @param inValue the value to read into
     *
     */
    public static void readTest(@Nonnull String []inTests,
                                @Nonnull Value inValue)
    {
      if(inTests.length % 4 != 0)
        throw new IllegalArgumentException("quadruplets of input test strings "
                                           + "must be given");

      for(int i = 0; i < inTests.length; i += 4)
      {
        ParseReader reader =
          new ParseReader(new java.io.StringReader(inTests[i + 1]), "test");

        if(inTests[i + 2] == null)
          assertFalse(i / 4 + ": " + inTests[i]
                      + ", should not have been read",
                      inValue.read(reader));
        else
        {
          assertTrue(i / 4 + ": " + inTests[i] + ", should have been read",
                     inValue.read(reader));
          assertEquals(i / 4 + ": " + inTests[i] + ", does not match",
                       inTests[i + 2], inValue.toString());
        }

        String rest = reader.readLine();

        if(inTests[i + 3] != null)
          assertEquals(i / 4 + ": " + inTests[i] + ", rest does not match",
                       inTests[i + 3], rest);
        else
          assertTrue(i / 4 + ": " + inTests[i] + ", still having rest '"
                     + rest + "'", rest.length() == 0);
      }
    }

    //......................................................................
    //----- setTest --------------------------------------------------------

    /**
     * A simple test routine for testing sets.
     *
     * @param inTests the note, test and return value triplets
     * @param inValue the value to read into
     *
     */
    public static void setTest(@Nonnull String []inTests,
                               @Nonnull Value inValue)
    {
      if(inTests.length % 4 != 0)
        throw new IllegalArgumentException("quadruplets of input test strings "
                                           + "must be given");

      for(int i = 0; i < inTests.length; i += 4)
      {
        String rest = inValue.setFromString(inTests[i + 1]);

        if(inTests[i + 2] == null)
          assertTrue(i / 4 + ": " + inTests[i]
                     + ", should not have been set",
                     rest == inTests[i + 1]);
        else
          assertEquals(i / 4 + ": " + inTests[i] + ", does not match",
                       inTests[i + 2], inValue.toString());

        if(inTests[i + 3] != null)
          assertEquals(i / 4 + ": " + inTests[i] + ", rest does not match",
                       inTests[i + 3], rest);
        else
          assertTrue(i / 4 + ": " + inTests[i] + ", still having rest '"
                     + rest + "'", rest.length() == 0);
      }
    }

    //......................................................................
    //----- cloneCreateResetTest -------------------------------------------

    /**
     * A simple check to test clone(), create() and reset().
     *
     * @param inValue the value to test
     *
     */
    @SuppressWarnings("unchecked")
    public static void cloneCreateResetTest(@Nonnull Value inValue)
    {
      if(inValue == null)
        throw new IllegalArgumentException("must have a value here");

      // create a new value
      Value newValue = inValue.create();

      assertEquals("new not undefined", false, newValue.isDefined());
      assertEquals("new not undefined", UNDEFINED, newValue.toString());
      assertEquals("new not same class",
                   inValue.getClass(), newValue.getClass());
      assertEquals("new value not same formatter",
                   inValue.m_formatter, newValue.m_formatter);
      assertEquals("new value not same group",
                   inValue.m_grouping, newValue.m_grouping);
      assertEquals("new value not same edit type",
                   inValue.m_editType, newValue.m_editType);
      assertEquals("new value not same edit values",
                   inValue.m_editValues, newValue.m_editValues);

      // clone the value
      Value clone = inValue.clone();

      assertEquals("clone not defined", true, clone.isDefined());
      assertEquals("not correctly cloned", 0, inValue.compareTo(clone));
      assertEquals("not correctly cloned", inValue.toString(),
                   clone.toString());

      String current = inValue.toString();
      clone.reset();

      assertEquals("not everything cloned", current, inValue.toString());

      // reset the value
      assertEquals("not undefined after reset", false, clone.isDefined());
      assertEquals("not undefined after reset", UNDEFINED, clone.toString());
    }

    //......................................................................

    //----- value ----------------------------------------------------------

    /** A specific value class. */
    private Value m_value = new Value<Value>()
    {
      private boolean m_defined = false;
      protected boolean doRead(@Nonnull ParseReader inReader)
      {
        m_defined = inReader.expect("guru");
        return m_defined;
      }

      public void reset()
      {
        m_defined = false;
      }

      public @Nonnull Command doFormat()
      {
        return new Command("guru");
      }

      public boolean isDefined()
      {
        return m_defined;
      }

      public @Nonnull String doToString()
      {
        return "guru";
      }

      public Value create() {
        Value copy = this.clone();
        copy.reset();
        return copy;
      }
    };

    //......................................................................

    //----- value tests ----------------------------------------------------

    /** The value tests Test. */
    @org.junit.Test
    public void read()
    {
      String []tests =
      {
        "simple", "guru", "guru", null,
        "invalid", "hello", null, "hello",
        "remark", "{*} guru", "{*}guru", null,
        "invalid remark", "{* guru", null, "{* guru",
        "invalid value", "{*} hello", null, "{*} hello",
        "valid with rest", "  guru   hello", "guru", "   hello",
      };

      readTest(tests, m_value);
      setTest(tests, m_value);

      cloneCreateResetTest(m_value);
    }

    //......................................................................
    //----- formatting -----------------------------------------------------

    /** The formatting Test. */
    @org.junit.Test
    public void formatting()
    {
      assertEquals("undefined", new Color("error", UNDEFINED),
                   m_value.format(false));
      assertEquals("undefined", new Command(new Object [0]),
                   m_value.format(true));

      m_value.setFromString("guru");
      assertEquals("defined", new Command("guru"),
                   m_value.format(false));
      assertEquals("defined", new Command("guru"),
                   m_value.format(true));

      m_value.setFromString("{*} guru");
      assertEquals("defined, remark",
                   new net.ixitxachitls.output.commands.Span
                   ("HOUSE_RULE",
                    new net.ixitxachitls.output.commands.Window
                    (new Command("guru"), "House Rule")),
                   m_value.format(false));
      assertEquals("defined, remark",
                   new net.ixitxachitls.output.commands.Span
                   ("HOUSE_RULE",
                    new net.ixitxachitls.output.commands.Window
                    (new Command("guru"), "House Rule")),
                   m_value.format(true));
    }

    //......................................................................
    //----- equalsHash -----------------------------------------------------

    /** The equalsHash Test. */
    @org.junit.Test
    public void equalsHash()
    {
      assertTrue("read", m_value.setFromString("guru").isEmpty());

      Value clone = m_value.clone();
      assertTrue("equals", m_value.equals(clone));
      assertTrue("equals", clone.equals(m_value));
      assertEquals("hash", m_value.hashCode(), clone.hashCode());

      clone.reset();
      assertFalse("equals", m_value.equals(clone));
      assertFalse("equals", clone.equals(m_value));
      assertFalse("hash", m_value.hashCode() == clone.hashCode());
    }

    //......................................................................

  }

  //........................................................................
}
