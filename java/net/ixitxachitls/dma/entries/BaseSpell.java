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

package net.ixitxachitls.dma.entries;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.collect.Multimap;

import net.ixitxachitls.dma.entries.extensions.BaseIncomplete;
import net.ixitxachitls.dma.entries.indexes.Index;
import net.ixitxachitls.dma.values.Distance;
import net.ixitxachitls.dma.values.Duration;
import net.ixitxachitls.dma.values.EnumSelection;
import net.ixitxachitls.dma.values.LongFormattedText;
import net.ixitxachitls.dma.values.Multiple;
import net.ixitxachitls.dma.values.Name;
import net.ixitxachitls.dma.values.Number;
import net.ixitxachitls.dma.values.Parameters;
import net.ixitxachitls.dma.values.Selection;
import net.ixitxachitls.dma.values.Text;
import net.ixitxachitls.dma.values.Union;
import net.ixitxachitls.dma.values.Value;
import net.ixitxachitls.dma.values.ValueList;
import net.ixitxachitls.input.ParseReader;
import net.ixitxachitls.util.configuration.Config;
import net.ixitxachitls.util.logging.Log;

//..........................................................................

//------------------------------------------------------------------- header

/**
 * This is the basic jDMA base spell.
 *
 * @file          BaseSpell.java
 *
 * @author        balsiger@ixitxachitls.net (Peter 'Merlin' Balsiger)
 */

//..........................................................................

//__________________________________________________________________________

@ParametersAreNonnullByDefault
public class BaseSpell extends BaseEntry
{
  //----------------------------------------------------------------- nested

  //----- schools ----------------------------------------------------------

  /** The possible spell schools (cf. PHB 172/173). */
  public enum School implements EnumSelection.Named, EnumSelection.Short
  {
    /** Abjuration. */
    ABJURATION("Abjuration", "Abjur"),

    /** Conjuration. */
    CONJURATION("Conjuration", "Conj"),

    /** Divination. */
    DIVINATION("Divination", "Div"),

    /** Enchantment. */
    ENCHANTMENT("Enchantment", "Ench"),

    /** Evocation. */
    EVOCATION("Evocation", "Evoc"),

    /** Illusion. */
    ILLUSION("Illusion", "Illus"),

    /** Necromancy. */
    NECROMANCY("Necromancy", "Necro"),

    /** Transmutation. */
    TRANSMUTATION("Transmutation", "Trans"),

    /** Universal. */
    UNIVERSAL("Universal", "Univ");

    /** The value's name. */
    private String m_name;

    /** The value's short name. */
    private String m_short;

    /** Create the name.
     *
     * @param inName       the name of the value
     * @param inShort      the short name of the value
     *
     */
    private School(String inName, String inShort)
    {
      m_name = constant("school.name", inName);
      m_short = constant("school.short", inShort);
    }

    /** Get the name of the value.
     *
     * @return the name of the value
     *
     */
    public String getName()
    {
      return m_name;
    }

    /** Get the name of the value.
     *
     * @return the short name of the value
     *
     */
    public String getShort()
    {
      return m_short;
    }

    /** Get the save as string.
     *
     * @return the name of the value
     *
     */
    public String toString()
    {
      return m_name;
    }
  }

  //........................................................................
  //----- subschools -------------------------------------------------------

  /** The possible spell schools (cf. PHB 172/173). */
  public enum Subschool implements EnumSelection.Named
  {
    /** None. */
    NONE("None"),

    /** Calling. */
    CALLING("Calling"),

    /** Creation or Calling. */
    CREATION_OR_CALLING("Creation or Calling"),

    /** Creation. */
    CREATION("Creation"),

    /** Healing. */
    HEALING("Healing"),

    /** Summoning. */
    SUMMONING("Summoning"),

    /** Teleportation. */
    TELEPORTATION("Teleportation"),

    /** Scrying. */
    SCRYING("Scrying"),

    /** Charmn. */
    CHARM("Charm"),

    /** Compulsion. */
    COMPULSION("Compulsion"),

    /** Figment or Glamer. */
    FIGMENT_OR_GLAMER("Figment or Glamer"),

    /** Figment. */
    FIGMENT("Figment"),

    /** Glamer. */
    GLAMER("Glamer"),

    /** A figment. */
    FIGMENT_GLAMER("Figment, Glamer"),

    /** Pattern. */
    PATTERN("Pattern"),

    /** Phantasm. */
    PHANTASM("Phantasm"),

    /** Shadow. */
    SHADOW("Shadow");

    /** The value's name. */
    private String m_name;

