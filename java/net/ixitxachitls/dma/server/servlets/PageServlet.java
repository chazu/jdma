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

package net.ixitxachitls.dma.server.servlets;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.concurrent.Immutable;
import javax.servlet.http.HttpServletResponse;

import org.easymock.EasyMock;

import net.ixitxachitls.dma.entries.AbstractEntry;
import net.ixitxachitls.dma.entries.BaseCharacter;
import net.ixitxachitls.dma.output.soy.SoyRenderer;
import net.ixitxachitls.output.commands.Color;
import net.ixitxachitls.output.commands.Command;
import net.ixitxachitls.output.commands.Table;
import net.ixitxachitls.output.html.HTMLBodyWriter;
import net.ixitxachitls.output.html.HTMLDocument;
import net.ixitxachitls.output.html.HTMLWriter;
import net.ixitxachitls.util.Encodings;
import net.ixitxachitls.util.Files;
import net.ixitxachitls.util.logging.Log;

//..........................................................................

//------------------------------------------------------------------- header

/**
 * The base servlet for all dma pages. This class is mainly responsible for
 * rendering headers and footers and everything that is the same for every dma
 * page.
 *
 * @file          PageServlet.java
 *
 * @author        balsiger@ixitxachitls.net (Peter Balsiger)
 *
 */

//..........................................................................

//__________________________________________________________________________

@Immutable
public class PageServlet extends SoyServlet
{
  //--------------------------------------------------------- constructor(s)

  /**
   * Create the servlet.
   *
   */
  public PageServlet()
  {
  }

  //........................................................................

  //-------------------------------------------------------------- variables

  /** The id for serialization. */
  private static final long serialVersionUID = 1L;

  //........................................................................

  //-------------------------------------------------------------- accessors
  //........................................................................

  //----------------------------------------------------------- manipulators

  //----------------------------- collectData ------------------------------

  /**
   * Collect the data that is to be printed.
   *
   * @param    inRequest  the request for the page
   * @param    inRenderer the renderer for sub values
   *
   * @return   a map with key/value pairs for data (values can be primitives
   *           or maps or lists)
   *
   */
  protected @Nonnull Map<String, Object> collectData
    (@Nonnull DMARequest inRequest, @Nonnull SoyRenderer inRenderer)
  {
    Map<String, Object> data = super.collectData(inRequest, inRenderer);

    boolean bodyOnly = inRequest.isBodyOnly();
    String path = inRequest.getRequestURI();

    java.io.StringWriter buffer = new java.io.StringWriter();
    HTMLWriter writer;
    if(bodyOnly)
      writer = new HTMLBodyWriter(new PrintWriter(buffer));
    else
      writer = new HTMLWriter(new PrintWriter(buffer));

    if(!bodyOnly)
      writeHeader(writer, path, inRequest);

    writeBody(writer, path, inRequest);

    if(!bodyOnly)
      writeFooter(writer, path, inRequest);

    writer.close();

    data.put("oldcontent", buffer.toString());
    if(!data.containsKey("content"))
      data.put("content", "No new content defined, yet.");

    return data;
  }

  //........................................................................
  //------------------------- collectInjectedData --------------------------

  /**
   * Collect the injected data that is to be printed.
   *
   * @param    inRequest the request for the page
   * @param    inRenderer the renderer for rendering sub values
   *
   * @return   a map with key/value pairs for data (values can be primitives
   *           or maps or lists)
   *
   */
  protected @Nonnull Map<String, Object> collectInjectedData
    (@Nonnull DMARequest inRequest, SoyRenderer inRenderer)
  {
    Map<String, Object> data = super.collectInjectedData(inRequest, inRenderer);

    if(inRequest.getUser() == null)
      data.put("dm", false);
    else
    {
      AbstractEntry entry = getEntry(inRequest.getRequestURI());
      data.put("dm", entry != null && entry.isDM(inRequest.getUser()));
    }

    return data;
  }

