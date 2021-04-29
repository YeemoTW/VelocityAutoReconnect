package de.flori4nk.velocityautoreconnect.listeners;

/*
MIT License

VelocityAutoReconnect
Copyright (c) 2021 Flori4nK <contact@flori4nk.de>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

import java.util.regex.Pattern;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.player.KickedFromServerEvent.RedirectPlayer;
import com.velocitypowered.api.proxy.Player;
import de.flori4nk.velocityautoreconnect.VelocityAutoReconnect;
import de.flori4nk.velocityautoreconnect.misc.Utility;
import net.kyori.adventure.text.Component;

public class KickListener {

	private Pattern kickFilterWhitelist;
	private Pattern kickFilterBlacklist;
	
	public KickListener() {
		// Compile whitelist / blacklist Patterns from configured expressions
		this.kickFilterBlacklist = Pattern.compile(VelocityAutoReconnect.getConfigurationManager().getProperty("kick-filter.blacklist"), Pattern.DOTALL);
		this.kickFilterWhitelist = Pattern.compile(VelocityAutoReconnect.getConfigurationManager().getProperty("kick-filter.whitelist"), Pattern.DOTALL);
	}
	
	@Subscribe(order = PostOrder.NORMAL)
	public void onPlayerKick(KickedFromServerEvent event) {
		// Check whether the result of the kick actually was a redirection.
		if(event.getResult() instanceof KickedFromServerEvent.RedirectPlayer) {
			KickedFromServerEvent.RedirectPlayer playerRedirection = (RedirectPlayer) event.getResult();
			Player player = event.getPlayer();
			// Get the kick reason, when possible. Use an empty Component if the kick reason isn't present.
			Component kickReason = event.getServerKickReason().isPresent() ? event.getServerKickReason().get() : Component.empty();
			String kickReasonText = kickReason.toString();
			
			if(!Utility.doServerNamesMatch(playerRedirection.getServer(), VelocityAutoReconnect.getLimboServer())) {
				return;
			}
			
			if(VelocityAutoReconnect.getConfigurationManager().getBooleanProperty("kick-filter.whitelist.enabled") 
					&& !this.kickFilterWhitelist.matcher(kickReasonText).matches()) {
				player.disconnect(kickReason);
				return;
			}
			
			if(VelocityAutoReconnect.getConfigurationManager().getBooleanProperty("kick-filter.blacklist.enabled")
					&& this.kickFilterBlacklist.matcher(kickReasonText).matches()) {
				player.disconnect(kickReason);
				return;
			}
			// Add player and previous server to the Map.
			VelocityAutoReconnect.getPlayerManager().addPlayer(event.getPlayer(), event.getServer());
			Utility.sendWelcomeMessage(player);
		}
	}
}