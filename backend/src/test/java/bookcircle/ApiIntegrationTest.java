package bookcircle;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void user_can_update_profile_and_change_password() throws Exception {
        String email = uniqueEmail("reader");
        String password = "secret123";

        String token = registerAndGetToken(email, password);

        JsonNode initialProfile = getMe(token);
        assertEquals(email, initialProfile.get("email").asText());
        assertEquals("USER", initialProfile.get("role").asText());

        String updatedEmail = uniqueEmail("reader-new");
        MvcResult updateProfileResult = mockMvc.perform(
                        patch("/users/me")
                                .header("Authorization", bearer(token))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(Map.of(
                                        "email", updatedEmail,
                                        "nickname", "reader_nick",
                                        "phoneNumber", "+77001234567"
                                )))
                ).andExpect(status().isOk())
                .andReturn();

        JsonNode updatedProfile = objectMapper.readTree(updateProfileResult.getResponse().getContentAsString());
        assertEquals(updatedEmail, updatedProfile.get("email").asText());
        assertEquals("reader_nick", updatedProfile.get("nickname").asText());
        assertEquals("+77001234567", updatedProfile.get("phoneNumber").asText());

        mockMvc.perform(
                        patch("/users/me")
                                .header("Authorization", bearer(token))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(Map.of("currentPassword", password)))
                ).andExpect(status().isBadRequest());

        String newPassword = "secret456";
        mockMvc.perform(
                        patch("/users/me")
                                .header("Authorization", bearer(token))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(Map.of(
                                        "currentPassword", password,
                                        "newPassword", newPassword
                                )))
                ).andExpect(status().isOk());

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(Map.of(
                                        "email", updatedEmail,
                                        "password", password
                                )))
                ).andExpect(status().isUnauthorized());

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(Map.of(
                                        "email", updatedEmail,
                                        "password", newPassword
                                )))
                ).andExpect(status().isOk());
    }

    @Test
    void room_member_can_see_all_members_and_their_progress() throws Exception {
        String adminToken = loginAndGetToken("admin@bookcircle", "admin123");
        Long bookId = createBookAndGetId(adminToken, "Test Book " + UUID.randomUUID());

        String user1Email = uniqueEmail("u1");
        String user2Email = uniqueEmail("u2");
        String outsiderEmail = uniqueEmail("outsider");
        String userPassword = "secret123";

        String user1Token = registerAndGetToken(user1Email, userPassword);
        String user2Token = registerAndGetToken(user2Email, userPassword);
        String outsiderToken = registerAndGetToken(outsiderEmail, userPassword);

        MvcResult createRoomResult = mockMvc.perform(
                        post("/rooms")
                                .header("Authorization", bearer(user1Token))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(Map.of(
                                        "name", "Room " + UUID.randomUUID(),
                                        "bookId", bookId,
                                        "h3Index", "8928308280fffff"
                                )))
                ).andExpect(status().isOk())
                .andReturn();

        Long roomId = objectMapper.readTree(createRoomResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(
                        post("/rooms/{roomId}/join", roomId)
                                .header("Authorization", bearer(user2Token))
                ).andExpect(status().isOk());

        mockMvc.perform(
                        post("/progress")
                                .header("Authorization", bearer(user1Token))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(Map.of(
                                        "roomId", roomId,
                                        "chapterNumber", 5
                                )))
                ).andExpect(status().isOk());

        mockMvc.perform(
                        post("/progress")
                                .header("Authorization", bearer(user2Token))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(Map.of(
                                        "roomId", roomId,
                                        "chapterNumber", 3
                                )))
                ).andExpect(status().isOk());

        MvcResult membersProgressResult = mockMvc.perform(
                        get("/rooms/{roomId}/members/progress", roomId)
                                .header("Authorization", bearer(user1Token))
                ).andExpect(status().isOk())
                .andReturn();

        JsonNode membersProgress = objectMapper.readTree(membersProgressResult.getResponse().getContentAsString());
        assertEquals(roomId.longValue(), membersProgress.get("roomId").asLong());
        assertEquals(2, membersProgress.get("members").size());

        Map<String, Integer> chapterByEmail = new HashMap<>();
        for (JsonNode member : membersProgress.get("members")) {
            chapterByEmail.put(member.get("email").asText(), member.get("chapterNumber").asInt());
        }
        assertEquals(5, chapterByEmail.get(user1Email));
        assertEquals(3, chapterByEmail.get(user2Email));

        mockMvc.perform(
                        get("/rooms/{roomId}/members/progress", roomId)
                                .header("Authorization", bearer(outsiderToken))
                ).andExpect(status().isForbidden());
    }

    private String registerAndGetToken(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(
                        post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(Map.of("email", email, "password", password)))
                ).andExpect(status().isOk())
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertTrue(body.hasNonNull("accessToken"));
        return body.get("accessToken").asText();
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(Map.of("email", email, "password", password)))
                ).andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    private Long createBookAndGetId(String token, String title) throws Exception {
        MvcResult result = mockMvc.perform(
                        post("/books")
                                .header("Authorization", bearer(token))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(Map.of(
                                        "title", title,
                                        "author", "Test Author",
                                        "totalChapters", 20
                                )))
                ).andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private JsonNode getMe(String token) throws Exception {
        MvcResult result = mockMvc.perform(
                        get("/users/me")
                                .header("Authorization", bearer(token))
                ).andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private String uniqueEmail(String prefix) {
        return prefix + "+" + UUID.randomUUID().toString().replace("-", "") + "@example.com";
    }

    private String toJson(Object body) throws Exception {
        return objectMapper.writeValueAsString(body);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
