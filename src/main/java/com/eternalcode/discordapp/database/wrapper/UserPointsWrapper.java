package com.eternalcode.discordapp.database.wrapper;

import com.eternalcode.discordapp.database.model.UserPoints;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "user_points")
public class UserPointsWrapper {

        @DatabaseField(id = true)
        private Long id;
        private Long points;

        @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "user_id")
        private UserWrapper user;

        public UserPointsWrapper() {
        }

        public UserPointsWrapper(Long id, Long points, UserWrapper user) {
            this.id = id;
            this.points = points;
            this.user = user;
        }

        public static UserPointsWrapper from(UserPoints userPoints) {
            return new UserPointsWrapper(userPoints.getId(), userPoints.getPoints(), UserWrapper.from(userPoints.getUser()));
        }

        public UserPoints toUserPoints() {
            return new UserPoints(this.id, this.points, this.user.toUser());
        }
}