    /** Create the name.
     *
     * @param inName       the name of the value
     *
     */
    private Subschool(String inName)
    {
      m_name = constant("subschool.name", inName);
    }

    /** Get the name of the value.
     *
     * @return the name of the value
     *
     */
    public String getName()
    {
      return m_name;
    }

    /** Get the save as string.
     *
     * @return the name of the value
     *
     */
    public String toString()
    {
      return m_name;
    }
  }

  //........................................................................
  //----- descriptors ------------------------------------------------------

  /** The possible spell descriptors. */
  public enum Descriptor implements EnumSelection.Named
  {
    /** Acid. */
    ACID("Acid"),

    /** Air. */
    AIR("Air"),

    /** Chaotic. */
    CHAOTIC("Chaotic"),

    /** Cold. */
    COLD("Cold"),

    /** Darkness. */
    DARKNESS("Darkness"),

    /** Death. */
    DEATH("Death"),

    /** Earth. */
    EARTH("Earth"),

    /** Electricity. */
    ELECTRICITY("Electricity"),

    /** Evil. */
    EVIL("Evil"),

    /** Fear. */
    FEAR("Fear"),

    /** Fire or Cold. */
    FIRE_OR_COLD("Fire or Cold"),

    /** Fire. */
    FIRE("Fire"),

    /** Force. */
    FORCE("Force"),

    /** Good. */
    GOOD("Good"),

    /** Language-dependent. */
    LANGUAGE_DEPENDENT("Language-dependent"),

    /** Lawful. */
    LAWFUL("Lawful"),

    /** Light. */
    LIGHT("Light"),

    /** Mind-affecting. */
    MIND_AFFECTING("Mind-affecting"),

    /** Scrying. */
    SCRYING("Scrying"),

    /** Sonic. */
    SONIC("Sonic"),

    /** Water. */
    WATER("Water"),

    /** See Text. */
    SEE_TEXT("See Text");

    /** The value's name. */
    private String m_name;

    /** The value's short name. */
    private String m_short;

    /** Create the name.
     *
     * @param inName       the name of the value
     *
     */
    private Descriptor(String inName)
    {
      m_name = constant("descriptor.name", inName);
    }

    /** Get the name of the value.
     *
     * @return the name of the value
     *
     */
    public String getName()
    {
      return m_name;
    }

    /** Get the save as string.
     *
     * @return the name of the value
     *
     */
    public String toString()
    {
      return m_name;
    }
  }

  //........................................................................
  //----- spell class ------------------------------------------------------

  /** The possible spell classes. */
  public enum SpellClass implements EnumSelection.Named, EnumSelection.Short
  {
    /** Assassin. */
    ASSASSIN("Assassin", "Asn"),

    /** Bard. */
    BARD("Bard", "Brd"),

    /** Clecric. */
    CLERIC("Cleric", "Clr"),

    /** Druid. */
    DRUID("Druid", "Drd"),

    /** Paladin. */
    PALADIN("Paladin", "Pal"),

    /** Ranger. */
    RANGER("Ranger", "Rgr"),

    /** Sorcerer. */
    SORCERER("Sorcerer", "Sor"),

    /** Wizard. */
    WIZARD("Wizard", "Wiz"),

    /** Air. */
    AIR("Air", "Air"),

    /** Animal. */
    ANIMAL("Animal", "Animal"),

    /** Chaos. */
    CHAOS("Chaos", "Chaos"),

    /** Death. */
    DEATH("Death", "Death"),

    /** Destruction. */
    DESTRUCTION("Destruction", "Destruction"),

    /** Drow. */
    DROW("Drow", "Drow"),

    /** Earth. */
    EARTH("Earth", "Earth"),

    /** Evil. */
    EVIL("Evil", "Evil"),

    /** Fire. */
    FIRE("Fire", "Fire"),

    /** Good. */
    GOOD("Good", "Good"),

    /** Healing. */
    HEALING("Healing", "Healing"),

    /** Knowledge. */
    KNOWLEDGE("Knowledge", "Knowledge"),

    /** Law. */
    LAW("Law", "Law"),

    /** Luck. */
    LUCK("Luck", "Luck"),

    /** Magic. */
    MAGIC("Magic", "Magic"),

    /** Plant. */
    PLANT("Plant", "Plant"),

    /** Protection. */
    PROTECTION("Protection", "Protection"),

    /** Strength. */
    STRENGTH("Strength", "Strength"),

    /** Sun. */
    SUN("Sun", "Sun"),

    /** Travel. */
    TRAVEL("Travel", "Travel"),

    /** Trickery. */
    TRICKERY("Trickery", "Trickery"),

