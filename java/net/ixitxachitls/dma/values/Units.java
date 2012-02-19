/******************************************************************************
 * Copyright (c) 2002-2012 Peter 'Merlin' Balsiger and Fredy 'Mythos' Dobler
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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import net.ixitxachitls.input.ParseReader;
import net.ixitxachitls.output.commands.Command;
import net.ixitxachitls.output.commands.Span;
import net.ixitxachitls.output.commands.Table;
import net.ixitxachitls.output.commands.Window;
import net.ixitxachitls.util.ArrayIterator;

//..........................................................................

//------------------------------------------------------------------- header

/**
 * This class stores a unit based value.
 *
 * @file          Units.java
 *
 * @author        balsiger@ixitxachitls.net (Peter 'Merlin' Balsiger)
 *
 * @param         <T> the real type of value being used
 *
 */

//..........................................................................

//__________________________________________________________________________

@Immutable
public class Units<T extends Units> extends Value<T>
{
  //----------------------------------------------------------------- nested

  //----- Unit -------------------------------------------------------------

  /**
   *  This class store a single unit description, but no value.
   */
  @Immutable
  public static class Unit
  {
    //-------------------------------- Unit --------------------------------

    /**
     * Create the unit object from the given definition string.
     *
     * The string must have the form:
     *
     *   x/y : unit|units : other-1|other-2|other-3
     *
     * Where x/y is the factor used to convert this unit to the base unit.
     * unit defines the name of the unit, whereas units is the plural name
     * used (optional). The other-n values are optional but can be given to
     * denote other unit names that should be parsed. These values are never
     * used for printing, only for parsing.
     *
     * @param       inDefinition the definition of the unit
     *
     */
    public Unit(@Nonnull String inDefinition)
    {
      String []parts = inDefinition.split("\\s*:\\s*");

      if(parts.length != 3 && parts.length != 2)
        throw new IllegalArgumentException("invalid unit definition (parts)");

      // parse the basic value
      String []numbers = parts[0].split("\\s*/\\s*");
      String []units   = parts[1].split("\\s*\\|\\s*");
      String []other = null;
      if(parts.length == 3)
        other = parts[2].split("\\s*\\|\\s*");

      if(units.length <= 0)
        throw new IllegalArgumentException("invalid unit definition (unit)");

      if(numbers.length != 2)
        throw new IllegalArgumentException("invalid unit definition "
                                           + "(numbers)");

      // set the values
      m_multiplier = Integer.parseInt(numbers[0]);
      m_divisor    = Integer.parseInt(numbers[1]);

      m_unit = units[0];

      if(units.length > 1)
        m_units = units[1];
      else
        m_units = m_unit;

      if(other != null && other.length > 0)
        m_other = other;
    }

    //......................................................................
    //-------------------------------- Unit --------------------------------

    /**
     * Create the unit from the values given.
     *
     * @param       inUnit       the name of the unit
     * @param       inUnits      the plural name of the unit (can be null to
     *                           use the same as the unit name)
     * @param       inOther      other names parsed (can be null)
     * @param       inMultiplier the multiplier to get to the base value
     * @param       inDivisor    the divisor to get to the base value
     *
     */
    public Unit(@Nonnull String inUnit, @Nullable String inUnits,
                int inMultiplier, int inDivisor,
                @Nonnull String ... inOther)
    {
      if(inMultiplier <= 0)
        throw new IllegalArgumentException("the multiplier must be positive");

      if(inDivisor <= 0)
        throw new IllegalArgumentException("the divisor must be positive");

      m_unit       = inUnit;
      m_multiplier = inMultiplier;
      m_divisor    = inDivisor;
      m_other      = inOther;

      if(inUnits == null)
        m_units = m_unit;
      else
        m_units = inUnits;
    }

    //......................................................................

    /** The multiplier to the base value. */
    private int m_multiplier;

    /** The divisor to the base value. */
    private int m_divisor;

    /** The name of the unit. */
    private @Nonnull String m_unit;

    /** The plural name of the unit. */
    protected @Nullable String m_units;

    /** Other parsed name of the unit. */
    private @Nullable String []m_other;

    //------------------------------- equals -------------------------------

    /**
     * Check if this unit is equal to the given one.
     *
     * We don't take the plural name or the other names into account
     * when comparing for equivalence.
     *
     * We cannot use a generic method or the right type for the other object
     * here, because the Arrays.equals method that is used to compare two
     * arrays of Unit does not support this.
     *
     * @param       inOther the unit to compare to
     *
     * @return      true if equal, false if not
     *
     */
    public boolean equals(@Nullable Object inOther)
    {
      if(inOther == null || !(inOther instanceof Unit))
        return false;

      Unit other = (Unit)inOther;

      if(this == other)
        return true;

      if(!m_unit.equals(other.m_unit))
        return false;

      if(m_multiplier != other.m_multiplier)
        return false;

      if(m_divisor != other.m_divisor)
        return false;

      return true;
    }

    //......................................................................
    //------------------------------ hashCode ------------------------------

    /**
     * Compute a hashcode for the value.
     *
     * @return      the hashcode
     *
     */
    public int hashCode()
    {
      return m_unit.hashCode();
    }

    //......................................................................
    //----------------------------- getAsBase ------------------------------

    /**
     * Convert the given value into the value represented in the base unit.
     *
     * @param       inValue the value to convert
     *
     * @return      the converted value
     *
     */
    public @Nonnull Rational getAsBase(@Nonnull Rational inValue)
    {
      Rational result = inValue;

      if(m_multiplier != 1 || m_divisor != 1)
      {
        result = result.multiply(m_multiplier);
        result = result.divide(m_divisor);
      }

      return result;
    }

    //......................................................................
    //------------------------------ toString ------------------------------

    /**
     * Convert the given value with the unit into a String for printing.
     *
     * @param       inValue the value to print
     *
     * @return      the convertd value as String
     *
     */
    public String toString(@Nonnull Rational inValue)
    {
      if(inValue.isNull())
          return "0 " + m_unit;

      if(inValue.isSingular())
        return inValue.toString() + " " + m_unit;
      else
        return inValue.toString() + " " + m_units;
    }

    //......................................................................
    //------------------------------ format --------------------------------

    /**
     * Format the value for printing.
     *
     * @param       inValue the value to print
     *
     * @return      the convert value as a command
     *
     */
    public Command format(@Nonnull Rational inValue)
    {
      if(inValue.isNull())
        return new Command("0 " + m_unit);

      return new Span("unit", new Command(new Object []
        {
          inValue.format(false),
          " ",
          inValue.isSingular() ? m_unit : m_units,
        }));
    }

