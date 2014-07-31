package org.beShare.event;

import org.beShare.data.ChatMessage;
import org.beShare.gui.ChatPoster;
/**
	ChatMessageListener - Defines the interface for classes wishing to listen
	for new ChatMessage events.
	
	Last Update: 2-28-2002
	
	@author Bryan Varner
	@version 1.0
*/
public interface ChatMessageListener{
	public void chatMessage(ChatMessage cm);
	public void addChatPoster(ChatPoster p);
	public void removeChatPoster(ChatPoster p);
}
