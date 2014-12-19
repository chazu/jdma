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

package net.ixitxachitls.dma.server.servlets;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import com.google.template.soy.data.SoyData;

import org.easymock.EasyMock;

import net.ixitxachitls.dma.data.DMADataFactory;
import net.ixitxachitls.dma.entries.AbstractEntry;
import net.ixitxachitls.dma.entries.AbstractType;
import net.ixitxachitls.dma.entries.BaseCharacter;
import net.ixitxachitls.dma.entries.indexes.Index;
import net.ixitxachitls.dma.output.soy.SoyRenderer;
import net.ixitxachitls.dma.output.soy.SoyValue;
import net.ixitxachitls.util.Strings;
import net.ixitxachitls.util.logging.Log;

/**
 * The base handler for all indexes.
 *
 * @file          IndexServlet.java
 * @author        balsiger@ixitxachitls.net (Peter Balsiger)
 */

@Immutable
public class IndexServlet extends PageServlet
{
  /**
   * Create the servlet for indexes.
   */
  public IndexServlet()
  {
  }

  /** The id for serialization. */
  private static final long serialVersionUID = 1L;

  @Override
  protected String getTemplateName(DMARequest inDMARequest,
                                   Map<String, SoyData> inData)
  {
    if(inData.get("path") == null
        || inData.get("name") == null
        || inData.get("title") == null)
      return "dma.errors.invalidPage";

    return "dma.entry.indexoverview";
  }

  @Override
  protected Map<String, Object> collectData(DMARequest inRequest,
                                            SoyRenderer inRenderer)
  {
    Map<String, Object> data = super.collectData(inRequest, inRenderer);

    String path = inRequest.getRequestURI();
    if(path == null)
      return data;

    String []match =
      Strings.getPatterns(path, "^/_index/([^/]+)/([^/]+)(?:/(.*$))?");
    Optional<? extends AbstractType<? extends AbstractEntry>> type = null;
    String name = null;
    String group = null;

    if(match.length == 3)
    {
      type = AbstractType.getTyped(match[0]);
      name = match[1];
      group = match[2];
    }

    if(name == null || name.isEmpty() || !type.isPresent())
    {
      return data;
    }

    name = name.replace("%20", " ");
    data.put("name", name);
    data.put("type", type.get().getMultipleLink());

    if(group != null)
      group = group.replace("%20", " ");

    data.put("group", group);

    // determine the index to use
    // TODO: this needs to be fixed. We currently don't show indexes anywere.
    Index index = null; //ValueGroup.getIndex(name, type);
    if(index == null)
      return data;

    Log.info("serving dynamic " + type + " index '" + name + "/"
             + group + "'");

    String title = index.getTitle();
    data.put("title", title);
    if(group == null)
    {
      // get all the index groups available
      // SortedSet<String> indexes =
      //   DMADataFactory.get().getIndexNames(name, type, false);
      SortedSet<String> indexes =
        DMADataFactory.get().getValues(type.get(), Index.PREFIX + name);

      if(indexes.size() == 1)
        group = indexes.iterator().next();
      else
      {
        if(isNested(indexes))
        {
          SortedMap<String, List<String>> groups = nestedGroups(indexes);
          data.put("indexes", groups);
          data.put("keys", new ArrayList<String>(groups.keySet()));
        }
        else
          data.put("indexes", new ArrayList<String>(indexes));

        return data;
      }
    }

    title += " - " + group.replace("::", " ");

    List<? extends AbstractEntry> rawEntries =
      DMADataFactory.get().getIndexEntries(name, type.get(), null, group,
                                           inRequest.getStart(),
                                           inRequest.getPageSize() + 1);

    List<SoyValue> entries = new ArrayList<>();
    for(AbstractEntry entry : rawEntries)
      entries.add(new SoyValue(entry.getKey().toString(), entry));

    data.put("start", inRequest.getStart());
    data.put("pagesize", inRequest.getPageSize());
    data.put("entries", entries);

    return data;
  }

  /**
   * Generate the data structure for nested groups.
   *
   * @param  inValues the index groups
   *
   * @return A sorted map of index groups to indexs pages
   */
  public static SortedMap<String, List<String>> nestedGroups
    (SortedSet<String> inValues)
  {
    SortedSetMultimap<String, String> groups = TreeMultimap.create();
    for(String value : inValues)
    {
      String []parts = Index.stringToGroups(value);
      if(parts.length >= 2)
        groups.put(parts[0], parts[1]);
      else
        groups.put(parts[0], "");
    }

    SortedMap<String, List<String>> result =
      new TreeMap<String, List<String>>();
    for (String key : groups.keys())
      result.put(key, new ArrayList<String>(groups.get(key)));

    return result;
  }

