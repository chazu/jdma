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


package net.ixitxachitls.dma.entries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;

import net.ixitxachitls.dma.data.DMADataFactory;
import net.ixitxachitls.dma.proto.Entries.QualityProto;
import net.ixitxachitls.dma.values.ExpressionValue;
import net.ixitxachitls.dma.values.Speed;
import net.ixitxachitls.dma.values.Values;
import net.ixitxachitls.dma.values.enums.MovementMode;

/**
 * A monster specific quality.
 *
 * @file   Quality.java
 * @author balsiger@ixitxachitls.net (Peter Balsiger)
 */
public class Quality extends NestedEntry
{
  /** Create a default quality. */
  public Quality()
  {
  }

  /** The parameters defined for this quality to parameterize the base value. */
  private final Map<String, String> m_parameters = new HashMap<>();

  /** The base quality, if found. */
  private Optional<Optional<BaseQuality>> m_base = Optional.absent();

  /**
   * Get the base quality, if it can be found.
   *
   * @return the base quality, if found
   */
  public Optional<BaseQuality> getBase()
  {
    if(!m_base.isPresent())
    {
      if(m_name.isPresent())
        m_base = Optional.of(DMADataFactory.get().<BaseQuality>getEntry
            (new EntryKey(m_name.get(), BaseQuality.TYPE)));
      else
        return Optional.absent();
    }

    return m_base.get();
  }

  /**
   * Get the parameters for the quality.
   *
   * @return a map of key to value parameters
   */
  public Map<String, String> getParameters()
  {
    return Collections.unmodifiableMap(m_parameters);
  }

  /**
   * Get the speed modification for the given movement mode.
   *
   * @param inMode the movement mode for which to get the speed modification
   * @return the speed modification, if any
   */
  public Optional<Speed> getSpeed(MovementMode inMode)
  {
    if(!getBase().isPresent())
      return Optional.absent();

    Optional<ExpressionValue<Speed>> speed = getBase().get().getSpeed();
    if(!speed.isPresent())
      return Optional.absent();

    Optional<Speed> value = Optional.absent();
    if(speed.get().hasValue())
      value = speed.get().getValue();
    else
      value = speed.get().getValue(m_parameters, Speed.PARSER);

    if(value.isPresent() && value.get().getMode() == inMode)
      return value;

    return Optional.absent();
  }

  @Override
  public void set(Values inValues)
  {
    m_name = inValues.use("name", m_name);

    List<String> names = new ArrayList<>();
    List<String> values = new ArrayList<>();
    for(Map.Entry<String, String> entry : m_parameters.entrySet())
    {
      names.add(entry.getKey());
      values.add(entry.getValue());
    }

    names = inValues.use("parameter.name", names);
    values = inValues.use("parameter.value", values);

    m_parameters.clear();
    for(int i = 0; i < names.size() && i < values.size(); i++)
      m_parameters.put(names.get(i), values.get(i));
  }

  /**
   * Convert the quality to a proto.
   *
   * @return the qualities value in a proto
   */
  public QualityProto toProto()
  {
    QualityProto.Builder builder = QualityProto.newBuilder();

    if(m_name.isPresent())
      builder.setName(m_name.get());
    else
      builder.setName("unknown");

    for(Map.Entry<String, String> parameter : m_parameters.entrySet())
      builder.addParameter(QualityProto.Parameter.newBuilder()
                             .setName(parameter.getKey())
                             .setValue(parameter.getValue())
                             .build());

    QualityProto proto = builder.build();
    return proto;
  }

  /**
   * Create a quality from the given proto.
   *
   * @param inProto the proto values
   * @return the newly created quality
   */
  public static Quality fromProto(QualityProto inProto)
  {
    Quality quality = new Quality();
    quality.m_name = Optional.of(inProto.getName());
    for(QualityProto.Parameter parameter : inProto.getParameterList())
      quality.m_parameters.put(parameter.getName(), parameter.getValue());

    return quality;
  }
}
