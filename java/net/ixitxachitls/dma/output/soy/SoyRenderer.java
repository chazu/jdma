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

package net.ixitxachitls.dma.output.soy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.appengine.api.utils.SystemProperty;
import com.google.template.soy.data.SoyMapData;

import net.ixitxachitls.util.configuration.Config;

//..........................................................................

//------------------------------------------------------------------- header

/**
 * Renderer for soy templates.
 *
 *
 * @file          SoyRenderer.java
 *
 * @author        balsiger@ixitxachitls.net (Peter Balsiger)
 *
 */

//..........................................................................

//__________________________________________________________________________

@ParametersAreNonnullByDefault
public class SoyRenderer
{
  //--------------------------------------------------------- constructor(s)

  //----------------------------- SoyRenderer ------------------------------

  /**
   * Create the renderer.
   *
   * @param   inTemplate the soy template that can be used for rendering.
   *
   */
  public SoyRenderer(SoyTemplate inTemplate)
  {
    m_template = inTemplate;
  }

  //........................................................................

  //........................................................................

  //-------------------------------------------------------------- variables

  /** The soy template. */
  private SoyTemplate m_template;

  /** The data to be used when rendering, if any. */
  private @Nullable SoyMapData m_data = null;

  /** The injected data to be used when rendering, if any. */
  private @Nullable SoyMapData m_injected = null;

  /** Command starter character. */
  protected static final char s_command =
    Config.get("resource:writer/command", '\\');

  /** Argument starter character. */
  protected static final char s_argStart =
    Config.get("resource:writer/argument.start", '{');

  /** Argument ending character. */
  protected static final char s_argEnd =
    Config.get("resource:writer/argument.end", '}');

  /** Character starting an optional argument. */
  protected static final char s_optArgStart =
    Config.get("resource:writer/optional.argument.start", '[');

  /** Character ending an optional argument. */
  protected static final char s_optArgEnd =
    Config.get("resource:writer/optional.argument.end", ']');

  /** Escape character. */
  protected static final char s_escape =
    Config.get("resource:writer/escape", '\\');

  /** Marking character for argument starts. */
  protected static final char s_markArgStart = '\001';

  /** Marking character for argument ends. */
  protected static final char s_markArgEnd = '\002';

  /** Marking character for optional argument starts. */
  protected static final char s_markOptArgStart = '\003';

  /** Marking character for optional argument ends. */
  protected static final char s_markOptArgEnd = '\004';

  /** Marking character for escaped start. */
  protected static final char s_markStart = '\005';

  /** Marking character for escaped end. */
  protected static final char s_markEnd = '\006';

  /** The characters that are allowed in 'special character only commands'. */
  private static final String s_special = "<>=!~*#$%@?+|";

  /** The commands associated with each special command string. */
  private static final Map<String, String> s_specialNames =
    new HashMap<String, String>();

  /** The prefix for commands rendered. */
  private String m_commandPrefix = "dma.commands";

  static
  {
    // fill in the default special names
    s_specialNames.put("<",  "Less");
    s_specialNames.put("<=", "LessEqual");
    s_specialNames.put(">",  "Greater");
    s_specialNames.put(">=", "GreaterEqual");
    s_specialNames.put("\"", "Umlaut");
    s_specialNames.put("^",  "Hat");
  }

  //........................................................................

  //----------------------------------------------------------- manipulators

  //------------------------------- setData --------------------------------

  /**
   * Set the data to be used for rendering.
   *
   * @param       inData the data to use
   *
   */
  public void setData(@Nullable Map<String, Object> inData)
  {
    if(inData == null)
      m_data = null;
    else
      m_data = new SoyMapData(inData);
  }

  //........................................................................
  //----------------------------- setInjected ------------------------------

  /**
   * Set the injected data to bse used for rendering.
   *
   * @param   inData the injected data
   *
   */
  public void setInjected(@Nullable Map<String, Object> inData)
  {
    if(inData == null)
      m_injected = null;
    else
      m_injected = new SoyMapData(inData);
  }

  //........................................................................

  //........................................................................

  //------------------------------------------------- other member functions

  //-------------------------------- render --------------------------------

  /**
   * Render the template named.
   *
   * @param       inName      the name of the template to render.
   * @param       inData      the data for the template.
   * @param       inInjected  the injected data for the template.
   * @param       inDelegates the delegates used for rendering, if any
   *
   * @return      the rendered template as a string
   *
   */
  public String render(String inName,
                       @Nullable Map<String, Object> inData,
                       @Nullable Map<String, Object> inInjected,
                       @Nullable Set<String> inDelegates)
  {
    return m_template.render(inName, inData, inInjected, inDelegates);
  }

