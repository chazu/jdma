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

package net.ixitxachitls.dma.entries.indexes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.google.common.base.Joiner;

import net.ixitxachitls.dma.data.DMAData;
import net.ixitxachitls.dma.entries.AbstractEntry;
import net.ixitxachitls.dma.entries.AbstractType;
import net.ixitxachitls.dma.output.html.HTMLDocument;
import net.ixitxachitls.output.commands.Command;
import net.ixitxachitls.output.commands.Divider;
import net.ixitxachitls.output.commands.Editable;
import net.ixitxachitls.output.commands.Icon;
import net.ixitxachitls.output.commands.Link;
import net.ixitxachitls.output.commands.Par;
import net.ixitxachitls.output.commands.Table;
import net.ixitxachitls.output.commands.Title;
import net.ixitxachitls.output.html.HTMLWriter;
import net.ixitxachitls.util.Encodings;
import net.ixitxachitls.util.Pair;
import net.ixitxachitls.util.logging.Log;

//..........................................................................

//------------------------------------------------------------------- header

/**
 * An index that groups entries into a secondary level of names before
 * displaying.
 *
 *
 * @file          GroupedIndex.java
 *
 * @author        balsiger@ixitxachitels.net (Peter Balsiger)
 *
 */

//..........................................................................

//__________________________________________________________________________

@Immutable
public abstract class GroupedIndex extends Index
{
  //--------------------------------------------------------- constructor(s)

  //----------------------------- GroupedIndex -----------------------------

  /**
   * Create the simple index.
   *
   * @param         inTitle     the index title
   * @param         inType      the type of entries served
   * @param         inLevels    the number of grouping levels (1 means one
   *                            name index pointing to entries, 2 means a first
   *                            name index pointing to a second name index
   *                            pointing to a list of entries)
   *
   */
  public GroupedIndex(@Nonnull String inTitle,
                      AbstractType<? extends AbstractEntry> inType,
                      int inLevels)
  {
    this(inTitle, inType, inLevels, String.CASE_INSENSITIVE_ORDER);
  }

  //........................................................................
  //----------------------------- GroupedIndex -----------------------------

  /**
   * Create the simple index.
   *
   * @param         inTitle      the index title
   * @param         inType       the type of entries served
   * @param         inLevels     the number of grouping levels (1 means one
   *                             name index pointing to entries, 2 means a first
   *                             name index pointing to a second name index
   *                             pointing to a list of entries)
   * @param         inComparator the comparator for ordering groups
   *
   */
  public GroupedIndex(@Nonnull String inTitle,
                      AbstractType<? extends AbstractEntry> inType,
                      int inLevels, @Nonnull Comparator<String> inComparator)
  {
    super(inTitle, inType);

    if(inLevels < 0)
      throw new IllegalArgumentException("must have a positive number of "
                                         + "levels here");
    m_levels = inLevels;
    m_comparator = inComparator;
  }

  //........................................................................

  //........................................................................

  //-------------------------------------------------------------- variables

  /** The number of grouping levels. */
  private int m_levels;

  /** The comparator for ordering groups. */
  private @Nonnull Comparator<String> m_comparator;

  /** Joiner to concatenare groups. */
  private static Joiner s_groupJoiner = Joiner.on(" - ");

  /** Joiner to concatenare groups. */
  private static Joiner s_pathJoiner = Joiner.on("/");

  //........................................................................

  //-------------------------------------------------------------- accessors

  //---------------------------- getNavigation -----------------------------

  /**
   * Get the navigation information for the current index.
   *
   * @param       inName the index name
   * @param       inPath the path to the index to print
   *
   * @return      an array if text/url pairs for the navigation
   *
   */
  public @Nonnull String [] getNavigation(@Nonnull String inName,
                                          @Nonnull String inPath)
  {
    List<String> result = new ArrayList<String>();

    // main navigation
    StringBuffer path = new StringBuffer("/" + getType().getMultipleLink());
    result.add(getType().getMultipleLink());
    result.add(path.toString());

    // index navigation
    path.append("/" + inName);
    result.add(inName);
    result.add(path.toString());

    // grouped levels
    for(String group : groups(inPath))
    {
      path.append("/" + group);
      result.add(group);
      result.add(path.toString());
    }

    return result.toArray(new String[result.size()]);
  }

