package com.example.trendy_chat.repository;

import com.example.trendy_chat.entity.GroupChat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupChatRepository extends JpaRepository<GroupChat,String> {
}