  //........................................................................
  //-------------------------------- render --------------------------------

  /**
   * Render the template named.
   *
   * @param       inName      the name of the template to render.
   * @param       inData      the data for the template.
   * @param       inDelegates the delegates used for rendering, if any
   *
   * @return      the rendered template as a string
   *
   */
  public String render(String inName,
                       @Nullable Map<String, Object> inData,
                       @Nullable Set<String> inDelegates)
  {
    SoyMapData data = null;
    if(inData != null)
      data = new SoyMapData(inData);

    try
    {
      return m_template.render(inName, data, m_injected, inDelegates);
    }
    catch(Exception e) // $codepro.audit.disable caughtExceptions
    {
      // In case there is an error, we always want to recompile on dev.
      if(SystemProperty.environment.value()
         == SystemProperty.Environment.Value.Development)
        recompile();

      throw e;
    }
  }

  //........................................................................
  //-------------------------------- render --------------------------------

  /**
   * Render the template named.
   *
   * @param       inName      the name of the template to render.
   * @param       inDelegates the delegates used for rendering, if any
   *
   * @return      the rendered template as a string
   *
   */
  public String render(String inName, @Nullable Set<String> inDelegates)
  {
    return m_template.render(inName, m_data, m_injected, inDelegates);
  }

  //........................................................................
  //-------------------------------- render --------------------------------

  /**
   * Render the template named.
   *
   * @param       inName      the name of the template to render.
   *
   * @return      the rendered template as a string
   *
   */
  public String render(String inName)
  {
    return m_template.render(inName, m_data, m_injected, null);
  }

  //........................................................................
  //-------------------------------- render --------------------------------

  /**
   * Render the template named.
   *
   * @param       inName      the name of the template to render.
   * @param       inData      the data for the template.
   * @param       inInjected  the injected data for the template.
   *
   * @return      the rendered template as a string
   *
   */
  public String render(String inName,
                       @Nullable Map<String, Object> inData,
                       @Nullable Map<String, Object> inInjected)
  {
    return m_template.render(inName, inData, inInjected, null);
  }

  //........................................................................
  //-------------------------------- render --------------------------------

  /**
   * Render the template named.
   *
   * @param       inName      the name of the template to render.
   * @param       inData      the data for the template.
   *
   * @return      the rendered template as a string
   *
   */
  public String render(String inName, @Nullable Map<String, Object> inData)
  {
    return m_template.render(inName, new SoyMapData(inData), m_injected, null);
  }

  //........................................................................

  //------------------------------ recompile -------------------------------

  /**
   * Recompile the template this renderer is based on.
   *
   */
  public void recompile()
  {
    m_template.recompile();
  }

  //........................................................................

  //---------------------------- renderCommands ----------------------------