  //........................................................................
  //-------------------------------- groups --------------------------------

  /**
   * Get the groups for the index.
   *
   * @param       inPath the url path to the index
   *
   * @return      the groups to select the data for the index
   *
   */
  public @Nonnull String [] groups(@Nullable String inPath)
  {
    if(inPath == null || inPath.isEmpty())
      return new String[0];

    return inPath.split("/");
  }

  //........................................................................

  //........................................................................

  //----------------------------------------------------------- manipulators
  //........................................................................

  //------------------------------------------------- other member functions

  //-------------------------------- write ---------------------------------

  /**
   * Write the contents of the index to the given writer and request.
   *
   * @param      inWriter     the writer to output to
   * @param      inData       the data to print
   * @param      inName       the naming part of the index url
   * @param      inPath       the sub path of the index
   * @param      inPageSize   the size of the page as number of elements
   * @param      inPagination start and end entries to show
   *
   */
  public void write(@Nonnull HTMLWriter inWriter,
                    @Nonnull DMAData inData,
                    @Nonnull String inName,
                    @Nonnull String inPath,
                    int inPageSize, Pair<Integer, Integer> inPagination)
  {
    inWriter.title(getTitle());

    String []groups = groups(inPath);
    if(groups.length < m_levels)
      writeNames(inWriter, inData, inName, groups);
    else
      writeEntries(inWriter, inData, inName, inPath, groups, inPageSize,
                   inPagination);
  }

  //........................................................................
  //------------------------------ writeNames ------------------------------

  /**
   * Handle the name index.
   *
   * @param       inWriter  the output to write to
   * @param       inData    the data to print
   * @param       inPath    the path to the index pages
   * @param       inGroups  the groups for each level
   *
   */
  protected void writeNames(@Nonnull HTMLWriter inWriter,
                            @Nonnull DMAData inData,
                            @Nonnull String inPath,
                            @Nonnull String []inGroups)
  {
    // the general index
    Log.info("serving dynamic " + getTitle() + " index");

    // compute all the different category names
    SortedSet<String> names = new TreeSet<String>(m_comparator);
    names(names, inData, inGroups);

    String title;
    String titleValue;
    if(inGroups.length == 0)
      titleValue = getTitle();
    else
      titleValue = s_groupJoiner.join(inGroups);

    title = Encodings.toWordUpperCase(titleValue);

    HTMLDocument document = new HTMLDocument(title);

    if(hasImages())
      document.add(new Title(new Editable("*/" + title, getType(),
                                          new Icon(inPath + "/index.png",
                                                   title, "", true),
                                          getTitle() + "/" + title, titleValue,
                                          "name")));
    else
      document.add(new Title(new Editable("*/" + title, getType(), title,
                                          getTitle() + "/" + title, titleValue,
                                          "name")));

    document.add(new Par());

    List<Command> commands = new ArrayList<Command>();

    for(String name : names)
      if(hasImages())
        commands.add(new Icon(inPath + "/" + name.toLowerCase(Locale.US)
                              + ".png", name, inPath + "/" + name, true));
      else
        commands.add(new Link(new Divider("index-overview", name),
                              inPath + "/" + name, "index-link"));

    commands.add(new Divider("clear", ""));

    document.add(new Command(commands.toArray()));

    inWriter.add(document.toString());
  }

  //........................................................................
  //----------------------------- writeEntries -----------------------------

