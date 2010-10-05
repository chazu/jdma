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

package net.ixitxachitls.util.test;

import net.ixitxachitls.util.logging.Log;

//..........................................................................

//------------------------------------------------------------------- header

/**
 * This is a simple enhancement to a JUnit TestCase, adding a mock logger
 * to the test, including verifying test messages.
 *
 * @file          TestCase.java
 *
 * @author        balsiger@ixitxachitls.net (Peter 'Merlin' Balsiger)
 *
 */

//..........................................................................

//__________________________________________________________________________

public class TestCase extends org.junit.Assert
{
  //--------------------------------------------------------- constructor(s)
  //........................................................................

  //-------------------------------------------------------------- variables

  /** The mock logger to test logging statements. */
  protected Log.Test.MockLogger m_logger;

  /** The number of errors occurred. */
  protected int m_errors = 0;

  /** The number of failures occurred. */
  protected int m_failures = 0;

  static
  {
    // switch Configuration to test mode; must be done in a static init
    // to make sure that all static inits in the test use the test mode
    net.ixitxachitls.util.configuration.Config.setRewriting(true);
  }

  /** The name of the test logger. */
  private static final String s_logger = "test";

  //........................................................................

  //-------------------------------------------------------------- accessors

  //----------------------------- assertEquals -----------------------------

  /**
   * Check that the given object when converted to String matches the given
   * string.
   *
   * @param       inMessage  the message
   * @param       inExpected the expected string
   * @param       inActual   the actual object computed
   *
   */
//   public void assertEquals(String inMessage, String inExpected,
//                            Object inActual)
//   {
//     assertEquals(inMessage, inExpected, inActual.toString());
//   }

  //........................................................................

  //........................................................................

  //----------------------------------------------------------- manipulators

  //-------------------------------- setUp ---------------------------------

  /**
   * Setup the test for the next test case. This is called before each
   * test method.
   *
   */
  @org.junit.Before
  public void setUp()
  {
    m_logger = new Log.Test.MockLogger();
    Log.add(s_logger, m_logger);
    Log.setLevel(Log.Type.DEBUG);
    net.ixitxachitls.util.configuration.Config.setRewriting(true);
  }

  //........................................................................
  //------------------------------- tearDown -------------------------------

  /**
   * Tear down the test after a test case. This is called after each
   * test method.
   *
   */
  @org.junit.After
  public void tearDown()
  {
    m_logger.verify("logger tear down");

    Log.remove(s_logger);
    m_logger = null;
  }

  //........................................................................

  //........................................................................

  //------------------------------------------------- other member functions
  //........................................................................
}