    /** War. */
    WAR("War", "War"),

    /** Water. */
    Water("Water", "Water");

    /** The value's name. */
    private String m_name;

    /** The value's short name. */
    private String m_short;

    /** Create the name.
     *
     * @param inName       the name of the value
     * @param inShort      the short name of the value
     *
     */
    private SpellClass(String inName, String inShort)
    {
      m_name = constant("spellclass.name", inName);
      m_short = constant("spellclass.short", inShort);
    }

    /** Get the name of the value.
     *
     * @return the name of the value
     *
     */
    public String getName()
    {
      return m_name;
    }

    /** Get the name of the value.
     *
     * @return the short name of the value
     *
     */
    public String getShort()
    {
      return m_short;
    }

    /** Get the save as string.
     *
     * @return the name of the value
     *
     */
    public String toString()
    {
      return m_name;
    }
  }

  //........................................................................
  //----- components -------------------------------------------------------

  /** The possible spell components. */
  public enum Components implements EnumSelection.Named, EnumSelection.Short
  {
    /** Verbose. */
    VERBOSE("Verbose", "V"),

    /** Somatic. */
    SOMATIC("Somatic", "S"),

    /** Material/Divine Focus. */
    MATERIAL_DEVINE_FOCUS("Material/Divine Focus", "M/DF"),

    /** Material. */
    MATERIAL("Material", "M"),

    /** Focus/Divine Focus. */
    FOCUS_DIVINE_FOCUS("Focus/Divine Focus", "F/DF"),

    /** Focus. */
    FOCUS("Focus", "F"),

    /** Divine Focus. */
    DIVINE_FOCUS("Divine Focus", "DF"),

    /** Experience points. */
    EXPERIENCE_POINTS("Experience Points", "XP");

    /** The value's name. */
    private String m_name;

    /** The value's short name. */
    private String m_short;

    /** Create the name.
     *
     * @param inName       the name of the value
     * @param inShort      the short name of the value
     *
     */
    private Components(String inName, String inShort)
    {
      m_name = constant("school.name", inName);
      m_short = constant("school.short", inShort);
    }

    /** Get the name of the value.
     *
     * @return the name of the value
     *
     */
    public String getName()
    {
      return m_name;
    }

    /** Get the name of the value.
     *
     * @return the short name of the value
     *
     */
    public String getShort()
    {
      return m_short;
    }

    /** Get the save as string.
     *
     * @return the name of the value
     *
     */
    public String toString()
    {
      return m_name;
    }
  }

  //........................................................................
  //----- range ------------------------------------------------------------

  /** The possible spell ranges. */
  public enum Range implements EnumSelection.Named
  {
    /** Personal or Touch. */
    PERSONAL_OR_TOUCH("Personal or Touch"),

    /** Personal and Touch. */
    PERSONAL_AND_TOUCH("Personal and Touch"),

    /** Personal or Close. */
    PERSONAL_OR_CLOSE("Personal or Close"),

    /** Personal. */
    PERSONAL("Personal"),

    /** Touch. */
    TOUCH("Touch"),

    /** Close. */
    CLOSE("Close"),

    /** Medium. */
    MEDIUM("Medium"),

    /** Long. */
    LONG("Long"),

    /** Unlimited. */
    UNLIMITED("Unlimited"),

    /** 40 ft/level. */
    FOURTY_FEET_PER_LEVEL("40 ft/level"),

    /** See Text. */
    SEE_TEXT("See Text"),

    /** Anywhere within the area to be warded. */
    ANYWHERE_WITHIN_AREA_WARDED("Anywhere within the area to be warded"),

    /** Up to 10 ft/level. */
    UP_TO_TEN_FEET_PER_LEVEL("Up to 10 ft/level"),

    /** 1 mile/level. */
    ONE_MILE_PER_LEVEL("1 mile/level");

    /** The value's name. */
    private String m_name;

    /** Create the name.
     *
     * @param inName       the name of the value
     *
     */
    private Range(String inName)
    {
      m_name = constant("range.name", inName);
    }

    /** Get the name of the value.
     *
     * @return the name of the value
     *
     */
    public String getName()
    {
      return m_name;
    }

    /** Get the save as string.
     *
     * @return the name of the value
     *
     */
    public String toString()
    {
      return m_name;
    }
  }

  //........................................................................
  //----- effect -----------------------------------------------------------

  /** The possible spell ranges. */
  public enum Effect implements EnumSelection.Named
  {
    /** Ray. */
    RAY("Ray"),

    /** Spread. */
    SPREAD("Spread");

    /** The value's name. */
    private String m_name;