  /**
   * Handle a detailed index.
   *
   * @param       inWriter     the writer to output to
   * @param       inData       the data to print
   * @param       inName       the name of the index
   * @param       inPath       the path to the page
   * @param       inGroups     the groups for the page
   * @param       inPageSize   the size of the page as number of elements
   * @param       inPagination start and end entries to show
   *
   Ba   */
  protected void writeEntries(@Nonnull HTMLWriter inWriter,
                              @Nonnull DMAData inData,
                              @Nonnull String inName,
                              @Nonnull String inPath,
                              @Nonnull String []inGroups,
                              int inPageSize,
                              @Nonnull Pair<Integer, Integer> inPagination)
  {
    // determine start and end of index to show
    int start = inPagination.first();
    int end   = inPagination.second();

    if(end <= 0)
      end = start + inPageSize;

    Log.info("serving dynamic " + getTitle() + " index '" + inName + "/"
             + inPath + "'");

    String title;
    if(inGroups.length == 0)
      title = getTitle();
    else
      title = getTitle() + " - "
        + Encodings.toWordUpperCase(s_groupJoiner.join(inGroups));

    // create a detailed index file
    HTMLDocument document = new HTMLDocument(title);

    if(hasImages())
      document.add(new Title(new Editable
                             ("", getType(),
                              new Icon(inName.toLowerCase(Locale.US) + ".png",
                                       inPath, inPath, true),
                              getTitle() + "/" + title, inPath, "string")));
    else
      if(inGroups.length == 1)
        document.add(new Title(new Editable("*/" + title, getType(), title,
                                            getTitle() + "/" + title,
                                            inPath, "string")));
      else
        document.add(new Title(title));

    List<String> navigation = new ArrayList<String>();
    if(isPaginated())
      if(start > 0)
        if(start - inPageSize > 0)
          navigation.add("<a href=\"?start="
                         + (start - inPageSize)
                         + "\"  onclick=\"return util.link(event, '?start="
                         + (start - inPageSize) + "&end=" + end
                         + "');\" "
                         + "class=\"paginate-previous\">"
                         + "&laquo; previous</a>");
        else
          navigation.add("<a href=\"?\" "
                         + "onclick=\"return util.link(event, '?');\" "
                         + "class=\"paginate-previous\">"
                         + "&laquo; previous</a>");

    document.add(navigation);

    // TODO: extract this from the request
    boolean dm = true;

    List<? extends AbstractEntry> entries = inData.getEntriesList(getType());
    List<Object> cells = new ArrayList<Object>();
    for(AbstractEntry entry : entries)
      if(matches(inGroups, entry))
        cells.addAll(entry.printList(entry.getName(), dm));

    document.add(new Table("entrylist", entries.get(0).getListFormat(),
                           new Command(cells)));

    document.add(navigation);

    inWriter.add(document.toString());
  }

  //........................................................................

  //-------------------------------- names --------------------------------

  /**
   * Build up all the names to put up into the index.
   *
   * @param       ioNames  the names collected so far
   * @param       inData   all the data to used in the index
   * @param       inGroups the groups selected already
   *
   * @return      a set with all the names (usually Strings, but can be
   *              anything)
   *
   */
  public abstract @Nonnull Set<String> names(@Nonnull Set<String> ioNames,
                                             @Nonnull DMAData inData,
                                             @Nonnull String []inGroups);

  //........................................................................
  //------------------------------- matches --------------------------------

  /**
   * Check if the given entry matches the name.
   *
   * @param       inGroups the names for each group (level)
   * @param       inEntry   the entry to match on
   *
   * @return      true if entry matches the index, false if not
   *
   */
  public abstract boolean matches(@Nonnull String []inGroups,
                                  @Nonnull AbstractEntry inEntry);

  //........................................................................

  //........................................................................

  //------------------------------------------------------------------- test

  /** The test. */
  public static class Test extends net.ixitxachitls.util.test.TestCase
  {
    //----- writeFirst -----------------------------------------------------

