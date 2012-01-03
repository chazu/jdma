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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.Multimap;

import org.easymock.EasyMock;

import net.ixitxachitls.dma.data.DMAData;
import net.ixitxachitls.dma.data.DMADataFactory;
import net.ixitxachitls.dma.entries.AbstractEntry;
import net.ixitxachitls.dma.entries.AbstractType;
import net.ixitxachitls.dma.entries.BaseCharacter;
import net.ixitxachitls.dma.entries.Entry;
import net.ixitxachitls.util.Encodings;
import net.ixitxachitls.util.Strings;
import net.ixitxachitls.util.logging.Log;

//..........................................................................

//------------------------------------------------------------------- header

/**
 * The servlet to save given entries.
 *
 * @file          SaveActionServlet.java
 *
 * @author        balsiger@ixitxachitls.net (Peter Balsiger)
 *
 */

//..........................................................................

//__________________________________________________________________________

public class SaveActionServlet extends ActionServlet
{
  //----------------------------------------------------------------- nested

  /**
   * Storage for the change information for one or multiple entries.
   */
  private static class Changes
  {
    /**
     * Create a change for one or multiple entries.
     *
     * @param inID       entry id
     * @param inType     entry type
     * @param inFullType the full type of the entries
     * @param inData     the data the entry is stored
     * @param inOwner    the owner of the entry/entries
     *
     */
    public Changes(@Nonnull String inID,
                   @Nonnull AbstractType<? extends AbstractEntry> inType,
                   @Nonnull String inFullType,
                   @Nonnull DMAData inData,
                   @Nonnull AbstractEntry inOwner)
    {
      m_id = inID;
      m_name = m_id;
      m_type = inType;
      m_fullType = inFullType;
      m_data = inData;
      m_owner = inOwner;
    }

    /** The id of the entry/entries changed. */
    protected @Nonnull String m_id;

    /** The type of entry/entries changed. */
    protected @Nonnull AbstractType<? extends AbstractEntry> m_type;

    /** The data where the entry/entries is stored. */
    protected @Nonnull DMAData m_data;

    /** The owner of the entry/entries changed. */
    protected @Nullable AbstractEntry m_owner;

    /** The name of the entry changed. */
    protected @Nullable String m_name;

    /** The file for the entry/entries. */
    protected @Nullable String m_file;

    /** The full type of the entriy/entries changed. */
    protected @Nonnull String m_fullType;

    /** A flag if multiple entries are affected by the change. */
    protected boolean m_multiple = false;

    /** A map with all the changed values. */
    protected @Nonnull Map<String, String>m_values =
      new HashMap<String, String>();

    /**
     * Convert to a human readable string for debugging.
     *
     * @return the converted string
     *
     */
    public @Nonnull String toString()
    {
      return m_name + " [" + m_id + "]/" + m_type + " ("
        + (m_owner == null ? "no owner" : m_owner.getName())
        + "/" + m_file + (m_multiple ? ", single" : ", multiple") + "):"
        + m_values;
    }

    /**
     * Set a value in the change for a given key.
     *
     * @param inKey   the key of the value to change
     * @param inValue the value to change to
     *
     */
    public void set(@Nonnull String inKey, @Nonnull String inValue)
    {
      if("file".equals(inKey))
        m_file = inValue;
      else
      {
        if("name".equals(inKey))
          m_name = inValue;

        m_values.put(inKey, inValue);
      }

      if(inKey.indexOf("/") >= 0)
        m_multiple = true;
    }

    /**
     * Figure out the affected entries.
     *
     * @param ioErrors the errors encountered, will be adjusted
     *
     * @return a set with all the entries affected by this change
     *
     */
    public @Nonnull Set<AbstractEntry> entries(@Nonnull List<String> ioErrors)
    {
      Set<AbstractEntry> entries = new HashSet<AbstractEntry>();
      if(m_multiple)
      {
        assert m_values.size() == 1 : "Only expected a single value to change";
        String []parts = m_values.keySet().iterator().next().split("/");
        String index = parts[0];
        String value = parts[1];
        entries.addAll(m_data.getIndexEntries(index, m_type, value, 0, 0));
      }
      else
      {
        AbstractEntry entry = m_data.getEntry(m_id, m_type);

        if(entry == null)
        {
          // create a new entry for filling out
          Log.event(m_owner.getID(), "create",
                    "creating " + m_type + " entry '" + m_name + "'");

          entry = m_type.create(m_name, m_data);
          entry.setOwner(m_owner);
        }

        if(entry == null)
        {
          String error = "could not find " + m_type + " " + m_name
            + " for saving";
          Log.warning(error);
          ioErrors.add("gui.alert(" + Encodings.toJSString(error) + ");");
        }
        else
          entries.add(entry);
      }

      return entries;
    }
  }