  //........................................................................

  //----------------------------- writeHeader ------------------------------

  /**
   * Write the header to the writer.
   *
   * @param     inWriter  the writer to take up the content (will be closed
   *                      by the PageServlet)
   * @param     inPath    the path of the request
   * @param     inRequest the request for the page
   *
   */
  @OverridingMethodsMustInvokeSuper
  protected void writeHeader(@Nonnull HTMLWriter inWriter,
                             @Nonnull String inPath,
                             @Nonnull DMARequest inRequest)
  {
  }

  //........................................................................
  //------------------------------- writeBody ------------------------------

  /**
   * Handles the body content of the request.
   *
   * @param     inWriter  the writer to take up the content (will be closed
   *                      by the PageServlet)
   * @param     inPath    the path of the request
   * @param     inRequest the request for the page
   *
   */
  @OverridingMethodsMustInvokeSuper
  protected void writeBody(@Nonnull HTMLWriter inWriter,
                           @Nullable String inPath,
                           @Nonnull DMARequest inRequest)
  {
    // nothing done here
  }

  //........................................................................
  //----------------------------- writeFooter ------------------------------

  /**
   * Write the footer to the writer.
   *
   * @param     inWriter  the writer to take up the content (will be closed
   *                      by the PageServlet)
   * @param     inPath    the path of the request
   * @param     inRequest the request for the page
   *
   */
  @OverridingMethodsMustInvokeSuper
  protected void writeFooter(@Nonnull HTMLWriter inWriter,
                             @Nonnull String inPath,
                             @Nonnull DMARequest inRequest)
  {
  }

  //........................................................................

  //---------------------------- addNavigation -----------------------------

  /**
   * Add the navigation structure to the page.
   *
   * @param       inWriter   the writer to write to
   * @param       inSections the sections and subsections to the current page
   *
   */
  public void addNavigation(@Nonnull HTMLWriter inWriter,
                            @Nonnull String ... inSections)
  {
    StringBuilder builder = new StringBuilder();

    for(int i = 0; i < inSections.length; i += 2)
      if(inSections[i] != null)
      {
        builder.append(" &raquo; ");

        if(i + 1 < inSections.length)
          builder.append("<a href=\""
                         + Encodings.encodeHTMLAttribute(inSections[i + 1])
                         + "\" class=\"navigation-link\" "
                         + "onclick=\"return util.link(event, \\'"
                         + Encodings.encodeHTMLAttribute(inSections[i + 1])
                         + "\\');\" >"
                         + Encodings.encodeHTMLAttribute
                         (inSections[i].toLowerCase(Locale.US)) + "</a>");
        else
          builder.append(inSections[i]);
      }

    String navigation = builder.toString();
    if(navigation.isEmpty())
      navigation = "&nbsp;";

    inWriter.bodyScript("$('#subnavigation').html('" + navigation + "');");
  }

  //........................................................................

  //........................................................................

  //------------------------------------------------- other member functions

  //-------------------------------- format --------------------------------