    //......................................................................
    //------------------------------ toString ------------------------------

    /**
     * Convert the object into a human readable String (for debugging).
     *
     * @return      the human readable String conversion
     *
     */
    public @Nonnull String toString()
    {
      StringBuilder other = new StringBuilder();
      if(m_other != null)
        for(int i = 0; i < m_other.length; i++)
          other.append("/" + m_other[i]);

      return m_unit + "/" + m_units + other
        + " *" + m_multiplier + "/" + m_divisor;
    }

    //......................................................................
  }

  //........................................................................
  //----- Set --------------------------------------------------------------

  /**
   *  This class store a set of unit descriptions, but no values.
   *
   */
  @Immutable
  public static class Set
  {
    //-------------------------------- Set ---------------------------------

    /**
     * Create the set from the given definition String.
     *
     * The String must have the form:
     *
     *   x/y : name = unit-1, unit-2, unit-3
     *
     * Where x/y is the conversion factor to the base set. name is the
     * name of this set and unit-n are the units for this set.
     *
     * @param       inDefinition the definition String to parse
     *
     */
    public Set(@Nonnull String inDefinition)
    {
      String []intro = inDefinition.split("\\s*=\\s*");

      if(intro.length != 2)
        throw new IllegalArgumentException("invalid set (intro)");

      String []parts  = intro[0].split("\\s*:\\s*");

      if(parts.length != 2)
        throw new IllegalArgumentException("invalid set (parts)");

      // set conversion
      String []numbers = parts[0].split("\\s*/\\s*");

      if(numbers.length != 2)
        throw new IllegalArgumentException("invalid set (numbers)");

      m_multiplier = Integer.parseInt(numbers[0]);
      m_divisor    = Integer.parseInt(numbers[1]);

      // set name
      m_name = parts[1].trim();

      // get all the unit definitions
      String []units = intro[1].split("\\s*,\\s*");

      if(units.length <= 0)
        throw new IllegalArgumentException("no units given");

      m_units = new Unit[units.length];

      for(int i = 0; i < units.length; i++)
      {
        m_units[i] = new Unit(units[i]);

        Object result = m_names.put(m_units[i].m_unit, i);

        if(result != null)
          throw new IllegalArgumentException("unit name " + m_units[i].m_unit
                                             + " must be unique");

        if(!m_units[i].m_units.equals(m_units[i].m_unit))
        {
          result = m_names.put(m_units[i].m_units, i);

          if(result != null)
            throw new IllegalArgumentException("unit name "
                                               + m_units[i].m_units
                                               + " must be unique");
        }

        if(m_units[i].m_other != null)
          for(int j = 0; j < m_units[i].m_other.length; j++)
          {
            result = m_names.put(m_units[i].m_other[j], i);

            if(result != null)
              throw new IllegalArgumentException("unit name "
                                                 + m_units[i].m_other[j]
                                                 + " must be unique");
          }

         if(m_units[i].m_multiplier == 1 && m_units[i].m_divisor == 1)
          m_base = i;
      }
    }

    //......................................................................
    //-------------------------------- Set ---------------------------------

    /**
     * Create the set from the values given.
     *
     * @param       inName       the name of the set
     * @param       inMultiplier the multiplier to convert to base
     * @param       inDivisor    the divisor to convert to base
     * @param       inUnits      the units recognized for this set; they must
     *                           ordered be from the biggest down to the
     *                           largest; one of the units must be the base
     *                           units with a conversion factor of 1/1
     *
     */
    public Set(@Nonnull String inName, int inMultiplier, int inDivisor,
               @Nonnull Unit ... inUnits)
    {
      if(inUnits.length <= 0)
        throw new IllegalArgumentException("units must be given");

      if(inMultiplier <= 0)
        throw new IllegalArgumentException("the multiplier must be positive");

      if(inDivisor <= 0)
        throw new IllegalArgumentException("the divisor must be positive");

      m_name       = inName;
      m_multiplier = inMultiplier;
      m_divisor    = inDivisor;

      m_units = new Unit [inUnits.length];

      for(int i = 0; i < inUnits.length; i++)
      {
        m_units[i] = inUnits[i];

        if(m_names.put(m_units[i].m_unit, i) != null)
        throw new IllegalArgumentException("unit name " + m_units[i].m_unit
                                           + " must be unique");

        if(!m_units[i].m_units.equals(m_units[i].m_unit))
          if(m_names.put(m_units[i].m_units, i) != null)
        throw new IllegalArgumentException("unit name " + m_units[i].m_units
                                           + " must be unique");

        if(m_units[i].m_other != null)
          for(int j = 0; j < m_units[i].m_other.length; j++)
            if(m_names.put(m_units[i].m_other[j], i) != null)
        throw new IllegalArgumentException("unit name "
                                           + m_units[i].m_other[j]
                                           + " must be unique");

        if(m_units[i].m_multiplier == 1 && m_units[i].m_divisor == 1)
          m_base = i;
      }

      if(m_base < 0)
        throw new IllegalArgumentException("one of the units must be a "
                                           + "base unit");
    }

    //......................................................................

    /** The name of the set. */
    private @Nonnull String m_name;

    /** All the possible units. */
    protected @Nonnull Unit []m_units;

    /** The base unit. */
    private int m_base = -1;

    /** The multiplier to the base set. */
    private int m_multiplier;

    /** The divisor to the base set. */
    private int m_divisor;

    /** All the unit names understood. */
    private @Nonnull Map<String, Integer> m_names =
      new Hashtable<String, Integer>();

    //------------------------------- equals -------------------------------

    /**
     * Check if this set is equal to the given one.
     *
     * @param       inOther the set to compare to
     *
     * @return      true if equal, false if not
     *
     */
    public boolean equals(@Nullable Object inOther)
    {
      if(inOther == null)
        return false;

      if(!(inOther instanceof Set))
        return false;

      Set other = (Set)inOther;

      if(this == other)
        return true;

      if(!m_name.equals(other.m_name))
        return false;

      if(m_base != other.m_base)
        return false;

      if(m_multiplier != other.m_multiplier)
        return false;

      if(m_divisor != other.m_divisor)
        return false;

      return Arrays.equals(m_units, other.m_units);
    }

    //......................................................................
    //------------------------------ hashCode ------------------------------

