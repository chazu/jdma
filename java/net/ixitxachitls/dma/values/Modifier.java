/******************************************************************************
 * Copyright (c) 2002-2013 Peter 'Merlin' Balsiger and Fredy 'Mythos' Dobler
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


package net.ixitxachitls.dma.values;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Optional;

import net.ixitxachitls.dma.entries.ValueGroup;
import net.ixitxachitls.dma.proto.Values.ModifierProto;
import net.ixitxachitls.dma.values.enums.Named;
import net.ixitxachitls.util.Strings;

/**
 * A modifier value.
 *
 * @file   NewModifier.java
 * @author balsiger@ixitxachitls.net (Peter Balsiger)
 *
 */
public class Modifier extends NewValue.Arithmetic<ModifierProto>
{
  /** An interface for stackable objects. */
  public interface Stackable
  {

  }

  /** The modifiers type. */
  public enum Type implements Named
  {
    /** Dodging stuff. */
    DODGE("dodge", true, ModifierProto.Type.DODGE),

    /** Better armor. */
    ARMOR("armor", false, ModifierProto.Type.ARMOR),

    /** Some equipment benefit. */
    EQUIPMENT("equipment", false, ModifierProto.Type.EQUIPMENT),

    /** A shield helping out. */
    SHIELD("shield", false, ModifierProto.Type.SHIELD),

    /** The general or standard type. */
    GENERAL("general", true, ModifierProto.Type.GENERAL),

    /** Modifier for natural armor. */
    NATURAL_ARMOR("natural armor", false, ModifierProto.Type.NATURAL_ARMOR),

    /** A modifier from an ability. */
    ABILITY("ability", true, ModifierProto.Type.ABILITY),

    /** A modifier according to size. */
    SIZE("size", false, ModifierProto.Type.SIZE),

    /** A racial modifier. */
    RACIAL("racial", false, ModifierProto.Type.RACIAL),

    /** Circumstances giving a modifier. */
    CIRCUMSTANCE("circumstance", true, ModifierProto.Type.CIRCUMSTANCE),

    /** A magical enhancement modifier. */
    ENHANCEMENT("enhancement", false, ModifierProto.Type.ENHANCEMENT),

    /** A deflection modifier against attacks. */
    DEFLECTION("deflection", false, ModifierProto.Type.DEFLECTION),

    /** A rage modifier. */
    RAGE("rage", false, ModifierProto.Type.RAGE),

    /** A competence modifier against attacks. */
    COMPETENCE("competence", false, ModifierProto.Type.COMPETENCE);

    /** The value's name. */
    private final String m_name;

    /** Flag if the value stacks with others of its kind. */
    private final boolean m_stacks;

    /** The proto enum value. */
    private final ModifierProto.Type m_proto;

    /** Create the name.
     *
     * @param inName   the name of the value
     * @param inStacks true if this modifier stacks with similar ones, false
     *                 if not
     * @param inProto  the proto enum value
     */
    private Type(String inName, boolean inStacks, ModifierProto.Type inProto)
    {
      m_name = ValueGroup.constant("product.part", inName);
      m_stacks = inStacks;
      m_proto = inProto;
    }

    /** Get the name of the value.
     *
     * @return the name of the value
     *
     */
    @Override
    public String getName()
    {
      return m_name;
    }

    /** Convert to a human readable string.
     *
     * @return the converted string
     *
     */
    @Override
    public String toString()
    {
        return m_name;
    }

    /** Check if this type stacks with similar ones of its kind.
     *
     * @return true if it stacks, false if not
     *
     */
    public boolean stacks()
    {
      return m_stacks;
    }

    /**
     * Get the proto value for this value.
     *
     * @return the proto enum value
     */
    public ModifierProto.Type getProto()
    {
      return m_proto;
    }

    /**
     * Get the group matching the given proto value.
     *
     * @param  inProto     the proto value to look for
     * @return the matched enum (will throw exception if not found)
     */
    public static Type fromProto(ModifierProto.Type inProto)
    {
      for(Type type : values())
        if(type.m_proto == inProto)
          return type;

      throw new IllegalStateException("invalid proto type: " + inProto);
    }

