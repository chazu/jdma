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

package net.ixitxachitls.util;

import java.util.Date;

import javax.annotation.Nullable;

import net.ixitxachitls.util.logging.Log;

/**
 * A tracer to time and log execution times.
 *
 * @file   Tracer.java
 * @author balsiger@ixitxachitls.net (Peter Balsiger)
 *
 */
public class Tracer
{
  public Tracer(String inName)
  {
    m_name = inName;
    m_start = new Date().getTime();
  }

  private String m_name;
  private long m_start;

  public void done()
  {
    done(null);
  }

  public void done(@Nullable String inText)
  {
    long ms = new Date().getTime() - m_start;
    String text;
    if(inText == null)
      text = "";
    else
      text = " (" + inText + ")";

    if (ms < 1000)
      Log.trace(m_name + " took " + ms + "ms" + text + ".");
    else
      Log.trace(m_name + " took " + (ms / 1000.0) + "s" + text + ".");
  }
}