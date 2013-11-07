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

package net.ixitxachitls.dma.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.annotation.ParametersAreNonnullByDefault;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.repackaged.com.google.common.base.Charsets;
import com.google.appengine.tools.remoteapi.RemoteApiInstaller;
import com.google.appengine.tools.remoteapi.RemoteApiOptions;
import com.google.common.io.ByteSink;
import com.google.common.io.CharSink;

import net.ixitxachitls.dma.data.DMADatastore;
import net.ixitxachitls.dma.entries.AbstractEntry;
import net.ixitxachitls.dma.proto.Entries.BaseCampaignProto;
import net.ixitxachitls.dma.proto.Entries.BaseCharacterProto;
import net.ixitxachitls.dma.proto.Entries.BaseEncounterProto;
import net.ixitxachitls.dma.proto.Entries.BaseFeatProto;
import net.ixitxachitls.dma.proto.Entries.BaseItemProto;
import net.ixitxachitls.dma.proto.Entries.BaseLevelProto;
import net.ixitxachitls.dma.proto.Entries.BaseMonsterProto;
import net.ixitxachitls.dma.proto.Entries.BaseProductProto;
import net.ixitxachitls.dma.proto.Entries.BaseQualityProto;
import net.ixitxachitls.dma.proto.Entries.BaseSkillProto;
import net.ixitxachitls.dma.proto.Entries.BaseSpellProto;
import net.ixitxachitls.dma.proto.Entries.CharacterProto;
import net.ixitxachitls.dma.proto.Entries.EncounterProto;
import net.ixitxachitls.dma.proto.Entries.EntriesProto;
import net.ixitxachitls.dma.proto.Entries.ItemProto;
import net.ixitxachitls.dma.proto.Entries.LevelProto;
import net.ixitxachitls.dma.proto.Entries.MonsterProto;
import net.ixitxachitls.dma.proto.Entries.NPCProto;
import net.ixitxachitls.dma.proto.Entries.ProductProto;
import net.ixitxachitls.util.CommandLineParser;
import net.ixitxachitls.util.Files;
import net.ixitxachitls.util.logging.ANSILogger;
import net.ixitxachitls.util.logging.Log;

//..........................................................................

//------------------------------------------------------------------- header

/**
 * A utility to export dma entries into the app engine data store.
 *
 * Useage:
 *
 * java net.ixitxachitls.dma.server.Exporter
 * -h jdmaixit.appspot.com -p 443 -u balsiger@ixitxachitls.net <dir>
 *
 * Exports base  characters from the datastore to file file.dma
 * (leave out host and port for local storage).
 *
 * @file          Exporter.java
 *
 * @author        balsiger@ixitxachitls.net (Peter Balsiger)
 *
 */

//..........................................................................

//__________________________________________________________________________

@ParametersAreNonnullByDefault
public final class Exporter
{
  //--------------------------------------------------------- constructor(s)

  //------------------------------- Exporter -------------------------------

  /**
   * Prevent instantiation.
   *
   */
  private Exporter()
  {
    // nothing to do
  }

  //........................................................................

  //........................................................................

  //-------------------------------------------------------------- variables

  static
  {
    net.ixitxachitls.dma.server.servlets.DMARequest.ensureTypes();
  }

  //........................................................................

  //-------------------------------------------------------------- accessors
  //........................................................................

  //----------------------------------------------------------- manipulators
  //........................................................................

  //------------------------------------------------- other member functions
  //........................................................................

  //--------------------------------------------------------- main/debugging

  //--------------------------------- main ---------------------------------