  //........................................................................

  //--------------------------------------------------------- constructor(s)

  //--------------------------- SaveActionServlet --------------------------

  /**
   * Create the entry action servlet.
   */
  public SaveActionServlet()
  {
    m_data = DMADataFactory.getBaseData();
  }

  //......................................................................

  //........................................................................

  //-------------------------------------------------------------- variables

  /** All the available data. */
  protected @Nonnull DMAData m_data;

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
  @SuppressWarnings("unchecked")
  protected @Nonnull String doAction(@Nonnull DMARequest inRequest,
                                     @Nonnull HttpServletResponse inResponse)
  {
    List<String> errors = new ArrayList<String>();
    Set<AbstractEntry> entries = new HashSet<AbstractEntry>();
    for(Changes change : preprocess(inRequest, inRequest.getParams(), errors))
      for(AbstractEntry entry : change.entries(errors))
      {
        for(Map.Entry<String, String> keyValue : change.m_values.entrySet())
        {
          if(!entry.canEdit(keyValue.getKey(), inRequest.getUser()))
          {
            String error = "not allowed to edit " + keyValue.getKey() + " in "
              + change.m_type + " " + entry.getID();
            Log.warning(error);
            errors.add("gui.alert(" + Encodings.toJSString(error) + ");");
            continue;
          }

          String rest = entry.set(keyValue.getKey(), keyValue.getValue());
          if(rest != null)
          {
            Log.warning("Could not fully parse " + keyValue.getKey()
                        + " value for " + change.m_type + " " + change.m_id
                        + ": '" + rest + "'");
            errors.add("edit.unparsed("
                       + Encodings.toJSString(change.m_fullType) + ", "
                       + Encodings.toJSString(change.m_id) + ", "
                       + Encodings.toJSString(keyValue.getKey()) + ", "
                       + Encodings.toJSString(rest) + ");");
          }
          else
            entries.add(entry);
        }
      }

    List<String> saved = new ArrayList<String>();

    // do we really have something to do?
    String path = "";
    if(entries.size() <= 0)
      errors.add("gui.alert('No values to save');");
    else
    {
      // update all entries and mark them as saved
      for(AbstractEntry entry : entries)
      {
        if(entry.save())
          saved.add(Encodings.escapeJS(entry.getType().toString()) + " "
                    + Encodings.escapeJS(entry.getName()));
        else
          errors.add("Coult not store " + entry.getType() + " '"
                     + entry.getName() + "'");
      }

      if(entries.size() == 1)
        path = Encodings.toJSString(entries.iterator().next().getPath());
    }

    return
      (errors.isEmpty() ? "" : "gui.alert('Parse error for values');")
      + (saved.isEmpty() ? ""
         : "gui.info('The following entries were updated:<p>"
         + Strings.BR_JOINER.join(saved) + "'); "
         + "util.link(null" + (path.isEmpty() ? "" : ", "  + path) + ");")
      + Strings.NEWLINE_JOINER.join(errors);
  }

  //........................................................................
  //------------------------------ preprocess ------------------------------

  /**
   * Preprocess the request by collecting all changes that need to be made.
   *
   * @param       inRequest the original request for the page
   * @param       inParams  the params for the request
   * @param       ioErrors  the errors encountered
   *
   * @return      a collection of all changes requested
   *
   */
  private Collection<Changes> preprocess
    (@Nonnull DMARequest inRequest,
     @Nonnull Multimap<String, String> inParams,
     @Nonnull List<String> ioErrors)
  {
    Map<String, Changes> changes = new HashMap<String, Changes>();
    for(Map.Entry<String, String> param : inParams.entries())
    {
      String []parts = param.getKey().split("::");

      // not a real key value pair
      if(parts.length != 3)
        continue;

      String fullType = parts[0];
      String typeName = Strings.getPattern(fullType, "/([^/]+)$");
      AbstractType<? extends AbstractEntry> type = null;
      if(typeName != null)
        type = AbstractType.get(typeName);
      else
        type = AbstractType.get(fullType);

      String id = parts[1];
      String key = parts[2];

      if(type == null)
      {
        String error = "invalid type '" + fullType + "' ignored";
        Log.warning(error);
        ioErrors.add("gui.alert(" + Encodings.toJSString(error) + ");");
        continue;
      }

      Changes change = changes.get(id + ":" + fullType);
      if(change == null)
      {
        change = new Changes(id, type, fullType,
                             getData(inRequest, fullType, m_data),
                             inRequest.getUser());
        changes.put(id + ":" + fullType, change);
      }

      change.set(key, param.getValue());
    }

    return changes.values();
  }

