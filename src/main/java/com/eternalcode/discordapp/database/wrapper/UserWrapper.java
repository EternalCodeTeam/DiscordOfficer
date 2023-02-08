package com.eternalcode.discordapp.database.wrapper;

import com.eternalcode.discordapp.database.model.User;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "users")
public class UserWrapper {

    @DatabaseField(id = true)
    private Long id;

    @DatabaseField
    private String username;

    public UserWrapper() {
    }

    public UserWrapper(Long id, String username) {
        this.username = username;
    }

    public static UserWrapper from(User user) {
        return new UserWrapper(user.getId(), user.getUsername());
    }

    public User toUser() {
        return new User(this.id, this.username);
    }
}
