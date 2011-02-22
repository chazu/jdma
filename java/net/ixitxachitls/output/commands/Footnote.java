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

package net.ixitxachitls.output.commands;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import net.ixitxachitls.util.configuration.Config;

//..........................................................................

//------------------------------------------------------------------- header

/**
 * The footnote command.
 *
 * @file          Footnote.java
 *
 * @author        balsiger@ixitxachitls.net (Peter Balsiger)
 *
 */

//..........................................................................

//__________________________________________________________________________

@Immutable
public class Footnote extends BaseCommand
{
  //--------------------------------------------------------- constructor(s)

  //------------------------------- Footnote -------------------------------

  /**
   * The constructor for the footnote command.
   *
   * @param       inText the text to set footnote
   *
   */
  public Footnote(@Nonnull Object inText)
  {
    this();

    withArguments(inText);
  }

  //........................................................................
  //------------------------------- Footnote -------------------------------

  /**
   * The constructor for the footnote command.
   *
   * @param       inMark the mark to use for setting this footnote
   * @param       inText the text to set footnote
   *
   */
  public Footnote(@Nonnull Object inMark, @Nonnull Object inText)
  {
    this(inText);

    withOptionals(inMark);
  }

  //........................................................................
  //------------------------------- Footnote -------------------------------

  /**
   * This is the internal constructor for a command.
   *
   */
  protected Footnote()
  {
    super(FOOTNOTE, 1, 1);
  }

  //........................................................................

  //........................................................................

  //-------------------------------------------------------------- variables

  /** Command for inserting a paragraph break. */
  public static final String FOOTNOTE =
    Config.get("resource:commands/footnote", "footnote");

  //........................................................................

  //-------------------------------------------------------------- accessors
  //........................................................................

  //----------------------------------------------------------- manipulators
  //........................................................................

  //------------------------------------------------- other member functions
  //........................................................................

  //------------------------------------------------------------------- test

  /** The test. */
  public static class Test extends net.ixitxachitls.util.test.TestCase
  {
    //----- arguments ------------------------------------------------------

    /** Testing arguments. */
    @org.junit.Test
    public void arguments()
    {
      Command command = new Footnote("text");
      assertEquals("setup", "\\footnote{text}", command.toString());

      command = new Footnote("mark", "text");
      assertEquals("setup", "\\footnote[mark]{text}", command.toString());
    }

    //......................................................................
  }

  //........................................................................
}