  //........................................................................

  //........................................................................

  //------------------------------------------------- other member functions
  //........................................................................

  //------------------------------------------------------------------- test

    /** The test. */
  public static class Test extends net.ixitxachitls.util.test.TestCase
  {
    /** Setup before tests. */
    @org.junit.Before
    public void setUp()
    {
      net.ixitxachitls.util.configuration
        .Config.set("web.data.datastore", false);
      net.ixitxachitls.util.configuration
        .Config.set("web.data.datafiles", false);
    }

    /** Cleanup after tests. */
    @org.junit.After
    public void tearDown()
    {
      net.ixitxachitls.dma.data.DMADataFactory.clearBase();
    }

    //----- save -----------------------------------------------------------

    /** The save Test. */
    @org.junit.Test
    public void save()
    {
      DMAData data = net.ixitxachitls.dma.data.DMADataFactory.getBaseData();
      net.ixitxachitls.dma.entries.BaseEntry entry =
        new net.ixitxachitls.dma.entries.BaseEntry("test", data);

      SaveActionServlet servlet = new SaveActionServlet();
      DMARequest request = EasyMock.createMock(DMARequest.class);
      HttpServletResponse response =
        EasyMock.createMock(HttpServletResponse.class);
      BaseCharacter user = EasyMock.createMock(BaseCharacter.class);
      com.google.common.collect.Multimap<String, String> params =
        com.google.common.collect.ImmutableMultimap.of
        ("/base entry::test::name", "guru");

      EasyMock.expect(user.getName()).andStubReturn("user");
      EasyMock.expect(request.getUser()).andStubReturn(user);
      EasyMock.expect(request.getParams()).andStubReturn(params);
      EasyMock.expect(user.hasAccess(BaseCharacter.Group.ADMIN))
        .andStubReturn(true);
      EasyMock.expect(data.getEntry
                      ("test", net.ixitxachitls.dma.entries.BaseEntry.TYPE))
        .andStubReturn(entry);
      EasyMock.expect(data.update(entry)).andReturn(true);

      EasyMock.replay(data, request, response, user);

      assertEquals("name", "test", entry.getName());

      assertEquals("result",
                   "gui.info('The following entries were updated:"
                   + "<p>base entry guru'); util.link(null, '/entry/guru');",
                   servlet.doAction(request, response));

      assertEquals("name", "guru", entry.getName());

      EasyMock.verify(data, request, response, user);
    }

    //......................................................................
    //----- no access ------------------------------------------------------

    /** The save Test. */
    @org.junit.Test
    public void noAccess()
    {
      DMAData data = net.ixitxachitls.dma.data.DMADataFactory.getBaseData();
      net.ixitxachitls.dma.entries.BaseEntry entry =
        new net.ixitxachitls.dma.entries.BaseEntry("test", data);

      SaveActionServlet servlet = new SaveActionServlet();
      DMARequest request = EasyMock.createMock(DMARequest.class);
      HttpServletResponse response =
        EasyMock.createMock(HttpServletResponse.class);
      BaseCharacter user = EasyMock.createMock(BaseCharacter.class);
      com.google.common.collect.Multimap<String, String> params =
        com.google.common.collect.ImmutableMultimap.of
        ("/base entry::test::name", "guru");

      EasyMock.expect(request.getUser()).andStubReturn(user);
      EasyMock.expect(request.getParams()).andStubReturn(params);
      EasyMock.expect(user.hasAccess(BaseCharacter.Group.ADMIN))
        .andStubReturn(false);
      EasyMock.expect(data.getEntry
                      ("test", net.ixitxachitls.dma.entries.BaseEntry.TYPE))
        .andStubReturn(entry);

      EasyMock.replay(data, request, response, user);

      assertEquals("name", "test", entry.getName());

      assertEquals("result",
                   "gui.alert('Parse error for values');"
                   + "gui.alert('not allowed to edit name in base entry "
                   + "test');\n"
                   + "gui.alert('No values to save');",
                   servlet.doAction(request, response));

      assertEquals("name", "test", entry.getName());

      m_logger.addExpected("WARNING: not allowed to edit name in base entry "
                           + "test");
      EasyMock.verify(data, request, response, user);
    }

