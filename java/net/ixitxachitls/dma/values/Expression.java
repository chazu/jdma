/******************************************************************************
 * Copyright (c) 2002-2012 Peter 'Merlin' Balsiger and Fred 'Mythos' Dobler
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.ixitxachitls.input.ParseReader;
import net.ixitxachitls.util.logging.Log;

//..........................................................................

//------------------------------------------------------------------- header

/**
 * The base expression for all expressions.
 *
 * @file          Expression.java
 *
 * @author        balsiger@ixitxachitls.net (Peter Balsiger)
 *
 */

//..........................................................................

//__________________________________________________________________________

public abstract class Expression implements Comparable<Expression>
{
  //----------------------------------------------------------------- nested

  //----- Shared -----------------------------------------------------------

  /** A class to share data when computing expressions. */
  public static class Shared
  {
    /** Simple default constructor. */
    protected Shared()
    {
      // nothing to do
    }

    /** The number of magical armor plusses computed so far. */
    private int m_magicArmor = 0;

    /** The number of magical weapon plusses computed so far. */
    private int m_magicWeapon = 0;

    /**
     * Convert to a string.
     *
     * @return the converted string
     */
    @Override
    public String toString()
    {
      return "magic armor " + m_magicArmor + ", magic weapon " + m_magicWeapon;
    }
  }

  //........................................................................
  //----- MagicArmor -------------------------------------------------------

  /** An expression representing the number of plusses for magical armor. */
  public static class MagicArmor extends Expression
  {
    /**
     * Create the magic armor expression.
     *
     * @param inPlus the number of magical plusses for the armor
     */
    public MagicArmor(int inPlus)
    {
      super(1);

      m_plus = inPlus;
    }

    /** The number of plusses for magical armor. */
    private int m_plus;

    /** Pattern for magical armor. */
    private static final Pattern s_pattern =
      Pattern.compile("magic armor\\((\\d+)\\)");

    @Override
    public @Nonnull String toString()
    {
      return "[magic armor(" + m_plus + ")]";
    }

    /**
     * Parse a magic armor from the given text.
     *
     * @param  inText the text to parse from
     *
     * @return the parsed magic armor or null if not properly parsed
     */
    protected static @Nullable MagicArmor parse(@Nonnull String inText)
    {
      Matcher matcher = s_pattern.matcher(inText);
      if(matcher.find())
        return new MagicArmor(Integer.parseInt(matcher.group(1)));

      return null;
    }

    //------------------------------ compute -------------------------------

    /**
     * Compute the value for the expression.  We compute the value here in
     * relation to previously computed magic armor values and only adding the
     * difference.
     *
     * @param       inValue   the value to compute from
     * @param       ioShared  shared data for all expressions
     *
     * @return      the computed, adjusted value, if any
     *
     */
    public @Nullable Value compute(@Nullable Value inValue,
                                   @Nonnull Shared ioShared)
    {
      // (m + p)^2 - m^2 = 2mp + p^2
      int factor = 2 * ioShared.m_magicArmor * m_plus + m_plus * m_plus;
      ioShared.m_magicArmor += m_plus;

      Money value = new Money(0, 0, factor * 1000, 0);
      if(inValue == null)
        return value;

      if(!(inValue instanceof Money))
        return inValue;

      return ((Money)inValue).add(value);
    }

    //......................................................................
  }

  //........................................................................
  //----- Factor -----------------------------------------------------------

  /** An expression representing factor to multiply the values with. */
  public static class Factor extends Expression
  {
    /**
     * Create the factor expression.
     *
     * @param inMultiply the multiplication factor
     * @param inDivide   the dividing factor
     */
    public Factor(long inMultiply, long inDivide)
    {
      super(2);

      m_multiply = inMultiply;
      m_divide = inDivide;
    }

    /** The multiplication factor. */
    private long m_multiply;

    /** The dividing factor. */
    private long m_divide = 1;

    /** Pattern for a factor. */
    private static final Pattern s_pattern =
      Pattern.compile("\\*\\s+(\\d+)(?:/(\\d+))?");

    @Override
    public @Nonnull String toString()
    {
      if(m_divide != 1)
        return "[* " + m_multiply + "/" + m_divide + "]";

      return "[* " + m_multiply + "]";
    }

    /**
     * Parse a magic weapon from the given text.
     *
     * @param  inText the text to parse from
     *
     * @return the parsed magic weapon or null if not properly parsed
     */
    protected static @Nullable Factor parse(@Nonnull String inText)
    {
      Matcher matcher = s_pattern.matcher(inText);
      if(matcher.find())
        return new Factor(Integer.parseInt(matcher.group(1)),
                          Integer.parseInt(matcher.group(2)));

      return null;
    }

    //------------------------------ compute -------------------------------