    /** Create the name.
     *
     * @param inName       the name of the value
     *
     */
    private Effect(String inName)
    {
      m_name = constant("effect.name", inName);
    }

    /** Get the name of the value.
     *
     * @return the name of the value
     *
     */
    public String getName()
    {
      return m_name;
    }

    /** Get the save as string.
     *
     * @return the name of the value
     *
     */
    public String toString()
    {
      return m_name;
    }
  }

  //........................................................................
  //----- spell durations --------------------------------------------------

  /** The possible spell durations (cf. PHB 176). */
  static final String []SPELL_DURATIONS =
    Config.get("/game/spell.durations", new String []
      {
        "Instantaneous or concentration (up to 1 round/level)",
        "Instantaneous or 1 round/level",
        "Instantaneous (1 round)",
        "Instantaneous (1d4 rounds)",
        "Instantaneous/1 hour",
        "Instantaneous",
        "Permanent until Discharged",
        "Permanent until triggered, then 1 round/level",
        "Permanent or until discharged until released or 1d4 days + one "
        + "day/level",
        "Permanent",
        "Concentration (up to 1 round/level) or instantaneous",
        "Concentration up to 1 round/level",
        "Concentration + 1 round/level",
        "Concentration + 1 hour/level",
        "Concentration up to 1 min/level",
        "Concentration up to 10 min/level",
        "Concentration + 2 rounds",
        "Concentration + 3 rounds",
        "Concentration (maximum 10 rounds)",
        "Concentration",
        "Discharge",
        "Indefinite",
        "See Text",
        "4d12 hours",
        "5 rounds or less",
        "One hour or less",
        "One round per three levels",
        "One hour/level or until discharged",
        "One round/level or One round",
        "Until landing or 1 round/level",
        "10 min/level or until used",
        "10 min/level or until discharged",
        "One day/level or until discharged",
        "1d6+2 rounds",
        "One minute or until discharged",
        "One hour plus 12 hours",
        "2d4 rounds",
        "Sixty days or until discharged",
        "One hour/level or until you return to your body",
        "30 minutes or until discharged",
        "One round + 1 round per three levels",
        "30 minutes and 2d6 rounds",
        "1 round/level (D) and concentration + 3 rounds",
        "1 hour/caster level or until discharged, then 1 round/caster level",
        "1d4+1 rounds (apparent time)",
        "1d4+1 rounds",
        "One Usage per two levels",
        "1 round/level or 1 round",
        "Seven days or seven months",
        "Until triggered or broken",
        "Until expended or 10 min/level",
        "1 hour/level or until completed",
        "1 hour/level or until expended",
        "1 round/level or until all beams are exhausted",
        "Up to 1 round/level",
        "No more than 1 hour/level or until discharged (destination is "
        + "reached)",
     });

  //........................................................................
  //----- levels -----------------------------------------------------------

  /** The possible level measurements. */
  static final String []LEVELS =
    Config.get("/game/levels", new String []
      {
        "level",
        "2 level",
        "3 level",
      });

  //........................................................................
  //----- spell durations --------------------------------------------------

  /** The possible spell duration. */
  static final String []SPELL_DURATION_FLAGS =
    Config.get("/game/spell.duration.flags", new String []
      {
        "(D)",
      });

  //........................................................................
  //----- saving throws ----------------------------------------------------

  /** The possible spell saving throws (PHB p. 176/177). */
  static final String []SAVING_THROWS =
    Config.get("/game/saving.throws", new String []
      {
        "Negates",
        "Partial",
        "Half",
        "None and Will Negates (Object)",
        "None or Will Negates (Harmless, Object)",
        "None or Will Negates (Harmless)",
        "None or Will Negates (Object)",
        "None or Will Negates",
        "None or Reflex Half",
        "None or Will disbelief (if interacted with)",
        "None",
        "No and Will Negates (Harmless)",
        "No",
        "Disbelief",
        "(Object)",
        "(Harmless)",
        "Will Disbelief (if interacted with) then Fortitude Partial",
        "Will Disbelief (if interacted with)",
        "Will Half (Harmless) or Will Half",
        "Will Half (Harmless)",
        "Will Half",
        "Will Negates (Harmless) or Will Negates (Harmless, Object)",
        "Will Negates (Harmless) or Will Negates (Object)",
        "Will Negates (Harmless, Object)",
        "Will Negates (Harmless)",
        "Will Negates (Object) Will Negates (Object) or Fortitude Half",
        "Will Negates (Object) or None",
        "Will Negates (Object)",
        "Will Negates (Blinding Only)",
        "Will Negates or Fortitude Negates",
        "Will Negates or None (Object)",
        "Will Negates or Will",
        "Will Negates",
        "Will Partial",
        "Fortitude Negates, Will Partial",
        "Fortitude Negates (Harmless)",
        "Fortitude Negates (Object)",
        "Fortitude Negates",
        "Fortitude Half",
        "Fortitude Partial or Will Negates",
        "Fortitude Partial or Reflex Negates (Object)",
        "Fortitude Partial (Object)",
        "Fortitude Partial",
        "Reflex Half or Reflex Negates",
        "Reflex Half",
        "Reflex Partial",
        "Reflex Negates (Object)",
        "Reflex Negates and Reflex Half",
        "Reflex Negates",
        "See Text",
      });