  /**
   * Format the given list of entries.
   *
   * @param       inWriter       the write to write to
   * @param       inEntries      the entries to be written
   * @param       inUser         the user for which to format, if any
   * @param       inStart        the start index of the entries to print
   * @param       inPageSize     the full size of the page
   *
   */
  protected void format(@Nonnull HTMLWriter inWriter,
                        @Nonnull List<? extends AbstractEntry> inEntries,
                        @Nullable BaseCharacter inUser,
                        int inStart, int inPageSize)
  {
    HTMLDocument document = new HTMLDocument("");

    List<String> navigation = new ArrayList<String>();
    if(inStart > 0)
      if(inStart - inPageSize > 0)
        navigation.add("<a href=\"?start="
                       + (inStart - inPageSize)
                       + "\"  onclick=\"return util.link(event, '?start="
                       + (inStart - inPageSize) + "');\" "
                       + "class=\"paginate-previous\">"
                       + "&laquo; previous</a>");
      else
        navigation.add("<a href=\"\" "
                       + "onclick=\"return util.link(event, '?');\" "
                       + "class=\"paginate-previous\">"
                       + "&laquo; previous</a>");

    if(inEntries.size() > inPageSize)
      navigation.add("<a href=\"?start="
                     + (inStart + inPageSize) + "\" "
                     + " onclick=\"return util.link(event, '?start="
                     + (inStart + inPageSize) + "');\" "
                     + "class=\"paginate-next\">"
                     + "&raquo; next</a>");

    document.add(navigation);

    if(inEntries.isEmpty())
      document.add(new Color("error", "No entries found!"));
    else
    {
      String format = "";
      List<Object> cells = new ArrayList<Object>();
      for(AbstractEntry entry : inEntries)
        if(entry != null)
        {
          if(format.isEmpty())
            format = entry.getListFormat();

          cells.addAll(entry.printList(entry.getName(), inUser));
        }
        else
          Log.error("There were null entries in the index!");

      document.add(new Table("entrylist", format, new Command(cells)));
    }
    document.add(navigation);

    inWriter.add(document.toString());
  }

  //........................................................................
  //------------------------------ writeIcon -------------------------------

  /**
   * Write an icon to the current position in the writer.
   *
   * @param       inWriter   the html writer to output to
   * @param       inIcon     the name of the image to use for the icon
   *                         (/icons/ will be prefixed, .png appended)
   * @param       inCaption  the caption below the icon
   * @param       inURL      the url to link to when clicking the icon
   *
   */
  protected static void writeIcon(@Nonnull HTMLWriter inWriter,
                                  @Nonnull String inIcon,
                                  @Nonnull String inCaption,
                                  @Nonnull String inURL)
  {
    inWriter
      .begin("div").classes("caption-container")
      .begin("a").classes("icon-link").href(inURL)
      .onClick("link(event, '" + inURL + "');")
      .begin("img")
      .src("/icons/"
           + Files.encodeName(inIcon.toLowerCase(Locale.US)) + ".png")
      .alt(inCaption).tooltip(inCaption).classes("icon highlight")
      .end("img")
      .begin("div").classes("caption").add(inCaption).end("div")
      .end("a")
      .end("div");
  }

  //........................................................................
  //------------------------------ writeError ------------------------------

  /**
   * Write an error text to the output.
   *
   * @param    inWriter   the output to write to
   * @param    inTitle    the title of the error
   * @param    inMessage  the error message
   *
   */
  protected static void writeError(@Nonnull HTMLWriter inWriter,
                                   @Nonnull String inTitle,
                                   @Nonnull String inMessage)
  {
    inWriter
      .title(inTitle)
      .begin("h1").add(inTitle).end("h1")
      .add(inMessage);

    Log.warning(inTitle + " - " + inMessage);
  }

  //........................................................................

  //........................................................................

  //------------------------------------------------------------------- test

  /** The test. */
  public static class Test extends net.ixitxachitls.server.ServerUtils.Test
  {
    //----- simple ---------------------------------------------------------