    /** Testing building of commands. */
    @org.junit.Test
    public void writeFirst()
    {
      GroupedIndex index =
        new GroupedIndex("title",
                         net.ixitxachitls.dma.entries.BaseCharacter.TYPE, 2)
        {
          private static final long serialVersionUID = 1L;

          public Set<String> names(@Nonnull Set<String> ioNames,
                                   @Nonnull DMAData inData,
                                   @Nonnull String []inGroups)
          {
            ioNames.add("first1");
            ioNames.add("first2");

            return ioNames;
          }

          public boolean matches(String []inGroups, AbstractEntry inEntry)
          {
            return true;
          }
        };
      index.withoutPagination();

      assertEquals("title", "title", index.getTitle());
      assertFalse("images", index.hasImages());
      assertFalse("paginated", index.isPaginated());

      java.io.StringWriter content = new java.io.StringWriter();
      HTMLWriter writer = new HTMLWriter(new java.io.PrintWriter(content));

      index.write(writer,
                  new DMAData.Test.Data(new net.ixitxachitls.dma.entries
                                        .BaseCharacter("first",
                                                       new DMAData("path")),
                                        new net.ixitxachitls.dma.entries
                                        .BaseCharacter("second",
                                                       new DMAData("path"))),
                  "index-name", "", 50,
                  new Pair<Integer, Integer>(0, 10));
      writer.close();

      assertEquals("content",
                   "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                   + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN"
                   + "\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"
                   + "\">\n"
                   + "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n"
                   + "<HTML>\n"
                   + "  <HEAD>\n"
                   + "    <TITLE>title</TITLE>\n"
                   + "  </HEAD>\n"
                   + "  <BODY>\n"
                   + "    \n"
                   + "<h1><dmaeditable key=\"title/Title\" value=\"title\" "
                   + "id=\"*/Title\" class=\"editable\" "
                   + "entry=\"base character\" type=\"name\"><span>Title"
                   + "</span></dmaeditable></h1>\n"
                   + "\n"
                   + "<p />\n"
                   + "<a href=\"index-name/first1\" class=\"index-link\" "
                   + "onclick=\"return util.link(event, "
                   + "'index-name/first1');\">"
                   + "<div class=\"index-overview\">first1</div></a>"
                   + "<a href=\"index-name/first2\" class=\"index-link\" "
                   + "onclick=\"return util.link(event, "
                   + "'index-name/first2');\">"
                   + "<div class=\"index-overview\">first2</div></a>"
                   + "<div class=\"clear\"></div>\n"
                   + "  </BODY>\n"
                   + "</HTML>\n", content.toString());

      m_logger.addExpected("WARNING: cannot find file "
                           + "'path/Products/first.dma'");
      m_logger.addExpected("WARNING: cannot find file "
                           + "'path/Products/second.dma'");
    }

    //......................................................................
    //----- writeSecond ---------------------------------------------------

    /** Testing building of commands. */
    @org.junit.Test
    public void writeSecond()
    {
      GroupedIndex index =
        new GroupedIndex("title",
                         net.ixitxachitls.dma.entries.BaseCharacter.TYPE, 2)
        {
          private static final long serialVersionUID = 1L;

          public Set<String> names(@Nonnull Set<String> ioNames,
                                   @Nonnull DMAData inData,
                                   @Nonnull String []inGroups)
          {
            ioNames.add("second");

            return ioNames;
          }

          public boolean matches(String []inGroups, AbstractEntry inEntry)
          {
            return true;
          }
        };
      index.withoutPagination();

      assertEquals("title", "title", index.getTitle());
      assertFalse("images", index.hasImages());
      assertFalse("paginated", index.isPaginated());

      java.io.StringWriter content = new java.io.StringWriter();
      HTMLWriter writer = new HTMLWriter(new java.io.PrintWriter(content));

      index.write(writer,
                  new DMAData.Test.Data(new net.ixitxachitls.dma.entries
                                        .BaseCharacter("first",
                                                       new DMAData("path")),
                                        new net.ixitxachitls.dma.entries
                                        .BaseCharacter("second",
                                                       new DMAData("path"))),
                  "index-name", "first1", 50,
                  new Pair<Integer, Integer>(0, 10));
      writer.close();

      assertEquals("content",
                   "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                   + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN"
                   + "\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"
                   + "\">\n"
                   + "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n"
                   + "<HTML>\n"
                   + "  <HEAD>\n"
                   + "    <TITLE>title</TITLE>\n"
                   + "  </HEAD>\n"
                   + "  <BODY>\n"
                   + "    \n"
                   + "<h1><dmaeditable key=\"title/First1\" value=\"first1\" "
                   + "id=\"*/First1\" class=\"editable\" "
                   + "entry=\"base character\" type=\"name\"><span>First1"
                   + "</span></dmaeditable></h1>\n"
                   + "\n"
                   + "<p />\n"
                   + "<a href=\"index-name/second\" class=\"index-link\" "
                   + "onclick=\"return util.link(event, "
                   + "'index-name/second');\">"
                   + "<div class=\"index-overview\">second</div>"
                   + "</a><div class=\"clear\"></div>\n"
                   + "  </BODY>\n"
                   + "</HTML>\n", content.toString());

      m_logger.addExpected("WARNING: cannot find file "
                           + "'path/Products/first.dma'");
      m_logger.addExpected("WARNING: cannot find file "
                           + "'path/Products/second.dma'");
    }

