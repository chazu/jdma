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

package net.ixitxachitls.dma.server.servlets;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;

import net.ixitxachitls.server.servlets.BaseServlet;
import net.ixitxachitls.util.Strings;

//..........................................................................

//------------------------------------------------------------------- header

/**
 * A small servlet to serve blobs.
 *
 *
 * @file          BlobServlet.java
 *
 * @author        balsiger@ixitxachitls.net (Peter Balsiger)
 *
 */

//..........................................................................

//__________________________________________________________________________

public class BlobServlet extends BaseServlet
{
  //--------------------------------------------------------- constructor(s)

  //........................................................................

  //-------------------------------------------------------------- variables

  /** The id for serialization. */
  private static final long serialVersionUID = 1L;

  /** The blob store service. */
  private @Nonnull BlobstoreService m_blobs =
    BlobstoreServiceFactory.getBlobstoreService();

  //........................................................................

  //-------------------------------------------------------------- accessors

  //........................................................................

  //----------------------------------------------------------- manipulators

  //........................................................................

  //------------------------------------------------- other member functions

  /**
   * Handle requests to a blob.
   *
   * @param inRequest  the http request
   * @param inResponse the http response
   *
   * @return a special result in case of error or null if ok
   *
   * @throws IOException if problems reading the blob
   *
   */
  public @Nullable SpecialResult handle(@Nonnull HttpServletRequest inRequest,
                                        @Nonnull HttpServletResponse inResponse)
    throws IOException
  {
    String path = Strings.getPattern(inRequest.getPathInfo(), "/([^/]*)$");
    BlobKey key = new BlobKey(path);
    m_blobs.serve(key, inResponse);

    return null;
  }

  //........................................................................
}