    /** The simple Test.
     *
     * @throws Exception should not happen
     */
    @org.junit.Test
    public void simple() throws Exception
    {
      m_localServiceTestHelper.setEnvIsLoggedIn(false);
      DMARequest request = EasyMock.createMock(DMARequest.class);
      HttpServletResponse response =
        EasyMock.createMock(HttpServletResponse.class);
      MockServletOutputStream output = new MockServletOutputStream();

      response.setHeader("Content-Type", "text/html");
      response.setHeader("Cache-Control", "max-age=0");
      EasyMock.expect(request.isBodyOnly()).andReturn(false).anyTimes();
      EasyMock.expect(request.getUser()).andReturn(null);
      EasyMock.expect(request.getOriginalPath()).andReturn("index.html");
      EasyMock.expect(request.getQueryString()).andReturn("").anyTimes();
      EasyMock.expect(request.getRequestURI()).andReturn("/about.html");
      EasyMock.expect(response.getOutputStream()).andReturn(output);
      EasyMock.replay(request, response);

      PageServlet servlet = new PageServlet() {
          private static final long serialVersionUID = 1L;
          @Override
          protected void writeBody(@Nonnull HTMLWriter inWriter,
                                   @Nullable String inPath,
                                   @Nonnull DMARequest inRequest)
          {
            super.writeBody(inWriter, inPath, inRequest);
            inWriter.add("This is the body.");
          }
        };

      assertNull("handle", servlet.handle(request, response));
      assertEquals("content",
                   "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                   + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN"
                   + "\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"
                   + "\">\n"
                   + "<HTML xmlns=\"http://www.w3.org/1999/xhtml\">\n"
                   + "  <HEAD>\n"
                   + "    <SCRIPT type=\"text/javascript\" "
                   + "src=\"/js/jquery-1.5.1.min.js\"></SCRIPT>\n"
                   + "    <LINK rel=\"STYLESHEET\" type=\"text/css\" "
                   + "href=\"/css/smoothness/jquery-ui-1.8.14.custom.css\" />\n"
                   + "    <SCRIPT type=\"text/javascript\" "
                   + "src=\"/js/jquery-ui-1.8.14.custom.min.js\"></SCRIPT>\n"
                   + "    <SCRIPT type=\"text/javascript\" "
                   + "src=\"/js/util.js\"></SCRIPT>\n"
                   + "    <SCRIPT type=\"text/javascript\" src=\"/js/form.js\">"
                   + "</SCRIPT>\n"
                   + "    <LINK rel=\"STYLESHEET\" type=\"text/css\" "
                   + "href=\"/css/gui.css\" />\n"
                   + "    <SCRIPT type=\"text/javascript\" src=\"/js/gui.js\">"
                   + "</SCRIPT>\n"
                   + "    <SCRIPT type=\"text/javascript\" src=\"/js/edit.js\">"
                   + "</SCRIPT>\n"
                   + "    <SCRIPT type=\"text/javascript\" src=\"/js/item.js\">"
                   + "</SCRIPT>\n"
                   + "    <LINK rel=\"STYLESHEET\" type=\"text/css\" "
                   + "href=\"/css/jdma.css\" />\n"
                   + "    <SCRIPT type=\"text/javascript\" "
                   + "src=\"/js/jdma.js\"></SCRIPT>\n"
                   + "    <META name=\"viewport\" "
                   + "content=\"width=device-width, height=device-height\"/>\n"
                   + "    <META name=\"Content-Type\" "
                   + "content=\"text/html; charset=utf-8\" xml:lang=\"en\" "
                   + "lang=\"en\"/>\n"
                   + "    <LINK ref=\"SHORTCUT ICON\" "
                   + "href=\"/icons/favicon.png\" />\n"
                   + "    <SCRIPT type=\"text/javascript\">\n"
                   + "      if(location.hostname != 'localhost')\n"
                   + "      {\n"
                   + "        var _gaq = _gaq || [];\n"
                   + "        _gaq.push(['_setAccount', 'UA-1524401-1']);\n"
                   + "        _gaq.push(['_trackPageview']);\n"
                   + "        (function() {\n"
                   +  "          var ga = document.createElement('script'); "
                   + "ga.type = 'text/javascript'; ga.async = true;\n"
                   + "          ga.src = "
                   + "('https:' == document.location.protocol "
                   + "? 'https://ssl' : 'http://www') + "
                   + "'.google-analytics.com/ga.js';\n"
                   + "          var s = "
                   + "document.getElementsByTagName('script')[0]; "
                   + "s.parentNode.insertBefore(ga, s);\n"
                   + "        })();\n"
                   + "      }\n"
                   + "    </SCRIPT>\n"
                   + "  </HEAD>\n"
                   + "  <BODY>\n"
                   + "    <!-- This file was generate by jDMA, version "
                   + "Allip () -->\n"
                   + "    <DIV id=\"header\">\n"
                   + "      <DIV id=\"header-right\">\n"
                   + "        <A id=\"login-icon\" class=\"sprite\" "
                   + "title=\"Login\" "
                   + "href=\"/_ah/login?continue=index.html\">\n"
                   + "</A>\n"
                   + "        <A class=\"sprite library\" title=\"Library\" "
                   + "href=\"/library\" "
                   + "onclick=\"util.link(event, '/library');\">\n"
                   + "</A>\n"
                   + "        <A class=\"sprite about\" title=\"About\" "
                   + "href=\"/about.html\" "
                   + "onclick=\"util.link(event, '/about.html')\">\n"
                   + "</A>\n"
                   + "        <DIV onmouseover=\"$('#search :input').show()\" "
                   + "onmouseout=\"$('#search :input').hide()\">\n"
                   + "          <FORM class=\"search\" id=\"search\" "
                   + "onsubmit=\"util.link(event, '/search/' + "
                   + "this.search.value)\">\n"
                   + "            <INPUT name=\"search\"/>\n"
                   + "            <DIV class=\"sprite search\" "
                   + "title=\"Search\">\n"
                   + "            </DIV>\n"
                   + "          </FORM>\n"
                   + "        </DIV>\n"
                   + "      </DIV>\n"
                   + "      <DIV id=\"header-left\">\n"
                   + "        DMA\n"
                   + "      </DIV>\n"
                   + "      <DIV id=\"navigation\">\n"
                   + "        <A id=\"home\" class=\"sprite\" title=\"Home\" "
                   + "href=\"/\" onclick=\"util.link(event, '/')\">\n"
                   + "</A>\n"
                   + "        <SPAN id=\"subnavigation\">\n"
                   + "          &nbsp;\n"
                   + "        </SPAN>\n"
                   + "      </DIV>\n"
                   + "      <DIV id=\"actions\">\n"
                   + "      </DIV>\n"
                   + "    </DIV>\n"
                   + "    <DIV id=\"page\" class=\"page\">\n"
                   + "      This is the body.\n"
                   + "    </DIV>\n"
                   + "    <DIV class=\"footer\">\n"
                   + "      <P/>\n"
                   + "      <DIV class=\"version\">\n"
                   + "        jDMA version Allip (build )\n"
                   + "      </DIV>\n"
                   + "      <IMG src=\"/icons/html5.png\" "
                   + "alt=\"Uses HTML 5!\"/>\n"
                   + "      <IMG src=\"http://code.google.com/appengine/"
                   + "images/appengine-silver-120x30.gif\" "
                   + "alt=\"Powered by Google App Engine\"/>\n"
                   + "      <A href=\"https://plus.google.com/"
                   + "109501801834573360902?prsrc=3\" "
                   + "style=\"text-decoration:none;\">"
                   + "<IMG src=\"https://ssl.gstatic.com/images/icons/"
                   + "gplus-32.png\" alt=\"jDMA on Google+\" "
                   + "style=\"border:0;width:32px;height:32px;\"/>\n"
                   + "</A>\n"
                   + "    </DIV>\n"
                   + "  </BODY>\n"
                   + "</HTML>\n",
                   output.toString());

      output.close();
      EasyMock.verify(request, response);
    }

