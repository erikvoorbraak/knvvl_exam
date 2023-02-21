package org.knvvl.exam.rest;

import static org.knvvl.exam.rest.QuestionRestService.GSON;
import static org.knvvl.exam.services.Utils.getAsString;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

import org.knvvl.exam.entities.User;
import org.knvvl.exam.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@RestController
@RequestMapping("api")
public class UserRestService
{
    @Autowired
    private UserService userService;

    @GetMapping(value = "/users", produces = APPLICATION_JSON_VALUE)
    String getUsers()
    {
        JsonArray all = new JsonArray();
        userService.findAll().stream().map(UserRestService::toJson).forEach(all::add);
        return GSON.toJson(all);
    }

    @GetMapping(value = "/users/me", produces = APPLICATION_JSON_VALUE)
    String getCurrentUser()
    {
        JsonObject json = toJson(userService.getCurrentUser());
        return GSON.toJson(json);
    }

    private static JsonObject toJson(User user)
    {
        JsonObject json = new JsonObject();
        json.addProperty("id", user.getId());
        json.addProperty("username", user.getUsername());
        json.addProperty("email", user.getEmail());
        json.addProperty("url", "/api/users/" + user.getId());
        return json;
    }

    @PostMapping(path = "users", consumes = APPLICATION_JSON_VALUE, produces = TEXT_PLAIN_VALUE)
    public ResponseEntity<String> createUser(@RequestBody String body)
    {
        JsonObject form = GSON.fromJson(body, JsonObject.class);
        String username = getAsString(form, "username");
        if (username.length() < 6)
            return ResponseEntity.status(BAD_REQUEST).body("Username must be at least 6 characters");
        if (username.length() > 50)
            return ResponseEntity.status(BAD_REQUEST).body("Username must be at most 50 characters");
        String password = getAsString(form, "password");
        String email = getAsString(form, "email");
        String msg = userService.validatePassword(password);
        if (msg != null)
            return ResponseEntity.status(BAD_REQUEST).body(msg);
        userService.addUser(username, password, email);
        return ResponseEntity.status(OK).body(null);
    }

    @PostMapping(path = "users/me", consumes = APPLICATION_JSON_VALUE, produces = TEXT_PLAIN_VALUE)
    public ResponseEntity<String> updateUser(@RequestBody String body)
    {
        JsonObject form = GSON.fromJson(body, JsonObject.class);
        User user = userService.getCurrentUser();
        String password = getAsString(form, "password");
        String email = getAsString(form, "email");
        if (!email.isBlank())
            user.setEmail(email);
        if (!password.isBlank()) {
            String msg = userService.validatePassword(password);
            if (msg != null)
                return ResponseEntity.status(BAD_REQUEST).body(msg);
        }
        userService.saveUser(user, password);
        return ResponseEntity.status(OK).body(null);
    }
}