    //......................................................................
    //----- invalid id -----------------------------------------------------

    /** The save Test. */
    @org.junit.Test
    public void invalidID()
    {
      DMAData data = net.ixitxachitls.dma.data.DMADataFactory.getBaseData();
      net.ixitxachitls.dma.entries.BaseEntry entry =
        new net.ixitxachitls.dma.entries.BaseEntry("test", data);

      SaveActionServlet servlet = new SaveActionServlet();
      DMARequest request = EasyMock.createMock(DMARequest.class);
      HttpServletResponse response =
        EasyMock.createMock(HttpServletResponse.class);
      BaseCharacter user = EasyMock.createMock(BaseCharacter.class);
      com.google.common.collect.Multimap<String, String> params =
        com.google.common.collect.ImmutableMultimap.of
        ("/base entry::guru::name", "guru");

      EasyMock.expect(request.getUser()).andStubReturn(user);
      EasyMock.expect(request.getParams()).andStubReturn(params);
      EasyMock.expect(user.hasAccess(BaseCharacter.Group.ADMIN))
        .andStubReturn(false);
      EasyMock.expect(data.getEntry
                      ("guru", net.ixitxachitls.dma.entries.BaseEntry.TYPE))
        .andStubReturn(null);

      EasyMock.replay(data, request, response, user);

      assertEquals("name", "test", entry.getName());

      assertEquals("result",
                   "gui.alert('Parse error for values');"
                   + "gui.alert('could not find base entry guru for saving');\n"
                   + "gui.alert('No values to save');",
                   servlet.doAction(request, response));

      assertEquals("name", "test", entry.getName());

      m_logger.addExpected("WARNING: could not find base entry guru for "
                           + "saving");
      EasyMock.verify(data, request, response, user);
    }

    //......................................................................
    //----- invalid type ---------------------------------------------------

    /** The save Test. */
    @org.junit.Test
    public void invalidType()
    {
      DMAData data = net.ixitxachitls.dma.data.DMADataFactory.getBaseData();
      net.ixitxachitls.dma.entries.BaseEntry entry =
        new net.ixitxachitls.dma.entries.BaseEntry("test", data);

      SaveActionServlet servlet = new SaveActionServlet();
      DMARequest request = EasyMock.createMock(DMARequest.class);
      HttpServletResponse response =
        EasyMock.createMock(HttpServletResponse.class);
      BaseCharacter user = EasyMock.createMock(BaseCharacter.class);
      com.google.common.collect.Multimap<String, String> params =
        com.google.common.collect.ImmutableMultimap.of
        ("guru::test::name", "guru");

      EasyMock.expect(request.getUser()).andStubReturn(user);
      EasyMock.expect(request.getParams()).andStubReturn(params);
      EasyMock.expect(user.hasAccess(BaseCharacter.Group.ADMIN))
        .andStubReturn(false);

      EasyMock.replay(data, request, response, user);

      assertEquals("name", "test", entry.getName());

      assertEquals("result",
                   "gui.alert('Parse error for values');"
                   + "gui.alert('invalid type \\'guru\\' ignored');\n"
                   + "gui.alert('No values to save');",
                   servlet.doAction(request, response));

      assertEquals("name", "test", entry.getName());

      m_logger.addExpected("WARNING: invalid type 'guru' ignored");
      EasyMock.verify(data, request, response, user);
    }

    //......................................................................
    //----- invalid key ----------------------------------------------------

    /** The save Test. */
    @org.junit.Test
    public void invalidKey()
    {
      DMAData data = net.ixitxachitls.dma.data.DMADataFactory.getBaseData();
      net.ixitxachitls.dma.entries.BaseEntry entry =
        new net.ixitxachitls.dma.entries.BaseEntry("test", data);

      SaveActionServlet servlet = new SaveActionServlet();
      DMARequest request = EasyMock.createMock(DMARequest.class);
      HttpServletResponse response =
        EasyMock.createMock(HttpServletResponse.class);
      BaseCharacter user = EasyMock.createMock(BaseCharacter.class);
      com.google.common.collect.Multimap<String, String> params =
        com.google.common.collect.ImmutableMultimap.of
        ("/base entry::test::guru", "guru",
         "/base entry::test::description", "\"test\"");

      EasyMock.expect(request.getUser()).andStubReturn(user);
      EasyMock.expect(request.getParams()).andStubReturn(params);
      EasyMock.expect(user.hasAccess(BaseCharacter.Group.ADMIN))
        .andStubReturn(true);
      EasyMock.expect(data.getEntry
                      ("test", net.ixitxachitls.dma.entries.BaseEntry.TYPE))
        .andStubReturn(entry);
      EasyMock.expect(data.update(entry)).andReturn(true);

      EasyMock.replay(data, request, response, user);

      assertEquals("name", "test", entry.getName());

      assertEquals("result",
                   "gui.alert('Parse error for values');"
                   + "gui.info('The following entries were updated:"
                   + "<p>base entry test'); "
                   + "util.link(null, '/entry/test');"
                   + "edit.unparsed('/base entry', 'test', 'guru', 'guru');",
                   servlet.doAction(request, response));

      assertEquals("name", "test", entry.getName());

      m_logger.addExpected("WARNING: Could not fully parse guru value for "
                           + "base entry test: 'guru'");
      EasyMock.verify(data, request, response, user);
    }