    /**
     * Get the armor type from the given string.
     *
     * @param inValue the string representation
     * @return the matching type, if any
     */
    public static Optional<Type> fromString(String inValue)
    {
      for(Type type : values())
        if(type.getName().equalsIgnoreCase(inValue))
          return Optional.of(type);

      return Optional.absent();
    }

    /**
     * Get the possible names of types.
     *
     * @return a list of the namees
     */
    public static List<String> names()
    {
      List<String> names = new ArrayList<>();
      for(Type type : values())
        names.add(type.getName());

      return names;
    }
  }

  public static final Parser<Modifier> PARSER = new Parser<Modifier>(0)
  {
    @Override
    protected Optional<Modifier> doParse(String ... inValues)
    {
      return parse(Strings.COMMA_SPLITTER.splitToList
                   (Strings.COMMA_JOINER.join(inValues)));
    }

    private Optional<Modifier> parse(List<String> inValues)
    {
      List<String> values = new ArrayList<>(inValues);
      Collections.reverse(values);
      Optional<Modifier> result = Optional.absent();
      for(String value : values)
      {
        String []parts =
          Strings.getPatterns(value,
                              "^([+-]\\d+)\\s*(" + types + ")?\\s*"
                              + "(?: if\\s+(.*))?$");
        if(parts == null || parts.length == 0)
          return Optional.absent();

        try
        {
          int modifier = Integer.parseInt(parts[0]);
          Type type;
          if(parts[1] == null)
            type = Type.GENERAL;
          else
            type = Type.fromString(parts[1]).get();

          Optional<String> condition = Optional.fromNullable(parts[2]);

          result =
            Optional.of(new Modifier(modifier, type, condition, result));
        }
        catch(NumberFormatException e)
        {
          return Optional.absent();
        }
      }

      return result;
    }
  };

  public Modifier()
  {
    this(0, Type.GENERAL, Optional.<String>absent(),
         Optional.<Modifier>absent());
  }

  public Modifier(int inModifier, Type inType, Optional<String> inCondition,
                  Optional<Modifier> inNext)
  {
    m_modifier = inModifier;
    m_type = inType;
    m_condition = inCondition;
    m_next = inNext;
  }

  private static final String types = Strings.PIPE_JOINER.join(Type.names());

  /** The modifier value itself. */
  private final int m_modifier;

  /** The type of the modifier. */
  private final Type m_type;

  /** The default type, if any. */
  private final Type m_defaultType = Type.GENERAL;

  /** The condition for the modifier, if any. */
  private final Optional<String> m_condition;

  /** A next modifier, if any. */
  private final Optional<Modifier> m_next;

  /**
   * Get the value of the modifier, ignoring additional modifiers.
   *
   * @return      the requested valu
   */
  public int getModifier()
  {
    return m_modifier;
  }

  /**
   * Get the type of the modifier.
   *
   * @return the modifier type
   */
  public Type getType()
  {
    return m_type;
  }

  /**
   * Get the condition of the modifier, if any.
   *
   * @return the condition
   */
  public Optional<String> getCondition()
  {
    return m_condition;
  }

  /**
   * Get the next modifier if there are chained modifiers.
   *
   * @return the next modifier
   */
  public Optional<Modifier> getNext()
  {
    return m_next;
  }

  @Override
  public String toString()
  {
    StringBuilder result = new StringBuilder();
    if(m_modifier >= 0)
      result.append("+");

    result.append(m_modifier);

    if(m_type != Type.GENERAL)
      result.append(" " + m_type);

    if(m_condition.isPresent())
      result.append(" if " + m_condition.get());

    if(m_next.isPresent())
      result.append(", " + m_next.get());

    return result.toString();
  }

  @Override
  public ModifierProto toProto()
  {
    ModifierProto.Builder builder = ModifierProto.newBuilder();
    addToProto(builder);

    return builder.build();
  }