  /**
   * Main routine for the exporter utility.
   *
   * @param    inArguments the command line arguments
   *
   * @throws   Exception too lazy to handle
   *
   */
  public static void main(String []inArguments) throws Exception
  {
    Log.setLevel(Log.Type.INFO);
    Log.add("import", new ANSILogger());

    CommandLineParser clp =
      new CommandLineParser
      (new CommandLineParser.StringOption
       ("h", "host", "The host to connect to.", "localhost"),
       new CommandLineParser.StringOption
       ("t", "type", "The type of entries to export (or file for blobs)",
        ""),
       new CommandLineParser.IntegerOption
       ("p", "port", "The port to connect to.", 8888),
       new CommandLineParser.StringOption
       ("u", "username", "The username to connect with.",
        "balsiger@ixitxachitls.net"));

    String dir = clp.parse(inArguments);

    if(dir == null || dir.isEmpty())
    {
      System.err.println("Must have an output directory");
      return;
    }

    String password =
      new String(System.console().readPassword("password for "
                                               + clp.getString("username")
                                               + ": "));

    RemoteApiOptions options = new RemoteApiOptions()
      .server(clp.getString("host"), clp.getInteger("port"))
      .credentials(clp.getString("username"), password);

    RemoteApiInstaller installer = new RemoteApiInstaller();
    installer.install(options);

    // init the dma files
    EntriesProto.Builder entries = EntriesProto.newBuilder();

    try
    {
      DatastoreService store = DatastoreServiceFactory.getDatastoreService();
      ImagesService image = ImagesServiceFactory.getImagesService();
      DMADatastore dmaStore = new DMADatastore();
      Log.important("reading entities from datastore");

      Query query;
      if(clp.getString("type").isEmpty())
        query = new Query();
      else
        query = new Query(clp.getString("type"));

      for(Entity entity : store.prepare(query).asIterable
            (FetchOptions.Builder.withChunkSize(1000)))
      {
        // ignore internal entities
        if(entity.getKind().startsWith("__"))
          continue;

        // write out blobs specially
        if("file".equals(entity.getKind()))
        {
          String filePath = extractFilePath(entity.getParent());
          String name = (String)entity.getProperty("name");
          String path = (String)entity.getProperty("path");
          String extension =
            Files.mimeExtension((String)entity.getProperty("type"));
          File blobDir = new File(Files.concatenate(dir, "blobs", filePath));
          if(!blobDir.exists())
            if(!blobDir.mkdirs())
              Log.warning("could not create directory " + blobDir);

          String file =
            Files.concatenate(dir, "blobs", filePath, name + "." + extension);

          for(int i = 1; i <= 5; i++)
          {
            FileOutputStream output = null;
            InputStream input = null;

            try
            {
              try
              {
                String url = image.getServingUrl
                  (ServingUrlOptions.Builder.withBlobKey(new BlobKey(path)));

                URLConnection connection = new URL(url).openConnection();

                byte[] buffer = new byte[100 * 1024];

                output = new FileOutputStream(file);
                input = connection.getInputStream();

                for(int read = input.read(buffer); read > 0;
                    read = input.read(buffer))
                  output.write(buffer, 0, read);

                break;
              }
              catch(java.io.IOException e)
              {
                Log.error("Deadline exceeded when trying to download file "
                          + file + " (retrying " + i + "): " + e);
              }
              finally
              {
                if(input != null)
                  input.close();
              }
            }
            finally
            {
              if(output != null)
                output.close();
            }
          }

          Log.important("Wrote blob " + file);

          continue;
        }

        Log.important("converting entity " + entity.getKind() + ": "
                      + entity.getKey());
        AbstractEntry entry = dmaStore.convert(entity);

        if(entry == null)
        {
          Log.warning("could not convert " + entity);
          continue;
        }

        switch(entry.getType().getName())
        {
          case "base character":
            entries.addBaseCharacter((BaseCharacterProto)entry.toProto());
            break;

          case "base product":
            entries.addBaseProduct((BaseProductProto)entry.toProto());
            break;

          case "base spell":
            entries.addBaseSpell((BaseSpellProto)entry.toProto());
            break;

          case "base quality":
            entries.addBaseQuality((BaseQualityProto)entry.toProto());
            break;

          case "base feat":
            entries.addBaseFeat((BaseFeatProto)entry.toProto());
            break;

          case "base skill":
            entries.addBaseSkill((BaseSkillProto)entry.toProto());
            break;

          case "base item":
            entries.addBaseItem((BaseItemProto)entry.toProto());
            break;

          case "base campaign":
            entries.addBaseCampaign((BaseCampaignProto)entry.toProto());
            break;

          case "base monster":
            entries.addBaseMonster((BaseMonsterProto)entry.toProto());
            break;

          case "base encounter":
            entries.addBaseEncounter((BaseEncounterProto)entry.toProto());
            break;

          case "base level":
            entries.addBaseLevel((BaseLevelProto)entry.toProto());
            break;

          case "product":
            entries.addProduct((ProductProto)entry.toProto());
            break;

          case "character":
            entries.addCharacter((CharacterProto)entry.toProto());
            break;

          case "item":
            entries.addItem((ItemProto)entry.toProto());
            break;

          case "encounter":
            entries.addEncounter((EncounterProto)entry.toProto());
            break;

          case "level":
            entries.addLevel((LevelProto)entry.toProto());
            break;

          case "monster":
            entries.addMonster((MonsterProto)entry.toProto());
            break;

          case "npc":
            entries.addNpc((NPCProto)entry.toProto());
            break;

          default:
            System.err.println("could not convert entry type "
                               + entry.getType().getName());
        }
      }

      ByteSink bytes =
        com.google.common.io.Files.asByteSink(new File(dir + "/export.pb"));
      EntriesProto data = entries.build();
      bytes.write(data.toByteArray());

      CharSink chars =
        com.google.common.io.Files.asCharSink(new File(dir + "/export.ascii"),
                                              Charsets.UTF_8);
      chars.write(data.toString());
    }
    finally
    {
      installer.uninstall();
    }
  }

  //........................................................................
  //--------------------------- extractFilePath ----------------------------

  /**
   * Extract the path for the file from the given file entity, without the file
   * name.
   *
   * @param    inKey the key of the file's parent
   *
   * @return   the full path for the file, withouth filename
   */
  private static String extractFilePath(Key inKey)
  {
    String id = inKey.getName();
    String type = inKey.getKind();

    Key parent = inKey.getParent();
    if(parent != null)
      return Files.concatenate(extractFilePath(parent), type, id);

    return Files.concatenate(type, id);
  }

  //........................................................................


  //........................................................................
}
