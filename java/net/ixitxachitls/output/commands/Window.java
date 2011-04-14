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

package net.ixitxachitls.output.commands;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import net.ixitxachitls.util.configuration.Config;

//..........................................................................

//------------------------------------------------------------------- header

/**
 * The window command.
 *
 * @file          Window.java
 *
 * @author        balsiger@ixitxachitls.net (Peter Balsiger)
 *
 */

//..........................................................................

//__________________________________________________________________________

@Immutable
public class Window extends BaseCommand
{
  //--------------------------------------------------------- constructor(s)

  //-------------------------------- Window --------------------------------

  /**
   * The constructor for the window command.
   *
   * @param       inText   the text to set the window around
   * @param       inWindow the text in the window itself
   *
   */
  public Window(@Nonnull Object inText, @Nonnull Object inWindow)
  {
    this();

    withArguments(inText, inWindow);
  }

  //........................................................................
  //-------------------------------- Window --------------------------------

  /**
   * The constructor for the window command.
   *
   * @param       inText   the text to set the window around
   * @param       inWindow the text in the window itself
   * @param       inASCII  the text for ascii printing
   *
   */
  public Window(@Nonnull Object inText, @Nonnull Object inWindow,
                @Nonnull Object inASCII)
  {
    this(inText, inWindow);

    withOptionals(inASCII);
  }

  //........................................................................
  //-------------------------------- Window --------------------------------

  /**
   * This is the internal constructor for a command.
   *
   */
  protected Window()
  {
    super(NAME, 1, 2);
  }

  //........................................................................

  //........................................................................

  //-------------------------------------------------------------- variables

  /** Command for adding a window with additional text. */
  public static final String NAME =
    Config.get("resource:commands/window", "window");

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
    public void testArguments()
    {
      Command command = new Window("text", "window");
      assertEquals("setup", "\\window{text}{window}", command.toString());

      command = new Window("text", "window", "ascii");
      assertEquals("setup", "\\window[ascii]{text}{window}",
                   command.toString());
    }

    //......................................................................
  }

  //........................................................................
}