  /**
   * Check if the index values represent a nested index or not.
   *
   * @param   inValues the index groups
   *
   * @return  true if nested groups are given, false if not
   */
  public static boolean isNested(SortedSet<String> inValues)
  {
    for(String value : inValues)
      if(value.contains("::"))
        return true;

    return false;
  }

  //----------------------------------------------------------------------------

  /** The test. */
  public static class Test extends net.ixitxachitls.server.ServerUtils.Test
  {
    /** The nested Test. */
    @org.junit.Test
    public void nested()
    {
      assertFalse("empty", isNested(ImmutableSortedSet.<String>of()));
      assertFalse("non-nested", isNested(ImmutableSortedSet.of("one")));
      assertFalse("non-nested", isNested(ImmutableSortedSet.of("one", "two")));
      assertTrue("nested", isNested(ImmutableSortedSet.of("one::nested")));
      assertTrue("nested",
                 isNested(ImmutableSortedSet.of("one", "two::nested")));
    }

    /** The groups Test. */
    @org.junit.Test
    public void groups()
    {
      assertEquals("empty", "{}",
                   nestedGroups(ImmutableSortedSet.<String>of()).toString());
      assertEquals("non-nested", "{one=[], three=[], two=[]}",
                   nestedGroups(ImmutableSortedSet.of("one", "two", "three"))
                   .toString());
      assertEquals("nested",
                   "{one=[first, second, third], three=[], two=[first]}",
                   nestedGroups(ImmutableSortedSet.of
                       ("one::first", "two::first", "one::second",
                        "one::third", "three"))
                       .toString());
    }

    /**
     * The nopath Test.
     *
     * @throws Exception should not happen
     */
    @org.junit.Test
    public void noPath() throws Exception
    {
      assertPattern("no path", ".*No path given for page..*", content(""));
    }

    /**
     * The invalid path Test.
     *
     * @throws Exception should not happen
     */
    @org.junit.Test
    public void invalidPath() throws Exception
    {
      assertPattern("invalid path", ".*No path given for page.*",
                    content("some page"));
    }

    /**
     *  The simple Test.
     *
     * @throws Exception should not happen
     */
    @org.junit.Test
    public void simple() throws Exception
    {
      assertPattern("invalid path", ".*No path given for page.*",
                    content("some page"));
      // TODO: the following does not work at the moment.
      /*
      assertEquals("simple",
                   "index overview (title: Worlds, "
                   + "indexes: [Index-1, Index-2, Index-3], "
                   + "name: worlds, keys: no keys)",
                 content("/_index/base item/worlds"));
                 */
    }

    /**
     * Check the contents of the generated page for a pattern.
     *
     * @param       inPath    the path to the page
     *
     * @return      the content generated
     *
     * @throws Exception should not happen
     */
    private String content(@Nullable String inPath) throws Exception
    {
      IndexServlet servlet = new IndexServlet();

      DMARequest request = EasyMock.createMock(DMARequest.class);
      HttpServletResponse response =
          EasyMock.createMock(HttpServletResponse.class);
      StringWriter writer = new StringWriter();

      response.setContentType("text/html");
      response.setCharacterEncoding("UTF-8");
      response.setHeader("Cache-Control", "max-age=0");
      EasyMock.expect(request.isBodyOnly()).andStubReturn(false);
      EasyMock.expect(request.getRequestURI()).andStubReturn(inPath);
      EasyMock.expect(request.getOriginalPath()).andStubReturn(inPath);
      EasyMock.expect(request.hasUser()).andStubReturn(true);
      EasyMock.expect(request.getUser()).andReturn(
          Optional.of(new BaseCharacter("test"))).anyTimes();
      EasyMock.expect(request.hasUserOverride()).andStubReturn(false);
      EasyMock.expect(response.getWriter())
          .andStubReturn(new PrintWriter(writer));
      EasyMock.replay(request, response);

      assertFalse("no error", servlet.handle(request, response).isPresent());

      EasyMock.verify(request, response);

      return writer.toString();
    }
  }
}