    /**
     * Compute the value for the expression.
     *
     * @param       inValue   the value to compute from
     * @param       ioShared  shared data for all expressions
     *
     * @return      the compute, adjusted value
     *
     */
    public @Nullable Value compute(@Nullable Value inValue,
                                   @Nonnull Shared ioShared)
    {
      if(inValue == null)
        return null;

      return inValue.multiply(m_multiply).divide(m_divide);
    }

    //......................................................................
  }

  //........................................................................
  //----- Equal -----------------------------------------------------------

  /** An expression representing equal to a value. */
  public static class Equal extends Expression
  {
    /**
     * Create the equal expression.
     *
     * @param inValue the value to set to
     *
     */
    public Equal(@Nonnull String inValue)
    {
      super(10);

      m_value = inValue;
    }

    /** The value to set to. */
    private @Nonnull String m_value;

    /** Pattern for a equal. */
    private static final Pattern s_pattern = Pattern.compile("\\s*=\\s*(.*)");

    @Override
    public @Nonnull String toString()
    {
      return "[= " + m_value + "]";
    }

    /**
     * Parse a magic weapon from the given text.
     *
     * @param  inText the text to parse from
     *
     * @return the parsed magic weapon or null if not properly parsed
     */
    protected static @Nullable Equal parse(@Nonnull String inText)
    {
      Matcher matcher = s_pattern.matcher(inText);
      if(matcher.find())
        return new Equal(matcher.group(1));

      return null;
    }

    /**
     * Compute the value for the expression.
     *
     * @param       inValue   the value to compute from
     * @param       ioShared  shared data for all expressions
     *
     * @return      the compute, adjusted value
     *
     */
    public @Nullable Value compute(@Nullable Value inValue,
                                   @Nonnull Shared ioShared)
    {
      if(inValue == null)
        return null;

      return inValue.read(m_value);
    }
  }

  //........................................................................
  //----- Wand -------------------------------------------------------------

  /** An expression representing a wand with spell and caster levels. */
  public static class Wand extends Expression
  {
    /**
     * Create the wand expression.
     *
     * @param inSpellLevel the level of the spell stored in the wand
     *
     */
    public Wand(int inSpellLevel)
    {
      this(inSpellLevel, inSpellLevel > 1 ? inSpellLevel * 2 - 1 : 1);
    }

    /**
     * Create the wand expression.
     *
     * @param inSpellLevel  the level of the spell
     * @param inCasterLevel the level of the caster
     */
    public Wand(int inSpellLevel, int inCasterLevel)
    {
      super(1);

      if(inCasterLevel < 1
         || (inSpellLevel > 0 && inCasterLevel < inSpellLevel * 2 - 1))
        throw new IllegalArgumentException("caster level too low");

      m_spellLevel = inSpellLevel;
      m_casterLevel = inCasterLevel;
    }

    /** The spell level of the wand. */
    private int m_spellLevel;

    /** The dividing factor. */
    private int m_casterLevel;

    /** Pattern for a wand (spell level only). */
    private static final Pattern s_pattern =
      Pattern.compile("wand\\((\\d+)(?:,\\s*(\\d+))?\\)");

    @Override
    public @Nonnull String toString()
    {
      if(m_casterLevel == m_spellLevel * 2 - 1)
        return "[wand(" + m_spellLevel + ")]";

      return "[wand(" + m_spellLevel + ", " + m_casterLevel + ")]";
    }

    /**
     * Parse a magic weapon from the given text.
     *
     * @param  inText the text to parse from
     *
     * @return the parsed magic weapon or null if not properly parsed
     */
    protected static @Nullable Wand parse(@Nonnull String inText)
    {
      Matcher matcher = s_pattern.matcher(inText);
      if(matcher.find())
        if(matcher.group(2) != null)
          return new Wand(Integer.parseInt(matcher.group(1)),
                          Integer.parseInt(matcher.group(2)));
        else
          return new Wand(Integer.parseInt(matcher.group(1)));

      return null;
    }

    /**
     * Compute the value for the expression.
     * NOTE: this does not include any costs for the spell components and/or XP.
     *
     * @param       inValue   the value to compute from
     * @param       ioShared  shared data for all expressions
     *
     * @return      the compute, adjusted value
     *
     */
    @SuppressWarnings("unchecked")
    public @Nullable Value compute(@Nullable Value inValue,
                                   @Nonnull Shared ioShared)
    {
      Money value;
      if(m_spellLevel == 0)
        value = new Money(0, 75 * m_casterLevel, 0, 0).simplify();
      else
        value = new Money(0, 0, 15 * m_spellLevel * m_casterLevel, 0);

      if(inValue == null)
        return value;

      return inValue.add(value);
    }
  }

  //........................................................................
  //----- Potion -----------------------------------------------------------

