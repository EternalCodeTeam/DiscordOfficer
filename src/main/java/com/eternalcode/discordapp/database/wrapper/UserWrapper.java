package com.eternalcode.discordapp.database.wrapper;

import com.eternalcode.discordapp.database.model.User;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "edc_users")
public class UserWrapper {

    @DatabaseField(id = true)
    private Long id;

    public UserWrapper() {
    }

    public UserWrapper(Long id) {
        this.id = id;
    }

    public static UserWrapper from(User user) {
        return new UserWrapper(user.getId());
    }

    public User toUser() {
        return new User(this.id);
    }
}
