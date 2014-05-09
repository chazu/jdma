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


package net.ixitxachitls.dma.values;

import com.google.common.base.Optional;

import net.ixitxachitls.dma.proto.Entries.BaseMagicProto;
import net.ixitxachitls.dma.proto.Entries.BaseMagicProto.Modifier;

public class NamedModifier extends NewValue.Addable<BaseMagicProto.Modifier>
{
  public NamedModifier(ModifierType inType, NewModifier inModifier)
  {
    m_type = inType;
    m_modifier = inModifier;
  }

  private final ModifierType m_type;
  private final NewModifier m_modifier;

  public final static NewValue.Parser<NamedModifier> PARSER =
    new NewValue.Parser<NamedModifier>(2)
    {
      @Override
      protected Optional<NamedModifier> doParse(String inType,
                                               String inModifier)
      {
        Optional<ModifierType> type = ModifierType.PARSER.parse(inType);
        Optional<NewModifier> modifier = NewModifier.PARSER.parse(inModifier);

        if(type.isPresent() && modifier.isPresent())
          return Optional.of(new NamedModifier(type.get(),
                                               modifier.get()));

        return Optional.absent();
      }
    };

  @Override
  public String toString()
  {
    return m_type + " " + m_modifier;
  }

  public ModifierType getType()
  {
    return m_type;
  }

  public NewModifier getModifier()
  {
    return m_modifier;
  }

  /**
   * Create a named modifier from the given proto.
   *
   * @param inProto the proto to generate from
   * @return the newly created named modifier
   */
  public static NamedModifier fromProto(Modifier inProto)
  {
    return new NamedModifier(ModifierType.fromProto(inProto.getType()),
                             NewModifier.fromProto(inProto.getModifier()));
  }

  @Override
  public Addable<BaseMagicProto.Modifier> add(Addable inValue)
  {
    if(!canAdd(inValue))
      return this;

    NamedModifier value = (NamedModifier)inValue;
    return new NamedModifier(m_type,
                               (NewModifier)
                               m_modifier.add(value.getModifier()));
  }

  @Override
  public boolean canAdd(Addable inValue)
  {
    if(!(inValue instanceof NamedModifier))
      return false;

    NamedModifier value = (NamedModifier)inValue;
    return m_type == value.m_type;
  }

  @Override
  public BaseMagicProto.Modifier toProto()
  {
    return BaseMagicProto.Modifier.newBuilder()
      .setType(m_type.toProto())
      .setModifier(m_modifier.toProto())
      .build();
  }
}