  /** An expression representing a potion with spell levels. */
  public static class Potion extends Expression
  {
    /**
     * Create the potion expression.
     *
     * @param inSpellLevel the level of the spell
     */
    public Potion(int inSpellLevel)
    {
      this(inSpellLevel, inSpellLevel > 1 ? inSpellLevel * 2 - 1 : 1);
    }

    /**
     * Create the potion expression.
     *
     * @param inSpellLevel  the level of the spell
     * @param inCasterLevel the level of the caster
     */
    public Potion(int inSpellLevel, int inCasterLevel)
    {
      super(1);

      if(inCasterLevel < 1
         || (inSpellLevel > 0 && inCasterLevel < inSpellLevel * 2 - 1))
        throw new IllegalArgumentException("caster level too low");

      m_spellLevel = inSpellLevel;
      m_casterLevel = inCasterLevel;
    }

    /** The spell level of the wand. */
    private int m_spellLevel;

    /** The dividing factor. */
    private int m_casterLevel;

    /** Pattern for a wand (spell level only). */
    private static final Pattern s_pattern =
      Pattern.compile("potion\\((\\d+)(?:,\\s*(\\d+))?\\)");

    @Override
    public @Nonnull String toString()
    {
      if(m_casterLevel == m_spellLevel * 2 - 1)
        return "[potion(" + m_spellLevel + ")]";

      return "[potion(" + m_spellLevel + ", " + m_casterLevel + ")]";
    }

    /**
     * Parse a potion from the given text.
     *
     * @param  inText the text to parse from
     *
     * @return the parsed magic weapon or null if not properly parsed
     */
    protected static @Nullable Potion parse(@Nonnull String inText)
    {
      Matcher matcher = s_pattern.matcher(inText);
      if(matcher.find())
        if(matcher.group(2) != null)
          return new Potion(Integer.parseInt(matcher.group(1)),
                          Integer.parseInt(matcher.group(2)));
        else
          return new Potion(Integer.parseInt(matcher.group(1)));

      return null;
    }

    /**
     * Compute the value for the expression.
     * NOTE: this does not include any costs for the spell components and/or XP.
     *
     * @param       inValue   the value to compute from
     * @param       ioShared  shared data for all expressions
     *
     * @return      the compute, adjusted value
     *
     */
    @SuppressWarnings("unchecked")
    public @Nullable Value compute(@Nullable Value inValue,
                                   @Nonnull Shared ioShared)
    {
      Money value;
      if(m_spellLevel == 0)
        value = new Money(0, 0, 25 * m_casterLevel, 0);
      else
        value = new Money(0, 0, 50 * m_spellLevel * m_casterLevel, 0);

      if(inValue == null)
        return value;

      return inValue.add(value);
    }
  }

  //........................................................................
  //----- MagicWeapon ------------------------------------------------------

  /** An expression representing the number of plusses for magical weapon. */
  public static class MagicWeapon extends Expression
  {
    /**
     * Create the magic weapon expression.
     *
     * @param inPlus the number of magical plusses for the weapon
     */
    public MagicWeapon(int inPlus)
    {
      super(1);

      m_plus = inPlus;
    }

    /** The number of plusses for magical armor. */
    private int m_plus;

    /** Pattern for magical armor. */
    private static final Pattern s_pattern =
      Pattern.compile("magic weapon\\((\\d+)\\)");

    @Override
    public @Nonnull String toString()
    {
      return "[magic weapon(" + m_plus + ")]";
    }

    /**
     * Parse a magic weapon from the given text.
     *
     * @param  inText the text to parse from
     *
     * @return the parsed magic weapon or null if not properly parsed
     */
    protected static @Nullable MagicWeapon parse(@Nonnull String inText)
    {
      Matcher matcher = s_pattern.matcher(inText);
      if(matcher.find())
        return new MagicWeapon(Integer.parseInt(matcher.group(1)));

      return null;
    }

    /**
     * Compute the value for the expression.  We compute the value here in
     * relation to previously computed magic armor values and only adding the
     * difference.
     *
     * @param       inValue   the value to compute from
     * @param       ioShared  shared data for all expressions
     *
     * @return      the compute, adjusted value
     *
     */
    public @Nullable Value compute(@Nullable Value inValue,
                                   @Nonnull Shared ioShared)
    {
      // 2 * (m + p)^2 - m^2 = 4mp + 2 * p^2
      int factor = 4 * ioShared.m_magicArmor * m_plus + 2 * m_plus * m_plus;
      ioShared.m_magicWeapon += m_plus;

      Money value = new Money(0, 0, factor * 1000, 0);

      if(inValue == null)
        return value;

      if(!(inValue instanceof Money))
        return inValue;

      return ((Money)inValue).add(value);
    }
  }

  //........................................................................

  //........................................................................