  /**
   * Render the commands in the given text.
   *
   * @param    inText the text containing commands
   *
   * @return   the rendered text
   *
   */
  public String renderCommands(String inText)
  {
    if(inText.isEmpty())
      return inText;

    String text = inText.replace("\n\n", "\\par ");

    if(text.indexOf(s_command) < 0)
      return text;

    // mark the brackets in the text
    final StringBuilder builder = new StringBuilder();
    text = markBrackets(text, s_escape, s_argStart, s_argEnd,
                        s_markArgStart, s_markArgEnd);
    text = markBrackets(text, s_escape, s_optArgStart, s_optArgEnd,
                        s_markOptArgStart, s_markOptArgEnd);

    for(int start = 0; start < text.length(); )
    {
      // search the first command (don't accept escaped commands)
      int pos = -1;
      for(pos = text.indexOf(s_command, start); pos >= 0;
          pos = text.indexOf(s_command, pos + 1))
        if((pos == 0 || text.charAt(pos - 1) != s_escape)
           && (s_escape != s_command || text.charAt(pos + 1) != s_command)
           && (Character.isLetterOrDigit(text.charAt(pos + 1))
               || s_special.indexOf(text.charAt(pos + 1)) >= 0))
          break;

      if(pos < 0)
      {
        // no commands any more
        builder.append(clean(text.substring(start)));

        break;
      }

      // intermediate text
      if(pos > 0)
        builder.append(clean(text.substring(start, pos)));

      // ok, we really have a command now
      int end = pos + 1;
      if(Character.isLetterOrDigit(text.charAt(end)))
      {
        for( ; end < text.length(); end++)
          if(!Character.isLetterOrDigit(text.charAt(end)))
            break;
      }
      else
        for( ; end < text.length(); end++)
          if(s_special.indexOf(text.charAt(end)) < 0)
            break;

      final String name = text.substring(pos + 1, end);

      // extract the optional arguments
      final List<String> optionals = new ArrayList<String>();

      Pattern pattern = Pattern.compile("^\\s*" + s_markOptArgStart
                                        + "<(\\d+)>((?:[^" + s_markOptArgEnd
                                        + "]*?" + s_markOptArgEnd
                                        + "<\\1>\\s*" + s_markOptArgStart
                                        + "<\\1>)*[^" + s_markOptArgEnd
                                        + "]*?)" + s_markOptArgEnd + "<\\1>");

      Matcher matcher = pattern.matcher(text.substring(end));
      if(matcher.find())
      {
        String []args = matcher.group(2).split(s_markOptArgEnd + "<"
                                               + matcher.group(1) + ">\\s*"
                                               + s_markOptArgStart + "<"
                                               + matcher.group(1) + ">");

        end += matcher.end();

        // now we have all the arguments, we need to parse them as well
        for(int i = 0; i < args.length; i++)
          optionals.add(renderCommands(args[i]));
      }

      // extract the arguments (we have to copy the above because we need to
      // update the position in the String as well as extracting the arguments;
      // to solve that, the method would not be able to be static)
      final List<String> arguments = new ArrayList<String>();

      pattern = Pattern.compile("^\\s*" + s_markArgStart + "<(\\d+)>((?:[^"
                                + s_markArgEnd + "]*?" + s_markArgEnd
                                + "<\\1>\\s*" + s_markArgStart
                                + "<\\1>)*[^" + s_markArgEnd + "]*?)"
                                + s_markArgEnd + "<\\1>");

      matcher = pattern.matcher(text.substring(end));
      if(matcher.find())
      {
        String []args = matcher.group(2).split(s_markArgEnd + "<"
                                               + matcher.group(1) + ">\\s*"
                                               + s_markArgStart + "<"
                                               + matcher.group(1) + ">");

        end += matcher.end();

        // now we have all the arguments, we need to parse them as well
        for(int i = 0; i < args.length; i++)
          arguments.add(renderCommands(args[i]));
      }

      // now we try to render the command (we have to use an additional
      // template to avoid exceptions because a rendering is already in place)
      // TODO: need to catch proper exception here
      try
      {
        builder.append(m_template.render
                       (m_commandPrefix + "." + name,
                        new SoyMapData("opt", optionals, "arg", arguments),
                        null, null));
      }
      catch(com.google.template.soy.tofu.SoyTofuException e)
      {
        // we assume the template does not exist, thus let's just add an error
        builder.append("<div class=\"error\">" + name + ": " + e.getMessage()
                       + "</div>");
        e.printStackTrace(System.err);
      }

      // if no arguments were found, then skip the character directly following
      // the command (must be a white space)
      if(arguments.size() == 0 && optionals.size() == 0
         && text.length() > end
         && Character.isWhitespace(text.charAt(end)))
        end++;

      start = end;
    }

    return builder.toString();
  }

  //........................................................................
  //----------------------------- markBrackets -----------------------------

  /**
   * Mark (and remove) the brackets given as arguments in the given string.
   * Brackets are marked with a number denoting their nesting level, i.e.
   * with marker<0> for nesting level 0.
   *
   * @param       inText        the text to replace in
   * @param       inEscape      the escape character to denote brackets to
   *                            ignore
   * @param       inStart       the start bracket character
   * @param       inEnd         the end bracket character
   * @param       inMarkerStart the character to use as marker start (will be
   *                            followed by the nesting leven in pointy
   *                            brackets)
   * @param       inMarkerEnd   the character to use as marker end (will be
   *                            followed by the nesting leven in pointy
   *                            brackets)
   *
   * @return      the text with all brackets replaced
   *
   */
  protected static String markBrackets(String inText,
                                       char inEscape,
                                       char inStart, char inEnd,
                                       char inMarkerStart,
                                       char inMarkerEnd)
  {
    assert inStart != '<' : "cannot replace '<' brackets";
    assert inEnd != '>' : "cannot replace '>' brackets";

    // remove all escaped markers
    inText = inText.replaceAll("\\" + inEscape + "\\" + inStart,
                               "" + s_markStart);
    inText = inText.replaceAll("\\" + inEscape + "\\" + inEnd, "" + s_markEnd);

    // we mark all bracket markers for easier replacement
    Pattern pattern = Pattern.compile("\\" + inStart + "([^\\" + inStart
                                      + "\\" + inEnd + "]*?)\\" + inEnd,
                                      Pattern.DOTALL);

    int i = 0;
    for(Matcher matcher = pattern.matcher(inText); matcher.find(0);
        matcher = pattern.matcher(inText))
      // replace the nested brackets
      inText = matcher.replaceAll(inMarkerStart + "<#" + i + "#>$1"
                                  + inMarkerEnd + "<#" + i++ + "#>");

    // 'invert' the number to make sure that really the nesting level starts
    // at 0 on the outside not on the inside
    // with the above, {{}} is {<1>{<0>}<0>}<1> instead of {<0>{<1>}<1>}<0>
    pattern = Pattern.compile(inMarkerStart + "<#(\\d+)#>(.*?)"
                              + inMarkerEnd + "<#\\1#>", Pattern.DOTALL);

    i = 0;
    for(Matcher matcher = pattern.matcher(inText);
        matcher.find(0);
        matcher = pattern.matcher(inText))
      // replace the nested brackets
      inText = matcher.replaceAll(inMarkerStart + "<" + i + ">$2"
                                  + inMarkerEnd + "<" + i++ + ">");

    // replace all non 0 markers (we can't leave nested markers or parsing
    // of multiple arguments may fail
    inText = inText.replaceAll(inMarkerStart + "<[1-9]\\d*>", "" + inStart);
    inText = inText.replaceAll(inMarkerEnd   + "<[1-9]\\d*>", "" + inEnd);

    // replace removed escaped markers
    inText = inText.replaceAll("" + s_markStart, "\\" + inEscape + inStart);
    inText = inText.replaceAll("" + s_markEnd, "\\" + inEscape + inEnd);

    return inText;
  }

