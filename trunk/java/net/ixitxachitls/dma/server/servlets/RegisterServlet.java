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

package net.ixitxachitls.dma.server.servlets;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import net.ixitxachitls.dma.data.DMADataFactory;
import net.ixitxachitls.dma.entries.AbstractEntry;
import net.ixitxachitls.dma.entries.BaseCharacter;
import net.ixitxachitls.dma.entries.BaseCharacter.Group;
import net.ixitxachitls.util.logging.Log;

//..........................................................................

//------------------------------------------------------------------- header

/**
 * The servlet responsible for handling registration requests.
 *
 * @file          RegisterServlet.java
 *
 * @author        fred@dobler.net (Fred Dobler)
 *
 */

//..........................................................................

//__________________________________________________________________________

public class RegisterServlet extends ActionServlet
{
  //--------------------------------------------------------- constructor(s)

  //----------------------------- RegisterServlet ----------------------
  /**
    * Create the servlet.
    */
  public RegisterServlet()
  {
    //nothing to do
  }

  //........................................................................

  //........................................................................

  //-------------------------------------------------------------- variables

  /** The id for serialization. */
  private static final long serialVersionUID = 1L;

  //........................................................................

  //-------------------------------------------------------------- accessors
  //........................................................................

  //----------------------------------------------------------- manipulators

  //------------------------------- doAction -------------------------------

  /**
   *
   * Execute the action associated with this servlet.
   *
   * @param       inRequest  the request for the page
   * @param       inResponse the response to write to
   *
   * @return      the javascript code to send back to the client
   *
   */
  protected @Nonnull String doAction(@Nonnull DMARequest inRequest,
                                     @Nonnull HttpServletResponse inResponse)
  {
    String username = inRequest.getParam("username");
    String realname = inRequest.getParam("realname");

    if(username == null)
      return "No username given";

    if(realname == null)
      return "No real name given";

    UserService userService = UserServiceFactory.getUserService();
    if(!userService.isUserLoggedIn())
    {
      return "You must login to register";
    }

    BaseCharacter user = DMADataFactory.get().getEntry
      (AbstractEntry.createKey(username, BaseCharacter.TYPE));
    if(user != null)
    {
      return "Username allready used, choose a new one.";
    }

    //Save the new user with the default group GUEST.
    BaseCharacter baseCharacter = new BaseCharacter(username, userService
        .getCurrentUser().getEmail());
    baseCharacter.setRealName(realname);
    baseCharacter.setGroup(Group.GUEST);
    baseCharacter.save();

    Log.event(username, "register", "user registered");

    return "";
  }

  //........................................................................

  //........................................................................

  //------------------------------------------------- other member functions

  //........................................................................

  //------------------------------------------------------------------- test

  /** The tests. */
  public static class Test extends net.ixitxachitls.server.ServerUtils.Test
  {
  }

  //........................................................................
}