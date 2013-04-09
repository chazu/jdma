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

package net.ixitxachitls.server.servlets;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.ThreadSafe;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.easymock.EasyMock;

import net.ixitxachitls.util.resources.Resource;
import net.ixitxachitls.util.resources.TemplateResource;

//..........................................................................

//------------------------------------------------------------------- header

/**
 * This class represents a simple HTTP handler for static content. This can be
 * html files, text or images.
 *
 * @file          FileServlet.java
 *
 * @author        balsiger@ixitxachitls.net (Peter Balsiger)
 *
 */

//..........................................................................

//__________________________________________________________________________

@ThreadSafe
@ParametersAreNonnullByDefault
public class TemplateServlet extends FileServlet
{
  //--------------------------------------------------------- constructor(s)

  //--------------------------- TemplateServlet ----------------------------

  /**
   * The default constructor.
   */
  public TemplateServlet()
  {
    m_prefix = "web.templates.default";
  }

  //........................................................................
  //--------------------------- TemplateServlet ----------------------------

  /**
   * Create the handler for templated files.
   *
   * @param       inRoot         the root directory (in classpath) for the
   *                             files to be served
   * @param       inType         the type of the files to handle (e.g. return)
   * @param       inPrefix       the prefix into the configuration to get
   *                             templates
   *
   */
  public TemplateServlet(String inRoot, @Nullable String inType,
                         String inPrefix)
  {
    super(inRoot, inType, false);

    m_prefix = inPrefix;
  }

  //........................................................................

  //........................................................................

  //-------------------------------------------------------------- variables

  /** The prefix into the configuration for template values. */
  protected String m_prefix;

  /** The id for serialization. */
  private static final long serialVersionUID = 1L;

  //........................................................................

  //-------------------------------------------------------------- accessors

  //----------------------------- getResource ------------------------------

  /**
   * Get the resource used for serving this request.
   *
   * @param       inPath the path to the resource
   *
   * @return      the requested resource
   *
   */
  @Override
  protected Resource getResource(String inPath)
  {
    return TemplateResource.get(inPath, m_prefix);
  }

  //........................................................................

  //........................................................................

  //----------------------------------------------------------- manipulators

  //--------------------------------- init ---------------------------------

  /**
   * Initialize the servlet.
   *
   * @param inConfig the intial configuration (from web.xml)
   *
   */
  @Override
  public void init(ServletConfig inConfig)
  {
    super.init(inConfig);

    // prefix
    String param = inConfig.getInitParameter("prefix");
    if(param != null)
      m_prefix = param;
  }

  //........................................................................

  //........................................................................

  //------------------------------------------------- other member functions
  //........................................................................

  //------------------------------------------------------------------- test

  /** The tests. */
  public static class Test extends net.ixitxachitls.util.test.TestCase
  {
    //----- handle ---------------------------------------------------------

    /**
     * The handle Test.
     *
     * @throws Exception should not happen
     */
    @org.junit.Test
    public void handle() throws Exception
    {
      HttpServletRequest request =
        EasyMock.createMock(HttpServletRequest.class);
      HttpServletResponse response =
        EasyMock.createMock(HttpServletResponse.class);
      BaseServlet.Test.MockServletOutputStream output =
        new BaseServlet.Test.MockServletOutputStream();

      EasyMock.expect(request.getPathInfo())
        .andReturn("/css/jdma.css");
      response.setHeader("Content-Type", "text/plain");
      EasyMock.expect(response.getOutputStream()).andReturn(output);
      EasyMock.replay(request, response);

      TemplateServlet servlet =
        new TemplateServlet("", "text/plain", "test/test/template");

      System.setProperty("test/test/template.color_Monster", "single word");
      m_logger.banClass(net.ixitxachitls.util.Strings.class);
      assertNull("handle", servlet.handle(request, response));
      assertPattern("content",
                    ".*A.Monster         \\{ color: single word \\}.*",
                    output.toString());

      output.close();
      EasyMock.verify(request, response);
    }

    //......................................................................
  }

  //........................................................................
}