    //......................................................................
    //----- bodyOnly -------------------------------------------------------

    /** The simple Test.
     *
     * @throws Exception should not happen
     */
    @org.junit.Test
    public void bodyOnly() throws Exception
    {
      DMARequest request = EasyMock.createMock(DMARequest.class);
      HttpServletResponse response =
        EasyMock.createMock(HttpServletResponse.class);
      MockServletOutputStream output = new MockServletOutputStream();

      response.setHeader("Content-Type", "text/html");
      response.setHeader("Cache-Control", "max-age=0");
      EasyMock.expect(request.isBodyOnly()).andReturn(true).anyTimes();
      EasyMock.expect(request.getQueryString()).andReturn("").anyTimes();
      EasyMock.expect(request.getRequestURI()).andReturn("/about.html");
      EasyMock.expect(response.getOutputStream()).andReturn(output);
      EasyMock.replay(request, response);

      PageServlet servlet = new PageServlet() {
          private static final long serialVersionUID = 1L;
          @Override
          protected void writeBody(@Nonnull HTMLWriter inWriter,
                                   @Nullable String inPath,
                                   @Nonnull DMARequest inRequest)
          {
            super.writeBody(inWriter, inPath, inRequest);
            inWriter.add("This is the body.");
          }
        };

      assertNull("handle", servlet.handle(request, response));
      assertEquals("content", "    This is the body.\n", output.toString());

      output.close();
      EasyMock.verify(request, response);
    }

