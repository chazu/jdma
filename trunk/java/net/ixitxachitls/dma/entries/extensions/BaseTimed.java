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

package net.ixitxachitls.dma.entries.extensions;

import javax.annotation.Nonnull;

import com.google.common.collect.Multimap;

import net.ixitxachitls.dma.entries.BaseItem;
import net.ixitxachitls.dma.entries.indexes.Index;
import net.ixitxachitls.dma.output.ListPrint;
import net.ixitxachitls.dma.output.Print;
import net.ixitxachitls.dma.values.RandomDuration;

//..........................................................................

//------------------------------------------------------------------- header

/**
 * This is the timed duration for all the entries.
 *
 * @file          BaseTimed.java
 *
 * @author        balsiger@ixitxachitls.net (Peter 'Merlin' Balsiger)
 *
 */

//..........................................................................

//__________________________________________________________________________

public class BaseTimed extends BaseExtension<BaseItem>
{
  //--------------------------------------------------------- constructor(s)

  //------------------------------- BaseTimed ------------------------------

  /**
   * Default constructor.
   *
   * @param       inEntry the base item attached to
   * @param       inName the name of the extension
   *
   */
  public BaseTimed(@Nonnull BaseItem inEntry, @Nonnull String inName)
  {
    super(inEntry, inName);
  }

  //........................................................................
  //------------------------------- BaseTimed ------------------------------

  /**
   * Complete constructor.
   *
   * @param       inEntry the base item attached to
   * @param       inTag   the tag for this specific instance
   * @param       inName  the name of the extension
   *
   */
  // public BaseTimed(BaseItem inEntry, String inTag, String inName)
  // {
  //   super(inEntry, inTag, inName);
  // }

  //........................................................................

  //........................................................................

  //-------------------------------------------------------------- variables

  /** The printer for printing the whole base item. */
  public static final Print s_pagePrint =
    new Print("%duration");

  //----- duration ---------------------------------------------------------

  /** The real duration value. */
  @Key("duration")
  protected @Nonnull RandomDuration m_duration = new RandomDuration();

  static
  {
    addIndex(new Index(Index.Path.DURATIONS, "Durations", BaseItem.TYPE));
  }

  //........................................................................

  static
  {
    setAutoExtensions(BaseTimed.class, "timed");
    extractVariables(BaseItem.class, BaseTimed.class);
  }

  //........................................................................

  //-------------------------------------------------------------- accessors

  //----------------------------- getPagePrint -----------------------------

  /**
   * Get the print for a full page.
   *
   * @return the print for page printing
   *
   */
  @Override
protected @Nonnull Print getPagePrint()
  {
    return s_pagePrint;
  }

  //........................................................................
  //----------------------------- getListPrint -----------------------------

  /**
   * Get the print for a list entry.
   *
   * @return the print for list entry
   *
   */
  @Override
protected @Nonnull ListPrint getListPrint()
  {
    return s_listPrint;
  }

  //........................................................................
  //------------------------- computeIndexValues ---------------------------

  /**
   * Get all the values for all the indexes.
   *
   * @param       ioValues a multi map of values per index name
   *
   */
  @Override
  public void computeIndexValues(@Nonnull Multimap<Index.Path, String> ioValues)
  {
    super.computeIndexValues(ioValues);

    // durations
    ioValues.put(Index.Path.DURATIONS, m_duration.group());
  }

  //........................................................................

  //........................................................................

  //........................................................................

  //----------------------------------------------------------- manipulators
  //........................................................................

  //------------------------------------------------- other member functions
  //........................................................................

  //------------------------------------------------------------------- test

  // no standalone tests, see BaseItem

  //........................................................................
}