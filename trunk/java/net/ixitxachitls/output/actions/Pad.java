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

package net.ixitxachitls.output.actions;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import net.ixitxachitls.output.Document;

//..........................................................................

//------------------------------------------------------------------- header

/**
 * This is a padding action, allow to replace all white spaces with a given
 * character.
 *
 * @file          Pad.java
 *
 * @author        balsiger@ixitxachitls.net (Peter 'Merlin' Balsiger)
 *
 */

//..........................................................................

//__________________________________________________________________________

@Immutable
public class Pad extends Action
{
  //--------------------------------------------------------- constructor(s)

  //----------------------------------- Pad --------------------------------

  /**
   * Construct the action.
   *
   * @param       inPad  the character to use for padding
   *
   */
  public Pad(char inPad)
  {
    m_pad = inPad;
  }

  //........................................................................

  //........................................................................

  //-------------------------------------------------------------- variables

  /** Padding character. */
  protected char m_pad;

  //........................................................................

  //-------------------------------------------------------------- accessors
  //........................................................................

  //----------------------------------------------------------- manipulators
  //........................................................................

  //------------------------------------------------- other member functions

  //------------------------------- execute --------------------------------

  /**
   * Execute the action onto the given document.
   *
   * @param       inDocument  the document to output to
   * @param       inOptionals the optional argument
   * @param       inArguments the arguments
   *
   */
  public void execute(@Nonnull Document inDocument,
                      @Nullable List<? extends Object> inOptionals,
                      @Nullable List<? extends Object> inArguments)
  {
    if(inArguments == null || inArguments.size() != 1)
      throw new IllegalArgumentException("expecting just one argument");

    Document sub = inDocument.createSubDocument();

    sub.add(inArguments.get(0));

    inDocument.add("" + m_pad);
    inDocument.add(sub.toString().trim().replaceAll("\\s", "" + m_pad));
    inDocument.add("" + m_pad);
  }

  //........................................................................

  //........................................................................

  //------------------------------------------------------------------- test

  /** The test. */
  public static class Test extends net.ixitxachitls.util.test.TestCase
  {
    //----- all ------------------------------------------------------------

    /** Test all. */
    @org.junit.Test
    public void all()
    {
      Action action = new Pad('*');

      Document doc = new Document();

      action.execute(doc, null,
                     com.google.common.collect.ImmutableList.of
                     ("  this is some text to see\n\nhow it is      "
                      + "padded   "));

      assertEquals("execution did not produce desired result",
                   "*this*is*some*text*to*see**how*it*is******padded*",
                   doc.toString());
    }

    //......................................................................
  }

  //........................................................................
}