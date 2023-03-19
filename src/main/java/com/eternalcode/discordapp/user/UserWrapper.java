package com.eternalcode.discordapp.user;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "edc_users")
class UserWrapper {

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