    /**
     * Compute a hashcode for the value.
     *
     * @return      the hashcode
     *
     */
    public int hashCode()
    {
      return m_name.hashCode();
    }

    //......................................................................
    //----------------------------- expectUnit -----------------------------

    /**
     * Check if a unit of this set will come next in the given stream.
     *
     * @param       inReader the reader to look in
     *
     * @return      the index of the unit found, or -1 if none was found
     *
     */
    public int expectUnit(@Nonnull ParseReader inReader)
    {
      String key = inReader.expect(m_names.keySet().iterator());

      if(key == null)
        return -1;

      Integer index = m_names.get(key);

      if(index == null)
        return -1;

      return index;
    }

    //......................................................................
    //------------------------------ convert ------------------------------

    /**
     * Convert the given normalized value into a complete set of values
     * for the set.
     *
     * If simplification is desired, the value will be distributed as good
     * as possible over all units. If not, the value given will simply be
     * stored in the base unit.
     *
     * @param       inNormalized the normalized value to convert
     * @param       inLevel      the level of simplification
     * @param       inSimplify   flag if simplification is desired
     *
     * @return      the complete values
     *
     */
    public @Nonnull Rational []convert(@Nonnull Rational inNormalized,
                                       int inLevel, boolean inSimplify)
    {
      Rational total = inNormalized;
      total = total.multiply(m_multiplier);
      total = total.divide(m_divisor);

      if(!inSimplify)
      {
        Rational []result = new Rational[m_units.length];
        result[m_base] = total;

        return result;
      }

      return simplify(total, inLevel);
    }

    //......................................................................
    //------------------------------ simplify ------------------------------

    /**
     * Simplify the given normalized or total value into a complete set
     * of values for the current set.
     *
     * @param       inTotal the total for the values
     * @param       inLevel the simplification level denoting the highest
     *                      denominator that will be tolerated
     *
     * @return      the simplified values
     *
     */
    public @Nonnull Rational []simplify(@Nonnull Rational inTotal, int inLevel)
    {
      Rational []result = new Rational[m_units.length];
      Rational rest = inTotal;

      // to higher
      for(int i = 0; i < m_units.length; i++)
      {
        if(i != m_base)
        {
          // convert to current unit
          rest = rest.multiply(m_units[i].m_divisor);
          rest = rest.divide(m_units[i].m_multiplier);
        }

        // determine the simplification level dependant on the dividing
        // factor from this to the next
        int level = inLevel;

        if(i + 1 < m_units.length)
          if(m_units[i + 1].m_divisor > 1)
            level =
              Math.min(inLevel,
                       m_units[i + 1].m_divisor / m_units[i].m_divisor);

        if(rest.getDenominator() < level || i + 1 >= m_units.length)
        {
          // all matches (no clone necessary because we stop here)
          result[i] = rest;

          break;
        }
        else
          if(rest.getLeader() > 0)
          {
            // partially matches
            result[i] = new Rational(rest.getLeader());

            rest = rest.subtract(rest.getLeader());
          }

        if(i != m_base)
        {
          // convert back
          rest = rest.multiply(m_units[i].m_multiplier);
          rest = rest.divide(m_units[i].m_divisor);
        }
      }

      return result;
    }

    //......................................................................
    //------------------------------ toString ------------------------------

    /**
     * Convert the object into a human readable String (for debugging).
     *
     * @return      the human readable String conversion
     *
     */
    public @Nonnull String toString()
    {
      StringBuffer result = new StringBuffer(m_name);

      result.append(" (");
      result.append(m_multiplier);
      result.append("/");
      result.append(m_divisor);
      result.append("): ");

      for(int i = 0; i < m_units.length; i++)
      {
        result.append(m_units[i].toString());

        if(i < m_units.length - 1)
          result.append(", ");
      }

      return result.toString();
    }

    //......................................................................
    //---------------------------- getBaseUnit -----------------------------

    /**
     * Get the name of the base unit of this set.
     *
     * @return      the requested name
     *
     */
    public @Nonnull String getBaseUnit()
    {
      return m_units[m_base].m_units;
    }

    //......................................................................
    //----------------------------- getAsBase ------------------------------

    /**
     * Convert the given values into the base unit.
     *
     * @param       inValues the values to convert
     *
     * @return      the converted value
     *
     */
    public @Nonnull Rational getAsBase(@Nonnull Rational []inValues)
    {
      Rational result = new Rational(0);

      for(int i = 0; i < inValues.length; i++)
        if(inValues[i] != null)
          result = result.add(m_units[i].getAsBase(inValues[i]));

      return result;
    }

    //......................................................................
    //-------------------------------- format ------------------------------

    /**
     * Format the set into a command.
     *
     * @param       inValues the values to print with the set information
     *
     * @return      the format user for printing this set with the given values
     *
     */
    protected @Nonnull Command format(@Nonnull Rational []inValues)
    {
      assert inValues.length == m_units.length
        : "number of units and values don't match";

      ArrayList<Object> commands = new ArrayList<Object>();

      for(int i = 0; i < m_units.length; i++)
        if(inValues[i] != null && !inValues[i].isNull())
        {
          if(commands.size() > 0)
            commands.add(" ");

          commands.add(m_units[i].format(inValues[i]));
        }

      if(commands.size() == 0)
        return new Span("unit", "0 " + getBaseUnit());

      return new Command(commands.toArray());
    }

    //......................................................................
  }

  //........................................................................

  //........................................................................

  //--------------------------------------------------------- constructor(s)

  //------------------------------- Units --------------------------------

  /**
   * Construct the units object with undefined values.
   *
   * @param       inDefinition    a string representing the definition of the
   *                              type
   * @param       inSimplifyLevel the maximal number to be allowed in
   *                              denominators before the value is
   *                              relegated to the next smaller unit
   *
   */
  public Units(@Nonnull String inDefinition, int inSimplifyLevel)
  {
    m_sets = parseDefinition(inDefinition);
    m_simplifyLevel = inSimplifyLevel;
  }

  //........................................................................
  //------------------------------- Units --------------------------------

  /**
   * Construct the units object with sets.
   *
   * @param       inSets          the sets defined for these units
   * @param       inSimplifyLevel the maximal number to be allowed in
   *                              denominators before the value is
   *                              relegated to the next smaller unit
   *
   */
  public Units(@Nonnull Set []inSets, int inSimplifyLevel)
  {
    if(inSets.length <= 0)
      throw new IllegalArgumentException("at least one set must be given");

    m_sets = Arrays.copyOf(inSets, inSets.length);
    m_simplifyLevel = inSimplifyLevel;
  }