  //........................................................................
  //----- spell resistance -------------------------------------------------

  /** The possible spell resistances. */
  static final String []SPELL_RESISTANCES =
    Config.get("/game/spell.resistance", new String []
      {
        "No and Yes (Object)",
        "No and Yes (Harmless)",
        "No and Yes",
        "No or Yes (Harmless)",
        "No or Yes (Object)",
        "No (object) and Yes",
        "No (harmless)",
        "No",
        "Yes or No (Object)",
        "Yes or No",
        "Yes (harmless) or Yes (Harmless, Object)",
        "Yes (harmless) or Yes",
        "Yes (harmless)",
        "Yes (object)",
        "Yes (harmless, object)",
        "Yes",
        "See Text",
      });

  //........................................................................

  //........................................................................

  //--------------------------------------------------------- constructor(s)

  //------------------------------ BaseSpell -------------------------------

  /**
   * This is the internal, default constructor for an undefined value.
   *
   */
  protected BaseSpell()
  {
    super(TYPE);
  }

  //........................................................................
  //------------------------------ BaseSpell -------------------------------

  /**
   * This is the normal constructor.
   *
   * @param       inName the name of the base item
   */
  public BaseSpell(String inName)
  {
    super(inName, TYPE);
  }

  //........................................................................

  //........................................................................

  //-------------------------------------------------------------- variables

  /** The type of this entry. */
  public static final BaseType<BaseSpell> TYPE =
    new BaseType<BaseSpell>(BaseSpell.class);

  //----- school -----------------------------------------------------------

  /** The total standard value of the base item. */
  @Key("school")
  protected Multiple m_school = new Multiple(new Multiple.Element []
    {
      new Multiple.Element(new EnumSelection<School>(School.class), false),
      new Multiple.Element(new ValueList<EnumSelection<Subschool>>
                           (new EnumSelection<Subschool>(Subschool.class), ","),
                           true, " (", ")"),
    });

  static
  {
    addIndex(new Index(Index.Path.SCHOOLS, "Schools", TYPE));
  }

  //........................................................................
  //----- summary ----------------------------------------------------------

  /** The summary text for the spell. */
  @Key("summary")
  protected LongFormattedText m_summary = new LongFormattedText();

  //........................................................................
  //----- descriptor -------------------------------------------------------

  /** The spell descriptor. */
  @Key("descriptor")
  protected ValueList<EnumSelection<Descriptor>> m_descriptor =
    new ValueList<EnumSelection<Descriptor>>
    (new EnumSelection<Descriptor>(Descriptor.class), ",");

  static
  {
    addIndex(new Index(Index.Path.DESCRIPTORS, "Descriptors", TYPE));
  }

  //........................................................................
  //----- level ------------------------------------------------------------

  /** The spell level. */
  @Key("level")
  protected ValueList<Multiple> m_level =
    new ValueList<Multiple>(new Multiple(new Multiple.Element []
      {
        new Multiple.Element
        (new EnumSelection<SpellClass>(SpellClass.class), false),
        new Multiple.Element(new Number(0, 9), false),
      }), ", ");

  static
  {
    addIndex(new Index(Index.Path.CLASSES, "Classes", TYPE));
    addIndex(new Index(Index.Path.LEVELS, "Levels", TYPE));
  }

  //........................................................................
  //----- components -------------------------------------------------------

  /** The various components. */
  @Key("components")
  protected ValueList<EnumSelection<Components>> m_components =
    new ValueList<EnumSelection<Components>>
    (new EnumSelection<Components>(Components.class), ", ");

  static
  {
    addIndex(new Index(Index.Path.COMPONENTS, "Components", TYPE));
  }

  //........................................................................
  //----- material ---------------------------------------------------------

  /** The specific material components. */
  @Key("material")
  protected ValueList<Multiple> m_material =
    new ValueList<Multiple>(new Multiple(new Multiple.Element []
      {
        new Multiple.Element(new Name(), false, null, ":"),
        new Multiple.Element(new ValueList<Text>(new Text(), ", "), false),
      }), ", ");

