package bookcircle.controller;

import bookcircle.dto.RoomDtos;
import bookcircle.service.RoomService;
import bookcircle.util.AuthUtil;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    public RoomDtos.RoomResponse create(@Valid @RequestBody RoomDtos.CreateRoomRequest req) {
        return roomService.createRoom(AuthUtil.principal().userId(), req);
    }

    @PostMapping("/{roomId}/join")
    public void join(@PathVariable Long roomId) {
        roomService.joinRoom(AuthUtil.principal().userId(), roomId);
    }

    @GetMapping
    public List<RoomDtos.RoomResponse> getRooms(){
        return roomService.getRooms();
    }


    @GetMapping("/by-h3")
    public List<RoomDtos.RoomResponse> byH3(@RequestParam String h3Index) {
        return roomService.findByH3(h3Index);
    }

    @DeleteMapping("/{roomId}")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public void delete(@PathVariable Long roomId) {
        roomService.deleteRoom(AuthUtil.principal().userId(), roomId);
    }
}