  //........................................................................
  //-------------------------------- clean ---------------------------------

  /**
   * Remove all the markers from the text and replace the brackets back.
   *
   * @param       inText the text to replace the markers
   *
   * @return      the replaced text
   *
   */
  protected static String clean(String inText)
  {
    inText = inText.replaceAll(s_markArgStart + "<\\d+>", "" + s_argStart);
    inText = inText.replaceAll(s_markArgEnd + "<\\d+>", "" + s_argEnd);
    inText = inText.replaceAll(s_markOptArgStart + "<\\d+>",
                               "" + s_optArgStart);
    inText = inText.replaceAll(s_markOptArgEnd + "<\\d+>", "" + s_optArgEnd);

    return inText;
  }

  //........................................................................

  //........................................................................

  //------------------------------------------------------------------- test

  /** The tests. */
  public static class Test extends net.ixitxachitls.util.test.TestCase
  {
    //----- mark -----------------------------------------------------------

    /** Testing marking of passages. */
    @org.junit.Test
    public void mark()
    {
      assertEquals("simple", "\001<0>abc\002<0>",
                   markBrackets("{abc}", '\\', '{', '}', '\001', '\002'));
      assertEquals("multiple", "\001<0>abc\002<0>  \001<0>\002<0>",
                   markBrackets("{abc}  {}", '\\', '{', '}', '\001', '\002'));
      assertEquals("nested",
                   "\001<0>a{b{}}{{}}c\002<0>\001<0>\002<0>",
                   markBrackets("{a{b{}}{{}}c}{}", '\\', '{', '}', '\001',
                                '\002'));
      assertEquals("escaped", "\001<0>a\\{b\\}c\002<0>",
                   markBrackets("{a\\{b\\}c}", '\\', '{', '}', '\001',
                                '\002'));
      assertEquals("incomplete", "{a\001<0>b\002<0>{",
                   markBrackets("{a{b}{", '\\', '{', '}', '\001', '\002'));
    }

    //......................................................................
    //----- renderCommands -------------------------------------------------

    /** The renderCommands Test. */
    @org.junit.Test
    public void renderCommands()
    {
      SoyRenderer renderer =
        new SoyRenderer(new SoyTemplate("lib/test/soy/test"));

      renderer.m_commandPrefix = "test.commands";
      assertEquals("empty", "", renderer.renderCommands(""));
      assertEquals("text", "just a text",
                   renderer.renderCommands("just a text"));
      assertEquals("commands", "just a -- command --",
                   renderer.renderCommands("just a \\bold{command}"));
    }

    //......................................................................
    //----- render ---------------------------------------------------------

    /** The render Test. */
    @org.junit.Test
    public void render()
    {
      SoyRenderer renderer =
        new SoyRenderer(new SoyTemplate("lib/test/soy/test"));

      renderer.m_commandPrefix = "test.commands";
      assertEquals("render",
                   "first: first data second: second data "
                   + "third: first injected fourth: second injected "
                   + "fifth: jDMA",
                   renderer.render
                   ("test.commands.test",
                    SoyTemplate.map("first", "first data",
                                    "second", "second data"),
                    SoyTemplate.map("first", "first injected",
                                    "second", "second injected")));
    }

    //......................................................................

  }

  //........................................................................
}