  static
  {
    addIndex(new Index(Index.Path.MATERIALS, "Materials", TYPE));
  }

  //........................................................................
  //----- focus ------------------------------------------------------------

  /** The specific material components. */
  @Key("focus")
  protected Multiple m_focus = new Multiple(new Multiple.Element []
    {
      new Multiple.Element(new Name(), false, null, ":"),
      new Multiple.Element(new ValueList<Text>(new Text(), ", "), false),
    });

  static
  {
    addIndex(new Index(Index.Path.FOCUSES, "Focuses", TYPE));
  }

  //........................................................................
  //----- casting time -----------------------------------------------------

  /** The casting time required for this spell. */
  @Key("casting time")
  protected Duration m_castingTime = new Duration();

  static
  {
    addIndex(new Index(Index.Path.CASTING_TIMES, "Casting Times", TYPE));
  }

  //........................................................................
  //----- range ------------------------------------------------------------

  /** The range of the spell. */
  @Key("range")
  protected Union m_range =
    new Union(new EnumSelection<Range>(Range.class), new Distance())
    .withEditType("name");

  static
  {
    addIndex(new Index(Index.Path.RANGES, "Ranges", TYPE));
  }

  //........................................................................
  //----- effect -----------------------------------------------------------

  /** The target of the spell. */
  @Key("effect")
  protected Multiple m_effect = new Multiple(new Multiple.Element []
    {
      new Multiple.Element(new Distance(), true),
      new Multiple.Element(new EnumSelection<Effect>(Effect.class), true),
      new Multiple.Element(new Text(), false),
    });

  static
  {
    addIndex(new Index(Index.Path.EFFECT_TYPES, "Effect Types", TYPE));
    addIndex(new Index(Index.Path.EFFECTS, "Effects", TYPE));
  }

  //........................................................................
  //----- target -----------------------------------------------------------

  /** The target of the spell. */
  @Key("target")
  protected Text m_target = new Text();

  //........................................................................
  //----- area -------------------------------------------------------------

  /** The area of the spell. */
  @Key("area")
  protected Text m_area = new Text();

  //........................................................................
  //----- duration ---------------------------------------------------------

  /** The duration of the spell. */
  @Key("duration")
  protected Multiple m_duration = new Multiple(new Multiple.Element []
    {
      new Multiple.Element
      (new Union
       (new Selection(SPELL_DURATIONS),
        new Multiple(new Multiple.Element []
          {
            new Multiple.Element(new Duration(), false),
            new Multiple.Element(new Selection(LEVELS), true, "/", null),
            new Multiple.Element(new Duration(), true, "+", null),
          })), false),
      new Multiple.Element(new Selection(SPELL_DURATION_FLAGS), true),
      new Multiple.Element(new Text(), true),
    }).withEditType("non-empty");

  static
  {
    addIndex(new Index(Index.Path.DURATIONS, "Durations", TYPE));
  }

  //........................................................................
  //----- saving throw -----------------------------------------------------

  /** The saving throw for the spell. */
  @Key("saving throw")
  protected Selection m_savingThrow = new Selection(SAVING_THROWS);

  static
  {
    addIndex(new Index(Index.Path.SAVING_THROWS, "Saving Throws", TYPE));
  }

  //........................................................................
  //----- spell resistance -------------------------------------------------

  /** The spell resistance for the spell. */
  @Key("spell resistance")
  protected Selection m_resistance = new Selection(SPELL_RESISTANCES);

  static
  {
    addIndex(new Index(Index.Path.SPELL_RESISTANCES, "Spell Resistances",
                       TYPE));
  }

  //........................................................................

  static
  {
    extractVariables(BaseSpell.class);
    extractVariables(BaseSpell.class, BaseIncomplete.class);
  }

  //----- special indexes --------------------------------------------------

  static
  {
    addIndex(new Index(Index.Path.WORLDS, "Worlds", TYPE));
    addIndex(new Index(Index.Path.REFERENCES, "References", TYPE));
    addIndex(new Index(Index.Path.EXTENSIONS, "Extensions", TYPE));
  }

  //........................................................................

  //........................................................................

  //-------------------------------------------------------------- accessors

  //------------------------------ getSummary ------------------------------

