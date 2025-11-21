package com.example.trendy_chat.controller;

import com.example.trendy_chat.entity.BlockList;
import com.example.trendy_chat.entity.User;
import com.example.trendy_chat.repository.BlockListRepository;
import com.example.trendy_chat.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/trendy/block")
public class BlockController {
    private final BlockListRepository blockRepo;
    private final UserRepository userRepo;

    public BlockController(BlockListRepository blockRepo, UserRepository userRepo) {
        this.blockRepo = blockRepo;
        this.userRepo = userRepo;
    }

    /**
     * Block a user
     * POST /trendy/block
     */
    @PostMapping
    public ResponseEntity<?> blockUser(@RequestBody Map<String, String> body) {
        String blockerId = body.get("blockerId");
        String blockedId = body.get("blockedId");

        if (blockerId == null || blockedId == null) {
            return ResponseEntity.badRequest().body("blockerId và blockedId không được để trống");
        }

        if (blockerId.equals(blockedId)) {
            return ResponseEntity.badRequest().body("Không thể chặn chính mình");
        }

        // Check if already blocked
        if (blockRepo.existsByMaNguoiChanAndMaNguoiBiChan(blockerId, blockedId)) {
            return ResponseEntity.badRequest().body("Đã chặn người dùng này");
        }

        BlockList block = new BlockList();
        block.setMaChan(UUID.randomUUID().toString());
        block.setMaNguoiChan(blockerId);
        block.setMaNguoiBiChan(blockedId);
        block.setNgayChan(LocalDateTime.now());

        blockRepo.save(block);

        return ResponseEntity.ok(Map.of(
            "message", "Đã chặn người dùng",
            "blockedId", blockedId
        ));
    }
    @DeleteMapping("/{blockedId}")
    public ResponseEntity<?> unblockUser(
            @PathVariable String blockedId,
            @RequestParam String blockerId
    ) {
        blockRepo.deleteByMaNguoiChanAndMaNguoiBiChan(blockerId, blockedId);
        return ResponseEntity.ok(Map.of("message", "Đã bỏ chặn người dùng"));
    }
    @GetMapping("/list")
    public ResponseEntity<?> getBlockedUsers(@RequestParam String userId) {
        List<BlockList> blocks = blockRepo.findByMaNguoiChan(userId);

        List<Map<String, Object>> blockedUsers = blocks.stream().map(b -> {
            User blockedUser = userRepo.findById(b.getMaNguoiBiChan()).orElse(null);
            
            Map<String, Object> userMap = new java.util.HashMap<>();
            userMap.put("id", b.getMaNguoiBiChan());
            userMap.put("name", blockedUser != null ? blockedUser.getTen() : b.getMaNguoiBiChan());
            userMap.put("avatar", blockedUser != null && blockedUser.getAvatar() != null ? 
                blockedUser.getAvatar() : "");
            userMap.put("blockedAt", b.getNgayChan());
            
            return userMap;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(blockedUsers);
    }

    @GetMapping("/blocked-by")
    public ResponseEntity<?> getBlockedBy(@RequestParam String userId) {
        List<BlockList> blocks = blockRepo.findByMaNguoiBiChan(userId);

        List<Map<String, Object>> blockers = blocks.stream().map(b -> {
            Map<String, Object> blockerMap = new java.util.HashMap<>();
            blockerMap.put("id", b.getMaNguoiChan());
            return blockerMap;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(blockers);
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkBlocked(
            @RequestParam String blockerId,
            @RequestParam String blockedId
    ) {
        boolean isBlocked = blockRepo.existsByMaNguoiChanAndMaNguoiBiChan(blockerId, blockedId);
        return ResponseEntity.ok(Map.of("isBlocked", isBlocked));
    }
}