    //......................................................................
    //----- navigation -----------------------------------------------------

    /** The navigation Test. */
    @org.junit.Test
    public void navigation()
    {
      java.io.ByteArrayOutputStream output =
        new java.io.ByteArrayOutputStream();
      HTMLWriter writer = new HTMLWriter(new PrintWriter(output));

      PageServlet servlet = new PageServlet();

      servlet.addNavigation(writer, "s1", "l1", "s2", "l2", "s3", "l3");
      writer.close();
      assertEquals("3 sections",
                   "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                   + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN"
                   + "\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"
                   + "\">\n"
                   + "<HTML xmlns=\"http://www.w3.org/1999/xhtml\">\n"
                   + "  <BODY>\n"
                   + "    <SCRIPT type=\"text/javascript\">\n"
                   + "      $('#subnavigation').html(' &raquo; <a href=\"l1\" "
                   + "class=\"navigation-link\" "
                   + "onclick=\"return util.link(event, \\'l1\\');\" >s1</a> "
                   + "&raquo; <a href=\"l2\" class=\"navigation-link\" "
                   + "onclick=\"return util.link(event, \\'l2\\');\" >s2</a> "
                   + "&raquo; <a href=\"l3\" class=\"navigation-link\" "
                   + "onclick=\"return util.link(event, \\'l3\\');\" >s3</a>"
                   + "');\n"
                   + "    </SCRIPT>\n"
                   + "  </BODY>\n"
                   + "</HTML>\n", output.toString());

      output = new java.io.ByteArrayOutputStream();
      writer = new HTMLWriter(new PrintWriter(output));

      servlet.addNavigation(writer);
      writer.close();
      assertEquals("no section",
                   "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                   + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN"
                   + "\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"
                   + "\">\n"
                   + "<HTML xmlns=\"http://www.w3.org/1999/xhtml\">\n"
                   + "  <BODY>\n"
                   + "    <SCRIPT type=\"text/javascript\">\n"
                   + "      $('#subnavigation').html('&nbsp;');\n"
                   + "    </SCRIPT>\n"
                   + "  </BODY>\n"
                   + "</HTML>\n", output.toString());
    }

    //......................................................................
    //----- formatEntries --------------------------------------------------