  /**
   * Get a summary for the entry.
   *
   * @param       inParameters optional parametrs to further specifiy the entry
   *                           (for spells the first argument is the class and
   *                            level and the second is the dc)
   *
   * @return      the string with the summary
   *
   */
  public String getSummary(String ... inParameters)
  {
    SpellClass kind = null;
    int level = 1;
    int dc = 10;

    if(inParameters.length > 0)
    {
      String []words = inParameters[0].split(" ");
      try
      {
        kind = SpellClass.valueOf(words[0]);
      }
      catch(IllegalArgumentException e)
      {
        Log.warning("cannot convert spell class '" + words[0] + ": " + e);
      }

      try
      {
        level = Integer.parseInt(words[1]);
      }
      catch(NumberFormatException e)
      {
        Log.warning("Cannot convert spell level '" + words[1] + "': " + e);
      }
    }

    try
    {
      if(inParameters.length > 1)
        dc = Integer.parseInt(inParameters[1]);
    }
    catch(NumberFormatException e)
    {
      Log.warning("Cannot convert spell dc '" + inParameters[1] + "': " + e);
    }

    return getSummary(kind, level, dc);
  }

  //........................................................................
  //------------------------------ getSummary ------------------------------

  /**
   * Get the summary of the spell description, with appropriate level and
   * other computations done.
   *
   * @param       inKind  the kind of spellcaster
   * @param       inLevel the spell level to compute for
   * @param       inDC    the DC for the spell to compute
   *
   * @return      a string with the summary
   *
   */
  @SuppressWarnings("unchecked")
  public String getSummary(@Nullable SpellClass inKind, int inLevel,
                                    int inDC)
  {
    String summary = m_summary.get();

    if(summary == null)
      return "";

    int level = 0;

    for(Multiple levelData : m_level)
    {
      if(((EnumSelection<SpellClass>)levelData.get(0)).getSelected() == inKind)
      {
        level = (int)((Number)levelData.get(1)).get();

        break;
      }
    }

    int dc = inDC + level;

    // fill in dynamic values and replace expressions
    return computeExpressions
      (summary, new Parameters()
       .with("level", new Number(inLevel, 0, 100), Parameters.Type.ADD)
       .with("dc", new Number(dc, -100, 100), Parameters.Type.MAX));
  }

  //........................................................................
  //------------------------------ getSummary ------------------------------

  /**
   * Get a summary for the entry, using the given parameters.
   *
   * @param       inParameters  the parameters to modify the summary
   *
   * @return      the string with the summary
   */
  public String getSummary(@Nullable Parameters inParameters)
  {
    String summary = m_summary.get();

    if(summary == null || summary.isEmpty())
      return "(no summary)";

    if(inParameters == null || !inParameters.isDefined())
      return summary;

    summary = computeExpressions(summary, inParameters);

    Value<?> notes =
      inParameters != null ? inParameters.getValue("Notes") : null;
    if(notes != null)
      summary += " (" + notes + ")";

    return summary;
  }

  //........................................................................
  //-------------------------- computeIndexValues --------------------------

  /**
   * Get all the values for all the indexes.
   *
   * @return      a multi map of values per index name
   *
   */
  @SuppressWarnings("unchecked")
  @Override
  public Multimap<Index.Path, String> computeIndexValues()
  {
    Multimap<Index.Path, String> values = super.computeIndexValues();

    values.put(Index.Path.SCHOOLS, m_school.get(0).group());
    values.put(Index.Path.DESCRIPTORS, m_descriptor.group());
    values.put(Index.Path.CASTING_TIMES, m_castingTime.group());
    values.put(Index.Path.RANGES, m_range.group());
    values.put(Index.Path.EFFECT_TYPES, m_effect.get(1).group());
    values.put(Index.Path.DURATIONS, m_duration.get(0).group());
    values.put(Index.Path.SAVING_THROWS, m_savingThrow.group());
    values.put(Index.Path.SPELL_RESISTANCES, m_resistance.group());
    values.put(Index.Path.FOCUSES, m_focus.get(0).group());

    for(Multiple level : m_level)
    {
      values.put(Index.Path.LEVELS, level.get(1).group());
      values.put(Index.Path.CLASSES, level.get(0).group());
    }

    for(EnumSelection<Components> component : m_components)
      values.put(Index.Path.COMPONENTS, component.group());

    for(Multiple material : m_material)
      values.put(Index.Path.MATERIALS, material.group());

    for(EnumSelection<Subschool> subschool
          : (ValueList<EnumSelection<Subschool>>)m_school.get(1))
      values.put(Index.Path.SUBSCHOOLS, subschool.group());

    return values;
  }

  //........................................................................
  //--------------------------------- isDM ---------------------------------