    //......................................................................
    //----- collet changes -------------------------------------------------

    /** The changes Test. */
    @org.junit.Test
    public void collectChanges()
    {
      DMAData data = net.ixitxachitls.dma.data.DMADataFactory.getBaseData();

      SaveActionServlet servlet = new SaveActionServlet();
      DMARequest request = EasyMock.createMock(DMARequest.class);
      BaseCharacter user = EasyMock.createMock(BaseCharacter.class);

      EasyMock.expect(request.getUser()).andStubReturn(user);
      EasyMock.expect(user.getProductData()).andStubReturn(null);

      EasyMock.replay(data, request, user);

      Multimap<String, String> params =
        com.google.common.collect.ImmutableSetMultimap.<String, String>builder()
        .put("/base entry::id::name", "guru")
        .put("/base entry::id::description", "\"text\"")
        .put("/base entry::id2::name", "guru2")
        .put("/base entry::id2::file", "file")
        .put("/base entry::id2::worlds", "wolrd")
        .put("something/base entry::my-id::name", "name guru")
        .put("/user/me/base entry::id::key", "value")
        .put("/user/me/base entry::id::file", "file")
        .put("/user/me/base entry::id::key2", "value2")
        .put("/base entry::*/Person::key", "value")
        .put("/base entry::*/Something::key", "value")
        .build();
      List<String> errors = new ArrayList<String>();
      Collection<Changes> changes = servlet.preprocess(request, params, errors);

      java.util.Iterator<Changes> i = changes.iterator();
      checkChanges(i.next(), "my-id", "name guru", null, "something/base entry",
                   data, user, "name", "name guru");
      checkChanges(i.next(), "id", "id", "file", "/user/me/base entry", null,
                   user, "key", "value", "key2", "value2");
      checkChanges(i.next(), "id2", "guru2", "file", "/base entry", data, user,
                   "name", "guru2", "worlds", "wolrd");
      checkChanges(i.next(), "id", "guru", null, "/base entry", data, user,
                   "name", "guru", "description", "\"text\"");
      checkChanges(i.next(), "*/Something", "*/Something", null, "/base entry",
                   data, user, "key", "value");
      checkChanges(i.next(), "*/Person", "*/Person", null, "/base entry",
                   data, user, "key", "value");
      assertFalse(i.hasNext());

      EasyMock.verify(data, request, user);
    }

    /** Check assertions for changes.
     *
     * @param inChanges   the changes to check
     * @param inID        the expected id of the entry changed
     * @param inName      the expected name of the entry changed
     * @param inFile      the expected file for the entry changed
     * @param inFullType  the expected full type for the entry changed
     * @param inData      the expected data object for the entry changed
     * @param inOwner     the expected owner for the entry changed
     * @param inKeyValues the expected key value pairs for the entry changed
     *
     */
    private void checkChanges(Changes inChanges, String inID, String inName,
                              String inFile, String inFullType, DMAData inData,
                              AbstractEntry inOwner, String ... inKeyValues)
    {
      assertEquals("id", inID, inChanges.m_id);
      assertEquals("name", inName, inChanges.m_name);
      assertEquals("file", inFile, inChanges.m_file);
      assertEquals("full type", inFullType, inChanges.m_fullType);
      assertEquals("data", inData, inChanges.m_data);
      assertEquals("owner", inOwner, inChanges.m_owner);

      assertEquals("number of values", inChanges.m_values.keySet().size(),
                   inKeyValues.length / 2);
      for(int i = 0; i < inKeyValues.length; i += 2)
        assertEquals(inKeyValues[i], inKeyValues[i + 1],
                     inChanges.m_values.get(inKeyValues[i]));
    }

    //......................................................................
  }

  //........................................................................
}