    /** The formatEntries Test. */
    @org.junit.Test
    public void formatEntries()
    {
      java.io.StringWriter content = new java.io.StringWriter();
      HTMLWriter writer = new HTMLWriter(new java.io.PrintWriter(content));
      List<AbstractEntry> entries = new ArrayList<AbstractEntry>();
      net.ixitxachitls.dma.entries.BaseCharacter first =
        new net.ixitxachitls.dma.entries.BaseCharacter("first")
        {
          @Override
          public boolean isDM(@Nullable BaseCharacter inUser)
          {
            return true;
          }

          @Override
          public @Nonnull net.ixitxachitls.dma.entries.Variables
            getVariables()
          {
            return super.getVariables
              (net.ixitxachitls.dma.entries.BaseCharacter.class);
          }
        };
      net.ixitxachitls.dma.entries.BaseCharacter second =
        new net.ixitxachitls.dma.entries.BaseCharacter("second")
        {
          @Override
          public boolean isDM(@Nullable BaseCharacter inUser)
          {
            return true;
          }

          @Override
          public @Nonnull net.ixitxachitls.dma.entries.Variables
            getVariables()
          {
            return super.getVariables
              (net.ixitxachitls.dma.entries.BaseCharacter.class);
          }
        };
      entries.add(first);
      entries.add(second);

      PageServlet servlet = new PageServlet();
      servlet.format(writer, entries, first, 0, 50);

      writer.close();

      assertEquals("content",
                   "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                   + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN"
                   + "\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"
                   + "\">\n"
                   + "<HTML xmlns=\"http://www.w3.org/1999/xhtml\">\n"
                   + "  <BODY>\n"
                   + "    \n"
                   + "<table class=\"entrylist\"><tr class=\"title\">"
                   + "<td class=\"title\"></td><td class=\"title\">Name</td>"
                   + "<td class=\"title\">Real Name</td>"
                   + "<td class=\"title\">Group</td>"
                   + "<td class=\"title\">Last Action</td>"
                   + "</tr><tr><td class=\"label\">"
                   + "<img src=\"/icons/labels/BaseCharacter.png\" "
                   + "alt=\"BaseCharacter\" class=\"image label\"/> "
                   + "<div id=\"linkrow-user-first\" class=\"\">\n"
                   + "<script type='text/javascript'>"
                   + "util.linkRow(document.getElementById"
                   + "('linkrow-user-first'), '/user/first');</script>\n"
                   + "</div></td><td class=\"name\">first</td>"
                   + "<td class=\"name\"><dmaeditable name=\"real name\" "
                   + "value=\"$undefined$\" key=\"/base character/first\" "
                   + "class=\"editable\" type=\"string\"><span></span>"
                   + "</dmaeditable></td>"
                   + "<td class=\"group\"><dmaeditable name=\"group\" "
                   + "value=\"$undefined$\" key=\"/base character/first\" "
                   + "class=\"editable\" type=\"selection\" note=\"\" "
                   + "values=\"Guest||User||Player||DM||Admin\"><span></span>"
                   + "</dmaeditable></td>"
                   + "<td class=\"action\"></td></tr>"
                   + "<tr><td class=\"label\">"
                   + "<img src=\"/icons/labels/BaseCharacter.png\" "
                   + "alt=\"BaseCharacter\" class=\"image label\"/> "
                   + "<div id=\"linkrow-user-second\" class=\"\">\n"
                   + "<script type='text/javascript'>"
                   + "util.linkRow(document.getElementById"
                   + "('linkrow-user-second'), '/user/second');</script>\n"
                   + "</div></td><td class=\"name\">second</td>"
                   + "<td class=\"name\"><dmaeditable name=\"real name\" "
                   + "value=\"$undefined$\" key=\"/base character/second\" "
                   + "class=\"editable\" type=\"string\"><span></span>"
                   + "</dmaeditable></td>"
                   + "<td class=\"group\"><dmaeditable name=\"group\" "
                   + "value=\"$undefined$\" key=\"/base character/second\" "
                   + "class=\"editable\" type=\"selection\" note=\"\" "
                   + "values=\"Guest||User||Player||DM||Admin\"><span></span>"
                   + "</dmaeditable></td>"
                   + "<td class=\"action\"></td></tr></table>\n"
                   + "  </BODY>\n"
                   + "</HTML>\n", content.toString());
    }

    //......................................................................
  }

  //........................................................................
}