    //......................................................................
    //----- writeEntries ---------------------------------------------------

    /** Testing building of commands. */
    @org.junit.Test
    public void writeEntries()
    {
      GroupedIndex index =
        new GroupedIndex("title",
                         net.ixitxachitls.dma.entries.BaseCharacter.TYPE, 2)
        {
          private static final long serialVersionUID = 1L;

          public Set<String> names(@Nonnull Set<String> ioNames,
                                   @Nonnull DMAData inData,
                                   @Nonnull String []inGroups)
          {
            return ioNames;
          }

          public boolean matches(String []inGroups, AbstractEntry inEntry)
          {
            return true;
          }
        };
      index.withoutPagination();

      assertEquals("title", "title", index.getTitle());
      assertFalse("images", index.hasImages());
      assertFalse("paginated", index.isPaginated());

      java.io.StringWriter content = new java.io.StringWriter();
      HTMLWriter writer = new HTMLWriter(new java.io.PrintWriter(content));

      index.write(writer,
                  new DMAData.Test.Data(new net.ixitxachitls.dma.entries
                                        .BaseCharacter("first",
                                                       new DMAData("path")),
                                        new net.ixitxachitls.dma.entries
                                        .BaseCharacter("second",
                                                       new DMAData("path"))),
                  "index-name", "first1/second", 50,
                  new Pair<Integer, Integer>(0, 10));
      writer.close();

      assertEquals("content",
                   "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                   + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN"
                   + "\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"
                   + "\">\n"
                   + "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n"
                   + "<HTML>\n"
                   + "  <HEAD>\n"
                   + "    <TITLE>title</TITLE>\n"
                   + "  </HEAD>\n"
                   + "  <BODY>\n"
                   + "    \n"
                   + "<h1>title - First1 - Second</h1>\n"
                   + "\n"
                   + "<table class=\"entrylist\"><tr class=\"title\">"
                   + "<td class=\"title\"></td><td class=\"title\">Name</td>"
                   + "<td class=\"title\">Real Name</td>"
                   + "<td class=\"title\">Group</td>"
                   + "<td class=\"title\">Last Login</td>"
                   + "<td class=\"title\">Last Action</td>"
                   + "</tr><tr><td class=\"label\">"
                   + "<img src=\"/icons/labels/BaseCharacter.png\" "
                   + "alt=\"BaseCharacter\" class=\"image label\"/> "
                   + "<div id=\"linkrow-user-first\" class=\"\">\n"
                   + "<script type='text/javascript'>"
                   + "util.linkRow(document.getElementById"
                   + "('linkrow-user-first'), '/user/first');</script>\n"
                   + "</div></td><td class=\"name\">first</td>"
                   + "<td class=\"name\"><dmaeditable key=\"real name\" "
                   + "value=\"$undefined$\" id=\"first\" class=\"editable\" "
                   + "entry=\"base character\" type=\"string\"><span></span>"
                   + "</dmaeditable></td>"
                   + "<td class=\"group\"><dmaeditable key=\"group\" "
                   + "value=\"$undefined$\" id=\"first\" class=\"editable\" "
                   + "entry=\"base character\" type=\"selection\" note=\"\" "
                   + "values=\"Guest||User||Player||DM||Admin\"><span></span>"
                   + "</dmaeditable></td>"
                   + "<td class=\"last\"></td>"
                   + "<td class=\"action\"></td></tr>"
                   + "<tr><td class=\"label\">"
                   + "<img src=\"/icons/labels/BaseCharacter.png\" "
                   + "alt=\"BaseCharacter\" class=\"image label\"/> "
                   + "<div id=\"linkrow-user-second\" class=\"\">\n"
                   + "<script type='text/javascript'>"
                   + "util.linkRow(document.getElementById"
                   + "('linkrow-user-second'), '/user/second');</script>\n"
                   + "</div></td><td class=\"name\">second</td>"
                   + "<td class=\"name\"><dmaeditable key=\"real name\" "
                   + "value=\"$undefined$\" id=\"second\" class=\"editable\" "
                   + "entry=\"base character\" type=\"string\"><span></span>"
                   + "</dmaeditable></td>"
                   + "<td class=\"group\"><dmaeditable key=\"group\" "
                   + "value=\"$undefined$\" id=\"second\" class=\"editable\" "
                   + "entry=\"base character\" type=\"selection\" note=\"\" "
                   + "values=\"Guest||User||Player||DM||Admin\"><span></span>"
                   + "</dmaeditable></td>"
                   + "<td class=\"last\"></td>"
                   + "<td class=\"action\"></td></tr></table>\n"
                   + "  </BODY>\n"
                   + "</HTML>\n", content.toString());

      m_logger.addExpected("WARNING: cannot find file "
                           + "'path/Products/first.dma'");
      m_logger.addExpected("WARNING: cannot find file "
                           + "'path/Products/second.dma'");
    }

