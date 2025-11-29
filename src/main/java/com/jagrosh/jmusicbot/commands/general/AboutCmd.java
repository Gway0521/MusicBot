/*
 * Copyright 2016-2018 John Grosh (jagrosh) and Kaidan Gustave (TheMonitorLizard)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.jmusicbot.commands.general;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import java.awt.Color;

/**
 * Simple About command
 */
public class AboutCmd extends Command
{
    private final Color color;
    private final String description;
    private final String[] features;
    private final Permission[] perms;
    private boolean isAuthor = true;
    private String replacementCharacter = "\uD83D\uDCBB";

    public AboutCmd(Color color, String description, String[] features, Permission[] perms)
    {
        this.color = color;
        this.description = description;
        this.features = features;
        this.name = "about";
        this.help = "shows info about the bot";
        this.guildOnly = false;
        this.perms = perms;
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
    }

    public void setIsAuthor(boolean value)
    {
        this.isAuthor = value;
    }

    public void setReplacementCharacter(String value)
    {
        this.replacementCharacter = value;
    }

    @Override
    protected void execute(CommandEvent event)
    {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(color);
        builder.setDescription(description);

        StringBuilder permsStr = new StringBuilder();
        for(Permission p: perms)
            permsStr.append("`").append(p.getName()).append("` ");

        builder.addField("Features", String.join("\n", features), true);
        builder.addField("Recommended Permissions", permsStr.toString(), true);

        builder.setFooter("JMusicBot", null);

        event.reply(builder.build());
    }
}