  //........................................................................
  //------------------------------- Units --------------------------------

  /**
   * Construct the units object with sets.
   *
   * @param       inValues        the values to initialize with
   * @param       inSets          the sets defined for these units
   * @param       inSet           the value set to
   * @param       inSimplifyLevel the maximal number to be allowed in
   *                              denominators before the value is
   *                              relegated to the next smaller unit
   *
   * @undefined   IllegalArgumentException if sets null or empty
   * @undefined   IllegalArgumentException if values null
   * @undefined   IllegalArgumentException if set null
   *
   */
  public Units(@Nonnull Rational []inValues, @Nonnull Set []inSets,
               @Nonnull Set inSet, int inSimplifyLevel)
  {
    if(inSets.length <= 0)
        throw new IllegalArgumentException("at least one set must be given");

    if(inValues.length != inSet.m_units.length)
        throw new IllegalArgumentException("number of values invalid");

    m_values = Arrays.copyOf(inValues, inValues.length);
    m_sets = Arrays.copyOf(inSets, inSets.length);
    m_simplifyLevel = inSimplifyLevel;
    m_set = inSet;
  }

  //........................................................................

  {
    withEditType("name");
  }

  //------------------------------- create --------------------------------

  /**
   * Create a new list with the same type information as this one, but one
   * that is still undefined.
   *
   * @return      a similar list, but without any contents
   *
   * @undefined   never
   *
   */
  @SuppressWarnings("unchecked") // this only works if this method is
                                 // overridden in all derivations
  public T create()
  {
    return super.create((T)new Units(m_sets, m_simplifyLevel));
  }

  //........................................................................

  //........................................................................

  //-------------------------------------------------------------- variables

  /** The sets used in for this units. */
  protected @Nonnull Set []m_sets;

  /** The values of each unit. */
  protected @Nullable Rational []m_values;

  /** The set of units currently used. */
  protected @Nullable Set m_set;

  /** The level of simplification. */
  protected int m_simplifyLevel = 0;

  //........................................................................

  //-------------------------------------------------------------- accessors

  //------------------------------- getValues ------------------------------

  /**
   * Get all the values.
   *
   * @return      the values
   *
   */
  public Iterator<Rational> getValues()
  {
    return new ArrayIterator<Rational>(m_values);
  }

  //........................................................................
  //------------------------------ getAsBase -------------------------------

  /**
   * Get the whole value but as if it was converted to the base of the base
   * unit of the current group..
   *
   * @return      the total value in base units
   *
   */
  public @Nonnull Rational getAsBase()
  {
    if(!isDefined())
      return new Rational();

    return m_set.getAsBase(m_values);
  }

  //........................................................................
  //----------------------------- getBaseUnit ------------------------------

  /**
   * Get the whole name of the base unit.
   *
   * @return      the name of the base unit
   *
   */
  public @Nonnull String getBaseUnit()
  {
    if(!isDefined())
      return Value.UNDEFINED;

    return m_set.getBaseUnit();
  }

  //........................................................................

  //------------------------------- doFormat -------------------------------

  /**
   * Format the value for printing.
   *
   * @return      the command that can be printed
   *
   */
  @Override
  protected @Nonnull Command doFormat()
  {
    ArrayList<Object> table = new ArrayList<Object>();

    table.add("Total:");
    table.add(new Command(new Object []
      {
        getAsBase().format(false),
        " ",
        getBaseUnit(),
      }));
    table.add("");
    table.add("");

    for(int i = 0; i < m_sets.length; i++)
    {
      if(m_sets[i] == m_set)
        continue;

      table.add(m_sets[i].m_name + ":");

      Units converted = toSet(i, true);
      table.add(converted.m_set.format(converted.m_values));
    }

    return new Window(formatUnits(),
                      new Table("#inline#1:L,,;100:L",
                                table.toArray(new Object[table.size()])));
  }

  //........................................................................
  //----------------------------- formatUnits ------------------------------

  /**
   * Format all the units for printing.
   *
   * @return      the command to print all unit values
   *
   */
  protected @Nonnull Command formatUnits()
  {
    ArrayList<Object> commands = new ArrayList<Object>();

    for(int i = 0; i < m_values.length; i++)
    {
      Command command = formatSingleUnit(i);
      if(command == null)
        continue;

      if(commands.size() > 0)
        commands.add(" ");

      commands.add(command);
    }

    if(commands.size() == 0)
      return new Span("unit", "0 " + getBaseUnit());

    return new Command(commands);
  }

  //........................................................................
  //--------------------------- formatSingleUnit ---------------------------