    //......................................................................
    //----- groups ---------------------------------------------------------

    /** The groups Test. */
    @org.junit.Test
    public void groups()
    {
      GroupedIndex index =
        new GroupedIndex("title",
                         net.ixitxachitls.dma.entries.BaseCharacter.TYPE, 2)
        {
          private static final long serialVersionUID = 1L;

          public Set<String> names(@Nonnull Set<String> ioNames,
                                   @Nonnull DMAData inData,
                                   @Nonnull String []inGroups)
          {
            return ioNames;
          }

          public boolean matches(String []inGroups, AbstractEntry inEntry)
          {
            return true;
          }
        };

      assertEquals("null", "[]", java.util.Arrays.toString(index.groups(null)));
      assertEquals("empty", "[]", java.util.Arrays.toString(index.groups("")));
      assertEquals("single", "[first]",
                   java.util.Arrays.toString(index.groups("first")));
      assertEquals("double", "[first, second]",
                   java.util.Arrays.toString(index.groups("first/second")));
      assertEquals("trailing /", "[first, second]",
                   java.util.Arrays.toString(index.groups("first/second/")));
      assertEquals("leading /", "[, first, second]",
                   java.util.Arrays.toString(index.groups("/first/second")));
    }

    //......................................................................
    //----- navigation -----------------------------------------------------

    /** The navigation Test. */
    @org.junit.Test
    public void navigation()
    {
      GroupedIndex index =
        new GroupedIndex("title",
                         net.ixitxachitls.dma.entries.BaseCharacter.TYPE, 2)
        {
          private static final long serialVersionUID = 1L;

          public Set<String> names(@Nonnull Set<String> ioNames,
                                   @Nonnull DMAData inData,
                                   @Nonnull String []inGroups)
          {
            return ioNames;
          }

          public boolean matches(String []inGroups, AbstractEntry inEntry)
          {
            return true;
          }
        };

      assertEquals("navigation",
                   "[users, /users, "
                   + "name, /users/name, "
                   + "first, /users/name/first, "
                   + "second, /users/name/first/second]",
                   java.util.Arrays.toString(index.getNavigation
                                             ("name", "first/second")));
    }

    //......................................................................
  }

  //........................................................................
}
