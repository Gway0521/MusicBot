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

/**
 * Simple Ping command
 */
public class PingCmd extends Command
{
    public PingCmd()
    {
        this.name = "ping";
        this.help = "checks the bot's latency";
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event)
    {
        event.reply("Ping: ...", m -> {
            long ping = m.getTimeCreated().toInstant().toEpochMilli() - event.getMessage().getTimeCreated().toInstant().toEpochMilli();
            m.editMessage("Ping: " + ping + "ms | Websocket: " + event.getJDA().getGatewayPing() + "ms").queue();
        });
    }
}