  /**
   * Format a single unit value, given by index.
   *
   * @param       inIndex the index of the unit value to format.
   *
   * @return      the command to format the value or null if there is no value
   *
   */
  protected @Nullable Command formatSingleUnit(int inIndex)
  {
    assert inIndex >= 0 && inIndex <= m_values.length;

    if(m_values[inIndex] == null || !m_values[inIndex].isDefined())
      return null;

    return m_set.m_units[inIndex].format(m_values[inIndex]);
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
  protected @Nonnull String doToString()
  {
    if(m_set == null)
      return "$undefined$";

    StringBuilder result = new StringBuilder();

    for(int i = 0; i < m_set.m_units.length; i++)
    {
      String single = singleUnitToString(i);
      if(!single.isEmpty())
        result.append(single + " ");
    }

    String end = result.toString().trim();

    if(end.length() == 0)
      return "0 " + m_set.getBaseUnit();

    return end;
  }

  //........................................................................
  //-------------------------- singleUnitToString --------------------------

  /**
   * Convert a single unit to a string.
   *
   * @param       inIndex the index of the unit to print
   *
   * @return      the converted string
   *
   */
  protected @Nonnull String singleUnitToString(int inIndex)
  {
    if(inIndex < 0 || inIndex >= m_values.length)
      throw new IllegalArgumentException("invalid index given");

    if(m_values[inIndex] == null || m_values[inIndex].isNull())
      return "";

    return m_set.m_units[inIndex].toString(m_values[inIndex]);
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

  //-------------------------------- toSet ---------------------------------

  /**
   * Convert the value into another set of units.
   *
   * @param       inSet      the set to convert to
   * @param       inSimplify a flag to denote if a simplification should be
   *                         done when converting
   *
   * @return      the converted set
   *
   */
  @SuppressWarnings("unchecked") // must cast this on return
  public @Nonnull T toSet(int inSet, boolean inSimplify)
  {
    if(inSet < 0 || inSet >= m_sets.length)
      throw new IllegalArgumentException("invalid set given");

    if(m_sets[inSet] == m_set)
      return (T)this;

    Rational normalized = m_set.getAsBase(m_values);
    normalized = normalized.divide(m_set.m_multiplier);
    normalized = normalized.multiply(m_set.m_divisor);

    Rational []values = m_sets[inSet].convert(normalized, m_simplifyLevel,
                                              inSimplify);

    return as(values, inSet);
  }

  //........................................................................

  //------------------------------ compareTo -------------------------------

  /**
   * Compare the current units object to another one.
   *
   * @param       inOther the object to compare to
   *
   * @return      0 if equal, < 0 if less than the other, > 0 if bigger
   *
   */
  public int compareTo(@Nonnull Object inOther)
  {
    if(this == inOther)
      return 0;

    // if classes are not the same, use the super comparison,
    // e.g. lexicographic comparison
    if(!(inOther instanceof Units) || inOther.getClass() != getClass())
      return super.compareTo(inOther);

    // check if the sets are the same
    if(m_sets.length != ((Units)inOther).m_sets.length)
      return super.compareTo(inOther);

    for(int i = 0; i < m_sets.length; i++)
      if(!m_sets[i].equals(((Units)inOther).m_sets[i]))
        return super.compareTo(inOther);

    // use real value comparison here
    return getAsBase().compare(((Units)inOther).getAsBase());
  }

  //........................................................................

  //........................................................................

  //----------------------------------------------------------- manipulators

  //---------------------------------- as ----------------------------------

  /**
   * Create a value as the current one with the given values.
   *
   * @param       inValues the new values to set to
   * @param       inSet    the set to set the values for
   *
   * @return      the newly created value
   *
   */
  public T as(@Nonnull Rational []inValues, int inSet)
  {
    if(inSet < 0 || inSet >= m_sets.length)
      throw new IllegalArgumentException("invalid set specified");

    if(inValues.length != m_sets[inSet].m_units.length)
      throw new IllegalArgumentException("invalid values given");

    T result = create();
    result.m_values = new Rational [inValues.length];

    for(int i = 0; i < inValues.length; i++)
      if(inValues[i] != null)
        result.m_values[i] = inValues[i];

    result.m_set = m_sets[inSet];

    return result;
  }

  //........................................................................
  //--------------------------------- add ----------------------------------

  /**
   * Add the other units to this one.
   *
   * @param       inOther the other unit to the current to
   *
   * @return      a new object with the addition of the given
   *
   */
  @SuppressWarnings("unchecked") // have to cast this on return
  public @Nonnull T add(@Nonnull T inOther)
  {
    if(!inOther.isDefined())
      return (T)this;

    // check the equality of types
    if(m_sets.length != inOther.m_sets.length)
      throw new IllegalArgumentException("types of units don't match");

    if(m_sets != inOther.m_sets)
      if(!Arrays.equals(m_sets, inOther.m_sets))
        throw new IllegalArgumentException("sets dont' match");

    // now do the real adding of values
    T result = create();

    // if we don't have a set right now, we use the one given
    result.m_set = (m_set == null ? inOther.m_set : m_set);
    result.m_values =
      m_values == null ? new Rational [result.m_set.m_units.length] : m_values;

    // first the easy one, the same sets
    if(result.m_set == inOther.m_set)
    {
      for(int i = 0; i < result.m_values.length; i++)
        if(inOther.m_values[i] != null)
          if(m_values != null && m_values[i] != null)
            result.m_values[i] = m_values[i].add(inOther.m_values[i]);
          else
            result.m_values[i] = inOther.m_values[i];
        else
          if(m_values != null)
            result.m_values[i] = m_values[i];
    }
    else
    {
      // determine the set
      int i;
      for(i = 0; i < m_sets.length; i++)
        if(m_sets[i] == result.m_set)
          break;

      return add((T)inOther.toSet(i, true));
    }

    return result;
  }

  //........................................................................
  //------------------------------- subtract -------------------------------

  /**
   * Subtract the other units from this one.
   *
   * @param       inOther the other units to subtract them from this one
   *
   * @return      a new object with the subtraction result
   *
   */
  @SuppressWarnings("unchecked") // need to cast
  public @Nonnull T subtract(@Nonnull T inOther)
  {
    // check the equality of types
    if(m_sets.length != inOther.m_sets.length)
      throw new IllegalArgumentException("sets don't match");

    if(m_sets != inOther.m_sets)
      if(!Arrays.equals(m_sets, inOther.m_sets))
        throw new IllegalArgumentException("sets not equal");

    // now do the real adding of values
    T result = create();
    result.m_values = new Rational[m_values.length];

    // if we don't have a set right now, we use the one given
    result.m_set = m_set == null ? inOther.m_set : m_set;
    result.m_values =
      m_values == null ? new Rational [m_set.m_units.length] : m_values;

    // first the easy one, the same sets
    if(m_set == inOther.m_set)
    {
      for(int i = 0; i < m_values.length; i++)
        if(inOther.m_values[i] != null)
          if(m_values[i] != null)
            result.m_values[i] = m_values[i].subtract(inOther.m_values[i]);
          else
            result.m_values[i] =
              new Rational(-inOther.m_values[i].getLeader(),
                           inOther.m_values[i].getNominator(),
                           inOther.m_values[i].getDenominator());
    }
    else
    {
      // determine the set
      int i;
      for(i = 0; i < m_sets.length; i++)
        if(m_sets[i] == m_set)
          break;

      return subtract((T)inOther.toSet(i, true));
    }

    return result;
  }

  //........................................................................
  //------------------------------- multiply -------------------------------

  /**
   * Multiply the units.
   *
   * @param       inValue the multiplication factor
   *
   * @return      a new value with the result of the multiplication
   *
   */
  @SuppressWarnings("unchecked") // need to cast this
  public T multiply(long inValue)
  {
    if(m_values == null)
      return (T)this;

    T result = create();
    result.m_values = new Rational[m_values.length];
    result.m_set = m_set;
    for(int i = 0; i < m_values.length; i++)
      if(m_values[i] != null)
        result.m_values[i] = m_values[i].multiply(inValue);

    return result;
  }

  //........................................................................
  //------------------------------- multiply -------------------------------

  /**
   * Multiply the units.
   *
   * @param       inValue the multiplication factor
   *
   * @return      a new value with the result of the multiplication
   *
   */
  @SuppressWarnings("unchecked") // need to cast
  public T multiply(@Nonnull Rational inValue)
  {
    if(m_values == null)
      return (T)this;

    T result = create();
    result.m_values = new Rational[m_values.length];
    result.m_set = m_set;
    for(int i = 0; i < m_values.length; i++)
      if(m_values[i] != null)
        result.m_values[i] = m_values[i].multiply(inValue);

    return (T)result.simplify();
  }

  //........................................................................
  //-------------------------------- divide --------------------------------

  /**
   * Divide the units.
   *
   * @param       inValue the division factor
   *
   * @return      a new object containig the result of the division
   *
   */
  @SuppressWarnings("unchecked") // need to cast this
  public T divide(long inValue)
  {
    if(!isDefined())
      return (T)this;

    T result = create();
    result.m_values = new Rational[m_values.length];
    result.m_set = m_set;
    for(int i = 0; i < m_values.length; i++)
      if(m_values[i] != null)
        result.m_values[i] = m_values[i].divide(inValue);

    return result;
  }

  //........................................................................
  //-------------------------------- divide --------------------------------

  /**
   * Divide the units.
   *
   * @param       inValue the dividing factor
   *
   * @return      a new result containing the result of the division
   *
   */
  @SuppressWarnings("unchecked") // need to cast
  public T divide(@Nonnull Rational inValue)
  {
    T result = create();
    result.m_values = new Rational[m_values.length];
    result.m_set = m_set;
    for(int i = 0; i < m_values.length; i++)
      if(m_values[i] != null)
        result.m_values[i] = m_values[i].divide(inValue);

    return (T)result.simplify();
  }

  //........................................................................
  //------------------------------- simplify -------------------------------

  /**
   * Simplify the current value by converting it to a normalized value.
   *
   * @return the simplified value
   *
   */
  @SuppressWarnings("unchecked") // need to cast
  public T simplify()
  {
    if(m_set == null)
      return (T)this;

    T result = create();
    result.m_values =
      m_set.simplify(m_set.getAsBase(m_values), m_simplifyLevel);
    result.m_set = m_set;

    return result;
  }

  //........................................................................

  //------------------------------- doRead ---------------------------------

  /**
   * Read the value from the reader and replace the current one.
   *
   * @param       inReader   the reader to read from
   *
   * @return      true if read, false if not
   *
   */
  @Override
  public boolean doRead(@Nonnull ParseReader inReader)
  {
    int i = readSingleUnit(inReader);
    if(i < 0)
      return false;

    while(readSingleUnit(inReader) >= 0)
      ;

    return true;
  }

  //........................................................................
  //---------------------------- readSingleUnit ----------------------------

  /**
   * Read a single unit.
   *
   * @param       inReader the reader to read from
   *
   * @return      the number of the unit read or -1 if none read
   *
   */
  protected int readSingleUnit(@Nonnull ParseReader inReader)
  {
    ParseReader.Position pos = inReader.getPosition();
    Rational read = new Rational().read(inReader);

    if(read == null || read.getValue() < 0)
    {
      inReader.seek(pos);
      return -1;
    }

    if(m_set == null)
    {
      // read the first value, i.e. determine what values to read
      int i;
      int index = -1;
      for(i = 0; i < m_sets.length; i++)
      {
        index = m_sets[i].expectUnit(inReader);

        if(index >= 0)
          break;
      }

      // found no valid set
      if(i >= m_sets.length)
      {
        inReader.seek(pos);
        return -1;
      }

      // we use the set found
      m_set = m_sets[i];
      m_values = new Rational[m_set.m_units.length];
      m_values[index] = read;

      return index;
    }

    int index = m_set.expectUnit(inReader);
    if(index >= 0)
      if(m_values[index] == null)
        m_values[index] = read;
      else
        m_values[index] = m_values[index].add(read);
    else
      inReader.seek(pos);

    return index;
  }

  //........................................................................


  //........................................................................

  //------------------------------------------------- other member functions

  //--------------------------- parseDefinition ----------------------------

  /**
   * Parse the string definition of the unit sets and create the set object.
   *
   * @param       inDefinition the definition of the sets
   *
   * @return      all the parsed sets
   *
   */
  protected static @Nonnull Set []parseDefinition(@Nonnull String inDefinition)
  {
    String []sets = inDefinition.split("\\.");

    Set []result = new Set[sets.length];

    for(int i = 0; i < sets.length; i++)
      result[i] = new Set(sets[i]);

    return result;
  }

  //........................................................................

  //........................................................................

  //------------------------------------------------------------------- test

  /** This is the test. */
  public static class Test extends net.ixitxachitls.util.test.TestCase
  {
    /** The test sets. */
    private static Set []s_sets =
    {
      new Set("official", 1, 1,
              new Unit("pp", "pps", 10,   1, "platinum"),
              new Unit("gp", "gps",  1,   1, "gold"),
              new Unit("sp", null,   1,  10, "silver"),
              new Unit("cp", null,   1, 100)),
      new Set("my", 2, 5,
              new Unit("my pp", "my pps", 400, 1, "my platinum"),
              new Unit("my gp", "my gps",  20, 1, "my gold"),
              new Unit("my sp", "my sp",    1, 1, "my silver")),
      new Set("guru", 1, 2,
              new Unit("guru", "gurus", 1, 1)),
    };

    /** The second test set. */
    private static Set []s_sets2 =
    {
      new Set("official", 1, 1,
              new Unit("pp", null, 10,   1),
              new Unit("gp", null,  1,   1),
              new Unit("sp", null,  1,  10),
              new Unit("cp", null,  1, 100)),
      new Set("my", 2, 5,
              new Unit("my pp", null, 400, 1),
              new Unit("my gp", null,  20, 1),
              new Unit("my sp", null,   1, 1)),
      new Set("guru", 1, 2,
              new Unit("guru", null, 1, 1)),
    };

    //----- init -----------------------------------------------------------

    /** Testing initializations. */
    @org.junit.Test
    public void init()
    {
      Units value = new Units(s_sets, 1);

      // undefined value
      assertEquals("not undefined at start", false, value.isDefined());
      assertEquals("undefined value not correct", "$undefined$",
                   value.toString());
      assertEquals("undefined value not correct", "$undefined$",
                   value.getAsBase().toString());
      assertEquals("undefined value not correct", "$undefined$",
                   value.getBaseUnit());

      // now with some value
      value = new Units(new Rational []
        {
          new Rational(1),
          new Rational(10),
          null,
          new Rational(1)
        }, s_sets, s_sets[0], 4);

      //System.out.println(value);

      assertEquals("not defined after setting", true, value.isDefined());
      assertEquals("base",      "20 1/100", value.getAsBase().toString());
      assertEquals("base unit", "gps", value.getBaseUnit());
      assertEquals("output", "1 pp 10 gps 1 cp",
                   value.toString());

      assertEquals("convert 0", "1 pp 10 gps 1 cp",
                   value.toSet(0, false).toString());
      assertEquals("convert 1", "8 1/250 my sp",
                   value.toSet(1, false).toString());
      assertEquals("convert double", "10 1/200 gurus",
                   value.toSet(1, false).toSet(2, false).toString());
      assertEquals("convert 0", "2 pps 1 cp",
                   value.toSet(1, false).toSet(0, true).toString());
      assertEquals("convert 1", "8 1/250 my sp",
                   value.toSet(1, true).toString());
      assertEquals("convert double", "10 1/200 gurus",
                   value.toSet(1, false).toSet(2, true).toString());
      assertEquals("output", "\\window{\\span{unit}{1 pp} "
                   + "\\span{unit}{10 gps} \\span{unit}{1 cp}}"
                   + "{\\table{#inline#1:L,,;100:L}"
                   + "{Total:}{\\frac[20]{1}{100} gps}"
                   + "{}{}{my:}{\\span{unit}{\\frac[8]{1}{250} my sp}}"
                   + "{guru:}{\\span{unit}{\\frac[10]{1}{200} gurus}}}",
                   value.format(false).toString());

      // definition from string
      value = new Units("1/1 : official = 10/1   : pp|pps : platinum,"
                        + "                  1/1   : gp|gps : gold,"
                        + "                  1/10  : sp : silver,"
                        + "                  1/100 : cp."
                        + "2/5 : my = 400/1 : my pp|my pps : my platinum,"
                        + "            20/1 : my gp|my gps : my gold,"
                        + "             1/1 : my sp : my silber."
                        + "1/2 : guru = 1/1 : guru|gurus", 4);

      value = value.as(new Rational []
        {
          new Rational(1),
          new Rational(10),
          null,
          new Rational(1)
        }, 0);

      assertEquals("not defined after setting", true, value.isDefined());
      assertEquals("base",      "20 1/100", value.getAsBase().toString());
      assertEquals("base unit", "gps", value.getBaseUnit());
      assertEquals("output", "1 pp 10 gps 1 cp",
                   value.toString());

      assertEquals("convert 0", "1 pp 10 gps 1 cp",
                   value.toSet(0, false).toString());
      assertEquals("convert 1", "8 1/250 my sp",
                   value.toSet(1, false).toString());
      assertEquals("convert double", "10 1/200 gurus",
                   value.toSet(1, false).toSet(2, false).toString());
      assertEquals("convert 0", "2 pps 1 cp",
                   value.toSet(1, false).toSet(0, true).toString());
      assertEquals("convert 1", "8 1/250 my sp",
                   value.toSet(1, true).toString());
      assertEquals("convert double", "10 1/200 gurus",
                   value.toSet(1, false).toSet(2, true).toString());
      assertEquals("output", "\\window{\\span{unit}{1 pp} "
                   + "\\span{unit}{10 gps} \\span{unit}{1 cp}}"
                   + "{\\table{#inline#1:L,,;100:L}"
                   + "{Total:}{\\frac[20]{1}{100} gps}"
                   + "{}{}{my:}{\\span{unit}{\\frac[8]{1}{250} my sp}}"
                   + "{guru:}{\\span{unit}{\\frac[10]{1}{200} gurus}}}",
                   value.format(false).toString());
    }

    //......................................................................
    //----- read -----------------------------------------------------------

    /** Testing reading. */
    @org.junit.Test
    public void read()
    {
      String []tests =
        {
          "empty", "", null, null,
          "incomplete", "10", null, "10",
          "incomplete 2", "10 d", null, "10 d",
          "incomplete 3", "d 10", null, "d 10",
          "plural", "10gp", "10 gps", null,
          "whites", "10      \n  silver", "10 sp", null,
          "whites 2", "10\ncp", "10 cp", null,
          "sum", "1 gp 2 gp", "3 gps", null,
          "fraction", "1/2 gp", "1/2 gp", null,
          "several", "10 pp 1 gp 5 sp 1/2 cp", "10 pps 1 gp 5 sp 1/2 cp", null,
          "comma", "10 pp 1 gp,", "10 pps 1 gp", ",",
        };

      Value.Test.readTest(tests, new Units(s_sets, 1));
    }

    //......................................................................
    //----- add ------------------------------------------------------------

    /** Testing additions. */
    @org.junit.Test
    @SuppressWarnings("unchecked")
    public void add()
    {
      Units<Units> unit = new Units<Units>(s_sets, 1);

      unit = unit.add(new Units(new Rational []
        { new Rational(1), new Rational(5), null, new Rational(1, 2) }, s_sets,
                                s_sets[0], 1));
      assertEquals("add 1", "1 pp 5 gps 1/2 cp", unit.toString());

      unit = unit.add(new Units(new Rational []
        { new Rational(1), new Rational(5), null, new Rational(1, 2) },
                                s_sets2, s_sets[0], 1));
      assertEquals("add 1", "2 pps 10 gps 1 cp", unit.toString());

      unit = unit.add(new Units(new Rational []
        { new Rational(1, 2, 3) }, s_sets, s_sets[2], 1));
      assertEquals("add 1", "2 pps 13 gps 3 sp 4 1/3 cp", unit.toString());
    }

    //......................................................................
    //----- subtract -------------------------------------------------------

    /** Testing subtractions. */
    @org.junit.Test
    @SuppressWarnings("unchecked")
    public void subtract()
    {
      Units<Units> unit = new Units<Units>(s_sets, 1);

      unit = unit.add(new Units(new Rational []
        { new Rational(1), new Rational(5), null, new Rational(1, 2) }, s_sets,
                                s_sets[0], 1));
      assertEquals("subtract 2", "1 pp 5 gps 1/2 cp", unit.toString());

      unit = unit.subtract(new Units(new Rational []
        { null, new Rational(2, 1, 2), null, null }, s_sets2, s_sets[0], 1));
      assertEquals("subtract 2", "1 pp 2 1/2 gps 1/2 cp", unit.toString());

      unit = unit.subtract(new Units(new Rational []
        { new Rational(1, 2, 3) }, s_sets, s_sets[2], 1));
      assertEquals("subtract 3", "1 pp -1/2 gp -3 sp -2 5/6 cp",
                   unit.toString());

      unit = unit.simplify();
      assertEquals("simplified", "9 gps 1 sp 7 1/6 cp", unit.toString());
    }

    //......................................................................
    //----- multiply -------------------------------------------------------

    /** Testing multiplications. */
    @org.junit.Test
    @SuppressWarnings("unchecked")
    public void multiply()
    {
      Units<Units> value = new Units<Units>(new Rational []
        {
          new Rational(1),
          new Rational(5),
          null,
          new Rational(7)
        }, s_sets, s_sets[0], 4);

      value = value.multiply(2);
      assertEquals("multiply", "2 pps 10 gps 14 cp", value.toString());

      value = value.simplify();
      assertEquals("multiply", "3 pps 1 sp 4 cp", value.toString());

      value = value.multiply(new Rational(1, 3));
      assertEquals("multiply", "1 pp 4 2/3 cp", value.toString());
    }

    //......................................................................
    //----- divide ---------------------------------------------------------

    /** Testing divisions. */
    @org.junit.Test
    @SuppressWarnings("unchecked")
    public void divide()
    {
      Units<Units> value = new Units<Units>(new Rational []
        {
          new Rational(1),
          new Rational(5),
          null,
          new Rational(7)
        }, s_sets, s_sets[0], 4);

      value = value.divide(3);
      assertEquals("divide", "1/3 pp 1 2/3 gps 2 1/3 cp", value.toString());

      value = value.simplify();
      assertEquals("divide", "5 gps 2 1/3 cp", value.toString());

      value = value.divide(new Rational(1, 2));
      assertEquals("divide", "1 pp 4 2/3 cp", value.toString());
    }

    //......................................................................
    //----- compare --------------------------------------------------------

    /** Comparing tests. */
    @org.junit.Test
    public void compare()
    {
      Units<Units> low = new Units<Units>(new Rational []
        {
          new Rational(1),
          new Rational(5),
          null,
          new Rational(7)
        }, s_sets, s_sets[0], 4);

      assertEquals("compare low", 0, low.compareTo(low));

      Units<Units> high = new Units<Units>(new Rational []
        {
          new Rational(1),
          new Rational(5),
          new Rational(1),
          null,
        }, s_sets, s_sets[0], 4);

      assertEquals("compare high", 0, high.compareTo(high));

      assertTrue("compare low - high", low.compareTo(high) < 0);
      assertTrue("compare high - low", high.compareTo(low) > 0);

      Units<Units> set = new Units<Units>(new Rational []
        {
          new Rational(1),
          null,
          null,
        }, s_sets, s_sets[1], 4);

      assertEquals("compare set", 0, set.compareTo(set));

      assertTrue("compare set - low", set.compareTo(low) > 0);
      assertTrue("compare low - set", low.compareTo(set) < 0);
    }

    //......................................................................

    //----- Unit -----------------------------------------------------------

    /** Testing units. */
    @org.junit.Test
    public void unit()
    {
      Unit unit =
        new Unit("sp", "sps", 1, 10, "silver", "silvers", "silver piece",
                 "silver pieces");

      Rational value = new Rational(0);
      assertEquals("undefined",
                   "sp/sps/silver/silvers/silver piece/silver pieces *1/10",
                   unit.toString());
      assertEquals("undefined", "0 sp",
                   unit.format(value).toString());

      value = new Rational(1, 2, 3);

      assertEquals("set",
                   "sp/sps/silver/silvers/silver piece/silver pieces "
                   + "*1/10", unit.toString());
      assertEquals("set", "\\span{unit}{\\frac[1]{2}{3} sps}",
                   unit.format(value).toString());

      value = new Rational(1);

      assertEquals("set",
                   "sp/sps/silver/silvers/silver piece/silver pieces "
                   + "*1/10", unit.toString());
      assertEquals("set", "\\span{unit}{1 sp}",
                   unit.format(value).toString());

      // now a String defined unit
      unit = new Unit("2/3 : test|tests : other|still other|last");

      assertEquals("string defined", "test/tests/other/still other/last *2/3",
                   unit.toString());

      unit = new Unit("2/3 : test : other|still other|last");

      assertEquals("string defined", "test/test/other/still other/last *2/3",
                   unit.toString());

      unit = new Unit("2/3 : test");

      assertEquals("string defined", "test/test *2/3", unit.toString());

      unit = new Unit("2/3 : test : ");

      assertEquals("string defined", "test/test *2/3", unit.toString());
    }

    //......................................................................
    //----- Set ------------------------------------------------------------

    /** Testing setters. */
    @org.junit.Test
    public void set()
    {
      Set set = new Set("standard", 42, 1,
                        new Unit("pp", null, 10, 1, "platinum", "platinums",
                                 "platinum piece", "platinum pieces"),
                        new Unit("gp", null, 1, 1, "gold", "golds",
                                 "gold piece", "gold pieces"),
                        new Unit("sp", null, 1, 10, "silver", "silvers",
                                 "silver piece", "silver pieces"),
                        new Unit("cp", null, 1, 100, "copper", "coppers",
                                 "copper piece", "copper pieces"));

      assertEquals("undefined",
                   "standard (42/1): "
                   + "pp/pp/platinum/platinums/platinum piece/platinum pieces "
                   + "*10/1, "
                   + "gp/gp/gold/golds/gold piece/gold pieces *1/1, "
                   + "sp/sp/silver/silvers/silver piece/silver pieces *1/10, "
                   + "cp/cp/copper/coppers/copper piece/copper pieces *1/100",
                   set.toString());

      // now a String definition
      set = new Set("1/2    : Metric = 1000/1    : t : tons|ton,"
                    + "                     1/1    : kg : kilograms|kilogram,"
                    + "                     1/1000 : g : grams|gram");

      assertEquals("string definition",
                   "Metric (1/2): t/t/tons/ton *1000/1, "
                   + "kg/kg/kilograms/kilogram *1/1, g/g/grams/gram *1/1000",
                   set.toString());
    }

    //......................................................................
  }

  //........................................................................
}