  //--------------------------------------------------------- constructor(s)

  //------------------------------ Expression ------------------------------

  /**
   * Create the expression.
   *
   * @param inPriority the priority of the expression for sorting
   *
   */
  public Expression(int inPriority)
  {
    m_priority = inPriority;
  }

  //........................................................................

  //........................................................................

  //-------------------------------------------------------------- variables

  /** The remark starter. */
  private static final char s_start = '[';

  /** The remark end. */
  private static final char s_end = ']';

  /** The priority of the expression. */
  private int m_priority;

  //........................................................................

  //-------------------------------------------------------------- accessors

  //-------------------------------- parse ---------------------------------

  /**
   * Parse the string into the proper expression.
   *
   * @param    inText the text to parse
   *
   * @return   the expression parsed, if any
   *
   */
  protected static @Nullable Expression parse(@Nonnull String inText)
  {
    Expression result = MagicArmor.parse(inText);
    if(result != null)
      return result;

    result = MagicWeapon.parse(inText);
    if(result != null)
      return result;

    result = Wand.parse(inText);
    if(result != null)
      return result;

    result = Potion.parse(inText);
    if(result != null)
      return result;

    result = Equal.parse(inText);
    if(result != null)
      return result;

    Log.warning("could not parse expression for: '" + inText + "'");
    return null;
  }

  //........................................................................
  //------------------------------ compareTo -------------------------------

  /**
   * Compare this expression to the other one.
   *
   * @param       inOther the expression to compare to
   *
   * @return      <0 if this is smaller than the other, >0 if bigger, 0 if equal
   *
   */
  public int compareTo(@Nullable Expression inOther)
  {
    if(inOther == null)
      return +1;

    int diff = m_priority - inOther.m_priority;
    if(diff != 0)
      return diff;

    return toString().compareTo(inOther.toString());
  }

  //........................................................................
  //-------------------------------- equals --------------------------------

  /**
   * Determine if this key is equal to the given object.
   *
   * @param inOther the object to compare for
   *
   * @return true if they are equal, false if not
   */
  @Override
  public boolean equals(Object inOther)
  {
    if(this == inOther)
      return true;

    if(inOther == null)
      return false;

    if(!(inOther instanceof Expression))
      return false;

    return toString().equals(inOther.toString());
  }

  //........................................................................
  //------------------------------- hashCode -------------------------------

  /**
   * Compute the hash code for the key.
   *
   * @return the hash value
   */
  @Override
  public int hashCode()
  {
    return toString().hashCode();
  }

  //........................................................................
  //-------------------------------- shared --------------------------------

  /**
   * Create a shared storage for expression computing.
   *
   * @return      the shared data
   *
   */
  public @Nonnull Shared shared()
  {
    return new Shared();
  }

  //........................................................................

  //........................................................................

  //----------------------------------------------------------- manipulators

  //--------------------------------- read ---------------------------------

  /**
   * Read an expression from the given reader.
   *
   * @param       inReader   the reader to read from
   *
   * @return      the Remark read or null if none was found
   *
   */
  public static @Nullable Expression read(@Nonnull ParseReader inReader)
  {
    ParseReader.Position pos = inReader.getPosition();

    if(!inReader.expect(s_start))
    {
      inReader.seek(pos);
      return null;
    }

    String expression = inReader.read(s_end).trim();

    if(!inReader.expect(s_end))
    {
      inReader.seek(pos);

      return null;
    }

    return Expression.parse(expression);
  }

  //........................................................................

  //........................................................................

  //------------------------------------------------- other member functions

  //------------------------------- compute --------------------------------

  /**
   * Compute the value for the expression.
   *
   * @param       inValue   the value to compute from
   * @param       ioShared  shared data for all expressions
   *
   * @return      the compute, adjusted value
   *
   */
  public abstract @Nullable Value compute(@Nonnull Value inValue,
                                          @Nonnull Shared ioShared);

  //........................................................................

  //........................................................................

  //------------------------------------------------------------------- test

  /** The test. */
  public static class Test extends net.ixitxachitls.util.test.TestCase
  {
    //----- read -----------------------------------------------------------

    /** The read Test. */
    @org.junit.Test
    public void read()
    {
      ParseReader reader = reader("[magic armor(42)]");

      Expression expression = Expression.read(reader);
      assertEquals("magic armor", "[magic armor(42)]", expression.toString());
    }

    //......................................................................

    /**
     * Create the reader with the given input.
     *
     * @param  inInput the input the reader will read from
     *
     * @return the parse reader
     *
     */
    private ParseReader reader(@Nonnull String inInput)
    {
      return new ParseReader(new java.io.StringReader(inInput), "test");
    }
  }

  //........................................................................

  //--------------------------------------------------------- main/debugging

  //........................................................................
}