  /**
   * Check whether the given user is the DM for this entry. Every user is a DM
   * for a base campaign.
   *
   * @param       inUser the user accessing
   *
   * @return      true for DM, false for not
   *
   */
  @Override
  public boolean isDM(@Nullable BaseCharacter inUser)
  {
    if(inUser == null)
      return false;

    return inUser.hasAccess(BaseCharacter.Group.ADMIN);
  }

  //........................................................................

  //........................................................................

  //----------------------------------------------------------- manipulators
  //........................................................................

  //------------------------------------------------- other member functions
  //........................................................................

  //------------------------------------------------------------------- test

  /** The test. */
  public static class Test extends net.ixitxachitls.util.test.TestCase
  {
    //----- createBaseSpell() ----------------------------------------------

    /** Create a typical base item for testing purposes.
     *
     * @return the newly created base item
     *
     */
    public static AbstractEntry createBaseSpell()
    {
      ParseReader reader =
        new ParseReader(new java.io.StringReader(s_text), "test");

      return BaseSpell.read(reader);
    }

    //......................................................................

    //----- text -----------------------------------------------------------

    /** Test text. */
    private static String s_text =
      "#----- Acid Fog -----------------------------------------------\n"
      + "\n"
      + "base spell Acid Fog =\n"
      + "\n"
      + "  synonyms          \"Fog, Acid\";\n"
      + "  school            Conjuration (Creation);\n"
      + "  descriptor        acid;\n"
      + "  level             Sorcerer 6, Wizard 6, Water 7;\n"
      + "  components        V, S, M/DF;\n"
      + "  casting time      1 standard action;\n"
      + "  range             medium;\n"
      + "  effect            20 ft spread \"Fog\";\n"
      + "  material          combined : \"dried, powdered peas (pinch)\",\n"
      + "                    \"powdered animal hoof\";\n"
      + "  casting time      1 standard action;\n"
      + "  duration          1 round/level;\n"
      + "  saving throw      None;\n"
      + "  spell resistance  No;\n"
      + "  worlds            generic;\n"
      + "  references        WTC 17524: 196;\n"
      + "  short description \"Fog deals acid damage.\";\n"
      + "  description       \n"
      + "\n"
      + "  \"\\Spell{Acid fog} creates a billowing mass of misty vapors "
      + "similar to that\n"
      + "  produced by a \\Spell{solid fog} spell. In addition to slowing "
      + "creatures down\n"
      + "  and obscuring sight, this spell's vapors are highly acidic. Each "
      + "round on\n"
      + "  your turn, starting when you cast the spell, the fog deals 2d6 "
      + "points of acid\n"
      + "  damage to each creature and object within it.\".\n"
      + "\n"
      + "#..............................................................\n";

    //......................................................................
    //----- read -----------------------------------------------------------

    /** Test reading. */
    @org.junit.Test
    public void testRead()
    {
      String result =
        "#----- Acid Fog\n"
        + "\n"
        + "base spell Acid Fog =\n"
        + "\n"
        + "  school            Conjuration (Creation);\n"
        + "  descriptor        Acid;\n"
        + "  level             Sorcerer 6, Wizard 6, Water 7;\n"
        + "  components        Verbose, Somatic, Material/Divine Focus;\n"
        + "  material          combined:\"dried, powdered peas (pinch)\", "
        + "\"powdered animal hoof\";\n"
        + "  casting time      1 standard action;\n"
        + "  range             Medium;\n"
        + "  effect            20 ft Spread \"Fog\";\n"
        + "  duration          1 round/level;\n"
        + "  saving throw      None;\n"
        + "  spell resistance  No;\n"
        + "  worlds            Generic;\n"
        + "  references        WTC 17524: 196;\n"
        + "  description       "
        + "\"\\Spell{Acid fog} creates a billowing mass of misty vapors "
        + "similar to that\n"
        + "                    produced by a \\Spell{solid fog} spell. In "
        + "addition to slowing creatures down\n"
        + "                    and obscuring sight, this spell's vapors are "
        + "highly acidic. Each round on\n"
        + "                    your turn, starting when you cast the spell, "
        + "the fog deals 2d6 points of acid\n"
        + "                    damage to each creature and object within "
        + "it.\";\n"
        + "  short description \"Fog deals acid damage.\";\n"
        + "  synonyms          \"Fog, Acid\";\n"
        + "  name              Acid Fog.\n"
        + "\n"
        + "#.....\n";

      AbstractEntry entry = createBaseSpell();

      assertNotNull("base item should have been read", entry);
      assertEquals("base item name does not match", "Acid Fog",
                   entry.getName());
      assertEquals("base item does not match", result, entry.toString());
    }

    //......................................................................
  }

  //........................................................................
}