    /**
   * Add the values of this modifier to the given proto.
   *
   * @param inBuilder the builder to fill
   */
  private void addToProto(ModifierProto.Builder inBuilder)
  {
    ModifierProto.Modifier.Builder modifier =
      ModifierProto.Modifier.newBuilder();

    modifier.setBaseValue(m_modifier);
    modifier.setType(m_type.getProto());
    if (m_condition.isPresent())
      modifier.setCondition(m_condition.get());

    inBuilder.addModifier(modifier.build());

    if(m_next.isPresent())
      m_next.get().addToProto(inBuilder);
   }

  @Override
  public NewValue.Arithmetic<ModifierProto>
    add(NewValue.Arithmetic<ModifierProto> inValue)
  {
    if(!(inValue instanceof Modifier))
      return this;

    Modifier value = (Modifier)inValue;
    if(m_type == value.m_type && m_condition.equals(value.m_condition))
    {
      Optional<Modifier> next;
      if(!m_next.isPresent())
        next = value.m_next;
      else if(!value.m_next.isPresent())
        next = m_next;
      else
        next = Optional.of((Modifier)m_next.get().add(value.m_next.get()));

      if(m_type.stacks())
        return new Modifier(m_modifier + value.m_modifier, m_type,
                               m_condition, next);
      else
        return new Modifier(Math.max(m_modifier, value.m_modifier), m_type,
                               m_condition, next);
    }

    if(!m_next.isPresent())
      return new Modifier(m_modifier, m_type, m_condition,
                             Optional.of(value));

    return new Modifier(m_modifier, m_type, m_condition,
                             Optional.of((Modifier)m_next.get().add(value)));
  }

  @Override
  public NewValue.Arithmetic<ModifierProto> multiply(int inFactor)
  {
    return new Modifier(m_modifier * inFactor, m_type, m_condition,
                           m_next.isPresent()
                             ? Optional.of((Modifier)
                                           m_next.get().multiply(inFactor))
                             : m_next);
  }

  /**
   * Create a new modifier with the values from the given proto.
   *
   * @param inProto the proto to read the values from
   * @return the newly created critical
   */
  public static Modifier fromProto(ModifierProto inProto)
  {
    Modifier result = null;
    List<ModifierProto.Modifier> modifiers =
      new ArrayList<>(inProto.getModifierList());
    Collections.reverse(modifiers);
    for(ModifierProto.Modifier modifier : modifiers)
    {
      result = new Modifier(modifier.getBaseValue(),
                               Type.fromProto(modifier.getType()),
                               modifier.hasCondition()
                                 ? Optional.of(modifier.getCondition())
                                 : Optional.<String>absent(),
                               Optional.fromNullable(result));
    }

    return result;
  }

  @Override
  public boolean canAdd(NewValue.Arithmetic<ModifierProto> inValue)
  {
    return inValue instanceof Modifier;
  }

  //----------------------------------------------------------------------------

  /** The test. */
  public static class Test extends net.ixitxachitls.util.test.TestCase
  {
    /** Testing init. */
    @org.junit.Test
    public void parse()
    {
      assertEquals("parse", "None", PARSER.parse(" none  ").get().toString());
      assertEquals("parse", "x3", PARSER.parse(" x 3  ").get().toString());
      assertEquals("parse", "x3", PARSER.parse(" 20/x3  ").get().toString());
      assertEquals("parse", "19-20/x2",
                   PARSER.parse(" 19 - 20 / x 2 ").get().toString());
      assertEquals("parse", "12-19/x5",
                   PARSER.parse("12-19/x5").get().toString());
      assertFalse("parse", PARSER.parse("/").isPresent());
      assertFalse("parse", PARSER.parse("").isPresent());
      assertFalse("parse", PARSER.parse("12").isPresent());
      assertFalse("parse", PARSER.parse("x").isPresent());
      assertFalse("parse", PARSER.parse("19-20 x3").isPresent());
      assertFalse("parse", PARSER.parse("19 - x 4").isPresent());
    }
  }
}
